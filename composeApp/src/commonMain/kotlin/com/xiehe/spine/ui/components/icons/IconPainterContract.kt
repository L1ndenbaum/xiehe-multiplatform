package com.xiehe.spine.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import spine.composeapp.generated.resources.Res
import spine.composeapp.generated.resources.icons_action_icon_add
import spine.composeapp.generated.resources.icons_action_icon_back
import spine.composeapp.generated.resources.icons_action_icon_bell
import spine.composeapp.generated.resources.icons_action_icon_calendar
import spine.composeapp.generated.resources.icons_action_icon_chevron_down
import spine.composeapp.generated.resources.icons_action_icon_chevron_right
import spine.composeapp.generated.resources.icons_action_icon_delete
import spine.composeapp.generated.resources.icons_action_icon_download
import spine.composeapp.generated.resources.icons_action_icon_eye
import spine.composeapp.generated.resources.icons_action_icon_eye_off
import spine.composeapp.generated.resources.icons_action_icon_export
import spine.composeapp.generated.resources.icons_action_icon_import
import spine.composeapp.generated.resources.icons_action_icon_minus
import spine.composeapp.generated.resources.icons_action_icon_save
import spine.composeapp.generated.resources.icons_action_icon_search
import spine.composeapp.generated.resources.icons_analysis_icon_ai_detect
import spine.composeapp.generated.resources.icons_analysis_icon_ai_measure
import spine.composeapp.generated.resources.icons_analysis_icon_report
import spine.composeapp.generated.resources.icons_analysis_icon_toolkit
import spine.composeapp.generated.resources.icons_navigation_icon_dashboard
import spine.composeapp.generated.resources.icons_navigation_icon_images
import spine.composeapp.generated.resources.icons_navigation_icon_patients
import spine.composeapp.generated.resources.icons_navigation_icon_profile
import spine.composeapp.generated.resources.icons_measure_toolkit_avt
import spine.composeapp.generated.resources.icons_measure_toolkit_ca
import spine.composeapp.generated.resources.icons_measure_toolkit_cobb
import spine.composeapp.generated.resources.icons_measure_toolkit_move
import spine.composeapp.generated.resources.icons_measure_toolkit_pelvic
import spine.composeapp.generated.resources.icons_measure_toolkit_standard_distance
import spine.composeapp.generated.resources.icons_measure_toolkit_t1_tilt
import spine.composeapp.generated.resources.icons_measure_toolkit_ts
import spine.composeapp.generated.resources.icons_status_icon_check
import spine.composeapp.generated.resources.icons_status_icon_hourglass
import spine.composeapp.generated.resources.icons_status_icon_image
import spine.composeapp.generated.resources.icons_status_icon_lock
import spine.composeapp.generated.resources.icons_status_icon_message
import spine.composeapp.generated.resources.icons_status_icon_settings
import spine.composeapp.generated.resources.icons_status_icon_users

@Composable
expect fun platformIconPainter(glyph: IconToken): Painter

internal fun IconToken.composeDrawable(): DrawableResource {
    return when (this) {
        IconToken.DASHBOARD -> Res.drawable.icons_navigation_icon_dashboard
        IconToken.PATIENTS -> Res.drawable.icons_navigation_icon_patients
        IconToken.IMAGES -> Res.drawable.icons_navigation_icon_images
        IconToken.MESSAGE -> Res.drawable.icons_status_icon_message
        IconToken.PROFILE -> Res.drawable.icons_navigation_icon_profile
        IconToken.BACK -> Res.drawable.icons_action_icon_back
        IconToken.ADD -> Res.drawable.icons_action_icon_add
        IconToken.BELL -> Res.drawable.icons_action_icon_bell
        IconToken.SAVE -> Res.drawable.icons_action_icon_save
        IconToken.IMPORT -> Res.drawable.icons_action_icon_import
        IconToken.EXPORT -> Res.drawable.icons_action_icon_export
        IconToken.MINUS -> Res.drawable.icons_action_icon_minus
        IconToken.USERS -> Res.drawable.icons_status_icon_users
        IconToken.HOURGLASS -> Res.drawable.icons_status_icon_hourglass
        IconToken.CHECK -> Res.drawable.icons_status_icon_check
        IconToken.IMAGE -> Res.drawable.icons_status_icon_image
        IconToken.SEARCH -> Res.drawable.icons_action_icon_search
        IconToken.CALENDAR -> Res.drawable.icons_action_icon_calendar
        IconToken.LOCK -> Res.drawable.icons_status_icon_lock
        IconToken.SETTINGS -> Res.drawable.icons_status_icon_settings
        IconToken.CHEVRON_DOWN -> Res.drawable.icons_action_icon_chevron_down
        IconToken.CHEVRON_RIGHT -> Res.drawable.icons_action_icon_chevron_right
        IconToken.EYE -> Res.drawable.icons_action_icon_eye
        IconToken.EYE_OFF -> Res.drawable.icons_action_icon_eye_off
        IconToken.DOWNLOAD -> Res.drawable.icons_action_icon_download
        IconToken.DELETE -> Res.drawable.icons_action_icon_delete
        IconToken.AI_DETECT -> Res.drawable.icons_analysis_icon_ai_detect
        IconToken.AI_MEASURE -> Res.drawable.icons_analysis_icon_ai_measure
        IconToken.REPORT -> Res.drawable.icons_analysis_icon_report
        IconToken.MEASURE_TOOLKIT -> Res.drawable.icons_analysis_icon_toolkit
        IconToken.MEASURE_MOVE -> Res.drawable.icons_measure_toolkit_move
        IconToken.MEASURE_T1_TILT -> Res.drawable.icons_measure_toolkit_t1_tilt
        IconToken.MEASURE_COBB -> Res.drawable.icons_measure_toolkit_cobb
        IconToken.MEASURE_CA -> Res.drawable.icons_measure_toolkit_ca
        IconToken.MEASURE_PELVIC -> Res.drawable.icons_measure_toolkit_pelvic
        IconToken.MEASURE_TS -> Res.drawable.icons_measure_toolkit_ts
        IconToken.MEASURE_AVT -> Res.drawable.icons_measure_toolkit_avt
        IconToken.MEASURE_STANDARD_DISTANCE -> Res.drawable.icons_measure_toolkit_standard_distance
    }
}
