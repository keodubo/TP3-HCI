package com.comprartir.mobile.products.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductsModule {
    @Binds
    @Singleton
    abstract fun bindProductsRepository(impl: DefaultProductsRepository): ProductsRepository
}
