# Glucocalc — Glucose/HbA1c Calculator (Android)

## Project Overview

Kotlin/Android port of the Glucocalc calculator (Qt original at
`/Volumes/disk3/source/glucocalc`; SwiftUI ports in `/Users/jeff/source/apple/glucocalc`).
Estimates HbA1c and average blood glucose using the 2008 ADAG formulas, plus
glucose/HbA1c unit conversions.

**Location:** `/Users/jeff/source/android/glucocalc`
**Package/App ID:** `com.jocala.glucocalc`
**Stack:** Kotlin + ViewBinding + XML (no Compose — matches sibling projects)
**Min SDK:** 26 | **Target/Compile SDK:** 35 | **ABI:** arm64-v8a
**Test device:** onntab1 (onn11Core, Android 16, connected via adb at
`adb devices` — serial `192.168.1.101:41833`)

---

## Build & Install

```bash
./gradlew :app:assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
adb -s 192.168.1.101:41833 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s 192.168.1.101:41833 shell am start -n com.jocala.glucocalc/.MainActivity
```

Gradle wrapper is 8.7 (copied from kalireader). AGP 8.6.0, Kotlin 1.9.24, JDK 21.

---

## Behavior (ported 1:1 from the SwiftUI/Qt apps)

- **Math** (`GlucocalcMath.kt`): `eAG = 28.7*A1c - 46.7` mg/dl (mmol/L = /18);
  `A1c = (eAG + 46.7)/28.7`; IFCC `= 10.93*A1c - 23.50`; NGSP `= 0.09148*IFCC + 2.152`.
  UK(IFCC) means input in mmol/L or mmol/mol (convert first: `*18`, or
  `0.09148*input + 2.152`).
- **Keypad**: `7 8 9 C / 4 5 6 ⌫ / 1 2 3 = / 0 . (empty) (empty)`. `=` computes,
  `C` clears (zeros input + both results), `⌫` backspaces.
- **Input rules**: max `999.99` (3 integer digits, 2 decimals), no leading
  zeros, single `.`; typing a digit after `=` starts a fresh entry.
- **Display**: three label/value rows (input + both outputs), labels left,
  values right-aligned; all show `0` initially and after `C`; input value
  larger/bolder than the outputs; units live in the labels.
- **Mode** (Calculate eAG / Calculate HbA1c): switching clears results, keeps input.
- **UK (IFCC) switch**: converts the current input to the other unit system on
  the fly and recomputes results (results always show both units).
- **Invalid input** (empty/zero): "Invalid input!" alert.

---

## Conventions

- **Commits:** Do not commit unless explicitly directed
- **Math:** Keep formulas exactly matching the Qt source; do not "improve" values
- **Project gen:** No xcodegen here — plain Gradle; wrapper 8.7 stays in sync
  with the other Android projects
