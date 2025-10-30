package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    init {
        viewModelScope.launch {
            SupabaseManager.client.auth.sessionStatus.collect { status ->
                Log.d("AuthViewModel", "Statut de la session Supabase : $status")
                _authState.value = when (status) {
                    is SessionStatus.Authenticated -> {
                        _userEmail.value = status.session.user?.email
                        AuthState.SignedIn
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _userEmail.value = null
                        AuthState.SignedOut
                    }
                    else -> AuthState.Loading
                }
            }
        }
    }

    fun signUp(emailValue: String, passwordValue: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Tentative d'inscription pour l'email : $emailValue")
            _authState.value = AuthState.Loading
            try {
                SupabaseManager.client.auth.signUpWith(Email) {
                    email = emailValue
                    password = passwordValue
                }
                _authState.value = AuthState.SignedIn
                Log.d("AuthViewModel", "Inscription réussie pour l'email : $emailValue")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue lors de l'inscription")
                Log.e("AuthViewModel", "Échec de l'inscription pour l'email : $emailValue", e)
            }
        }
    }

    fun signIn(emailValue: String, passwordValue: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Tentative de connexion pour l'email : $emailValue")
            _authState.value = AuthState.Loading
            try {
                SupabaseManager.client.auth.signInWith(Email) {
                    email = emailValue
                    password = passwordValue
                }
                _authState.value = AuthState.SignedIn
                Log.d("AuthViewModel", "Connexion réussie pour l'email : $emailValue")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue lors de la connexion")
                Log.e("AuthViewModel", "Échec de la connexion pour l'email : $emailValue", e)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Tentative de déconnexion.")
            try {
                SupabaseManager.client.auth.signOut()
                _authState.value = AuthState.SignedOut
                Log.d("AuthViewModel", "Déconnexion réussie.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue lors de la déconnexion")
                Log.e("AuthViewModel", "Échec de la déconnexion.", e)
            }
        }
    }

    fun updateUserEmail(newEmail: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseManager.client.auth.updateUser {
                    email = newEmail
                }
                _userEmail.value = newEmail
            } catch (e: Exception) {
                onError(e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun updatePassword(newPassword: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseManager.client.auth.updateUser {
                    password = newPassword
                }
            } catch (e: Exception) {
                onError(e.message ?: "Une erreur est survenue")
            }
        }
    }
}

sealed class AuthState {
    object Initializing : AuthState()
    object SignedIn : AuthState()
    object SignedOut : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
