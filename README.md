# Glucocalc — Glucose/HbA1c Calculator (Android)

A Kotlin calculator that estimates HbA1c and average blood glucose using the
**2008 ADAG Study Group formulas**, plus glucose/HbA1c unit conversions.
Behavior and look match the SwiftUI iOS/macOS version.

## Features

- Keypad-driven input: `7 8 9 C / 4 5 6 ⌫ / 1 2 3 = / 0 .`, with `=` computing,
  `C` clearing (zeros input + results), `⌫` backspacing
- **Calculate eAG** (from HbA1c) and **Calculate HbA1c** (from eAG) modes
- **UK (IFCC) switch** converts the current input to the other unit system on
  the fly (mg/dl ↔ mmol/L, % ↔ mmol/mol) and recomputes results
- Results always show both units (mg/dl + mmol/L, or % + mmol/mol)
- Display: three label/value rows, all values `0` initially and after `C`
- Day/night adaptive theme, rounded-square keys, Help dialog in the toolbar

## Math (2008 ADAG Study Group)

- eAG = 28.7 × A1c − 46.7 (mg/dl); mmol/L = /18
- A1c = (eAG + 46.7) / 28.7 (%); IFCC = 10.93 × A1c − 23.50 (mmol/mol)
- NGSP = 0.09148 × IFCC + 2.152; mmol/L ↔ mg/dl: ×18 / ÷18

## Build & install

Kotlin + ViewBinding + XML, AGP 8.6.0, Gradle wrapper 8.7, minSdk 26,
targetSdk 35, arm64-v8a.

```bash
./gradlew :app:assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.jocala.glucocalc/.MainActivity
```

## License

GPL-3.0 — see [LICENSE](LICENSE).
