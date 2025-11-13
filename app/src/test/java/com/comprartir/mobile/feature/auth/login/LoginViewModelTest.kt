package com.comprartir.mobile.feature.auth.login

import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.data.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun buttonDisabledWhenFieldsAreBlank() {
        assertFalse(viewModel.uiState.value.isLoginEnabled)
    }

    @Test
    fun buttonEnabledWhenEmailAndPasswordProvided() {
        viewModel.onEvent(LoginEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("safePass123"))

        assertTrue(viewModel.uiState.value.isLoginEnabled)
    }

    @Test
    fun buttonDisabledAgainWhenEmailCleared() {
        viewModel.onEvent(LoginEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("safePass123"))
        viewModel.onEvent(LoginEvent.EmailChanged(""))

        assertFalse(viewModel.uiState.value.isLoginEnabled)
    }

    @Test
    fun submitInvokesCallbackAndResetsLoading() = runTest {
        viewModel.onEvent(LoginEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("safePass123"))
        var callbackInvoked = false

        viewModel.onEvent(LoginEvent.Submit { callbackInvoked = true })

        advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(repository.isAuthenticatedValue)
    }

    private class FakeAuthRepository : AuthRepository {

        private val internalUser = MutableStateFlow<UserAccount?>(null)

        val isAuthenticatedValue: Boolean
            get() = internalUser.value?.isVerified == true

        override val currentUser: Flow<UserAccount?> = internalUser

        override val isAuthenticated: Flow<Boolean> = internalUser.map { user ->
            user?.isVerified == true
        }

        override suspend fun register(email: String, password: String, name: String, surname: String) {
            // No-op for tests
        }

        override suspend fun verify(email: String, code: String) {
            // No-op for tests
        }

        override suspend fun signIn(email: String, password: String) {
            internalUser.value = UserAccount(
                id = "id",
                email = email,
                displayName = "Test User",
                photoUrl = null,
                isVerified = true,
            )
        }

        override suspend fun signOut() {
            internalUser.value = null
        }

        override suspend fun updatePassword(currentPassword: String, newPassword: String) {
            // No-op for tests
        }

        override suspend fun sendPasswordRecoveryCode(email: String) {
            // No-op for tests
        }

        override suspend fun resetPassword(email: String, resetToken: String, newPassword: String) {
            // No-op for tests
        }
    }
}
