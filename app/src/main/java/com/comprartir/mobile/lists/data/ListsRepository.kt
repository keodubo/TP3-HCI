package com.comprartir.mobile.lists.data

import android.util.Log
import com.comprartir.mobile.auth.data.AuthRepository
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
import com.comprartir.mobile.core.network.ShoppingListCreateRequest
import com.comprartir.mobile.core.network.ShoppingListUpsertRequest
import com.comprartir.mobile.core.network.fetchAllPages
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    suspend fun createList(name: String, description: String? = null, isRecurring: Boolean = false)
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
    private val authRepository: AuthRepository,
    private val json: Json,
) : ShoppingListsRepository {

    private var lastSyncedUserId: String? = null
    private val syncLock = Any()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeLists(): Flow<List<ShoppingList>> = authRepository.currentUser
        .map { user ->
            val userId = user?.id.orEmpty()
            Log.d(TAG, "observeLists: currentUser changed - userId='$userId', email='${user?.email}'")
            userId
        }
        .flatMapLatest { userId ->
            Log.d(TAG, "observeLists: flatMapLatest START for userId='$userId'")
            if (userId.isEmpty()) {
                Log.w(TAG, "observeLists: userId is EMPTY, returning empty list")
                flowOf(emptyList())
            } else {
                combine(
                    shoppingListDao.observeLists(userId),
                    listItemDao.observeAllItems(userId),
                ) { listEntities, itemEntities ->
                    Log.d(TAG, "observeLists: Got ${listEntities.size} lists from Room for userId=$userId")
                    Log.d(TAG, "observeLists: List IDs = ${listEntities.map { it.id }}")
                    Log.d(TAG, "observeLists: List names = ${listEntities.map { it.name }}")
                    Log.d(TAG, "observeLists: List ownerIds = ${listEntities.map { it.ownerId }}")
                    Log.d(TAG, "observeLists: Got ${itemEntities.size} items from Room for userId=$userId")
                    Log.d(TAG, "observeLists: Items by listId: ${itemEntities.groupBy { it.listId }.mapValues { it.value.size }}")
                    val groupedItems = itemEntities.groupBy { it.listId }
                    val domainLists = listEntities.map { entity -> 
                        val items = groupedItems[entity.id].orEmpty()
                        Log.d(TAG, "observeLists: List ${entity.name} (${entity.id}) has ${items.size} items")
                        entity.toDomainModel(items)
                    }
                    Log.d(TAG, "observeLists: Mapped to ${domainLists.size} domain models")
                    Log.d(TAG, "observeLists: Domain lists with items: ${domainLists.map { "${it.name}:${it.items.size}" }}")
                    domainLists
                }
            }
        }.onStart { ensureSynced() }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeList(listId: String): Flow<ShoppingList?> = authRepository.currentUser
        .map { it?.id.orEmpty() }
        .flatMapLatest { userId ->
            if (userId.isEmpty()) {
                flowOf(null)
            } else {
                combine(
                    shoppingListDao.observeList(listId, userId),
                    listItemDao.observeItems(listId),
                ) { listEntity, items ->
                    listEntity?.toDomainModel(items)
                }
            }
        }.onStart { ensureSynced() }

    override suspend fun refresh() {
        Log.d(TAG, "refresh: Manual refresh requested")
        withContext(Dispatchers.IO) { refreshListsInternal() }
    }

    override suspend fun createList(name: String, description: String?, isRecurring: Boolean) {
        withContext(Dispatchers.IO) {
            val currentUserId = authRepository.currentUser.firstOrNull()?.id.orEmpty()
            Log.d(TAG, "createList: ========================================")
            Log.d(TAG, "createList: STARTING - name='$name', description='$description', isRecurring=$isRecurring")
            Log.d(TAG, "createList: Current userId=$currentUserId")
            
            // Use the specific CREATE DTO that only includes fields the backend accepts
            val request = ShoppingListCreateRequest(
                name = name,
                description = description.orEmpty(), // Backend requires this field present
                recurring = isRecurring,
                metadata = null, // Optional, not used for now
            )
            
            // Log the actual JSON that will be sent
            val jsonBody = json.encodeToString(request)
            Log.d(TAG, "createList: ✨ REQUEST JSON: $jsonBody")
            Log.d(TAG, "createList: Request object: name='${request.name}', description='${request.description}', recurring=${request.recurring}")
            Log.d(TAG, "createList: Calling API POST /api/shopping-lists...")
            
            val response = try {
                val result = api.createShoppingList(request)
                Log.d(TAG, "createList: ✅ HTTP SUCCESS - list created")
                Log.d(TAG, "createList: Response: id=${result.id}, name=${result.name}")
                Log.d(TAG, "createList: Response owner: id=${result.owner?.id}, email=${result.owner?.email}")
                result
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "createList: ❌ HTTP ${e.code()} ERROR")
                Log.e(TAG, "createList: Error body: $errorBody")
                Log.e(TAG, "createList: Full exception: ${e.message()}", e)
                when (e.code()) {
                    401 -> throw Exception("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", e)
                    else -> throw Exception("HTTP ${e.code()}: $errorBody", e)
                }
            } catch (e: kotlinx.serialization.SerializationException) {
                Log.e(TAG, "createList: ❌ Serialization error: ${e.message}", e)
                Log.e(TAG, "createList: This might be a mismatch between backend response and DTO")
                throw Exception("Failed to parse response: ${e.message}", e)
            } catch (e: Exception) {
                Log.e(TAG, "createList: ❌ FAILED with exception: ${e.message}", e)
                throw e
            }
            
            Log.d(TAG, "createList: ✅ API SUCCESS - Backend returned list id=${response.id}")
            Log.d(TAG, "createList: Response details: name='${response.name}', owner.id=${response.owner?.id}")
            Log.d(TAG, "createList: sharedWith=${response.sharedWith.size}, items=${response.items.size}")
            
            val entity = response.toEntity()
            Log.d(TAG, "createList: Mapped to entity -> id=${entity.id}, ownerId=${entity.ownerId}, name='${entity.name}'")
            Log.d(TAG, "createList: Entity sharedWith=${entity.sharedWith}, isShared=${entity.isShared}")
            Log.d(TAG, "createList: Inserting into Room...")
            
            shoppingListDao.upsert(entity)
            Log.d(TAG, "createList: ✅ Entity saved to Room")
            
            // Verify the list was saved
            Log.d(TAG, "createList: Verifying save by querying Room for userId=$currentUserId...")
            val savedLists = shoppingListDao.observeLists(currentUserId).firstOrNull() ?: emptyList()
            Log.d(TAG, "createList: Room now has ${savedLists.size} lists for userId=$currentUserId")
            Log.d(TAG, "createList: Saved list IDs: ${savedLists.map { it.id }}")
            val justCreated = savedLists.find { it.id == response.id }
            if (justCreated != null) {
                Log.d(TAG, "createList: ✅ NEW LIST FOUND in Room: id=${justCreated.id}, name='${justCreated.name}'")
            } else {
                Log.e(TAG, "createList: ⚠️ WARNING - New list NOT FOUND in Room query!")
            }
            
            listItemDao.deleteByList(response.id)
            if (response.items.isNotEmpty()) {
                listItemDao.upsertAll(response.items.map { it.toEntity(response.id) })
                Log.d(TAG, "createList: Saved ${response.items.size} items for list")
            }
            
            Log.d(TAG, "createList: ========================================")
        }
    }

    override suspend fun updateList(listId: String, name: String, description: String?) {
        withContext(Dispatchers.IO) {
            val response = api.updateShoppingList(
                id = listId,
                payload = ShoppingListUpsertRequest(
                    name = name,
                    description = description.orEmpty(),
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
        val currentUserId = authRepository.currentUser.firstOrNull()?.id.orEmpty()
        Log.d(TAG, "ensureSynced: Called for userId=$currentUserId, lastSyncedUserId=$lastSyncedUserId")
        
        val shouldSync = synchronized(syncLock) {
            if (lastSyncedUserId != currentUserId) {
                Log.d(TAG, "ensureSynced: User changed or first sync, will sync")
                lastSyncedUserId = currentUserId
                true
            } else {
                Log.d(TAG, "ensureSynced: Already synced for this user, skipping")
                false
            }
        }
        
        if (shouldSync) {
            withContext(Dispatchers.IO) {
                refreshListsInternal()
            }
        }
    }

    private suspend fun refreshListsInternal() {
        try {
            val currentUserId = authRepository.currentUser.firstOrNull()?.id.orEmpty()
            Log.d(TAG, "refreshListsInternal: Fetching lists from backend for userId=$currentUserId")
            val lists = fetchAllPages { page, perPage ->
                api.getShoppingLists(page = page, perPage = perPage)
            }
            Log.d(TAG, "refreshListsInternal: Backend returned ${lists.size} lists")
            Log.d(TAG, "refreshListsInternal: List IDs from backend = ${lists.map { it.id }}")
            Log.d(TAG, "refreshListsInternal: List names from backend = ${lists.map { it.name }}")
            Log.d(TAG, "refreshListsInternal: OwnerIds = ${lists.map { "${it.id}:${it.owner?.id}" }}")
            Log.d(TAG, "refreshListsInternal: Clearing Room tables...")
            shoppingListDao.clearAll()
            listItemDao.clearAll()
            Log.d(TAG, "refreshListsInternal: Saving ${lists.size} lists to Room...")
            lists.forEach { list ->
                val entity = list.toEntity()
                Log.d(TAG, "refreshListsInternal: List ${list.id}: name='${list.name}', owner.id=${list.owner?.id}, entity.ownerId=${entity.ownerId}")
                shoppingListDao.upsert(entity)
                if (list.items.isNotEmpty()) {
                    listItemDao.upsertAll(list.items.map { it.toEntity(list.id) })
                    Log.d(TAG, "refreshListsInternal: Saved ${list.items.size} items for list ${list.id}")
                }
            }
            Log.d(TAG, "refreshListsInternal: ✅ All lists saved to Room successfully!")
        } catch (e: retrofit2.HttpException) {
            Log.w(TAG, "Failed to refresh shopping lists - HTTP ${e.code()}", e)
            when (e.code()) {
                401 -> throw Exception("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", e)
                else -> throw e
            }
        } catch (throwable: Throwable) {
            Log.w(TAG, "Failed to refresh shopping lists", throwable)
            throw throwable
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
