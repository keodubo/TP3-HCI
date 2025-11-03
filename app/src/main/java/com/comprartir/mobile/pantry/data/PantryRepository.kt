package com.comprartir.mobile.pantry.data

import android.util.Log
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.PantryDao
import com.comprartir.mobile.core.database.entity.PantryItemEntity
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.PantryItemUpsertRequest
import com.comprartir.mobile.core.network.fetchAllPages
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

interface PantryRepository {
    fun observePantry(): Flow<List<PantryItem>>
    suspend fun refresh()
    suspend fun upsertItem(item: PantryItem)
    suspend fun deleteItem(itemId: String)
}

@Singleton
class DefaultPantryRepository @Inject constructor(
    private val pantryDao: PantryDao,
    private val api: ComprartirApi,
) : PantryRepository {

    private val hasSyncedPantry = AtomicBoolean(false)
    private val cachedPantryId = AtomicReference<String?>(null)

    override fun observePantry(): Flow<List<PantryItem>> = pantryDao.observePantry()
        .map { entities -> entities.map { it.toDomainModel() } }
        .onStart { ensureSynced() }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) { refreshPantryInternal() }
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

    private suspend fun ensureSynced() {
        if (hasSyncedPantry.compareAndSet(false, true)) {
            withContext(Dispatchers.IO) {
                refreshPantryInternal()
            }
        }
    }

    private suspend fun refreshPantryInternal() {
        try {
            val pantries = fetchAllPages { page, perPage ->
                api.getPantries(page = page, perPage = perPage)
            }
            pantryDao.clearAll()
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
}
