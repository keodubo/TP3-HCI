package com.comprartir.mobile.profile.data

import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.core.database.dao.ProfileDao
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
        val displayName = "${userProfile.name} ${userProfile.surname}".trim()
        
        // Update profile via API
        api.updateProfile(
            ProfileUpdateRequest(
                displayName = displayName,
                phoneNumber = userProfile.phoneNumber,
                preferredLanguage = if (userProfile.language != AppLanguage.SYSTEM) 
                    userProfile.language.code else null,
                themeMode = if (userProfile.theme != AppTheme.SYSTEM) 
                    userProfile.theme.code else null,
            )
        )
        
        // Refresh to get updated data
        refresh()
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