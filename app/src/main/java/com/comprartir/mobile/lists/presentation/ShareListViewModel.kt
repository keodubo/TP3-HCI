package com.comprartir.mobile.lists.presentation

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comprartir.mobile.R
import com.comprartir.mobile.lists.data.SharedUser
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@HiltViewModel
class ShareListViewModel @Inject constructor(
    private val shoppingListsRepository: ShoppingListsRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val listId: String = savedStateHandle.get<String>("listId") ?: ""
    private val listNameArg: String = savedStateHandle.get<String>("listName") ?: ""

    private val _state = MutableStateFlow(
        ShareListUiState(
            listId = listId,
            shareLink = if (listId.isNotBlank()) shoppingListsRepository.getShareLink(listId) else "",
            listName = listNameArg,
        )
    )
    val state: StateFlow<ShareListUiState> = _state.asStateFlow()

    init {
        if (listId.isNotBlank()) {
            refreshSharedUsers()
        } else {
            _state.update { it.copy(errorMessage = "Lista inválida") }
        }
    }

    fun refreshSharedUsers() {
        if (listId.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { shoppingListsRepository.fetchSharedUsers(listId) }
                .onSuccess { users ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            sharedUsers = users.map { user -> user.toUi() },
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    val fallback = context.getString(R.string.share_list_load_error)
                    val friendly = throwable.toReadableMessage(fallback)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = friendly,
                        )
                    }
                }
        }
    }

    fun onInviteEmailChanged(email: String) {
        _state.update { it.copy(inviteEmail = email, errorMessage = null) }
    }

    fun onInviteClicked() {
        val email = state.value.inviteEmail.trim()
        if (listId.isBlank()) {
            _state.update { it.copy(errorMessage = "Lista inválida") }
            return
        }
        if (email.isBlank()) {
            _state.update { it.copy(errorMessage = null, infoMessageRes = R.string.list_detail_share_email_required) }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(errorMessage = null, infoMessageRes = R.string.list_detail_share_email_invalid) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isInviting = true, infoMessageRes = null) }
            runCatching { shoppingListsRepository.shareList(listId, email) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            inviteEmail = "",
                            isInviting = false,
                            shareLink = result.shareLink,
                            sharedUsers = result.sharedUsers.map { user -> user.toUi() },
                            infoMessageRes = R.string.list_detail_share_invite_success,
                        )
                    }
                }
                .onFailure { throwable ->
                    val fallback = context.getString(R.string.share_list_invite_error)
                    val friendly = throwable.toReadableMessage(fallback)
                    _state.update {
                        it.copy(
                            isInviting = false,
                            errorMessage = friendly,
                        )
                    }
                }
        }
    }

    fun onRemoveSharedUserClicked(userId: String) {
        if (listId.isBlank() || state.value.removingUserId == userId) return
        viewModelScope.launch {
            _state.update { it.copy(removingUserId = userId, errorMessage = null) }
            runCatching { shoppingListsRepository.revokeShare(listId, userId) }
                .onSuccess {
                    _state.update { it.copy(removingUserId = null, infoMessageRes = R.string.share_list_remove_success) }
                    refreshSharedUsers()
                }
                .onFailure { throwable ->
                    val fallback = context.getString(R.string.share_list_remove_error)
                    val friendly = throwable.toReadableMessage(fallback)
                    _state.update {
                        it.copy(
                            removingUserId = null,
                            errorMessage = friendly,
                        )
                    }
                }
        }
    }

    fun onMessageConsumed() {
        _state.update { it.copy(infoMessageRes = null) }
    }

    fun onErrorConsumed() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun SharedUser.toUi(): SharedUserUi = SharedUserUi(
        id = id,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
    )

    private fun Throwable.toReadableMessage(fallback: String): String {
        if (this is HttpException) {
            val rawBody = try {
                response()?.errorBody()?.string()
            } catch (exception: Exception) {
                null
            }
            if (!rawBody.isNullOrBlank()) {
                return try {
                    val json = JSONObject(rawBody)
                    val msg = json.optString("message")
                    if (msg.isNotBlank()) msg else rawBody
                } catch (_: Exception) {
                    rawBody
                }
            }
        }
        return message?.takeIf { it.isNotBlank() } ?: fallback
    }
}

data class ShareListUiState(
    val listId: String = "",
    val listName: String = "",
    val shareLink: String = "",
    val inviteEmail: String = "",
    val isInviting: Boolean = false,
    val isLoading: Boolean = true,
    val sharedUsers: List<SharedUserUi> = emptyList(),
    val removingUserId: String? = null,
    val errorMessage: String? = null,
    val infoMessageRes: Int? = null,
)

data class SharedUserUi(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
)
