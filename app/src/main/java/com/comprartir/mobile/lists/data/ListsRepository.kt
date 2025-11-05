package com.comprartir.mobile.lists.data

import android.util.Log
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.ListItemDao
import com.comprartir.mobile.core.database.dao.ShoppingListDao
import com.comprartir.mobile.core.database.entity.ListItemEntity
import com.comprartir.mobile.core.database.entity.ShoppingListEntity
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.MoveListToPantryRequest
import com.comprartir.mobile.core.network.ShareListRequest
import com.comprartir.mobile.core.network.ShoppingListItemPatchRequest
import com.comprartir.mobile.core.network.ShoppingListItemUpsertRequest
import com.comprartir.mobile.core.network.ShoppingListPurchaseRequest
import com.comprartir.mobile.core.network.ShoppingListUpsertRequest
import com.comprartir.mobile.core.network.fetchAllPages
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

data class ShoppingList(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val sharedWith: List<String>,
    val isRecurring: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastPurchasedAt: Instant?,
    val items: List<ListItem>,
)

data class ListItem(
    val id: String,
    val productId: String,
    val name: String,
    val quantity: Double,
    val unit: String?,
    val isAcquired: Boolean,
    val categoryId: String?,
    val pantryId: String?,
    val notes: String?,
    val addedAt: Instant,
    val updatedAt: Instant,
)

interface ShoppingListsRepository {
    fun observeLists(): Flow<List<ShoppingList>>
    fun observeList(listId: String): Flow<ShoppingList?>
    suspend fun refresh()
    suspend fun createList(name: String, description: String? = null)
    suspend fun updateList(listId: String, name: String, description: String?)
    suspend fun deleteList(listId: String)
    suspend fun toggleAcquired(listId: String, itemId: String, isAcquired: Boolean)
    suspend fun addItem(listId: String, productId: String, quantity: Double, unit: String? = null)
    suspend fun deleteItem(listId: String, itemId: String)
    suspend fun purchaseList(listId: String)
    suspend fun resetList(listId: String)
    suspend fun moveListToPantry(listId: String, pantryId: String)
    suspend fun shareList(listId: String, email: String)
    suspend fun getSharedUsers(listId: String)
    suspend fun revokeShare(listId: String, userId: String)
}

