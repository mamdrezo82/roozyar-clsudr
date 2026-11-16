package com.roozyar.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roozyar.app.domain.model.UserProfile
import com.roozyar.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(private val repository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val p = repository.getProfile()
                _uiState.update { it.copy(isLoading = false, profile = p) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, error = "خواندن اطلاعات ممکن نشد.") }
            }
        }
    }

    private fun edit(block: (UserProfile) -> UserProfile) = _uiState.update { it.copy(profile = block(it.profile)) }

    fun onName(v: String) = edit { it.copy(displayName = v) }
    fun onBio(v: String) = edit { it.copy(bio = v) }
    fun onEmail(v: String) = edit { it.copy(email = v) }
    fun onPhone(v: String) = edit { it.copy(phone = v) }
    fun onShowEmail(v: Boolean) = edit { it.copy(showEmail = v) }
    fun onShowPhone(v: Boolean) = edit { it.copy(showPhone = v) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val p = s.profile
                repository.saveProfile(p.copy(displayName = p.displayName.trim(), bio = p.bio.trim(), email = p.email.trim(), phone = p.phone.trim()))
                _uiState.update { it.copy(isSaving = false, done = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isSaving = false, error = "ذخیره‌سازی انجام نشد.") }
            }
        }
    }
}
