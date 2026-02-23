# ApiClient 与错误处理

## 模块
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/data/ApiClient.kt`
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/data/ApiEnvelopeModels.kt`
- `composeApp/src/commonMain/kotlin/com/xiehe/spine/core/model/AppResult.kt`

## 功能分类
- API 请求统一入口（GET/POST/PUT）
- Envelope 解包
- HTTP 异常转换
- 网络错误语义化提示
- 可选网络诊断日志

## 具体实现

### 1. 请求协议
- 所有通用请求都走 `ApiClient.request()`。
- 约定后端返回 envelope：
  - 成功：`ApiEnvelope<T>`，真实数据在 `data`。
  - 失败：`ApiErrorEnvelope`。

### 2. 返回模型
- 成功：`AppResult.Success<T>`
- 失败：`AppResult.Failure`
  - 字段：`message`, `code`, `isUnauthorized`, `debugDetails`

### 3. 鉴权头注入
- `attachAuth(accessToken)` 自动注入 `Authorization: Bearer ...`。

### 4. 失败处理细节
- `ClientRequestException`：
  - 读取 status 与错误 envelope
  - 识别 401 并设置 `isUnauthorized = true`
- 其他异常：
  - 走 `classifyNetworkError(rawMessage)` 转换为用户可读信息

### 5. 网络错误语义化（示例）
- `ECONNREFUSED` -> 后端拒绝连接提示
- `CLEARTEXT` -> 明文 HTTP 被系统阻止提示
- `EPERM` -> 权限/系统策略阻止提示
- `ENETUNREACH` -> 网络不可达提示
- `timeout` -> 连接超时提示

### 6. 诊断日志
- `enableDiagnostics=true` 时打印请求开始与结束耗时。
- 默认关闭；Android Debug 模式可启用。

