```
███╗   ███╗██╗██████╗ ██████╗  ██████╗ ██████╗ ███╗   ███╗ █████╗ ████████╗███████╗
████╗ ████║██║██╔══██╗██╔══██╗██╔═══██╗██╔══██╗████╗ ████║██╔══██╗╚══██╔══╝██╔════╝
██╔████╔██║██║██████╔╝██████╔╝██║   ██║██████╔╝██╔████╔██║███████║   ██║   █████╗  
██║╚██╔╝██║██║██╔══██╗██╔══██╗██║   ██║██╔══██╗██║╚██╔╝██║██╔══██║   ██║   ██╔══╝  
██║ ╚═╝ ██║██║██║  ██║██║  ██║╚██████╔╝██║  ██║██║ ╚═╝ ██║██║  ██║   ██║   ███████╗
╚═╝     ╚═╝╚═╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚══════╝
```

**Your screen mirroring companion — orientation, brightness and voice, all from your pocket.**

[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen?style=flat-square)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

---

## ✦ What is this?

**MirrorMate** is a lightweight Android utility built for people who do screen mirroring. When your phone is connected to a TV, projector or external display, you shouldn't have to reach for it every time you want to rotate the screen, adjust brightness, or summon your voice assistant.

MirrorMate puts everything in reach — through a Quick Settings tile and fully customizable volume button gestures — so you can stay in control without ever touching the screen.

---

## ✦ Features

- 🔄 &nbsp;**Instant rotation toggle** — portrait ↔ landscape from your Quick Settings tile
- 🔒 &nbsp;**Locks orientation** — disables auto-rotate so it stays exactly where you put it
- 💡 &nbsp;**Per-orientation brightness** — set a different brightness for portrait and landscape, applied automatically on every toggle
- 🎛️ &nbsp;**Custom volume trigger sequences** — define your own up/down button pattern (3–8 presses) to toggle rotation or wake your voice assistant
- 🎙️ &nbsp;**Voice assistant trigger** — custom volume sequence wakes your default voice assistant instantly
- ⏱️ &nbsp;**Adjustable detection window** — tune how fast the gesture needs to be (500ms–10000ms)
- 📵 &nbsp;**Call-aware** — volume triggers are automatically paused during phone calls
- 🎛️ &nbsp;**Quick Settings tile** — lives right in your notification shade
- 🪶 &nbsp;**Featherweight** — no background services, no battery drain, no bloat

---

## ✦ Preview

```
┌─────────────────────────────────────────┐
│  Quick Settings                         │
│                                         │
│  ┌──────────┐     ┌──────────┐          │
│  │  Wi-Fi   │     │Bluetooth │          │
│  └──────────┘     └──────────┘          │
│                                         │
│  ┌──────────┐     ┌──────────┐          │
│  │ ↻ Rotate │     │ Airplane │          │
│  │ [tap me] │     │  mode    │          │
│  └──────────┘     └──────────┘          │
└─────────────────────────────────────────┘
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
git clone https://github.com/yourusername/mirrormate.git
cd mirrormate
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

### Required permission
1. **Open MirrorMate** from your launcher
2. Tap **"Continue"** and enable **"Modify system settings"** for MirrorMate
3. Come back to the app — the settings screen will appear

### Add the tile
1. Pull down your notification shade and tap the **pencil / edit** icon
2. Find **"Rotate Screen"** and drag it into your tiles
3. Tap it anytime to toggle orientation ✓

### Enable volume gestures *(optional)*
> ⚠️ If installed outside the Play Store, Android restricts Accessibility access. Follow the in-app instructions carefully.

1. Tick **"Volume trigger"** in the MirrorMate settings screen
2. Grant the **"Read phone state"** permission — required to pause triggers during calls
3. Follow the step-by-step guide to enable the **Accessibility service**
4. Once enabled, the checkbox activates automatically
5. Optionally configure your own custom trigger sequences

### Configure custom trigger sequences *(optional)*
1. With volume trigger enabled, tap **"Configure rotation sequence"** or **"Configure voice sequence"**
2. Press volume up/down buttons in your desired pattern (3–8 presses)
3. Tap **Save** — MirrorMate validates that sequences don't conflict with each other

---

## ✦ How it works

```
User taps tile  ────────────────────────────────────────────────────┐
                                                                    │
