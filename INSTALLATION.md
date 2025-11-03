# Installation Guide

## Recommended Test Devices
- **Pixel 6 (Phone)** – Android 14 (API 34), portrait & landscape
- **Pixel Tablet** – Android 14 (API 34), landscape-first experience
- **Pixel 4 XL** – Android 11 (API 30) for backward compatibility to minSdk 29

## Prerequisites
1. Android Studio Giraffe (or newer) with Android SDK Platform 34 installed
2. Java 17 or Android Studio bundled JDK
3. USB debugging enabled on physical devices (Settings → Developer options)

## Build Steps
1. Clone or copy the repository to your local machine.
2. Configure the Android SDK path in `local.properties` (`sdk.dir=/path/to/sdk`) if Android Studio has not generated it.
3. Optionally set `comprartir.apiBaseUrl` in `local.properties`, `gradle.properties`, or an `.env.android` file (see below). When unset the app falls back to `http://10.0.2.2:8080/api`, which maps to `localhost:8080` on the emulator.
4. From the project root run `./gradlew assembleReleaseApk` *(or use `./scripts/assemble_release.sh`)*.
5. The generated APKs will be stored under `app/build/outputs/apk/` grouped by flavor (`phone`, `tablet`).

## Physical Device Installation (ADB)
1. Connect the device via USB and verify it appears: `adb devices`.
2. Install the preferred flavor (example for phone):
   ```bash
   adb install -r app/build/outputs/apk/phone/release/app-phone-release.apk
   ```
3. Launch *Comprartir* from the device app drawer.

## Emulator Installation
1. Create an Android Virtual Device that matches a recommended profile above.
2. Start the emulator, then drag-and-drop the APK onto the emulator window **or** run `adb install` as in the physical device section.

## Post-Install Checks
- Confirm the UI loads in the device locale (Spanish/English)
- Rotate the device to verify adaptive layouts (bottom bar vs navigation rail)
- Toggle theme/language in Settings to verify DataStore persistence stubs
- Inspect inputs and CTAs: pill corners, 44dp height, and brand-green focus halo should match the Vue experience

## Environment Configuration
Create a copy of `.env.android.example` as `.env.android` (gitignored) to override environment-specific values without touching checked-in files. Supported keys today:

- `comprartir.apiBaseUrl` – Base URL for the Comprartir backend (default `http://10.0.2.2:8080/api`).

You can define the same key in `local.properties` or `gradle.properties` if you prefer Gradle-based configuration.

## Current Persistence & API Status
- Room entities/DAOs persist users, profiles, products, categories, shopping lists, list items, and pantry inventory. Each repository loads from Retrofit first (with paging helpers) and then emits Room-backed flows for offline access.
- `core/network/ComprartirApi` is a fully implemented Retrofit interface using Kotlinx Serialization, bearer-token interception, and debug logging.
- Authentication responses store the JWT inside `AuthTokenRepository` (DataStore). A 401 response clears the token so the user can re-authenticate cleanly.
- Repository methods perform optimistic UI updates when possible and surface errors to the view models for Compose screens to display.

## Design Tokens in Use
- Palette: surfaces `#F4F6F8`, borders `#E5E7EB`, primary text `#0F172A`, brand greens (`#4DA851` / `#3E8E47` / `#E9F7F0`)
- Typography: HK/Hanken Grotesk hierarchy (Display 32/700, Title 24–18/600, Body 15/400, Labels 15–13/500) using the bundled font resources under `app/src/main/res/font`
- Shapes: 10dp radii for small elements, 16dp cards, 24dp dialogs, and pill-shaped buttons/inputs by default
- Inputs: 44dp height, 16dp horizontal padding, placeholder color `#9CA3AF`, and focus halo `rgba(77,168,81,0.12)`
