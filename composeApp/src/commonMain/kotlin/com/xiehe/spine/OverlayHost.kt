package com.xiehe.spine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AppContainer
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.navigation.shared.HeaderTextAction
import com.xiehe.spine.ui.components.navigation.shared.SimpleShellHeader
import com.xiehe.spine.ui.motion.AppConfirmDialogHost
import com.xiehe.spine.ui.motion.AppOverlayEntryHost
import com.xiehe.spine.ui.motion.AppRouteContentHost
import com.xiehe.spine.ui.screens.image.ImageAnalysisScreen
import com.xiehe.spine.ui.screens.image.ImageUploadScreen
import com.xiehe.spine.ui.screens.message.MessagesScreen
import com.xiehe.spine.ui.screens.patient.PatientDetailScreen
import com.xiehe.spine.ui.screens.patient.PatientEditScreen
import com.xiehe.spine.ui.screens.patient.PatientFormScreen
import com.xiehe.spine.ui.screens.profile.AppearanceScreen
import com.xiehe.spine.ui.screens.profile.ChangePasswordScreen
import com.xiehe.spine.ui.screens.profile.OrganizationCreateTeamScreen
import com.xiehe.spine.ui.screens.profile.OrganizationInviteScreen
import com.xiehe.spine.ui.screens.profile.OrganizationScreen
import com.xiehe.spine.ui.screens.profile.PersonalInfoScreen
import com.xiehe.spine.ui.screens.shared.MobileShell
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.organization.canInviteMembers
import com.xiehe.spine.ui.viewmodel.organization.currentMember
import com.xiehe.spine.ui.viewmodel.organization.selectedTeam
import com.xiehe.spine.ui.viewmodel.profile.AppearanceViewModel

@Composable
internal fun OverlayHost(
    route: OverlayRoute,
    visible: Boolean,
    session: UserSession,
    container: AppContainer,
    scopedViewModels: SessionScopedViewModels,
    appearanceVm: AppearanceViewModel,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRouteChange: (OverlayRoute?) -> Unit,
    onLogoutRequested: suspend () -> Unit,
    onSessionUpdated: (UserSession) -> Unit,
    onSessionExpired: (String) -> Unit,
    onExited: (() -> Unit)? = null,
) {
    when (route) {
        is OverlayRoute.PatientDetail,
        OverlayRoute.PatientForm,
        is OverlayRoute.PatientEdit,
        -> PatientOverlayContent(
            route = route,
            session = session,
            container = container,
            scopedViewModels = scopedViewModels,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onRouteChange = onRouteChange,
            onLogoutRequested = onLogoutRequested,
            onSessionUpdated = onSessionUpdated,
            onSessionExpired = onSessionExpired,
            visible = visible,
            onExited = onExited,
        )

        is OverlayRoute.ImageAnalysis,
        OverlayRoute.ImageUpload,
        -> ImageOverlayContent(
            route = route,
            session = session,
            container = container,
            scopedViewModels = scopedViewModels,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onRouteChange = onRouteChange,
            onLogoutRequested = onLogoutRequested,
            onSessionUpdated = onSessionUpdated,
            onSessionExpired = onSessionExpired,
            visible = visible,
            onExited = onExited,
        )

        OverlayRoute.Appearance,
        OverlayRoute.PersonalInfo,
        OverlayRoute.Organization,
        OverlayRoute.OrganizationCreateTeam,
        OverlayRoute.OrganizationInvite,
        OverlayRoute.ChangePassword,
        OverlayRoute.Messages,
        -> ProfileOverlayContent(
            route = route,
            session = session,
            container = container,
            scopedViewModels = scopedViewModels,
            appearanceVm = appearanceVm,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onRouteChange = onRouteChange,
            onLogoutRequested = onLogoutRequested,
            onSessionUpdated = onSessionUpdated,
            onSessionExpired = onSessionExpired,
            visible = visible,
            onExited = onExited,
        )
    }
}

