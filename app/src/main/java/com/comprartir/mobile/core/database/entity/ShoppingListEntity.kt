package com.comprartir.mobile.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "shared_with") val sharedWith: List<String>,
    @ColumnInfo(name = "is_shared") val isShared: Boolean = sharedWith.isNotEmpty(),
    @ColumnInfo(name = "is_recurring") val isRecurring: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
    @ColumnInfo(name = "last_purchased_at") val lastPurchasedAt: Instant? = null,
)