@Singleton
class DefaultShoppingListsRepository @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val listItemDao: ListItemDao,
    private val api: ComprartirApi,
) : ShoppingListsRepository {

    private val hasSyncedLists = AtomicBoolean(false)

    override fun observeLists(): Flow<List<ShoppingList>> = combine(
        shoppingListDao.observeLists(),
        listItemDao.observeAllItems(),
    ) { listEntities, itemEntities ->
        val groupedItems = itemEntities.groupBy { it.listId }
        listEntities.map { entity -> entity.toDomainModel(groupedItems[entity.id].orEmpty()) }
    }.onStart { ensureSynced() }

    override fun observeList(listId: String): Flow<ShoppingList?> = combine(
        shoppingListDao.observeList(listId),
        listItemDao.observeItems(listId),
    ) { listEntity, items ->
        listEntity?.toDomainModel(items)
    }.onStart { ensureSynced() }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) { refreshListsInternal() }
    }

    override suspend fun createList(name: String, description: String?) {
        withContext(Dispatchers.IO) {
            val response = api.createShoppingList(
                ShoppingListUpsertRequest(
                    name = name,
                    description = description,
                )
            )
            shoppingListDao.upsert(response.toEntity())
            listItemDao.deleteByList(response.id)
            if (response.items.isNotEmpty()) {
                listItemDao.upsertAll(response.items.map { it.toEntity(response.id) })
            }
        }
    }

    override suspend fun updateList(listId: String, name: String, description: String?) {
        withContext(Dispatchers.IO) {
            val response = api.updateShoppingList(
                id = listId,
                payload = ShoppingListUpsertRequest(
                    name = name,
                    description = description,
                )
            )
            shoppingListDao.upsert(response.toEntity())
        }
    }

    override suspend fun deleteList(listId: String) {
        withContext(Dispatchers.IO) {
            api.deleteShoppingList(listId)
            shoppingListDao.delete(listId)
        }
    }

    override suspend fun toggleAcquired(listId: String, itemId: String, isAcquired: Boolean) {
        withContext(Dispatchers.IO) {
            val response = api.patchListItem(
                listId = listId,
                itemId = itemId,
                payload = ShoppingListItemPatchRequest(purchased = isAcquired),
            )
            listItemDao.upsert(response.toEntity(listId))
        }
    }

    override suspend fun addItem(listId: String, productId: String, quantity: Double, unit: String?) {
        withContext(Dispatchers.IO) {
            val response = api.addListItem(
                listId = listId,
                payload = ShoppingListItemUpsertRequest(
                    productId = productId,
                    quantity = quantity,
                    unit = unit,
                ),
            )
            listItemDao.upsert(response.toEntity(listId))
        }
    }

    override suspend fun deleteItem(listId: String, itemId: String) {
        withContext(Dispatchers.IO) {
            api.deleteListItem(listId, itemId)
            listItemDao.delete(itemId)
        }
    }

    override suspend fun purchaseList(listId: String) {
        withContext(Dispatchers.IO) {
            api.markListPurchased(listId, ShoppingListPurchaseRequest())
        }
    }

    override suspend fun resetList(listId: String) {
        withContext(Dispatchers.IO) {
            api.resetList(listId)
        }
    }

    override suspend fun moveListToPantry(listId: String, pantryId: String) {
        withContext(Dispatchers.IO) {
            api.moveListToPantry(listId, MoveListToPantryRequest(pantryId = pantryId))
        }
    }

    override suspend fun shareList(listId: String, email: String) {
        withContext(Dispatchers.IO) {
            api.shareList(listId, ShareListRequest(recipients = listOf(email)))
        }
    }

    override suspend fun getSharedUsers(listId: String) {
        withContext(Dispatchers.IO) {
            api.getSharedUsers(listId)
        }
    }

    override suspend fun revokeShare(listId: String, userId: String) {
        withContext(Dispatchers.IO) {
            api.revokeListShare(listId, userId)
        }
    }

    private suspend fun ensureSynced() {
        if (hasSyncedLists.compareAndSet(false, true)) {
            withContext(Dispatchers.IO) {
                refreshListsInternal()
            }
        }
    }

    private suspend fun refreshListsInternal() {
        try {
            val lists = fetchAllPages { page, perPage ->
                api.getShoppingLists(page = page, perPage = perPage)
            }
            shoppingListDao.clearAll()
            listItemDao.clearAll()
            lists.forEach { list ->
                shoppingListDao.upsert(list.toEntity())
                if (list.items.isNotEmpty()) {
                    listItemDao.upsertAll(list.items.map { it.toEntity(list.id) })
                }
            }
        } catch (throwable: Throwable) {
            Log.w(TAG, "Failed to refresh shopping lists", throwable)
        }
    }

    private fun ShoppingListEntity.toDomainModel(items: List<ListItemEntity>): ShoppingList = ShoppingList(
        id = id,
        name = name,
        description = description,
        ownerId = ownerId,
        sharedWith = sharedWith,
        isRecurring = isRecurring,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastPurchasedAt = lastPurchasedAt,
        items = items.map { it.toDomainModel() },
    )

    private fun ListItemEntity.toDomainModel(): ListItem = ListItem(
        id = id,
        productId = productId,
        name = productName ?: productId,
        quantity = quantity,
        unit = unit,
        isAcquired = isAcquired,
        categoryId = categoryId,
        pantryId = pantryId,
        notes = notes,
        addedAt = addedAt,
        updatedAt = updatedAt,
    )

    companion object {
        private const val TAG = "ShoppingListsRepo"
    }
}
