# Known Issues

## Device-dependent Network Behavior (Resolved)

- Symptom: the same build may connect normally on one phone but fail on another with `socket failed: ECONNREFUSED`.
- Root cause confirmed: the failing phone had app-level network access permission disabled.
- Fix: enable network access permission for the app on that device.
- Current status: backend connectivity works normally after permission is enabled.
