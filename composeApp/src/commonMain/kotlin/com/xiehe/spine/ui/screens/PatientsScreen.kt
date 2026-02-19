package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.Avatar
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.CompactButton
import com.xiehe.spine.ui.components.FilterSelector
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.PickerDialog
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.TextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.AgeFilter
import com.xiehe.spine.ui.viewmodel.GenderFilter
import com.xiehe.spine.ui.viewmodel.PatientsViewModel
import kotlinx.coroutines.delay

private enum class PatientsPicker {
    GENDER,
    AGE,
}

@Composable
fun PatientsScreen(
    vm: PatientsViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onOpenPatient: (Int) -> Unit,
    onEditPatient: (Int) -> Unit,
) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    var picker by remember { mutableStateOf<PatientsPicker?>(null) }

    LaunchedEffect(session.accessToken, state.search, state.genderFilter, state.ageFilter) {
        delay(260)
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
            TextField(
                value = state.search,
                onValueChange = vm::updateSearch,
                placeholder = "搜索患者姓名、ID或电话...",
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.SEARCH,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterSelector(
                    text = state.genderFilter.label,
                    modifier = Modifier.weight(1f),
                    leadingGlyph = IconToken.PROFILE,
                    onClick = { picker = PatientsPicker.GENDER },
                )
                FilterSelector(
                    text = state.ageFilter.label,
                    modifier = Modifier.weight(1f),
                    leadingGlyph = IconToken.CALENDAR,
                    onClick = { picker = PatientsPicker.AGE },
                )
            }

            state.errorMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = listState,
        ) {
            items(state.items, key = { it.id }) { patient ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(name = patient.name)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "${patient.name}·${patient.gender}·${patient.age}岁", style = SpineTheme.typography.title)
                            Text(text = patient.patientId, style = SpineTheme.typography.subhead)
                            Text(text = patient.phone ?: "无手机号", style = SpineTheme.typography.subhead)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompactButton(
                                text = "编辑",
                                onClick = { onEditPatient(patient.id) },
                                containerColor = SpineTheme.colors.warning,
                                contentColor = SpineTheme.colors.onPrimary,
                            )
                            CompactButton(
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
                    Text(
                        text = "加载中...",
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = SpineTheme.typography.subhead,
                    )
                }
            }
        }
    }

    when (picker) {
        PatientsPicker.GENDER -> OptionPickerOverlay(
            title = "选择性别",
            options = GenderFilter.entries.map { it.label },
            selected = state.genderFilter.label,
            onDismiss = { picker = null },
            onSelect = { selected ->
                GenderFilter.entries.firstOrNull { it.label == selected }?.let(vm::updateGenderFilter)
                picker = null
            },
        )

        PatientsPicker.AGE -> OptionPickerOverlay(
            title = "选择年龄范围",
            options = AgeFilter.entries.map { it.label },
            selected = state.ageFilter.label,
            onDismiss = { picker = null },
            onSelect = { selected ->
                AgeFilter.entries.firstOrNull { it.label == selected }?.let(vm::updateAgeFilter)
                picker = null
            },
        )

        null -> Unit
    }
}

@Composable
private fun OptionPickerOverlay(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    PickerDialog(
        title = "",
        onDismissRequest = onDismiss,
        showActionRow = false,
    ) { dismiss ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = SpineTheme.typography.title)
            options.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (item == selected) SpineTheme.colors.primaryMuted else SpineTheme.colors.surface,
                            shape = RoundedCornerShape(SpineTheme.radius.md),
                        )
                        .clickable {
                            onSelect(item)
                            dismiss()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = item)
                    if (item == selected) {
                        Text(text = "✓", color = SpineTheme.colors.primary)
                    }
                }
            }
        }
    }
}
