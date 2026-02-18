# Changelog

## 2026-02-18
- Added `AGENT.md` development rules for mandatory change recording and major-change commits.
- Replaced template UI with a Compose Multiplatform MVVM application shell.
- Implemented custom HEX-based design system (`SpineTheme`) with brand and light/dark mode support.
- Added auto-by-time dark mode support to the appearance settings.
- Implemented Android persistent storage for session and appearance preferences.
- Added Ktor + serialization network stack and integrated login/dashboard/patient APIs.
- Added patient list/detail/create flows and profile/appearance settings screens.
- Added placeholder routes for unstable backend modules.
- Split GitHub release workflows into dev/main channels with different cleartext HTTP policies.
- Main release now enforces no cleartext HTTP, while dev release enables cleartext HTTP for current backend.
- Standardized release APK filename to `xiehe-spine.apk` and app display name to `xiehe-spine`.
- Added common tests for theme preference persistence and session store behavior.
- Recorded a known issue: backend connectivity can vary by device/network environment (some phones route traffic through VPN/proxy and may fail with `ECONNREFUSED`).
- Fixed shell safe-area strategy to avoid full-screen content being squeezed on some devices.
- Fixed overlay-route navigation bug where bottom tab switching failed from personal info subpages.
- Refactored shell UI by extracting a dedicated `SpineNavBar`/`SpineNavItem` module and moving `MobileShell` to a separate file.
- Reduced startup debug noise: removed automatic network probe logs; debug diagnostics remain opt-in and release-disabled.
