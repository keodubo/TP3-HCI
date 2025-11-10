package com.comprartir.mobile.feature.listdetail.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ListDetailModule {
    @Binds
    @Singleton
    abstract fun bindListDetailRepository(
        impl: FakeListDetailRepository,
    ): ListDetailRepository
}

