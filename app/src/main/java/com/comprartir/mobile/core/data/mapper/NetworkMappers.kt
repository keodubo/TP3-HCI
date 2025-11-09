package com.comprartir.mobile.core.data.mapper

import com.comprartir.mobile.core.database.entity.CategoryEntity
import com.comprartir.mobile.core.database.entity.ListItemEntity
import com.comprartir.mobile.core.database.entity.PantryItemEntity
import com.comprartir.mobile.core.database.entity.ProductEntity
import com.comprartir.mobile.core.database.entity.ProfileEntity
import com.comprartir.mobile.core.database.entity.ShoppingListEntity
import com.comprartir.mobile.core.database.entity.UserEntity
import com.comprartir.mobile.core.network.CategoryDto
import com.comprartir.mobile.core.network.PantryItemDto
import com.comprartir.mobile.core.network.ProductDto
import com.comprartir.mobile.core.network.ProfileDto
import com.comprartir.mobile.core.network.ShoppingListDto
import com.comprartir.mobile.core.network.ShoppingListItemDto
import com.comprartir.mobile.core.network.UserDto
import java.time.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    displayName = displayName ?: listOfNotNull(name, surname).joinToString(" ").ifBlank { email },
    photoUrl = photoUrl,
    isVerified = isVerified,
    createdAt = Instant.now(),
    updatedAt = Instant.now(),
)

fun ProfileDto.toEntity(): ProfileEntity = ProfileEntity(
    userId = userId,
    bio = bio,
    phoneNumber = phoneNumber,
    preferredLanguage = preferredLanguage,
    notificationOptIn = notificationOptIn,
    themeMode = themeMode,
    updatedAt = updatedAt,
)

fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description,
    color = metadata.stringOrNull("color"),
    iconName = metadata.stringOrNull("icon"),
    createdAt = createdAt ?: Instant.now(),
    updatedAt = updatedAt ?: Instant.now(),
)

fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    categoryId = category?.id ?: categoryId,
    unit = unit,
    defaultQuantity = defaultQuantity,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ShoppingListDto.toEntity(): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    description = description,
    ownerId = ownerId,
    sharedWith = sharedUsers.map { it.email },
    isShared = sharedUsers.isNotEmpty(),
    isRecurring = recurring ?: isRecurring,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPurchasedAt = lastPurchasedAt,
)

fun ShoppingListItemDto.toEntity(listId: String): ListItemEntity = ListItemEntity(
    id = id,
    listId = listId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unit = unit,
    isAcquired = purchased || (isAcquired == true),
    notes = metadata.stringOrNull("notes"),
    addedBy = metadata.stringOrNull("added_by"),
    addedAt = createdAt ?: Instant.now(),
    updatedAt = updatedAt ?: createdAt ?: Instant.now(),
    categoryId = categoryId,
    pantryId = pantryId,
)

fun PantryItemDto.toEntity(): PantryItemEntity = PantryItemEntity(
    id = id,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unit = unit,
    expiresAt = expirationDate,
    createdAt = createdAt ?: Instant.now(),
    updatedAt = updatedAt ?: createdAt ?: Instant.now(),
    location = metadata.stringOrNull("location"),
    categoryId = categoryId,
    pantryId = pantryId,
)

private fun JsonObject?.stringOrNull(key: String): String? = this
    ?.get(key)
    ?.jsonPrimitive
    ?.contentOrNull
