# CI/CD 与发布

## 模块
- 工作流文件：`.github/workflows/android-ci.yml`

## 功能分类
- CI：分支/PR 构建与基础测试
- Release：tag 驱动，CI 成功后发布 APK
- 环境策略：dev/main 分支按可追溯祖先关系决定明文 HTTP 策略

## 具体实现

### 1. 触发规则
- `push` 到 `dev`、`main`：触发 `ci` job。
- `pull_request` 到 `dev`、`main`：触发 `ci` job。
- `push` tag（`v*`）：触发 `ci`，然后触发 `release`（`needs: ci`）。

### 2. CI job
- JDK 17 + Gradle 缓存。
- 执行命令：
  - `./gradlew :androidApp:assembleDebug :composeApp:jvmTest --stacktrace`
- 上传调试 APK 工件：`androidApp/build/outputs/apk/debug/*.apk`。

### 3. Release job
- 仅在 tag push 场景执行：`if: startsWith(github.ref, 'refs/tags/')`。
- 计算 release profile：
  - 若 tag 对应提交是 `origin/main` 祖先：`allow_cleartext=false`，非预发布。
  - 若是 `origin/dev` 祖先：`allow_cleartext=true`，预发布。
- 打包命令：
  - `./gradlew :androidApp:assembleDebug -PallowCleartextTraffic=<true|false>`
- 产物标准名：`xiehe-spine.apk`
- 发布动作：`softprops/action-gh-release@v2`

### 4. HTTP 明文策略
- Manifest 中使用占位：`android:usesCleartextTraffic="${usesCleartextTraffic}"`
- `androidApp/build.gradle.kts`：
  - `debug` 默认允许（可通过 `-PallowCleartextTraffic` 覆盖）
  - `release` 默认不允许（可通过 `-PallowCleartextTraffic` 覆盖）
- 工作流通过 tag 分支归属自动传入该参数，保证：
  - dev release 可连当前 HTTP 后端
  - main release 强制 HTTPS 预期

### 5. 应用标识与包名
- Android applicationId：`com.xiehe.spine`
- 应用名资源：`androidApp/src/main/res/values/strings.xml`
- 当前显示名称：`Mesh 智慧门诊`

