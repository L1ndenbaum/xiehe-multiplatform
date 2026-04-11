package com.xiehe.spine.ui.components.analysis.viewer.catalog

import com.xiehe.spine.ui.components.icon.shared.IconToken

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

enum class AnnotationToolSection(val title: String) {
    BASIC("基础模式"),
    MEASURE("测量标注"),
    AUXILIARY("辅助图形"),
}

data class AnnotationToolDefinition(
    val id: String,
    val label: String,
    val icon: IconToken,
    val section: AnnotationToolSection,
    val pointsNeeded: Int,
    val supportsDoubleTapFinish: Boolean = false,
)

val ANNOTATION_TOOL_CATALOG = listOf(
    AnnotationToolDefinition(
        id = TOOL_MOVE,
        label = "移动",
        icon = IconToken.MEASURE_MOVE,
        section = AnnotationToolSection.BASIC,
        pointsNeeded = 0,
    ),
    AnnotationToolDefinition(
        id = TOOL_T1_TILT,
        label = "T1 Tilt",
        icon = IconToken.MEASURE_T1_TILT,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_COBB,
        label = "Cobb",
        icon = IconToken.MEASURE_COBB,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 4,
    ),
    AnnotationToolDefinition(
        id = TOOL_CA,
        label = "CA",
        icon = IconToken.MEASURE_CA,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_PELVIC,
        label = "Pelvic",
        icon = IconToken.MEASURE_PELVIC,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_SACRAL,
        label = "Sacral",
        icon = IconToken.MEASURE_SACRAL,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_TS,
        label = "TS",
        icon = IconToken.MEASURE_TS,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_AVT,
        label = "AVT",
        icon = IconToken.MEASURE_AVT,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_STANDARD_DISTANCE,
        label = "标准距离",
        icon = IconToken.MEASURE_STANDARD_DISTANCE,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_VERTEBRA_CENTER,
        label = "椎体中心",
        icon = IconToken.MEASURE_VERTEBRA_CENTER,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 4,
    ),
    AnnotationToolDefinition(
        id = TOOL_DISTANCE,
        label = "距离",
        icon = IconToken.MEASURE_DISTANCE,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_ANGLE,
        label = "角度",
        icon = IconToken.MEASURE_ANGLE,
        section = AnnotationToolSection.MEASURE,
        pointsNeeded = 3,
    ),
    AnnotationToolDefinition(
        id = TOOL_AUX_CIRCLE,
        label = "Circle",
        icon = IconToken.MEASURE_AUX_CIRCLE,
        section = AnnotationToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_AUX_ELLIPSE,
        label = "Ellipse",
        icon = IconToken.MEASURE_AUX_ELLIPSE,
        section = AnnotationToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_AUX_BOX,
        label = "Box",
        icon = IconToken.MEASURE_AUX_BOX,
        section = AnnotationToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_AUX_ARROW,
        label = "Arrow",
        icon = IconToken.MEASURE_AUX_ARROW,
        section = AnnotationToolSection.AUXILIARY,
        pointsNeeded = 2,
    ),
    AnnotationToolDefinition(
        id = TOOL_AUX_POLYGON,
        label = "Polygon",
        icon = IconToken.MEASURE_AUX_POLYGON,
        section = AnnotationToolSection.AUXILIARY,
        pointsNeeded = 0,
        supportsDoubleTapFinish = true,
    ),
)

private val annotationToolMap = ANNOTATION_TOOL_CATALOG.associateBy(AnnotationToolDefinition::id)

fun getAnnotationTool(toolId: String): AnnotationToolDefinition? = annotationToolMap[toolId]
