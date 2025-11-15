# TODO

Complete the items in order; each block lists the remaining Functional (RF) and Non-Functional (RNF) requirements plus polish work that keeps you on spec.

## Priority 1 – Mandatory RF gaps
1. [ ] **RF6 – Manage Products**
   - `app/src/main/java/com/comprartir/mobile/products/presentation/ProductsScreen.kt:26` only lets users search and list products; there is no way to create, edit, favorite, or delete catalog entries even though `ProductsRepository` exposes `upsertProduct`/`deleteProduct`.
   - Build product detail / editor flows (dialogs or screens) that call the repository, add deletion with confirmation, and surface validation + error messages. Consider letting users pick default unit/quantity so list creation pulls consistent data.
2. [ ] **RF10 – Categorize Products**
   - `app/src/main/java/com/comprartir/mobile/products/presentation/CategorizeProductsScreen.kt:17` is just a title with a TODO comment. Implement the drag & drop / bulk assignment UI, hook it to `CategorizeProductsViewModel.assignCategory`, and display both unassigned and categorized items so users see progress.

## Priority 2 – Optional RF backlog (do once the mandatory work is stable)
3. [ ] **RF15 – Manage Products in Pantry**
   - The Pantry screen at `app/src/main/java/com/comprartir/mobile/pantry/presentation/PantryScreen.kt:29` is read-only, while `PantryRepository` (`app/src/main/java/com/comprartir/mobile/pantry/data/PantryRepository.kt:37`) already exposes create/update/delete/share operations.
   - Add UI to add/remove pantry items, edit quantities/expiration dates, manage multiple pantries, and invite/revoke collaborators so this optional requirement is actually met.
4. [ ] **RF14 – Recurring Shopping Lists**
   - Creation supports the `isRecurring` flag (`ListsViewModel.showCreateDialog`), but editing ignores it: `ListsViewModel.confirmEditList` and `ShoppingListsRepository.updateList` (`app/src/main/java/com/comprartir/mobile/lists/data/ListsRepository.kt:188-277`) never send the recurring value, so updates reset lists to non-recurring.
   - Persist the checkbox state when editing, surface recurring status in list cards, and (optionally) add a recap screen for recurring instances (`AppDestination.OptionalRecurringLists`).
5. [ ] **RF13 – Shopping List History**
   - Navigation always shows “History” (`app/src/main/java/com/comprartir/mobile/core/ui/NavigationItems.kt:12`), but `AppNavHost` only adds the route when `featureFlags.rf13History` is true and still renders an `OptionalFeaturePlaceholder` (`app/src/main/java/com/comprartir/mobile/core/navigation/AppNavHost.kt:269`).
   - Either hide the destination until the feature is implemented or build the actual history screen (API fetch + filters) and enable the feature flag by default.

## Priority 3 – Supporting RF polish
6. [ ] **RF2 – Verify Account: resend flow**
   - `app/src/main/java/com/comprartir/mobile/feature/auth/verify/VerifyViewModel.kt:86` starts a countdown but never calls the backend (`TODO: authRepository.resendVerification`). Wire this to the API so users can request a new code and show feedback/snackbar errors.
7. [ ] **RF11 – Acquire products screen parity**
   - The dedicated acquire route (`app/src/main/java/com/comprartir/mobile/lists/presentation/AcquireProductScreen.kt:14`) is still a stub even though the requirement calls for a checklist-like experience. Mirror the web UX by loading the active list, allowing quick mark-as-purchased interactions, and adding offline-friendly cues.
8. [ ] **RF5 – Profile enhancements**
   - Profile editing works for text fields, but avatar/background actions are still TODOs (`app/src/main/java/com/comprartir/mobile/profile/presentation/ProfileScreen.kt:64`). Plug in an image picker + upload flow (or disable the controls) so profile management feels complete.
9. [ ] **Dashboard activity feed**
   - `HomeViewModel` hardcodes `recentActivity = emptyList()` (`app/src/main/java/com/comprartir/mobile/feature/home/viewmodel/HomeViewModel.kt:33`), so the activity section in `HomeScreen` never shows data. Populate it using either a backend feed or synthesized events from local list mutations to match the spec’s “incorporate class characteristics” guidance for the main screen.

## RNF gaps
10. [ ] **RNF7 – Barcode scanning**
    - `IntegrationPlaceholders.launchBarcodeScanner` (`app/src/main/java/com/comprartir/mobile/core/util/IntegrationPlaceholders.kt:1`) is an empty stub. Add the actual camera permission flow and a barcode scanning component, then expose it via the top bar when `featureFlags.rnf7Barcode` is enabled.
11. [ ] **RNF8 – Voice commands**
    - `IntegrationPlaceholders.startVoiceCommandSession` is also a stub (same file). Decide on a speech provider, request mic permissions, and wire the callback so shopping lists can be dictated hands-free.
12. [ ] **RNF9 – Product photo capture**
    - `IntegrationPlaceholders.captureProductPhoto` is unimplemented. Integrate `ActivityResultContracts.TakePicture` or a camera library, store the image (and metadata) per product, and add UI affordances to view/remove the photos.

## Navigation & UX hygiene (after the RF/RNF work)
13. [ ] **Hide routes for disabled optional features**
    - Because `primaryNavigationItems` always exposes `OptionalHistory` (`app/src/main/java/com/comprartir/mobile/core/ui/NavigationItems.kt:12`) while the nav graph only registers it when feature flags are true (`AppNavHost.kt:269`) and `FeatureFlags.Disabled` is the default (`ComprartirApp.kt:30`), tapping “History” today throws.
    - Either conditionally build the bottom navigation list based on the feature flags, or enable the matching destinations by default.
14. [ ] **Extend personalization controls**
    - The Settings screen hints at more customization (`TODO` at `app/src/main/java/com/comprartir/mobile/shared/settings/SettingsScreen.kt:105`). Once core/optional requirements are done, add accent color + density toggles to better satisfy RNF3’s “allow customization” spirit.
