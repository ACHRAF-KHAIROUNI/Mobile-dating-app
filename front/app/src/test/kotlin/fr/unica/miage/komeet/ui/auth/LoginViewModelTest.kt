package fr.unica.miage.komeet.ui.auth

import fr.unica.miage.komeet.data.remote.dto.UserDto
import fr.unica.miage.komeet.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        val loggedInFlow = MutableStateFlow(false)
        val userFlow = MutableStateFlow<UserDto?>(null)

        override val isLoggedIn: Flow<Boolean> = loggedInFlow
        override val currentUser: Flow<UserDto?> = userFlow

        var shouldSucceed: Boolean = true

        override suspend fun login(email: String, password: String): Result<UserDto> {
            return if (shouldSucceed) {
                val user = UserDto("u1", email, "Alice")
                loggedInFlow.value = true
                userFlow.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Identifiants invalides"))
            }
        }

        override suspend fun register(email: String, password: String, displayName: String): Result<UserDto> {
            return Result.success(UserDto("u1", email, displayName))
        }

        override suspend fun logout(): Result<Unit> {
            loggedInFlow.value = false
            userFlow.value = null
            return Result.success(Unit)
        }
    }

    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAuthRepository()
        viewModel = LoginViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_withBlankFields_setsErrorMessage() {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun login_success_updatesState() = runTest {
        fakeRepo.shouldSucceed = true
        viewModel.onEmailChange("alice@example.com")
        viewModel.onPasswordChange("Password123")
        viewModel.login()

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun login_failure_setsErrorMessage() = runTest {
        fakeRepo.shouldSucceed = false
        viewModel.onEmailChange("alice@example.com")
        viewModel.onPasswordChange("WrongPassword")
        viewModel.login()

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSuccess)
        assertEquals("Identifiants invalides", viewModel.uiState.value.errorMessage)
    }
}
