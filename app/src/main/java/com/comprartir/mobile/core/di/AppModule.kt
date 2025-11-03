package com.comprartir.mobile.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.comprartir.mobile.core.database.ComprartirDatabase
import com.comprartir.mobile.core.database.dao.CategoryDao
import com.comprartir.mobile.core.database.dao.ListItemDao
import com.comprartir.mobile.core.database.dao.PantryDao
import com.comprartir.mobile.core.database.dao.ProductDao
import com.comprartir.mobile.core.database.dao.ProfileDao
import com.comprartir.mobile.core.database.dao.ShoppingListDao
import com.comprartir.mobile.core.database.dao.UserDao
import com.comprartir.mobile.core.util.FeatureFlags
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_prefs"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ComprartirDatabase = Room.databaseBuilder(
        context,
        ComprartirDatabase::class.java,
        "comprartir.db",
    )
        // TODO: Provide migrations once schema is defined.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES_NAME) },
    )

    @Provides
    @Singleton
    fun provideFeatureFlags(): FeatureFlags = FeatureFlags.Disabled

    @Provides
    fun provideProductDao(database: ComprartirDatabase): ProductDao = database.productDao()

    @Provides
    fun provideShoppingListDao(database: ComprartirDatabase): ShoppingListDao = database.shoppingListDao()

    @Provides
    fun providePantryDao(database: ComprartirDatabase): PantryDao = database.pantryDao()

    @Provides
    fun provideCategoryDao(database: ComprartirDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideListItemDao(database: ComprartirDatabase): ListItemDao = database.listItemDao()

    @Provides
    fun provideUserDao(database: ComprartirDatabase): UserDao = database.userDao()

    @Provides
    fun provideProfileDao(database: ComprartirDatabase): ProfileDao = database.profileDao()
}
