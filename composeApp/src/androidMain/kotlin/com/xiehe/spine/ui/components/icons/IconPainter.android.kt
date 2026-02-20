package com.xiehe.spine.ui.components

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
        IconToken.DASHBOARD -> "icons_navigation_icon_dashboard"
        IconToken.PATIENTS -> "icons_navigation_icon_patients"
        IconToken.IMAGES -> "icons_navigation_icon_images"
        IconToken.MESSAGE -> "icons_status_icon_message"
        IconToken.PROFILE -> "icons_navigation_icon_profile"
        IconToken.BACK -> "icons_action_icon_back"
        IconToken.ADD -> "icons_action_icon_add"
        IconToken.BELL -> "icons_action_icon_bell"
        IconToken.SAVE -> "icons_action_icon_save"
        IconToken.IMPORT -> "icons_action_icon_import"
        IconToken.EXPORT -> "icons_action_icon_export"
        IconToken.MINUS -> "icons_action_icon_minus"
        IconToken.USERS -> "icons_status_icon_users"
        IconToken.HOURGLASS -> "icons_status_icon_hourglass"
        IconToken.CHECK -> "icons_status_icon_check"
        IconToken.IMAGE -> "icons_status_icon_image"
        IconToken.SEARCH -> "icons_action_icon_search"
        IconToken.CALENDAR -> "icons_action_icon_calendar"
        IconToken.LOCK -> "icons_status_icon_lock"
        IconToken.SETTINGS -> "icons_status_icon_settings"
        IconToken.CHEVRON_DOWN -> "icons_action_icon_chevron_down"
        IconToken.CHEVRON_RIGHT -> "icons_action_icon_chevron_right"
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
        IconToken.MEASURE_TS -> "icons_measure_toolkit_ts"
        IconToken.MEASURE_AVT -> "icons_measure_toolkit_avt"
        IconToken.MEASURE_STANDARD_DISTANCE -> "icons_measure_toolkit_standard_distance"
    }
}
