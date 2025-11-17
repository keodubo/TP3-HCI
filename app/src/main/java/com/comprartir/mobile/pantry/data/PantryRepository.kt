package com.comprartir.mobile.pantry.data

import android.util.Log
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.PantryDao
import com.comprartir.mobile.core.database.entity.PantryItemEntity
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.PantryItemUpsertRequest
import com.comprartir.mobile.core.network.PantryUpsertRequest
import com.comprartir.mobile.core.network.SharePantryRequest
import com.comprartir.mobile.core.network.fetchAllPages
import com.comprartir.mobile.core.network.UserSummaryDto
import com.comprartir.mobile.core.network.PantryDto
import com.comprartir.mobile.lists.data.ListItem
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

data class PantryItem(
    val id: String,
    val productId: String,
    val name: String,
    val quantity: Double,
    val unit: String?,
    val expiresAt: Instant?,
    val pantryId: String?,
    val categoryId: String?,
    val location: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class PantrySummary(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val isOwner: Boolean,
    val sharedUsers: List<SharedPantryUser>,
)

data class SharedPantryUser(
    val id: String,
    val displayName: String,
    val email: String,
)

interface PantryRepository {
    fun observePantries(): Flow<List<PantrySummary>>
    fun observePantry(): Flow<List<PantryItem>>
    suspend fun refresh()
    suspend fun addItemsFromList(pantryId: String, items: List<ListItem>)
    suspend fun createPantry(name: String, description: String?)
    suspend fun updatePantry(pantryId: String, name: String, description: String?)
    suspend fun deletePantry(pantryId: String)
    suspend fun upsertItem(item: PantryItem)
    suspend fun deleteItem(itemId: String)
    suspend fun sharePantry(pantryId: String, email: String)
    suspend fun getSharedUsers(pantryId: String): List<SharedPantryUser>
    suspend fun revokeShare(pantryId: String, userId: String)
}

@Singleton
class DefaultPantryRepository @Inject constructor(
    private val pantryDao: PantryDao,
    private val api: ComprartirApi,
    private val authRepository: AuthRepository,
) : PantryRepository {

    private val hasSyncedPantry = AtomicBoolean(false)
    private val cachedPantryId = AtomicReference<String?>(null)
    private val pantriesState = MutableStateFlow<List<PantrySummary>>(emptyList())

    override fun observePantries(): Flow<List<PantrySummary>> = pantriesState

    override fun observePantry(): Flow<List<PantryItem>> = pantryDao.observePantry()
        .map { entities -> entities.map { it.toDomainModel() } }
        .onStart { ensureSynced() }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) { refreshPantryInternal() }
    }

    override suspend fun addItemsFromList(pantryId: String, items: List<ListItem>) {
        if (items.isEmpty()) return
        withContext(Dispatchers.IO) {
            val errors = mutableListOf<String>()
            items.forEach { item ->
                runCatching {
                    Log.d(TAG, "addItemsFromList: Adding item ${item.name} (productId=${item.productId}) to pantry $pantryId")
                    val payload = PantryItemUpsertRequest(
                        productId = item.productId,
                        quantity = item.quantity,
                        unit = item.unit,
                    )
                    val response = api.addPantryItem(pantryId, payload)
                    pantryDao.upsert(response.copy(pantryId = response.pantryId ?: pantryId).toEntity())
                    Log.d(TAG, "addItemsFromList: Successfully added ${item.name} to pantry")
                }.onFailure { throwable ->
                    Log.e(TAG, "addItemsFromList: Failed to add ${item.name} to pantry $pantryId", throwable)
                    errors.add(item.name)
                }
            }
            if (errors.isNotEmpty()) {
                Log.w(TAG, "addItemsFromList: Failed to add ${errors.size} items: ${errors.joinToString()}")
                // Don't throw, just log the error and continue with successful items
            }
            refreshPantryInternal()
        }
    }

    override suspend fun createPantry(name: String, description: String?) {
        withContext(Dispatchers.IO) {
            api.createPantry(PantryUpsertRequest(name = name, description = description))
            refreshPantryInternal()
        }
    }

    override suspend fun updatePantry(pantryId: String, name: String, description: String?) {
        withContext(Dispatchers.IO) {
            api.updatePantry(pantryId, PantryUpsertRequest(name = name, description = description))
            refreshPantryInternal()
        }
    }

    override suspend fun deletePantry(pantryId: String) {
        withContext(Dispatchers.IO) {
            api.deletePantry(pantryId)
            refreshPantryInternal()
        }
    }

    override suspend fun upsertItem(item: PantryItem) {
        withContext(Dispatchers.IO) {
            val payload = PantryItemUpsertRequest(
                productId = item.productId,
                quantity = item.quantity,
                unit = item.unit,
                expirationDate = item.expiresAt,
            )
            val pantryId = item.pantryId ?: defaultPantryId()
            val response = if (item.id.isBlank()) {
                api.addPantryItem(pantryId, payload)
            } else {
                api.updatePantryItem(pantryId, item.id, payload)
            }
            pantryDao.upsert(response.toEntity())
        }
    }

    override suspend fun deleteItem(itemId: String) {
        withContext(Dispatchers.IO) {
            val existing = pantryDao.getById(itemId)
            val pantryId = existing?.pantryId ?: defaultPantryId()
            try {
                api.deletePantryItem(pantryId, itemId)
            } catch (throwable: Throwable) {
                Log.w(TAG, "Failed to delete pantry item $itemId", throwable)
            }
            pantryDao.delete(itemId)
        }
    }

    override suspend fun sharePantry(pantryId: String, email: String) {
        withContext(Dispatchers.IO) {
            api.sharePantry(pantryId, SharePantryRequest(email = email))
            refreshPantryInternal()
        }
    }

    override suspend fun getSharedUsers(pantryId: String): List<SharedPantryUser> =
        withContext(Dispatchers.IO) {
            api.getPantrySharedUsers(pantryId).map { it.toSharedPantryUser() }
    }

    override suspend fun revokeShare(pantryId: String, userId: String) {
        withContext(Dispatchers.IO) {
            api.revokePantryShare(pantryId, userId)
            refreshPantryInternal()
        }
    }

    private suspend fun ensureSynced() {
        if (hasSyncedPantry.compareAndSet(false, true)) {
            withContext(Dispatchers.IO) {
                refreshPantryInternal()
            }
        }
    }

    private suspend fun refreshPantryInternal() {
        try {
            val currentUserId = authRepository.currentUser.firstOrNull()?.id.orEmpty()
            val pantries = fetchAllPages { page, perPage ->
                api.getPantries(page = page, perPage = perPage)
            }
            pantryDao.clearAll()
            pantriesState.value = pantries.map { it.toSummary(currentUserId) }
            if (pantries.isEmpty()) {
                return
            }
            cachedPantryId.set(pantries.first().id)
            pantries.forEach { pantry ->
                try {
                    val items = fetchAllPages { page, perPage ->
                        api.getPantryItems(pantryId = pantry.id, page = page, perPage = perPage)
                    }
                    pantryDao.upsertAll(items.map { it.copy(pantryId = pantry.id).toEntity() })
                } catch (throwable: Throwable) {
                    Log.w(TAG, "Failed to refresh items for pantry ${pantry.id}", throwable)
                }
            }
        } catch (throwable: Throwable) {
            Log.w(TAG, "Failed to refresh pantry", throwable)
        }
    }

    private suspend fun defaultPantryId(): String {
        cachedPantryId.get()?.let { return it }
        val pantries = try {
            api.getPantries(page = 1, perPage = 1).data
        } catch (throwable: Throwable) {
            Log.w(TAG, "Failed to fetch default pantry id", throwable)
            emptyList()
        }
        val id = pantries.firstOrNull()?.id ?: TEMP_PANTRY_ID
        cachedPantryId.set(id)
        return id
    }

    private fun PantryItemEntity.toDomainModel(): PantryItem = PantryItem(
        id = id,
        productId = productId,
        name = productName ?: productId,
        quantity = quantity,
        unit = unit,
        expiresAt = expiresAt,
        pantryId = pantryId,
        categoryId = categoryId,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        private const val TAG = "PantryRepository"
        private const val TEMP_PANTRY_ID = "default"
    }

    private fun PantryDto.toSummary(currentUserId: String): PantrySummary =
        PantrySummary(
            id = id,
            name = name,
            description = description,
            ownerId = ownerId,
            isOwner = ownerId == currentUserId,
            sharedUsers = sharedUsers.map { it.toSharedPantryUser() },
        )

    private fun UserSummaryDto.toSharedPantryUser(): SharedPantryUser =
        SharedPantryUser(
            id = id,
            displayName = displayName.takeIf { it.isNotBlank() } ?: name.orEmpty(),
            email = email,
        )
}
