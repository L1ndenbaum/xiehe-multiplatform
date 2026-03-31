package com.xiehe.spine.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.patient.PatientRepository
import com.xiehe.spine.data.patient.PatientSummary
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.picker.OptionPickerOverlay
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.patient.GenderFilter
import com.xiehe.spine.ui.viewmodel.patient.PatientsViewModel
import kotlinx.coroutines.delay

@Composable
fun PatientsScreen(
    vm: PatientsViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onSessionExpired: (String) -> Unit = {},
    onOpenPatient: (Int) -> Unit,
    showInlineSearch: Boolean = true,
    onEditPatient: (Int) -> Unit,
) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    var showGenderPicker by remember { mutableStateOf(false) }

    LaunchedEffect(session.accessToken, state.search, state.genderFilter, state.ageFilter) {
        delay(260)
        vm.refresh(session, repository, onSessionUpdated, onSessionExpired)
    }

    LaunchedEffect(listState.canScrollForward, state.page, state.totalPages, state.loadingMore) {
        if (!listState.canScrollForward && state.page < state.totalPages && !state.loadingMore) {
            vm.loadMore(session, repository, onSessionUpdated, onSessionExpired)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showInlineSearch) {
                com.xiehe.spine.ui.components.form.input.TextField(
                    value = state.search,
                    onValueChange = vm::updateSearch,
                    placeholder = "搜索患者姓名、ID或手机号",
                    modifier = Modifier.fillMaxWidth(),
                    leadingGlyph = com.xiehe.spine.ui.components.icon.shared.IconToken.SEARCH,
                )
            }

            GenderFilterChip(
                text = "性别: ${state.genderFilter.label.removePrefix("全部")}".let {
                    if (state.genderFilter == GenderFilter.ALL) "性别: 全部" else it
                },
                onClick = { showGenderPicker = true },
            )

            state.errorMessage?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = it,
                        style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.items, key = { it.id }) { patient ->
                    PatientSummaryCard(
                        patient = patient,
                        onOpenPatient = { onOpenPatient(patient.id) },
                        onEditPatient = { onEditPatient(patient.id) },
                    )
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

    if (showGenderPicker) {
        OptionPickerOverlay(
            title = "选择性别",
            options = GenderFilter.entries.map { it.label },
            selected = state.genderFilter.label,
            onDismiss = { showGenderPicker = false },
            onSelect = { selected ->
                GenderFilter.entries.firstOrNull { it.label == selected }?.let(vm::updateGenderFilter)
                showGenderPicker = false
            },
        )
    }
}

@Composable
private fun GenderFilterChip(
    text: String,
    onClick: () -> Unit,
) {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "≡",
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Bold),
            color = colors.primary,
        )
        Text(
            text = text,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun PatientSummaryCard(
    patient: PatientSummary,
    onOpenPatient: () -> Unit,
    onEditPatient: () -> Unit,
) {
    val colors = SpineTheme.colors
    val style = patientCardStyle(patient.gender)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(style.avatarGradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = style.avatarText,
                    style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = patient.name,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                    Text(
                        text = patient.gender,
                        style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = style.badgeText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(style.badgeBackground)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = patient.age?.let { "${it}岁" } ?: "年龄未填写",
                    style = SpineTheme.typography.subhead,
                    color = colors.textSecondary,
                )
                Text(
                    text = "${patient.patientId} · ${patient.phone ?: "无手机号"}",
                    style = SpineTheme.typography.caption,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.borderSubtle),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PatientActionButton(
                text = "编辑",
                background = colors.success.copy(alpha = if (colors.isDark) 0.18f else 0.1f),
                textColor = colors.success,
                modifier = Modifier.weight(1f),
                onClick = onEditPatient,
            )
            PatientActionButton(
                text = "查看",
                background = Brush.linearGradient(
                    listOf(colors.primary.copy(alpha = 0.9f), colors.primary),
                ),
                textColor = colors.onPrimary,
                modifier = Modifier.weight(1f),
                onClick = onOpenPatient,
            )
        }
    }
}

@Composable
private fun PatientActionButton(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    background: Any,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                when (background) {
                    is Brush -> Modifier.background(background)
                    is Color -> Modifier.background(background)
                    else -> Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}

private data class PatientCardStyle(
    val avatarGradient: List<Color>,
    val avatarText: String,
    val badgeBackground: Color,
    val badgeText: Color,
)

@Composable
private fun patientCardStyle(gender: String): PatientCardStyle {
    val colors = SpineTheme.colors
    return if (gender == "女") {
        PatientCardStyle(
            avatarGradient = listOf(Color(0xFFF9A8D4), Color(0xFFEC4899)),
            avatarText = "女",
            badgeBackground = Color(0xFFFDF2F8),
            badgeText = Color(0xFFDB2777),
        )
    } else {
        PatientCardStyle(
            avatarGradient = listOf(colors.primary.copy(alpha = 0.72f), colors.primary),
            avatarText = "男",
            badgeBackground = colors.primaryMuted,
            badgeText = colors.primary,
        )
    }
}
