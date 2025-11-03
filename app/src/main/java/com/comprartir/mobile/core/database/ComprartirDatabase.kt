package com.comprartir.mobile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.comprartir.mobile.core.database.dao.CategoryDao
import com.comprartir.mobile.core.database.dao.ListItemDao
import com.comprartir.mobile.core.database.dao.PantryDao
import com.comprartir.mobile.core.database.dao.ProductDao
import com.comprartir.mobile.core.database.dao.ProfileDao
import com.comprartir.mobile.core.database.dao.ShoppingListDao
import com.comprartir.mobile.core.database.dao.UserDao
import com.comprartir.mobile.core.database.entity.CategoryEntity
import com.comprartir.mobile.core.database.entity.ListItemEntity
import com.comprartir.mobile.core.database.entity.PantryItemEntity
import com.comprartir.mobile.core.database.entity.ProductEntity
import com.comprartir.mobile.core.database.entity.ProfileEntity
import com.comprartir.mobile.core.database.entity.ShoppingListEntity
import com.comprartir.mobile.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProfileEntity::class,
        ProductEntity::class,
        CategoryEntity::class,
        ShoppingListEntity::class,
        ListItemEntity::class,
        PantryItemEntity::class,
    ],
    version = 2,
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class ComprartirDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun listItemDao(): ListItemDao
    abstract fun pantryDao(): PantryDao
}
