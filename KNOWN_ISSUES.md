# Known Issues

## Device-dependent Network Behavior (Open)

- Symptom: the same build may connect normally on one phone but fail on another with `socket failed: ECONNREFUSED`.
- Current finding: some devices have VPN/proxy/network-policy differences, which can change routing behavior for app traffic.
- Impact: login/health-check requests to `http://115.190.121.59:8080` may fail on specific devices even when backend is healthy.
- Temporary workaround:
  - Test on a clean network profile/device.
  - Disable or bypass VPN/proxy for app traffic when possible.
- Next step: implement runtime network diagnostics page and fallback host/proxy strategy for constrained networks.
