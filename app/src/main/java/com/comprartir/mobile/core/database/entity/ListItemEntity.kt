package com.comprartir.mobile.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "list_items")
data class ListItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "list_id") val listId: String,
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "product_name") val productName: String?,
    val quantity: Double = 1.0,
    val unit: String?,
    @ColumnInfo(name = "is_acquired") val isAcquired: Boolean = false,
    val notes: String?,
    @ColumnInfo(name = "added_by") val addedBy: String?,
    @ColumnInfo(name = "added_at") val addedAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = addedAt,
    @ColumnInfo(name = "category_id") val categoryId: String? = null,
    @ColumnInfo(name = "pantry_id") val pantryId: String? = null,
)
