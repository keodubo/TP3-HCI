package com.comprartir.mobile.purchase.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import com.comprartir.mobile.pantry.data.PantrySummary
import com.comprartir.mobile.pantry.data.PantryRepository
import com.comprartir.mobile.purchase.data.Purchase
import com.comprartir.mobile.purchase.data.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: PurchaseRepository,
    private val shoppingListsRepository: ShoppingListsRepository,
    private val pantryRepository: PantryRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryUiState(isLoading = true))
    val uiState: StateFlow<PurchaseHistoryUiState> = _uiState.asStateFlow()

    init {
        observePurchases()
        observeCompletedLists()
        observePantries()
        refresh()
    }

    private fun observePurchases() {
        viewModelScope.launch {
            repository.purchases.collectLatest { purchases ->
                _uiState.update { current ->
                    current.copy(sections = purchases.toSections())
                }
            }
        }
    }

    private fun observeCompletedLists() {
        viewModelScope.launch {
            shoppingListsRepository.observeLists().collectLatest { lists ->
                val completed = lists
                    .filter { it.items.isNotEmpty() && it.items.all { item -> item.isAcquired } }
                    .map { it.toCompletedHistoryItem() }
                    .sortedByDescending { it.completedAt }
                _uiState.update { current ->
                    current.copy(completedLists = completed)
                }
            }
        }
    }

    private fun observePantries() {
        viewModelScope.launch {
            pantryRepository.observePantries().collectLatest { pantries ->
                _uiState.update { current ->
                    current.copy(pantries = pantries)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val hasContent = _uiState.value.sections.isNotEmpty() || _uiState.value.completedLists.isNotEmpty()
            if (hasContent) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            runCatching { 
                repository.refresh()
                shoppingListsRepository.refresh()
            }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        val fallback = context.getString(R.string.history_error_generic)
                        current.copy(
                            errorMessage = throwable.message ?: fallback,
                        )
                    }
                }
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    isRefreshing = false,
                )
            }
        }
    }

    fun retry() {
        refresh()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun restoreList(listId: String) {
        viewModelScope.launch {
            runCatching {
                shoppingListsRepository.restoreList(listId)
                shoppingListsRepository.refresh()
            }.onSuccess {
                _uiState.update { it.copy(snackbarMessage = R.string.history_list_restored) }
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        errorMessage = throwable.message ?: context.getString(R.string.history_error_generic),
                    )
                }
            }
        }
    }

    fun onSnackbarConsumed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showAddToPantryDialog(listId: String) {
        viewModelScope.launch {
            // Get the list details
            val list = shoppingListsRepository.observeList(listId).firstOrNull()
            if (list != null) {
                _uiState.update { 
                    it.copy(
                        showAddToPantryDialog = true,
                        selectedListForPantry = list
                    )
                }
            }
        }
    }

    fun dismissAddToPantryDialog() {
        _uiState.update { 
            it.copy(
                showAddToPantryDialog = false,
                selectedListForPantry = null
            )
        }
    }

    fun addListToPantry(pantryId: String) {
        val list = _uiState.value.selectedListForPantry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToPantry = true) }
            runCatching {
                pantryRepository.addItemsFromList(pantryId, list.items)
            }.onSuccess {
                dismissAddToPantryDialog()
                _uiState.update { 
                    it.copy(
                        isAddingToPantry = false,
                        snackbarMessage = R.string.history_add_to_pantry_success
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { 
                    it.copy(
                        isAddingToPantry = false,
                        errorMessage = throwable.message ?: context.getString(R.string.history_error_generic)
                    )
                }
            }
        }
    }

    private fun List<Purchase>.toSections(): List<PurchaseHistorySection> {
        if (isEmpty()) return emptyList()
        val zone = ZoneId.systemDefault()
        return this
            .sortedByDescending { it.purchasedAt }
            .groupBy { purchase -> purchase.purchasedAt.atZone(zone).toLocalDate() }
            .map { (date, dayPurchases) ->
                PurchaseHistorySection(
                    date = date,
                    purchases = dayPurchases
                        .sortedByDescending { it.purchasedAt }
                        .map { purchase ->
                            PurchaseHistoryItem(
                                id = purchase.id,
                                listId = purchase.listId,
                                listName = purchase.listName,
                                purchasedAt = purchase.purchasedAt,
                                totalItems = purchase.totalItems,
                                acquiredItems = purchase.acquiredItems,
                                isRecurring = purchase.isRecurring,
                            )
                        },
                )
            }
            .sortedByDescending { it.date }
    }

}

data class PurchaseHistoryUiState(
    val sections: List<PurchaseHistorySection> = emptyList(),
    val completedLists: List<CompletedListHistoryItem> = emptyList(),
    val pantries: List<PantrySummary> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    @StringRes val snackbarMessage: Int? = null,
    val showAddToPantryDialog: Boolean = false,
    val selectedListForPantry: ShoppingList? = null,
    val isAddingToPantry: Boolean = false,
)

data class PurchaseHistorySection(
    val date: LocalDate,
    val purchases: List<PurchaseHistoryItem>,
)

data class PurchaseHistoryItem(
    val id: String,
    val listId: String,
    val listName: String?,
    val purchasedAt: Instant,
    val totalItems: Int,
    val acquiredItems: Int,
    val isRecurring: Boolean,
)

data class CompletedListHistoryItem(
    val id: String,
    val name: String,
    val completedAt: Instant?,
    val totalItems: Int,
)

private fun ShoppingList.toCompletedHistoryItem(): CompletedListHistoryItem =
    CompletedListHistoryItem(
        id = id,
        name = name.ifBlank { id },
        completedAt = lastPurchasedAt ?: updatedAt,
        totalItems = items.size,
    )
