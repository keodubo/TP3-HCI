package com.comprartir.mobile.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.feature.home.domain.GetHomeListsUseCase
import com.comprartir.mobile.feature.home.model.ActivityUi
import com.comprartir.mobile.feature.home.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeListsUseCase: GetHomeListsUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeLists()
    }

    private fun observeLists() {
        viewModelScope.launch {
            combine(
                getHomeListsUseCase(),
                authRepository.currentUser,
            ) { listsData, user ->
                HomeUiState(
                    userName = user?.displayName.orEmpty(),
                    recentLists = listsData.recentLists,
                    sharedLists = listsData.sharedLists,
                    recentActivity = sampleActivity(), // Activity still using sample data
                    isLoading = false,
                    error = null,
                )
            }
                .catch { throwable ->
                    _uiState.update { current ->
                        current.copy(isLoading = false, error = throwable.message ?: "Error desconocido")
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getHomeListsUseCase.refresh()
                // The flow will automatically update with new data
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(isLoading = false, error = throwable.message ?: "Error al actualizar")
                }
            }
        }
    }

    private fun sampleActivity(): List<ActivityUi> = listOf(
        ActivityUi(
            id = "activity-1",
            description = "Marcaste 4 productos como adquiridos",
            timestamp = "Hoy · 10:24",
            iconType = "check",
        ),
        ActivityUi(
            id = "activity-2",
            description = "Rocío agregó \"Yerba mate\" a Mudanza Abril",
            timestamp = "Ayer · 18:02",
            iconType = "share",
        ),
        ActivityUi(
            id = "activity-3",
            description = "Se creó la lista \"Frutas y verduras\"",
            timestamp = "Esta semana",
            iconType = "list",
        ),
    )
}
