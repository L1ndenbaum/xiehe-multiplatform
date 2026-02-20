package com.xiehe.spine.ui.viewmodel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageMeasurementItem
import com.xiehe.spine.data.MeasurementPoint
import com.xiehe.spine.data.MeasurementRepository
import com.xiehe.spine.data.SaveMeasurementItem
import com.xiehe.spine.data.SaveMeasurementsRequest
import kotlin.math.roundToInt
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
)

data class ImageAnalysisUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
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
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val snapshot = _state.value
        val fileId = snapshot.fileId ?: return
        if (snapshot.measurements.isEmpty()) {
            _state.update { it.copy(bannerMessage = "暂无可保存的标注数据") }
            return
        }

        scope.launch {
            _state.update { it.copy(saving = true, bannerMessage = null, errorMessage = null) }
            val request = SaveMeasurementsRequest(
                imageId = fileId.toString(),
                measurements = snapshot.measurements.map {
                    SaveMeasurementItem(
                        type = it.type,
                        value = it.value.takeIf { value -> value != "--" },
                        points = it.points,
                        description = null,
                    )
                },
                reportText = snapshot.reportText.ifBlank { null },
            )
            when (val result = repository.saveMeasurements(session, request)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update {
                        it.copy(
                            saving = false,
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
        )
    }

    private fun JsonElement?.jsonContent(): String? {
        val primitive = this ?: return null
        return runCatching { primitive.jsonPrimitive.content }.getOrNull()
    }
}
