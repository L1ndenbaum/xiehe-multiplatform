package com.xiehe.spine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.SpineAvatar
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineCompactButton
import com.xiehe.spine.ui.components.SpineFilterSelector
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.components.SpineTextField
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpineTextField(
                value = state.search,
                onValueChange = vm::updateSearch,
                placeholder = "搜索患者姓名、ID或电话...",
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = SpineGlyph.SEARCH,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SpineFilterSelector(
                    text = state.genderFilter.label,
                    modifier = Modifier.weight(1f),
                    leadingGlyph = SpineGlyph.PROFILE,
                    onClick = { picker = PatientsPicker.GENDER },
                )
                SpineFilterSelector(
                    text = state.ageFilter.label,
                    modifier = Modifier.weight(1f),
                    leadingGlyph = SpineGlyph.CALENDAR,
                    onClick = { picker = PatientsPicker.AGE },
                )
            }

            state.errorMessage?.let {
                SpineText(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
}

@Composable
private fun OptionPickerOverlay(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.22f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(SpineTheme.colors.surface, RoundedCornerShape(SpineTheme.radius.lg))
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {},
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpineText(text = title, style = SpineTheme.typography.title)
            options.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (item == selected) SpineTheme.colors.primaryMuted else SpineTheme.colors.surface,
                            shape = RoundedCornerShape(SpineTheme.radius.md),
                        )
                        .clickable { onSelect(item) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SpineText(text = item)
                    if (item == selected) {
                        SpineText(text = "✓", color = SpineTheme.colors.primary)
                    }
                }
            }
        }
    }
}
