# 🌊 SeaSentry — Maritime Border Geofencing & Distress Alert System

**SeaSentry** is an offline-capable, real-time maritime safety and emergency warning system designed for fishing vessels and coastal trawlers. It prevents inadvertent boundary crossings across the **International Maritime Boundary Line (IMBL)**, provides immediate visual, auditory, and haptic warnings, simulates offline peer-to-peer mesh SOS distress relays, and supplies an offline maritime emergency survival guide.

---

## 📌 Table of Contents
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [Key Features & Capabilities](#-key-features--capabilities)
- [Module Breakdown](#-module-breakdown)
  - [1. Geofence & Alert Engine](#1-geofence--alert-engine)
  - [2. Interactive IMBL Simulation Demo](#2-interactive-imbl-simulation-demo)
  - [3. Emergency Siren & Haptic Manager](#3-emergency-siren--haptic-manager)
  - [4. P2P Mesh SOS Relay & Coast Guard Command](#4-p2p-mesh-sos-relay--coast-guard-command)
  - [5. Offline Maritime Survival Guide](#5-offline-maritime-survival-guide)
  - [6. Offline Room Database & Event Logging](#6-offline-room-database--event-logging)
- [Screen Catalog & UI Flow](#-screen-catalog--ui-flow)
- [Project Directory Structure](#-project-directory-structure)
- [Unit & Integration Test Suite](#-unit--integration-test-suite)
- [Building & Running the App](#-building--running-the-app)

---

## 🛠 Architecture & Tech Stack

| Component | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9+ | 100% Kotlin codebase |
| **UI Framework** | Jetpack Compose (Material 3) | Declarative UI, smooth animations, zero external bloat |
| **Architecture** | Unidirectional Data Flow (UDF) / StateFlow | Reactive UI state using Kotlin Coroutines & `StateFlow` |
| **Local Persistence**| Room Database (SQLite) | Offline persistence for geofence breach events & SOS dispatches |
| **Audio & Haptics** | `MediaPlayer` & Android `Vibrator` | Dedicated hardware siren loops and custom waveform vibration |
| **Navigation** | Stateful Compose Router (`Crossfade`) | Zero-lag screen transitions and modal overlays |
| **Geodesy Engine** | Pure Haversine Mathematical Model | Great-circle distance & bearing calculation without external API dependencies |

---

## 🚀 Key Features & Capabilities

1. **Deterministic IMBL Proximity Geofencing**: Computes real-time vessel distance to international borders and enforces multi-tier warning zones.
2. **Interactive Live Simulation Demo**: Visualizes real-time vessel telemetry, progress, and distance countdown towards the IMBL border with step controls.
3. **Pulsing Emergency Warning Modal**: Full-screen modal with immediate nautical instructions (`TURN BOAT 180° SOUTH IMMEDIATELY`), high-priority siren, and haptic feedback.
4. **Mesh SOS Distress Beacon**: Simulates ad-hoc multi-hop vessel-to-vessel mesh propagation delivering alerts to the Coast Guard station.
5. **Coast Guard Command Dashboard**: Tactical interface for monitoring distress signals, mesh hop paths, and active fleet vessels.
6. **Offline Maritime Survival Guide**: Field handbook featuring 8 emergency guides, actionable checklists, VHF radio scripts, and search filters.
7. **Room Safety History Logging**: Persistent audit trail of all boundary approaches and emergency events.

---

## 📦 Module Breakdown

### 1. Geofence & Alert Engine
Located in: [`com.seasentry.app.geofence`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/geofence)
- [`GeofenceEngine.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/geofence/GeofenceEngine.kt): Calculates spherical Earth distance (Haversine formula) and initial bearing from vessel coordinates to maritime boundary points.
- [`AlertLevel.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/geofence/AlertLevel.kt): Defines the 4-tier alert threshold model:
  - 🟢 **SAFE**: Distance $> 1000\text{ m}$ — Normal navigation.
  - 🟡 **ADVISORY**: Distance $\le 1000\text{ m}$ — Approaching proximity alert.
  - 🟠 **WARNING**: Distance $\le 500\text{ m}$ — Danger alert, reduce speed & alter course.
  - 🔴 **CRITICAL**: Distance $\le 200\text{ m}$ / Breach $\le 50\text{ m}$ — Immediate 180° turn mandated.

### 2. Interactive IMBL Simulation Demo
Located in: [`com.seasentry.app.demo`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/demo)
- [`MockLocationProvider.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/demo/MockLocationProvider.kt): 9-step deterministic route beginning at harbor departure (`18.9220° N`) and progressing across all 4 alert zones up to the boundary line (`18.9390° N`).
- [`DemoController.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/demo/DemoController.kt): Manages the simulation lifecycle (`start`, `pause`, `restart`, `reset`), triggers the critical warning modal on entry to critical zone, and logs events to Room DB.

### 3. Emergency Siren & Haptic Manager
Located in: [`com.seasentry.app.alert`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/alert)
- [`EmergencyAlertManager.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/alert/EmergencyAlertManager.kt): Manages audio playback (`R.raw.emergency_alert`) with `USAGE_ALARM` attributes, and controls multi-pulse vibration patterns (`VibrationEffect.createWaveform`) for high-urgency notifications.

### 4. P2P Mesh SOS Relay & Coast Guard Command
Located in: [`com.seasentry.app.sos`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/sos) and [`com.seasentry.app.relay`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/relay)
- [`SOSManager.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/sos/SOSManager.kt): Coordinates single-tap emergency SOS broadcast, database storage, and resolution.
- [`RelayManager.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/relay/RelayManager.kt): Simulates multi-hop peer relay:
  $$\text{Vessel Beacon} \longrightarrow \text{Nearby Vessel-12 (Hop 1)} \longrightarrow \text{Coast Guard Sector Alpha (Hop 2)}$$
- [`CoastGuardScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/CoastGuardScreen.kt): Sector Alpha fleet monitor with live distress banner, transmission path details, and acknowledge/resolve workflow.

### 5. Offline Maritime Survival Guide
Located in: [`com.seasentry.app.survival`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/survival)
- [`SurvivalGuideRepository.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/survival/SurvivalGuideRepository.kt): 8 comprehensive maritime survival topics:
  1. **IMBL Border Emergency & Evasion** (180° turn & border de-escalation)
  2. **Engine Failure in Open Sea** (Sea anchor / drogue deployment & diagnostics)
  3. **Man Overboard (MOB) Rescue Protocol** (Williamson Turn, marker release)
  4. **Severe Weather & Gale Storm Defense** (Heave-to maneuvering, hatch dogging)
  5. **Collision Risk & COLREGS Evasion** (Starboard turns, acoustic whistle signals)
  6. **Fire Onboard Marine Vessel** (Fuel remote shut-off, Class B suppression)
  7. **SOS Mayday & GMDSS Transmission** (Standard VHF 16 voice format & EPIRBs)
  8. **Cold Water Survival & Hypothermia** (H.E.L.P. posture, circular huddles)

### 6. Offline Room Database & Event Logging
Located in: [`com.seasentry.app.data`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/data)
- [`AppDatabase.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/data/AppDatabase.kt): SQLite database instance.
- [`AlertDao.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/data/AlertDao.kt) & [`AlertEvent.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/data/AlertEvent.kt): Persists all geofence proximity events.
- [`SOSDao.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/data/SOSDao.kt) & [`SOSEvent.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/data/SOSEvent.kt): Persists emergency distress broadcasts and resolution timestamps.

---

## 📱 Screen Catalog & UI Flow

```
[ LoginScreen ]
       │ (Vessel ID Auth)
       ▼
  [ HubScreen ] ── (Bottom SOS Action) ──► [ SOS Broadcast & Mesh Relay ]
   ├── [ Interactive IMBL Demo ] ── (≤ 200m Breach) ──► [ CriticalWarningScreen (Modal) ]
   ├── [ Safety History & Logs ]
   ├── [ Offline Survival Guide ]
   ├── [ Coast Guard Command View ]
   └── [ App Settings ]
```

| Screen | File | Highlights |
| :--- | :--- | :--- |
| **Hub Screen** | [`HubScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/HubScreen.kt) | Live vessel coordinates, NavIC satellite indicator, live ocean analytics card, modular quick actions, docked emergency SOS button. |
| **Demo Screen** | [`DemoScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/DemoScreen.kt) | Vessel telemetry card, real-time distance countdown, dynamic progress bar, geofence threshold legend, simulation playback buttons. |
| **Critical Warning** | [`CriticalWarningScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/CriticalWarningScreen.kt) | Pulsing bell animation, emergency red background, bold evasive commands, siren & vibration integration, mute action. |
| **Coast Guard View** | [`CoastGuardScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/CoastGuardScreen.kt) | Sector Alpha fleet radar list, active distress banner, mesh hop counter, acknowledge & resolve dispatch button. |
| **Safety History** | [`SafetyHistoryScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/SafetyHistoryScreen.kt) | Chronological alert history from Room DB, severity tier badges, clear history option. |
| **Survival Guide** | [`SurvivalGuideScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/SurvivalGuideScreen.kt) | Search bar, category chips (Navigation, Propulsion, Weather, Emergency, Rescue), interactive checklist cards, VHF channel scripts. |
| **Settings Screen** | [`SettingsScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/SettingsScreen.kt) | Vessel registration info, alert volume toggles, dark theme toggle, offline cache status. |
| **Login Screen** | [`LoginScreen.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/main/java/com/seasentry/app/ui/screens/LoginScreen.kt) | Vessel registration number input, quick start credentials, nautical header art. |

---

## 📂 Project Directory Structure

```
SeaSentry/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/seasentry/app/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── alert/
│   │   │   │   │   └── EmergencyAlertManager.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── AlertDao.kt
│   │   │   │   │   ├── AlertEvent.kt
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── SOSDao.kt
│   │   │   │   │   └── SOSEvent.kt
│   │   │   │   ├── demo/
│   │   │   │   │   ├── DemoController.kt
│   │   │   │   │   └── MockLocationProvider.kt
│   │   │   │   ├── geofence/
│   │   │   │   │   ├── AlertLevel.kt
│   │   │   │   │   ├── GeofenceConfig.kt
│   │   │   │   │   └── GeofenceEngine.kt
│   │   │   │   ├── relay/
│   │   │   │   │   └── RelayManager.kt
│   │   │   │   ├── sos/
│   │   │   │   │   └── SOSManager.kt
│   │   │   │   ├── survival/
│   │   │   │   │   ├── SurvivalGuideRepository.kt
│   │   │   │   │   └── SurvivalModel.kt
│   │   │   │   └── ui/
│   │   │   │       ├── SeaSentryApp.kt
│   │   │   │       ├── components/
│   │   │   │       │   └── SeaSentryComponents.kt
│   │   │   │       ├── screens/
│   │   │   │       │   ├── CoastGuardScreen.kt
│   │   │   │       │   ├── CriticalWarningScreen.kt
│   │   │   │       │   ├── DemoScreen.kt
│   │   │   │       │   ├── HubScreen.kt
│   │   │   │       │   ├── LoginScreen.kt
│   │   │   │       │   ├── SafetyHistoryScreen.kt
│   │   │   │       │   ├── SettingsScreen.kt
│   │   │   │       │   └── SurvivalGuideScreen.kt
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt
│   │   │   │           ├── Theme.kt
│   │   │   │           └── Type.kt
│   │   │   └── res/
│   │   │       ├── raw/
│   │   │       │   └── emergency_alert.wav
│   │   │       └── values/
│   │   └── test/java/com/seasentry/app/
│   │       ├── DemoControllerTest.kt
│   │       ├── EntityTest.kt
│   │       ├── GeofenceEngineTest.kt
│   │       ├── MockLocationProviderTest.kt
│   │       ├── SOSManagerTest.kt
│   │       └── SurvivalGuideTest.kt
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🧪 Unit & Integration Test Suite

All core systems are tested across 6 unit test suites:

| Test File | Target Under Test | Coverage |
| :--- | :--- | :--- |
| [`GeofenceEngineTest.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/test/java/com/seasentry/app/GeofenceEngineTest.kt) | `GeofenceEngine` | Haversine distance accuracy (~1890m benchmark), bearing math, Safe/Advisory/Warning/Critical transitions, breach condition. |
| [`MockLocationProviderTest.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/test/java/com/seasentry/app/MockLocationProviderTest.kt) | `MockLocationProvider` | Waypoint advancement, state resets, direct step jumping, waypoint coordinate validity. |
| [`DemoControllerTest.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/test/java/com/seasentry/app/DemoControllerTest.kt) | `DemoController` | State flow bindings, alert transitions, critical screen activation, mute & reset behavior. |
| [`SOSManagerTest.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/test/java/com/seasentry/app/SOSManagerTest.kt) | `SOSManager` & `RelayManager` | Distress signal trigger, status state changes, resolve lifecycle. |
| [`SurvivalGuideTest.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/test/java/com/seasentry/app/SurvivalGuideTest.kt) | `SurvivalGuideRepository` | Guide indexing, category filtering, search keyword querying. |
| [`EntityTest.kt`](file:///c:/Users/Shlok/Desktop/SeaSentry/app/src/test/java/com/seasentry/app/EntityTest.kt) | Room Entities (`AlertEvent`, `SOSEvent`) | Entity construction, default values, and timestamp handling. |

To run the full test suite from the terminal:
```bash
./gradlew test
```

---

## ⚡ Building & Running the App

### Prerequisites
- **Android Studio Iguana / Jellyfish / Ladybug** or newer
- **JDK 17**
- **Android SDK API 26+** (Target SDK 34)

### Build Steps
```bash
# Clone repository
git clone https://github.com/Prranjal15/SeaSentry.git
cd SeaSentry

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`