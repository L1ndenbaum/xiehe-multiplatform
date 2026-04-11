package com.xiehe.spine.ui.components.analysis.image

import androidx.compose.ui.graphics.Color
import com.xiehe.spine.ui.viewmodel.image.AnalysisMeasurementKind
import com.xiehe.spine.ui.viewmodel.image.ImageAnalysisMeasurement

object AnalysisMeasurementPalette {
    private val metricColors = mapOf(
        "T1 Tilt" to Color(0xFF4AA3FF),
        "Cobb" to Color(0xFFF2C94C),
        "CA" to Color(0xFF56CCF2),
        "Pelvic" to Color(0xFFBB6BD9),
        "Sacral" to Color(0xFFF2994A),
        "AVT" to Color(0xFF27AE60),
        "TTS" to Color(0xFF2D9CDB),
        "LLD" to Color(0xFFEB5757),
        "TS(Trunk Shift)" to Color(0xFF2F80ED),
        "T1 Slope" to Color(0xFF4AA3FF),
        "C2-C7 CL" to Color(0xFFF2C94C),
        "TK T2-T5" to Color(0xFFF2994A),
        "TK T5-T12" to Color(0xFFBB6BD9),
        "T10-L2" to Color(0xFF9B51E0),
        "LL L1-S1" to Color(0xFF6FCF97),
        "LL L1-L4" to Color(0xFF27AE60),
        "LL L4-S1" to Color(0xFF219653),
        "TPA" to Color(0xFF56CCF2),
        "SVA" to Color(0xFF2D9CDB),
        "PI" to Color(0xFFF2994A),
        "PT" to Color(0xFFEB5757),
        "SS" to Color(0xFFBB6BD9),
        "椎体中心" to Color(0xFF2D9CDB),
        "距离标注" to Color(0xFF56CCF2),
        "长度测量" to Color(0xFF56CCF2),
        "角度标注" to Color(0xFFF2C94C),
        "角度测量" to Color(0xFFF2C94C),
        "辅助水平线" to Color(0xFF27AE60),
        "辅助垂直线" to Color(0xFF27AE60),
        "Auxiliary Circle" to Color(0xFF94A3B8),
        "Auxiliary Ellipse" to Color(0xFF94A3B8),
        "Auxiliary Box" to Color(0xFF94A3B8),
        "Arrow" to Color(0xFF94A3B8),
        "Polygons" to Color(0xFF94A3B8),
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


