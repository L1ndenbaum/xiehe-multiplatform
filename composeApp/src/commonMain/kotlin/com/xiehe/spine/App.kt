package com.xiehe.spine

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AppContainer
import com.xiehe.spine.ui.screens.AppearanceScreen
import com.xiehe.spine.ui.screens.DashboardScreen
import com.xiehe.spine.ui.screens.LoginScreen
import com.xiehe.spine.ui.screens.MobileShell
import com.xiehe.spine.ui.screens.PatientDetailScreen
import com.xiehe.spine.ui.screens.PatientFormScreen
import com.xiehe.spine.ui.screens.PatientsScreen
import com.xiehe.spine.ui.screens.PlaceholderScreen
import com.xiehe.spine.ui.screens.ProfileScreen
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.AppearanceViewModel
import com.xiehe.spine.ui.viewmodel.DashboardViewModel
import com.xiehe.spine.ui.viewmodel.LoginViewModel
import com.xiehe.spine.ui.viewmodel.PatientDetailViewModel
import com.xiehe.spine.ui.viewmodel.PatientFormViewModel
import com.xiehe.spine.ui.viewmodel.PatientsViewModel
import kotlinx.coroutines.delay

private sealed interface OverlayRoute {
    data class PatientDetail(val patientId: Int) : OverlayRoute
    data object PatientForm : OverlayRoute
    data class PatientEdit(val patientId: Int) : OverlayRoute
    data object Appearance : OverlayRoute
    data object PersonalInfo : OverlayRoute
    data object ChangePassword : OverlayRoute
}

private data class AppScene(
    val tab: Int,
    val route: OverlayRoute?,
)

