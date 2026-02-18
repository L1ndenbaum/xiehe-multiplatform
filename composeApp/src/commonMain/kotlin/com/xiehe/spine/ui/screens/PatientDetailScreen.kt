package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.PatientDetailViewModel

@Composable
fun PatientDetailScreen(
    patientId: Int,
    vm: PatientDetailViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(patientId, session.accessToken) {
        vm.load(patientId, session, repository, onSessionUpdated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.errorMessage?.let {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
        }
        val detail = state.detail
        if (detail == null) {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(text = if (state.loading) "加载患者详情中..." else "暂无患者详情")
            }
        } else {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText("姓名：${detail.name}", style = SpineTheme.typography.title)
                SpineText("患者ID：${detail.patientId}")
                SpineText("性别：${detail.gender}  年龄：${detail.age}")
                SpineText("出生日期：${detail.birthDate}")
                SpineText("联系电话：${detail.phone ?: "-"}")
                SpineText("身份证号：${detail.idCard ?: "-"}")
                SpineText("地址：${detail.address ?: "-"}")
                SpineText("紧急联系人：${detail.emergencyContactName ?: "-"}")
                SpineText("紧急联系人电话：${detail.emergencyContactPhone ?: "-"}")
            }
        }
    }
}
