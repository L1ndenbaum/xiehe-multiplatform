package com.xiehe.spine.ui.screens.image

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AiInferenceRepository
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.MeasurementRepository
import com.xiehe.spine.ui.components.analysis.image.AnalysisBottomAction
import com.xiehe.spine.ui.components.analysis.image.AnalysisBottomBar
import com.xiehe.spine.ui.components.analysis.image.AnalysisReportPanel
import com.xiehe.spine.ui.components.analysis.image.AnalysisSettingsPanel
import com.xiehe.spine.ui.components.analysis.image.AnalysisTopBar
import com.xiehe.spine.ui.components.analysis.image.ImageViewport
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.analysis.image.MeasureToolPanel
import com.xiehe.spine.ui.components.analysis.image.MeasurementResultsPanel
import com.xiehe.spine.ui.components.card.shared.OperationVerifyCard
import com.xiehe.spine.ui.components.form.picker.PickerDialog
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.file.FileSaveResult
import com.xiehe.spine.ui.components.form.file.rememberDownloadedFileSaver
import com.xiehe.spine.ui.components.form.file.rememberJsonFilePickerLauncher
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.image.AnalysisMeasurementKind
import com.xiehe.spine.ui.viewmodel.image.ImageAnalysisViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun ImageAnalysisScreen(
    fileId: Int,
    patientId: Int?,
    examType: String,
    vm: ImageAnalysisViewModel,
    session: UserSession,
    imageRepository: ImageFileRepository,
    measurementRepository: MeasurementRepository,
    aiRepository: AiInferenceRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showAiConfirm by remember { mutableStateOf(false) }
    var aiConfirmVisible by remember { mutableStateOf(false) }
    var showReportGenerateConfirm by remember { mutableStateOf(false) }
    var reportGenerateConfirmVisible by remember { mutableStateOf(false) }
    val downloadedFileSaver = rememberDownloadedFileSaver()
    val jsonPicker = rememberJsonFilePickerLauncher { localJsonFile ->
        if (localJsonFile == null) {
            vm.notifyActionUnavailable("未选择JSON文件")
        } else {
            vm.importAnnotationsJson(localJsonFile.text)
        }
    }

    LaunchedEffect(fileId, session.accessToken) {
        vm.load(
            fileId = fileId,
            session = session,
            imageRepository = imageRepository,
            measurementRepository = measurementRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    LaunchedEffect(state.bannerMessage) {
        if (state.bannerMessage != null) {
            delay(2200)
            vm.clearBanner()
        }
    }

    val imageBitmap = remember(state.imageBytes) {
        state.imageBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }
    val computedMeasurements = remember(state.measurements) {
        state.measurements.filter {
            it.kind == AnalysisMeasurementKind.COMPUTED && it.panelVisible
        }
    }
    val detectedPointFields = remember(state.measurements) {
        state.measurements.filter {
            it.kind == AnalysisMeasurementKind.DETECTED
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220)),
    ) {
        AnalysisTopBar(
            modifier = Modifier.statusBarsPadding(),
            doctorName = session.username,
            examType = examType,
            fileId = fileId,
            patientId = patientId,
            onBack = onBack,
            onSave = {
                vm.saveMeasurements(
                    session = session,
                    repository = measurementRepository,
                    examType = examType,
                    patientId = patientId,
                    onSessionUpdated = onSessionUpdated,
                )
            },
            onImportJson = { jsonPicker.launch() },
            onExportJson = {
                scope.launch {
                    val payload = vm.exportAnnotationsJson()
                    val filename = "annotations_${fileId}_${currentEpochSeconds()}.json"
                    when (
                        val saveResult = downloadedFileSaver.save(
                            fileName = filename,
                            mimeType = "application/json",
                            bytes = payload.encodeToByteArray(),
                        )
                    ) {
                        is FileSaveResult.Success -> {
                            val location = saveResult.location ?: "下载目录"
                            vm.notifyActionUnavailable("已导出JSON到 $location")
                        }

                        is FileSaveResult.Failure -> {
                            vm.notifyActionUnavailable("导出失败：${saveResult.message}")
                        }
                    }
                }
            },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            ImageViewport(
                bitmap = imageBitmap,
                measurements = state.measurements,
                hiddenKeys = state.hiddenMeasurementKeys,
                activeToolId = state.activeToolId,
                pendingPoints = state.pendingPoints,
                isImageLocked = state.isImageLocked,
                zoomPercent = state.zoomPercent,
                contrast = state.contrast,
                brightness = state.brightness,
                onCanvasTap = vm::onCanvasTap,
                onCanvasDoubleTap = vm::onCanvasDoubleTap,
                modifier = Modifier.fillMaxSize(),
            )

            MeasurementResultsPanel(
                standardDistanceLabel = state.standardDistanceLabel,
                expanded = state.resultsExpanded,
                computedMeasurements = computedMeasurements,
                detectedPoseFields = detectedPointFields,
                hiddenKeys = state.hiddenMeasurementKeys,
                onToggleExpanded = vm::toggleResultsExpanded,
                onToggleItemVisibility = vm::toggleMeasurementVisibility,
                onDeleteItem = vm::removeMeasurement,
                onShowAll = { vm.setAllMeasurementsVisible(true) },
                onHideAll = { vm.setAllMeasurementsVisible(false) },
                onShowComputed = { vm.setComputedMeasurementsVisible(true) },
                onHideComputed = { vm.setComputedMeasurementsVisible(false) },
                onShowDetected = { vm.setDetectedMeasurementsVisible(true) },
                onHideDetected = { vm.setDetectedMeasurementsVisible(false) },
                modifier = Modifier
                    .align(Alignment.TopEnd),
            )

            if (state.loading) {
                LoadingOverlay(message = "...正在加载中")
            } else if (state.aiRunning) {
                LoadingOverlay(message = state.aiRunningLabel ?: "...正在加载中")
            } else if (state.saving) {
                LoadingOverlay(message = "...正在保存标注")
            } else if (state.reportLoading) {
                LoadingOverlay(message = "...正在加载报告")
            } else if (state.reportGenerating) {
                LoadingOverlay(message = "...正在生成报告")
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(SpineTheme.colors.error.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    style = SpineTheme.typography.caption,
                    color = SpineTheme.colors.error,
                    maxLines = 2,
                )
            }

            val bannerBottomPadding = if (state.errorMessage != null) 56.dp else 8.dp
            state.bannerMessage?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bannerBottomPadding)
                        .background(SpineTheme.colors.surface, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    style = SpineTheme.typography.caption,
                    color = SpineTheme.colors.textPrimary,
                    maxLines = 2,
                )
            }
        }

        AnalysisBottomBar(
            modifier = Modifier.navigationBarsPadding(),
            onAction = { action ->
                when (action) {
                    AnalysisBottomAction.AI_DETECT -> {
                        showAiConfirm = true
                        aiConfirmVisible = false
                    }
                    AnalysisBottomAction.REPORT -> {
                        vm.openReportPanel(
                            examType = examType,
                            patientId = patientId,
                            session = session,
                            repository = measurementRepository,
                            onSessionUpdated = onSessionUpdated,
                        )
                    }
                    AnalysisBottomAction.TOOLKIT -> vm.openToolsPanel()
                    AnalysisBottomAction.SETTINGS -> vm.openSettingsPanel()
                }
            },
        )
    }

    if (showAiConfirm) {
        LaunchedEffect(showAiConfirm) {
            if (showAiConfirm) {
                delay(16)
                aiConfirmVisible = true
            }
        }
        val overlayAlpha by animateFloatAsState(
            targetValue = if (aiConfirmVisible) 0.36f else 0f,
            animationSpec = tween(220),
            label = "analysis_ai_confirm_overlay_alpha",
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        scope.launch {
                            aiConfirmVisible = false
                            delay(220)
                            showAiConfirm = false
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = aiConfirmVisible,
                enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 4 },
                exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { it / 5 },
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
                        title = "AI检测确认",
                        message = "AI检测后，现有的测量结果会全部被覆盖，是否继续?",
                        confirmText = "继续",
                        cancelText = "取消",
                        confirmButtonColor = SpineTheme.colors.primary,
                        cancelButtonColor = SpineTheme.colors.textSecondary,
                        onCancel = {
                            scope.launch {
                                aiConfirmVisible = false
                                delay(220)
                                showAiConfirm = false
                            }
                        },
                        onConfirm = {
                            scope.launch {
                                aiConfirmVisible = false
                                delay(220)
                                showAiConfirm = false
                                vm.runAiDetect(
                                    fileId = fileId,
                                    repository = aiRepository,
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    if (state.showReportPanel) {
        PickerDialog(
            title = "",
            onDismissRequest = vm::closeReportPanel,
            showActionRow = false,
            maxDialogWidth = 352.dp,
            maxDialogHeightFraction = 0.68f,
        ) { _ ->
            AnalysisReportPanel(
                examType = state.reportExamType,
                imageId = state.reportImageId,
                patientId = state.reportPatientId,
                savedAt = state.reportSavedAt,
                generatedAt = state.reportGeneratedAt,
                reportText = state.reportText,
                onReportTextChange = vm::updateReportText,
                onGenerateByAi = {
                    showReportGenerateConfirm = true
                    reportGenerateConfirmVisible = false
                },
            )
        }
    }

    if (showReportGenerateConfirm) {
        LaunchedEffect(showReportGenerateConfirm) {
            if (showReportGenerateConfirm) {
                delay(16)
                reportGenerateConfirmVisible = true
            }
        }
        val overlayAlpha by animateFloatAsState(
            targetValue = if (reportGenerateConfirmVisible) 0.36f else 0f,
            animationSpec = tween(220),
            label = "analysis_report_confirm_overlay_alpha",
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        scope.launch {
                            reportGenerateConfirmVisible = false
                            delay(220)
                            showReportGenerateConfirm = false
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = reportGenerateConfirmVisible,
                enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 4 },
                exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { it / 5 },
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
                        title = "AI生成报告确认",
                        message = "AI生成报告后，现有的填写信息会被覆盖，是否继续?",
                        confirmText = "继续",
                        cancelText = "取消",
                        confirmButtonColor = SpineTheme.colors.primary,
                        cancelButtonColor = SpineTheme.colors.textSecondary,
                        onCancel = {
                            scope.launch {
                                reportGenerateConfirmVisible = false
                                delay(220)
                                showReportGenerateConfirm = false
                            }
                        },
                        onConfirm = {
                            scope.launch {
                                reportGenerateConfirmVisible = false
                                delay(220)
                                showReportGenerateConfirm = false
                                vm.generateReport(
                                    session = session,
                                    repository = measurementRepository,
                                    examType = examType,
                                    onSessionUpdated = onSessionUpdated,
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    if (state.showToolsPanel) {
        PickerDialog(
            title = "",
            onDismissRequest = vm::closeToolsPanel,
            showActionRow = false,
        ) { dismiss ->
            MeasureToolPanel(
                tools = vm.availableTools(),
                activeToolId = state.activeToolId,
            ) { toolId ->
                vm.selectTool(toolId)
                dismiss()
            }
        }
    }

    if (state.showSettingsPanel) {
        PickerDialog(
            title = "",
            onDismissRequest = vm::closeSettingsPanel,
            showActionRow = false,
        ) { dismiss ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "影像设置",
                    style = SpineTheme.typography.title,
                )
                AnalysisSettingsPanel(
                    zoomPercent = state.zoomPercent,
                    contrast = state.contrast,
                    brightness = state.brightness,
                    standardDistanceInput = state.standardDistanceInput,
                    isImageLocked = state.isImageLocked,
                    onClearAll = {
                        vm.clearMeasurements()
                    },
                    onZoomChange = vm::adjustZoom,
                    onContrastChange = vm::adjustContrast,
                    onBrightnessChange = vm::adjustBrightness,
                    onStandardDistanceChange = vm::updateStandardDistanceInput,
                    onToggleImageLock = vm::toggleImageLocked,
                )
            }
        }
    }
}


