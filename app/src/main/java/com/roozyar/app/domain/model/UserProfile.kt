package com.roozyar.app.domain.model

data class UserProfile(
    val displayName: String = "",
    val bio: String = "",
    val email: String = "",
    val phone: String = "",
    val showEmail: Boolean = false,
    val showPhone: Boolean = false,
)