@Composable
private fun PatientOverlayContent(
    route: OverlayRoute,
    session: UserSession,
    container: AppContainer,
    scopedViewModels: SessionScopedViewModels,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRouteChange: (OverlayRoute?) -> Unit,
    onLogoutRequested: suspend () -> Unit,
    onSessionUpdated: (UserSession) -> Unit,
    onSessionExpired: (String) -> Unit,
    visible: Boolean,
    onExited: (() -> Unit)?,
) {
    val detailRoute = route as? OverlayRoute.PatientDetail
    var showDeletePatientConfirm by remember(detailRoute?.patientId) { mutableStateOf(false) }

    MobileShell(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        showBottomBar = false,
        headerContent = {
            when (route) {
                is OverlayRoute.PatientDetail -> {
                    val colors = SpineTheme.colors
                    SimpleShellHeader(
                        title = "患者详情",
                        subtitle = "查看和管理患者完整信息",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                        actionsContent = {
                            HeaderTextAction(
                                text = "编辑信息",
                                onClick = { onRouteChange(OverlayRoute.PatientEdit(route.patientId)) },
                            )
                            HeaderTextAction(
                                text = "删除",
                                onClick = {
                                    showDeletePatientConfirm = true
                                },
                                fill = colors.error,
                                borderColor = colors.error,
                                textColor = colors.onPrimary,
                            )
                        },
                    )
                }

                OverlayRoute.PatientForm -> {
                    SimpleShellHeader(
                        title = "添加患者",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                }

                is OverlayRoute.PatientEdit -> {
                    SimpleShellHeader(
                        title = "编辑患者",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(OverlayRoute.PatientDetail(route.patientId)) },
                    )
                }

                else -> Unit
            }
        },
    ) {
        AppOverlayEntryHost(
            visible = visible,
            onExited = onExited,
        ) {
            AppRouteContentHost(
                targetState = route,
                orderOf = ::patientOverlayOrder,
                label = "patient_overlay_content_transition",
            ) { currentRoute ->
                when (currentRoute) {
                    is OverlayRoute.PatientDetail -> {
                        PatientDetailScreen(
                            patientId = currentRoute.patientId,
                            vm = scopedViewModels.patientDetailVm,
                            session = session,
                            patientRepository = container.patientRepository,
                            imageRepository = container.imageFileRepository,
                            onSessionUpdated = onSessionUpdated,
                            onSessionExpired = onSessionExpired,
                            onOpenAnalysis = { fileId, patientId, examType ->
                                onRouteChange(
                                    OverlayRoute.ImageAnalysis(
                                        fileId = fileId,
                                        patientId = patientId,
                                        examType = examType,
                                    ),
                                )
                            },
                            onOpenImageUpload = { onRouteChange(OverlayRoute.ImageUpload) },
                        )

                        if (showDeletePatientConfirm) {
                            AppConfirmDialogHost(
                                visible = showDeletePatientConfirm,
                                title = "删除患者",
                                message = "确认删除该患者吗？该操作会执行软删除，患者记录将不再显示在列表中。",
                                confirmText = "删除",
                                cancelText = "取消",
                                confirmButtonColor = SpineTheme.colors.error,
                                cancelButtonColor = SpineTheme.colors.textSecondary,
                                confirmTextColor = SpineTheme.colors.onPrimary,
                                cancelTextColor = SpineTheme.colors.onPrimary,
                                onDismissRequest = { showDeletePatientConfirm = false },
                                onConfirm = {
                                    showDeletePatientConfirm = false
                                    scopedViewModels.patientDetailVm.delete(
                                        patientId = currentRoute.patientId,
                                        session = session,
                                        repository = container.patientRepository,
                                        onSessionUpdated = onSessionUpdated,
                                        onDeleted = { updatedSession ->
                                            onSessionUpdated(updatedSession)
                                            onTabSelected(1)
                                            onRouteChange(null)
                                            scopedViewModels.patientsVm.refresh(
                                                session = updatedSession,
                                                repository = container.patientRepository,
                                                onSessionUpdated = onSessionUpdated,
                                                onSessionExpired = onSessionExpired,
                                            )
                                        },
                                        onSessionExpired = onSessionExpired,
                                    )
                                },
                            )
                        }
                    }

                    OverlayRoute.PatientForm -> {
                        PatientFormScreen(
                            vm = scopedViewModels.patientFormVm,
                            session = session,
                            repository = container.patientRepository,
                            onSessionUpdated = onSessionUpdated,
                            onSessionExpired = onSessionExpired,
                            onSubmitSuccess = {
                                onRouteChange(null)
                                onTabSelected(1)
                            },
                        )
                    }

                    is OverlayRoute.PatientEdit -> {
                        PatientEditScreen(
                            patientId = currentRoute.patientId,
                            vm = scopedViewModels.patientEditVm,
                            session = session,
                            repository = container.patientRepository,
                            onSessionUpdated = onSessionUpdated,
                            onSessionExpired = onSessionExpired,
                            onSubmitSuccess = {
                                onRouteChange(OverlayRoute.PatientDetail(currentRoute.patientId))
                            },
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun ImageOverlayContent(
    route: OverlayRoute,
    session: UserSession,
    container: AppContainer,
    scopedViewModels: SessionScopedViewModels,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRouteChange: (OverlayRoute?) -> Unit,
    onLogoutRequested: suspend () -> Unit,
    onSessionUpdated: (UserSession) -> Unit,
    onSessionExpired: (String) -> Unit,
    visible: Boolean,
    onExited: (() -> Unit)?,
) {
    AppOverlayEntryHost(
        visible = visible,
        onExited = onExited,
    ) {
        when (route) {
            is OverlayRoute.ImageAnalysis -> {
                ImageAnalysisScreen(
                    fileId = route.fileId,
                    patientId = route.patientId,
                    examType = route.examType,
                    vm = scopedViewModels.imageAnalysisVm,
                    session = session,
                    imageRepository = container.imageFileRepository,
                    measurementRepository = container.measurementRepository,
                    aiRepository = container.aiInferenceRepository,
                    onSessionUpdated = onSessionUpdated,
                    onBack = { onRouteChange(null) },
                    onSessionExpired = onSessionExpired,
                )
            }

            OverlayRoute.ImageUpload -> {
                MobileShell(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    showBottomBar = false,
                    headerContent = {
                        SimpleShellHeader(
                            title = "上传影像",
                            leadingGlyph = IconToken.BACK,
                            onLeadingAction = { onRouteChange(null) },
                        )
                    },
                ) {
                    ImageUploadScreen(
                        vm = scopedViewModels.imageUploadVm,
                        session = session,
                        patientRepository = container.patientRepository,
                        imageRepository = container.imageFileRepository,
                        onSessionUpdated = onSessionUpdated,
                        onSessionExpired = onSessionExpired,
                        onUploadSuccess = {
                            onRouteChange(null)
                            onTabSelected(2)
                            scopedViewModels.imagesVm.refresh(
                                session = session,
                                repository = container.imageFileRepository,
                                onSessionUpdated = onSessionUpdated,
                                onSessionExpired = onSessionExpired,
                            )
                        },
                    )
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun ProfileOverlayContent(
    route: OverlayRoute,
    session: UserSession,
    container: AppContainer,
    scopedViewModels: SessionScopedViewModels,
    appearanceVm: AppearanceViewModel,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRouteChange: (OverlayRoute?) -> Unit,
    onLogoutRequested: suspend () -> Unit,
    onSessionUpdated: (UserSession) -> Unit,
    onSessionExpired: (String) -> Unit,
    visible: Boolean,
    onExited: (() -> Unit)?,
) {
    val messagesState by scopedViewModels.messagesVm.state.collectAsState()
    val organizationState by scopedViewModels.organizationVm.state.collectAsState()
    var appearanceSaveTrigger by remember { mutableStateOf(0) }
    val canInviteMembers = organizationState.canInviteMembers(session.userId)
    val canCreateTeam =
        session.isSuperuser ||
            session.isSystemAdmin ||
            (organizationState.currentMember(session.userId)?.isSystemAdmin == true)

    MobileShell(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        showBottomBar = false,
        headerContent = {
            when (route) {
                OverlayRoute.Appearance -> {
                    SimpleShellHeader(
                        title = "系统设置",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                        actionsContent = {
                            HeaderTextAction(
                                text = "保存设置",
                                leadingGlyph = IconToken.SAVE,
                                onClick = { appearanceSaveTrigger += 1 },
                            )
                        },
                    )
                }

                OverlayRoute.PersonalInfo -> {
                    SimpleShellHeader(
                        title = "个人信息",
                        subtitle = "查看和修改您的个人资料",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                }

                OverlayRoute.Organization -> {
                    SimpleShellHeader(
                        title = "组织管理",
                        subtitle = "查看和管理组织成员",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                        actionsContent = if (canCreateTeam || canInviteMembers) {
                            {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.End,
                                ) {
                                    if (canCreateTeam) {
                                        HeaderTextAction(
                                            text = "创建团队",
                                            leadingGlyph = IconToken.ADD,
                                            onClick = { onRouteChange(OverlayRoute.OrganizationCreateTeam) },
                                        )
                                    }
                                    if (canInviteMembers) {
                                        HeaderTextAction(
                                            text = "邀请成员",
                                            leadingGlyph = IconToken.ADD,
                                            onClick = { onRouteChange(OverlayRoute.OrganizationInvite) },
                                        )
                                    }
                                }
                            }
                        } else {
                            null
                        },
                    )
                }

                OverlayRoute.OrganizationCreateTeam -> {
                    SimpleShellHeader(
                        title = "创建新团队",
                        subtitle = "创建新的协作团队",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(OverlayRoute.Organization) },
                    )
                }

                OverlayRoute.OrganizationInvite -> {
                    SimpleShellHeader(
                        title = "邀请新成员",
                        subtitle = organizationState.selectedTeam?.name ?: "发送组织邀请",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(OverlayRoute.Organization) },
                    )
                }

                OverlayRoute.ChangePassword -> {
                    SimpleShellHeader(
                        title = "修改密码",
                        subtitle = "定期更换密码保障账号安全",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                }

                OverlayRoute.Messages -> {
                    SimpleShellHeader(
                        title = "消息中心",
                        subtitle = "${messagesState.items.size}条消息",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                }

                else -> Unit
            }
        },
    ) {
        AppOverlayEntryHost(
            visible = visible,
            onExited = onExited,
        ) {
            AppRouteContentHost(
                targetState = route,
                orderOf = ::profileOverlayOrder,
                label = "profile_overlay_content_transition",
            ) { currentRoute ->
                when (currentRoute) {
                OverlayRoute.Appearance -> {
                    AppearanceScreen(
                        vm = appearanceVm,
                        saveTrigger = appearanceSaveTrigger,
                    )
                }

                    OverlayRoute.PersonalInfo -> {
                        PersonalInfoScreen(
                            vm = scopedViewModels.personalInfoVm,
                            session = session,
                            authRepository = container.authRepository,
                            onSessionUpdated = onSessionUpdated,
                            onSessionExpired = onSessionExpired,
                        )
                    }

                    OverlayRoute.Organization -> {
                        OrganizationScreen(
                            vm = scopedViewModels.organizationVm,
                            session = session,
                            repository = container.organizationRepository,
                            onSessionUpdated = onSessionUpdated,
                            onSessionExpired = onSessionExpired,
                        )
                    }

                    OverlayRoute.OrganizationCreateTeam -> {
                        OrganizationCreateTeamScreen(
                            vm = scopedViewModels.organizationVm,
                            session = session,
                            repository = container.organizationRepository,
                            onSessionUpdated = onSessionUpdated,
                            onFinished = { onRouteChange(OverlayRoute.Organization) },
                            onSessionExpired = onSessionExpired,
                        )
                    }

                    OverlayRoute.OrganizationInvite -> {
                        OrganizationInviteScreen(
                            vm = scopedViewModels.organizationVm,
                            session = session,
                            repository = container.organizationRepository,
                            onSessionUpdated = onSessionUpdated,
                            onFinished = { onRouteChange(OverlayRoute.Organization) },
                            onSessionExpired = onSessionExpired,
                        )
                    }

                    OverlayRoute.ChangePassword -> {
                        ChangePasswordScreen(
                            session = session,
                            authRepository = container.authRepository,
                            onPasswordChanged = onLogoutRequested,
                            onFinished = { onRouteChange(null) },
                            onSessionExpired = onSessionExpired,
                        )
                    }

                    OverlayRoute.Messages -> {
                        MessagesScreen(
                            vm = scopedViewModels.messagesVm,
                            session = session,
                            repository = container.notificationRepository,
                            onSessionUpdated = onSessionUpdated,
                            onSessionExpired = onSessionExpired,
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

private fun patientOverlayOrder(route: OverlayRoute): Int = when (route) {
    is OverlayRoute.PatientEdit -> 1
    is OverlayRoute.PatientDetail,
    OverlayRoute.PatientForm,
    -> 0
    else -> 0
}

private fun profileOverlayOrder(route: OverlayRoute): Int = when (route) {
    OverlayRoute.OrganizationInvite,
    OverlayRoute.OrganizationCreateTeam,
    OverlayRoute.ChangePassword,
    -> 1
    OverlayRoute.Appearance,
    OverlayRoute.PersonalInfo,
    OverlayRoute.Organization,
    OverlayRoute.Messages,
    -> 0
    else -> 0
}
