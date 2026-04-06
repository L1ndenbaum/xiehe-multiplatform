package com.xiehe.spine.ui.components.icon.shared

import com.xiehe.spine.ui.components.feedback.shared.Text
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.theme.SpineTheme

enum class IconToken {
    DASHBOARD,
    LAYOUT_DASHBOARD,
    PATIENTS,
    HEART_PULSE,
    IMAGES,
    MESSAGE,
    PROFILE,
    USER,
    USER_ROUND,
    USER_PLUS,
    BACK,
    ADD,
    BELL,
    BELL_RING,
    SAVE,
    IMPORT,
    EXPORT,
    UPLOAD,
    MINUS,
    USERS,
    HOURGLASS,
    CHECK,
    IMAGE,
    SCAN_SEARCH,
    SEARCH,
    PHONE,
    EDIT,
    CALENDAR,
    CLOCK,
    LOCK,
    SETTINGS,
    ARROW_RIGHT,
    CHEVRON_DOWN,
    CHEVRON_RIGHT,
    MAGIC_WAND,
    EYE,
    EYE_OFF,
    DOWNLOAD,
    DELETE,
    AI_DETECT,
    AI_MEASURE,
    REPORT,
    MEASURE_TOOLKIT,
    MEASURE_MOVE,
    MEASURE_T1_TILT,
    MEASURE_COBB,
    MEASURE_CA,
    MEASURE_PELVIC,
    MEASURE_SACRAL,
    MEASURE_TS,
    MEASURE_AVT,
    MEASURE_STANDARD_DISTANCE,
    MEASURE_VERTEBRA_CENTER,
    MEASURE_DISTANCE,
    MEASURE_ANGLE,
    MEASURE_AUX_CIRCLE,
    MEASURE_AUX_ELLIPSE,
    MEASURE_AUX_BOX,
    MEASURE_AUX_ARROW,
    MEASURE_AUX_POLYGON,
}

@Composable
fun AppIcon(
    glyph: IconToken,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.textSecondary,
) {
    Image(
        painter = platformIconPainter(glyph),
        contentDescription = glyph.name,
        modifier = Modifier.size(18.dp).then(modifier),
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    tint: Color = SpineTheme.colors.primary,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(SpineTheme.radius.full))
            .background(SpineTheme.colors.primaryMuted.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.72f)) {
            val stroke = size.minDimension * 0.14f
            drawArc(
                color = tint.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke),
            )
            drawArc(
                color = tint,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                style = Stroke(stroke),
            )
        }
    }
}
