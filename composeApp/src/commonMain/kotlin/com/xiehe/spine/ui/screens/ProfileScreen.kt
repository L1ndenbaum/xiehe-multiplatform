package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.ui.components.SpineButton
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineText
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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SpineCard(modifier = Modifier.fillMaxWidth()) {
            SpineText(text = session.fullName ?: session.username, style = SpineTheme.typography.title)
            SpineText(text = session.email ?: "未设置邮箱")
            SpineText(text = "用户ID: ${session.userId}")
        }
        SpineButton(text = "个人信息", onClick = onOpenPersonalInfo, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.PROFILE)
        SpineButton(text = "修改密码", onClick = onOpenChangePassword, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.CHECK)
        SpineButton(text = "外观设置", onClick = onOpenAppearance, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.DASHBOARD)
        SpineButton(text = "退出登录", onClick = onLogout, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.BACK)
    }
}
