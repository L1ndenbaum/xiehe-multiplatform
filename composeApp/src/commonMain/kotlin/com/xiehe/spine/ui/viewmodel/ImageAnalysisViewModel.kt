package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AiDetectResponse
import com.xiehe.spine.data.AiInferenceRepository
import com.xiehe.spine.data.AiPointNode
import com.xiehe.spine.data.AiPredictResponse
import com.xiehe.spine.data.GenerateReportMeasurementItem
import com.xiehe.spine.data.GenerateReportRequest
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageMeasurementItem
import com.xiehe.spine.data.MeasurementPoint
import com.xiehe.spine.data.MeasurementRepository
import com.xiehe.spine.data.SaveMeasurementItem
import com.xiehe.spine.data.SaveMeasurementsRequest
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

data class ImageAnalysisMeasurement(
    val key: String,
    val type: String,
    val value: String,
    val points: List<MeasurementPoint>,
    val description: String? = null,
)

data class ImageAnalysisUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val aiRunning: Boolean = false,
    val aiRunningLabel: String? = null,
    val fileId: Int? = null,
    val imageBytes: ByteArray? = null,
    val measurements: List<ImageAnalysisMeasurement> = emptyList(),
    val hiddenMeasurementKeys: Set<String> = emptySet(),
    val reportText: String = "",
    val standardDistanceLabel: String = "标准距离 100mm",
    val showToolsPanel: Boolean = false,
    val showSettingsPanel: Boolean = false,
    val resultsExpanded: Boolean = true,
    val zoomPercent: Int = 100,
    val contrast: Int = 0,
    val brightness: Int = 0,
    val errorMessage: String? = null,
    val bannerMessage: String? = null,
)

class ImageAnalysisViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(ImageAnalysisUiState())
    val state: StateFlow<ImageAnalysisUiState> = _state.asStateFlow()

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
                    hiddenMeasurementKeys = emptySet(),
                    imageBytes = null,
                )
            }

            var activeSession = session
            var imageBytes: ByteArray? = null
            var items: List<ImageAnalysisMeasurement> = emptyList()
            var reportText = ""
            var standardDistanceLabel = "标准距离 100mm"
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
                    standardDistanceLabel = payload.standardDistance
                        ?.let { "标准距离 ${it.roundToInt()}mm" }
                        ?: standardDistanceLabel
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
                    hiddenMeasurementKeys = emptySet(),
                    reportText = reportText,
                    standardDistanceLabel = standardDistanceLabel,
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
                hiddenMeasurementKeys = emptySet(),
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

    fun runAiDetect(
        fileId: Int,
        repository: AiInferenceRepository,
    ) {
        runAiAction(label = "AI检测中...") { bytes ->
            when (val result = repository.detectKeypoints(fileName = "image_$fileId.png", bytes = bytes)) {
                is AppResult.Success -> {
                    val overlay = mapDetectResponse(result.data)
                    _state.update {
                        it.copy(
                            aiRunning = false,
                            aiRunningLabel = null,
                            measurements = overlay,
                            hiddenMeasurementKeys = emptySet(),
                            bannerMessage = "AI检测完成，已覆盖当前标注层",
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
        runAiAction(label = "AI测量中...") { bytes ->
            when (val result = repository.predict(fileName = "image_$fileId.png", bytes = bytes)) {
                is AppResult.Success -> {
                    val overlay = mapPredictResponse(result.data)
                    _state.update {
                        it.copy(
                            aiRunning = false,
                            aiRunningLabel = null,
                            measurements = overlay,
                            hiddenMeasurementKeys = emptySet(),
                            bannerMessage = "AI测量完成，已覆盖当前标注层",
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

    fun clearMeasurements() {
        _state.update {
            it.copy(
                measurements = emptyList(),
                hiddenMeasurementKeys = emptySet(),
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
        if (snapshot.saving) {
            return
        }
        if (snapshot.measurements.isEmpty()) {
            _state.update { it.copy(bannerMessage = "暂无可保存的标注数据") }
            return
        }

        scope.launch {
            _state.update { it.copy(saving = true, bannerMessage = null, errorMessage = null) }
            var activeSession = session

            val reportRequest = GenerateReportRequest(
                examType = examType,
                imageId = fileId.toString(),
                measurements = snapshot.measurements.map { measurement ->
                    GenerateReportMeasurementItem(
                        description = measurement.description ?: defaultDescription(measurement.type),
                        type = measurement.type,
                        value = measurement.valueForReport(),
                    )
                },
            )

            val reportText = when (val reportResult = repository.generateReport(activeSession, reportRequest)) {
                is AppResult.Success -> {
                    activeSession = reportResult.data.first
                    onSessionUpdated(activeSession)
                    reportResult.data.second.report
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            errorMessage = reportResult.message,
                            bannerMessage = reportResult.message,
                        )
                    }
                    return@launch
                }
            }

            val saveRequest = SaveMeasurementsRequest(
                examType = examType,
                imageId = fileId.toString(),
                patientId = patientId?.toString(),
                measurements = snapshot.measurements.map { measurement ->
                    SaveMeasurementItem(
                        id = measurement.key,
                        type = measurement.type,
                        value = measurement.value.takeUnless { it == "--" },
                        points = measurement.points,
                        description = measurement.description ?: defaultDescription(measurement.type),
                    )
                },
                reportText = reportText,
                savedAt = Instant.fromEpochSeconds(currentEpochSeconds()).toString(),
            )

            when (val result = repository.saveMeasurements(activeSession, fileId, saveRequest)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update {
                        it.copy(
                            saving = false,
                            reportText = reportText,
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
        return ImageAnalysisMeasurement(
            key = id?.takeIf { it.isNotBlank() } ?: "$type#$index",
            type = type,
            value = valueLabel,
            points = points,
            description = description,
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
            val value = item.angle?.let { angle ->
                val rounded = ((angle * 10.0).roundToInt() / 10.0)
                "${rounded}°"
            } ?: "--"
            ImageAnalysisMeasurement(
                key = "ai_predict_${item.type}_$index",
                type = item.type,
                value = value,
                points = item.points,
                description = "${item.type}测量",
            )
        }
    }

    private fun mapDetectResponse(response: AiDetectResponse): List<ImageAnalysisMeasurement> {
        val output = mutableListOf<ImageAnalysisMeasurement>()

        val pose = response.poseKeypoints
        val linePairs = listOf(
            Triple("CA", "CR", "CL"),
            Triple("Pelvic", "IR", "IL"),
            Triple("Sacral", "SR", "SL"),
        )
        linePairs.forEachIndexed { index, (type, startKey, endKey) ->
            val start = pose[startKey]
            val end = pose[endKey]
            if (start != null && end != null) {
                val points = listOf(start.toPoint(), end.toPoint())
                output += ImageAnalysisMeasurement(
                    key = "ai_detect_pose_line_${type}_$index",
                    type = type,
                    value = formatLineAngle(points),
                    points = points,
                    description = "${type}测量",
                )
            }
        }

        pose.entries.sortedBy { it.key }.forEachIndexed { index, (name, node) ->
            output += ImageAnalysisMeasurement(
                key = "ai_detect_pose_point_${name}_$index",
                type = name,
                value = name,
                points = listOf(node.toPoint()),
                description = "AI检测-躯干关键点",
            )
        }

        response.vertebrae.entries.sortedBy { it.key }.forEachIndexed { index, (name, vertebra) ->
            val corners = vertebra.corners
            if (corners != null) {
                listOfNotNull(
                    corners.topLeft?.let { tl ->
                        corners.topRight?.let { tr -> "${name}-Top" to listOf(tl.toPoint(), tr.toPoint()) }
                    },
                    corners.bottomLeft?.let { bl ->
                        corners.bottomRight?.let { br -> "${name}-Bottom" to listOf(bl.toPoint(), br.toPoint()) }
                    },
                    corners.topMid?.let { tm ->
                        corners.bottomMid?.let { bm -> "${name}-Axis" to listOf(tm.toPoint(), bm.toPoint()) }
                    },
                ).forEachIndexed { lineIndex, (type, points) ->
                    output += ImageAnalysisMeasurement(
                        key = "ai_detect_vertebra_${name}_${lineIndex}_$index",
                        type = type,
                        value = formatLineAngle(points),
                        points = points,
                        description = "AI检测-$name 测量线",
                    )
                }
            }
        }

        return output
    }

    private fun formatLineAngle(points: List<MeasurementPoint>): String {
        if (points.size < 2) return "--"
        val dx = points[1].x - points[0].x
        val dy = points[1].y - points[0].y
        val angle = atan2(dy, dx) * 180.0 / PI
        val rounded = ((angle * 10.0).roundToInt() / 10.0)
        return "${rounded}°"
    }

    private fun AiPointNode.toPoint(): MeasurementPoint {
        return MeasurementPoint(x = x, y = y)
    }

    private fun ImageAnalysisMeasurement.valueForReport(): String {
        return value.takeUnless { it.isBlank() || it == "--" } ?: when {
            points.size >= 2 -> formatLineAngle(points)
            points.size == 1 -> type
            else -> "--"
        }
    }

    private fun defaultDescription(type: String): String {
        return when {
            type.startsWith("AI检测", ignoreCase = true) -> "$type 自动检测结果"
            else -> "$type 测量"
        }
    }
}
