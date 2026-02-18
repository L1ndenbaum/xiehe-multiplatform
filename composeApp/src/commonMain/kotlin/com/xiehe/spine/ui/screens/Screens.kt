package com.xiehe.spine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.DashboardRepository
import com.xiehe.spine.data.PatientRepository
import com.xiehe.spine.ui.components.SpineButton
import com.xiehe.spine.ui.components.SpineCard
import com.xiehe.spine.ui.components.SpineGlyph
import com.xiehe.spine.ui.components.SpineMiniBarChart
import com.xiehe.spine.ui.components.SpineProgressRing
import com.xiehe.spine.ui.components.SpineSelectablePill
import com.xiehe.spine.ui.components.SpineText
import com.xiehe.spine.ui.components.SpineTextField
import com.xiehe.spine.ui.theme.SpineTheme
import com.xiehe.spine.ui.theme.ThemeBrand
import com.xiehe.spine.ui.theme.ThemeMode
import com.xiehe.spine.ui.viewmodel.AppearanceViewModel
import com.xiehe.spine.ui.viewmodel.DashboardViewModel
import com.xiehe.spine.ui.viewmodel.LoginViewModel
import com.xiehe.spine.ui.viewmodel.PatientDetailViewModel
import com.xiehe.spine.ui.viewmodel.PatientFormViewModel
import com.xiehe.spine.ui.viewmodel.PatientsViewModel

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLogin: () -> Unit,
    onHealthCheck: () -> Unit,
    showNetworkDiagnostics: Boolean,
) {
    val state by vm.state.collectAsState()
    val spacing = SpineTheme.spacing
    val colors = SpineTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.x2l),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            SpineText(text = "协和医疗", style = SpineTheme.typography.display)
            SpineText(text = "登录医疗影像诊断系统", style = SpineTheme.typography.subhead)
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.base)) {
            SpineTextField(
                value = state.username,
                onValueChange = vm::updateUsername,
                placeholder = "用户名",
                modifier = Modifier.fillMaxWidth(),
            )
            SpineTextField(
                value = state.password,
                onValueChange = vm::updatePassword,
                placeholder = "密码",
                password = true,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn() + slideInVertically { -it / 3 },
                exit = fadeOut() + slideOutVertically { -it / 3 },
            ) {
                SpineText(
                    text = state.errorMessage ?: "",
                    style = SpineTheme.typography.subhead.copy(color = colors.error),
                )
            }
            state.errorDetails?.let {
                SpineText(
                    text = it,
                    style = SpineTheme.typography.caption.copy(color = colors.textSecondary),
                )
            }
            SpineButton(
                text = if (state.loading) "登录中..." else "登录",
                onClick = onLogin,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth(),
                leadingGlyph = SpineGlyph.PROFILE,
            )
            if (showNetworkDiagnostics) {
                SpineButton(
                    text = if (state.healthChecking) "检测中..." else "连接自检(/health)",
                    onClick = onHealthCheck,
                    enabled = !state.healthChecking,
                    modifier = Modifier.fillMaxWidth(),
                    leadingGlyph = SpineGlyph.BELL,
                )
                state.healthStatus?.let {
                    SpineText(
                        text = it,
                        style = SpineTheme.typography.subhead.copy(
                            color = if (it.startsWith("后端连通")) colors.success else colors.warning,
                        ),
                    )
                }
                state.healthDetails?.let {
                    SpineText(
                        text = it,
                        style = SpineTheme.typography.caption.copy(color = colors.textSecondary),
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    session: UserSession,
    repository: DashboardRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()
    val spacing = SpineTheme.spacing

    LaunchedEffect(session.accessToken) {
        vm.load(session, repository, onSessionUpdated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
        ) {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(
                    text = state.errorMessage ?: "",
                    style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error),
                )
            }
        }
        val overview = state.data
        if (overview == null) {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(text = if (state.loading) "加载工作台数据中..." else "暂无数据")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.base)) {
                StatCard("累计患者", overview.totalPatients.toString(), SpineGlyph.USERS, modifier = Modifier.weight(1f))
                StatCard("待处理影像", overview.pendingImages.toString(), SpineGlyph.HOURGLASS, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.base)) {
                StatCard("已完成影像", overview.processedImages.toString(), SpineGlyph.CHECK, modifier = Modifier.weight(1f))
                StatCard("累计影像", overview.totalImages.toString(), SpineGlyph.IMAGE, modifier = Modifier.weight(1f))
            }
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SpineText("完成率 ${overview.completionRate}%", style = SpineTheme.typography.title)
                        SpineText("平均处理时长 ${overview.averageProcessingTime} 小时")
                        SpineText("系统提醒 ${overview.systemAlerts}")
                    }
                    SpineProgressRing(progress = (overview.completionRate / 100f).toFloat())
                }
                SpineMiniBarChart(
                    values = listOf(
                        overview.newPatientsToday.toFloat(),
                        (overview.newPatientsWeek / 7f),
                        overview.imagesToday.toFloat(),
                        (overview.imagesWeek / 7f),
                    ),
                    labels = listOf("今患", "周均患", "今影", "周均影"),
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    glyph: SpineGlyph,
    modifier: Modifier = Modifier,
) {
    SpineCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpineText(text = title, style = SpineTheme.typography.subhead)
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
                    .background(
                        color = SpineTheme.colors.primaryMuted,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                com.xiehe.spine.ui.components.SpineGlyphIcon(
                    glyph = glyph,
                    tint = SpineTheme.colors.primary,
                    modifier = Modifier.width(16.dp).height(16.dp),
                )
            }
        }
        SpineText(text = value, style = SpineTheme.typography.display.copy(fontSize = 38.sp))
    }
}

