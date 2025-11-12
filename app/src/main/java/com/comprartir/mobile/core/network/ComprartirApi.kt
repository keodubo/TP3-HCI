package com.comprartir.mobile.core.network

import com.comprartir.mobile.core.network.serialization.InstantIsoSerializer
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit definition of the Comprartir backend surface.
 */
interface ComprartirApi {

    // region Auth
    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("users/logout")
    suspend fun logout()

    @POST("users/send-verification")
    suspend fun sendVerification(@Body request: SendVerificationRequest)

    @POST("users/verify-account")
    suspend fun verifyAccount(@Body request: VerifyAccountRequest): UserDto

    @POST("users/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest)

    @POST("users/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): AuthResponse

    @POST("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    @GET("users/profile")
    suspend fun fetchProfile(): ProfileDto
    
    @GET("users/profile")
    suspend fun getUserProfile(): UserDto

    @PUT("users/profile")
    suspend fun updateProfile(@Body payload: ProfileUpdateRequest): ProfileDto
    // endregion

    // region Categories
    @GET("categories")
    suspend fun getCategories(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): ApiListResponse<CategoryDto>

    @POST("categories")
    suspend fun createCategory(@Body payload: CategoryUpsertRequest): CategoryDto

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: String): CategoryDto

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body payload: CategoryUpsertRequest,
    ): CategoryDto

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String)
    // endregion

    // region Products
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("category_id") categoryId: String? = null,
        @Query("search") search: String? = null,
    ): ApiListResponse<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): ProductDto

    @POST("products")
    suspend fun createProduct(@Body payload: ProductUpsertRequest): ProductDto

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body payload: ProductUpsertRequest,
    ): ProductDto

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String)
    // endregion

    // region Shopping Lists
    @GET("shopping-lists")
    suspend fun getShoppingLists(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): ApiListResponse<ShoppingListDto>

    @GET("shopping-lists/{id}")
    suspend fun getShoppingList(@Path("id") id: String): ShoppingListDto

    @POST("shopping-lists")
    suspend fun createShoppingList(@Body payload: ShoppingListUpsertRequest): ShoppingListDto

    @PUT("shopping-lists/{id}")
    suspend fun updateShoppingList(
        @Path("id") id: String,
        @Body payload: ShoppingListUpsertRequest,
    ): ShoppingListDto

    @DELETE("shopping-lists/{id}")
    suspend fun deleteShoppingList(@Path("id") id: String)

    @POST("shopping-lists/{id}/purchase")
    suspend fun markListPurchased(
        @Path("id") id: String,
        @Body payload: ShoppingListPurchaseRequest,
    ): ShoppingListDto

    @POST("shopping-lists/{id}/reset")
    suspend fun resetList(
        @Path("id") id: String,
        @Body payload: ShoppingListResetRequest = ShoppingListResetRequest(),
    ): ShoppingListDto

    @POST("shopping-lists/{id}/move-to-pantry")
    suspend fun moveListToPantry(
        @Path("id") id: String,
        @Body payload: MoveListToPantryRequest,
    ): PantryTransferResponse

    @POST("shopping-lists/{id}/share")
    suspend fun shareList(
        @Path("id") id: String,
        @Body payload: ShareListRequest,
    ): ShoppingListDto

    @GET("shopping-lists/{id}/shared-users")
    suspend fun getSharedUsers(@Path("id") id: String): ApiListResponse<UserSummaryDto>

    @DELETE("shopping-lists/{id}/share/{userId}")
    suspend fun revokeListShare(
        @Path("id") id: String,
        @Path("userId") userId: String,
    )

    @GET("shopping-lists/{listId}/items")
    suspend fun getShoppingListItems(
        @Path("listId") listId: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): ApiListResponse<ShoppingListItemDto>

    @POST("shopping-lists/{listId}/items")
    suspend fun addListItem(
        @Path("listId") listId: String,
        @Body payload: ShoppingListItemUpsertRequest,
    ): ShoppingListItemDto

    @PUT("shopping-lists/{listId}/items/{itemId}")
    suspend fun updateListItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String,
        @Body payload: ShoppingListItemUpsertRequest,
    ): ShoppingListItemDto

    @PATCH("shopping-lists/{listId}/items/{itemId}")
    suspend fun patchListItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String,
        @Body payload: ShoppingListItemPatchRequest,
    ): ShoppingListItemDto

    @DELETE("shopping-lists/{listId}/items/{itemId}")
    suspend fun deleteListItem(
        @Path("listId") listId: String,
        @Path("itemId") itemId: String,
    )
    // endregion

    // region Pantry
    @GET("pantries")
    suspend fun getPantries(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): ApiListResponse<PantryDto>

    @POST("pantries")
    suspend fun createPantry(@Body payload: PantryUpsertRequest): PantryDto

    @GET("pantries/{id}")
    suspend fun getPantry(@Path("id") id: String): PantryDto

    @PUT("pantries/{id}")
    suspend fun updatePantry(
        @Path("id") id: String,
        @Body payload: PantryUpsertRequest,
    ): PantryDto

    @DELETE("pantries/{id}")
    suspend fun deletePantry(@Path("id") id: String)

    @POST("pantries/{id}/share")
    suspend fun sharePantry(
        @Path("id") id: String,
        @Body payload: SharePantryRequest,
    ): PantryDto

    @GET("pantries/{id}/shared-users")
    suspend fun getPantrySharedUsers(@Path("id") id: String): ApiListResponse<UserSummaryDto>

    @DELETE("pantries/{id}/share/{userId}")
    suspend fun revokePantryShare(
        @Path("id") id: String,
        @Path("userId") userId: String,
    )

    @GET("pantries/{id}/items")
    suspend fun getPantryItems(
        @Path("id") pantryId: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): ApiListResponse<PantryItemDto>

    @POST("pantries/{id}/items")
    suspend fun addPantryItem(
        @Path("id") pantryId: String,
        @Body payload: PantryItemUpsertRequest,
    ): PantryItemDto

    @PUT("pantries/{id}/items/{itemId}")
    suspend fun updatePantryItem(
        @Path("id") pantryId: String,
        @Path("itemId") itemId: String,
        @Body payload: PantryItemUpsertRequest,
    ): PantryItemDto

    @DELETE("pantries/{id}/items/{itemId}")
    suspend fun deletePantryItem(
        @Path("id") pantryId: String,
        @Path("itemId") itemId: String,
    )
    // endregion

    // region Purchases
    @GET("purchases")
    suspend fun getPurchases(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
    ): ApiListResponse<PurchaseDto>

    @GET("purchases/{id}")
    suspend fun getPurchase(@Path("id") id: String): PurchaseDto

    @POST("purchases/{id}/restore")
    suspend fun restorePurchase(@Path("id") id: String): PurchaseDto
    // endregion
}

