package com.xiehe.spine.ui.components.card.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xiehe.spine.data.image.ImageFileSummary
import com.xiehe.spine.data.image.ImageWorkflowStatus
import com.xiehe.spine.data.image.normalizeImageStatus
import com.xiehe.spine.data.patient.PatientDetail
import com.xiehe.spine.ui.components.card.image.imageStatusPresentation
import com.xiehe.spine.ui.components.card.image.inferExamType
import com.xiehe.spine.ui.components.card.shared.Card
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.theme.SpineTheme

@Composable
fun PatientBasicInfoCard(detail: PatientDetail) {
    val colors = SpineTheme.colors
    val avatarGradient = patientAvatarGradient(detail.gender)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(avatarGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detail.gender.takeIf { !it.isNullOrBlank() } ?: "患",
                        style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = detail.name,
                        style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                    )
                    Text(
                        text = detail.patientId,
                        style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
                        color = colors.textSecondary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoCapsule(text = detail.gender.ifBlank { "未填写" })
                        InfoCapsule(text = "${detail.age}岁")
                    }
                }
            }
            PatientStatusBadge(status = detail.status)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DetailHighlight(
                label = "联系电话",
                value = detail.phone.orDash(),
                modifier = Modifier.weight(1f),
            )
            DetailHighlight(
                label = "电子邮箱",
                value = detail.email.orDash(),
                modifier = Modifier.weight(1f),
            )
        }

        SectionDivider()

        DetailGridRow(
            leftLabel = "姓名",
            leftValue = detail.name,
            rightLabel = "患者编号",
            rightValue = detail.patientId,
        )
        DetailGridRow(
            leftLabel = "性别",
            leftValue = detail.gender.ifBlank { "-" },
            rightLabel = "出生日期",
            rightValue = formatBirthDate(detail.birthDate),
        )
        DetailGridRow(
            leftLabel = "年龄",
            leftValue = "${detail.age}岁",
            rightLabel = "身份证号码",
            rightValue = detail.idCard.orDash(),
        )
        DetailGridRow(
            leftLabel = "医保卡号",
            leftValue = detail.insuranceNumber.orDash(),
            rightLabel = "家庭地址",
            rightValue = detail.address.orDash(),
        )
        DetailGridRow(
            leftLabel = "紧急联系人",
            leftValue = detail.emergencyContactName.orDash(),
            rightLabel = "紧急联系电话",
            rightValue = detail.emergencyContactPhone.orDash(),
        )

        detail.medicalHistory
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { medicalHistory ->
                SectionDivider()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "病史备注",
                        style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary,
                    )
                    Text(
                        text = medicalHistory,
                        style = SpineTheme.typography.body,
                        color = colors.textPrimary,
                    )
                }
            }
    }
}

@Composable
fun PatientDetailActionsCard(
    onEditPatient: () -> Unit,
    onOpenImageUpload: () -> Unit,
) {
    val colors = SpineTheme.colors
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionButton(
                text = "编辑资料",
                textColor = colors.primary,
                background = colors.primaryMuted,
                modifier = Modifier.weight(1f),
                onClick = onEditPatient,
            )
            ActionButton(
                text = "上传影像",
                textColor = colors.onPrimary,
                background = Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.92f), colors.primary)),
                modifier = Modifier.weight(1f),
                onClick = onOpenImageUpload,
            )
        }
    }
}

@Composable
fun PatientOverviewCards(
    detail: PatientDetail,
    relatedImages: List<ImageFileSummary>,
) {
    val pendingCount = relatedImages.count {
        normalizeImageStatus(it.status) in setOf(ImageWorkflowStatus.UPLOADED, ImageWorkflowStatus.PROCESSING)
    }
    val recentUpload = relatedImages
        .mapNotNull { it.createdAt ?: it.uploadedAt ?: it.studyDate }
        .maxOrNull()
        ?.let(::formatRecordDate)
        ?: "暂无记录"
    val medicalSummary = detail.medicalHistory
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: detail.insuranceNumber?.trim()?.takeIf { it.isNotBlank() }
        ?: "暂无病史记录"
    val medicalSummaryLabel = if (!detail.medicalHistory.isNullOrBlank()) "病史摘要" else "医保卡号"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OverviewMetricCard(
            title = "就诊统计",
            primary = relatedImages.size.toString(),
            primaryLabel = "关联影像总数",
            secondary = recentUpload,
            secondaryLabel = "最近上传日期",
            accent = listOf(Color(0xFFBBF7D0), Color(0xFF10B981)),
            modifier = Modifier.weight(1f),
        )
        OverviewMetricCard(
            title = "医疗信息",
            primary = pendingCount.toString(),
            primaryLabel = "待处理影像",
            secondary = medicalSummary,
            secondaryLabel = medicalSummaryLabel,
            accent = listOf(Color(0xFFDDD6FE), Color(0xFF8B5CF6)),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun PatientImageRecordsCard(
    images: List<ImageFileSummary>,
    onOpenImageUpload: () -> Unit,
    onOpenAnalysis: (Int, Int?, String) -> Unit,
) {
    val colors = SpineTheme.colors
    val records = images.sortedByDescending { it.createdAt ?: it.uploadedAt ?: it.studyDate.orEmpty() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "影像记录",
                    style = SpineTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
                Text(
                    text = "查看患者历史影像与当前状态",
                    style = SpineTheme.typography.caption,
                    color = colors.textTertiary,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.92f), colors.primary)))
                    .clickable(onClick = onOpenImageUpload)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "上传影像",
                    style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onPrimary,
                )
            }
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceMuted)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
            ) {
                Text(
                    text = "暂无影像记录，上传后会显示在这里。",
                    style = SpineTheme.typography.subhead,
                    color = colors.textSecondary,
                )
            }
            return@Card
        }

        RecordsHeaderRow()

        records.forEachIndexed { index, image ->
            ImageRecordRow(image = image, onOpenAnalysis = onOpenAnalysis)
            if (index != records.lastIndex) {
                SectionDivider()
            }
        }
    }
}

