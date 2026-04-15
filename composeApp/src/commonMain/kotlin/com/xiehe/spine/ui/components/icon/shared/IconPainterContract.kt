package com.xiehe.spine.ui.components.icon.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import spine.composeapp.generated.resources.Res
import spine.composeapp.generated.resources.icons_action_icon_add
import spine.composeapp.generated.resources.icons_action_icon_back
import spine.composeapp.generated.resources.icons_action_icon_bell
import spine.composeapp.generated.resources.icons_action_icon_calendar
import spine.composeapp.generated.resources.icons_action_icon_clock
import spine.composeapp.generated.resources.icons_action_icon_chevron_down
import spine.composeapp.generated.resources.icons_action_icon_chevron_right
import spine.composeapp.generated.resources.icons_action_icon_delete
import spine.composeapp.generated.resources.icons_action_icon_download
import spine.composeapp.generated.resources.icons_action_icon_edit
import spine.composeapp.generated.resources.icons_action_icon_export
import spine.composeapp.generated.resources.icons_action_icon_eye
import spine.composeapp.generated.resources.icons_action_icon_eye_off
import spine.composeapp.generated.resources.icons_action_icon_import
import spine.composeapp.generated.resources.icons_action_icon_magicwand
import spine.composeapp.generated.resources.icons_action_icon_minus
import spine.composeapp.generated.resources.icons_action_icon_phone
import spine.composeapp.generated.resources.icons_action_icon_save
import spine.composeapp.generated.resources.icons_action_icon_search
import spine.composeapp.generated.resources.icons_action_icon_upload
import spine.composeapp.generated.resources.icons_action_icon_user_plus
import spine.composeapp.generated.resources.icons_analysis_icon_ai_detect
import spine.composeapp.generated.resources.icons_analysis_icon_ai_measure
import spine.composeapp.generated.resources.icons_analysis_icon_report
import spine.composeapp.generated.resources.icons_analysis_icon_toolkit
import spine.composeapp.generated.resources.icons_measure_action_redo
import spine.composeapp.generated.resources.icons_measure_action_undo
import spine.composeapp.generated.resources.icons_measure_action_zoom
import spine.composeapp.generated.resources.icons_measure_icon_to_fullscreen
import spine.composeapp.generated.resources.icons_measure_icon_toggle_measure_list
import spine.composeapp.generated.resources.icons_measure_icon_toggle_toolkit
import spine.composeapp.generated.resources.icons_measure_toolkit_angle
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_arrow
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_box
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_circle
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_ellipse
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_horizontal_line
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_polygon
import spine.composeapp.generated.resources.icons_measure_toolkit_aux_vertical_line
import spine.composeapp.generated.resources.icons_measure_toolkit_avt
import spine.composeapp.generated.resources.icons_measure_toolkit_ca
import spine.composeapp.generated.resources.icons_measure_toolkit_c7_offset
import spine.composeapp.generated.resources.icons_measure_toolkit_cl
import spine.composeapp.generated.resources.icons_measure_toolkit_cobb
import spine.composeapp.generated.resources.icons_measure_toolkit_distance
import spine.composeapp.generated.resources.icons_measure_toolkit_lld
import spine.composeapp.generated.resources.icons_measure_toolkit_ll_l1_l4
import spine.composeapp.generated.resources.icons_measure_toolkit_ll_l1_s1
import spine.composeapp.generated.resources.icons_measure_toolkit_ll_l4_s1
import spine.composeapp.generated.resources.icons_measure_toolkit_move
import spine.composeapp.generated.resources.icons_measure_toolkit_pelvic
import spine.composeapp.generated.resources.icons_measure_toolkit_pi
import spine.composeapp.generated.resources.icons_measure_toolkit_pt
import spine.composeapp.generated.resources.icons_measure_toolkit_sacral
import spine.composeapp.generated.resources.icons_measure_toolkit_ss
import spine.composeapp.generated.resources.icons_measure_toolkit_sva
import spine.composeapp.generated.resources.icons_measure_toolkit_t10_l2
import spine.composeapp.generated.resources.icons_measure_toolkit_t1_slope
import spine.composeapp.generated.resources.icons_measure_toolkit_standard_distance
import spine.composeapp.generated.resources.icons_measure_toolkit_t1_tilt
import spine.composeapp.generated.resources.icons_measure_toolkit_tk_t2_t5
import spine.composeapp.generated.resources.icons_measure_toolkit_tk_t5_t12
import spine.composeapp.generated.resources.icons_measure_toolkit_tpa
import spine.composeapp.generated.resources.icons_measure_toolkit_ts
import spine.composeapp.generated.resources.icons_measure_toolkit_vertebra_center
import spine.composeapp.generated.resources.icons_navigation_icon_dashboard
import spine.composeapp.generated.resources.icons_navigation_icon_images
import spine.composeapp.generated.resources.icons_navigation_icon_patients
import spine.composeapp.generated.resources.icons_navigation_icon_profile
import spine.composeapp.generated.resources.icons_status_icon_check
import spine.composeapp.generated.resources.icons_status_icon_heart_pulse
import spine.composeapp.generated.resources.icons_status_icon_hourglass
import spine.composeapp.generated.resources.icons_status_icon_image
import spine.composeapp.generated.resources.icons_status_icon_lock
import spine.composeapp.generated.resources.icons_status_icon_message
import spine.composeapp.generated.resources.icons_status_icon_scan_search
import spine.composeapp.generated.resources.icons_status_icon_settings
import spine.composeapp.generated.resources.icons_status_icon_users

