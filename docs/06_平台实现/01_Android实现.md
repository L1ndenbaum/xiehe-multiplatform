# Android 实现

## 模块
- 启动：`androidApp/src/main/kotlin/com/xiehe/spine/MainActivity.kt`
- 容器注入：`androidApp/src/main/kotlin/com/xiehe/spine/AndroidAppContainer.kt`
- Manifest：`androidApp/src/main/AndroidManifest.xml`
- Android actual：`composeApp/src/androidMain/kotlin/...`

## 功能分类
- Android App 壳与依赖注入
- 本地持久化（会话、主题、图片缓存）
- Android 文件选择与下载保存
- Android 图标资源渲染

## 具体实现

### 1. 启动与注入
- `MainActivity` 在 `onCreate` 中创建 `AppContainer` 并传入 `App()`。
- `BASE_URL` 来自 `BuildConfig`，由 Gradle buildType 写入。

### 2. 存储实现
- `AndroidKeyValueStore`：SharedPreferences（`spine_prefs`）。
- `AndroidImageBinaryStore`：`filesDir/image_cache` 按 `image_<id>.bin` 持久化。

### 3. 文件输入输出
- 图片选择器：`rememberImageFilePickerLauncher`（`OpenDocument(image/*)`）。
- JSON 选择器：`rememberJsonFilePickerLauncher`（json/text）。
- 文件下载保存：
  - Android 10+：MediaStore Downloads
  - Android 10-：`getExternalFilesDir(DIRECTORY_DOWNLOADS)`

### 4. 图标渲染
- Android 通过 `IconToken.androidDrawableName()` -> drawable 名 -> `painterResource(id)`。
- drawable 向量由 `androidApp` 构建任务从共享 SVG 自动转换生成。

### 5. 网络与明文策略
- Manifest：`usesCleartextTraffic` 由占位符注入。
- 默认：debug 允许、release 禁止；CI 按 tag 分支归属覆盖。

