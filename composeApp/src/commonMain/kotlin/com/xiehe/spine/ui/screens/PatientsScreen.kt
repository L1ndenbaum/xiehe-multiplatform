package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.avatar.shared.Avatar
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.button.shared.CompactButton
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.form.picker.PickerDialog
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
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
    showInlineSearch: Boolean = true,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showInlineSearch) {
                TextField(
                    value = state.search,
                    onValueChange = vm::updateSearch,
                    placeholder = "搜索患者姓名、ID或手机号",
                    modifier = Modifier.fillMaxWidth(),
                    leadingGlyph = IconToken.SEARCH,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    text = state.genderFilter.label,
                    active = state.genderFilter != GenderFilter.ALL,
                    onClick = { picker = PatientsPicker.GENDER },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    text = state.ageFilter.label,
                    active = state.ageFilter != AgeFilter.ALL,
                    onClick = { picker = PatientsPicker.AGE },
                    modifier = Modifier.weight(1f),
                )
            }

            state.errorMessage?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                state = listState,
            ) {
                items(state.items, key = { it.id }) { patient ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(name = patient.name, size = 52.dp)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = patient.name,
                                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                    Text(
                                        text = patient.gender,
                                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                        color = SpineTheme.colors.primary,
                                        modifier = Modifier
                                            .background(
                                                SpineTheme.colors.primaryMuted,
                                                RoundedCornerShape(SpineTheme.radius.full),
                                            )
                                            .padding(horizontal = 7.dp, vertical = 3.dp),
                                    )
                                }
                                Text(text = "${patient.age}岁 · ${patient.patientId}", color = SpineTheme.colors.textSecondary)
                                Text(text = patient.phone ?: "无手机号", color = SpineTheme.colors.textSecondary)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            CompactButton(
                                text = "编辑",
                                onClick = { onEditPatient(patient.id) },
                                containerColor = SpineTheme.colors.success,
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

                item {
                    if (state.loading || state.loadingMore) {
                        Text(
                            text = "加载中...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            style = SpineTheme.typography.subhead,
                            color = SpineTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }

        if (state.loading && state.items.isEmpty()) {
            LoadingOverlay(message = "...正在加载中")
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
private fun FilterChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = modifier
            .background(
                if (active) colors.primary else colors.surface,
                RoundedCornerShape(SpineTheme.radius.full),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = if (active) colors.onPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "▾",
            style = SpineTheme.typography.subhead,
            color = if (active) colors.onPrimary else colors.textSecondary,
        )
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
                            color = if (item == selected) SpineTheme.colors.primary else SpineTheme.colors.surfaceMuted,
                            shape = RoundedCornerShape(SpineTheme.radius.md),
                        )
                        .clickable {
                            onSelect(item)
                            dismiss()
                        }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item,
                        color = if (item == selected) SpineTheme.colors.onPrimary else SpineTheme.colors.textPrimary,
                    )
                    if (item == selected) {
                        Text(text = "✓", color = SpineTheme.colors.onPrimary)
                    }
                }
            }
        }
    }
}
