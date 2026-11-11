package com.roozyar.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Profile data is stored on-device only. Email and phone are optional and independently hideable. */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "",
    val email: String? = null,
    val phone: String? = null,
    val showEmailInProfile: Boolean = false,
    val showPhoneInProfile: Boolean = false,
)