// region Auth DTOs
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val surname: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class SendVerificationRequest(
    val email: String,
)

@Serializable
data class VerifyAccountRequest(
    val code: String,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    @SerialName("reset_token") val resetToken: String,
    val password: String,
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto? = null,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
    val name: String? = null,
    val surname: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    val metadata: JsonObject? = null,
)

@Serializable
data class UserSummaryDto(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    val avatar: String? = null,
)
// endregion

// region Profile DTOs
@Serializable
data class ProfileDto(
    @SerialName("user_id") val userId: String,
    val bio: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String? = null,
    @SerialName("notification_opt_in") val notificationOptIn: Boolean = true,
    @SerialName("theme_mode") val themeMode: String = "system",
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant,
)

@Serializable
data class ProfileUpdateRequest(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String? = null,
    @SerialName("notification_opt_in") val notificationOptIn: Boolean? = null,
    @SerialName("theme_mode") val themeMode: String? = null,
)
// endregion

// region Catalog DTOs
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("created_at")
    @Serializable(with = InstantIsoSerializer::class)
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant? = null,
)

@Serializable
data class CategoryUpsertRequest(
    val name: String,
    val description: String? = null,
    val metadata: JsonObject? = null,
)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val category: CategoryDto? = null,
    val metadata: JsonObject? = null,
    val unit: String? = null,
    @SerialName("default_quantity") val defaultQuantity: Double = 1.0,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("created_at")
    @Serializable(with = InstantIsoSerializer::class)
    val createdAt: Instant,
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant,
)

@Serializable
data class ProductUpsertRequest(
    val name: String,
    val description: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val unit: String? = null,
    @SerialName("default_quantity") val defaultQuantity: Double = 1.0,
    val metadata: JsonObject? = null,
)
// endregion