@Composable
private fun DetailGridRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DetailGridCell(label = leftLabel, value = leftValue, modifier = Modifier.weight(1f))
        DetailGridCell(label = rightLabel, value = rightValue, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailGridCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
            color = colors.textTertiary,
        )
        Text(
            text = value.ifBlank { "-" },
            style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            maxLines = 3,
        )
    }
}

@Composable
private fun DetailHighlight(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceMuted)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = colors.textTertiary,
        )
        Text(
            text = value,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    textColor: Color,
    background: Any,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                when (background) {
                    is Brush -> Modifier.background(background)
                    is Color -> Modifier.background(background)
                    else -> Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}

@Composable
private fun InfoCapsule(text: String) {
    val colors = SpineTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.primaryMuted)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.primary,
        )
    }
}

@Composable
private fun PatientStatusBadge(status: String?) {
    val colors = SpineTheme.colors
    val active = status.isNullOrBlank() ||
        (!status.equals("inactive", ignoreCase = true) && status != "非活跃")
    val background = if (active) Color(0xFFECFDF5) else colors.surfaceMuted
    val border = if (active) Color(0xFFBBF7D0) else colors.borderSubtle
    val textColor = if (active) Color(0xFF059669) else colors.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = if (active) "活跃" else "非活跃",
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
        )
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    primary: String,
    primaryLabel: String,
    secondary: String,
    secondaryLabel: String,
    accent: List<Color>,
    modifier: Modifier = Modifier,
) {
    val colors = SpineTheme.colors
    Card(modifier = modifier) {
        Text(
            text = title,
            style = SpineTheme.typography.body.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(accent)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = primary,
                    style = SpineTheme.typography.display.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                )
                Text(
                    text = primaryLabel,
                    style = SpineTheme.typography.subhead,
                    color = colors.textSecondary,
                )
            }
        }
        Text(
            text = secondaryLabel,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = colors.textTertiary,
        )
        Text(
            text = secondary,
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 3,
        )
    }
}

@Composable
private fun RecordsHeaderRow() {
    val colors = SpineTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecordsHeaderCell("上传日期", 82.dp)
        RecordsHeaderCell("文件名", 118.dp)
        RecordsHeaderCell("类型", 54.dp)
        RecordsHeaderCell("状态", 58.dp)
        RecordsHeaderCell("操作", 48.dp)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.borderSubtle),
    )
}

@Composable
private fun RecordsHeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
        color = SpineTheme.colors.textTertiary,
    )
}

@Composable
private fun ImageRecordRow(
    image: ImageFileSummary,
    onOpenAnalysis: (Int, Int?, String) -> Unit,
) {
    val colors = SpineTheme.colors
    val status = imageStatusPresentation(image.status)
    val examType = inferExamType(image)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatRecordDate(image.createdAt ?: image.uploadedAt ?: image.studyDate),
            modifier = Modifier.width(82.dp),
            style = SpineTheme.typography.subhead,
            color = colors.textSecondary,
        )
        Text(
            text = compactFileName(image.originalFilename),
            modifier = Modifier.width(118.dp),
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
        )
        StatusPill(
            text = compactExamType(examType),
            background = colors.primaryMuted,
            textColor = colors.primary,
            width = 54.dp,
        )
        StatusPill(
            text = status.text,
            background = status.background,
            textColor = status.textColor,
            width = 58.dp,
        )
        Text(
            text = "查看",
            modifier = Modifier
                .width(48.dp)
                .clickable { onOpenAnalysis(image.id, image.patientId, examType) },
            style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = colors.primary,
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    background: Color,
    textColor: Color,
    width: Dp,
) {
    Box(
        modifier = Modifier
            .width(width)
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, textColor.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SpineTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SpineTheme.colors.borderSubtle.copy(alpha = 0.75f)),
    )
}

private fun patientAvatarGradient(gender: String): List<Color> {
    return if (gender == "女") {
        listOf(Color(0xFFF9A8D4), Color(0xFFEC4899))
    } else {
        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
    }
}

private fun formatBirthDate(raw: String): String {
    val datePart = raw.trim().take(10)
    val parts = datePart.split("-")
    return if (parts.size == 3) {
        "${parts[0]}年${parts[1]}月${parts[2]}日"
    } else {
        raw
    }
}

private fun compactFileName(fileName: String): String {
    return if (fileName.length <= 16) fileName else "${fileName.take(12)}..."
}

private fun compactExamType(examType: String): String {
    return when {
        examType.contains("CT", ignoreCase = true) -> "CT"
        examType.contains("MRI", ignoreCase = true) -> "MRI"
        examType.contains("DR", ignoreCase = true) -> "DR"
        examType.contains("X", ignoreCase = true) -> "X光"
        else -> "检查"
    }
}

private fun formatRecordDate(raw: String?): String {
    val normalized = raw?.replace('T', ' ')?.trim().orEmpty()
    return when {
        normalized.length >= 10 -> normalized.take(10).replace('-', '/')
        normalized.isBlank() -> "--"
        else -> normalized
    }
}

private fun String?.orDash(): String {
    return this?.trim()?.takeIf { it.isNotBlank() } ?: "-"
}
