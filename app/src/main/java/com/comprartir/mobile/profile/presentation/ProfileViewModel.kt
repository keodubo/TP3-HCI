package com.comprartir.mobile.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.profile.data.ProfileRepository
import com.comprartir.mobile.profile.data.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.profile.collect { profile ->
                _state.update { it.copy(profile = profile, isLoading = false) }
            }
        }
        refresh()
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(profile = it.profile.copy(name = name)) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(profile = it.profile.copy(email = email)) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { repository.updateProfile(_state.value.profile) }
                .onSuccess {
                    _state.update { it.copy(snackbarMessageRes = R.string.profile_saved) }
                }
                .onFailure { throwable ->
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun onSnackbarConsumed() {
        _state.update { it.copy(snackbarMessageRes = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.refresh() }
                .onFailure { throwable ->
                    _state.update { current -> current.copy(errorMessage = throwable.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val snackbarMessageRes: Int? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
