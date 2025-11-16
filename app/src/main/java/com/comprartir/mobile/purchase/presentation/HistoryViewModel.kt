package com.comprartir.mobile.purchase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.purchase.data.Purchase
import com.comprartir.mobile.purchase.data.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: PurchaseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseHistoryUiState(isLoading = true))
    val uiState: StateFlow<PurchaseHistoryUiState> = _uiState.asStateFlow()

    init {
        observePurchases()
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

    fun refresh() {
        viewModelScope.launch {
            val hasContent = _uiState.value.sections.isNotEmpty()
            if (hasContent) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            runCatching { repository.refresh() }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            errorMessage = throwable.message ?: DEFAULT_ERROR_MESSAGE,
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
                                restoredAt = purchase.restoredAt,
                                totalItems = purchase.totalItems,
                                acquiredItems = purchase.acquiredItems,
                                isRecurring = purchase.isRecurring,
                            )
                        },
                )
            }
            .sortedByDescending { it.date }
    }

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "No se pudo cargar el historial"
    }
}

data class PurchaseHistoryUiState(
    val sections: List<PurchaseHistorySection> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
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
    val restoredAt: Instant?,
    val totalItems: Int,
    val acquiredItems: Int,
    val isRecurring: Boolean,
)
