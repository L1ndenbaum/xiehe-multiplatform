package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.ui.components.Avatar
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.AppIcon
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun ProfileScreen(
    session: UserSession,
    onOpenAppearance: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(
                    name = session.fullName ?: session.username,
                    size = 56.dp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = session.fullName ?: session.username,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(text = "医生", style = SpineTheme.typography.subhead, color = SpineTheme.colors.textSecondary)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            ProfileMenuRow(
                label = "个人信息",
                glyph = IconToken.PROFILE,
                onClick = onOpenPersonalInfo,
            )
            ProfileMenuRow(
                label = "修改密码",
                glyph = IconToken.LOCK,
                onClick = onOpenChangePassword,
            )
            ProfileMenuRow(
                label = "系统设置",
                glyph = IconToken.SETTINGS,
                onClick = onOpenAppearance,
            )
        }

        Button(
            text = "退出登录",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            leadingGlyph = null,
            customContainerColor = SpineTheme.colors.warning,
            customContentColor = SpineTheme.colors.onPrimary,
        )
    }
}

@Composable
private fun ProfileMenuRow(
    label: String,
    glyph: IconToken,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SpineTheme.radius.sm))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(glyph = glyph, tint = SpineTheme.colors.textTertiary)
            Text(text = label, style = SpineTheme.typography.title)
        }
        AppIcon(glyph = IconToken.CHEVRON_RIGHT, tint = SpineTheme.colors.textTertiary)
    }
}
