package com.xiehe.spine.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.ImageFileSummary
import com.xiehe.spine.ui.theme.SpineTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

data class ImageTaskAction(
    val text: String,
    val glyph: IconToken,
    val style: ImageTaskActionStyle,
    val onClick: () -> Unit,
)

enum class ImageTaskActionStyle {
    PRIMARY,
    OUTLINE,
    DANGER,
}

private sealed interface ThumbnailState {
    data object Loading : ThumbnailState
    data class Success(val bitmap: ImageBitmap) : ThumbnailState
    data object Error : ThumbnailState
}

@Composable
fun ImageTaskCard(
    item: ImageFileSummary,
    session: UserSession,
    repository: ImageFileRepository,
    onSessionUpdated: (UserSession) -> Unit,
    actions: List<ImageTaskAction>,
    modifier: Modifier = Modifier,
    compactActionText: Boolean = false,
    singleActionBottomRight: Boolean = false,
    patientNameOverride: String? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ImageThumbnail(
                    fileId = item.id,
                    session = session,
                    repository = repository,
                    onSessionUpdated = onSessionUpdated,
                    modifier = Modifier
                        .size(width = 96.dp, height = 96.dp)
                        .clip(RoundedCornerShape(SpineTheme.radius.md))
                        .background(SpineTheme.colors.surfaceMuted),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.originalFilename,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                    )
                    Text(
                        text = imageSubtitle(item = item, patientNameOverride = patientNameOverride),
                        style = SpineTheme.typography.subhead,
                        color = SpineTheme.colors.textSecondary,
                        maxLines = 1,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = imageTimeLabel(item),
                            style = SpineTheme.typography.caption,
                            color = SpineTheme.colors.textTertiary,
                            maxLines = 1,
                        )
                        val status = imageStatusPresentation(item.status)
                        StatusChip(
                            text = status.text,
                            textColor = status.textColor,
                            background = status.background,
                        )
                    }
                }
            }

            if (actions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (singleActionBottomRight && actions.size == 1) {
                        val action = actions.first()
                        Spacer(modifier = Modifier.weight(1f))
                        ImageActionButton(
                            text = action.text,
                            glyph = action.glyph,
                            style = action.style,
                            compactText = compactActionText,
                            modifier = Modifier.width(128.dp),
                            onClick = action.onClick,
                        )
                    } else {
                        actions.forEach { action ->
                            ImageActionButton(
                                text = action.text,
                                glyph = action.glyph,
                                style = action.style,
                                compactText = compactActionText,
                                modifier = Modifier.weight(1f),
                                onClick = action.onClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    textColor: Color,
    background: Color,
) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(SpineTheme.radius.sm))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}

@Composable
private fun ImageThumbnail(
    fileId: Int,
    session: UserSession,
    repository: ImageFileRepository,
    onSessionUpdated: (UserSession) -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember(fileId) { mutableStateOf<ThumbnailState>(ThumbnailState.Loading) }
    var retryNonce by remember(fileId) { mutableIntStateOf(0) }

    LaunchedEffect(fileId, session.accessToken, retryNonce) {
        state = ThumbnailState.Loading
        when (val result = repository.downloadImageBytes(session, fileId)) {
            is AppResult.Success -> {
                onSessionUpdated(result.data.first)
                val bitmap = withContext(Dispatchers.Default) {
                    runCatching { result.data.second.decodeToImageBitmap() }.getOrNull()
                }
                if (bitmap == null && retryNonce == 0) {
                    repository.evictImageCache(fileId)
                    retryNonce += 1
                    return@LaunchedEffect
                }
                state = if (bitmap != null) ThumbnailState.Success(bitmap) else ThumbnailState.Error
            }

            is AppResult.Failure -> {
                state = ThumbnailState.Error
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (val current = state) {
            ThumbnailState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AppIcon(glyph = IconToken.HOURGLASS, tint = SpineTheme.colors.textTertiary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "加载中",
                        style = SpineTheme.typography.caption,
                        color = SpineTheme.colors.textTertiary,
                    )
                }
            }

            is ThumbnailState.Success -> {
                Image(
                    bitmap = current.bitmap,
                    contentDescription = "image_preview_$fileId",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            ThumbnailState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AppIcon(glyph = IconToken.IMAGE, tint = SpineTheme.colors.textTertiary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "加载失败",
                        style = SpineTheme.typography.caption,
                        color = SpineTheme.colors.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageActionButton(
    text: String,
    glyph: IconToken,
    style: ImageTaskActionStyle,
    compactText: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = SpineTheme.colors
    val corner = RoundedCornerShape(SpineTheme.radius.md)
    val contentColor = when (style) {
        ImageTaskActionStyle.PRIMARY -> colors.onPrimary
        ImageTaskActionStyle.OUTLINE -> colors.textSecondary
        ImageTaskActionStyle.DANGER -> colors.error
    }
    val background = when (style) {
        ImageTaskActionStyle.PRIMARY -> colors.primary
        ImageTaskActionStyle.OUTLINE -> colors.surface
        ImageTaskActionStyle.DANGER -> colors.surface
    }
    val border = when (style) {
        ImageTaskActionStyle.PRIMARY -> Color.Transparent
        ImageTaskActionStyle.OUTLINE -> colors.borderStrong
        ImageTaskActionStyle.DANGER -> colors.error.copy(alpha = 0.45f)
    }

    Row(
        modifier = modifier
            .height(40.dp)
            .clip(corner)
            .background(background)
            .border(width = 1.dp, color = border, shape = corner)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(glyph = glyph, tint = contentColor, modifier = Modifier.size(14.dp))
        Text(
            text = text,
            style = if (compactText) {
                SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold)
            } else {
                SpineTheme.typography.body.copy(fontWeight = FontWeight.SemiBold)
            },
            color = contentColor,
            modifier = Modifier.padding(start = 5.dp),
            maxLines = 1,
        )
    }
}

data class ImageStatusPresentation(
    val text: String,
    val textColor: Color,
    val background: Color,
)

@Composable
fun imageStatusPresentation(rawStatus: String?): ImageStatusPresentation {
    val colors = SpineTheme.colors
    return when (rawStatus?.uppercase()) {
        "UPLOADED" -> ImageStatusPresentation(
            text = "待审核",
            textColor = colors.primary,
            background = colors.primaryMuted,
        )

        "PROCESSED" -> ImageStatusPresentation(
            text = "已归档",
            textColor = colors.textSecondary,
            background = colors.surfaceMuted,
        )

        "PROCESSING" -> ImageStatusPresentation(
            text = "处理中",
            textColor = colors.warning,
            background = colors.warning.copy(alpha = 0.16f),
        )

        "FAILED" -> ImageStatusPresentation(
            text = "失败",
            textColor = colors.error,
            background = colors.error.copy(alpha = 0.16f),
        )

        else -> ImageStatusPresentation(
            text = rawStatus ?: "未知",
            textColor = colors.textSecondary,
            background = colors.surfaceMuted,
        )
    }
}

fun inferExamType(item: ImageFileSummary): String {
    return when (item.modality?.uppercase()) {
        "XR", "X-RAY", "X_RAY" -> "正位X光片"
        "CT" -> "CT"
        "MRI", "MR" -> "MRI"
        else -> item.description?.takeIf { it.isNotBlank() } ?: "正位X光片"
    }
}

fun imageSubtitle(
    item: ImageFileSummary,
    patientNameOverride: String? = null,
): String {
    val patient = patientNameOverride?.takeIf { it.isNotBlank() }
        ?: item.patientName?.takeIf { it.isNotBlank() }
        ?: item.patientId?.let { "患者#$it" }
        ?: "未关联患者"
    val exam = item.description?.takeIf { it.isNotBlank() }
        ?: item.modality?.let { modalityLabel(it) }
        ?: "影像检查"
    return "$patient · $exam"
}

fun imageTimeLabel(item: ImageFileSummary): String {
    val source = item.uploadedAt ?: item.createdAt ?: item.studyDate
    if (source.isNullOrBlank()) {
        return "时间未知"
    }
    return source.replace("T", " ").take(19)
}

private fun modalityLabel(modality: String): String {
    return when (modality.uppercase()) {
        "XR" -> "X-ray"
        "CT" -> "CT"
        "MRI" -> "MRI"
        else -> modality
    }
}
