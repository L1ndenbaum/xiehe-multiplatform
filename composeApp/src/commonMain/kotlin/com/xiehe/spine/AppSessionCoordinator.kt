package com.xiehe.spine

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AppContainer
import com.xiehe.spine.ui.components.feedback.shared.FloatingToast
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.viewmodel.auth.LoginViewModel
import com.xiehe.spine.ui.viewmodel.auth.RegisterViewModel
import com.xiehe.spine.ui.viewmodel.profile.AppearanceViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

@Composable
internal fun AppSessionCoordinator(
    container: AppContainer,
    showNetworkDiagnostics: Boolean,
) {
    val loginVm = remember(container) { LoginViewModel() }
    val registerVm = remember(container) { RegisterViewModel() }
    val appearanceVm = remember(container) { AppearanceViewModel(container.themeRepository) }

    var session by remember(container) {
        mutableStateOf<UserSession?>(container.authRepository.restoreSession())
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var route by remember { mutableStateOf<OverlayRoute?>(null) }
    var authRoute by remember { mutableStateOf(AuthRoute.LOGIN) }
    var dashboardBootstrap by remember(session?.userId) { mutableStateOf(DashboardBootstrapState()) }
    var sessionExpiredMessage by remember { mutableStateOf<String?>(null) }
    var handlingSessionExpiry by remember { mutableStateOf(false) }

    val onTabSelected: (Int) -> Unit = remember {
        { tab ->
            selectedTab = tab
            route = null
        }
    }

    val onSessionEvent: (SessionEvent) -> Unit = { event ->
        when (event) {
            is SessionEvent.SessionExpired -> {
                if (session != null && !handlingSessionExpiry) {
                    sessionExpiredMessage = event.message
                    handlingSessionExpiry = true
                }
            }
        }
    }

    suspend fun resetToLogin(clearRemoteSession: Boolean, activeSession: UserSession? = session) {
        if (clearRemoteSession) {
            if (activeSession != null) {
                container.authRepository.logout(activeSession)
            } else {
                container.authRepository.logout()
            }
        } else {
            container.authRepository.logout()
        }
        session = null
        route = null
        selectedTab = 0
        authRoute = AuthRoute.LOGIN
        dashboardBootstrap = DashboardBootstrapState()
        sessionExpiredMessage = null
        handlingSessionExpiry = false
    }

    if (session == null) {
        AuthHost(
            authRoute = authRoute,
            loginVm = loginVm,
            registerVm = registerVm,
            authRepository = container.authRepository,
            showNetworkDiagnostics = showNetworkDiagnostics,
            onAuthRouteChange = { authRoute = it },
            onLogin = {
                session = it
                selectedTab = 0
                route = null
                authRoute = AuthRoute.LOGIN
                dashboardBootstrap = DashboardBootstrapState()
            },
        )
        return
    }

    val activeSession = requireNotNull(session)
    val scopedViewModels = rememberSessionScopedViewModels(activeSession.userId)

    PlatformBackHandler(enabled = route != null) {
        route = null
    }

    LaunchedEffect(activeSession.refreshToken, activeSession.accessTokenExpiresAtEpochSeconds) {
        while (true) {
            val current = session ?: break
            val expiresAt = current.accessTokenExpiresAtEpochSeconds ?: break
            val waitSeconds = (expiresAt - currentEpochSeconds() - 120L).coerceAtLeast(30L)
            delay(waitSeconds * 1000L)
            val latest = session ?: break
            when (val result = container.authRepository.ensureFreshSession(latest)) {
                is AppResult.Success -> {
                    if (result.data != latest) {
                        session = result.data
                    }
                }

                is AppResult.Failure -> {
                    if (result.isUnauthorized) {
                        onSessionEvent(result.asSessionExpiredEvent())
                        break
                    }
                }
            }
        }
    }

    LaunchedEffect(activeSession.accessToken) {
        val current = session ?: return@LaunchedEffect
        when (val result = container.authRepository.getCurrentUser(current)) {
            is AppResult.Success -> {
                val updated = result.data.first
                if (updated != current) {
                    session = updated
                }
            }

            is AppResult.Failure -> {
                if (result.isUnauthorized) {
                    onSessionEvent(result.asSessionExpiredEvent())
                }
            }
        }
    }

    LaunchedEffect(activeSession.userId) {
        val current = session ?: return@LaunchedEffect
        dashboardBootstrap = DashboardBootstrapState(loading = true)

        val patientsDeferred = async { container.patientRepository.loadAllPatients(current) }
        val imagesDeferred = async { container.imageFileRepository.loadAllImageFiles(current) }

        val patientsResult = patientsDeferred.await()
        val imagesResult = imagesDeferred.await()

        val latestSession = listOfNotNull(
            (patientsResult as? AppResult.Success)?.data?.first,
            (imagesResult as? AppResult.Success)?.data?.first,
        ).lastOrNull() ?: current

        if (latestSession != current) {
            session = latestSession
        }

        val unauthorized = listOf(patientsResult, imagesResult).any { result ->
            result is AppResult.Failure && result.isUnauthorized
        }
        if (unauthorized) {
            onSessionEvent(SessionEvent.SessionExpired())
            return@LaunchedEffect
        }

        dashboardBootstrap = DashboardBootstrapState(
            loading = false,
            patients = (patientsResult as? AppResult.Success)?.data?.second.orEmpty(),
            images = (imagesResult as? AppResult.Success)?.data?.second.orEmpty(),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = route,
            transitionSpec = {
                val noOverlayTransition = initialState == null && targetState == null
                if (noOverlayTransition) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 6 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 7 },
                        )
                }
            },
            label = "scene_transition",
        ) { currentRoute ->
            if (currentRoute == null) {
                MainShellHost(
                    session = activeSession,
                    container = container,
                    scopedViewModels = scopedViewModels,
                    selectedTab = selectedTab,
                    dashboardBootstrap = dashboardBootstrap,
                    onTabSelected = onTabSelected,
                    onSessionUpdated = { session = it },
                    onLogoutRequested = { resetToLogin(clearRemoteSession = true, activeSession = activeSession) },
                    onRouteChange = { route = it },
                    onSessionExpired = { onSessionEvent(SessionEvent.SessionExpired(it)) },
                )
            } else {
                OverlayHost(
                    route = currentRoute,
                    session = activeSession,
                    container = container,
                    scopedViewModels = scopedViewModels,
                    appearanceVm = appearanceVm,
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    onRouteChange = { route = it },
                    onSessionUpdated = { session = it },
                    onSessionExpired = { onSessionEvent(SessionEvent.SessionExpired(it)) },
                )
            }
        }

        sessionExpiredMessage?.let { message ->
            FloatingToast(
                message = message,
                accentColor = com.xiehe.spine.ui.theme.SpineTheme.colors.warning,
                icon = IconToken.MESSAGE,
                durationMillis = 1800L,
                onDismiss = { sessionExpiredMessage = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp),
            )
        }
    }

    LaunchedEffect(handlingSessionExpiry, sessionExpiredMessage) {
        if (!handlingSessionExpiry || sessionExpiredMessage == null) {
            return@LaunchedEffect
        }
        delay(1800L)
        resetToLogin(clearRemoteSession = false, activeSession = session)
    }

    LaunchedEffect(session?.accessToken) {
        if (session == null) {
            selectedTab = 0
            route = null
            authRoute = AuthRoute.LOGIN
        }
    }
}
