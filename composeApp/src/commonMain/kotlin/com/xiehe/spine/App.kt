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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AppContainer
import com.xiehe.spine.data.image.ImageFileSummary
import com.xiehe.spine.data.patient.PatientSummary
import com.xiehe.spine.ui.components.feedback.shared.AppStartupScreen
import com.xiehe.spine.ui.components.navigation.shared.DashboardShellHeader
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.navigation.shared.SearchShellHeader
import com.xiehe.spine.ui.components.navigation.shared.SimpleShellHeader
import com.xiehe.spine.ui.screens.profile.AppearanceScreen
import com.xiehe.spine.ui.screens.profile.ChangePasswordScreen
import com.xiehe.spine.ui.screens.dashboard.DashboardScreen
import com.xiehe.spine.ui.screens.image.ImageAnalysisScreen
import com.xiehe.spine.ui.screens.image.ImageUploadScreen
import com.xiehe.spine.ui.screens.image.ImagesScreen
import com.xiehe.spine.ui.screens.auth.LoginScreen
import com.xiehe.spine.ui.screens.message.MessagesScreen
import com.xiehe.spine.ui.screens.shared.MobileShell
import com.xiehe.spine.ui.screens.patient.PatientDetailScreen
import com.xiehe.spine.ui.screens.patient.PatientEditScreen
import com.xiehe.spine.ui.screens.patient.PatientFormScreen
import com.xiehe.spine.ui.screens.patient.PatientsScreen
import com.xiehe.spine.ui.screens.profile.PersonalInfoScreen
import com.xiehe.spine.ui.screens.profile.ProfileScreen
import com.xiehe.spine.ui.screens.auth.RegisterScreen
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.profile.AppearanceViewModel
import com.xiehe.spine.ui.viewmodel.dashboard.DashboardViewModel
import com.xiehe.spine.ui.viewmodel.image.ImageAnalysisViewModel
import com.xiehe.spine.ui.viewmodel.image.ImageUploadViewModel
import com.xiehe.spine.ui.viewmodel.image.ImagesViewModel
import com.xiehe.spine.ui.viewmodel.auth.LoginViewModel
import com.xiehe.spine.ui.viewmodel.message.MessagesViewModel
import com.xiehe.spine.ui.viewmodel.patient.PatientDetailViewModel
import com.xiehe.spine.ui.viewmodel.patient.PatientEditViewModel
import com.xiehe.spine.ui.viewmodel.patient.PatientFormViewModel
import com.xiehe.spine.ui.viewmodel.patient.PatientsViewModel
import com.xiehe.spine.ui.viewmodel.profile.PersonalInfoViewModel
import com.xiehe.spine.ui.viewmodel.auth.RegisterViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

private enum class AuthRoute {
    LOGIN,
    REGISTER,
}

private sealed interface OverlayRoute {
    data class PatientDetail(val patientId: Int) : OverlayRoute
    data class ImageAnalysis(
        val fileId: Int,
        val patientId: Int?,
        val examType: String,
    ) : OverlayRoute

    data object PatientForm : OverlayRoute
    data class PatientEdit(val patientId: Int) : OverlayRoute
    data object Appearance : OverlayRoute
    data object PersonalInfo : OverlayRoute
    data object ChangePassword : OverlayRoute
    data object Messages : OverlayRoute
    data object ImageUpload : OverlayRoute
}

private data class DashboardBootstrapState(
    val loading: Boolean = true,
    val patients: List<PatientSummary> = emptyList(),
    val images: List<ImageFileSummary> = emptyList(),
)

