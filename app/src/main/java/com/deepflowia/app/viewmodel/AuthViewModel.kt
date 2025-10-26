package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            SupabaseManager.client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _authState.value = AuthState.SignedIn
                        Log.d("AuthViewModel", "L'utilisateur est authentifié.")
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _authState.value = AuthState.SignedOut
                        Log.d("AuthViewModel", "L'utilisateur n'est pas authentifié.")
                    }
                    else -> {
                        Log.d("AuthViewModel", "État de la session : $status")
                    }
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
                Log.d("AuthViewModel", "Déconnexion réussie.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue lors de la déconnexion")
                Log.e("AuthViewModel", "Échec de la déconnexion.", e)
            }
        }
    }
}

sealed class AuthState {
    object SignedIn : AuthState()
    object SignedOut : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
