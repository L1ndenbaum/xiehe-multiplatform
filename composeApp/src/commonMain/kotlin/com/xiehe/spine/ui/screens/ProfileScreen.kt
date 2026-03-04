package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val colors = SpineTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(
                    name = session.fullName ?: session.username,
                    size = 64.dp,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = session.fullName ?: session.username,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileTag(text = "医生", active = true)
                        ProfileTag(text = "已认证", active = false)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.surfaceMuted, RoundedCornerShape(SpineTheme.radius.md)),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(glyph = IconToken.IMAGE, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileStat("管理患者", "128")
                ProfileStat("本月审核", "56")
                ProfileStat("完成率", "98%")
            }
        }

        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 620.dp)) {
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
            modifier = Modifier.fillMaxWidth().widthIn(max = 620.dp),
            leadingGlyph = null,
            customContainerColor = colors.error,
            customContentColor = colors.onPrimary,
        )
    }
}

@Composable
private fun ProfileTag(
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
private fun ProfileStat(
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
private fun ProfileMenuRow(
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
