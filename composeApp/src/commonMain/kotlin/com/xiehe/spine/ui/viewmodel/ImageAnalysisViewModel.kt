package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.currentEpochSeconds
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.AiDetectResponse
import com.xiehe.spine.data.AiInferenceRepository
import com.xiehe.spine.data.AiPointNode
import com.xiehe.spine.data.AiPredictResponse
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.AiVertebraCorners
import com.xiehe.spine.data.ImageMeasurementItem
import com.xiehe.spine.data.MeasurementPoint
import com.xiehe.spine.data.MeasurementRepository
import com.xiehe.spine.data.SaveMeasurementItem
import com.xiehe.spine.data.SaveMeasurementsRequest
import kotlin.math.PI
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

enum class AnalysisMeasurementKind {
    COMPUTED,
    DETECTED,
}

private const val DEFAULT_STANDARD_DISTANCE_MM = 100.0
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
                    standardDistanceMm = DEFAULT_STANDARD_DISTANCE_MM,
                    standardDistancePoints = DEFAULT_STANDARD_DISTANCE_POINTS,
                    standardDistanceInput = formatStandardDistanceInput(DEFAULT_STANDARD_DISTANCE_MM),
                    hiddenMeasurementKeys = emptySet(),
                    imageBytes = null,
                )
            }

            var activeSession = session
            var imageBytes: ByteArray? = null
            var items: List<ImageAnalysisMeasurement> = emptyList()
            var reportText = ""
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
                    reportText = reportText,
                    standardDistanceLabel = buildStandardDistanceLabel(standardDistanceMm ?: DEFAULT_STANDARD_DISTANCE_MM),
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
            .filter { it.points.size >= 2 && it.value != "--" }
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

            val saveRequest = SaveMeasurementsRequest(
                examType = examType,
                imageId = fileId.toString(),
                patientId = patientId?.toString(),
                measurements = persistableMeasurements.map(::toSaveMeasurementItem),
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
            kind = AnalysisMeasurementKind.COMPUTED,
            panelVisible = true,
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

    private fun buildPosePanelItems(response: AiDetectResponse): List<ImageAnalysisMeasurement> {
        val pose = response.poseKeypoints
        return posePanelOrder.map { name ->
            val node = pose[name]
            val point = node?.toPoint()
            ImageAnalysisMeasurement(
                key = "ai_detect_pose_$name",
                type = name,
                value = point?.let(::formatPointValue) ?: "--",
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
                    value = formatPointValue(point),
                    points = listOf(point),
                    description = "AI检测-$displayName",
                    kind = AnalysisMeasurementKind.DETECTED,
                    pointLabel = displayName,
                    confidence = cornerPoint.confidence ?: cornerPoint.conf ?: node.confidence,
                    panelVisible = false,
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

    private companion object {
        val posePanelOrder = listOf("CR", "CL", "IR", "IL", "SR", "SL")
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
        val vertebraCorner = detectVertebraCornerLabel(label)
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
                id = "ai-detection-pose-$label",
                type = "AI检测-$label",
                value = label,
                points = measurement.points,
                description = "AI检测-躯干关键点 (置信度: ${formatConfidencePercent(measurement.confidence)})",
            )
        }
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
}
