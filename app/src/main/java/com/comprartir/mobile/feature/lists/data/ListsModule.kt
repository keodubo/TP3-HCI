package com.comprartir.mobile.feature.lists.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DISABLED: This module provided FakeListsRepository which is no longer used in production.
// The feature/lists UI screens have been migrated to use the real ShoppingListsRepository
// from the lists/ module. This fake repository is kept for reference but not injected.
//
// @Module
// @InstallIn(SingletonComponent::class)
// abstract class ListsModule {
//     @Binds
//     @Singleton
//     abstract fun bindListsRepository(
//         impl: FakeListsRepository,
//     ): ListsRepository
// }
