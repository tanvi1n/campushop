package com.example.campushop.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushop.data.model.User
import com.example.campushop.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // UI State
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkAuthStatus()
    }

    // Check if user is already logged in
    private fun checkAuthStatus() {
        if (repository.isUserLoggedIn()) {
            val userId = repository.getCurrentUserId()
            userId?.let {
                viewModelScope.launch {
                    loadUserData(it)
                }
            }
        }
    }

    // Register new user
    fun register(
        email: String,
        password: String,
        name: String,
        department: String,
        year: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.registerUser(email, password, name, department, year)

            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthState.Success("Registration successful!")
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Registration failed")
            }
        }
    }

    // Login user
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.loginUser(email, password)

            result.onSuccess { userId ->
                loadUserData(userId)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Login failed")
            }
        }
    }

    // Load user data
    private suspend fun loadUserData(userId: String) {
        val result = repository.getUserData(userId)

        result.onSuccess { user ->
            _currentUser.value = user
            _authState.value = AuthState.Success("Login successful!")
        }.onFailure { exception ->
            _authState.value = AuthState.Error(exception.message ?: "Failed to load user data")
        }
    }

    // Logout user
    fun logout() {
        repository.logoutUser()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    // Reset auth state
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

// Sealed class for different auth states
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}