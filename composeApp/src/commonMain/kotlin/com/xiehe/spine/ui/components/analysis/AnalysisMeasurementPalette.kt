package com.xiehe.spine.ui.components

import androidx.compose.ui.graphics.Color
import com.xiehe.spine.ui.viewmodel.AnalysisMeasurementKind
import com.xiehe.spine.ui.viewmodel.ImageAnalysisMeasurement

object AnalysisMeasurementPalette {
    private val metricColors = mapOf(
        "T1 Tilt" to Color(0xFF4AA3FF),
        "Cobb" to Color(0xFFF2C94C),
        "CA" to Color(0xFF56CCF2),
        "Pelvic" to Color(0xFFBB6BD9),
        "Sacral" to Color(0xFFF2994A),
        "AVT" to Color(0xFF27AE60),
        "TS" to Color(0xFF2D9CDB),
        "椎体中心" to Color(0xFF2D9CDB),
        "距离标注" to Color(0xFF56CCF2),
        "角度标注" to Color(0xFFF2C94C),
    )

    val detectedPointColor: Color = Color(0xFF6FCF97)
    val helperDashedColor: Color = Color(0xFF27AE60)
    val draftColor: Color = Color(0xFFEB5757)
    val labelBackground: Color = Color(0xCC0B1220)

    fun colorFor(measurement: ImageAnalysisMeasurement): Color {
        if (measurement.kind == AnalysisMeasurementKind.DETECTED) return detectedPointColor
        return metricColors[measurement.type] ?: Color(0xFFE0E7FF)
    }

    fun valueColorFor(measurement: ImageAnalysisMeasurement): Color {
        return if (measurement.kind == AnalysisMeasurementKind.DETECTED) {
            detectedPointColor
        } else {
            metricColors[measurement.type] ?: Color(0xFFE0E7FF)
        }
    }

    fun shouldShowMetricTag(measurement: ImageAnalysisMeasurement): Boolean {
        if (measurement.kind != AnalysisMeasurementKind.COMPUTED) return false
        if (measurement.auxiliary) return false
        if (measurement.type == "标准距离") return false
        if (measurement.value == "--") return false
        return measurement.points.size >= 2 || measurement.type == "椎体中心"
    }

    fun supportsColoredTag(type: String): Boolean {
        return metricColors.containsKey(type)
    }
}
