package com.comprartir.mobile.products.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.products.data.Product
import com.comprartir.mobile.products.data.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsUiState())
    val state: StateFlow<ProductsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCatalog().collectLatest { products ->
                _state.update {
                    it.copy(
                        products = products,
                        filteredProducts = filterProducts(products, it.searchQuery),
                    )
                }
            }
        }
        refresh()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                filteredProducts = filterProducts(it.products, query),
            )
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

    private fun filterProducts(products: List<Product>, query: String): List<Product> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return products
        return products.filter { product ->
            product.name.lowercase().contains(normalized)
                    || (product.description?.lowercase()?.contains(normalized) == true)
                    || (product.category?.name?.lowercase()?.contains(normalized) == true)
        }
    }
}

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
