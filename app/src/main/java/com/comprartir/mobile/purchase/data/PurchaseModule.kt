package com.comprartir.mobile.purchase.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PurchaseModule {
    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(
        impl: DefaultPurchaseRepository,
    ): PurchaseRepository
}