@Composable
expect fun platformIconPainter(glyph: IconToken): Painter

//internal fun IconToken.composeDrawable(): DrawableResource {
//    return when (this) {
//        IconToken.DASHBOARD,
//        IconToken.LAYOUT_DASHBOARD,
//        -> Res.drawable.icons_navigation_icon_dashboard
//
//        IconToken.PATIENTS -> Res.drawable.icons_navigation_icon_patients
//        IconToken.HEART_PULSE -> Res.drawable.icons_status_icon_heart_pulse
//        IconToken.IMAGES -> Res.drawable.icons_navigation_icon_images
//        IconToken.MESSAGE -> Res.drawable.icons_status_icon_message
//        IconToken.PROFILE,
//        IconToken.USER,
//        IconToken.USER_ROUND,
//        -> Res.drawable.icons_navigation_icon_profile
//
//        IconToken.USER_PLUS -> Res.drawable.icons_action_icon_user_plus
//        IconToken.BACK -> Res.drawable.icons_action_icon_back
//        IconToken.ADD -> Res.drawable.icons_action_icon_add
//        IconToken.BELL,
//        IconToken.BELL_RING,
//        -> Res.drawable.icons_action_icon_bell
//
//        IconToken.SAVE -> Res.drawable.icons_action_icon_save
//        IconToken.IMPORT -> Res.drawable.icons_action_icon_import
//        IconToken.EXPORT -> Res.drawable.icons_action_icon_export
//        IconToken.UPLOAD -> Res.drawable.icons_action_icon_upload
//        IconToken.MINUS -> Res.drawable.icons_action_icon_minus
//        IconToken.USERS -> Res.drawable.icons_status_icon_users
//        IconToken.HOURGLASS -> Res.drawable.icons_status_icon_hourglass
//        IconToken.CHECK -> Res.drawable.icons_status_icon_check
//        IconToken.IMAGE -> Res.drawable.icons_status_icon_image
//        IconToken.SCAN_SEARCH -> Res.drawable.icons_status_icon_scan_search
//        IconToken.SEARCH -> Res.drawable.icons_action_icon_search
//        IconToken.PHONE -> Res.drawable.icons_action_icon_phone
//        IconToken.EDIT -> Res.drawable.icons_action_icon_edit
//        IconToken.CALENDAR -> Res.drawable.icons_action_icon_calendar
//        IconToken.CLOCK -> Res.drawable.icons_action_icon_clock
//        IconToken.LOCK -> Res.drawable.icons_status_icon_lock
//        IconToken.SETTINGS -> Res.drawable.icons_status_icon_settings
//        IconToken.ARROW_RIGHT,
//        IconToken.CHEVRON_RIGHT,
//        -> Res.drawable.icons_action_icon_chevron_right
//
//        IconToken.CHEVRON_DOWN -> Res.drawable.icons_action_icon_chevron_down
//        IconToken.EYE -> Res.drawable.icons_action_icon_eye
//        IconToken.EYE_OFF -> Res.drawable.icons_action_icon_eye_off
//        IconToken.MAGIC_WAND -> Res.drawable.icons_action_icon_magicwand
//        IconToken.DOWNLOAD -> Res.drawable.icons_action_icon_download
//        IconToken.DELETE -> Res.drawable.icons_action_icon_delete
//        IconToken.AI_DETECT -> Res.drawable.icons_analysis_icon_ai_detect
//        IconToken.AI_MEASURE -> Res.drawable.icons_analysis_icon_ai_measure
//        IconToken.REPORT -> Res.drawable.icons_analysis_icon_report
//        IconToken.MEASURE_TOOLKIT -> Res.drawable.icons_analysis_icon_toolkit
//        IconToken.MEASURE_TOGGLE_TOOLKIT -> Res.drawable.icons_measure_icon_toggle_toolkit
//        IconToken.MEASURE_TOGGLE_MEASURE_LIST -> Res.drawable.icons_measure_icon_toggle_measure_list
//        IconToken.MEASURE_TO_FULLSCREEN -> Res.drawable.icons_measure_icon_to_fullscreen
//        IconToken.MEASURE_MOVE -> Res.drawable.icons_measure_toolkit_move
//        IconToken.MEASURE_ZOOM -> Res.drawable.icons_measure_action_zoom
//        IconToken.MEASURE_UNDO -> Res.drawable.icons_measure_action_undo
//        IconToken.MEASURE_REDO -> Res.drawable.icons_measure_action_redo
//        IconToken.MEASURE_T1_TILT -> Res.drawable.icons_measure_toolkit_t1_tilt
//        IconToken.MEASURE_COBB -> Res.drawable.icons_measure_toolkit_cobb
//        IconToken.MEASURE_CA -> Res.drawable.icons_measure_toolkit_ca
//        IconToken.MEASURE_PELVIC -> Res.drawable.icons_measure_toolkit_pelvic
//        IconToken.MEASURE_SACRAL -> Res.drawable.icons_measure_toolkit_sacral
//        IconToken.MEASURE_TS -> Res.drawable.icons_measure_toolkit_ts
//        IconToken.MEASURE_AVT -> Res.drawable.icons_measure_toolkit_avt
//        IconToken.MEASURE_LLD -> Res.drawable.icons_measure_toolkit_lld
//        IconToken.MEASURE_C7_OFFSET -> Res.drawable.icons_measure_toolkit_c7_offset
//        IconToken.MEASURE_T1_SLOPE -> Res.drawable.icons_measure_toolkit_t1_slope
//        IconToken.MEASURE_CL -> Res.drawable.icons_measure_toolkit_cl
//        IconToken.MEASURE_TK_T2_T5 -> Res.drawable.icons_measure_toolkit_tk_t2_t5
//        IconToken.MEASURE_TK_T5_T12 -> Res.drawable.icons_measure_toolkit_tk_t5_t12
//        IconToken.MEASURE_T10_L2 -> Res.drawable.icons_measure_toolkit_t10_l2
//        IconToken.MEASURE_LL_L1_S1 -> Res.drawable.icons_measure_toolkit_ll_l1_s1
//        IconToken.MEASURE_LL_L1_L4 -> Res.drawable.icons_measure_toolkit_ll_l1_l4
//        IconToken.MEASURE_LL_L4_S1 -> Res.drawable.icons_measure_toolkit_ll_l4_s1
//        IconToken.MEASURE_TPA -> Res.drawable.icons_measure_toolkit_tpa
//        IconToken.MEASURE_SVA -> Res.drawable.icons_measure_toolkit_sva
//        IconToken.MEASURE_PI -> Res.drawable.icons_measure_toolkit_pi
//        IconToken.MEASURE_PT -> Res.drawable.icons_measure_toolkit_pt
//        IconToken.MEASURE_SS -> Res.drawable.icons_measure_toolkit_ss
//        IconToken.MEASURE_STANDARD_DISTANCE -> Res.drawable.icons_measure_toolkit_standard_distance
//        IconToken.MEASURE_VERTEBRA_CENTER -> Res.drawable.icons_measure_toolkit_vertebra_center
//        IconToken.MEASURE_DISTANCE -> Res.drawable.icons_measure_toolkit_distance
//        IconToken.MEASURE_ANGLE -> Res.drawable.icons_measure_toolkit_angle
//        IconToken.MEASURE_AUX_CIRCLE -> Res.drawable.icons_measure_toolkit_aux_circle
//        IconToken.MEASURE_AUX_ELLIPSE -> Res.drawable.icons_measure_toolkit_aux_ellipse
//        IconToken.MEASURE_AUX_BOX -> Res.drawable.icons_measure_toolkit_aux_box
//        IconToken.MEASURE_AUX_ARROW -> Res.drawable.icons_measure_toolkit_aux_arrow
//        IconToken.MEASURE_AUX_POLYGON -> Res.drawable.icons_measure_toolkit_aux_polygon
//        IconToken.MEASURE_AUX_HORIZONTAL_LINE -> Res.drawable.icons_measure_toolkit_aux_horizontal_line
//        IconToken.MEASURE_AUX_VERTICAL_LINE -> Res.drawable.icons_measure_toolkit_aux_vertical_line
//    }
//}
