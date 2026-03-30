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
