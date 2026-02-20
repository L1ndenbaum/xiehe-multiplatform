package com.xiehe.spine.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun AnalysisTopBar(
    modifier: Modifier = Modifier,
    doctorName: String,
    fileId: Int,
    patientId: Int?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onImportJson: () -> Unit,
    onExportJson: () -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.95f),
                        colors.primary.copy(alpha = 0.82f),
                    ),
                ),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(colors.onPrimary.copy(alpha = 0.2f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(glyph = IconToken.BACK, tint = colors.onPrimary, modifier = Modifier.size(15.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "$doctorName · 正位X光片",
                style = SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onPrimary,
                maxLines = 1,
            )
            Text(
                text = "影像ID: $fileId｜患者ID: ${patientId ?: "--"}",
                style = SpineTheme.typography.caption,
                color = colors.onPrimary.copy(alpha = 0.88f),
                maxLines = 1,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopActionButton(
                text = "保存",
                icon = IconToken.SAVE,
                onClick = onSave,
                isPrimary = true,
                modifier = Modifier.height(52.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TopActionButton(
                    text = "导入",
                    icon = IconToken.IMPORT,
                    onClick = onImportJson,
                    isPrimary = false,
                    modifier = Modifier.height(23.dp),
                )
                TopActionButton(
                    text = "导出",
                    icon = IconToken.EXPORT,
                    onClick = onExportJson,
                    isPrimary = false,
                    modifier = Modifier.height(23.dp),
                )
            }
        }
    }
}

@Composable
private fun TopActionButton(
    text: String,
    icon: IconToken,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isPrimary) {
                    colors.onPrimary.copy(alpha = 0.18f)
                } else {
                    colors.onPrimary.copy(alpha = 0.12f)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            glyph = icon,
            tint = colors.onPrimary,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = text,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onPrimary,
            modifier = Modifier.padding(start = 3.dp),
        )
    }
}
