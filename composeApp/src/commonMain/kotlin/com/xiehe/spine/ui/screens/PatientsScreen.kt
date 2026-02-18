package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.SpineAvatar
import com.xiehe.spine.ui.components.SpineButton
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineCompactButton
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.components.SpineTextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.PatientsViewModel

@Composable
fun PatientsScreen(
    vm: PatientsViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onAddPatient: () -> Unit,
    onOpenPatient: (Int) -> Unit,
    onEditPatient: (Int) -> Unit,
) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(session.accessToken) {
        vm.refresh(session, repository, onSessionUpdated)
    }

    LaunchedEffect(listState.canScrollForward, state.page, state.totalPages, state.loadingMore) {
        if (!listState.canScrollForward && state.page < state.totalPages && !state.loadingMore) {
            vm.loadMore(session, repository, onSessionUpdated)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SpineTextField(
                value = state.search,
                onValueChange = vm::updateSearch,
                placeholder = "按姓名/患者ID检索",
                modifier = Modifier.weight(1f),
            )
            SpineButton(
                text = "查询",
                onClick = { vm.refresh(session, repository, onSessionUpdated) },
                modifier = Modifier.width(76.dp),
                leadingGlyph = SpineGlyph.DASHBOARD,
            )
        }
        SpineButton(
            text = "新增患者",
            onClick = onAddPatient,
            modifier = Modifier.fillMaxWidth(),
            leadingGlyph = SpineGlyph.ADD,
        )
        state.errorMessage?.let {
            SpineText(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = listState,
        ) {
            items(state.items, key = { it.id }) { patient ->
                SpineCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SpineAvatar(name = patient.name)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            SpineText(text = "${patient.name}·${patient.gender}·${patient.age}岁", style = SpineTheme.typography.title)
                            SpineText(text = patient.patientId, style = SpineTheme.typography.subhead)
                            SpineText(text = patient.phone ?: "无手机号", style = SpineTheme.typography.subhead)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpineCompactButton(
                                text = "编辑",
                                onClick = { onEditPatient(patient.id) },
                                containerColor = SpineTheme.colors.warning,
                                contentColor = SpineTheme.colors.onPrimary,
                            )
                            SpineCompactButton(
                                text = "查看",
                                onClick = { onOpenPatient(patient.id) },
                                containerColor = SpineTheme.colors.primary,
                                contentColor = SpineTheme.colors.onPrimary,
                            )
                        }
                    }
                }
            }
            item {
                if (state.loading || state.loadingMore) {
                    SpineText(
                        text = "加载中...",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = SpineTheme.typography.subhead,
                    )
                }
            }
        }
    }
}
