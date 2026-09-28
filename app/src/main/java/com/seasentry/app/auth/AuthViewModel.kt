package com.seasentry.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class AuthScreen {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val isSplashShowing: Boolean = true,
    val activeAccount: VesselAccount? = null,
    val currentScreen: AuthScreen = AuthScreen.LOGIN,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
) {
    val isLoggedIn: Boolean get() = activeAccount != null
}

/**
 * ViewModel managing authentication state, login, register, and demo vessel workflows.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isSplashShowing = MutableStateFlow(true)
    private val _currentScreen = MutableStateFlow(AuthScreen.LOGIN)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<AuthUiState> = combine(
        _isSplashShowing,
        authRepository.activeSession,
        _currentScreen,
        _errorMessage,
        _isLoading
    ) { isSplash, activeAccount, currentScreen, errorMessage, isLoading ->
        AuthUiState(
            isSplashShowing = isSplash,
            activeAccount = activeAccount,
            currentScreen = currentScreen,
            errorMessage = errorMessage,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthUiState(
            isSplashShowing = true,
            activeAccount = authRepository.activeSession.value
        )
    )

    fun onSplashFinished() {
        _isSplashShowing.value = false
    }

    fun showLogin() {
        _errorMessage.value = null
        _currentScreen.value = AuthScreen.LOGIN
    }

    fun showRegister() {
        _errorMessage.value = null
        _currentScreen.value = AuthScreen.REGISTER
    }

    fun login(registrationNumber: String, pin: String) {
        _errorMessage.value = null
        _isLoading.value = true
        val result = authRepository.login(registrationNumber, pin)
        _isLoading.value = false
        result.onFailure { error ->
            _errorMessage.value = error.message ?: "Authentication failed. Please try again."
        }
    }

    fun register(
        vesselName: String,
        registrationNumber: String,
        captainName: String,
        homePort: String,
        pin: String
    ) {
        _errorMessage.value = null
        _isLoading.value = true
        val result = authRepository.register(
            vesselName = vesselName,
            registrationNumber = registrationNumber,
            captainName = captainName,
            homePort = homePort,
            pin = pin
        )
        _isLoading.value = false
        result.onFailure { error ->
            _errorMessage.value = error.message ?: "Registration failed. Please check your inputs."
        }
    }

    fun loginDemoVessel() {
        _errorMessage.value = null
        _isLoading.value = true
        val result = authRepository.loginDemoVessel()
        _isLoading.value = false
        result.onFailure { error ->
            _errorMessage.value = error.message ?: "Could not initialize demo vessel."
        }
    }

    fun logout() {
        _errorMessage.value = null
        authRepository.logout()
        _currentScreen.value = AuthScreen.LOGIN
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
