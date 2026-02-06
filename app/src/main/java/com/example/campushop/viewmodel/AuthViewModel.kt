package com.example.campushop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushop.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val profileComplete: Boolean) : AuthState()
    object ProfileIncomplete : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithGoogle(account)

            if (result.isSuccess) {
                val profileComplete = result.getOrNull() ?: false
                if (profileComplete) {
                    // Profile is complete, navigate to main screen
                    _authState.value = AuthState.Success(profileComplete = true)
                } else {
                    // Profile is incomplete, navigate to profile setup
                    _authState.value = AuthState.ProfileIncomplete
                }
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Sign in failed"
                )
            }
        }
    }

    fun updateProfile(name: String, rollNumber: String, department: String, year: String) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            if (userId == null) {
                _authState.value = AuthState.Error("User not logged in")
                return@launch
            }

            _authState.value = AuthState.Loading
            val result = repository.updateUserProfile(userId, name, rollNumber, department, year)

            if (result.isSuccess) {
                _authState.value = AuthState.Success(profileComplete = true)
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}