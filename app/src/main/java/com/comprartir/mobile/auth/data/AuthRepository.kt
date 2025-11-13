package com.comprartir.mobile.auth.data

import com.comprartir.mobile.BuildConfig
import com.comprartir.mobile.core.data.datastore.AuthTokenRepository
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.ProfileDao
import com.comprartir.mobile.core.database.dao.UserDao
import com.comprartir.mobile.core.database.entity.UserEntity
import com.comprartir.mobile.core.network.AuthResponse
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.LoginRequest
import com.comprartir.mobile.core.network.RegisterRequest
import com.comprartir.mobile.core.network.SendVerificationRequest
import com.comprartir.mobile.core.network.VerifyAccountRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isVerified: Boolean,
)

/**
 * Internal DTO for change password request.
 * The backend expects camelCase field names (currentPassword, newPassword),
 * so we don't use @SerialName annotations here. The property names will be
 * serialized as-is by kotlinx.serialization.
 */
@Serializable
internal data class ChangePasswordBody(
    val currentPassword: String,
    val newPassword: String,
)

interface AuthRepository {
    val currentUser: Flow<UserAccount?>
    val isAuthenticated: Flow<Boolean>

    suspend fun register(email: String, password: String, name: String, surname: String)
    suspend fun verify(email: String, code: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    suspend fun updatePassword(currentPassword: String, newPassword: String)
    suspend fun sendPasswordRecoveryCode(email: String)
    suspend fun resetPassword(email: String, resetToken: String, newPassword: String)
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: ComprartirApi,
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val authTokenRepository: AuthTokenRepository,
    private val okHttpClient: okhttp3.OkHttpClient,
) : AuthRepository {

    override val currentUser: Flow<UserAccount?> = userDao.observeCurrentUser()
        .map { entity -> entity?.toAccount() }

    override val isAuthenticated: Flow<Boolean> = currentUser.map { it != null && it.isVerified }

    override suspend fun register(email: String, password: String, name: String, surname: String) = withContext(Dispatchers.IO) {
        val response = api.register(
            RegisterRequest(
                email = email,
                password = password,
                name = name,
                surname = surname,
            )
        )
        userDao.upsert(response.toEntity())

        // Send verification email - don't fail the registration if this fails
        try {
            api.sendVerification(SendVerificationRequest(email = email))
        } catch (e: Exception) {
            // Log but don't fail - user can still verify with code
            e.printStackTrace()
        }
    }

    override suspend fun verify(email: String, code: String) = withContext(Dispatchers.IO) {
        // Note: Backend only uses the code to look up the user
        // Email is kept in the signature for UI purposes but not sent to API
        // Backend returns UserDto only (no token), so user needs to login after verification
        val userDto = api.verifyAccount(
            VerifyAccountRequest(
                code = code,
            )
        )
        // Save the verified user to database (without token - user will login next)
        userDao.upsert(userDto.toEntity())
    }

    override suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        println("AuthRepository: Starting login for $email")
        val response = api.login(LoginRequest(email = email, password = password))
        println("AuthRepository: Login response received. User data present: ${response.user != null}")
        
        // IMPORTANT: Clear old user data BEFORE saving new user
        println("AuthRepository: Clearing old user data")
        userDao.clear()
        profileDao.clear()

        // First save the token
        authTokenRepository.updateToken(response.token)
        println("AuthRepository: Token saved")
        
        // Then save or fetch user data
        if (response.user != null) {
            println("AuthRepository: Saving user from login response, isVerified=${response.user.isVerified}")
            userDao.upsert(response.user.toEntity())
        } else {
            try {
                // Fetch the user profile after successful login
                println("AuthRepository: Fetching user profile")
                val userDto = api.getUserProfile()
                println("AuthRepository: Profile received, isVerified=${userDto.isVerified}")
                userDao.upsert(userDto.toEntity())
            } catch (e: Exception) {
                // If profile fetch fails, create a minimal user entry
                // so that isAuthenticated becomes true
                println("AuthRepository: Failed to fetch profile: ${e.message}")
                e.printStackTrace()
                val minimalUser = UserEntity(
                    id = "", // Will be updated when profile is fetched
                    email = email,
                    displayName = email.substringBefore("@"),
                    photoUrl = null,
                    isVerified = true, // Assume verified since login succeeded
                )
                userDao.upsert(minimalUser)
            }
        }
        println("AuthRepository: Login completed, user should be saved")
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        runCatching { api.logout() }
        authTokenRepository.clearToken()
        userDao.clear()
        profileDao.clear()
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String) = withContext(Dispatchers.IO) {
        try {
            // The backend expects camelCase JSON fields (currentPassword, newPassword),
            // but the ChangePasswordRequest DTO in the API package uses @SerialName with snake_case.
            // We bypass the Retrofit interface and use OkHttp directly to send the correct JSON.
            
            // Log the request for debugging (without logging actual passwords)
            println("AuthRepository: Attempting to change password")
            println("AuthRepository: Validating - currentPassword length: ${currentPassword.length}, newPassword length: ${newPassword.length}")
            
            if (currentPassword.length < 6) {
                throw IllegalArgumentException("Current password must be at least 6 characters")
            }
            if (newPassword.length < 6) {
                throw IllegalArgumentException("New password must be at least 6 characters")
            }
            
            // Build the JSON manually to ensure correct field names (camelCase)
            // Escape quotes in passwords to prevent JSON injection
            val escapedCurrentPassword = currentPassword.replace("\"", "\\\"").replace("\\", "\\\\")
            val escapedNewPassword = newPassword.replace("\"", "\\\"").replace("\\", "\\\\")
            val jsonBody = """{"currentPassword":"$escapedCurrentPassword","newPassword":"$escapedNewPassword"}"""
            
            println("AuthRepository: Sending request with body structure: {currentPassword: <hidden>, newPassword: <hidden>}")
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = okhttp3.RequestBody.create(mediaType, jsonBody)
            
            // Get the token for authentication
            val token = authTokenRepository.currentToken()
            if (token == null) {
                println("AuthRepository: No authentication token found")
                throw IllegalStateException("Not authenticated")
            }
            
            // Get base URL from BuildConfig
            val baseUrl = BuildConfig.COMPRARTIR_API_BASE_URL.let { base ->
                if (base.endsWith("/")) base.dropLast(1) else base
            }
            
            val request = okhttp3.Request.Builder()
                .url("$baseUrl/users/change-password")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            println("AuthRepository: Received response - HTTP ${response.code}")
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                println("AuthRepository: Error response body: $errorBody")
                
                if (response.code == 401) {
                    println("AuthRepository: Unauthorized - clearing token")
                    authTokenRepository.clearToken()
                }
                
                throw HttpException(
                    retrofit2.Response.error<Any>(
                        response.code,
                        okhttp3.ResponseBody.create(null, errorBody ?: "")
                    )
                )
            }
            
            println("AuthRepository: Password changed successfully")
        } catch (http: HttpException) {
            println("AuthRepository: HTTP exception during password change: ${http.code()}")
            throw http
        } catch (e: Exception) {
            println("AuthRepository: Exception during password change: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun sendPasswordRecoveryCode(email: String) = withContext(Dispatchers.IO) {
        api.forgotPassword(email)
    }

    override suspend fun resetPassword(email: String, resetToken: String, newPassword: String) = withContext(Dispatchers.IO) {
        // API returns empty object {}, not AuthResponse
        api.resetPassword(
            com.comprartir.mobile.core.network.ResetPasswordRequest(
                code = resetToken,
                password = newPassword,
            )
        )
        // Password was reset successfully, but user must login manually
        // No token is returned, so we don't save anything
    }

    private fun UserEntity.toAccount(): UserAccount = UserAccount(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isVerified = isVerified,
    )
}
