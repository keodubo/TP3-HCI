package com.comprartir.mobile.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.core.data.datastore.UserPreferencesDataSource
import com.comprartir.mobile.profile.data.ProfileRepository
import com.comprartir.mobile.profile.data.UserProfile
import com.comprartir.mobile.profile.domain.AppLanguage
import com.comprartir.mobile.profile.domain.AppTheme
import com.comprartir.mobile.profile.domain.FieldError
import com.comprartir.mobile.profile.domain.ProfileField
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesDataSource: UserPreferencesDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()
    
    private var originalProfile: UserProfile = UserProfile()

    init {
        viewModelScope.launch {
            combine(
                repository.profile,
                userPreferencesDataSource.userPreferences().map { AppLanguage.fromCode(it.languageOverride) },
            ) { profile, languagePreference ->
                profile.copy(language = languagePreference)
            }.collect { mergedProfile ->
                originalProfile = mergedProfile
                if (!_state.value.isEditing) {
                    _state.update {
                        it.copy(
                            currentProfile = mergedProfile,
                            isLoading = false,
                        )
                    }
                }
            }
        }
        refresh()
    }

    fun onEditClicked() {
        _state.update { 
            it.copy(
                isEditing = true,
                currentProfile = originalProfile,
                fieldErrors = emptyMap()
            ) 
        }
    }

    fun onCancelEdit() {
        _state.update { 
            it.copy(
                isEditing = false,
                currentProfile = originalProfile,
                fieldErrors = emptyMap()
            ) 
        }
    }

    fun onNameChanged(name: String) {
        _state.update { current ->
            val newProfile = current.currentProfile.copy(name = name)
            val errors = validateField(ProfileField.NAME, name, current.fieldErrors)
            current.copy(
                currentProfile = newProfile,
                fieldErrors = errors,
                hasUnsavedChanges = newProfile != originalProfile
            )
        }
    }

    fun onSurnameChanged(surname: String) {
        _state.update { current ->
            val newProfile = current.currentProfile.copy(surname = surname)
            val errors = validateField(ProfileField.SURNAME, surname, current.fieldErrors)
            current.copy(
                currentProfile = newProfile,
                fieldErrors = errors,
                hasUnsavedChanges = newProfile != originalProfile
            )
        }
    }

    fun onLanguageChanged(language: AppLanguage) {
        _state.update { current ->
            val newProfile = current.currentProfile.copy(language = language)
            current.copy(
                currentProfile = newProfile,
                hasUnsavedChanges = newProfile != originalProfile
            )
        }
        persistLanguagePreference(language)
    }

    fun onThemeChanged(theme: AppTheme) {
        _state.update { current ->
            val newProfile = current.currentProfile.copy(theme = theme)
            current.copy(
                currentProfile = newProfile,
                hasUnsavedChanges = newProfile != originalProfile
            )
        }
    }

    fun onSaveClicked() {
        val currentState = _state.value
        
        // Validate all fields before saving
        val allErrors = validateAllFields(currentState.currentProfile)
        if (allErrors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = allErrors) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, snackbarMessage = null) }
            
            runCatching { 
                repository.updateProfile(currentState.currentProfile) 
            }
            .onSuccess {
                originalProfile = currentState.currentProfile
                _state.update { 
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        hasUnsavedChanges = false,
                        snackbarMessage = R.string.profile_updated_success,
                        fieldErrors = emptyMap()
                    ) 
                }
            }
            .onFailure { throwable ->
                _state.update { 
                    it.copy(
                        isSaving = false,
                        snackbarMessage = R.string.profile_update_error
                    ) 
                }
            }
        }
    }

    fun onSnackbarConsumed() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { repository.refresh() }
                .onFailure { 
                    // Silently fail, data will still come from cache
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun validateField(
        field: ProfileField, 
        value: String, 
        currentErrors: Map<ProfileField, Int>
    ): Map<ProfileField, Int> {
        val errors = currentErrors.toMutableMap()
        
        when (field) {
            ProfileField.NAME -> {
                when {
                    value.isBlank() -> errors[field] = R.string.profile_error_name_empty
                    value.length < 2 -> errors[field] = R.string.profile_error_name_too_short
                    else -> errors.remove(field)
                }
            }
            ProfileField.SURNAME -> {
                when {
                    value.isBlank() -> errors[field] = R.string.profile_error_surname_empty
                    value.length < 2 -> errors[field] = R.string.profile_error_surname_too_short
                    else -> errors.remove(field)
                }
            }
        }
        
        return errors
    }

    private fun validateAllFields(profile: UserProfile): Map<ProfileField, Int> {
        val errors = mutableMapOf<ProfileField, Int>()
        
        if (profile.name.isBlank()) {
            errors[ProfileField.NAME] = R.string.profile_error_name_empty
        } else if (profile.name.length < 2) {
            errors[ProfileField.NAME] = R.string.profile_error_name_too_short
        }
        
        if (profile.surname.isBlank()) {
            errors[ProfileField.SURNAME] = R.string.profile_error_surname_empty
        } else if (profile.surname.length < 2) {
            errors[ProfileField.SURNAME] = R.string.profile_error_surname_too_short
        }
        
        return errors
    }

    private fun persistLanguagePreference(language: AppLanguage) {
        viewModelScope.launch {
            val languageTag = language.code.takeIf { language != AppLanguage.SYSTEM }
            userPreferencesDataSource.updateLanguage(languageTag)
        }
    }

    fun onLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                authRepository.signOut()
            }.onSuccess {
                onComplete()
            }.onFailure {
                // Silently fail, but still navigate to login
                onComplete()
            }
        }
    }
}

data class ProfileUiState(
    val currentProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val fieldErrors: Map<ProfileField, Int> = emptyMap(),
    val snackbarMessage: Int? = null,
)
