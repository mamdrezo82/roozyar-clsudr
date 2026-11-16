package com.roozyar.app.domain.repository

import com.roozyar.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /** Emits an empty profile until one has been saved. */
    fun observeProfile(): Flow<UserProfile>
    suspend fun getProfile(): UserProfile
    suspend fun saveProfile(profile: UserProfile)
}
