package com.comprartir.mobile.pantry.presentation

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.core.util.FeatureFlags
import com.comprartir.mobile.pantry.data.PantryItem
import com.comprartir.mobile.pantry.data.PantryRepository
import com.comprartir.mobile.pantry.data.PantrySummary
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.products.data.Product
import com.comprartir.mobile.products.data.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val repository: PantryRepository,
    private val productsRepository: ProductsRepository,
    featureFlags: FeatureFlags,
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _state = MutableStateFlow(
        PantryUiState(
            showManagementFeatures = featureFlags.rf15PantryProducts,
        )
    )
    val state: StateFlow<PantryUiState> = _state.asStateFlow()

    init {
        observePantryData()
        refresh()
    }

    private fun observePantryData() {
        viewModelScope.launch {
            combine(
                repository.observePantries(),
                repository.observePantry(),
            ) { pantries, items ->
                pantries to items
            }.collect { (pantries, items) ->
                _state.update { current ->
                    val selectedId = current.selectedPantryId?.takeIf { id ->
                        pantries.any { it.id == id }
                    } ?: pantries.firstOrNull()?.id
                    val filteredItems = filterItems(items, selectedId)
                    current.copy(
                        pantries = pantries,
                        selectedPantryId = selectedId,
                        allItems = items,
                        items = filteredItems,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onSelectPantry(pantryId: String) {
        _state.update { current ->
            val filtered = filterItems(current.allItems, pantryId)
            current.copy(
                selectedPantryId = pantryId,
                items = filtered,
            )
        }
    }

    fun showPantryDialog(pantryId: String? = null) {
        val pantry = _state.value.pantries.find { it.id == pantryId }
        _state.update {
            it.copy(
                pantryDialog = PantryDialogState(
                    isVisible = true,
                    pantryId = pantry?.id,
                    name = pantry?.name.orEmpty(),
                    description = pantry?.description.orEmpty(),
                ),
            )
        }
    }

    fun dismissPantryDialog() {
        _state.update { it.copy(pantryDialog = PantryDialogState()) }
    }

    fun onPantryNameChanged(value: String) {
        _state.update { it.copy(pantryDialog = it.pantryDialog.copy(name = value, errorMessageRes = null)) }
    }

    fun onPantryDescriptionChanged(value: String) {
        _state.update { it.copy(pantryDialog = it.pantryDialog.copy(description = value)) }
    }

    fun savePantry() {
        val dialog = _state.value.pantryDialog
        val name = dialog.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(pantryDialog = dialog.copy(errorMessageRes = R.string.pantry_error_name_required)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(pantryDialog = dialog.copy(isSubmitting = true, errorMessageRes = null)) }
            runCatching {
                if (dialog.pantryId == null) {
                    repository.createPantry(name, dialog.description.takeIf { it.isNotBlank() })
                } else {
                    repository.updatePantry(dialog.pantryId, name, dialog.description.takeIf { it.isNotBlank() })
                }
                repository.refresh()
            }.onSuccess {
                dismissPantryDialog()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        pantryDialog = dialog.copy(
                            isSubmitting = false,
                            errorMessageRes = R.string.error_generic,
                        ),
                    )
                }
            }
        }
    }

    fun deleteCurrentPantry() {
        val pantryId = _state.value.pantryDialog.pantryId ?: return
        viewModelScope.launch {
            _state.update { it.copy(pantryDialog = it.pantryDialog.copy(isSubmitting = true, errorMessageRes = null)) }
            runCatching {
                repository.deletePantry(pantryId)
                repository.refresh()
            }.onSuccess {
                dismissPantryDialog()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        pantryDialog = it.pantryDialog.copy(
                            isSubmitting = false,
                            errorMessageRes = R.string.error_generic,
                        ),
                    )
                }
            }
        }
    }

    fun showItemDialog(itemId: String? = null) {
        val item = _state.value.allItems.find { it.id == itemId }
        _state.update {
            it.copy(
                itemDialog = PantryItemDialogState(
                    isVisible = true,
                    itemId = item?.id,
                    productId = item?.productId,
                    name = item?.name.orEmpty(),
                    quantity = item?.quantity?.let { qty -> if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString() }.orEmpty(),
                    unit = item?.unit.orEmpty(),
                    expirationDate = item?.expiresAt?.let { instant -> dateFormatter.format(instant.atZone(ZoneId.systemDefault()).toLocalDate()) }.orEmpty(),
                    isEditing = item != null,
                ),
            )
        }
    }

    fun dismissItemDialog() {
        _state.update { it.copy(itemDialog = PantryItemDialogState()) }
    }

    fun onItemNameChanged(value: String) {
        _state.update { it.copy(itemDialog = it.itemDialog.copy(name = value, errorMessageRes = null)) }
    }

    fun onItemQuantityChanged(value: String) {
        _state.update { it.copy(itemDialog = it.itemDialog.copy(quantity = value, errorMessageRes = null)) }
    }

    fun onItemUnitChanged(value: String) {
        _state.update { it.copy(itemDialog = it.itemDialog.copy(unit = value)) }
    }

    fun onItemExpirationChanged(value: String) {
        _state.update { it.copy(itemDialog = it.itemDialog.copy(expirationDate = value, errorMessageRes = null)) }
    }

    fun saveItem() {
        val dialog = _state.value.itemDialog
        val pantryId = _state.value.selectedPantryId
        if (!dialog.isVisible || pantryId == null) return
        val name = dialog.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(itemDialog = dialog.copy(errorMessageRes = R.string.pantry_error_name_required)) }
            return
        }
        val quantity = dialog.quantity.trim().toDoubleOrNull()
        if (quantity == null || quantity <= 0) {
            _state.update { it.copy(itemDialog = dialog.copy(errorMessageRes = R.string.pantry_error_quantity_invalid)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(itemDialog = dialog.copy(isSubmitting = true, errorMessageRes = null)) }
            runCatching {
                val productId = resolveProductId(dialog)
                val expiration = parseExpiration(dialog.expirationDate)
                val item = PantryItem(
                    id = dialog.itemId.orEmpty(),
                    productId = productId,
                    name = name,
                    quantity = quantity,
                    unit = dialog.unit.trim().ifBlank { null },
                    expiresAt = expiration,
                    pantryId = pantryId,
                    categoryId = null,
                    location = null,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                )
                repository.upsertItem(item)
            }.onSuccess {
                dismissItemDialog()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        itemDialog = dialog.copy(
                            isSubmitting = false,
                            errorMessageRes = R.string.error_generic,
                        ),
                    )
                }
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            runCatching { repository.deleteItem(itemId) }
                .onFailure { throwable ->
                    _state.update { it.copy(errorMessage = throwable.message ?: it.errorMessage) }
                }
        }
    }

    fun onShareEmailChanged(value: String) {
        if (!_state.value.showManagementFeatures) return
        _state.update { it.copy(shareState = it.shareState.copy(email = value, errorMessageRes = null)) }
    }

    fun inviteShare() {
        if (!_state.value.showManagementFeatures) return
        val pantryId = _state.value.selectedPantryId ?: return
        val email = _state.value.shareState.email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(shareState = it.shareState.copy(errorMessageRes = R.string.pantry_error_email_invalid)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(shareState = it.shareState.copy(isInviting = true, errorMessageRes = null)) }
            runCatching {
                repository.sharePantry(pantryId, email)
                repository.refresh()
            }.onSuccess {
                _state.update { it.copy(shareState = PantryShareUiState()) }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        shareState = it.shareState.copy(
                            isInviting = false,
                            errorMessageRes = R.string.error_generic,
                        ),
                    )
                }
            }
        }
    }

    fun removeSharedUser(userId: String) {
        if (!_state.value.showManagementFeatures) return
        val pantryId = _state.value.selectedPantryId ?: return
        viewModelScope.launch {
            _state.update { it.copy(shareState = it.shareState.copy(removingUserId = userId)) }
            runCatching {
                repository.revokeShare(pantryId, userId)
                repository.refresh()
            }.onSuccess {
                _state.update { it.copy(shareState = PantryShareUiState()) }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        shareState = it.shareState.copy(
                            removingUserId = null,
                            errorMessageRes = R.string.error_generic,
                        ),
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.refresh() }
                .onFailure { throwable ->
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleFilters() {
        _state.update { it.copy(isFiltersExpanded = !it.isFiltersExpanded) }
    }

    fun onSortOptionChange(option: PantrySortOption) {
        _state.update { it.copy(sortOption = option) }
    }

    fun onSortDirectionChange(direction: SortDirection) {
        _state.update { it.copy(sortDirection = direction) }
    }

    fun onPantryTypeFilterChange(filter: PantryTypeFilter) {
        _state.update { it.copy(pantryTypeFilter = filter) }
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                searchQuery = "",
                sortOption = PantrySortOption.RECENT,
                sortDirection = SortDirection.DESCENDING,
                pantryTypeFilter = PantryTypeFilter.ALL,
            )
        }
    }

    private suspend fun resolveProductId(dialog: PantryItemDialogState): String {
        if (dialog.isEditing && !dialog.productId.isNullOrBlank()) {
            return dialog.productId
        }
        val catalog = productsRepository.observeCatalog().firstOrNull().orEmpty()
        val existing = catalog.find { it.name.equals(dialog.name.trim(), ignoreCase = true) }
        if (existing != null) return existing.id
        val created = productsRepository.upsertProduct(
            Product(
                id = "",
                name = dialog.name.trim(),
                description = null,
                category = null,
                unit = dialog.unit.trim().ifBlank { null },
                defaultQuantity = dialog.quantity.trim().toDoubleOrNull() ?: 1.0,
                isFavorite = false,
            )
        )
        return created.id
    }

    private fun parseExpiration(value: String): Instant? {
        if (value.isBlank()) return null
        return try {
            LocalDate.parse(value, dateFormatter)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun filterItems(items: List<PantryItem>, pantryId: String?): List<PantryItem> =
        pantryId?.let { id ->
            items.filter { it.pantryId == null || it.pantryId == id }
        } ?: items
}

data class PantryUiState(
    val pantries: List<PantrySummary> = emptyList(),
    val selectedPantryId: String? = null,
    val allItems: List<PantryItem> = emptyList(),
    val items: List<PantryItem> = emptyList(),
    val searchQuery: String = "",
    val isFiltersExpanded: Boolean = false,
    val sortOption: PantrySortOption = PantrySortOption.RECENT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val pantryTypeFilter: PantryTypeFilter = PantryTypeFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val pantryDialog: PantryDialogState = PantryDialogState(),
    val itemDialog: PantryItemDialogState = PantryItemDialogState(),
    val shareState: PantryShareUiState = PantryShareUiState(),
    val showManagementFeatures: Boolean = false,
) {
    val selectedPantry: PantrySummary? get() = pantries.firstOrNull { it.id == selectedPantryId }
}

data class PantryDialogState(
    val isVisible: Boolean = false,
    val pantryId: String? = null,
    val name: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)

data class PantryItemDialogState(
    val isVisible: Boolean = false,
    val itemId: String? = null,
    val productId: String? = null,
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val expirationDate: String = "",
    val isSubmitting: Boolean = false,
    val isEditing: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
)

data class PantryShareUiState(
    val email: String = "",
    val isInviting: Boolean = false,
    val removingUserId: String? = null,
    @StringRes val errorMessageRes: Int? = null,
)

enum class PantrySortOption {
    RECENT,
    NAME,
    ITEM_COUNT,
}

enum class PantryTypeFilter {
    ALL,
    SHARED,
    PERSONAL,
}
