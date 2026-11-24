# NRCM STUDENT – Attendance & Academic Portal

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Unit%20Tests-10%20Passing-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

A modern, fast, and privacy-focused Android application designed for students of **Narsimha Reddy Engineering College (NRCM)** to monitor daily attendance, track period-wise records, view semester statistics, and simulate attendance planning.

Built natively using **Kotlin**, **Jetpack Compose**, and **Material 3**.

---

## 📱 Features

- **🔐 Direct Portal Authentication**  
  Direct, encrypted session handshake with the official college portal (`nrcmec.org/Student`). Zero intermediate proxy servers; credentials and session cookies never leave your device.

- **👤 Dynamic Student Name Display**  
  Fetches the logged-in student's name dynamically upon authentication, displaying it on the top app bar with auto-sizing text and title-case formatting.

- **📅 Period-Wise Month Breakdown**  
  Faithfully reproduces the official college attendance structure:  
  `Date | P1 | P2 | P3 | P4 | P5 | P6`  
  With color-coded **Present (P)** and **Absent (A)** indicators and horizontal month tab switching with an underline indicator.

- **📊 Semester Summary & Statistics**  
  Provides semester-wide statistics: total conducted classes, attended periods, and overall percentage, with a distinct highlight for the semester total.

- **🎯 Smart Attendance Planner**  
  - Target percentage presets: **75%**, **80%**, **85%**, or custom percentage slider.
  - Calculates exact **safe classes to skip** (while maintaining target).
  - Calculates exact **classes you must attend** to recover attendance.
  - Projects working days ($\lceil N / 6 \rceil$ periods/day) and estimated calendar target date (excluding Sundays).
  - Post-attendance skip projection so you know when you can breathe easy.

- **⚡ Multi-User Cache Isolation & Offline Mode**  
  Caches attendance data locally for instant loading upon reopening. Automatically wipes prior user cache if a different roll number logs in, preventing data leakage across shared devices.

- **🎨 Official College Branding**  
  Complete with official NRCM college adaptive and round launcher icons (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`, `v26 adaptive-icon`).

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose & Material 3
- **Architecture:** MVVM (Model-View-ViewModel) + unidirectional data flow (`StateFlow`)
- **Networking:** OkHttp 4 (CookieJar session management, TLS 1.3)
- **HTML Parsing:** Jsoup (DOM extraction from college portal tables)
- **Asynchronous Execution:** Kotlin Coroutines (`Dispatchers.IO`, `async`/`await`)
- **Unit Testing:** JUnit 4 (100% passing test coverage for planner calculations)

---

## 📂 Project Structure

```
NRCM/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/nrcm/student/
│   │   │   │   ├── MainActivity.kt           # Navigation and root activity
│   │   │   │   ├── PlannerLogic.kt           # Pure math engine for attendance planner
│   │   │   │   ├── data/
│   │   │   │   │   ├── AttendanceCache.kt    # SharedPreferences local storage & isolation
│   │   │   │   │   └── AttendanceModels.kt   # Data models (StudentAttendance, MonthRecord, DayRecord)
│   │   │   │   ├── network/
│   │   │   │   │   ├── LoginResult.kt        # Sealed classes for auth state
│   │   │   │   │   └── PortalClient.kt       # OkHttp + Jsoup parser for nrcmec.org
│   │   │   │   └── ui/
│   │   │   │       ├── MainViewModel.kt      # State management and UI events
│   │   │   │       └── Screens.kt            # Jetpack Compose UI (Login, Months, Summary, Planner)
│   │   │   └── res/                          # Adaptive launcher mipmaps, drawables, themes, colors
│   │   └── test/
│   │       └── java/com/nrcm/student/
│   │           └── PlannerLogicTest.kt       # Unit test suite for planner logic
└── build.gradle.kts                          # Build configuration
```

---

## 🚀 Building & Running from Source

### Prerequisites
- **JDK 17** (e.g., Microsoft OpenJDK 17 or Eclipse Temurin 17)
- **Android SDK** (API Level 34 or 35 recommended)

### 1. Clone the repository
```bash
git clone https://github.com/bharat24082007-spec/Nrcm-attendence-app.git
cd Nrcm-attendence-app
```

### 2. Run Unit Tests
```powershell
./gradlew testDebugUnitTest
```

### 3. Build the Debug APK
```powershell
./gradlew assembleDebug
```
The compiled APK will be generated at:  
`app/build/outputs/apk/debug/app-debug.apk`

### 4. Install onto a connected device or emulator
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.nrcm.student/.MainActivity
```

---

## 🔒 Privacy & Security

- **No Remote Telemetry or Tracking:** The application connects exclusively to the official `https://www.nrcmec.org` college domain.
- **Zero Logging of Credentials:** Student roll numbers, passwords, session cookies, and personal profile names are never logged or stored on any external server.
- **Cache Sanitization:** Clicking **Logout** or logging in with another roll number immediately deletes all stored credentials and attendance caches from local device storage.

---

## ⚖️ Disclaimer

This application is an independent, open-source utility developed for the convenience of students at Narsimha Reddy Engineering College. It is not an officially sponsored product of the college administration. All trademarks, college names, and logos belong to their respective owners.
