# Practical Project: Shopping List Manager
## Third Delivery: Mobile Interface Implementation

## Deliverables


### Mobile Application

A compressed file with ZIP or RAR extension must be submitted containing all necessary files to compile the application, along with the APK file needed to install it on an Android device without needing to use Android Studio.

**IMPORTANT:** The contents of the build folder present in the project should be omitted when generating the compressed file, as well as any other files that may have been generated for testing, frameworks that were ultimately not used, etc.

---

## Installation Instructions

The installation instructions must contain at minimum:

1. **Type of device (physical or emulator) and API Level versions used during testing.**
2. **Sequence of steps necessary to install and configure the application on a physical Android device.**

**NOTE:** The installation instructions do not necessarily need to be submitted separately and may be part of one of the report sections.

---

## Implementation

The mobile application implementation must comply with the following functional requirements:

- **RF1.** Register Account.
The system must allow a new user to create an account by entering their basic information and validating required fields.
- **RF2.** Verify Account.
The system must allow a user to verify their account (e.g., via a verification code) and only enable full access after successful verification.
- **RF3.** Modify Password.
The system must allow a logged-in user to change their password, enforcing basic security rules and validating the new password.
- **RF4.** Log In and Log Out.
The system must allow users to log in with valid credentials and log out, clearing session data and returning to the access screen.
- **RF5.** Manage Profile.
The system must allow users to view and edit their profile data (e.g., name, email, preferences) in a centralized flow.
- **RF6.** Manage Products.
The system must allow users to create, edit, delete, and search products in a global catalog that can be reused across lists.
- **RF7.** Manage Shopping Lists.
The system must allow users to create, edit, and delete shopping lists, updating summaries and counters accordingly.
- **RF8.** Manage Products in Shopping Lists.
The system must allow users to add, edit, reorder, and remove products inside a specific shopping list.
- **RF9.** Share Shopping Lists.
The system must allow users to share a shopping list with other people (e.g., via invitations or share links) and manage collaborators.
- **RF10.** Categorize Products.
The system must allow users to assign categories to products and use those categories for filtering, browsing, and organizing lists.
- **RF11.** Mark Products as Purchased.
The system must allow users to mark products in a list as “acquired/purchased”, updating the visual state and progress indicators.

Optionally, the following functional requirements may be implemented:

- **RF12.** Recover Password.
The system must allow users who forgot their password to request a password reset via email and complete the recovery process.
- **RF13.** Consult Shopping List History.
The system must allow users to consult a history of completed shopping lists, with options to view details and/or reuse them.
- **RF14.** Mark Shopping Lists as Recurring.
The system must allow users to mark lists as recurrent (e.g., weekly shopping), prioritizing them in reuse flows and summaries.
- **RF15.** Manage Products in Pantry.
The system must allow users to manage pantry inventories (create pantries, add/edit/remove items with quantities and units) and use this information to complement shopping lists.

It must also mandatorily comply with the following non-functional requirements:

- **RNF1.** Use the language set in the device's regional configuration and be localized at least to Spanish and English languages.
- **RNF2.** Use the application bar to display a contextual title and/or shortcuts that allow interaction with contextual information displayed on screen.
- **RNF3.** Allow customization of aspects related to application functionality.
- **RNF4.** Provide a different user experience depending on device form factor (phones and tablets).
- **RNF5.** Provide a different user experience depending on device orientation (vertical and horizontal).
- **RNF6.** Function on Android 10.0 - API Level 29 (or higher) devices.

Optionally, it may also comply with the following non-functional requirements:

- **RNF7.** Scan Product Codes.
- **RNF8.** Interact Using Voice Commands.
- **RNF9.** Take Photographs of Products.


# Recommendations

## Recommendations for Mobile Application Screens

❖ Avoid textual problems.
❖ Perform adequate error handling (returned by the API, connection failures, etc.).
❖ Consider navigation elements (App Bar, Up Button, Task Backstack, Navigation Drawer, Bottom Navigation, etc.).
❖ Incorporate the characteristics covered in class into the main screen.
❖ Validate forms early.

---

## Recommendations for Mobile Application Screens (cont.)

❖ Use graphic interface components that are part of the UI Toolkit appropriately.
❖ Make all offered functionalities explicit.
❖ Convey the feeling of always being in control (response times and stability).
❖ Allow customization of certain aspects of the application.
❖ Guarantee simplicity and ease of use.
❖ Be consistent with users' mental model.

---

## Recommendations for Mobile Application Screens (cont.)

❖ Achieve visual coherence (colors, typography, images, sizes, etc.).
❖ Implement mandatory functional and non-functional requirements.
❖ Allow easy modification of the API connection URL (IP and port).

---