@Composable
fun PatientsScreen(
    vm: PatientsViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onAddPatient: () -> Unit,
    onOpenPatient: (Int) -> Unit,
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
                SpineCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpineTheme.colors.surface)
                        .padding(0.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SpineText(text = patient.name, style = SpineTheme.typography.title)
                            SpineText(text = "ID: ${patient.patientId}  |  ${patient.gender}  ${patient.age}岁")
                            SpineText(text = patient.phone ?: "无手机号")
                        }
                        SpineButton(
                            text = "详情",
                            onClick = { onOpenPatient(patient.id) },
                            modifier = Modifier.width(72.dp),
                            leadingGlyph = SpineGlyph.PROFILE,
                        )
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

@Composable
fun PatientDetailScreen(
    patientId: Int,
    vm: PatientDetailViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(patientId, session.accessToken) {
        vm.load(patientId, session, repository, onSessionUpdated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.errorMessage?.let {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
            }
        }
        val detail = state.detail
        if (detail == null) {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText(text = if (state.loading) "加载患者详情中..." else "暂无患者详情")
            }
        } else {
            SpineCard(modifier = Modifier.fillMaxWidth()) {
                SpineText("姓名：${detail.name}", style = SpineTheme.typography.title)
                SpineText("患者ID：${detail.patientId}")
                SpineText("性别：${detail.gender}  年龄：${detail.age}")
                SpineText("出生日期：${detail.birthDate}")
                SpineText("联系电话：${detail.phone ?: "-"}")
                SpineText("身份证号：${detail.idCard ?: "-"}")
                SpineText("地址：${detail.address ?: "-"}")
                SpineText("紧急联系人：${detail.emergencyContactName ?: "-"}")
                SpineText("紧急联系人电话：${detail.emergencyContactPhone ?: "-"}")
            }
        }
    }
}

