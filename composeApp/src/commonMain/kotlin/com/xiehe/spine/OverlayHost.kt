package com.xiehe.spine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AppContainer
import com.xiehe.spine.ui.components.card.shared.OperationVerifyCard
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.navigation.shared.HeaderTextAction
import com.xiehe.spine.ui.components.navigation.shared.SimpleShellHeader
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun OverlayHost(
    route: OverlayRoute,
    session: UserSession,
    container: AppContainer,
    scopedViewModels: SessionScopedViewModels,
    appearanceVm: AppearanceViewModel,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRouteChange: (OverlayRoute?) -> Unit,
    onSessionUpdated: (UserSession) -> Unit,
    onUnauthorized: suspend () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val messagesState by scopedViewModels.messagesVm.state.collectAsState()
    val organizationState by scopedViewModels.organizationVm.state.collectAsState()

    when (route) {
        is OverlayRoute.PatientDetail -> {
            var showDeletePatientConfirm by remember(route.patientId) { mutableStateOf(false) }
            var deletePatientConfirmVisible by remember(route.patientId) { mutableStateOf(false) }

            MobileShell(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                headerContent = {
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
                                    deletePatientConfirmVisible = false
                                },
                                fill = colors.error,
                                borderColor = colors.error,
                                textColor = colors.onPrimary,
                            )
                        },
                    )
                },
            ) {
                PatientDetailScreen(
                    patientId = route.patientId,
                    vm = scopedViewModels.patientDetailVm,
                    session = session,
                    patientRepository = container.patientRepository,
                    imageRepository = container.imageFileRepository,
                    onSessionUpdated = onSessionUpdated,
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
            }

            if (showDeletePatientConfirm) {
                LaunchedEffect(showDeletePatientConfirm) {
                    if (showDeletePatientConfirm) {
                        delay(16)
                        deletePatientConfirmVisible = true
                    }
                }
                val overlayAlpha by animateFloatAsState(
                    targetValue = if (deletePatientConfirmVisible) 0.35f else 0f,
                    animationSpec = tween(220),
                    label = "patient_delete_confirm_overlay_alpha",
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                coroutineScope.launch {
                                    deletePatientConfirmVisible = false
                                    delay(220)
                                    showDeletePatientConfirm = false
                                }
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedVisibility(
                        visible = deletePatientConfirmVisible,
                        enter = fadeIn(animationSpec = tween(220)) +
                            slideInVertically(animationSpec = tween(220)) { it / 4 },
                        exit = fadeOut(animationSpec = tween(220)) +
                            slideOutVertically(animationSpec = tween(220)) { it / 5 },
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {},
                                ),
                        ) {
                            OperationVerifyCard(
                                title = "删除患者",
                                message = "确认删除该患者吗？该操作会执行软删除，患者记录将不再显示在列表中。",
                                confirmText = "删除",
                                cancelText = "取消",
                                confirmButtonColor = SpineTheme.colors.error,
                                cancelButtonColor = SpineTheme.colors.textSecondary,
                                onCancel = {
                                    coroutineScope.launch {
                                        deletePatientConfirmVisible = false
                                        delay(220)
                                        showDeletePatientConfirm = false
                                    }
                                },
                                onConfirm = {
                                    coroutineScope.launch {
                                        deletePatientConfirmVisible = false
                                        delay(220)
                                        showDeletePatientConfirm = false
                                    }
                                    scopedViewModels.patientDetailVm.delete(
                                        patientId = route.patientId,
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
                                            )
                                        },
                                        onUnauthorized = {
                                            coroutineScope.launch { onUnauthorized() }
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

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
                        onLeadingAction = { onRouteChange(null) },
                    )
                },
            ) {
                PatientFormScreen(
                    vm = scopedViewModels.patientFormVm,
                    session = session,
                    repository = container.patientRepository,
                    onSessionUpdated = onSessionUpdated,
                    onSubmitSuccess = {
                        onRouteChange(null)
                        onTabSelected(1)
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
                        onLeadingAction = { onRouteChange(null) },
                    )
                },
            ) {
                PatientEditScreen(
                    patientId = route.patientId,
                    vm = scopedViewModels.patientEditVm,
                    session = session,
                    repository = container.patientRepository,
                    onSessionUpdated = onSessionUpdated,
                    onSubmitSuccess = {
                        onRouteChange(OverlayRoute.PatientDetail(route.patientId))
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
                        title = "系统设置",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
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
                        subtitle = "查看和修改您的个人资料",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                },
            ) {
                PersonalInfoScreen(
                    vm = scopedViewModels.personalInfoVm,
                    session = session,
                    authRepository = container.authRepository,
                    onSessionUpdated = onSessionUpdated,
                )
            }
        }

        OverlayRoute.Organization -> {
            val canInviteMembers = organizationState.canInviteMembers(session.userId)
            val canCreateTeam =
                session.isSuperuser ||
                    session.isSystemAdmin ||
                    (organizationState.currentMember(session.userId)?.isSystemAdmin == true)
            MobileShell(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                headerContent = {
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
                },
            ) {
                OrganizationScreen(
                    vm = scopedViewModels.organizationVm,
                    session = session,
                    repository = container.organizationRepository,
                    onSessionUpdated = onSessionUpdated,
                )
            }
        }

        OverlayRoute.OrganizationCreateTeam -> {
            MobileShell(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                headerContent = {
                    SimpleShellHeader(
                        title = "创建新团队",
                        subtitle = "创建新的协作团队",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(OverlayRoute.Organization) },
                    )
                },
            ) {
                OrganizationCreateTeamScreen(
                    vm = scopedViewModels.organizationVm,
                    session = session,
                    repository = container.organizationRepository,
                    onSessionUpdated = onSessionUpdated,
                    onFinished = { onRouteChange(OverlayRoute.Organization) },
                )
            }
        }

        OverlayRoute.OrganizationInvite -> {
            MobileShell(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                headerContent = {
                    SimpleShellHeader(
                        title = "邀请新成员",
                        subtitle = organizationState.selectedTeam?.name ?: "发送组织邀请",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(OverlayRoute.Organization) },
                    )
                },
            ) {
                OrganizationInviteScreen(
                    vm = scopedViewModels.organizationVm,
                    session = session,
                    repository = container.organizationRepository,
                    onSessionUpdated = onSessionUpdated,
                    onFinished = { onRouteChange(OverlayRoute.Organization) },
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
                        subtitle = "定期更换密码保障账号安全",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                },
            ) {
                ChangePasswordScreen(
                    session = session,
                    authRepository = container.authRepository,
                    onSessionUpdated = onSessionUpdated,
                    onFinished = { onRouteChange(null) },
                )
            }
        }

        OverlayRoute.Messages -> {
            MobileShell(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                headerContent = {
                    SimpleShellHeader(
                        title = "消息中心",
                        subtitle = "${messagesState.items.size}条消息",
                        leadingGlyph = IconToken.BACK,
                        onLeadingAction = { onRouteChange(null) },
                    )
                },
            ) {
                MessagesScreen(
                    vm = scopedViewModels.messagesVm,
                    session = session,
                    repository = container.notificationRepository,
                    onSessionUpdated = onSessionUpdated,
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
                    onUploadSuccess = {
                        onRouteChange(null)
                        onTabSelected(2)
                        scopedViewModels.imagesVm.refresh(
                            session = session,
                            repository = container.imageFileRepository,
                            onSessionUpdated = onSessionUpdated,
                        )
                    },
                )
            }
        }
    }
}
