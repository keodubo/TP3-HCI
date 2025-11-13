package com.comprartir.mobile.profile.presentation

import com.comprartir.mobile.R
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ChangePasswordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = ChangePasswordViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields and no errors`() {
        val state = viewModel.state.value
        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertNull(state.currentPasswordError)
        assertNull(state.newPasswordError)
        assertNull(state.confirmPasswordError)
        assertFalse(state.isLoading)
        assertFalse(state.isPasswordChanged)
    }

    @Test
    fun `updating current password clears errors`() {
        viewModel.onCurrentPasswordChanged("newPass")
        val state = viewModel.state.value
        assertEquals("newPass", state.currentPassword)
        assertNull(state.currentPasswordError)
        assertNull(state.generalError)
    }

    @Test
    fun `updating new password clears errors`() {
        viewModel.onNewPasswordChanged("newPassword123")
        val state = viewModel.state.value
        assertEquals("newPassword123", state.newPassword)
        assertNull(state.newPasswordError)
    }

    @Test
    fun `updating confirm password clears errors`() {
        viewModel.onConfirmPasswordChanged("newPassword123")
        val state = viewModel.state.value
        assertEquals("newPassword123", state.confirmPassword)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `toggle current password visibility works`() {
        assertFalse(viewModel.state.value.showCurrentPassword)
        viewModel.onToggleCurrentPasswordVisibility()
        assertTrue(viewModel.state.value.showCurrentPassword)
        viewModel.onToggleCurrentPasswordVisibility()
        assertFalse(viewModel.state.value.showCurrentPassword)
    }

    @Test
    fun `toggle new password visibility works`() {
        assertFalse(viewModel.state.value.showNewPassword)
        viewModel.onToggleNewPasswordVisibility()
        assertTrue(viewModel.state.value.showNewPassword)
    }

    @Test
    fun `toggle confirm password visibility works`() {
        assertFalse(viewModel.state.value.showConfirmPassword)
        viewModel.onToggleConfirmPasswordVisibility()
        assertTrue(viewModel.state.value.showConfirmPassword)
    }

    @Test
    fun `validation fails when current password is empty`() {
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onConfirmPasswordChanged("newPass123")
        viewModel.onSavePassword()

        val state = viewModel.state.value
        assertEquals(R.string.change_password_error_current_empty, state.currentPasswordError)
        assertFalse(state.isPasswordChanged)
    }

    @Test
    fun `validation fails when new password is empty`() {
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onConfirmPasswordChanged("something")
        viewModel.onSavePassword()

        val state = viewModel.state.value
        assertEquals(R.string.change_password_error_new_empty, state.newPasswordError)
        assertFalse(state.isPasswordChanged)
    }

    @Test
    fun `validation fails when new password is too short`() {
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onNewPasswordChanged("short")
        viewModel.onConfirmPasswordChanged("short")
        viewModel.onSavePassword()

        val state = viewModel.state.value
        assertEquals(R.string.change_password_error_new_too_short, state.newPasswordError)
        assertFalse(state.isPasswordChanged)
    }

    @Test
    fun `validation fails when confirm password is empty`() {
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onSavePassword()

        val state = viewModel.state.value
        assertEquals(R.string.change_password_error_confirm_empty, state.confirmPasswordError)
        assertFalse(state.isPasswordChanged)
    }

    @Test
    fun `validation fails when passwords do not match`() {
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onConfirmPasswordChanged("differentPass123")
        viewModel.onSavePassword()

        val state = viewModel.state.value
        assertEquals(R.string.change_password_error_mismatch, state.confirmPasswordError)
        assertFalse(state.isPasswordChanged)
    }

    @Test
    fun `successful password change updates state correctly`() = runTest {
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onConfirmPasswordChanged("newPass123")
        
        viewModel.onSavePassword()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isPasswordChanged)
        assertFalse(state.isLoading)
        assertNull(state.generalError)
    }

    @Test
    fun `password change with incorrect current password shows error`() = runTest {
        repository.shouldFailWithUnauthorized = true
        
        viewModel.onCurrentPasswordChanged("wrongPass")
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onConfirmPasswordChanged("newPass123")
        
        viewModel.onSavePassword()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isPasswordChanged)
        assertFalse(state.isLoading)
        assertEquals(R.string.change_password_error_incorrect, state.generalError)
    }

    @Test
    fun `password change with network error shows generic error`() = runTest {
        repository.shouldFailWithGenericError = true
        
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onConfirmPasswordChanged("newPass123")
        
        viewModel.onSavePassword()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isPasswordChanged)
        assertFalse(state.isLoading)
        assertEquals(R.string.change_password_error_generic, state.generalError)
    }

    @Test
    fun `error dismissed clears general error`() {
        viewModel.onCurrentPasswordChanged("oldPass123")
        viewModel.onNewPasswordChanged("newPass123")
        viewModel.onConfirmPasswordChanged("newPass123")
        
        repository.shouldFailWithGenericError = true
        
        runTest {
            viewModel.onSavePassword()
            advanceUntilIdle()
        }

        assertNotNull(viewModel.state.value.generalError)
        
        viewModel.onErrorDismissed()
        
        assertNull(viewModel.state.value.generalError)
    }

    private class FakeAuthRepository : AuthRepository {

        var shouldFailWithUnauthorized = false
        var shouldFailWithGenericError = false

        private val internalUser = MutableStateFlow<UserAccount?>(
            UserAccount(
                id = "test-id",
                email = "test@example.com",
                displayName = "Test User",
                photoUrl = null,
                isVerified = true,
            )
        )

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
            // No-op for tests
        }

        override suspend fun signOut() {
            internalUser.value = null
        }

        override suspend fun updatePassword(currentPassword: String, newPassword: String) {
            when {
                shouldFailWithUnauthorized -> {
                    throw HttpException(
                        Response.error<Any>(401, okhttp3.ResponseBody.create(null, ""))
                    )
                }
                shouldFailWithGenericError -> {
                    throw Exception("Network error")
                }
                else -> {
                    // Success - do nothing
                }
            }
        }

        override suspend fun sendPasswordRecoveryCode(email: String) {
            // No-op for tests
        }

        override suspend fun resetPassword(email: String, resetToken: String, newPassword: String) {
            // No-op for tests
        }
    }
}
