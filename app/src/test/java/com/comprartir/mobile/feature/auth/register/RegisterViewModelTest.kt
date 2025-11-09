package com.comprartir.mobile.feature.auth.register

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.auth.data.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()
    private val applicationContext: Application = ApplicationProvider.getApplicationContext()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
        viewModel = RegisterViewModel(repository, applicationContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit disabled when fields empty`() {
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `submit enabled when all fields valid`() = runTest {
        populateValidFields()

        assertTrue(viewModel.uiState.value.isSubmitEnabled)

        viewModel.onEvent(RegisterEvent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.registerCallCount)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals(
            applicationContext.getString(R.string.register_success_message),
            viewModel.uiState.value.successMessage,
        )

        viewModel.onEvent(RegisterEvent.DismissSuccess)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertEquals(null, viewModel.uiState.value.successMessage)
    }

    @Test
    fun `passwords mismatch shows error`() {
        populateValidFields(confirmPassword = "different")

        viewModel.onEvent(RegisterEvent.Submit)

        val expectedError = applicationContext.getString(R.string.error_passwords_dont_match)
        assertEquals(expectedError, viewModel.uiState.value.confirmPasswordError)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `duplicate email surfaces friendly error`() = runTest {
        repository.shouldFailWithConflict = true
        populateValidFields()

        viewModel.onEvent(RegisterEvent.Submit)
        advanceUntilIdle()

        assertEquals(
            applicationContext.getString(R.string.error_email_already_registered),
            viewModel.uiState.value.errorMessage,
        )
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `invalid email shows error`() {
        viewModel.onEvent(RegisterEvent.NameChanged("Jane"))
        viewModel.onEvent(RegisterEvent.LastNameChanged("Doe"))
        viewModel.onEvent(RegisterEvent.EmailChanged("invalid-email"))

        val expectedError = applicationContext.getString(R.string.error_email_invalid)
        assertEquals(expectedError, viewModel.uiState.value.emailError)
    }

    private fun populateValidFields(
        name: String = "Jane",
        lastName: String = "Doe",
        email: String = "jane.doe@example.com",
        password: String = "secret1",
        confirmPassword: String = "secret1",
    ) {
        viewModel.onEvent(RegisterEvent.NameChanged(name))
        viewModel.onEvent(RegisterEvent.LastNameChanged(lastName))
        viewModel.onEvent(RegisterEvent.EmailChanged(email))
        viewModel.onEvent(RegisterEvent.PasswordChanged(password))
        viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(confirmPassword))
    }

    private class FakeAuthRepository : AuthRepository {
        private val userFlow = MutableStateFlow<UserAccount?>(null)

        var registerCallCount: Int = 0
        var shouldFailWithConflict: Boolean = false

        override val currentUser: Flow<UserAccount?> = userFlow
        override val isAuthenticated: Flow<Boolean> = MutableStateFlow(false)

        override suspend fun register(email: String, password: String) {
            if (shouldFailWithConflict) {
                val responseBody = "{\"error\":\"Conflict\"}".toResponseBody("application/json".toMediaType())
                throw HttpException(Response.error<Any>(409, responseBody))
            }
            registerCallCount += 1
        }

        override suspend fun verify(email: String, code: String) = Unit

        override suspend fun signIn(email: String, password: String) = Unit

        override suspend fun signOut() = Unit

        override suspend fun updatePassword(currentPassword: String, newPassword: String) = Unit
    }
}
