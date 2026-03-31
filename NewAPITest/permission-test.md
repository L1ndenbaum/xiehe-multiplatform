# 权限管理 API 实测记录

- 测试日期：2026-03-30
- 测试账号：`lirr`
- 测试目标：验证 `openapi.json` 中 `权限管理` 模块接口在真实后端上的实际可用性

## 汇总

- `openapi.json` 中权限管理模块共 23 个接口
- 已实际联调 17 个接口
- 已确认当前账号可成功调用：13 个
- 当前账号因权限或业务上下文限制未成功：3 个
- 后端明确报错：1 个
- 由于需要第二账号、邀请/申请数据或存在明显副作用，本轮未安全验证：6 个

## 已确认可用

| 接口 | 结果 | 说明 |
| --- | --- | --- |
| `GET /api/v1/permissions/permissions` | 200 | 权限列表可正常返回，分页结构正常 |
| `POST /api/v1/permissions/permissions` | 200 | 可成功创建权限；本次创建了 `PERM_20260330_075442` |
| `GET /api/v1/permissions/roles` | 200 | 角色列表可正常返回 |
| `POST /api/v1/permissions/roles` | 200 | 可成功创建角色；本次创建了 `ROLE_20260330_075442` |
| `GET /api/v1/permissions/user-groups` | 200 | 用户组列表可正常返回 |
| `GET /api/v1/permissions/users/6/permissions` | 200 | 当前用户权限详情可正常返回 |
| `POST /api/v1/permissions/assign-permissions` | 200 | 传空 `permissions` 也能成功返回，并写入审计日志 |
| `GET /api/v1/permissions/audit-logs` | 200 | 审计日志列表可正常返回 |
| `GET /api/v1/permissions/permission-matrix` | 200 | 可返回空矩阵结构，接口本身可用 |
| `GET /api/v1/permissions/teams/search` | 200 | 团队搜索可正常返回 |
| `GET /api/v1/permissions/teams/my` | 200 | 可正常返回当前账号所属团队；当前账号属于团队 `id=5` |
| `GET /api/v1/permissions/teams/5/members` | 200 | 团队成员列表可正常返回 |
| `GET /api/v1/permissions/invitations/my` | 200 | 我的团队邀请可正常返回，当前为空 |

## 当前账号受限或受业务上下文限制

| 接口 | 结果 | 说明 |
| --- | --- | --- |
| `POST /api/v1/permissions/teams` | 403 | 当前账号不能创建团队，后端返回“只有系统管理员可以创建团队” |
| `GET /api/v1/permissions/teams/5/join-requests` | 403 | 当前账号是团队成员但不是团队管理员，后端返回“只有团队管理员可以查看加入申请” |
| `POST /api/v1/permissions/teams/5/apply` | 400 | 当前账号已是该团队成员，后端返回“您已是该团队成员” |

## 后端错误

| 接口 | 结果 | 说明 |
| --- | --- | --- |
| `GET /api/v1/permissions/users` | 500 | 后端 SQL 报错：`Unknown column 'u.full_name' in 'field list'` |

## 本轮未安全验证

这些接口要么需要第二个测试账号，要么需要现成的邀请/申请记录，要么会直接修改现有团队成员，不适合在当前账号和当前环境下直接落成功态验证：

- `POST /api/v1/permissions/teams/{team_id}/join-requests/{request_id}/review`
- `DELETE /api/v1/permissions/teams/{team_id}/join-requests/{request_id}`
- `PATCH /api/v1/permissions/teams/{team_id}/members/{user_id}/role`
- `DELETE /api/v1/permissions/teams/{team_id}/members/{user_id}`
- `POST /api/v1/permissions/teams/{team_id}/invite`
- `POST /api/v1/permissions/invitations/{invitation_id}/respond`

## 结论

- 以当前 `lirr` 账号实测，权限管理模块的基础读接口大部分可用。
- 当前实现和权限模型有一个值得注意的点：医生账号也能成功调用“创建权限 / 创建角色 / 分配权限”，这和常见权限设计不一致，后续接入前需要确认这是不是预期行为。
- 团队接口已经接入了角色限制，至少“创建团队”和“查看加入申请”会根据身份返回 403。
- `/api/v1/permissions/users` 当前不能用于接入，后端需要先修复。

## 2026-03-31 管理员与普通用户只读对比

- 对比账号：
  - 管理员：`admin`
  - 普通用户：`lirr`
- 本轮只发起了登录和 `GET` 请求，没有使用管理员账号做任何创建、修改、删除操作。

### 账号身份差异

| 账号 | user.id | roles | is_superuser | is_system_admin | system_admin_level |
| --- | --- | --- | --- | --- | --- |
| `admin` | `1` | `["admin"]` | `true` | `true` | `2` |
| `lirr` | `6` | `["doctor"]` | `false` | `false` | `0` |

### 实测差异结论