@Composable
@Preview
fun App(
    container: AppContainer? = null,
    showNetworkDiagnostics: Boolean = false,
) {
    val appContainer = remember(container) { container ?: AppContainer.createInMemory() }
    val loginVm = remember { LoginViewModel() }
    val registerVm = remember { RegisterViewModel() }
    val messagesVm = remember { MessagesViewModel() }
    val dashboardVm = remember { DashboardViewModel() }
    val imagesVm = remember { ImagesViewModel() }
    val imageAnalysisVm = remember { ImageAnalysisViewModel() }
    val imageUploadVm = remember { ImageUploadViewModel() }
    val patientsVm = remember { PatientsViewModel() }
    val patientDetailVm = remember { PatientDetailViewModel() }
    val patientEditVm = remember { PatientEditViewModel() }
    val patientFormVm = remember { PatientFormViewModel() }
    val personalInfoVm = remember { PersonalInfoViewModel() }
    val appearanceVm = remember { AppearanceViewModel(appContainer.themeRepository) }

    var session by remember { mutableStateOf<UserSession?>(appContainer.authRepository.restoreSession()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var route by remember { mutableStateOf<OverlayRoute?>(null) }
    var authRoute by remember { mutableStateOf(AuthRoute.LOGIN) }
    var dashboardBootstrap by remember(session?.userId) { mutableStateOf(DashboardBootstrapState()) }

    val onTabSelected: (Int) -> Unit = remember {
        { tab ->
            selectedTab = tab
            route = null
        }
    }

    val themePreference by appContainer.themeRepository.preference.collectAsState()
    val patientsState by patientsVm.state.collectAsState()
    val imagesState by imagesVm.state.collectAsState()

    SpineTheme(preference = themePreference) {
        if (session == null) {
            PlatformBackHandler(enabled = authRoute == AuthRoute.REGISTER) {
                authRoute = AuthRoute.LOGIN
            }

            when (authRoute) {
                AuthRoute.LOGIN -> LoginScreen(
                    vm = loginVm,
                    onLogin = {
                        loginVm.submit(appContainer.authRepository) {
                            session = it
                            selectedTab = 0
                            route = null
                            authRoute = AuthRoute.LOGIN
                        }
                    },
                    onHealthCheck = {
                        loginVm.checkConnectivity(appContainer.authRepository)
                    },
                    showNetworkDiagnostics = showNetworkDiagnostics,
                    onOpenRegister = { authRoute = AuthRoute.REGISTER },
                )

                AuthRoute.REGISTER -> RegisterScreen(
                    vm = registerVm,
                    authRepository = appContainer.authRepository,
                    onBackToLogin = { authRoute = AuthRoute.LOGIN },
                )
            }
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
                    is AppResult.Success -> {
                        if (result.data != current) {
                            session = result.data
                        }
                    }

                    is AppResult.Failure -> {
                        if (result.isUnauthorized) {
                            appContainer.authRepository.logout()
                            session = null
                            route = null
                            selectedTab = 0
                            authRoute = AuthRoute.LOGIN
                            break
                        }
                    }
                }
            }
        }

        LaunchedEffect(session?.accessToken) {
            val current = session ?: return@LaunchedEffect
            when (val result = appContainer.authRepository.getCurrentUser(current)) {
                is AppResult.Success -> {
                    val updated = result.data.first
                    if (updated != current) {
                        session = updated
                    }
                }

                is AppResult.Failure -> {
                    if (result.isUnauthorized) {
                        appContainer.authRepository.logout()
                        session = null
                        route = null
                        selectedTab = 0
                        authRoute = AuthRoute.LOGIN
                    }
                }
            }
        }

        LaunchedEffect(session?.userId) {
            val current = session ?: return@LaunchedEffect
            dashboardBootstrap = DashboardBootstrapState(loading = true)

            val patientsDeferred = async { appContainer.patientRepository.loadAllPatients(current) }
            val imagesDeferred = async { appContainer.imageFileRepository.loadAllImageFiles(current) }

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
                appContainer.authRepository.logout()
                session = null
                route = null
                selectedTab = 0
                authRoute = AuthRoute.LOGIN
                return@LaunchedEffect
            }

            dashboardBootstrap = DashboardBootstrapState(
                loading = false,
                patients = (patientsResult as? AppResult.Success)?.data?.second.orEmpty(),
                images = (imagesResult as? AppResult.Success)?.data?.second.orEmpty(),
            )
        }

        val activeSession = session!!
        AnimatedContent(
            targetState = route,
            transitionSpec = {
                val noOverlayTransition = initialState == null && targetState == null
                if (noOverlayTransition) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 6 })
                        .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 7 })
                }
            },
            label = "scene_transition",
        ) { currentRoute ->
            if (currentRoute == null) {
                if (selectedTab == 0 && dashboardBootstrap.loading) {
                    AppStartupScreen(
                        title = "正在同步工作台数据",
                        message = "先加载患者与影像数据，再进入 Dashboard",
                    )
                    return@AnimatedContent
                }
                MobileShell(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    headerContent = {
                        when (selectedTab) {
                            0 -> DashboardShellHeader(
                                userName = activeSession.fullName ?: activeSession.username,
                                primaryMeta = "工作台",
                                secondaryMeta = "脊柱影像分析系统",
                                onMessages = { route = OverlayRoute.Messages },
                            )

                            1 -> SearchShellHeader(
                                title = "患者中心",
                                subtitle = "${patientsState.items.size} 位患者",
                                searchValue = patientsState.search,
                                onSearchValueChange = patientsVm::updateSearch,
                                searchPlaceholder = "搜索患者姓名、ID或手机号",
                                actionGlyph = IconToken.USER_PLUS,
                                onAction = { route = OverlayRoute.PatientForm },
                            )

                            2 -> SearchShellHeader(
                                title = "影像中心",
                                subtitle = "${imagesState.filteredItems.size} 份影像",
                                searchValue = imagesState.search,
                                onSearchValueChange = imagesVm::updateSearch,
                                searchPlaceholder = "搜索患者姓名、检查类型或文件名",
                                actionGlyph = IconToken.UPLOAD,
                                onAction = { route = OverlayRoute.ImageUpload },
                            )

                            else -> SimpleShellHeader(
                                title = "个人中心",
                                subtitle = activeSession.fullName ?: activeSession.username,
                            )
                        }
                    },
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val direction = if (targetState >= initialState) 1 else -1
                            (fadeIn(animationSpec = tween(240)) + slideInHorizontally(animationSpec = tween(260)) { full ->
                                direction * full / 6
                            }).togetherWith(
                                fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(200)) { full ->
                                    -direction * full / 7
                                },
                            )
                        },
                        label = "main_tab_content_transition",
                    ) { tab ->
                        when (tab) {
                            0 -> DashboardScreen(
                                vm = dashboardVm,
                                session = activeSession,
                                dashboardRepository = appContainer.dashboardRepository,
                                imageRepository = appContainer.imageFileRepository,
                                notificationRepository = appContainer.notificationRepository,
                                authRepository = appContainer.authRepository,
                                onSessionUpdated = { session = it },
                                preloadedPatients = dashboardBootstrap.patients,
                                preloadedImages = dashboardBootstrap.images,
                                onOpenAnalysis = { fileId, patientId, examType ->
                                    route = OverlayRoute.ImageAnalysis(
                                        fileId = fileId,
                                        patientId = patientId,
                                        examType = examType,
                                    )
                                },
                                onOpenPatientForm = { route = OverlayRoute.PatientForm },
                                onOpenImageUpload = { route = OverlayRoute.ImageUpload },
                                onOpenImagesTab = { onTabSelected(2) },
                                onOpenMessages = { route = OverlayRoute.Messages },
                            )

                            1 -> PatientsScreen(
                                vm = patientsVm,
                                session = activeSession,
                                repository = appContainer.patientRepository,
                                onSessionUpdated = { session = it },
                                onOpenPatient = { route = OverlayRoute.PatientDetail(it) },
                                onEditPatient = { route = OverlayRoute.PatientEdit(it) },
                                showInlineSearch = false,
                            )

                            2 -> ImagesScreen(
                                vm = imagesVm,
                                session = activeSession,
                                repository = appContainer.imageFileRepository,
                                onSessionUpdated = { session = it },
                                showInlineSearch = false,
                                onOpenAnalysis = { fileId, patientId, examType ->
                                    route = OverlayRoute.ImageAnalysis(
                                        fileId = fileId,
                                        patientId = patientId,
                                        examType = examType,
                                    )
                                },
                            )

                            else -> ProfileScreen(
                                session = activeSession,
                                onOpenAppearance = { route = OverlayRoute.Appearance },
                                onOpenPersonalInfo = { route = OverlayRoute.PersonalInfo },
                                onOpenChangePassword = { route = OverlayRoute.ChangePassword },
                                onLogout = {
                                    appContainer.authRepository.logout()
                                    session = null
                                    route = null
                                    selectedTab = 0
                                    authRoute = AuthRoute.LOGIN
                                },
                            )
                        }
                    }
                }
            } else {
                val current = requireNotNull(currentRoute)
                when (current) {
                    is OverlayRoute.PatientDetail -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "患者详情",
                                    subtitle = "查看和管理患者完整信息",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            PatientDetailScreen(
                                patientId = current.patientId,
                                vm = patientDetailVm,
                                session = activeSession,
                                patientRepository = appContainer.patientRepository,
                                imageRepository = appContainer.imageFileRepository,
                                onSessionUpdated = { session = it },
                                onEditPatient = { patientId ->
                                    route = OverlayRoute.PatientEdit(patientId)
                                },
                                onOpenAnalysis = { fileId, patientId, examType ->
                                    route = OverlayRoute.ImageAnalysis(
                                        fileId = fileId,
                                        patientId = patientId,
                                        examType = examType,
                                    )
                                },
                                onOpenImageUpload = { route = OverlayRoute.ImageUpload },
                            )
                        }
                    }

                    is OverlayRoute.ImageAnalysis -> {
                        ImageAnalysisScreen(
                            fileId = current.fileId,
                            patientId = current.patientId,
                            examType = current.examType,
                            vm = imageAnalysisVm,
                            session = activeSession,
                            imageRepository = appContainer.imageFileRepository,
                            measurementRepository = appContainer.measurementRepository,
                            aiRepository = appContainer.aiInferenceRepository,
                            onSessionUpdated = { session = it },
                            onBack = { route = null },
                        )
                    }

                    OverlayRoute.PatientForm -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "添加患者",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
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

                    is OverlayRoute.PatientEdit -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "编辑患者",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            PatientEditScreen(
                                patientId = current.patientId,
                                vm = patientEditVm,
                                session = activeSession,
                                repository = appContainer.patientRepository,
                                onSessionUpdated = { session = it },
                                onSubmitSuccess = {
                                    route = OverlayRoute.PatientDetail(current.patientId)
                                },
                            )
                        }
                    }

                    OverlayRoute.Appearance -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "外观设置",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            AppearanceScreen(vm = appearanceVm)
                        }
                    }

                    OverlayRoute.PersonalInfo -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "个人信息",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            PersonalInfoScreen(
                                vm = personalInfoVm,
                                session = activeSession,
                                authRepository = appContainer.authRepository,
                                onSessionUpdated = { session = it },
                            )
                        }
                    }

                    OverlayRoute.ChangePassword -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "修改密码",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            ChangePasswordScreen()
                        }
                    }

                    OverlayRoute.Messages -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "消息中心",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            MessagesScreen(
                                vm = messagesVm,
                                session = activeSession,
                                repository = appContainer.notificationRepository,
                                onSessionUpdated = { session = it },
                            )
                        }
                    }

                    OverlayRoute.ImageUpload -> {
                        MobileShell(
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                            headerContent = {
                                SimpleShellHeader(
                                    title = "上传影像",
                                    leadingGlyph = IconToken.BACK,
                                    onLeadingAction = { route = null },
                                )
                            },
                        ) {
                            ImageUploadScreen(
                                vm = imageUploadVm,
                                session = activeSession,
                                patientRepository = appContainer.patientRepository,
                                imageRepository = appContainer.imageFileRepository,
                                onSessionUpdated = { session = it },
                                onUploadSuccess = {
                                    route = null
                                    selectedTab = 2
                                    val latestSession = session ?: activeSession
                                    imagesVm.refresh(
                                        session = latestSession,
                                        repository = appContainer.imageFileRepository,
                                        onSessionUpdated = { updated -> session = updated },
                                    )
                                },
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
                authRoute = AuthRoute.LOGIN
            }
        }
    }
}
