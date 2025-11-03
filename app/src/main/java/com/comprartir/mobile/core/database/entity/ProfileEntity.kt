package com.comprartir.mobile.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    val bio: String?,
    @ColumnInfo(name = "phone_number") val phoneNumber: String?,
    @ColumnInfo(name = "preferred_language") val preferredLanguage: String?,
    @ColumnInfo(name = "notification_opt_in") val notificationOptIn: Boolean = true,
    @ColumnInfo(name = "theme_mode") val themeMode: String = "system",
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now(),
)
