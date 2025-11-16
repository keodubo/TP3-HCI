package com.comprartir.mobile.feature.listdetail.data

import com.comprartir.mobile.lists.data.ItemAlreadyExistsException
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import com.comprartir.mobile.products.data.Category
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface ListDetailRepository {
    fun observeList(listId: String): Flow<ListDetailData>
    fun observeCategories(): Flow<List<com.comprartir.mobile.products.data.Category>>
    suspend fun createCategory(name: String): com.comprartir.mobile.products.data.Category
    suspend fun toggleItem(listId: String, itemId: String, completed: Boolean)
    suspend fun deleteItem(listId: String, itemId: String): ListDetailItem?
    suspend fun restoreItem(listId: String, item: ListDetailItem)
    suspend fun addItem(
        listId: String,
        name: String,
        quantity: String,
        unit: String?,
        categoryId: String?,
        overrideCategory: Boolean,
    ): AddItemResult
    suspend fun updateItem(
        listId: String,
        itemId: String,
        name: String,
        quantity: String,
        unit: String?,
        categoryId: String?,
        overrideCategory: Boolean,
    ): ListDetailItem
}

/**
 * Minimal fake implementation kept for unit tests. Not used in production DI.
 */
class FakeListDetailRepository() : ListDetailRepository {
    private val flow = kotlinx.coroutines.flow.MutableStateFlow(
        ListDetailData(
            id = "demo",
            title = "Demo list",
            subtitle = "",
            shareLink = "",
            items = listOf(
                ListDetailItem(id = "i1", productId = "p1", name = "Leche descremada", quantity = "1", unit = "L", notes = null, isCompleted = false, categoryId = null),
                ListDetailItem(id = "i2", productId = "p2", name = "Pan integral", quantity = "2", unit = null, notes = null, isCompleted = true, categoryId = null),
            ),
            updatedAt = Instant.now(),
        )
    )

    override fun observeList(listId: String): Flow<ListDetailData> = flow

    override fun observeCategories(): Flow<List<com.comprartir.mobile.products.data.Category>> =
        flowOf(emptyList())

    override suspend fun createCategory(name: String): com.comprartir.mobile.products.data.Category {
        return com.comprartir.mobile.products.data.Category(
            id = "fake",
            name = name,
            description = null,
            color = null,
        )
    }

    override suspend fun toggleItem(listId: String, itemId: String, completed: Boolean) {
        flow.update { current ->
            current.copy(items = current.items.map { if (it.id == itemId) it.copy(isCompleted = completed) else it })
        }
    }

    override suspend fun deleteItem(listId: String, itemId: String): ListDetailItem? {
        var removed: ListDetailItem? = null
        flow.update { current ->
            val remaining = current.items.filterNot {
                val should = it.id == itemId
                if (should) removed = it
                should
            }
            current.copy(items = remaining)
        }
        return removed
    }

    override suspend fun restoreItem(listId: String, item: ListDetailItem) {
        flow.update { current -> current.copy(items = listOf(item) + current.items) }
    }

    override suspend fun addItem(
        listId: String,
        name: String,
        quantity: String,
        unit: String?,
        categoryId: String?,
        overrideCategory: Boolean,
    ): AddItemResult {
        val newItem = ListDetailItem(
            id = java.util.UUID.randomUUID().toString(),
            productId = java.util.UUID.randomUUID().toString(),
            name = name,
            quantity = quantity.ifBlank { "1" },
            unit = unit,
            notes = null,
            isCompleted = false,
            categoryId = categoryId,
        )
        flow.update { current -> current.copy(items = current.items + newItem) }
        return AddItemResult.Added(newItem)
    }

    override suspend fun updateItem(
        listId: String,
        itemId: String,
        name: String,
        quantity: String,
        unit: String?,
        categoryId: String?,
        overrideCategory: Boolean,
    ): ListDetailItem {
        var updated: ListDetailItem? = null
        flow.update { current ->
            current.copy(items = current.items.map { item ->
                if (item.id == itemId) {
                    item.copy(name = name, quantity = quantity, unit = unit, categoryId = categoryId).also { updated = it }
                } else {
                    item
                }
            })
        }
        return updated ?: throw IllegalStateException("Item not found")
    }
}

data class ListDetailData(
    val id: String,
    val title: String,
    val subtitle: String,
    val shareLink: String,
    val items: List<ListDetailItem>,
    val updatedAt: Instant? = null,
)

data class ListDetailItem(
    val id: String,
    val productId: String,
    val name: String,
    val quantity: String,
    val unit: String?,
    val notes: String?,
    val isCompleted: Boolean,
    val categoryId: String?,
)

