package com.comprartir.mobile.profile.data

import android.util.Log
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.data.UserAccount
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.ProfileDao
import com.comprartir.mobile.core.database.dao.UserDao
import com.comprartir.mobile.core.database.entity.ProfileEntity
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.ProfileUpdateRequest
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val phoneNumber: String? = null,
    val preferredLanguage: String? = "es",
    val notificationOptIn: Boolean = true,
    val themeMode: String = "system",
)

interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun refresh()
    suspend fun updateProfile(profile: UserProfile)
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val userDao: UserDao,
    private val authRepository: AuthRepository,
    private val api: ComprartirApi,
) : ProfileRepository {

    private val lastSyncedUserId = AtomicReference<String?>(null)

    override val profile: Flow<UserProfile> = authRepository.currentUser.flatMapLatest { account ->
        if (account == null) {
            flowOf(UserProfile())
        } else {
            profileDao.observeProfile(account.id)
                .map { entity -> entity?.toDomain(account) ?: account.toDefaultProfile() }
                .onStart { ensureSynced(account) }
        }
    }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val account = authRepository.currentUser.firstOrNull() ?: return@withContext
            lastSyncedUserId.set(account.id)
            fetchProfile()
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            val payload = ProfileUpdateRequest(
                displayName = profile.name,
                phoneNumber = profile.phoneNumber,
                preferredLanguage = profile.preferredLanguage,
                notificationOptIn = profile.notificationOptIn,
                themeMode = profile.themeMode,
            )
            val dto = api.updateProfile(payload)
            profileDao.upsert(dto.toEntity())
            val currentUser = userDao.getCurrentUser()
            if (currentUser != null) {
                userDao.upsert(
                    currentUser.copy(
                        displayName = profile.name.ifBlank { currentUser.displayName },
                        updatedAt = dto.updatedAt,
                    )
                )
            }
        }
    }

    private suspend fun ensureSynced(account: UserAccount) {
        val previous = lastSyncedUserId.getAndSet(account.id)
        if (previous != account.id) {
            withContext(Dispatchers.IO) {
                fetchProfile()
            }
        }
    }

    private suspend fun fetchProfile() {
        runCatching { api.fetchProfile() }
            .onSuccess { dto -> profileDao.upsert(dto.toEntity()) }
            .onFailure { throwable -> Log.w(TAG, "Failed to fetch profile", throwable) }
    }

    private fun ProfileEntity.toDomain(account: UserAccount): UserProfile = UserProfile(
        id = userId,
        name = account.displayName,
        email = account.email,
        avatarUrl = account.photoUrl,
        phoneNumber = phoneNumber,
        preferredLanguage = preferredLanguage,
        notificationOptIn = notificationOptIn,
        themeMode = themeMode,
    )

    private fun UserAccount.toDefaultProfile(): UserProfile = UserProfile(
        id = id,
        name = displayName,
        email = email,
        avatarUrl = photoUrl,
        phoneNumber = null,
        preferredLanguage = "es",
        notificationOptIn = true,
        themeMode = "system",
    )

    companion object {
        private const val TAG = "ProfileRepository"
    }
}