// region Shopping Lists DTOs
@Serializable
data class ShoppingListDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val owner: UserSummaryDto? = null,
    @SerialName("shared_users") val sharedUsers: List<UserSummaryDto> = emptyList(),
    val metadata: JsonObject? = null,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    val recurring: Boolean? = null,
    @SerialName("created_at")
    @Serializable(with = InstantIsoSerializer::class)
    val createdAt: Instant,
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant,
    @SerialName("last_purchased_at")
    @Serializable(with = InstantIsoSerializer::class)
    val lastPurchasedAt: Instant? = null,
    val items: List<ShoppingListItemDto> = emptyList(),
)

@Serializable
data class ShoppingListUpsertRequest(
    val name: String,
    val description: String? = null,
    @SerialName("shared_user_ids") val sharedUserIds: List<String> = emptyList(),
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    val metadata: JsonObject? = null,
)

@Serializable
data class ShoppingListItemDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_name") val productName: String? = null,
    val quantity: Double,
    val unit: String? = null,
    @SerialName("purchased") val purchased: Boolean = false,
    @SerialName("is_acquired") val isAcquired: Boolean? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("pantry_id") val pantryId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("created_at")
    @Serializable(with = InstantIsoSerializer::class)
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant? = null,
)

@Serializable
data class ShoppingListItemUpsertRequest(
    @SerialName("product_id") val productId: String,
    val quantity: Double,
    val unit: String? = null,
    val metadata: JsonObject? = null,
)

@Serializable
data class ShoppingListItemPatchRequest(
    val quantity: Double? = null,
    val unit: String? = null,
    @SerialName("purchased") val purchased: Boolean? = null,
)

@Serializable
data class ShoppingListPurchaseRequest(
    @SerialName("purchased_at")
    @Serializable(with = InstantIsoSerializer::class)
    val purchasedAt: Instant = Instant.now(),
)

@Serializable
data class ShoppingListResetRequest(
    val reason: String? = null,
)

@Serializable
data class MoveListToPantryRequest(
    @SerialName("pantry_id") val pantryId: String,
    val notes: String? = null,
)

@Serializable
data class ShareListRequest(
    val recipients: List<String>,
    val message: String? = null,
)

@Serializable
data class PantryTransferResponse(
    @SerialName("pantry_id") val pantryId: String,
    val items: List<PantryItemDto>,
)
// endregion

// region Pantry DTOs
@Serializable
data class PantryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val owner: UserSummaryDto? = null,
    @SerialName("shared_users") val sharedUsers: List<UserSummaryDto> = emptyList(),
    val metadata: JsonObject? = null,
    @SerialName("created_at")
    @Serializable(with = InstantIsoSerializer::class)
    val createdAt: Instant,
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant,
    val items: List<PantryItemDto> = emptyList(),
)

@Serializable
data class PantryUpsertRequest(
    val name: String,
    val description: String? = null,
    @SerialName("shared_user_ids") val sharedUserIds: List<String> = emptyList(),
    val metadata: JsonObject? = null,
)

@Serializable
data class PantryItemDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_name") val productName: String? = null,
    val quantity: Double,
    val unit: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("pantry_id") val pantryId: String? = null,
    val metadata: JsonObject? = null,
    @SerialName("expiration_date")
    @Serializable(with = InstantIsoSerializer::class)
    val expirationDate: Instant? = null,
    @SerialName("created_at")
    @Serializable(with = InstantIsoSerializer::class)
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    @Serializable(with = InstantIsoSerializer::class)
    val updatedAt: Instant? = null,
)

@Serializable
data class PantryItemUpsertRequest(
    @SerialName("product_id") val productId: String,
    val quantity: Double,
    val unit: String? = null,
    @SerialName("expiration_date")
    @Serializable(with = InstantIsoSerializer::class)
    val expirationDate: Instant? = null,
    val metadata: JsonObject? = null,
)

@Serializable
data class SharePantryRequest(
    val recipients: List<String>,
    val message: String? = null,
)
// endregion

// region Purchases DTOs
@Serializable
data class PurchaseDto(
    val id: String,
    @SerialName("list_id") val listId: String,
    val list: ShoppingListDto? = null,
    @SerialName("purchased_at")
    @Serializable(with = InstantIsoSerializer::class)
    val purchasedAt: Instant,
    @SerialName("restored_at")
    @Serializable(with = InstantIsoSerializer::class)
    val restoredAt: Instant? = null,
    val metadata: JsonObject? = null,
)
// endregion

// region Shared responses
@Serializable
data class ApiListResponse<T>(
    val data: List<T>,
    val page: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
    val total: Int? = null,
)
// endregion
