package com.comprartir.mobile.profile.data

import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.ProfileUpdateRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val name: String = "",
    val email: String = "",
)

interface ProfileRepository {
    val profile: Flow<UserProfile>
    suspend fun updateProfile(userProfile: UserProfile)
    suspend fun refresh()
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val api: ComprartirApi
) : ProfileRepository {

    override val profile: Flow<UserProfile>
        get() = TODO("Not yet implemented")

    override suspend fun updateProfile(userProfile: UserProfile) {
        api.updateProfile(ProfileUpdateRequest(displayName = userProfile.name))
    }

    override suspend fun refresh() {
        // Not yet implemented
    }
}