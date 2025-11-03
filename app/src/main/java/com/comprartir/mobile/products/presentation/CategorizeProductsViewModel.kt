package com.comprartir.mobile.products.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.products.data.Category
import com.comprartir.mobile.products.data.Product
import com.comprartir.mobile.products.data.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class CategorizeProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CategorizeProductsUiState())
    val state: StateFlow<CategorizeProductsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCatalog().collectLatest { products ->
                _state.value = _state.value.copy(products = products)
            }
        }
        viewModelScope.launch {
            repository.observeCategories().collectLatest { categories ->
                _state.value = _state.value.copy(categories = categories)
            }
        }
    }

    fun assignCategory(productId: String, categoryId: String) {
        viewModelScope.launch {
            repository.assignCategory(productId, categoryId)
        }
    }
}

data class CategorizeProductsUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
)
