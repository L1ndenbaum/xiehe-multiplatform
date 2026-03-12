package com.xiehe.spine.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.ui.components.card.profile.ProfileMenuRow
import com.xiehe.spine.ui.components.card.profile.ProfileOrganizationPalette
import com.xiehe.spine.ui.components.card.profile.ProfilePasswordPalette
import com.xiehe.spine.ui.components.card.profile.ProfilePersonalInfoPalette
import com.xiehe.spine.ui.components.card.profile.ProfileSettingsPalette
import com.xiehe.spine.ui.components.card.profile.ProfileStat
import com.xiehe.spine.ui.components.card.profile.ProfileTag
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.icon.shared.AppIcon
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.theme.SpineTheme

private val ProfilePageBackground = Color(0xFFF1F5F9)
private val ProfileCardBorder = Color(0xFFE2E8F0)
private val ProfileCardShadow = Color(0x120F172A)

@Composable
fun ProfileScreen(
    session: UserSession,
    onOpenAppearance: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    val displayName = session.fullName?.takeIf { it.isNotBlank() } ?: session.username
    val scrollState = rememberScrollState()
    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfilePageBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .shadow(18.dp, cardShape, ambientColor = ProfileCardShadow, spotColor = ProfileCardShadow)
                .clip(cardShape)
                .background(Color.White)
                .border(1.dp, ProfileCardBorder, cardShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x408B5CF6), spotColor = Color(0x408B5CF6))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(glyph = IconToken.USER_ROUND, tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = displayName,
                            style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileTag(text = "医生", active = true)
                            ProfileTag(text = "已认证", active = false)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, ProfileCardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(glyph = IconToken.SCAN_SEARCH, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ProfileCardBorder),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileStat(label = "管理患者", value = "128", modifier = Modifier.weight(1f))
                ProfileStatDivider()
                ProfileStat(label = "本月审核", value = "56", modifier = Modifier.weight(1f))
                ProfileStatDivider()
                ProfileStat(label = "完成率", value = "98%", modifier = Modifier.weight(1f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .shadow(16.dp, cardShape, ambientColor = ProfileCardShadow, spotColor = ProfileCardShadow)
                .clip(cardShape)
                .background(Color.White)
                .border(1.dp, ProfileCardBorder, cardShape),
        ) {
            ProfileMenuRow(
                label = "个人信息",
                glyph = IconToken.PROFILE,
                palette = ProfilePersonalInfoPalette,
                onClick = onOpenPersonalInfo,
                showDivider = true,
            )
            ProfileMenuRow(
                label = "组织管理",
                glyph = IconToken.USERS,
                palette = ProfileOrganizationPalette,
                onClick = null,
                showDivider = true,
            )
            ProfileMenuRow(
                label = "修改密码",
                glyph = IconToken.LOCK,
                palette = ProfilePasswordPalette,
                onClick = onOpenChangePassword,
                showDivider = true,
            )
            ProfileMenuRow(
                label = "系统设置",
                glyph = IconToken.SETTINGS,
                palette = ProfileSettingsPalette,
                onClick = onOpenAppearance,
                showDivider = false,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .height(56.dp)
                .shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = ProfileCardShadow, spotColor = ProfileCardShadow)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, ProfileCardBorder, RoundedCornerShape(20.dp))
                .clickable(onClick = onLogout),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(glyph = IconToken.BACK, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Text(
                    text = "退出登录",
                    style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFEF4444),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(46.dp)
            .background(ProfileCardBorder),
    )
}
