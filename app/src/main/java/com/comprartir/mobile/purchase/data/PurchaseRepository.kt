package com.comprartir.mobile.purchase.data

import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.PurchaseDto
import com.comprartir.mobile.core.network.fetchAllPages
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

data class Purchase(
    val id: String,
    val listId: String,
    val listName: String?,
    val purchasedAt: Instant,
    val totalItems: Int,
    val acquiredItems: Int,
    val isRecurring: Boolean,
)

interface PurchaseRepository {
    val purchases: StateFlow<List<Purchase>>
    suspend fun refresh()
    suspend fun getPurchase(purchaseId: String): Purchase
    suspend fun restorePurchase(purchaseId: String): Purchase
}

@Singleton
class DefaultPurchaseRepository @Inject constructor(
    private val api: ComprartirApi,
) : PurchaseRepository {

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    override val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val remotePurchases = fetchAllPages { page, perPage ->
            api.getPurchases(page = page, perPage = perPage)
        }.map { it.toDomain() }
        _purchases.value = remotePurchases.sortedByDescending { it.purchasedAt }
    }

    override suspend fun getPurchase(purchaseId: String): Purchase = withContext(Dispatchers.IO) {
        val purchase = api.getPurchase(purchaseId).toDomain()
        updateCache(purchase)
        purchase
    }

    override suspend fun restorePurchase(purchaseId: String): Purchase = withContext(Dispatchers.IO) {
        val restored = api.restorePurchase(purchaseId).toDomain()
        updateCache(restored)
        restored
    }

    private fun updateCache(purchase: Purchase) {
        _purchases.update { current ->
            val mutable = current.toMutableList()
            val index = mutable.indexOfFirst { it.id == purchase.id }
            if (index >= 0) {
                mutable[index] = purchase
            } else {
                mutable.add(purchase)
            }
            mutable.sortedByDescending { it.purchasedAt }
        }
    }
}

private fun PurchaseDto.toDomain(): Purchase {
    val items = list.items
    val acquired = items.count { it.purchased || it.isAcquired == true }
    return Purchase(
        id = id,
        listId = listId,
        listName = list.name,
        purchasedAt = purchasedAt,
        totalItems = items.size,
        acquiredItems = acquired,
        isRecurring = list.recurring ?: false,
    )
}