| 接口 | `admin` | `lirr` | 结论 |
| --- | --- | --- | --- |
| `GET /api/v1/permissions/permissions` | `200` | `200` | 两者都能看权限列表 |
| `GET /api/v1/permissions/roles` | `200` | `200` | 两者都能看角色列表 |
| `GET /api/v1/permissions/user-groups` | `200` | `200` | 两者都能看用户组列表 |
| `GET /api/v1/permissions/audit-logs` | `200` | `200` | 两者都能看审计日志，普通用户也未被限制 |
| `GET /api/v1/permissions/permission-matrix` | `200` | `200` | 两者都能看权限矩阵 |
| `GET /api/v1/permissions/teams/search` | `200` | `200` | 两者都能搜索团队 |
| `GET /api/v1/permissions/teams/my` | `200`，返回 3 个团队 | `200`，返回 1 个团队 | 差异来自账号所属团队不同，不是接口权限拦截 |
| `GET /api/v1/permissions/teams/5/members` | `200` | `200` | 两者都能看团队 5 的成员列表 |
| `GET /api/v1/permissions/teams/5/join-requests` | `200`，返回 2 条申请 | `403` | 这里有明确权限差异，普通用户被团队管理员校验拦住 |
| `GET /api/v1/permissions/invitations/my` | `200`，0 条 | `200`，0 条 | 两者都能看自己的邀请箱 |
| `GET /api/v1/permissions/users/{other_id}/permissions` | `200` | `200` | 两者都能跨用户查询权限详情，权限边界偏宽 |
| `GET /api/v1/permissions/users` | `500` | `500` | 与账号无关，是统一的后端 SQL 错误 |

### 补充观察

- `admin` 和 `lirr` 的登录态身份字段差异是正常的：管理员账号被标记为 `is_superuser=true`、`is_system_admin=true`。
- 但在真正的只读接口访问上，很多接口并没有体现出严格的管理员限制：
  - 普通用户 `lirr` 也能读取 `权限列表 / 角色列表 / 用户组列表 / 审计日志 / 权限矩阵`
  - 普通用户 `lirr` 也能查询其他用户的权限详情：`GET /api/v1/permissions/users/1/permissions` 返回 `200`
- 当前已实测到的明确权限分界点主要是团队管理侧：
  - `GET /api/v1/permissions/teams/5/join-requests`：管理员 `200`，普通用户 `403`
- `GET /api/v1/permissions/users/{user_id}/permissions` 返回结果本身也存在数据异常：
  - 登录态里 `admin.roles = ["admin"]`、`lirr.roles = ["doctor"]`
  - 但该接口返回的 `roles` 数组对两者都是空
  - `effective_permissions` 对两者也都是空
  这说明登录态声明和权限详情接口返回并不一致。

## 2026-03-31 补充：团队管理员账号对比

- 新增对比账号：
  - 团队管理员但非系统管理员：`TestZjh`
- 本轮同样只发起了登录和 `GET` 请求，没有使用该账号做任何创建、修改、删除操作。

### 三类账号身份对比

| 账号 | user.id | roles | is_superuser | is_system_admin | system_admin_level |
| --- | --- | --- | --- | --- | --- |
| `admin` | `1` | `["admin"]` | `true` | `true` | `2` |
| `TestZjh` | `23` | `["doctor"]` | `false` | `false` | `0` |
| `lirr` | `6` | `["doctor"]` | `false` | `false` | `0` |

### 新增实测差异

| 接口 | `admin` | `TestZjh` | `lirr` | 结论 |
| --- | --- | --- | --- | --- |
| `GET /api/v1/permissions/teams/5/join-requests` | `200`，2 条 | `200`，2 条 | `403` | 这里的真实权限边界是“团队管理员”而不是“系统管理员” |
| `GET /api/v1/permissions/teams/my` | `200`，3 个团队 | `200`，1 个团队 | `200`，1 个团队 | 三者都能查看自己的团队列表 |
| `GET /api/v1/permissions/teams/5/members` | `200` | `200` | `200` | 三者都能看团队成员列表 |
| `GET /api/v1/permissions/audit-logs` | `200` | `200` | `200` | 连团队管理员和普通医生也都能看审计日志，权限边界偏宽 |
| `GET /api/v1/permissions/users/{other_id}/permissions` | `200` | `200` | `200` | 三者都能跨用户查询权限详情，未体现更严格的隔离 |
| `GET /api/v1/permissions/users` | `500` | `500` | `500` | 与账号角色无关，统一是后端 SQL 错误 |

### 进一步结论

- `TestZjh` 的登录态没有系统管理员标记，但它能够访问 `GET /api/v1/permissions/teams/5/join-requests`，说明该接口确实按团队管理员身份授权。
- `TestZjh` 和 `lirr` 在登录态上都显示 `roles = ["doctor"]`，但前者能看团队加入申请、后者不能，这说明团队管理权限并不体现在登录响应的 `roles/permissions` 字段里。
- 当前后端的权限模型至少分成两层：
  - 系统级身份：`is_superuser / is_system_admin / system_admin_level`
  - 团队级身份：是否为某个团队的 `ADMIN`
- 但很多本应更敏感的只读接口目前没有体现出这两层差异：
  - `GET /api/v1/permissions/audit-logs`
  - `GET /api/v1/permissions/permissions`
  - `GET /api/v1/permissions/roles`
  - `GET /api/v1/permissions/user-groups`
  - `GET /api/v1/permissions/users/{user_id}/permissions`
  这些接口对系统管理员、团队管理员、普通医生都返回 `200`。
- `GET /api/v1/permissions/users/{user_id}/permissions` 的返回仍然不可靠：
  - `admin / TestZjh / lirr` 三个账号的 `effective_permissions` 都是空
  - `roles` 计数也都是 0
  - 但登录态里的身份字段和实际接口权限又明显不同
  所以这个接口目前不能作为前端权限展示或前端鉴权判断的可信来源。
