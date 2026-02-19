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
import spine.composeapp.generated.resources.icons_action_icon_eye
import spine.composeapp.generated.resources.icons_action_icon_eye_off
import spine.composeapp.generated.resources.icons_action_icon_search
import spine.composeapp.generated.resources.icons_navigation_icon_dashboard
import spine.composeapp.generated.resources.icons_navigation_icon_images
import spine.composeapp.generated.resources.icons_navigation_icon_patients
import spine.composeapp.generated.resources.icons_navigation_icon_profile
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
    }
}