Custom volume sequence matched  ─────────────────────────────────┐  │
                                                                 │  ▼
                                                       RotationHelper.execute()
                                                                  │
                                       ┌──────────────────────────┼──────────────────────┐
                                       │                          │                      │
                                       ▼                          ▼                      ▼
                               toggleRotation()         adjustBrightness()     collapseStatusBar()
                                       │                          │                      │
                           Read USER_ROTATION          Read new orientation     DismissActivity
                           Disable auto-rotate         Apply portrait or        finishes instantly
                           Write opposite value        landscape brightness


Custom voice sequence matched  ──► ACTION_VOICE_COMMAND  (wakes default voice assistant)


VolumeButtonService buffer logic:

  on every volume key press:
    │
    ├─► detection window expired? ──► reset buffer
    │
    ├─► append press to buffer (▼ or ▲)
    │
    ├─► buffer ends with rotation sequence? ──► fire rotation, reset buffer
    │
    ├─► buffer ends with voice sequence? ──► fire voice, reset buffer
    │
    └─► buffer length > longest sequence? ──► reset buffer
```

---

## ✦ Sequence configuration rules

- Minimum **3 presses**, maximum **8 presses**
- The two sequences (rotation and voice) must not be **identical**
- Neither sequence can be a **prefix** of the other — would always trigger the shorter one first
- Default rotation sequence: `▼ ▼ ▼ ▼ ▼`
- Default voice sequence: `▲ ▲ ▲ ▲ ▲`
- Triggers are **paused during phone calls** and while the sequence recorder is open

---

## ✦ Project structure

```
MirrorMate/
├── app/src/main/
│   ├── java/com/bin/mirrormate/
│   │   ├── MainActivity.java           # Settings screen + permission flow
│   │   ├── SequenceActivity.java       # Custom trigger sequence recorder
│   │   ├── RotationTileService.java    # Quick Settings tile
│   │   ├── VolumeButtonService.java    # Accessibility + volume gesture detection
│   │   ├── RotationHelper.java         # Shared rotation + brightness logic
│   │   └── DismissActivity.java        # Panel collapse helper
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml       # Settings UI
│   │   │   └── activity_sequence.xml  # Sequence recorder UI
│   │   ├── drawable/                   # Vector icons
│   │   ├── values/strings.xml          # All strings
│   │   └── xml/accessibility_service_config.xml
│   └── AndroidManifest.xml
├── app/build.gradle
└── settings.gradle
```

---

## ✦ Permissions

| Permission | Why |
|------------|-----|
| `WRITE_SETTINGS` | Required to change rotation and brightness system settings |
| `BIND_ACCESSIBILITY_SERVICE` | Required to detect volume button presses in background |
| `READ_PHONE_STATE` | Required to pause volume triggers during active phone calls |

No internet. No location. No contacts. No nonsense.

---

## ✦ Compatibility notes

| Android version | Panel collapse method |
|----------------|----------------------|
| 14+ (API 34+) | `startActivityAndCollapse(PendingIntent)` — official API |
| 7–13 (API 24–33) | `startActivityAndCollapse(Intent)` — deprecated but functional |

| Install source | Accessibility setup |
|----------------|----------------------|
| Play Store | Standard — enable directly in Accessibility settings |
| Sideloaded APK | Requires "Allow restricted settings" first (Android 13+) |

---

## ✦ License

```
MIT License — do whatever you want with it.
```

---

Made with ☕ for everyone tired of reaching for their phone during screen mirroring