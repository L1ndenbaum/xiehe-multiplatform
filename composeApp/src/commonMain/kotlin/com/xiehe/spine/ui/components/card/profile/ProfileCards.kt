package com.xiehe.spine.ui.components.card.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun ProfileTag(
    text: String,
    active: Boolean,
) {
    val colors = SpineTheme.colors
    Text(
        text = text,
        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
        color = if (active) colors.onPrimary else colors.success,
        modifier = Modifier
            .background(
                if (active) colors.primary else colors.success.copy(alpha = 0.16f),
                RoundedCornerShape(SpineTheme.radius.full),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun ProfileStat(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = SpineTheme.typography.title)
        Text(text = label, style = SpineTheme.typography.caption, color = SpineTheme.colors.textSecondary)
    }
}

@Composable
fun ProfileMenuRow(
    label: String,
    glyph: IconToken,
    onClick: () -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(colors.surface, RoundedCornerShape(SpineTheme.radius.md))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colors.primaryMuted, RoundedCornerShape(SpineTheme.radius.md)),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(glyph = glyph, tint = colors.primary, modifier = Modifier.size(15.dp))
            }
            Text(text = label, style = SpineTheme.typography.body)
        }
        AppIcon(glyph = IconToken.CHEVRON_RIGHT, tint = colors.textTertiary)
    }
}