@Composable
fun PatientFormScreen(
    vm: PatientFormViewModel,
    session: UserSession,
    repository: PatientRepository,
    onSessionUpdated: (UserSession) -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .verticalScroll(scroll)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpineTextField(value = state.name, onValueChange = vm::updateName, placeholder = "患者姓名")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SpineSelectablePill(text = "男", selected = state.gender == "男", onClick = { vm.updateGender("男") })
            SpineSelectablePill(text = "女", selected = state.gender == "女", onClick = { vm.updateGender("女") })
        }
        SpineTextField(value = state.birthDate, onValueChange = vm::updateBirthDate, placeholder = "出生日期(yyyy-MM-dd)")
        SpineTextField(value = state.phone, onValueChange = vm::updatePhone, placeholder = "手机号")
        SpineTextField(value = state.idCard, onValueChange = vm::updateIdCard, placeholder = "身份证号")
        SpineTextField(value = state.address, onValueChange = vm::updateAddress, placeholder = "联系地址")
        state.errorMessage?.let {
            SpineText(text = it, style = SpineTheme.typography.subhead.copy(color = SpineTheme.colors.error))
        }
        SpineButton(
            text = if (state.loading) "提交中..." else "保存患者",
            onClick = {
                vm.submit(
                    session = session,
                    repository = repository,
                    onSessionUpdated = onSessionUpdated,
                    onSuccess = onSubmitSuccess,
                )
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
            leadingGlyph = SpineGlyph.CHECK,
        )
    }
}

@Composable
fun ProfileScreen(
    session: UserSession,
    onOpenAppearance: () -> Unit,
    onOpenPersonalInfo: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SpineCard(modifier = Modifier.fillMaxWidth()) {
            SpineText(text = session.fullName ?: session.username, style = SpineTheme.typography.title)
            SpineText(text = session.email ?: "未设置邮箱")
            SpineText(text = "用户ID: ${session.userId}")
        }
        SpineButton(text = "个人信息", onClick = onOpenPersonalInfo, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.PROFILE)
        SpineButton(text = "修改密码", onClick = onOpenChangePassword, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.CHECK)
        SpineButton(text = "外观设置", onClick = onOpenAppearance, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.DASHBOARD)
        SpineButton(text = "退出登录", onClick = onLogout, modifier = Modifier.fillMaxWidth(), leadingGlyph = SpineGlyph.BACK)
    }
}

@Composable
fun AppearanceScreen(vm: AppearanceViewModel) {
    val preference by vm.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SpineCard(modifier = Modifier.fillMaxWidth()) {
            SpineText(text = "主色体系", style = SpineTheme.typography.title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpineSelectablePill(
                    text = "绿色",
                    selected = preference.brand == ThemeBrand.GREEN,
                    onClick = { vm.updateBrand(ThemeBrand.GREEN) },
                )
                SpineSelectablePill(
                    text = "蓝色",
                    selected = preference.brand == ThemeBrand.BLUE,
                    onClick = { vm.updateBrand(ThemeBrand.BLUE) },
                )
            }
        }

        SpineCard(modifier = Modifier.fillMaxWidth()) {
            SpineText(text = "深浅模式", style = SpineTheme.typography.title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpineSelectablePill(
                    text = "跟随系统",
                    selected = preference.mode == ThemeMode.SYSTEM,
                    onClick = { vm.updateMode(ThemeMode.SYSTEM) },
                )
                SpineSelectablePill(
                    text = "按时间",
                    selected = preference.mode == ThemeMode.AUTO_TIME,
                    onClick = { vm.updateMode(ThemeMode.AUTO_TIME) },
                )
                SpineSelectablePill(
                    text = "浅色",
                    selected = preference.mode == ThemeMode.LIGHT,
                    onClick = { vm.updateMode(ThemeMode.LIGHT) },
                )
                SpineSelectablePill(
                    text = "深色",
                    selected = preference.mode == ThemeMode.DARK,
                    onClick = { vm.updateMode(ThemeMode.DARK) },
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpineTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SpineText(text = title, style = SpineTheme.typography.title)
            SpineText(
                text = description,
                style = SpineTheme.typography.subhead,
                modifier = Modifier.padding(horizontal = 24.dp),
                maxLines = 3,
            )
        }
    }
}
