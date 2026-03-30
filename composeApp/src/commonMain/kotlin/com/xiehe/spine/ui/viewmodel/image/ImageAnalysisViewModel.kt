package com.xiehe.spine.ui.viewmodel.image

import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ai.AiDetectResponse
import com.xiehe.spine.data.ai.AiInferenceRepository
import com.xiehe.spine.data.ai.AiPointNode
import com.xiehe.spine.data.ai.AiPredictResponse
import com.xiehe.spine.data.measurement.GenerateReportMeasurementItem
import com.xiehe.spine.data.measurement.GenerateReportRequest
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.ai.AiVertebraCorners
import com.xiehe.spine.data.measurement.ImageMeasurementItem
import com.xiehe.spine.data.measurement.MeasurementPoint
import com.xiehe.spine.data.measurement.MeasurementRepository
import com.xiehe.spine.data.measurement.SaveMeasurementItem
import com.xiehe.spine.data.measurement.SaveMeasurementsRequest
import com.xiehe.spine.data.report.mapImageCategoryToReportExamType
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

enum class AnalysisMeasurementKind {
    COMPUTED,
    DETECTED,
}

enum class AnalysisToolSection(val title: String) {
    BASIC("基础模式"),
    MEASURE("测量标注"),
    AUXILIARY("辅助图形"),
}

data class AnalysisToolDefinition(
    val id: String,
    val label: String,
    val icon: IconToken,
    val section: AnalysisToolSection,
    val pointsNeeded: Int,
    val supportsDoubleTapFinish: Boolean = false,
)

private const val DEFAULT_STANDARD_DISTANCE_MM = 100.0
const val TOOL_MOVE = "move"
const val TOOL_T1_TILT = "t1_tilt"
const val TOOL_COBB = "cobb"
const val TOOL_CA = "ca"
const val TOOL_PELVIC = "pelvic"
const val TOOL_SACRAL = "sacral"
const val TOOL_TS = "ts"
const val TOOL_AVT = "avt"
const val TOOL_STANDARD_DISTANCE = "standard_distance"
const val TOOL_VERTEBRA_CENTER = "vertebra_center"
const val TOOL_DISTANCE = "distance"
const val TOOL_ANGLE = "angle"
const val TOOL_AUX_CIRCLE = "aux_circle"
const val TOOL_AUX_ELLIPSE = "aux_ellipse"
const val TOOL_AUX_BOX = "aux_box"
const val TOOL_AUX_ARROW = "aux_arrow"
const val TOOL_AUX_POLYGON = "aux_polygon"

val AnalysisToolsCatalog = listOf(
    AnalysisToolDefinition(
        id = TOOL_MOVE,
        label = "移动",
        icon = IconToken.MEASURE_MOVE,
        section = AnalysisToolSection.BASIC,
        pointsNeeded = 0,
    ),
    AnalysisToolDefinition(
        id = TOOL_T1_TILT,
        label = "T1 Tilt",
        icon = IconToken.MEASURE_T1_TILT,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_COBB,
        label = "Cobb",
        icon = IconToken.MEASURE_COBB,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 4,
    ),
    AnalysisToolDefinition(
        id = TOOL_CA,
        label = "CA",
        icon = IconToken.MEASURE_CA,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_PELVIC,
        label = "Pelvic",
        icon = IconToken.MEASURE_PELVIC,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_SACRAL,
        label = "Sacral",
        icon = IconToken.MEASURE_SACRAL,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_TS,
        label = "TS",
        icon = IconToken.MEASURE_TS,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_AVT,
        label = "AVT",
        icon = IconToken.MEASURE_AVT,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_STANDARD_DISTANCE,
        label = "标准距离",
        icon = IconToken.MEASURE_STANDARD_DISTANCE,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_VERTEBRA_CENTER,
        label = "椎体中心",
        icon = IconToken.MEASURE_VERTEBRA_CENTER,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 4,
    ),
    AnalysisToolDefinition(
        id = TOOL_DISTANCE,
        label = "距离",
        icon = IconToken.MEASURE_DISTANCE,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_ANGLE,
        label = "角度",
        icon = IconToken.MEASURE_ANGLE,
        section = AnalysisToolSection.MEASURE,
        pointsNeeded = 3,
    ),
    AnalysisToolDefinition(
        id = TOOL_AUX_CIRCLE,
        label = "Circle",
        icon = IconToken.MEASURE_AUX_CIRCLE,
        section = AnalysisToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_AUX_ELLIPSE,
        label = "Ellipse",
        icon = IconToken.MEASURE_AUX_ELLIPSE,
        section = AnalysisToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_AUX_BOX,
        label = "Box",
        icon = IconToken.MEASURE_AUX_BOX,
        section = AnalysisToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_AUX_ARROW,
        label = "Arrow",
        icon = IconToken.MEASURE_AUX_ARROW,
        section = AnalysisToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnalysisToolDefinition(
        id = TOOL_AUX_POLYGON,
        label = "Polygon",
        icon = IconToken.MEASURE_AUX_POLYGON,
        section = AnalysisToolSection.AUXILIARY,
        pointsNeeded = 0,
        supportsDoubleTapFinish = true,
    ),
)

private val DEFAULT_STANDARD_DISTANCE_POINTS = listOf(
    MeasurementPoint(x = 0.0, y = 0.0),
    MeasurementPoint(x = 200.0, y = 0.0),
)

data class ImageAnalysisMeasurement(
    val key: String,
    val type: String,
    val value: String,
    val points: List<MeasurementPoint>,
    val description: String? = null,
    val kind: AnalysisMeasurementKind = AnalysisMeasurementKind.COMPUTED,
    val pointLabel: String? = null,
    val confidence: Double? = null,
    val panelVisible: Boolean = true,
    val helperSegments: List<AnalysisHelperSegment> = emptyList(),
    val auxiliary: Boolean = false,
)

@Serializable
data class AnalysisHelperSegment(
    val start: MeasurementPoint,
    val end: MeasurementPoint,
    val dashed: Boolean = false,
)

data class ImageAnalysisUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val aiRunning: Boolean = false,
    val aiRunningLabel: String? = null,
    val fileId: Int? = null,
    val imageBytes: ByteArray? = null,
    val measurements: List<ImageAnalysisMeasurement> = emptyList(),
    val standardDistanceMm: Double? = DEFAULT_STANDARD_DISTANCE_MM,
    val standardDistancePoints: List<MeasurementPoint> = DEFAULT_STANDARD_DISTANCE_POINTS,
    val standardDistanceInput: String = "100",
    val hiddenMeasurementKeys: Set<String> = emptySet(),
    val reportText: String = "",
    val reportExamType: String = "",
    val reportImageId: String = "",
    val reportPatientId: String = "",
    val reportSavedAt: String = "",
    val reportGeneratedAt: String = "",
    val reportLoading: Boolean = false,
    val reportGenerating: Boolean = false,
    val showReportPanel: Boolean = false,
    val standardDistanceLabel: String = "标准距离 100mm",
    val showToolsPanel: Boolean = false,
    val showSettingsPanel: Boolean = false,
    val resultsExpanded: Boolean = true,
    val activeToolId: String = TOOL_MOVE,
    val pendingPoints: List<MeasurementPoint> = emptyList(),
    val isImageLocked: Boolean = false,
    val zoomPercent: Int = 100,
    val contrast: Int = 0,
    val brightness: Int = 0,
    val errorMessage: String? = null,
    val bannerMessage: String? = null,
)

class ImageAnalysisViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(ImageAnalysisUiState())
    val state: StateFlow<ImageAnalysisUiState> = _state.asStateFlow()
    private var manualIdCounter: Long = 0L

    fun load(
        fileId: Int,
        session: UserSession,
        imageRepository: ImageFileRepository,
        measurementRepository: MeasurementRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val current = _state.value
        if (current.fileId == fileId && (current.measurements.isNotEmpty() || current.imageBytes != null)) {
            return
        }

        scope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    aiRunning = false,
                    aiRunningLabel = null,
                    fileId = fileId,
                    errorMessage = null,
                    bannerMessage = null,
                    measurements = emptyList(),
                    standardDistanceMm = DEFAULT_STANDARD_DISTANCE_MM,
                    standardDistancePoints = DEFAULT_STANDARD_DISTANCE_POINTS,
                    standardDistanceInput = formatStandardDistanceInput(DEFAULT_STANDARD_DISTANCE_MM),
                    hiddenMeasurementKeys = emptySet(),
                    activeToolId = TOOL_MOVE,
                    pendingPoints = emptyList(),
                    isImageLocked = false,
                    imageBytes = null,
                    reportImageId = fileId.toString(),
                    reportSavedAt = "",
                    reportGeneratedAt = "",
                    reportLoading = false,
                    reportGenerating = false,
                    showReportPanel = false,
                )
            }

            var activeSession = session
            var imageBytes: ByteArray? = null
            var items: List<ImageAnalysisMeasurement> = emptyList()
            var reportText = ""
            var reportSavedAt = ""
            var standardDistanceMm: Double? = DEFAULT_STANDARD_DISTANCE_MM
            var standardDistancePoints: List<MeasurementPoint> = DEFAULT_STANDARD_DISTANCE_POINTS
            val errors = mutableListOf<String>()

            when (val measurementsResult = measurementRepository.loadMeasurements(activeSession, fileId)) {
                is AppResult.Success -> {
                    activeSession = measurementsResult.data.first
                    onSessionUpdated(activeSession)
                    val payload = measurementsResult.data.second
                    items = payload.measurements.mapIndexed { index, measurement ->
                        measurement.toUiMeasurement(index)
                    }
                    reportText = payload.reportText.orEmpty()
                    reportSavedAt = payload.savedAt.orEmpty()
                    standardDistanceMm = payload.standardDistance?.takeIf { it > 0.0 } ?: standardDistanceMm
                    standardDistancePoints = payload.standardDistancePoints.takeIf { it.size >= 2 } ?: standardDistancePoints
                }

                is AppResult.Failure -> {
                    errors += measurementsResult.message
                }
            }

            when (val imageResult = imageRepository.downloadImageBytes(activeSession, fileId)) {
                is AppResult.Success -> {
                    activeSession = imageResult.data.first
                    onSessionUpdated(activeSession)
                    imageBytes = imageResult.data.second
                }

                is AppResult.Failure -> {
                    errors += imageResult.message
                }
            }

            _state.update {
                it.copy(
                    loading = false,
                    aiRunning = false,
                    aiRunningLabel = null,
                    fileId = fileId,
                    imageBytes = imageBytes,
                    measurements = items,
                    standardDistanceMm = standardDistanceMm,
                    standardDistancePoints = standardDistancePoints,
                    standardDistanceInput = formatStandardDistanceInput(standardDistanceMm ?: DEFAULT_STANDARD_DISTANCE_MM),
                    hiddenMeasurementKeys = emptySet(),
                    activeToolId = TOOL_MOVE,
                    pendingPoints = emptyList(),
                    isImageLocked = false,
                    reportText = reportText,
                    standardDistanceLabel = buildStandardDistanceLabel(standardDistanceMm ?: DEFAULT_STANDARD_DISTANCE_MM),
                    reportImageId = fileId.toString(),
                    reportSavedAt = reportSavedAt,
                    reportGeneratedAt = "",
                    reportLoading = false,
                    reportGenerating = false,
                    errorMessage = errors.firstOrNull(),
                    bannerMessage = if (errors.size > 1) errors.joinToString("；") else null,
                )
            }
        }
    }

    fun refresh(
        session: UserSession,
        imageRepository: ImageFileRepository,
        measurementRepository: MeasurementRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val fileId = _state.value.fileId ?: return
        _state.update {
            it.copy(
                fileId = null,
                imageBytes = null,
                measurements = emptyList(),
                standardDistanceMm = DEFAULT_STANDARD_DISTANCE_MM,
                standardDistancePoints = DEFAULT_STANDARD_DISTANCE_POINTS,
                standardDistanceInput = formatStandardDistanceInput(DEFAULT_STANDARD_DISTANCE_MM),
                standardDistanceLabel = buildStandardDistanceLabel(DEFAULT_STANDARD_DISTANCE_MM),
                hiddenMeasurementKeys = emptySet(),
                activeToolId = TOOL_MOVE,
                pendingPoints = emptyList(),
                isImageLocked = false,
                reportSavedAt = "",
                reportGeneratedAt = "",
                reportLoading = false,
                reportGenerating = false,
                showReportPanel = false,
            )
        }
        load(
            fileId = fileId,
            session = session,
            imageRepository = imageRepository,
            measurementRepository = measurementRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    fun clearBanner() {
        _state.update { it.copy(bannerMessage = null) }
    }

    fun toggleResultsExpanded() {
        _state.update { it.copy(resultsExpanded = !it.resultsExpanded) }
    }

    fun toggleMeasurementVisibility(key: String) {
        _state.update { state ->
            val next = state.hiddenMeasurementKeys.toMutableSet()
            if (!next.add(key)) {
                next.remove(key)
            }
            state.copy(hiddenMeasurementKeys = next)
        }
    }

    fun setAllMeasurementsVisible(visible: Boolean) {
        _state.update { state ->
            state.copy(
                hiddenMeasurementKeys = if (visible) {
                    emptySet()
                } else {
                    state.measurements.mapTo(mutableSetOf()) { it.key }
                },
            )
        }
    }

    fun setComputedMeasurementsVisible(visible: Boolean) {
        _state.update { state ->
            val computedKeys = state.measurements
                .filter { it.kind == AnalysisMeasurementKind.COMPUTED && it.panelVisible }
                .map { it.key }
            if (computedKeys.isEmpty()) {
                return@update state
            }
            val hidden = state.hiddenMeasurementKeys.toMutableSet()
            if (visible) {
                hidden.removeAll(computedKeys.toSet())
            } else {
                hidden.addAll(computedKeys)
            }
            state.copy(hiddenMeasurementKeys = hidden)
        }
    }

    fun setDetectedMeasurementsVisible(visible: Boolean) {
        _state.update { state ->
            val detectedKeys = state.measurements
                .filter { it.kind == AnalysisMeasurementKind.DETECTED && it.panelVisible }
                .map { it.key }
            if (detectedKeys.isEmpty()) {
                return@update state
            }
            val hidden = state.hiddenMeasurementKeys.toMutableSet()
            if (visible) {
                hidden.removeAll(detectedKeys.toSet())
            } else {
                hidden.addAll(detectedKeys)
            }
            state.copy(hiddenMeasurementKeys = hidden)
        }
    }

    fun openReportPanel(
        examType: String,
        patientId: Int?,
        session: UserSession,
        repository: MeasurementRepository,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val fileId = _state.value.fileId ?: return
        _state.update {
            it.copy(
                showReportPanel = true,
                reportLoading = true,
                reportExamType = examType,
                reportImageId = fileId.toString(),
                reportPatientId = patientId?.toString().orEmpty(),
                reportGeneratedAt = "",
                bannerMessage = null,
                errorMessage = null,
            )
        }
        scope.launch {
            when (val result = repository.loadMeasurements(session, fileId)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    val payload = result.data.second
                    _state.update {
                        it.copy(
                            reportLoading = false,
                            reportText = payload.reportText.orEmpty(),
                            reportSavedAt = payload.savedAt.orEmpty(),
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            reportLoading = false,
                            reportText = "",
                            reportSavedAt = "",
                            errorMessage = result.message,
                            bannerMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun closeReportPanel() {
        _state.update {
            it.copy(
                showReportPanel = false,
                reportLoading = false,
                reportGenerating = false,
            )
        }
    }

    fun updateReportText(value: String) {
        _state.update { it.copy(reportText = value) }
    }

    fun generateReport(
        session: UserSession,
        repository: MeasurementRepository,
        examType: String,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val snapshot = _state.value
        val fileId = snapshot.fileId ?: return
        val reportExamType = mapImageCategoryToReportExamType(examType)
        if (reportExamType == null) {
            _state.update {
                it.copy(
                    bannerMessage = "体态照片暂不支持AI报告生成",
                    errorMessage = "体态照片暂不支持AI报告生成",
                )
            }
            return
        }
        val reportItems = snapshot.measurements.mapNotNull(::toGenerateReportItem)
        if (reportItems.isEmpty()) {
            _state.update { it.copy(bannerMessage = "暂无可用于生成报告的测量数据") }
            return
        }

        scope.launch {
            _state.update { it.copy(reportGenerating = true, bannerMessage = null, errorMessage = null) }
            val request = GenerateReportRequest(
                examType = reportExamType,
                imageId = fileId.toString(),
                measurements = reportItems,
            )
            when (val result = repository.generateReport(session, request)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    val payload = result.data.second
                    _state.update {
                        it.copy(
                            reportGenerating = false,
                            reportText = payload.report,
                            reportGeneratedAt = payload.generatedAt.orEmpty(),
                            errorMessage = null,
                            bannerMessage = "AI报告生成完成",
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            reportGenerating = false,
                            errorMessage = result.message,
                            bannerMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun openToolsPanel() {
        _state.update { it.copy(showToolsPanel = true) }
    }

    fun closeToolsPanel() {
        _state.update { it.copy(showToolsPanel = false) }
    }

    fun openSettingsPanel() {
        _state.update { it.copy(showSettingsPanel = true) }
    }

    fun closeSettingsPanel() {
        _state.update { it.copy(showSettingsPanel = false) }
    }

    fun availableTools(): List<AnalysisToolDefinition> = AnalysisToolsCatalog

    fun selectTool(toolId: String) {
        val tool = AnalysisToolsCatalog.firstOrNull { it.id == toolId } ?: return
        _state.update {
            it.copy(
                activeToolId = tool.id,
                pendingPoints = emptyList(),
                showToolsPanel = false,
                bannerMessage = "已切换工具：${tool.label}",
            )
        }
    }

    fun clearPendingPoints() {
        _state.update { it.copy(pendingPoints = emptyList()) }
    }

    fun removeMeasurement(key: String) {
        _state.update { current ->
            current.copy(
                measurements = current.measurements.filterNot { it.key == key },
                hiddenMeasurementKeys = current.hiddenMeasurementKeys - key,
                bannerMessage = "已删除标注项",
            )
        }
    }

    fun toggleImageLocked() {
        _state.update { it.copy(isImageLocked = !it.isImageLocked) }
    }

    fun onCanvasTap(point: MeasurementPoint) {
        val snapshot = _state.value
        val tool = AnalysisToolsCatalog.firstOrNull { it.id == snapshot.activeToolId } ?: return
        if (tool.id == TOOL_MOVE) return
        if (tool.id == TOOL_AUX_POLYGON) {
            _state.update { it.copy(pendingPoints = it.pendingPoints + point) }
            return
        }
        if (tool.pointsNeeded <= 0) return
        val nextPoints = snapshot.pendingPoints + point
        if (nextPoints.size < tool.pointsNeeded) {
            _state.update { it.copy(pendingPoints = nextPoints) }
            return
        }

        val measurement = computeManualMeasurement(toolId = tool.id, points = nextPoints, state = snapshot)
        if (measurement == null) {
            _state.update {
                it.copy(
                    pendingPoints = emptyList(),
                    bannerMessage = "当前工具计算失败，请重试",
                )
            }
            return
        }

        _state.update {
            it.copy(
                pendingPoints = emptyList(),
                measurements = it.measurements + measurement,
                hiddenMeasurementKeys = it.hiddenMeasurementKeys - measurement.key,
                standardDistancePoints = if (tool.id == TOOL_STANDARD_DISTANCE) nextPoints else it.standardDistancePoints,
                bannerMessage = "已新增 ${measurement.type} 测量",
            )
        }
    }

    fun onCanvasDoubleTap() {
        val snapshot = _state.value
        if (snapshot.activeToolId != TOOL_AUX_POLYGON) return
        if (snapshot.pendingPoints.size < 3) {
            _state.update { it.copy(bannerMessage = "Polygon 至少需要 3 个点") }
            return
        }
        val measurement = computeManualMeasurement(
            toolId = TOOL_AUX_POLYGON,
            points = snapshot.pendingPoints,
            state = snapshot,
        ) ?: return
        _state.update {
            it.copy(
                pendingPoints = emptyList(),
                measurements = it.measurements + measurement,
                hiddenMeasurementKeys = it.hiddenMeasurementKeys - measurement.key,
                bannerMessage = "已新增 Polygon 辅助图形",
            )
        }
    }

    fun exportAnnotationsJson(): String {
        val snapshot = _state.value
        val payload = ExportAnnotationSnapshot(
            imageId = snapshot.fileId?.toString().orEmpty(),
            measurements = snapshot.measurements.map { it.toExportItem() },
            standardDistance = snapshot.standardDistanceMm,
            standardDistancePoints = snapshot.standardDistancePoints,
            savedAt = Instant.fromEpochSeconds(currentEpochSeconds()).toString(),
        )
        return jsonCodec.encodeToString(payload)
    }

    fun importAnnotationsJson(json: String) {
        runCatching { jsonCodec.decodeFromString<ExportAnnotationSnapshot>(json) }
            .onSuccess { payload ->
                val importedMeasurements = payload.measurements.mapIndexed { index, item ->
                    item.toUiMeasurement(index)
                }
                val standardDistance = payload.standardDistance?.takeIf { it > 0.0 } ?: DEFAULT_STANDARD_DISTANCE_MM
                val standardPoints = payload.standardDistancePoints.takeIf { it.size >= 2 } ?: DEFAULT_STANDARD_DISTANCE_POINTS
                _state.update {
                    it.copy(
                        measurements = importedMeasurements,
                        standardDistanceMm = standardDistance,
                        standardDistancePoints = standardPoints,
                        standardDistanceInput = formatStandardDistanceInput(standardDistance),
                        standardDistanceLabel = buildStandardDistanceLabel(standardDistance),
                        hiddenMeasurementKeys = emptySet(),
                        pendingPoints = emptyList(),
                        bannerMessage = "导入标注成功，已覆盖当前标注层",
                        errorMessage = null,
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        bannerMessage = "导入失败：${error.message ?: "JSON格式错误"}",
                        errorMessage = "导入失败：${error.message ?: "JSON格式错误"}",
                    )
                }
            }
    }

    fun adjustZoom(delta: Int) {
        _state.update { it.copy(zoomPercent = (it.zoomPercent + delta).coerceIn(40, 400)) }
    }

    fun adjustContrast(delta: Int) {
        _state.update { it.copy(contrast = (it.contrast + delta).coerceIn(-100, 100)) }
    }

    fun adjustBrightness(delta: Int) {
        _state.update { it.copy(brightness = (it.brightness + delta).coerceIn(-100, 100)) }
    }

    fun notifyActionUnavailable(message: String) {
        _state.update { it.copy(bannerMessage = message) }
    }

    fun updateStandardDistanceInput(value: String) {
        val sanitized = value.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && index > 0 && '.' !in value.take(index))
        }
        _state.update { current ->
            val parsed = sanitized.toDoubleOrNull()
            if (parsed != null && parsed > 0.0) {
                current.copy(
                    standardDistanceInput = sanitized,
                    standardDistanceMm = parsed,
                    standardDistanceLabel = buildStandardDistanceLabel(parsed),
                )
            } else {
                current.copy(standardDistanceInput = sanitized)
            }
        }
    }

    fun runAiDetect(
        fileId: Int,
        repository: AiInferenceRepository,
    ) {
        runAiAction(label = "AI检测与测量中...") { bytes ->
            val calibration = CalibrationContext(
                standardDistanceMm = _state.value.standardDistanceMm,
                standardDistancePoints = _state.value.standardDistancePoints,
            )
            when (val result = repository.detectKeypoints(fileName = "image_$fileId.png", bytes = bytes)) {
                is AppResult.Success -> {
                    val overlay = mapDetectResponse(result.data, calibration)
                    _state.update {
                        it.copy(
                            aiRunning = false,
                            aiRunningLabel = null,
                            measurements = overlay,
                            hiddenMeasurementKeys = emptySet(),
                            pendingPoints = emptyList(),
                            bannerMessage = "AI检测与测量完成，已覆盖当前标注层",
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            aiRunning = false,
                            aiRunningLabel = null,
                            bannerMessage = result.message,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun runAiMeasure(
        fileId: Int,
        repository: AiInferenceRepository,
    ) {
        runAiDetect(fileId = fileId, repository = repository)
    }

    fun clearMeasurements() {
        _state.update {
            it.copy(
                measurements = emptyList(),
                hiddenMeasurementKeys = emptySet(),
                pendingPoints = emptyList(),
                bannerMessage = "已清空当前测量结果",
            )
        }
    }

    fun saveMeasurements(
        session: UserSession,
        repository: MeasurementRepository,
        examType: String,
        patientId: Int?,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val snapshot = _state.value
        val fileId = snapshot.fileId ?: return
        val computedMeasurements = snapshot.measurements
            .filter { it.kind == AnalysisMeasurementKind.COMPUTED }
            .filter { it.points.size >= 2 && it.value != "--" && it.type != "标准距离" && !it.auxiliary }
        val detectedKeypoints = snapshot.measurements
            .filter { it.kind == AnalysisMeasurementKind.DETECTED }
            .filter { it.points.isNotEmpty() }
        val persistableMeasurements = buildList {
            addAll(computedMeasurements)
            addAll(detectedKeypoints)
        }
        if (snapshot.saving) {
            return
        }
        if (persistableMeasurements.isEmpty()) {
            _state.update { it.copy(bannerMessage = "暂无可保存的标注数据") }
            return
        }

        scope.launch {
            _state.update { it.copy(saving = true, bannerMessage = null, errorMessage = null) }
            var activeSession = session
            val reportText = snapshot.reportText.ifBlank { "" }
            val savedAt = Instant.fromEpochSeconds(currentEpochSeconds()).toString()

            val saveRequest = SaveMeasurementsRequest(
                examType = examType,
                imageId = fileId.toString(),
                patientId = patientId?.toString(),
                measurements = persistableMeasurements.map(::toSaveMeasurementItem),
                reportText = reportText,
                savedAt = savedAt,
            )

            when (val result = repository.saveMeasurements(activeSession, fileId, saveRequest)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update {
                        it.copy(
                            saving = false,
                            reportText = reportText,
                            reportSavedAt = savedAt,
                            bannerMessage = "标注保存成功",
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            errorMessage = result.message,
                            bannerMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun ImageMeasurementItem.toUiMeasurement(index: Int): ImageAnalysisMeasurement {
        val valueFromField = value.jsonContent()
        val angleFromField = angle.jsonContent()
        val valueLabel = when {
            !valueFromField.isNullOrBlank() -> valueFromField
            !angleFromField.isNullOrBlank() -> {
                if (angleFromField.endsWith("°")) angleFromField else "${angleFromField}°"
            }
            else -> "--"
        }
        val rawId = id?.takeIf { it.isNotBlank() } ?: "$type#$index"
        val (kind, label, panelVisible) = detectMeasurementKind(
            id = rawId,
            type = type,
            value = valueLabel,
            points = points,
        )
        return ImageAnalysisMeasurement(
            key = rawId,
            type = type,
            value = valueLabel,
            points = points,
            description = description,
            kind = kind,
            pointLabel = label,
            panelVisible = panelVisible,
        )
    }

    private fun JsonElement?.jsonContent(): String? {
        val primitive = this ?: return null
        return runCatching { primitive.jsonPrimitive.content }.getOrNull()
    }

    private fun runAiAction(
        label: String,
        block: suspend (ByteArray) -> Unit,
    ) {
        val bytes = _state.value.imageBytes
        if (bytes == null) {
            _state.update { it.copy(bannerMessage = "当前影像尚未加载完成") }
            return
        }
        scope.launch {
            _state.update {
                it.copy(
                    aiRunning = true,
                    aiRunningLabel = label,
                    bannerMessage = null,
                    errorMessage = null,
                )
            }
            block(bytes)
        }
    }

    private fun mapPredictResponse(response: AiPredictResponse): List<ImageAnalysisMeasurement> {
        return response.measurements.mapIndexed { index, item ->
            ImageAnalysisMeasurement(
                key = "ai_predict_${item.type}_$index",
                type = item.type,
                value = item.angle?.let { formatAngle(it, signed = false) } ?: "--",
                points = item.points,
                description = "${item.type}测量",
                kind = AnalysisMeasurementKind.COMPUTED,
                panelVisible = true,
            )
        }
    }

    private fun mapDetectResponse(
        response: AiDetectResponse,
        calibration: CalibrationContext,
    ): List<ImageAnalysisMeasurement> {
        val output = mutableListOf<ImageAnalysisMeasurement>()
        output += computeSixMeasurements(response, calibration)
        output += buildPosePanelItems(response)
        output += buildVertebraCornerItems(response)
        return output
    }

    private fun computeManualMeasurement(
        toolId: String,
        points: List<MeasurementPoint>,
        state: ImageAnalysisUiState,
    ): ImageAnalysisMeasurement? {
        val key = nextManualMeasurementKey(toolId)
        val calibration = CalibrationContext(
            standardDistanceMm = state.standardDistanceMm,
            standardDistancePoints = state.standardDistancePoints,
        )

        return when (toolId) {
            TOOL_T1_TILT -> {
                val angle = lineAngleDegrees(points[0], points[1])
                ImageAnalysisMeasurement(
                    key = key,
                    type = "T1 Tilt",
                    value = formatAngle(angle, signed = true),
                    points = points,
                    description = "T1椎体倾斜角测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_COBB -> {
                val first = lineAngleDegrees(points[0], points[1])
                val second = lineAngleDegrees(points[2], points[3])
                val cobb = acuteAngle(first, second)
                ImageAnalysisMeasurement(
                    key = key,
                    type = "Cobb",
                    value = formatAngle(cobb, signed = false),
                    points = points,
                    description = "Cobb角测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_CA -> {
                val angle = lineAngleDegrees(points[0], points[1])
                ImageAnalysisMeasurement(
                    key = key,
                    type = "CA",
                    value = formatAngle(angle, signed = true),
                    points = points,
                    description = "锁骨角测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_PELVIC -> {
                val angle = lineAngleDegrees(points[0], points[1])
                ImageAnalysisMeasurement(
                    key = key,
                    type = "Pelvic",
                    value = formatAngle(angle, signed = true),
                    points = points,
                    description = "骨盆倾斜角测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_SACRAL -> {
                val angle = lineAngleDegrees(points[0], points[1])
                ImageAnalysisMeasurement(
                    key = key,
                    type = "Sacral",
                    value = formatAngle(angle, signed = true),
                    points = points,
                    description = "骶骨倾斜角测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_TS -> {
                val first = points[0]
                val second = points[1]
                val yRef = (first.y + second.y) / 2.0
                val left = MeasurementPoint(x = first.x, y = yRef)
                val right = MeasurementPoint(x = second.x, y = yRef)
                ImageAnalysisMeasurement(
                    key = key,
                    type = "TS",
                    value = formatDistanceValue(abs(second.x - first.x), calibration),
                    points = listOf(left, right),
                    description = "躯干偏移测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                    helperSegments = listOf(
                        AnalysisHelperSegment(
                            start = first,
                            end = MeasurementPoint(x = first.x, y = yRef),
                            dashed = true,
                        ),
                        AnalysisHelperSegment(
                            start = second,
                            end = MeasurementPoint(x = second.x, y = yRef),
                            dashed = true,
                        ),
                    ),
                )
            }

            TOOL_AVT -> {
                val first = points[0]
                val second = points[1]
                val yRef = (first.y + second.y) / 2.0
                val left = MeasurementPoint(x = first.x, y = yRef)
                val right = MeasurementPoint(x = second.x, y = yRef)
                ImageAnalysisMeasurement(
                    key = key,
                    type = "AVT",
                    value = formatDistanceValue(abs(second.x - first.x), calibration),
                    points = listOf(left, right),
                    description = "顶椎偏移测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                    helperSegments = listOf(
                        AnalysisHelperSegment(
                            start = first,
                            end = MeasurementPoint(x = first.x, y = yRef),
                            dashed = true,
                        ),
                        AnalysisHelperSegment(
                            start = second,
                            end = MeasurementPoint(x = second.x, y = yRef),
                            dashed = true,
                        ),
                    ),
                )
            }

            TOOL_STANDARD_DISTANCE -> {
                val mm = state.standardDistanceMm ?: DEFAULT_STANDARD_DISTANCE_MM
                ImageAnalysisMeasurement(
                    key = key,
                    type = "标准距离",
                    value = "${formatStandardDistanceInput(mm)}mm",
                    points = points,
                    description = "标准距离校准线",
                    kind = AnalysisMeasurementKind.COMPUTED,
                    panelVisible = false,
                )
            }

            TOOL_VERTEBRA_CENTER -> {
                val center = MeasurementPoint(
                    x = points.map { it.x }.average(),
                    y = points.map { it.y }.average(),
                )
                ImageAnalysisMeasurement(
                    key = key,
                    type = "椎体中心",
                    value = formatPointValue(center),
                    points = listOf(center),
                    description = "椎体中心标注",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_DISTANCE -> {
                val distancePx = hypot(points[1].x - points[0].x, points[1].y - points[0].y)
                ImageAnalysisMeasurement(
                    key = key,
                    type = "距离标注",
                    value = formatDistanceValue(distancePx, calibration),
                    points = points,
                    description = "距离标注",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_ANGLE -> {
                val angle = angleAtVertex(points[0], points[1], points[2])
                ImageAnalysisMeasurement(
                    key = key,
                    type = "角度标注",
                    value = formatAngle(angle, signed = false),
                    points = points,
                    description = "角度标注",
                    kind = AnalysisMeasurementKind.COMPUTED,
                )
            }

            TOOL_AUX_CIRCLE -> ImageAnalysisMeasurement(
                key = key,
                type = "Circle",
                value = "辅助图形",
                points = points,
                description = "辅助图形-Circle",
                kind = AnalysisMeasurementKind.COMPUTED,
                auxiliary = true,
            )

            TOOL_AUX_ELLIPSE -> ImageAnalysisMeasurement(
                key = key,
                type = "Ellipse",
                value = "辅助图形",
                points = points,
                description = "辅助图形-Ellipse",
                kind = AnalysisMeasurementKind.COMPUTED,
                auxiliary = true,
            )

            TOOL_AUX_BOX -> ImageAnalysisMeasurement(
                key = key,
                type = "Box",
                value = "辅助图形",
                points = points,
                description = "辅助图形-Box",
                kind = AnalysisMeasurementKind.COMPUTED,
                auxiliary = true,
            )

            TOOL_AUX_ARROW -> ImageAnalysisMeasurement(
                key = key,
                type = "Arrow",
                value = "辅助图形",
                points = points,
                description = "辅助图形-Arrow",
                kind = AnalysisMeasurementKind.COMPUTED,
                auxiliary = true,
            )

            TOOL_AUX_POLYGON -> ImageAnalysisMeasurement(
                key = key,
                type = "Polygon",
                value = "辅助图形",
                points = points,
                description = "辅助图形-Polygon",
                kind = AnalysisMeasurementKind.COMPUTED,
                auxiliary = true,
            )

            else -> null
        }
    }

    private fun nextManualMeasurementKey(toolId: String): String {
        manualIdCounter += 1
        return "manual_${toolId}_${currentEpochSeconds()}_${manualIdCounter}"
    }

    private fun buildPosePanelItems(response: AiDetectResponse): List<ImageAnalysisMeasurement> {
        val pose = response.poseKeypoints
        return posePanelOrder.map { name ->
            val node = pose[name]
            val point = node?.toPoint()
            ImageAnalysisMeasurement(
                key = "ai_detect_pose_$name",
                type = name,
                value = name,
                points = point?.let { listOf(it) } ?: emptyList(),
                description = "AI检测-躯干关键点",
                kind = AnalysisMeasurementKind.DETECTED,
                pointLabel = name,
                confidence = node?.confidence ?: node?.conf,
                panelVisible = true,
            )
        }
    }

    private fun buildVertebraCornerItems(response: AiDetectResponse): List<ImageAnalysisMeasurement> {
        val output = mutableListOf<ImageAnalysisMeasurement>()
        response.vertebrae.entries.sortedBy { it.key }.forEach { (name, node) ->
            val corners = node.corners ?: return@forEach
            listOfNotNull(
                corners.topLeft?.let { 1 to it },
                corners.topRight?.let { 2 to it },
                corners.bottomRight?.let { 3 to it },
                corners.bottomLeft?.let { 4 to it },
            ).forEach { (cornerOrder, cornerPoint) ->
                val point = cornerPoint.toPoint()
                val displayName = "$name-$cornerOrder"
                output += ImageAnalysisMeasurement(
                    key = "ai_detect_corner_${name}_$cornerOrder",
                    type = name,
                    value = displayName,
                    points = listOf(point),
                    description = "AI检测-$displayName",
                    kind = AnalysisMeasurementKind.DETECTED,
                    pointLabel = displayName,
                    confidence = cornerPoint.confidence ?: cornerPoint.conf ?: node.confidence,
                    panelVisible = true,
                )
            }
        }
        return output
    }

    private fun computeSixMeasurements(
        response: AiDetectResponse,
        calibration: CalibrationContext,
    ): List<ImageAnalysisMeasurement> {
        val output = mutableListOf<ImageAnalysisMeasurement>()
        val pose = response.poseKeypoints
        val vertebrae = response.vertebrae
        val t1Corners = vertebrae["T1"]?.corners

        output += angleMetricOrPlaceholder(
            key = "ai_compute_t1_tilt",
            type = "T1 Tilt",
            description = "T1椎体倾斜角测量",
            start = t1Corners?.topLeft?.toPoint(),
            end = t1Corners?.topRight?.toPoint(),
            signed = true,
        )

        output += measurementPlaceholder(
            key = "ai_compute_ca",
            type = "CA",
            description = "Cobb角测量",
        )

        output += angleMetricOrPlaceholder(
            key = "ai_compute_pelvic",
            type = "Pelvic",
            description = "骨盆倾斜角测量",
            start = pose["IR"]?.toPoint(),
            end = pose["IL"]?.toPoint(),
            signed = true,
        )

        output += angleMetricOrPlaceholder(
            key = "ai_compute_sacral",
            type = "Sacral",
            description = "骶骨倾斜角测量",
            start = pose["SR"]?.toPoint(),
            end = pose["SL"]?.toPoint(),
            signed = true,
        )

        val csvlX = run {
            val sr = pose["SR"]?.toPoint()
            val sl = pose["SL"]?.toPoint()
            if (sr != null && sl != null) (sr.x + sl.x) / 2.0 else null
        }
        val c7Center = vertebrae["C7"]?.corners.centerPoint()
        output += if (csvlX != null && c7Center != null) {
            val y = c7Center.y
            val left = MeasurementPoint(x = csvlX, y = y)
            val right = MeasurementPoint(x = c7Center.x, y = y)
            ImageAnalysisMeasurement(
                key = "ai_compute_ts",
                type = "TS",
                value = formatDistanceValue(abs(c7Center.x - csvlX), calibration),
                points = listOf(left, right),
                description = "躯干偏移测量",
                kind = AnalysisMeasurementKind.COMPUTED,
                panelVisible = true,
            )
        } else {
            measurementPlaceholder(
                key = "ai_compute_ts",
                type = "TS",
                description = "躯干偏移测量",
            )
        }

        val vertebraCenters = vertebrae.mapNotNull { (name, node) ->
            node.corners.centerPoint()?.let { name to it }
        }
        output += if (csvlX != null && vertebraCenters.isNotEmpty()) {
            val apex = vertebraCenters.maxByOrNull { (_, center) -> abs(center.x - csvlX) }
            if (apex != null) {
                val center = apex.second
                val left = MeasurementPoint(x = csvlX, y = center.y)
                val right = MeasurementPoint(x = center.x, y = center.y)
                ImageAnalysisMeasurement(
                    key = "ai_compute_avt",
                    type = "AVT",
                    value = formatDistanceValue(abs(center.x - csvlX), calibration),
                    points = listOf(left, right),
                    description = "顶椎偏移测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                    panelVisible = true,
                )
            } else {
                measurementPlaceholder(
                    key = "ai_compute_avt",
                    type = "AVT",
                    description = "顶椎偏移测量",
                )
            }
        } else {
            measurementPlaceholder(
                key = "ai_compute_avt",
                type = "AVT",
                description = "顶椎偏移测量",
            )
        }

        val candidates = mutableListOf<LineCandidate>()
        vertebrae.forEach { (name, node) ->
            val corners = node.corners ?: return@forEach
            corners.topLeft?.toPoint()?.let { tl ->
                corners.topRight?.toPoint()?.let { tr ->
                    candidates += LineCandidate(
                        name = "$name-Top",
                        p1 = tl,
                        p2 = tr,
                        angle = lineAngleDegrees(tl, tr),
                    )
                }
            }
            corners.bottomLeft?.toPoint()?.let { bl ->
                corners.bottomRight?.toPoint()?.let { br ->
                    candidates += LineCandidate(
                        name = "$name-Bottom",
                        p1 = bl,
                        p2 = br,
                        angle = lineAngleDegrees(bl, br),
                    )
                }
            }
        }
        output[1] = if (candidates.size >= 2) {
            val bestPair = findBestCobbPair(candidates)
            if (bestPair != null) {
                val reference = if (abs(bestPair.first.angle) >= abs(bestPair.second.angle)) bestPair.first else bestPair.second
                ImageAnalysisMeasurement(
                    key = "ai_compute_ca",
                    type = "CA",
                    value = formatAngle(bestPair.third, signed = false),
                    points = listOf(reference.p1, reference.p2),
                    description = "Cobb角测量",
                    kind = AnalysisMeasurementKind.COMPUTED,
                    panelVisible = true,
                )
            } else {
                measurementPlaceholder(
                    key = "ai_compute_ca",
                    type = "CA",
                    description = "Cobb角测量",
                )
            }
        } else {
            measurementPlaceholder(
                key = "ai_compute_ca",
                type = "CA",
                description = "Cobb角测量",
            )
        }

        return output
    }

    private fun angleMetricOrPlaceholder(
        key: String,
        type: String,
        description: String,
        start: MeasurementPoint?,
        end: MeasurementPoint?,
        signed: Boolean,
    ): ImageAnalysisMeasurement {
        if (start == null || end == null) {
            return measurementPlaceholder(
                key = key,
                type = type,
                description = description,
            )
        }
        return ImageAnalysisMeasurement(
            key = key,
            type = type,
            value = formatAngle(lineAngleDegrees(start, end), signed = signed),
            points = listOf(start, end),
            description = description,
            kind = AnalysisMeasurementKind.COMPUTED,
            panelVisible = true,
        )
    }

    private fun measurementPlaceholder(
        key: String,
        type: String,
        description: String,
    ): ImageAnalysisMeasurement {
        return ImageAnalysisMeasurement(
            key = key,
            type = type,
            value = "--",
            points = emptyList(),
            description = description,
            kind = AnalysisMeasurementKind.COMPUTED,
            panelVisible = true,
        )
    }

    private fun lineAngleDegrees(start: MeasurementPoint, end: MeasurementPoint): Double {
        val raw = atan2(end.y - start.y, end.x - start.x) * 180.0 / PI
        var normalized = raw
        while (normalized <= -90.0) normalized += 180.0
        while (normalized > 90.0) normalized -= 180.0
        return normalized
    }

    private fun findBestCobbPair(candidates: List<LineCandidate>): Triple<LineCandidate, LineCandidate, Double>? {
        var best: Triple<LineCandidate, LineCandidate, Double>? = null
        for (i in 0 until candidates.lastIndex) {
            for (j in i + 1 until candidates.size) {
                val first = candidates[i]
                val second = candidates[j]
                val angle = acuteAngle(first.angle, second.angle)
                if (best == null || angle > best.third) {
                    best = Triple(first, second, angle)
                }
            }
        }
        return best
    }

    private fun acuteAngle(first: Double, second: Double): Double {
        var diff = abs(first - second)
        if (diff > 180.0) diff = 360.0 - diff
        if (diff > 90.0) diff = 180.0 - diff
        return diff
    }

    private fun angleAtVertex(first: MeasurementPoint, vertex: MeasurementPoint, third: MeasurementPoint): Double {
        val v1x = first.x - vertex.x
        val v1y = first.y - vertex.y
        val v2x = third.x - vertex.x
        val v2y = third.y - vertex.y
        val dot = v1x * v2x + v1y * v2y
        val norm1 = hypot(v1x, v1y)
        val norm2 = hypot(v2x, v2y)
        if (norm1 == 0.0 || norm2 == 0.0) return 0.0
        val cos = (dot / (norm1 * norm2)).coerceIn(-1.0, 1.0)
        return acos(cos) * 180.0 / PI
    }

    private fun formatAngle(value: Double, signed: Boolean): String {
        val rounded = ((value * 10.0).roundToInt() / 10.0)
        return if (signed) "${rounded}°" else "${abs(rounded)}°"
    }

    private fun formatDistanceValue(pxDistance: Double, calibration: CalibrationContext): String {
        val mmPerPx = calibration.mmPerPx()
        return if (mmPerPx != null) {
            val value = pxDistance * mmPerPx
            "${((value * 10.0).roundToInt() / 10.0)}mm"
        } else {
            "${((pxDistance * 10.0).roundToInt() / 10.0)}px"
        }
    }

    private fun formatPointValue(point: MeasurementPoint): String {
        val x = ((point.x * 10.0).roundToInt() / 10.0)
        val y = ((point.y * 10.0).roundToInt() / 10.0)
        return "($x, $y)"
    }

    private fun AiPointNode.toPoint(): MeasurementPoint = MeasurementPoint(x = x, y = y)

    private fun AiVertebraCorners?.centerPoint(): MeasurementPoint? {
        val corners = this ?: return null
        corners.center?.toPoint()?.let { return it }
        val fourCorners = listOfNotNull(
            corners.topLeft?.toPoint(),
            corners.topRight?.toPoint(),
            corners.bottomLeft?.toPoint(),
            corners.bottomRight?.toPoint(),
        )
        if (fourCorners.isNotEmpty()) {
            return MeasurementPoint(
                x = fourCorners.map { it.x }.average(),
                y = fourCorners.map { it.y }.average(),
            )
        }
        val topMid = corners.topMid?.toPoint()
        val bottomMid = corners.bottomMid?.toPoint()
        if (topMid != null && bottomMid != null) {
            return MeasurementPoint(
                x = (topMid.x + bottomMid.x) / 2.0,
                y = (topMid.y + bottomMid.y) / 2.0,
            )
        }
        return null
    }

    private data class LineCandidate(
        val name: String,
        val p1: MeasurementPoint,
        val p2: MeasurementPoint,
        val angle: Double,
    )

    private data class CalibrationContext(
        val standardDistanceMm: Double?,
        val standardDistancePoints: List<MeasurementPoint>,
    ) {
        fun mmPerPx(): Double? {
            val mm = standardDistanceMm ?: return null
            if (standardDistancePoints.size < 2) return null
            val a = standardDistancePoints[0]
            val b = standardDistancePoints[1]
            val px = hypot(b.x - a.x, b.y - a.y)
            if (px <= 0.0) return null
            return mm / px
        }
    }

    private fun detectMeasurementKind(
        id: String,
        type: String,
        value: String,
        points: List<MeasurementPoint>,
    ): Triple<AnalysisMeasurementKind, String?, Boolean> {
        val raw = when {
            type.startsWith("AI检测-") -> type.removePrefix("AI检测-")
            id.startsWith("ai-detection-pose-") -> id.removePrefix("ai-detection-pose-")
            id.startsWith("ai-detection-") -> id.removePrefix("ai-detection-")
            else -> value
        }.ifBlank { type }

        val isDetected = type.startsWith("AI检测-") || id.startsWith("ai-detection-")
        if (!isDetected) {
            return Triple(AnalysisMeasurementKind.COMPUTED, null, true)
        }

        val isPose = raw in posePanelOrder
        return Triple(
            AnalysisMeasurementKind.DETECTED,
            raw,
            isPose || raw.contains('-'),
        )
    }

    private companion object {
        val posePanelOrder = listOf("CR", "CL", "IR", "IL", "SR", "SL")
        val jsonCodec = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = false
        }
    }

    private fun buildStandardDistanceLabel(valueMm: Double): String {
        val rounded = ((valueMm * 10.0).roundToInt() / 10.0)
        val display = if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
        return "标准距离 ${display}mm"
    }

    private fun formatStandardDistanceInput(valueMm: Double): String {
        val rounded = ((valueMm * 10.0).roundToInt() / 10.0)
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }

    private fun toSaveMeasurementItem(measurement: ImageAnalysisMeasurement): SaveMeasurementItem {
        if (measurement.kind == AnalysisMeasurementKind.COMPUTED) {
            return SaveMeasurementItem(
                id = measurement.key,
                type = measurement.type,
                value = measurement.value.takeUnless { it == "--" },
                points = measurement.points,
                description = measurement.description ?: defaultDescription(measurement.type),
            )
        }

        val label = measurement.pointLabel ?: measurement.type
        val normalizedLabel = label
            .removePrefix("AI检测-")
            .removePrefix("ai-detection-")
        val vertebraCorner = detectVertebraCornerLabel(normalizedLabel)
        return if (vertebraCorner != null) {
            val (vertebra, corner) = vertebraCorner
            val cornerCn = cornerCnLabel(corner)
            SaveMeasurementItem(
                id = "ai-detection-$vertebra-$corner",
                type = "AI检测-$vertebra-$corner",
                value = "$vertebra-$corner",
                points = measurement.points,
                description = "AI检测-${vertebra}角点${corner}($cornerCn) (置信度: ${formatConfidencePercent(measurement.confidence)})",
            )
        } else {
            SaveMeasurementItem(
                id = "ai-detection-pose-$normalizedLabel",
                type = "AI检测-$normalizedLabel",
                value = normalizedLabel,
                points = measurement.points,
                description = "AI检测-躯干关键点 (置信度: ${formatConfidencePercent(measurement.confidence)})",
            )
        }
    }

    private fun toGenerateReportItem(measurement: ImageAnalysisMeasurement): GenerateReportMeasurementItem? {
        if (measurement.auxiliary) return null
        if (measurement.kind == AnalysisMeasurementKind.COMPUTED && measurement.value == "--") return null
        if (measurement.kind == AnalysisMeasurementKind.COMPUTED && measurement.type == "标准距离") return null

        if (measurement.kind == AnalysisMeasurementKind.DETECTED) {
            val label = measurement.pointLabel ?: measurement.type
            val normalizedLabel = label
                .removePrefix("AI检测-")
                .removePrefix("ai-detection-")
            val vertebraCorner = detectVertebraCornerLabel(normalizedLabel)
            return if (vertebraCorner != null) {
                val (vertebra, corner) = vertebraCorner
                val cornerCn = cornerCnLabel(corner)
                GenerateReportMeasurementItem(
                    description = "AI检测-${vertebra}角点${corner}($cornerCn) (置信度: ${formatConfidencePercent(measurement.confidence)})",
                    type = "AI检测-$vertebra-$corner",
                    value = "$vertebra-$corner",
                )
            } else {
                GenerateReportMeasurementItem(
                    description = "AI检测-躯干关键点 (置信度: ${formatConfidencePercent(measurement.confidence)})",
                    type = "AI检测-$normalizedLabel",
                    value = normalizedLabel,
                )
            }
        }

        return GenerateReportMeasurementItem(
            description = measurement.description ?: defaultDescription(measurement.type),
            type = measurement.type,
            value = measurement.value,
        )
    }

    private fun detectVertebraCornerLabel(label: String): Pair<String, Int>? {
        val parts = label.split('-')
        if (parts.size != 2) return null
        val vertebra = parts[0]
        val corner = parts[1].toIntOrNull() ?: return null
        if (corner !in 1..4) return null
        return vertebra to corner
    }

    private fun cornerCnLabel(order: Int): String {
        return when (order) {
            1 -> "左上"
            2 -> "右上"
            3 -> "右下"
            4 -> "左下"
            else -> "-"
        }
    }

    private fun formatConfidencePercent(confidence: Double?): String {
        val value = confidence ?: return "--"
        val percent = if (value <= 1.0) value * 100.0 else value
        val rounded = ((percent * 10.0).roundToInt() / 10.0)
        return "${rounded}%"
    }

    private fun defaultDescription(type: String): String {
        return when {
            type.startsWith("AI检测", ignoreCase = true) -> "$type 自动检测结果"
            else -> "$type 测量"
        }
    }

    private fun ImageAnalysisMeasurement.toExportItem(): ExportMeasurementItem {
        return ExportMeasurementItem(
            id = key,
            type = type,
            value = value,
            points = points,
            description = description,
            kind = kind.name,
            pointLabel = pointLabel,
            confidence = confidence,
            panelVisible = panelVisible,
            helperSegments = helperSegments,
            auxiliary = auxiliary,
        )
    }

    private fun ExportMeasurementItem.toUiMeasurement(index: Int): ImageAnalysisMeasurement {
        val fallbackId = id?.takeIf { it.isNotBlank() } ?: "${type}#import#$index"
        val fallback = detectMeasurementKind(
            id = fallbackId,
            type = type,
            value = value ?: "--",
            points = points,
        )
        return ImageAnalysisMeasurement(
            key = fallbackId,
            type = type,
            value = value ?: "--",
            points = points,
            description = description,
            kind = kind?.let { runCatching { AnalysisMeasurementKind.valueOf(it) }.getOrNull() } ?: fallback.first,
            pointLabel = pointLabel ?: fallback.second,
            confidence = confidence,
            panelVisible = panelVisible ?: fallback.third,
            helperSegments = helperSegments ?: emptyList(),
            auxiliary = auxiliary ?: false,
        )
    }

    @Serializable
    private data class ExportAnnotationSnapshot(
        val imageId: String,
        val imageWidth: Int? = null,
        val imageHeight: Int? = null,
        val measurements: List<ExportMeasurementItem>,
        val standardDistance: Double? = null,
        val standardDistancePoints: List<MeasurementPoint> = emptyList(),
        val savedAt: String,
    )

    @Serializable
    private data class ExportMeasurementItem(
        val id: String? = null,
        val type: String,
        val value: String? = null,
        val points: List<MeasurementPoint> = emptyList(),
        val description: String? = null,
        val kind: String? = null,
        val pointLabel: String? = null,
        val confidence: Double? = null,
        val panelVisible: Boolean? = null,
        val helperSegments: List<AnalysisHelperSegment>? = null,
        val auxiliary: Boolean? = null,
    )
}




