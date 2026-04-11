package com.xiehe.spine.ui.components.analysis.viewer.catalog

import com.xiehe.spine.data.image.ImageCategory

private val commonMeasurementToolIds = listOf(
    TOOL_T1_TILT,
    TOOL_COBB,
    TOOL_CA,
    TOOL_PELVIC,
    TOOL_SACRAL,
    TOOL_TS,
    TOOL_AVT,
    TOOL_STANDARD_DISTANCE,
    TOOL_VERTEBRA_CENTER,
    TOOL_DISTANCE,
    TOOL_ANGLE,
)

private val auxiliaryToolIds = listOf(
    TOOL_AUX_CIRCLE,
    TOOL_AUX_ELLIPSE,
    TOOL_AUX_BOX,
    TOOL_AUX_ARROW,
    TOOL_AUX_POLYGON,
)

private fun mapToolIdsToCatalog(toolIds: List<String>): List<AnnotationToolDefinition> {
    return toolIds.mapNotNull(::getAnnotationTool)
}

fun getAnteriorTools(): List<AnnotationToolDefinition> {
    return mapToolIdsToCatalog(listOf(TOOL_MOVE) + commonMeasurementToolIds + auxiliaryToolIds)
}

fun getLateralTools(): List<AnnotationToolDefinition> {
    return mapToolIdsToCatalog(listOf(TOOL_MOVE) + commonMeasurementToolIds + auxiliaryToolIds)
}

fun getGenericTools(): List<AnnotationToolDefinition> {
    return mapToolIdsToCatalog(listOf(TOOL_MOVE) + commonMeasurementToolIds + auxiliaryToolIds)
}

fun getToolsForExamType(examType: String): List<AnnotationToolDefinition> {
    return when (ImageCategory.fromRaw(examType) ?: ImageCategory.fromLabel(examType)) {
        ImageCategory.FRONT,
        ImageCategory.LEFT_BENDING,
        ImageCategory.RIGHT_BENDING,
        -> getAnteriorTools()

        ImageCategory.SIDE -> getLateralTools()
        ImageCategory.POSTURE_PHOTO,
        null,
        -> getGenericTools()
    }
}
