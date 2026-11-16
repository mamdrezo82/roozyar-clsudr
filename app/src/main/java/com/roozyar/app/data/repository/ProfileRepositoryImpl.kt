package com.roozyar.app.data.repository

import com.roozyar.app.core.database.UserProfileDao
import com.roozyar.app.core.database.UserProfileEntity
import com.roozyar.app.domain.model.UserProfile
import com.roozyar.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

fun UserProfileEntity?.toDomain(): UserProfile =
    if (this == null) UserProfile() else UserProfile(displayName, bio, email.orEmpty(), phone.orEmpty(), showEmailInProfile, showPhoneInProfile)

fun UserProfile.toEntity() = UserProfileEntity(
    id = 1,
    displayName = displayName,
    bio = bio,
    email = email.ifBlank { null },
    phone = phone.ifBlank { null },
    showEmailInProfile = showEmail,
    showPhoneInProfile = showPhone,
)

class ProfileRepositoryImpl @Inject constructor(private val dao: UserProfileDao) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile> = dao.observeProfile().map { it.toDomain() }

    override suspend fun getProfile(): UserProfile = dao.observeProfile().first().toDomain()

    override suspend fun saveProfile(profile: UserProfile) = dao.save(profile.toEntity())
}
