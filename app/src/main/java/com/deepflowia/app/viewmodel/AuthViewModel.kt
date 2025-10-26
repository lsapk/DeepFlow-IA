package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        Log.d("AuthViewModel", "ViewModel initialisé.")
        SupabaseManager.client.auth.sessionStatus
            .onEach { status ->
                Log.d("AuthViewModel", "Nouveau statut de session : $status")
                _authState.value = when (status) {
                    is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> AuthState.SignedIn
                    is io.github.jan.supabase.gotrue.SessionStatus.Initializing -> AuthState.Loading
                    is io.github.jan.supabase.gotrue.SessionStatus.NotAuthenticated -> AuthState.SignedOut
                    is io.github.jan.supabase.gotrue.SessionStatus.RefreshFailure -> AuthState.Error(status.cause.message ?: "Erreur de rafraîchissement de la session")
                    else -> AuthState.Loading // Fallback for any other states
                }
            }
            .launchIn(viewModelScope)
    }

    fun signUp(emailValue: String, passwordValue: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Tentative d'inscription pour l'email : $emailValue")
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
