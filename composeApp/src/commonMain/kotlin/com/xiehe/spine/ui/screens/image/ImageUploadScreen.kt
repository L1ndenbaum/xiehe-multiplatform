package com.xiehe.spine.ui.screens.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.xiehe.spine.data.image.ImageFileRepository
import com.xiehe.spine.data.patient.PatientRepository
import com.xiehe.spine.ui.components.button.shared.Button
import com.xiehe.spine.ui.components.icon.shared.IconToken
import com.xiehe.spine.ui.components.feedback.shared.LoadingOverlay
import com.xiehe.spine.ui.components.form.picker.PickerDialog
import com.xiehe.spine.ui.components.feedback.shared.Text
import com.xiehe.spine.ui.components.form.input.TextField
import com.xiehe.spine.ui.components.form.file.rememberImageFilePickerLauncher
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.image.ImageUploadViewModel
import com.xiehe.spine.ui.viewmodel.image.UploadFilePayload

private enum class ImageUploadPicker {
    PATIENT,
    EXAM_TYPE,
}

@Composable
fun ImageUploadScreen(
    vm: ImageUploadViewModel,
    session: UserSession,
    patientRepository: PatientRepository,
    imageRepository: ImageFileRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onUploadSuccess: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()
    var picker by remember { mutableStateOf<ImageUploadPicker?>(null) }

    val imagePicker = rememberImageFilePickerLauncher { picked ->
        vm.setSelectedFile(
            picked?.let {
                UploadFilePayload(
                    name = it.name,
                    mimeType = it.mimeType,
                    bytes = it.bytes,
                )
            },
        )
    }

    LaunchedEffect(session.accessToken) {
        vm.loadPatients(
            session = session,
            repository = patientRepository,
            onSessionUpdated = onSessionUpdated,
        )
    }

    val selectedPatientName = state.patients.firstOrNull { it.id == state.selectedPatientId }?.name ?: "请选择患者"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PickerField(
                text = selectedPatientName,
                leadingGlyph = IconToken.PATIENTS,
                onClick = { picker = ImageUploadPicker.PATIENT },
            )

            PickerField(
                text = state.selectedExamType.label,
                leadingGlyph = IconToken.IMAGE,
                onClick = { picker = ImageUploadPicker.EXAM_TYPE },
            )

            TextField(
                value = state.selectedFile?.name ?: "",
                onValueChange = {},
                placeholder = "请选择影像文件",
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                leadingGlyph = IconToken.IMAGE,
                trailingGlyph = IconToken.ADD,
                onTrailingClick = { imagePicker.launch() },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "选择文件",
                    modifier = Modifier
                        .background(
                            color = SpineTheme.colors.primaryMuted,
                            shape = RoundedCornerShape(SpineTheme.radius.full),
                        )
                        .clickable { imagePicker.launch() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = SpineTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = SpineTheme.colors.primary,
                )
            }

            TextField(
                value = state.note,
                onValueChange = vm::updateNote,
                placeholder = "备注信息(可选)",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                leadingGlyph = IconToken.MESSAGE,
            )

            state.errorMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
            state.successMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.success))
            }

            Button(
                text = if (state.uploading) "上传中..." else "上传影像",
                onClick = {
                    vm.submit(
                        session = session,
                        repository = imageRepository,
                        onSessionUpdated = onSessionUpdated,
                        onSuccess = onUploadSuccess,
                    )
                },
                enabled = !state.uploading,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.IMAGE,
            )
        }

        if (state.uploading || state.loadingPatients) {
            LoadingOverlay(message = "...正在加载中")
        }
    }

    when (picker) {
        ImageUploadPicker.PATIENT -> {
            PickerDialog(
                title = "",
                onDismissRequest = { picker = null },
                showActionRow = false,
                edgeToEdge = true,
            ) { dismiss ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "选择患者", style = SpineTheme.typography.title)
                    state.patients.forEach { patient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (patient.id == state.selectedPatientId) SpineTheme.colors.primary else SpineTheme.colors.surfaceMuted,
                                    shape = RoundedCornerShape(SpineTheme.radius.md),
                                )
                                .clickable {
                                    vm.updatePatient(patient.id)
                                    dismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(patient.name, color = if (patient.id == state.selectedPatientId) SpineTheme.colors.onPrimary else SpineTheme.colors.textPrimary)
                            if (patient.id == state.selectedPatientId) {
                                Text("✓", color = SpineTheme.colors.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        ImageUploadPicker.EXAM_TYPE -> {
            PickerDialog(
                title = "",
                onDismissRequest = { picker = null },
                showActionRow = false,
                edgeToEdge = true,
            ) { dismiss ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "选择影像类别", style = SpineTheme.typography.title)
                    state.examTypes.forEach { examType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (examType == state.selectedExamType) SpineTheme.colors.primary else SpineTheme.colors.surfaceMuted,
                                    shape = RoundedCornerShape(SpineTheme.radius.md),
                                )
                                .clickable {
                                    vm.updateExamType(examType)
                                    dismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(examType.label, color = if (examType == state.selectedExamType) SpineTheme.colors.onPrimary else SpineTheme.colors.textPrimary)
                            if (examType == state.selectedExamType) {
                                Text("✓", color = SpineTheme.colors.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        null -> Unit
    }
}

@Composable
private fun PickerField(
    text: String,
    leadingGlyph: IconToken,
    onClick: () -> Unit,
) {
    TextField(
        value = text,
        onValueChange = {},
        placeholder = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        readOnly = true,
        leadingGlyph = leadingGlyph,
        trailingGlyph = IconToken.CHEVRON_DOWN,
        onTrailingClick = onClick,
    )
}