sealed interface AddItemResult {
    data class Added(val item: ListDetailItem) : AddItemResult
    data class AlreadyExists(val item: ListDetailItem) : AddItemResult
}

private fun com.comprartir.mobile.lists.data.ListItem.toDetailItem(): ListDetailItem = ListDetailItem(
    id = id,
    productId = productId,
    name = name,
    quantity = quantity.toString(),
    unit = unit,
    notes = notes,
    isCompleted = isAcquired,
    categoryId = categoryId,
)

private fun categoryFromId(categoryId: String?): Category? =
    categoryId?.takeIf { it.isNotBlank() }?.let { Category(id = it, name = "", description = null, color = null) }

@Singleton
class DefaultListDetailRepository @Inject constructor(
    private val shoppingListsRepository: ShoppingListsRepository,
    private val productsRepository: com.comprartir.mobile.products.data.ProductsRepository,
) : ListDetailRepository {

    override fun observeCategories(): Flow<List<com.comprartir.mobile.products.data.Category>> =
        productsRepository.observeCategories()

    override suspend fun createCategory(name: String): com.comprartir.mobile.products.data.Category =
        productsRepository.createCategory(name)

    override fun observeList(listId: String): Flow<ListDetailData> =
        shoppingListsRepository.observeList(listId).map { shoppingList ->
            if (shoppingList == null) {
                ListDetailData(id = listId, title = "", subtitle = "", shareLink = "", items = emptyList())
            } else {
                val items = shoppingList.items.map { it.toDetailItem() }
                ListDetailData(
                    id = shoppingList.id,
                    title = shoppingList.name,
                    subtitle = "",
                    shareLink = shoppingList.shareLink,
                    items = items,
                    updatedAt = shoppingList.updatedAt,
                )
            }
        }

    override suspend fun toggleItem(listId: String, itemId: String, completed: Boolean) {
        shoppingListsRepository.toggleAcquired(listId, itemId, completed)
    }

    override suspend fun deleteItem(listId: String, itemId: String): ListDetailItem? {
        val current = shoppingListsRepository.observeList(listId).firstOrNull()
        val removedDomain = current?.items?.find { it.id == itemId }
        val removed = removedDomain?.let {
            ListDetailItem(
                id = it.id,
                productId = it.productId,
                name = it.name,
                quantity = it.quantity.toString(),
                unit = it.unit,
                notes = it.notes,
                isCompleted = it.isAcquired,
                categoryId = it.categoryId,
            )
        }
        shoppingListsRepository.deleteItem(listId, itemId)
        return removed
    }

    override suspend fun restoreItem(listId: String, item: ListDetailItem) {
        val quantityDouble = item.quantity.toDoubleOrNull() ?: 1.0
        shoppingListsRepository.addItem(listId = listId, productId = item.productId, quantity = quantityDouble, unit = item.unit)
    }

    override suspend fun addItem(
        listId: String,
        name: String,
        quantity: String,
        unit: String?,
        categoryId: String?,
        overrideCategory: Boolean,
    ): AddItemResult {
        val qty = quantity.toDoubleOrNull() ?: 1.0

        android.util.Log.d("ListDetailRepository", "addItem: START - name=$name, qty=$qty, unit=$unit")

        // Try to find existing product in catalog by name
        val catalog = productsRepository.observeCatalog().firstOrNull().orEmpty()
        val existingProduct = catalog.find { it.name.equals(name, ignoreCase = true) }

        val selectedCategory = categoryFromId(categoryId)
        val product = if (existingProduct != null) {
            android.util.Log.d("ListDetailRepository", "addItem: Using existing product: id=${existingProduct.id}")
            val shouldUpdateCategory = overrideCategory && existingProduct.category?.id != selectedCategory?.id
            if (shouldUpdateCategory) {
                productsRepository.upsertProduct(existingProduct.copy(category = selectedCategory))
            } else {
                existingProduct
            }
        } else {
            // Create product in catalog - upsertProduct now returns Product with valid ID
            android.util.Log.d("ListDetailRepository", "addItem: Creating new product...")
            val newProduct = com.comprartir.mobile.products.data.Product(
                id = "",
                name = name,
                description = null,
                category = selectedCategory,
                unit = unit,
                defaultQuantity = qty,
                isFavorite = false,
            )
            val createdProduct = productsRepository.upsertProduct(newProduct)
            android.util.Log.d("ListDetailRepository", "addItem: Product created/found with id=${createdProduct.id}")
            createdProduct
        }
        
        // Validate product ID is numeric
        if (product.id.isBlank() || product.id.toIntOrNull() == null) {
            android.util.Log.e("ListDetailRepository", "addItem: INVALID product ID: '${product.id}'")
            throw IllegalStateException("Product ID must be a valid number, got: '${product.id}'")
        }

        android.util.Log.d("ListDetailRepository", "addItem: Adding item to list with productId=${product.id}")

        val fallbackItem = ListDetailItem(
            id = "",
            productId = product.id,
            name = name,
            quantity = qty.toString(),
            unit = unit,
            notes = null,
            isCompleted = false,
            categoryId = product.category?.id,
        )

        return try {
            // Add item to shopping list (this is the key operation)
            shoppingListsRepository.addItem(listId = listId, productId = product.id, quantity = qty, unit = unit)

            android.util.Log.d("ListDetailRepository", "addItem: Item added successfully, refreshing list items")
            shoppingListsRepository.refreshListItems(listId)

            val added = shoppingListsRepository.observeList(listId).firstOrNull()
                ?.items
                ?.find { it.productId == product.id && it.quantity == qty }
            val detail = added?.toDetailItem() ?: fallbackItem
            AddItemResult.Added(detail)
        } catch (duplicate: ItemAlreadyExistsException) {
            android.util.Log.w("ListDetailRepository", "addItem: Product already exists in list", duplicate)
            shoppingListsRepository.refreshListItems(listId)
            val existing = shoppingListsRepository.observeList(listId).firstOrNull()
                ?.items
                ?.find { it.productId == product.id }
            val detail = existing?.toDetailItem() ?: fallbackItem
            AddItemResult.AlreadyExists(detail)
        }
    }

    override suspend fun updateItem(
        listId: String,
        itemId: String,
        name: String,
        quantity: String,
        unit: String?,
        categoryId: String?,
        overrideCategory: Boolean,
    ): ListDetailItem {
        val qty = quantity.toDoubleOrNull() ?: 1.0

        val currentList = shoppingListsRepository.observeList(listId).firstOrNull()
        val currentItem = currentList?.items?.find { it.id == itemId }
        val catalog = productsRepository.observeCatalog().firstOrNull().orEmpty()
        val selectedCategory = categoryFromId(categoryId)

        val product = if (currentItem != null && currentItem.name == name) {
            val baseProduct = com.comprartir.mobile.products.data.Product(
                id = currentItem.productId,
                name = currentItem.name,
                description = null,
                category = currentItem.categoryId?.let { categoryFromId(it) },
                unit = currentItem.unit,
                defaultQuantity = currentItem.quantity,
                isFavorite = false,
            )
            if (overrideCategory) {
                productsRepository.upsertProduct(baseProduct.copy(category = selectedCategory))
            } else {
                baseProduct
            }
        } else {
            val existingProduct = catalog.find { it.name.equals(name, ignoreCase = true) }
            when {
                existingProduct != null -> {
                    val needsCategoryUpdate = overrideCategory && existingProduct.category?.id != selectedCategory?.id
                    if (needsCategoryUpdate) {
                        productsRepository.upsertProduct(existingProduct.copy(category = selectedCategory))
                    } else {
                        existingProduct
                    }
                }
                else -> {
                    val newProduct = com.comprartir.mobile.products.data.Product(
                        id = "",
                        name = name,
                        description = null,
                        category = if (overrideCategory) selectedCategory else null,
                        unit = unit,
                        defaultQuantity = qty,
                        isFavorite = false,
                    )
                    productsRepository.upsertProduct(newProduct)
                }
            }
        }

        shoppingListsRepository.updateItem(listId = listId, itemId = itemId, productId = product.id, quantity = qty, unit = unit)
        shoppingListsRepository.refreshListItems(listId)
        
        val updated = shoppingListsRepository.observeList(listId).firstOrNull()
        val updatedItem = updated?.items?.find { it.id == itemId }

        return if (updatedItem != null) {
            ListDetailItem(
                id = updatedItem.id,
                productId = updatedItem.productId,
                name = updatedItem.name,
                quantity = updatedItem.quantity.toString(),
                unit = updatedItem.unit,
                notes = updatedItem.notes,
                isCompleted = updatedItem.isAcquired,
                categoryId = updatedItem.categoryId,
            )
        } else {
            ListDetailItem(
                id = itemId,
                productId = product.id,
                name = name,
                quantity = qty.toString(),
                unit = unit,
                notes = currentItem?.notes,
                isCompleted = currentItem?.isAcquired ?: false,
                categoryId = product.category?.id ?: currentItem?.categoryId,
            )
        }
    }
}
