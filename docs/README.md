# Spine Multiplatform 项目文档总览

## 1. 项目范围
本项目是“协和医疗脊柱影像管理系统”的移动端重写工程，基于 Kotlin Compose Multiplatform。
当前支持平台为 Android 与 iOS 两个移动端。

设计与实现约束：
- UI 采用自定义 HEX 语义色系统，不依赖 Material3 颜色体系。
- 架构采用 MVVM：`Screen -> ViewModel -> Repository -> ApiClient`。
- 通用逻辑集中在 `composeApp/src/commonMain`，平台差异通过 `expect/actual` 与 platform source set 实现。

## 2. 架构总览

### 模块
- `:composeApp`：KMP 共享模块（业务、UI、状态、仓储、主题、资源）。
- `:androidApp`：Android 启动壳（`com.android.application`），依赖 `:composeApp`。
- `iosApp`：SwiftUI 包壳，承载 Compose UIViewController。

### 功能分类
- 核心层：会话与主题偏好存储、统一结果模型、平台时间/返回键抽象。
- 数据层：认证、患者、影像、测量/报告、通知、AI 推理、缓存。
- 表现层：通用组件 + 页面 + 导航壳 + 动画与交互。
- 平台层：文件选择/下载保存/图标渲染等 `expect/actual` 适配。

### 具体实现
- 总入口在 `composeApp/src/commonMain/kotlin/com/xiehe/spine/App.kt`。
- 一级 tab / shell 路由在 `composeApp/src/commonMain/kotlin/com/xiehe/spine/MainShellHost.kt`。
- overlay 路由在 `composeApp/src/commonMain/kotlin/com/xiehe/spine/OverlayHost.kt`。
- 共享动画 contract 与 modal host 在 `composeApp/src/commonMain/kotlin/com/xiehe/spine/ui/motion/`。
- 依赖组装在 `composeApp/src/commonMain/kotlin/com/xiehe/spine/data/AppContainer.kt`。
- Android 容器在 `androidApp/src/main/kotlin/com/xiehe/spine/AndroidAppContainer.kt`。

## 3. 代码目录速览
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/core`：状态基础设施。
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/data`：API 模型与仓储。
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/ui/theme`：颜色/排版/间距与主题运行时。
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/ui/components`：可复用 UI 组件。
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/ui/screens`：业务页面。
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/ui/viewmodel`：页面状态与业务编排。
- `composeApp/src/commonMain/composeResources/drawable`：SVG 图标资源（48 个）。
- `androidApp/src/main`：Android Manifest、图标与 Activity 启动入口。
- `.github/workflows/android-ci.yml`：CI + tag Release 工作流。

## 4. 当前功能完成面
- 认证：登录、注册、JWT 刷新、个人信息读取与增量更新。
- 工作台：概览指标、待处理任务卡片、消息入口。
- 患者中心：检索/筛选/分页、详情、创建、编辑。
- 影像中心：检索筛选、缩略图、下载、删除、上传、进入分析。
- 影像分析：
  - 影像查看、双指缩放、平移约束、手工测量/辅助图形。
  - AI 检测与本地六指标计算。
  - 测量列表显隐、单项删除、报告面板、标注导入/导出、保存。
- 外观设置：品牌色（绿/蓝）+ 模式（系统/按时间/浅色/深色）。

## 5. 发布与环境策略
- 开发分支 `dev`：Release 构建允许明文 HTTP（用于当前后端环境）。
- 主分支 `main`：Release 构建禁止明文 HTTP。
- tag 推送触发 Release，产物统一命名 `xiehe-spine.apk`。

具体见：`docs/01_工程与构建/02_CI_CD与发布.md`。

## 6. 文档索引

### 工程与构建
- `docs/01_工程与构建/01_模块结构.md`
- `docs/01_工程与构建/02_CI_CD与发布.md`

### 核心能力
- `docs/02_核心能力/01_会话与鉴权.md`
- `docs/02_核心能力/02_主题系统.md`
- `docs/02_核心能力/03_缓存系统.md`

### 数据层
- `docs/03_数据层/01_ApiClient与错误处理.md`
- `docs/03_数据层/02_业务仓储实现.md`

### UI 组件层
- `docs/04_UI组件层/01_导航与外壳.md`
- `docs/04_UI组件层/02_通用组件.md`
- `docs/04_UI组件层/03_影像分析组件.md`

### 功能模块
- `docs/05_功能模块/01_登录与注册.md`
- `docs/05_功能模块/02_工作台与消息.md`
- `docs/05_功能模块/03_患者中心.md`
- `docs/05_功能模块/04_影像中心与上传.md`
- `docs/05_功能模块/05_影像分析.md`
- `docs/05_功能模块/06_个人中心与个人信息.md`

### 平台实现
- `docs/06_平台实现/01_Android实现.md`
- `docs/06_平台实现/02_其他平台现状.md`

### 质量保障
- `docs/07_质量保障/01_测试与已知问题.md`

## 7. 当前技术债与后续建议
- `ImageAnalysisViewModel`（约 1800 行）承担了过多职责，建议拆分为：
  - AI 结果映射器
  - 手工测量计算器
  - 报告编排器
  - 导入导出编解码器
- 多平台文件选择/下载保存在 iOS/JVM/JS/Wasm 仍为占位实现。
- iOS `currentHour24()` 目前固定返回 12，按时间主题模式在 iOS 不准确。
- 报告面板含大量占位字段（只绑定 `reportText` 等核心字段），可按后端扩展逐项接入。
