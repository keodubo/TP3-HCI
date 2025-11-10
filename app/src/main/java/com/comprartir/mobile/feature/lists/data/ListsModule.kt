package com.comprartir.mobile.feature.lists.data

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
    abstract fun bindListsRepository(
        impl: FakeListsRepository,
    ): ListsRepository
}
