package com.comprartir.mobile.feature.listdetail.navigation

object ListDetailDestination {
    const val listIdArg = "listId"
    const val route = "lists/{listId}"

    fun createRoute(listId: String): String = "lists/$listId"
}

