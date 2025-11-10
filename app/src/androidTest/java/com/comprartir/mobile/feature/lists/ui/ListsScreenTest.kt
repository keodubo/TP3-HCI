package com.comprartir.mobile.feature.lists.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.comprartir.mobile.feature.lists.model.ListTypeFilter
import com.comprartir.mobile.feature.lists.model.ListsEvent
import com.comprartir.mobile.feature.lists.model.ListsSummaryUi
import com.comprartir.mobile.feature.lists.model.ListsUiState
import com.comprartir.mobile.feature.lists.model.ShoppingListUi
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.feature.lists.model.SortOption
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class ListsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun empty_state_is_displayed() {
        composeRule.setContent {
            ListsScreen(
                state = ListsUiState(
                    lists = emptyList(),
                    summary = ListsSummaryUi(),
                    sortOption = SortOption.RECENT,
                    sortDirection = SortDirection.DESCENDING,
                    listType = ListTypeFilter.ALL,
                    isFiltersExpanded = false,
                    isLoading = false,
                ),
                onEvent = {},
                currentRoute = "lists/manage",
                snackbarHostState = SnackbarHostState(),
                onNavigate = {},
            )
        }

        composeRule.onNodeWithText("Aún no tienes listas").assertIsDisplayed()
    }

    @Test
    fun filter_panel_toggles() {
        composeRule.setContent {
            ListsScreen(
                state = ListsUiState(isFiltersExpanded = false, summary = ListsSummaryUi(), isLoading = false),
                onEvent = {},
                currentRoute = "lists/manage",
                snackbarHostState = SnackbarHostState(),
                onNavigate = {},
            )
        }

        composeRule.onNodeWithText("Mostrar filtros").performClick()
        composeRule.onNodeWithText("Ocultar filtros").assertIsDisplayed()
    }

    @Test
    fun create_list_button_opens_dialog() {
        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    ListsUiState(
                        lists = emptyList(),
                        summary = ListsSummaryUi(),
                        isLoading = false,
                    )
                )
            }
            ListsScreen(
                state = uiState,
                onEvent = { event ->
                    if (event == ListsEvent.CreateList) {
                        uiState = uiState.copy(
                            createListState = uiState.createListState.copy(isVisible = true),
                        )
                    }
                },
                currentRoute = "lists/manage",
                snackbarHostState = SnackbarHostState(),
                onNavigate = {},
            )
        }

        composeRule.onNodeWithText("+ Nueva lista").performClick()
        composeRule.onNodeWithText("Nueva Lista de Compras").assertIsDisplayed()
    }

    @Test
    fun clicking_open_triggers_event() {
        var lastEvent: ListsEvent? = null
        composeRule.setContent {
            ListsScreen(
                state = ListsUiState(
                    lists = listOf(
                        ShoppingListUi(
                            id = "1",
                            name = "Semanal",
                            updatedAgo = "Hace 1h",
                            totalItems = 10,
                            acquiredItems = 5,
                            sharedWith = 1,
                            isShared = false,
                        ),
                    ),
                    summary = ListsSummaryUi(),
                    isLoading = false,
                ),
                onEvent = { lastEvent = it },
                currentRoute = "lists/manage",
                snackbarHostState = SnackbarHostState(),
                onNavigate = {},
            )
        }

        composeRule.onNodeWithText("Abrir").performClick()

        assertEquals(ListsEvent.OpenList("1"), lastEvent)
    }
}
