package com.comprartir.mobile.pantry.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PantryModule {
    @Binds
    @Singleton
    abstract fun bindPantryRepository(impl: DefaultPantryRepository): PantryRepository
}
