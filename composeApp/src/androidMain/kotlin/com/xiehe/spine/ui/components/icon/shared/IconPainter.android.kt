package com.xiehe.spine.ui.components.icon.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
actual fun platformIconPainter(glyph: IconToken): Painter {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(
        glyph.androidDrawableName(),
        "drawable",
        context.packageName,
    )
    return painterResource(id = if (resId != 0) resId else android.R.drawable.ic_menu_help)
}

private fun IconToken.androidDrawableName(): String {
    return when (this) {
        IconToken.DASHBOARD,
        IconToken.LAYOUT_DASHBOARD,
        -> "icons_navigation_icon_dashboard"

        IconToken.PATIENTS -> "icons_navigation_icon_patients"
        IconToken.HEART_PULSE -> "icons_status_icon_heart_pulse"
        IconToken.IMAGES -> "icons_navigation_icon_images"
        IconToken.MESSAGE -> "icons_status_icon_message"
        IconToken.PROFILE,
        IconToken.USER,
        IconToken.USER_ROUND,
        -> "icons_navigation_icon_profile"

        IconToken.USER_PLUS -> "icons_action_icon_user_plus"
        IconToken.BACK -> "icons_action_icon_back"
        IconToken.ADD -> "icons_action_icon_add"
        IconToken.BELL,
        IconToken.BELL_RING,
        -> "icons_action_icon_bell"

        IconToken.SAVE -> "icons_action_icon_save"
        IconToken.IMPORT -> "icons_action_icon_import"
        IconToken.EXPORT -> "icons_action_icon_export"
        IconToken.UPLOAD -> "icons_action_icon_upload"
        IconToken.MINUS -> "icons_action_icon_minus"
        IconToken.USERS -> "icons_status_icon_users"
        IconToken.HOURGLASS -> "icons_status_icon_hourglass"
        IconToken.CHECK -> "icons_status_icon_check"
        IconToken.IMAGE -> "icons_status_icon_image"
        IconToken.MAGIC_WAND -> "icons_action_icon_magicwand"
        IconToken.SCAN_SEARCH -> "icons_status_icon_scan_search"
        IconToken.SEARCH -> "icons_action_icon_search"
        IconToken.PHONE -> "icons_action_icon_phone"
        IconToken.EDIT -> "icons_action_icon_edit"
        IconToken.CALENDAR -> "icons_action_icon_calendar"
        IconToken.CLOCK -> "icons_action_icon_clock"
        IconToken.LOCK -> "icons_status_icon_lock"
        IconToken.SETTINGS -> "icons_status_icon_settings"
        IconToken.ARROW_RIGHT,
        IconToken.CHEVRON_RIGHT,
        -> "icons_action_icon_chevron_right"

        IconToken.CHEVRON_DOWN -> "icons_action_icon_chevron_down"
        IconToken.EYE -> "icons_action_icon_eye"
        IconToken.EYE_OFF -> "icons_action_icon_eye_off"
        IconToken.DOWNLOAD -> "icons_action_icon_download"
        IconToken.DELETE -> "icons_action_icon_delete"
        IconToken.AI_DETECT -> "icons_analysis_icon_ai_detect"
        IconToken.AI_MEASURE -> "icons_analysis_icon_ai_measure"
        IconToken.REPORT -> "icons_analysis_icon_report"
        IconToken.MEASURE_TOOLKIT -> "icons_analysis_icon_toolkit"
        IconToken.MEASURE_MOVE -> "icons_measure_toolkit_move"
        IconToken.MEASURE_T1_TILT -> "icons_measure_toolkit_t1_tilt"
        IconToken.MEASURE_COBB -> "icons_measure_toolkit_cobb"
        IconToken.MEASURE_CA -> "icons_measure_toolkit_ca"
        IconToken.MEASURE_PELVIC -> "icons_measure_toolkit_pelvic"
        IconToken.MEASURE_SACRAL -> "icons_measure_toolkit_sacral"
        IconToken.MEASURE_TS -> "icons_measure_toolkit_ts"
        IconToken.MEASURE_AVT -> "icons_measure_toolkit_avt"
        IconToken.MEASURE_STANDARD_DISTANCE -> "icons_measure_toolkit_standard_distance"
        IconToken.MEASURE_VERTEBRA_CENTER -> "icons_measure_toolkit_vertebra_center"
        IconToken.MEASURE_DISTANCE -> "icons_measure_toolkit_distance"
        IconToken.MEASURE_ANGLE -> "icons_measure_toolkit_angle"
        IconToken.MEASURE_AUX_CIRCLE -> "icons_measure_toolkit_aux_circle"
        IconToken.MEASURE_AUX_ELLIPSE -> "icons_measure_toolkit_aux_ellipse"
        IconToken.MEASURE_AUX_BOX -> "icons_measure_toolkit_aux_box"
        IconToken.MEASURE_AUX_ARROW -> "icons_measure_toolkit_aux_arrow"
        IconToken.MEASURE_AUX_POLYGON -> "icons_measure_toolkit_aux_polygon"
    }
}

