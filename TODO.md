# TODO

Complete the items in order; each block lists the remaining Functional (RF) and Non-Functional (RNF) requirements plus polish work that keeps you on spec.

## Priority 1 – Mandatory RF gaps
1. [ ] **RF6 – Manage Products**
   - `app/src/main/java/com/comprartir/mobile/products/presentation/ProductsScreen.kt:26` only lets users search and list products; there is no way to create, edit, favorite, or delete catalog entries even though `ProductsRepository` exposes `upsertProduct`/`deleteProduct`.
   - Build product detail / editor flows (dialogs or screens) that call the repository, add deletion with confirmation, and surface validation + error messages. Consider letting users pick default unit/quantity so list creation pulls consistent data.
2. [ ] **RF10 – Categorize Products**
   - `app/src/main/java/com/comprartir/mobile/products/presentation/CategorizeProductsScreen.kt:17` is just a title with a TODO comment. Implement the drag & drop / bulk assignment UI, hook it to `CategorizeProductsViewModel.assignCategory`, and display both unassigned and categorized items so users see progress.
3. [ ] **RF2 – Verify Account: resend flow**
   - `app/src/main/java/com/comprartir/mobile/feature/auth/verify/VerifyViewModel.kt:86` starts a countdown but never calls the backend (`TODO: authRepository.resendVerification`). Wire this to the API so users can request a new code and show feedback/snackbar errors.
4. [ ] **RF11 – Acquire products screen parity**
   - The dedicated acquire route (`app/src/main/java/com/comprartir/mobile/lists/presentation/AcquireProductScreen.kt:14`) is still a stub even though the requirement calls for a checklist-like experience. Mirror the web UX by loading the active list, allowing quick mark-as-purchased interactions, and adding offline-friendly cues.
5. [ ] **RF5 – Profile enhancements**
   - Profile editing works for text fields, but avatar/background actions are still TODOs (`app/src/main/java/com/comprartir/mobile/profile/presentation/ProfileScreen.kt:64`). Plug in an image picker + upload flow (or disable the controls) so profile management feels complete.
6. [ ] **Dashboard activity feed**
   - `HomeViewModel` hardcodes `recentActivity = emptyList()` (`app/src/main/java/com/comprartir/mobile/feature/home/viewmodel/HomeViewModel.kt:33`), so the activity section in `HomeScreen` never shows data. Populate it using either a backend feed or synthesized events from local list mutations to match the spec’s “incorporate class characteristics” guidance for the main screen.

## Priority 2 – Optional RF backlog (do once the mandatory work is stable)
1. [ ] **RF15 – Manage Products in Pantry**
   - The Pantry screen at `app/src/main/java/com/comprartir/mobile/pantry/presentation/PantryScreen.kt:29` is read-only, while `PantryRepository` (`app/src/main/java/com/comprartir/mobile/pantry/data/PantryRepository.kt:37`) already exposes create/update/delete/share operations.
   - Add UI to add/remove pantry items, edit quantities/expiration dates, manage multiple pantries, and invite/revoke collaborators so this optional requirement is actually met.
2. [ ] **RF14 – Recurring Shopping Lists**
   - Creation supports the `isRecurring` flag (`ListsViewModel.showCreateDialog`), but editing ignores it: `ListsViewModel.confirmEditList` and `ShoppingListsRepository.updateList` (`app/src/main/java/com/comprartir/mobile/lists/data/ListsRepository.kt:188-277`) never send the recurring value, so updates reset lists to non-recurring.
   - Persist the checkbox state when editing, surface recurring status in list cards, and (optionally) add a recap screen for recurring instances (`AppDestination.OptionalRecurringLists`).

## RNF gaps
1. [x]  **RNF1 – Localization**
    
    The application must support at least two languages (Spanish and English), automatically selecting the language based on the device settings and using proper string resources instead of hard-coded text.

2. [x]  **RNF2 – Contextual app bar**
    
    The application must provide a consistent top app bar that displays a contextual title and relevant actions for each screen (e.g., share, edit, filter), following platform conventions.
    
3. [ ]  **RNF3 – Personalization / customization**
    
    The application must allow users to personalize aspects of their experience, such as theme (light/dark/system), language preference, and notification toggles, persisting these choices across sessions.
    
4. [ ]  **RNF4 – Support for different form factors**
    
    The application must adapt its layout to different device form factors (phones and tablets), using responsive patterns such as bottom navigation on compact devices and navigation rail / master–detail layouts on larger screens.
    
5. [x]  **RNF5 – Support for different orientations**
    
    The application must behave correctly in both portrait and landscape orientations, preserving state across orientation changes and adjusting layouts to use the available space efficiently.
    
6. [x]  **RNF6 – Platform compatibility (Android 10+)**
    
    The application must target and run correctly on Android 10 (API 29) or higher, respecting platform guidelines for permissions, networking, and navigation.
    
---

Optionals:

7. [ ]  **RNF7 – Barcode integration (optional / advanced)**
    
    The application should support barcode scanning to add or identify products, integrating with the camera in a way that is consistent with Android UX patterns.
    
8. [ ]  **RNF8 – Voice input integration (optional / advanced)**
    
    The application should support voice input for actions such as adding products or searching lists, using Android’s speech recognition capabilities where available.
    
9. [ ]  **RNF9 – Photo capture integration (optional / advanced)**
    
    The application should allow capturing or attaching photos (e.g., of products or receipts), handling permissions, storage, and previews according to Android best practices.

## Navigation & UX hygiene (after the RF/RNF work)
1. [ ] **Hide routes for unfinished optional features**
    - Bottom navigation exposes optional destinations even when their screens are placeholders (`OptionalRecurringLists`, `OptionalPantryManagement`). Only expose implemented features or finish the matching flows before the routes appear.
2. [ ] **Extend personalization controls**
    - The Settings screen hints at more customization (`TODO` at `app/src/main/java/com/comprartir/mobile/shared/settings/SettingsScreen.kt:105`). Once core/optional requirements are done, add accent color + density toggles to better satisfy RNF3’s “allow customization” spirit.

## Release packaging
1. [ ] **Ship the deliverables**
    - Produce the clean ZIP/RAR bundle without the `build/` directory, include an installable APK, and document install instructions (device type + API level + install steps) as required in `deliverables.md`.
