package com.comprartir.mobile.purchase.data

import com.comprartir.mobile.core.network.ComprartirApi
import javax.inject.Inject
import javax.inject.Singleton

interface PurchaseRepository {
    suspend fun getPurchases()
    suspend fun getPurchase(purchaseId: String)
    suspend fun restorePurchase(purchaseId: String)
}

@Singleton
class DefaultPurchaseRepository @Inject constructor(
    private val api: ComprartirApi
) : PurchaseRepository {

    override suspend fun getPurchases() {
        api.getPurchases()
    }

    override suspend fun getPurchase(purchaseId: String) {
        api.getPurchase(purchaseId)
    }

    override suspend fun restorePurchase(purchaseId: String) {
        api.restorePurchase(purchaseId)
    }
}