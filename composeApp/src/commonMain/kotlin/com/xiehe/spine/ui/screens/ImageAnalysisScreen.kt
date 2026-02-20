package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AiInferenceRepository
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.MeasurementRepository
import com.xiehe.spine.ui.components.AnalysisBottomAction
import com.xiehe.spine.ui.components.AnalysisBottomBar
import com.xiehe.spine.ui.components.AnalysisSettingsPanel
import com.xiehe.spine.ui.components.AnalysisTopBar
import com.xiehe.spine.ui.components.ImageViewport
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.MeasureToolPanel
import com.xiehe.spine.ui.components.MeasurementResultsPanel
import com.xiehe.spine.ui.components.PickerDialog
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.ImageAnalysisViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun ImageAnalysisScreen(
    fileId: Int,
    vm: ImageAnalysisViewModel,
    session: UserSession,
    imageRepository: ImageFileRepository,
    measurementRepository: MeasurementRepository,
    aiRepository: AiInferenceRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        AnalysisTopBar(
            modifier = Modifier.statusBarsPadding(),
            doctorName = session.username,
            fileId = fileId,
            patientId = null,
            onBack = onBack,
            onSave = {
                vm.notifyActionUnavailable("保存接口暂未开放，当前仅支持读取测量结果")
            },
            onImportJson = { vm.notifyActionUnavailable("导入JSON：后端接口可用后接入") },
            onExportJson = { vm.notifyActionUnavailable("导出JSON：后端接口可用后接入") },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            ImageViewport(
                bitmap = imageBitmap,
                measurements = state.measurements,
                hiddenKeys = state.hiddenMeasurementKeys,
                zoomPercent = state.zoomPercent,
                contrast = state.contrast,
                brightness = state.brightness,
                modifier = Modifier.fillMaxSize(),
            )

            MeasurementResultsPanel(
                standardDistanceLabel = state.standardDistanceLabel,
                expanded = state.resultsExpanded,
                measurements = state.measurements,
                hiddenKeys = state.hiddenMeasurementKeys,
                onToggleExpanded = vm::toggleResultsExpanded,
                onToggleItemVisibility = vm::toggleMeasurementVisibility,
                onShowAll = { vm.setAllMeasurementsVisible(true) },
                onHideAll = { vm.setAllMeasurementsVisible(false) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
            )

            if (state.loading) {
                LoadingOverlay(message = "...正在加载中")
            } else if (state.aiRunning) {
                LoadingOverlay(message = state.aiRunningLabel ?: "...正在加载中")
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

            state.bannerMessage?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
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
                    AnalysisBottomAction.AI_DETECT -> vm.runAiDetect(
                        fileId = fileId,
                        repository = aiRepository,
                    )
                    AnalysisBottomAction.AI_MEASURE -> vm.runAiMeasure(
                        fileId = fileId,
                        repository = aiRepository,
                    )
                    AnalysisBottomAction.REPORT -> vm.notifyActionUnavailable("报告生成接口待接入")
                    AnalysisBottomAction.TOOLKIT -> vm.openToolsPanel()
                    AnalysisBottomAction.SETTINGS -> vm.openSettingsPanel()
                }
            },
        )
    }

    if (state.showToolsPanel) {
        PickerDialog(
            title = "",
            onDismissRequest = vm::closeToolsPanel,
            showActionRow = false,
        ) { dismiss ->
            MeasureToolPanel { tool ->
                vm.notifyActionUnavailable("已选择工具：$tool")
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
                    onClearAll = {
                        vm.clearMeasurements()
                    },
                    onZoomChange = vm::adjustZoom,
                    onContrastChange = vm::adjustContrast,
                    onBrightnessChange = vm::adjustBrightness,
                )
                Text(
                    text = "完成",
                    style = SpineTheme.typography.body,
                    color = SpineTheme.colors.primary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { dismiss() },
                )
            }
        }
    }
}
