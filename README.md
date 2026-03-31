<div align="center">

```
██████╗  ██████╗ ████████╗ █████╗ ████████╗███████╗
██╔══██╗██╔═══██╗╚══██╔══╝██╔══██╗╚══██╔══╝██╔════╝
██████╔╝██║   ██║   ██║   ███████║   ██║   █████╗  
██╔══██╗██║   ██║   ██║   ██╔══██║   ██║   ██╔══╝  
██║  ██║╚██████╔╝   ██║   ██║  ██║   ██║   ███████╗
╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝   ╚═╝   ╚══════╝
                                          T I L E
```

**A minimal Android Quick Settings tile to lock your screen orientation — instantly.**

[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen?style=flat-square)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-success?style=flat-square)]()

</div>

---

## ✦ What is this?

**RotationTile** adds a single tile to your Android Quick Settings panel that toggles your screen between **portrait** and **landscape** — without fumbling through settings menus.

One tap. Done.

---

## ✦ Features

- 🔄 &nbsp;**Instant toggle** between portrait and landscape
- 📌 &nbsp;**Locks orientation** — disables auto-rotate so it stays where you put it
- 🎛️ &nbsp;**Quick Settings tile** — lives right in your notification shade
- 🪶 &nbsp;**Featherweight** — no background services, no battery drain, no bloat
- 🔒 &nbsp;**One permission** — only needs `WRITE_SETTINGS` to change rotation

---

## ✦ Preview

```
┌────────────────────────────────────────┐
│  Quick Settings                        │
│                                        │
│  ┌──────────┐  ┌──────────┐            │
│  │  Wi-Fi   │  │ Bluetooth│            │
│  └──────────┘  └──────────┘            │
│                                        │
│  ┌──────────┐  ┌──────────┐            │
│  │ ↻ Rotate │  │ Airplane │            │
│  │ [tap me] │  │  mode    │            │
│  └──────────┘  └──────────┘            │
└────────────────────────────────────────┘
```

---

## ✦ Requirements

| Requirement | Version |
|-------------|---------|
| Android | 7.0+ (API 24) |
| Android Studio | Hedgehog or newer |
| JDK | 17+ |
| Gradle | 9.4.1 |

---

## ✦ Installation

### 1 — Clone the repo

```bash
git clone https://github.com/yourusername/RotationTile.git
cd RotationTile
```

### 2 — Build the APK

```bash
./gradlew assembleDebug
```

The APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 3 — Install on your device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or run directly from Android Studio with the ▶ button.

---

## ✦ Setup (one-time)

> The app needs `WRITE_SETTINGS` to change system rotation. This is a one-time step.

1. **Open the app** from your launcher — a dialog will appear
2. Tap **"Open Settings"** and enable the toggle for *Rotation Tile*
3. Pull down your notification shade and tap the **pencil / edit** icon
4. Find **"Rotate Screen"** and drag it into your tiles
5. Done — tap the tile anytime to toggle orientation ✓

---

## ✦ How it works

```
User taps tile
      │
      ▼
TileService.onClick()
      │
      ├─► Read current USER_ROTATION from Settings.System
      │
      ├─► Disable ACCELEROMETER_ROTATION (lock auto-rotate off)
      │
      ├─► Write opposite rotation value (0° ↔ 90°)
      │
      └─► Collapse notification panel
```

No background processes. No receivers. No wake locks. The tile service is only alive while the Quick Settings panel is open.

---

## ✦ Project structure

```
RotationTile/
├── app/src/main/
│   ├── java/com/bin/mirrormate/
│   │   ├── RotationTileService.java   # Core tile logic
│   │   ├── MainActivity.java          # Permission setup screen
│   │   └── DismissActivity.java       # Panel collapse helper
│   ├── res/
│   │   ├── drawable/                  # Vector icons
│   │   └── values/strings.xml
│   └── AndroidManifest.xml
├── app/build.gradle
└── settings.gradle
```

---

## ✦ Permissions

| Permission | Why |
|------------|-----|
| `WRITE_SETTINGS` | Required to change `USER_ROTATION` in system settings |

No internet. No location. No contacts. No nonsense.

---

## ✦ Compatibility notes

| Android version | Panel collapse method |
|----------------|----------------------|
| 14+ (API 34+) | `startActivityAndCollapse(PendingIntent)` — official API |
| 7–13 (API 24–33) | `startActivityAndCollapse(Intent)` — deprecated but functional |

---

## ✦ License

```
MIT License — do whatever you want with it.
```

---

<div align="center">

Made with ☕ and mild frustration at Android's rotation button

</div>
