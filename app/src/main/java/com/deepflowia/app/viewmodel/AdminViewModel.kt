package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.AdminUser
import com.deepflowia.app.models.UserRole
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<AdminUser>>(emptyList())
    val users: StateFlow<List<AdminUser>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Récupérer tous les utilisateurs depuis la table 'user'
                val userList = SupabaseManager.client.postgrest
                    .from("user")
                    .select()
                    .decodeList<AdminUser>()

                // 2. Récupérer tous les rôles depuis la table 'user_roles'
                val userRoles = SupabaseManager.client.postgrest
                    .from("user_roles")
                    .select()
                    .decodeList<UserRole>()

                // 3. Créer une map pour une recherche facile (userId -> role)
                val rolesMap = userRoles.associateBy({ it.userId }, { it.role })

                // 4. Combiner les données pour avoir le rôle correct
                val usersWithCorrectRoles = userList.map { user ->
                    // Le rôle de `user_roles` écrase celui (potentiellement obsolète) de la table `user`
                    user.copy(role = rolesMap[user.id] ?: "Non défini")
                }

                _users.value = usersWithCorrectRoles
                Log.d("AdminViewModel", "Utilisateurs récupérés et rôles combinés : ${usersWithCorrectRoles.size}")

            } catch (e: Exception) {
                Log.e("AdminViewModel", "Erreur lors de la récupération des utilisateurs", e)
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
