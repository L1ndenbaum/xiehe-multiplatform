package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.ImageTaskAction
import com.xiehe.spine.ui.components.ImageTaskActionStyle
import com.xiehe.spine.ui.components.ImageTaskCard
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.inferExamType
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.PatientDetailViewModel

@Composable
fun PatientDetailScreen(
    patientId: Int,
    vm: PatientDetailViewModel,
    session: UserSession,
    patientRepository: PatientRepository,
    imageRepository: ImageFileRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onOpenAnalysis: (Int, Int?, String) -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(patientId, session.accessToken) {
        vm.load(
            patientId = patientId,
            session = session,
            patientRepository = patientRepository,
            imageRepository = imageRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                state.errorMessage?.let {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
                    }
                }
            }

            val detail = state.detail
            if (detail == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = if (state.loading) "加载患者详情中..." else "暂无患者详情")
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        val (phonePrefix, phoneNumber) = splitPhone(detail.phone)
                        Text("姓名：${detail.name}", style = SpineTheme.typography.title)
                        Text("患者ID：${detail.patientId}")
                        Text("性别：${detail.gender}")
                        Text("年龄：${detail.age}岁")
                        Text("出生日期：${detail.birthDate}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("联系电话：")
                            if (phoneNumber == null) {
                                Text("-")
                            } else {
                                if (phonePrefix != null) {
                                    Text(
                                        text = phonePrefix,
                                        style = SpineTheme.typography.caption,
                                        color = SpineTheme.colors.onPrimary,
                                        modifier = Modifier
                                            .background(
                                                color = SpineTheme.colors.primary,
                                                shape = RoundedCornerShape(SpineTheme.radius.full),
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(phoneNumber)
                            }
                        }
                        Text("身份证号：${detail.idCard ?: "-"}")
                        Text("地址：${detail.address ?: "-"}")
                        Text("紧急联系人：${detail.emergencyContactName ?: "-"}")
                        Text("紧急联系人电话：${detail.emergencyContactPhone ?: "-"}")
                    }
                }

                item {
                    Text(
                        text = "影像记录",
                        style = SpineTheme.typography.title,
                        color = SpineTheme.colors.textPrimary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                if (state.relatedImages.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (state.relatedLoading) "加载影像中..." else "暂无影像记录",
                                color = SpineTheme.colors.textSecondary,
                            )
                        }
                    }
                } else {
                    items(state.relatedImages, key = { it.id }) { file ->
                        ImageTaskCard(
                            item = file,
                            session = session,
                            repository = imageRepository,
                            onSessionUpdated = onSessionUpdated,
                            compactActionText = true,
                            singleActionBottomRight = true,
                            actions = listOf(
                                ImageTaskAction(
                                    text = "立即处理",
                                    glyph = IconToken.EYE,
                                    style = ImageTaskActionStyle.PRIMARY,
                                    onClick = {
                                        onOpenAnalysis(file.id, file.patientId, inferExamType(file))
                                    },
                                ),
                            ),
                            patientNameOverride = detail.name,
                        )
                    }
                }
            }
        }

        if (state.loading) {
            LoadingOverlay(message = "...正在加载中")
        }
    }
}

private fun splitPhone(rawPhone: String?): Pair<String?, String?> {
    val phone = rawPhone?.trim().orEmpty()
    if (phone.isBlank()) {
        return null to null
    }
    if (phone.startsWith("+")) {
        val knownPrefix = listOf("+886", "+853", "+852", "+86").firstOrNull { phone.startsWith(it) }
        if (knownPrefix != null) {
            return knownPrefix to phone.removePrefix(knownPrefix).ifBlank { null }
        }
        val genericPrefix = "+" + phone.drop(1).takeWhile { it.isDigit() }.take(4)
        return genericPrefix to phone.removePrefix(genericPrefix).ifBlank { null }
    }
    return "+86" to phone
}
