package com.comprartir.mobile.profile.data

import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.ProfileDao
import com.comprartir.mobile.core.database.dao.UserDao
import com.comprartir.mobile.core.database.entity.ProfileEntity
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.ProfileUpdateRequest
import com.comprartir.mobile.profile.domain.AppLanguage
import com.comprartir.mobile.profile.domain.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val theme: AppTheme = AppTheme.SYSTEM,
    val bio: String? = null,
)

interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun updateProfile(userProfile: UserProfile)
    suspend fun refresh()
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val api: ComprartirApi,
    private val profileDao: ProfileDao,
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
) : ProfileRepository {

    override val profile: Flow<UserProfile> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UserProfile())
            } else {
                profileDao.observeProfile(user.id).map { profileEntity ->
                    val nameParts = user.displayName.split(" ", limit = 2)
                    val firstName = nameParts.getOrNull(0) ?: ""
                    val lastName = nameParts.getOrNull(1) ?: ""
                    
                    UserProfile(
                        userId = user.id,
                        name = firstName,
                        surname = lastName,
                        email = user.email,
                        photoUrl = user.photoUrl,
                        phoneNumber = profileEntity?.phoneNumber,
                        language = AppLanguage.fromCode(profileEntity?.preferredLanguage),
                        theme = AppTheme.fromCode(profileEntity?.themeMode),
                        bio = profileEntity?.bio,
                    )
                }
            }
        }
        .flowOn(Dispatchers.IO)

    override suspend fun updateProfile(userProfile: UserProfile) = withContext(Dispatchers.IO) {
        // Update profile via API - returns updated User with name/surname changes
        val updatedUser = api.updateProfile(
            ProfileUpdateRequest(
                name = userProfile.name,
                surname = userProfile.surname,
                metadata = buildJsonObject {
                    // Store language and theme in metadata since API doesn't have direct fields for them
                    if (userProfile.language != AppLanguage.SYSTEM) {
                        put("preferredLanguage", userProfile.language.code)
                    }
                    if (userProfile.theme != AppTheme.SYSTEM) {
                        put("themeMode", userProfile.theme.code)
                    }
                }
            )
        )

        // Save the updated user directly (includes name, surname, email, etc.)
        userDao.upsert(updatedUser.toEntity())

        // Also fetch and update ProfileEntity with latest preferences
        try {
            val profileDto = api.fetchProfile()
            val profileEntity = ProfileEntity(
                userId = profileDto.userId,
                bio = profileDto.bio,
                phoneNumber = profileDto.phoneNumber,
                preferredLanguage = profileDto.preferredLanguage,
                notificationOptIn = profileDto.notificationOptIn,
                themeMode = profileDto.themeMode,
                updatedAt = profileDto.updatedAt,
            )
            profileDao.upsert(profileEntity)
        } catch (e: Exception) {
            // If fetching profile fails, continue anyway - we already updated the user
            println("ProfileRepository: Failed to fetch profile after update: ${e.message}")
        }
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val profileDto = api.fetchProfile()
        val profileEntity = ProfileEntity(
            userId = profileDto.userId,
            bio = profileDto.bio,
            phoneNumber = profileDto.phoneNumber,
            preferredLanguage = profileDto.preferredLanguage,
            notificationOptIn = profileDto.notificationOptIn,
            themeMode = profileDto.themeMode,
            updatedAt = profileDto.updatedAt,
        )
        profileDao.upsert(profileEntity)
    }
}
