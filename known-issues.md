# Known Issues

## Device-dependent Network Behavior (Resolved)

- Symptom: the same build may connect normally on one phone but fail on another with `socket failed: ECONNREFUSED`.
- Root cause confirmed: the failing phone had app-level network access permission disabled.
- Fix: enable network access permission for the app on that device.
- Current status: backend connectivity works normally after permission is enabled.

## 消息通知系统

- `GET /api/v1/notifications/messages/stats` 与 `GET /api/v1/notifications/messages` 返回数据不一致。当前真实后端会返回 `stats.total_messages = 15`、`stats.unread_messages = 3`，但消息列表接口只返回 2 条消息，且 `pagination.total = 2`。
- `PUT /api/v1/notifications/messages/{message_id}/read` 虽然返回 `200 消息已标记为已读`，但随后重新查询列表时，`is_read` 仍然保持原值，后端未持久化已读状态。
- `DELETE /api/v1/notifications/messages/{message_id}` 虽然返回 `200 消息已删除`，但随后重新查询列表时，该消息仍然存在，后端未持久化删除状态。
- `POST /api/v1/notifications/messages` 当前真实后端返回 `500 发送消息失败: 'dict' object has no attribute 'id'`。
