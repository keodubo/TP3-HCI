package com.comprartir.mobile.auth.data

import com.comprartir.mobile.core.data.datastore.AuthTokenRepository
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.ProfileDao
import com.comprartir.mobile.core.database.dao.UserDao
import com.comprartir.mobile.core.database.entity.UserEntity
import com.comprartir.mobile.core.network.AuthResponse
import com.comprartir.mobile.core.network.ChangePasswordRequest
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.LoginRequest
import com.comprartir.mobile.core.network.RegisterRequest
import com.comprartir.mobile.core.network.VerifyAccountRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val isVerified: Boolean,
)

interface AuthRepository {
    val currentUser: Flow<UserAccount?>
    val isAuthenticated: Flow<Boolean>

    suspend fun register(email: String, password: String)
    suspend fun verify(code: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    suspend fun updatePassword(currentPassword: String, newPassword: String)
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: ComprartirApi,
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    private val authTokenRepository: AuthTokenRepository,
) : AuthRepository {

    override val currentUser: Flow<UserAccount?> = userDao.observeCurrentUser()
        .map { entity -> entity?.toAccount() }

    override val isAuthenticated: Flow<Boolean> = currentUser.map { it != null }

    override suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        val response = api.register(
            RegisterRequest(
                email = email,
                password = password,
                displayName = email.substringBefore("@"),
            )
        )
        persistAuth(response)
    }

    override suspend fun verify(code: String) = withContext(Dispatchers.IO) {
        val pendingUser = userDao.observeCurrentUser().firstOrNull()
            ?: throw IllegalStateException("No pending user to verify")
        val response = api.verifyAccount(
            VerifyAccountRequest(
                email = pendingUser.email,
                code = code,
            )
        )
        persistAuth(response)
    }

    override suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        val response = api.login(LoginRequest(email = email, password = password))
        persistAuth(response)
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        runCatching { api.logout() }
        authTokenRepository.clearToken()
        userDao.clear()
        profileDao.clear()
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String) = withContext(Dispatchers.IO) {
        try {
            api.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                )
            )
        } catch (http: HttpException) {
            if (http.code() == 401) {
                authTokenRepository.clearToken()
            }
            throw http
        }
    }

    private suspend fun persistAuth(response: AuthResponse) {
        authTokenRepository.updateToken(response.token)
        userDao.upsert(response.user.toEntity())
    }

    private fun UserEntity.toAccount(): UserAccount = UserAccount(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        isVerified = isVerified,
    )
}
