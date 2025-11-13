package com.comprartir.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.comprartir.mobile.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    // Note: shared_with is stored as semicolon-separated string (e.g., "user1;user2")
    // We use LIKE to check if userId appears in the string
    @Query("""
        SELECT * FROM shopping_lists 
        WHERE owner_id = :userId 
           OR shared_with LIKE '%' || :userId || '%'
    """)
    fun observeLists(userId: String): Flow<List<ShoppingListEntity>>

    @Query("""
        SELECT * FROM shopping_lists 
        WHERE id = :listId 
          AND (owner_id = :userId OR shared_with LIKE '%' || :userId || '%')
    """)
    fun observeList(listId: String, userId: String): Flow<ShoppingListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lists: List<ShoppingListEntity>)

    @Query("DELETE FROM shopping_lists WHERE id = :listId")
    suspend fun delete(listId: String)

    @Query("DELETE FROM shopping_lists")
    suspend fun clearAll()
}
