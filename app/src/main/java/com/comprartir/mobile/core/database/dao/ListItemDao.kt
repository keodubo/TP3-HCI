package com.comprartir.mobile.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.comprartir.mobile.core.database.entity.ListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListItemDao {
    @Query("SELECT * FROM list_items WHERE list_id = :listId ORDER BY added_at")
    fun observeItems(listId: String): Flow<List<ListItemEntity>>

    @Query("""
        SELECT li.* FROM list_items li
        INNER JOIN shopping_lists sl ON li.list_id = sl.id
        WHERE sl.owner_id = :userId OR sl.shared_with LIKE '%' || :userId || '%'
    """)
    fun observeAllItems(userId: String): Flow<List<ListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ListItemEntity>)

    @Query("DELETE FROM list_items WHERE id = :itemId")
    suspend fun delete(itemId: String)

    @Query("DELETE FROM list_items WHERE list_id = :listId")
    suspend fun deleteByList(listId: String)

    @Query("DELETE FROM list_items")
    suspend fun clearAll()
}
