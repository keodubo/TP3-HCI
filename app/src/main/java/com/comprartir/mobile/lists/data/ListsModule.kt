package com.comprartir.mobile.lists.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ListsModule {
    @Binds
    @Singleton
    abstract fun bindShoppingListsRepository(impl: DefaultShoppingListsRepository): ShoppingListsRepository
}
