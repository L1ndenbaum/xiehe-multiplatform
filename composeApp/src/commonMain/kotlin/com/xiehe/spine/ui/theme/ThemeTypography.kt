package com.xiehe.spine.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal fun defaultTypography(): SpineAppTypography {
    val family = FontFamily.SansSerif
    return SpineAppTypography(
        display = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 32.sp),
        title = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        body = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 15.sp),
        subhead = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 13.sp),
        caption = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    )
}
