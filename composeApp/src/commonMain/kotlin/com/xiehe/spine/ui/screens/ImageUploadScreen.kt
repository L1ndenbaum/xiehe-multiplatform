package com.xiehe.spine.ui.screens

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
import com.xiehe.spine.data.ImageFileRepository
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.Button
import com.xiehe.spine.ui.components.FilterSelector
import com.xiehe.spine.ui.components.IconToken
import com.xiehe.spine.ui.components.LoadingOverlay
import com.xiehe.spine.ui.components.PickerDialog
import com.xiehe.spine.ui.components.Text
import com.xiehe.spine.ui.components.TextField
import com.xiehe.spine.ui.components.rememberImageFilePickerLauncher
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.viewmodel.ImageUploadViewModel
import com.xiehe.spine.ui.viewmodel.UploadFilePayload

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterSelector(
                text = selectedPatientName,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = IconToken.PATIENTS,
                onClick = { picker = ImageUploadPicker.PATIENT },
            )

            FilterSelector(
                text = state.selectedExamType,
                modifier = Modifier.fillMaxWidth(),
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
                value = state.description,
                onValueChange = vm::updateDescription,
                placeholder = "描述信息(可选)",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
            )

            state.errorMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
            state.successMessage?.let {
                Text(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.primary))
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
            ) { dismiss ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "选择患者", style = SpineTheme.typography.title)
                    state.patients.forEach { patient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (patient.id == state.selectedPatientId) {
                                        SpineTheme.colors.primaryMuted
                                    } else {
                                        SpineTheme.colors.surface
                                    },
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
                            Text(patient.name)
                            if (patient.id == state.selectedPatientId) {
                                Text("✓", color = SpineTheme.colors.primary)
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
            ) { dismiss ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "选择检查类型", style = SpineTheme.typography.title)
                    state.examTypes.forEach { examType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (examType == state.selectedExamType) {
                                        SpineTheme.colors.primaryMuted
                                    } else {
                                        SpineTheme.colors.surface
                                    },
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
                            Text(examType)
                            if (examType == state.selectedExamType) {
                                Text("✓", color = SpineTheme.colors.primary)
                            }
                        }
                    }
                }
            }
        }

        null -> Unit
    }
}