@Composable
@Preview
fun App(
    container: AppContainer? = null,
    showNetworkDiagnostics: Boolean = false,
) {
    val appContainer = remember(container) { container ?: AppContainer.createInMemory() }
    val loginVm = remember { LoginViewModel() }
    val dashboardVm = remember { DashboardViewModel() }
    val patientsVm = remember { PatientsViewModel() }
    val patientDetailVm = remember { PatientDetailViewModel() }
    val patientFormVm = remember { PatientFormViewModel() }
    val appearanceVm = remember { AppearanceViewModel(appContainer.themeRepository) }

    var session by remember { mutableStateOf<UserSession?>(appContainer.authRepository.restoreSession()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var route by remember { mutableStateOf<OverlayRoute?>(null) }
    val onTabSelected: (Int) -> Unit = remember {
        { tab ->
            selectedTab = tab
            route = null
        }
    }

    val themePreference by appContainer.themeRepository.preference.collectAsState()

    SpineTheme(preference = themePreference) {
        if (session == null) {
            LoginScreen(
                vm = loginVm,
                onLogin = {
                    loginVm.submit(appContainer.authRepository) {
                        session = it
                        selectedTab = 0
                        route = null
                    }
                },
                onHealthCheck = {
                    loginVm.checkConnectivity(appContainer.authRepository)
                },
                showNetworkDiagnostics = showNetworkDiagnostics,
            )
            return@SpineTheme
        }

        PlatformBackHandler(enabled = route != null) {
            route = null
        }

        LaunchedEffect(session?.refreshToken, session?.accessTokenExpiresAtEpochSeconds) {
            while (true) {
                val active = session ?: break
                val expiresAt = active.accessTokenExpiresAtEpochSeconds ?: break
                val waitSeconds = (expiresAt - currentEpochSeconds() - 120L).coerceAtLeast(30L)
                delay(waitSeconds * 1000L)
                val current = session ?: break
                when (val result = appContainer.authRepository.ensureFreshSession(current)) {
                    is com.xiehe.spine.core.model.AppResult.Success -> {
                        if (result.data != current) {
                            session = result.data
                        }
                    }

                    is com.xiehe.spine.core.model.AppResult.Failure -> {
                        if (result.isUnauthorized) {
                            appContainer.authRepository.logout()
                            session = null
                            route = null
                            selectedTab = 0
                            break
                        }
                    }
                }
            }
        }

        val activeSession = session!!
        val scene = remember(selectedTab, route) { AppScene(tab = selectedTab, route = route) }
        AnimatedContent(
            targetState = scene,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 8 })
                    .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 10 })
                    .using(SizeTransform(clip = false))
            },
            label = "scene_transition",
        ) { currentScene ->
            if (currentScene.route == null) {
                when (currentScene.tab) {
                    0 -> MobileShell(
                        title = "工作台",
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        rightActionGlyph = SpineGlyph.BELL,
                    ) {
                        DashboardScreen(
                            vm = dashboardVm,
                            session = activeSession,
                            repository = appContainer.dashboardRepository,
                            onSessionUpdated = { session = it },
                        )
                    }

                    1 -> MobileShell(
                        title = "患者中心",
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        rightActionGlyph = SpineGlyph.ADD,
                        onRightAction = { route = OverlayRoute.PatientForm },
                    ) {
                        PatientsScreen(
                            vm = patientsVm,
                            session = activeSession,
                            repository = appContainer.patientRepository,
                            onSessionUpdated = { session = it },
                            onAddPatient = { route = OverlayRoute.PatientForm },
                            onOpenPatient = { route = OverlayRoute.PatientDetail(it) },
                            onEditPatient = { route = OverlayRoute.PatientEdit(it) },
                        )
                    }

                    2 -> MobileShell(
                        title = "影像中心",
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        rightActionGlyph = SpineGlyph.ADD,
                    ) {
                        PlaceholderScreen(
                            title = "影像模块建设中",
                            description = "当前后端上传与测量接口尚不稳定，已预留页面路由与架构扩展点。",
                        )
                    }

                    else -> MobileShell(
                        title = "个人中心",
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                    ) {
                        ProfileScreen(
                            session = activeSession,
                            onOpenAppearance = { route = OverlayRoute.Appearance },
                            onOpenPersonalInfo = { route = OverlayRoute.PersonalInfo },
                            onOpenChangePassword = { route = OverlayRoute.ChangePassword },
                            onLogout = {
                                appContainer.authRepository.logout()
                                session = null
                                route = null
                                selectedTab = 0
                            },
                        )
                    }
                }
            } else {
                val currentRoute = requireNotNull(currentScene.route)
                when (val current = currentRoute) {
                    is OverlayRoute.PatientDetail -> {
                        MobileShell(
                            title = "患者信息",
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            onBack = { route = null },
                        ) {
                            PatientDetailScreen(
                                patientId = current.patientId,
                                vm = patientDetailVm,
                                session = activeSession,
                                repository = appContainer.patientRepository,
                                onSessionUpdated = { session = it },
                            )
                        }
                    }

                    OverlayRoute.PatientForm -> {
                        MobileShell(
                            title = "添加患者",
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            onBack = { route = null },
                        ) {
                            PatientFormScreen(
                                vm = patientFormVm,
                                session = activeSession,
                                repository = appContainer.patientRepository,
                                onSessionUpdated = { session = it },
                                onSubmitSuccess = {
                                    route = null
                                    selectedTab = 1
                                },
                            )
                        }
                    }

                    OverlayRoute.Appearance -> {
                        MobileShell(
                            title = "外观设置",
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            onBack = { route = null },
                        ) {
                            AppearanceScreen(vm = appearanceVm)
                        }
                    }

                    is OverlayRoute.PatientEdit -> {
                        MobileShell(
                            title = "编辑患者",
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            onBack = { route = null },
                        ) {
                            PlaceholderScreen(
                                title = "编辑接口待完善",
                                description = "患者ID: ${current.patientId}。页面入口已接好，后端编辑接口可用后直接接入。",
                            )
                        }
                    }

                    OverlayRoute.PersonalInfo -> {
                        MobileShell(
                            title = "个人信息",
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            onBack = { route = null },
                        ) {
                            PlaceholderScreen(
                                title = "个人信息接口待完善",
                                description = "页面已就绪，等后端接口稳定后可直接接入。",
                            )
                        }
                    }

                    OverlayRoute.ChangePassword -> {
                        MobileShell(
                            title = "修改密码",
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            onBack = { route = null },
                        ) {
                            PlaceholderScreen(
                                title = "修改密码接口待完善",
                                description = "架构与导航已预留，后续可在不改路由的情况下直接接入。",
                            )
                        }
                    }

                }
            }
        }

        LaunchedEffect(session?.accessToken) {
            if (session == null) {
                selectedTab = 0
                route = null
            }
        }
    }
}
