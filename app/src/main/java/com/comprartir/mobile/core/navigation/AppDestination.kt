package com.comprartir.mobile.core.navigation

enum class AppDestination(val route: String) {
    Register("auth/register"),
    Verify("auth/verify"),
    UpdatePassword("auth/updatePassword"),
    SignIn("auth/signIn"),
    Dashboard("home/dashboard"),
    Profile("profile/manage"),
    Products("products/manage"),
    Lists("lists/manage"),
    ListDetails("lists/{listId}"),
    ShareList("lists/share"),
    Categorize("products/categorize"),
    AcquireProduct("lists/acquire"),
    Settings("settings"),
    Pantry("pantry/overview"),
    OptionalPasswordRecovery("auth/passwordRecovery"),
    OptionalHistory("lists/history"),
    OptionalRecurringLists("lists/recurring"),
    OptionalPantryManagement("pantry/manage"),
}

data class NavigationIntent(
    val destination: AppDestination,
    val arguments: Map<String, String> = emptyMap(),
)
