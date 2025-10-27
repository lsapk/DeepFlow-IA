package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import io.github.jan.supabase.auth.SessionStatus
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initializing)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            SupabaseManager.client.auth.sessionStatus.collect { status ->
                Log.d("AuthViewModel", "Nouveau statut de session : $status")
                _authState.value = when (status) {
                    is SessionStatus.Authenticated -> AuthState.SignedIn
                    is SessionStatus.NotAuthenticated -> AuthState.SignedOut
                    is SessionStatus.Initializing -> AuthState.Initializing
                    is SessionStatus.LoadingFromStorage -> AuthState.Initializing
                    is SessionStatus.NetworkError -> AuthState.Error("Erreur réseau")
                    is SessionStatus.RefreshFailure -> AuthState.Error(status.cause.message ?: "Impossible de rafraîchir la session")
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
                // L'état sera mis à jour automatiquement par le collecteur de sessionStatus
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
                // L'état sera mis à jour automatiquement par le collecteur de sessionStatus
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
                // L'état sera mis à jour automatiquement par le collecteur de sessionStatus
                Log.d("AuthViewModel", "Déconnexion réussie.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Une erreur est survenue lors de la déconnexion")
                Log.e("AuthViewModel", "Échec de la déconnexion.", e)
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
