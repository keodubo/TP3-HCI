package com.comprartir.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.comprartir.mobile.core.database.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items")
    fun observePantry(): Flow<List<PantryItemEntity>>
    
    @Query("SELECT * FROM pantry_items")
    suspend fun getAll(): List<PantryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PantryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PantryItemEntity>)

    @Query("SELECT * FROM pantry_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: String): PantryItemEntity?
    
    @Query("SELECT * FROM pantry_items WHERE pantry_id = :pantryId AND product_id = :productId LIMIT 1")
    suspend fun findByPantryAndProduct(pantryId: String, productId: String): PantryItemEntity?

    @Query("DELETE FROM pantry_items WHERE id = :itemId")
    suspend fun delete(itemId: String)

    @Query("DELETE FROM pantry_items")
    suspend fun clearAll()
}
