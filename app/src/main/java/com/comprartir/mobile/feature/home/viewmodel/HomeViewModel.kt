package com.comprartir.mobile.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.feature.home.model.ActivityUi
import com.comprartir.mobile.feature.home.model.HomeUiState
import com.comprartir.mobile.feature.home.model.RecentListUi
import com.comprartir.mobile.feature.home.model.SharedListUi
import com.comprartir.mobile.lists.data.ShoppingListsRepository as ListsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    @Suppress("unused")
    private val listsRepository: ListsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // TODO: Replace with listsRepository.observeLists() once backend integration is ready.
                delay(500)
                val userName = authRepository.currentUser.firstOrNull()?.displayName.orEmpty()
                _uiState.update { current ->
                    current.copy(
                        userName = userName,
                        recentLists = sampleRecentLists(),
                        sharedLists = sampleSharedLists(),
                        recentActivity = sampleActivity(),
                        isLoading = false,
                    )
                }
            } catch (throwable: Throwable) {
                _uiState.update { current ->
                    current.copy(isLoading = false, error = throwable.message)
                }
            }
        }
    }

    private fun sampleRecentLists(): List<RecentListUi> = listOf(
        RecentListUi(
            id = "recent-1",
            name = "Compras semanales",
            date = "Hace 2 días",
            itemCount = 18,
            status = "En progreso",
            isShared = true,
        ),
        RecentListUi(
            id = "recent-2",
            name = "Frutas y verduras",
            date = "Ayer",
            itemCount = 9,
            status = "Completada",
            isShared = false,
        ),
        RecentListUi(
            id = "recent-3",
            name = "Fiesta sábado",
            date = "Hace 4 días",
            itemCount = 24,
            status = "Pendiente",
            isShared = true,
        ),
    )

    private fun sampleSharedLists(): List<SharedListUi> = listOf(
        SharedListUi(
            id = "shared-1",
            name = "Mudanza Abril",
            ownerName = "Rocío García",
            lastUpdated = "Actualizada hace 1 h",
            avatarUrl = null,
        ),
        SharedListUi(
            id = "shared-2",
            name = "Club de lectura",
            ownerName = "Federico Díaz",
            lastUpdated = "Actualizada ayer",
            avatarUrl = null,
        ),
    )

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
