package com.comprartir.mobile.feature.listdetail.data

import com.comprartir.mobile.lists.data.ShoppingListsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface ListDetailRepository {
    fun observeList(listId: String): Flow<ListDetailData>
    suspend fun toggleItem(listId: String, itemId: String, completed: Boolean)
    suspend fun deleteItem(listId: String, itemId: String): ListDetailItem?
    suspend fun restoreItem(listId: String, item: ListDetailItem)
    suspend fun addItem(listId: String, name: String, quantity: String, unit: String?): ListDetailItem
}

/**
 * Minimal fake implementation kept for unit tests. Not used in production DI.
 */
class FakeListDetailRepository() : ListDetailRepository {
    private val flow = kotlinx.coroutines.flow.MutableStateFlow(
        ListDetailData(
            id = "demo",
            title = "Demo list",
            subtitle = "Actualizada hace 2 h",
            shareLink = "",
            items = listOf(
                ListDetailItem(id = "i1", productId = "p1", name = "Leche descremada", quantity = "1", unit = "L", notes = null, isCompleted = false),
                ListDetailItem(id = "i2", productId = "p2", name = "Pan integral", quantity = "2", unit = null, notes = null, isCompleted = true),
            ),
        )
    )

    override fun observeList(listId: String): Flow<ListDetailData> = flow

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

    override suspend fun addItem(listId: String, name: String, quantity: String, unit: String?): ListDetailItem {
        val newItem = ListDetailItem(id = java.util.UUID.randomUUID().toString(), productId = java.util.UUID.randomUUID().toString(), name = name, quantity = quantity.ifBlank { "1" }, unit = unit, notes = null, isCompleted = false)
        flow.update { current -> current.copy(items = current.items + newItem) }
        return newItem
    }
}

data class ListDetailData(
    val id: String,
    val title: String,
    val subtitle: String,
    val shareLink: String,
    val items: List<ListDetailItem>,
)

data class ListDetailItem(
    val id: String,
    val productId: String,
    val name: String,
    val quantity: String,
    val unit: String?,
    val notes: String?,
    val isCompleted: Boolean,
)

@Singleton
class DefaultListDetailRepository @Inject constructor(
    private val shoppingListsRepository: ShoppingListsRepository,
    private val productsRepository: com.comprartir.mobile.products.data.ProductsRepository,
) : ListDetailRepository {

    private fun formatRelativeTime(timestamp: java.time.Instant, now: java.time.Instant): String {
        val duration = java.time.Duration.between(timestamp, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}m"
            hours == 1L -> "Hace 1 hora"
            hours < 24 -> "Hace ${hours} horas"
            days == 1L -> "Ayer"
            days < 7 -> "Hace ${days} días"
            days < 14 -> "Hace 1 semana"
            days < 30 -> "Hace ${days / 7} semanas"
            days < 60 -> "Hace 1 mes"
            else -> "Hace ${days / 30} meses"
        }
    }

    override fun observeList(listId: String): Flow<ListDetailData> =
        shoppingListsRepository.observeList(listId).map { shoppingList ->
            if (shoppingList == null) {
                ListDetailData(id = listId, title = "", subtitle = "", shareLink = "", items = emptyList())
            } else {
                val now = java.time.Instant.now()
                val subtitle = "Actualizada ${formatRelativeTime(shoppingList.updatedAt, now).lowercase()}"
                val items = shoppingList.items.map { item ->
                    ListDetailItem(
                        id = item.id,
                        productId = item.productId,
                        name = item.name,
                        quantity = item.quantity.toString(),
                        unit = item.unit,
                        notes = item.notes,
                        isCompleted = item.isAcquired,
                    )
                }
                ListDetailData(
                    id = shoppingList.id,
                    title = shoppingList.name,
                    subtitle = subtitle,
                    shareLink = "",
                    items = items,
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
            )
        }
        shoppingListsRepository.deleteItem(listId, itemId)
        return removed
    }

    override suspend fun restoreItem(listId: String, item: ListDetailItem) {
        val quantityDouble = item.quantity.toDoubleOrNull() ?: 1.0
        shoppingListsRepository.addItem(listId = listId, productId = item.productId, quantity = quantityDouble, unit = item.unit)
    }

    override suspend fun addItem(listId: String, name: String, quantity: String, unit: String?): ListDetailItem {
        val qty = quantity.toDoubleOrNull() ?: 1.0

        // Try to find product by name in catalog
        val catalog = productsRepository.observeCatalog().firstOrNull().orEmpty()
        var product = catalog.find { it.name.equals(name, ignoreCase = true) }
        if (product == null) {
            // Create a minimal product and upsert it
            val newProduct = com.comprartir.mobile.products.data.Product(
                id = "",
                name = name,
                description = null,
                category = null,
                unit = unit,
                defaultQuantity = qty,
                isFavorite = false,
            )
            productsRepository.upsertProduct(newProduct)
            // refresh catalog snapshot
            product = productsRepository.observeCatalog().firstOrNull().orEmpty().find { it.name.equals(name, ignoreCase = true) }
        }

        val productId = product?.id ?: name

        shoppingListsRepository.addItem(listId = listId, productId = productId, quantity = qty, unit = unit)

        // Try to fetch latest list and return the added item if possible
        val updated = shoppingListsRepository.observeList(listId).firstOrNull()
        val added = updated?.items?.find { it.productId == productId && it.quantity == qty }

        return if (added != null) {
            ListDetailItem(
                id = added.id,
                productId = added.productId,
                name = added.name,
                quantity = added.quantity.toString(),
                unit = added.unit,
                notes = added.notes,
                isCompleted = added.isAcquired,
            )
        } else {
            // Fallback: return a constructed item
            ListDetailItem(
                id = java.util.UUID.randomUUID().toString(),
                productId = productId,
                name = name,
                quantity = qty.toString(),
                unit = unit,
                notes = null,
                isCompleted = false,
            )
        }
    }
}

