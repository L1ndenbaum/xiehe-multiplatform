package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import com.xiehe.spine.ui.components.Card
import com.xiehe.spine.ui.components.FileSaveResult
import com.xiehe.spine.ui.components.FilterSelector
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.ImageTaskAction
import com.xiehe.spine.ui.components.ImageTaskActionStyle
import com.xiehe.spine.ui.components.ImageTaskCard
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.OperationVerifyCard
import com.xiehe.spine.ui.components.PickerDialog
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.TextField
import com.xiehe.spine.ui.components.inferExamType
import com.xiehe.spine.ui.components.rememberDownloadedFileSaver
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.ImageStatusFilter
import com.xiehe.spine.ui.viewmodel.ImageTypeFilter
import com.xiehe.spine.ui.viewmodel.ImagesViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ImagesPicker {
    TYPE,
    STATUS,
}

@Composable
fun ImagesScreen(
    vm: ImagesViewModel,
    session: UserSession,
    repository: ImageFileRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onOpenAnalysis: (Int, Int?, String) -> Unit = { _, _, _ -> },
) {
    val state by vm.state.collectAsState()
    var picker by remember { mutableStateOf<ImagesPicker?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<ImageFileSummary?>(null) }
    var deleteDialogVisible by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var actionSuccess by remember { mutableStateOf<String?>(null) }
    var actionLoadingMessage by remember { mutableStateOf<String?>(null) }
    var downloadBannerMessage by remember { mutableStateOf<String?>(null) }
    var downloadBannerVisible by remember { mutableStateOf(false) }
    var downloadBannerJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val saver = rememberDownloadedFileSaver()

    LaunchedEffect(session.accessToken) {
        vm.refreshIfNeeded(session, repository, onSessionUpdated)
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
            TextField(
                value = state.search,
                onValueChange = vm::updateSearch,
                placeholder = "搜索患者姓名、检查类型或文件名...",
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.SEARCH,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterSelector(
                    text = state.typeFilter.label,
                    modifier = Modifier.weight(1f),
                    leadingGlyph = IconToken.IMAGE,
                    onClick = { picker = ImagesPicker.TYPE },
                )
                FilterSelector(
                    text = state.statusFilter.label,
                    modifier = Modifier.weight(1f),
                    leadingGlyph = IconToken.HOURGLASS,
                    onClick = { picker = ImagesPicker.STATUS },
                )
            }

            state.errorMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
            actionError?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
            actionSuccess?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.primary))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.filteredItems, key = { it.id }) { file ->
                    ImageTaskCard(
                        item = file,
                        session = session,
                        repository = repository,
                        onSessionUpdated = onSessionUpdated,
                        compactActionText = true,
                        actions = listOf(
                            ImageTaskAction(
                                text = "标注分析",
                                glyph = IconToken.EYE,
                                style = ImageTaskActionStyle.PRIMARY,
                                onClick = {
                                    onOpenAnalysis(
                                        file.id,
                                        file.patientId,
                                        inferExamType(file),
                                    )
                                },
                            ),
                            ImageTaskAction(
                                text = "下载",
                                glyph = IconToken.DOWNLOAD,
                                style = ImageTaskActionStyle.OUTLINE,
                                onClick = {
                                    coroutineScope.launch {
                                        actionError = null
                                        actionSuccess = null
                                        actionLoadingMessage = "...正在下载中"
                                        when (val result = repository.downloadImageBytes(session, file.id)) {
                                            is AppResult.Success -> {
                                                val activeSession = result.data.first
                                                onSessionUpdated(activeSession)
                                                val saveResult = saver.save(
                                                    fileName = file.originalFilename.ifBlank { "image_${file.id}.png" },
                                                    mimeType = file.mimeType ?: "image/png",
                                                    bytes = result.data.second,
                                                )
                                                when (saveResult) {
                                                    is FileSaveResult.Success -> {
                                                        val downloadHint = buildString {
                                                            append(file.originalFilename)
                                                            if (saveResult.location.isNullOrBlank()) {
                                                                append("已下载")
                                                            } else {
                                                                append("已下载到")
                                                                append(saveResult.location)
                                                            }
                                                        }
                                                        downloadBannerMessage = downloadHint
                                                        downloadBannerVisible = true
                                                        downloadBannerJob?.cancel()
                                                        downloadBannerJob = coroutineScope.launch {
                                                            delay(2300)
                                                            downloadBannerVisible = false
                                                            delay(220)
                                                            if (!downloadBannerVisible) {
                                                                downloadBannerMessage = null
                                                            }
                                                        }
                                                    }

                                                    is FileSaveResult.Failure -> {
                                                        actionError = saveResult.message
                                                    }
                                                }
                                            }

                                            is AppResult.Failure -> {
                                                actionError = result.message
                                            }
                                        }
                                        actionLoadingMessage = null
                                    }
                                },
                            ),
                            ImageTaskAction(
                                text = "删除",
                                glyph = IconToken.DELETE,
                                style = ImageTaskActionStyle.DANGER,
                                onClick = {
                                    pendingDeleteItem = file
                                    deleteDialogVisible = true
                                    actionError = null
                                    actionSuccess = null
                                },
                            ),
                        ),
                    )
                }

                item {
                    if (state.loading) {
                        Text(
                            text = "加载中...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            style = SpineTheme.typography.subhead,
                        )
                    } else if (state.filteredItems.isEmpty() && state.errorMessage == null) {
                        Text(
                            text = "暂无影像数据",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            style = SpineTheme.typography.subhead,
                            color = SpineTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }

        if ((state.loading && state.filteredItems.isEmpty()) || actionLoadingMessage != null) {
            LoadingOverlay(message = actionLoadingMessage ?: "...正在加载中")
        }

        AnimatedVisibility(
            visible = downloadBannerVisible && !downloadBannerMessage.isNullOrBlank(),
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 3 },
            exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { it / 3 },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = downloadBannerMessage.orEmpty(),
                    style = SpineTheme.typography.caption,
                    color = SpineTheme.colors.textSecondary,
                    maxLines = 2,
                )
            }
        }

        pendingDeleteItem?.let { item ->
            val overlayAlpha by animateFloatAsState(
                targetValue = if (deleteDialogVisible) 0.35f else 0f,
                animationSpec = tween(220),
                label = "delete_overlay_alpha",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            coroutineScope.launch {
                                deleteDialogVisible = false
                                delay(220)
                                pendingDeleteItem = null
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = deleteDialogVisible,
                    enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { it / 4 },
                    exit = fadeOut(animationSpec = tween(220)) + slideOutVertically(animationSpec = tween(220)) { it / 5 },
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                    ) {
                        OperationVerifyCard(
                            title = "删除影像",
                            message = "确认删除影像“${item.originalFilename}”吗？该操作不可恢复。",
                            confirmText = "删除",
                            cancelText = "取消",
                            confirmButtonColor = SpineTheme.colors.error,
                            cancelButtonColor = SpineTheme.colors.textSecondary,
                            onCancel = {
                                coroutineScope.launch {
                                    deleteDialogVisible = false
                                    delay(220)
                                    pendingDeleteItem = null
                                }
                            },
                            onConfirm = {
                                coroutineScope.launch {
                                    deleteDialogVisible = false
                                    delay(220)
                                    pendingDeleteItem = null
                                    actionError = null
                                    actionSuccess = null
                                    actionLoadingMessage = "...正在删除中"
                                    when (val result = repository.deleteImageFile(session, item.id)) {
                                        is AppResult.Success -> {
                                            val activeSession = result.data.first
                                            onSessionUpdated(activeSession)
                                            vm.refresh(
                                                session = activeSession,
                                                repository = repository,
                                                onSessionUpdated = onSessionUpdated,
                                            )
                                            actionSuccess = "删除成功"
                                        }

                                        is AppResult.Failure -> {
                                            actionError = result.message
                                        }
                                    }
                                    actionLoadingMessage = null
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    when (picker) {
        ImagesPicker.TYPE -> OptionPickerOverlay(
            title = "选择影像类型",
            options = ImageTypeFilter.entries.map { it.label },
            selected = state.typeFilter.label,
            onDismiss = { picker = null },
            onSelect = { selected ->
                ImageTypeFilter.entries.firstOrNull { it.label == selected }?.let(vm::updateTypeFilter)
                picker = null
            },
        )

        ImagesPicker.STATUS -> OptionPickerOverlay(
            title = "选择影像状态",
            options = ImageStatusFilter.entries.map { it.label },
            selected = state.statusFilter.label,
            onDismiss = { picker = null },
            onSelect = { selected ->
                ImageStatusFilter.entries.firstOrNull { it.label == selected }?.let(vm::updateStatusFilter)
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
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(SpineTheme.radius.md),
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
