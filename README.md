# Comprartir Mobile (TP3-HCI)

Jetpack Compose Android app that recreates the Comprartir shopping experience from the existing Vue web project. The project is structured around MVVM, Hilt dependency injection, and Material 3 adaptive design so phones, tablets, portrait, and landscape modes stay consistent with the web brand.

## Architecture & Stack
- Kotlin, Jetpack Compose, Material 3, Navigation Compose, and window-size classes for responsive UI
- MVVM with ViewModel + StateFlow, Room persistence, and repositories that hydrate from Retrofit + DataStore-backed auth tokens
- Dagger Hilt for dependency injection, wiring database DAOs, network components, and feature repositories
- Product flavors (`phone`, `tablet`) and orientation-aware layouts via `ResponsiveAppScaffold`
- Feature toggles (`FeatureFlags`) prepared for optional RF12–RF15 and RNF7–RNF9 integrations
- `core/network` bundles the Retrofit API, Kotlinx Serialization models, paging helpers, and an OkHttp interceptor that maps the stored JWT into `Authorization` headers

## Design System Highlights
- Palette mirrors the Vue app tokens: neutrals (`#F4F6F8` surfaces, `#E5E7EB` borders, `#0F172A` text), brand greens (`#4DA851` primary, `#3E8E47` pressed, `#E9F7F0` tint)
- Typography now uses the bundled HK/Hanken Grotesk font weights (400/500/600/700) under `app/src/main/res/font`
- Shapes follow rounded guidelines (10dp small radius, 16dp cards, 24dp dialogs) with pill-shaped buttons, chips, and inputs
- Spacing/gutter tokens match the web layout (16–40dp gutters, 1360dp max content width) and responsive scaffold centers content inside those bounds
- Elevated surfaces map web shadows to Compose elevation tokens (shadow1 ≈ 2dp, shadow2 ≈ 8dp)
- `ComprartirOutlinedTextField` enforces 44dp height, 16dp horizontal padding, pill corners, brand-green focus halo, and placeholder colors

## Module & Package Layout
```
app/
  core/        # design system, DI, navigation, datastore, room
  auth/        # RF1–RF4 account flows
  profile/     # RF5 profile management
  products/    # RF6 + RF10 product catalogue & categorisation
  lists/       # RF7–RF11 shopping list management and sharing
  pantry/      # RF15 scaffolding & RNF hooks
  shared/      # dashboard, settings, components, state helpers
```

- RF1 Register, RF2 Verify, RF3 Update Password, RF4 Sign-in/out screens backed by the Retrofit auth service + Room cache
- RF5 Profile editing backed by network persistence and DataStore user preferences
- RF6 Product catalogue with live search, Room caching, and network synchronisation
- RF7 Lists dashboard with create/share scaffolding, list detail view, and acquisition toggles backed by list/list-item repositories
- RF8/9/11 List detail + acquired toggles and acquisition screen stubs
- RF10 Categorise products screen connected to repository/category flows
- RF12–RF15 optional routes surfaced as toggled TODO placeholders
- RNF1 Locale-aware strings (`values/` + `values-es/`) and automatic device locale
- RNF2 Adaptive top app bar with contextual actions and RNF7–RNF9 stubs
- RNF3 Personalisation controls (theme, language, notifications)
- RNF4/RNF5 Responsive navigation (bottom bar vs rail) based on window size/orientation
- RNF6 Minimum SDK 29, target SDK 34

## Configuration
The app reads runtime configuration values from Gradle properties so each developer can point to a local or remote backend:

- **Base URL** – set `comprartir.apiBaseUrl` in `local.properties`, `gradle.properties`, or `.env.android` (see below). If no value is provided the default `http://10.0.2.2:8080/api` is used (emulator-localhost).
- **Optional .env** – copy `.env.android.example` to `.env.android` to keep credential-free overrides out of version control.
- Both product flavors expose the value through `BuildConfig.COMPRARTIR_API_BASE_URL` and the Hilt `NetworkModule` applies it to Retrofit.

## Build & Run
1. Ensure Android Studio / command-line SDK 34 tooling is installed.
2. Generate the Gradle wrapper distribution (already bundled) and sync: `./gradlew tasks`.
3. To build debug APKs: `./gradlew assemblePhoneDebug assembleTabletDebug`.
4. To build release APKs use the helper task: `./gradlew assembleReleaseApk` or `./scripts/assemble_release.sh`.

> `build/` outputs are ignored via `.gitignore` and should remain excluded from any ZIP submissions.

## Next Steps / TODO Highlights
1. Integrate shopping-list sharing flows (RF8/RF11) once the backend endpoints support invitations & acceptance.
2. Hook pantry CRUD UI to the new repository mutations (add/edit/delete) and expose bulk actions.
3. Introduce purchase history screens (RF13) using the `PurchaseDto` surface and Room cache.
4. Wire barcode scanning, voice commands, and photo capture into `IntegrationPlaceholders` for RNF7–RNF9.
5. Add instrumentation/unit tests once feature logic stabilises, especially for repository error handling and DataStore token management.
