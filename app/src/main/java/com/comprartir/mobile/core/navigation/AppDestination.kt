package com.comprartir.mobile.core.navigation

enum class AppDestination(val route: String) {
    Register("auth/register"),
    Verify("auth/verify"),
    UpdatePassword("auth/updatePassword"),
    ForgotPassword("auth/forgotPassword"),
    ResetPassword("auth/resetPassword"),
    SignIn("auth/signIn"),
    Dashboard("home/dashboard"),
    Profile("profile/manage"),
    ChangePassword("profile/changePassword"),
    Products("products/manage"),
    Categories("categories/manage"),
    Lists("lists/manage"),
    ListDetails("lists/{listId}"),
    ShareList("lists/share"),
    Categorize("products/categorize"),
    AcquireProduct("lists/acquire"),
    Settings("settings"),
    Pantry("pantry/overview"),
    PantryDetail("pantry/{pantryId}"),
    OptionalPasswordRecovery("auth/passwordRecovery"),
    OptionalHistory("lists/history"),
    OptionalRecurringLists("lists/recurring"),
    OptionalPantryManagement("pantry/manage"),
}

data class NavigationIntent(
    val destination: AppDestination,
    val arguments: Map<String, String> = emptyMap(),
)
