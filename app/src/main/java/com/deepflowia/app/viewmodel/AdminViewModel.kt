package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.AdminUser
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
                val userList = SupabaseManager.client.postgrest
                    .from("users")
                    .select(columns = Columns.raw("id, email, firstName:first_name, lastName:last_name, createdAt:created_at, disabled, user_roles(role)"))
                    .decodeList<AdminUser>()
                _users.value = userList
                Log.d("AdminViewModel", "Utilisateurs et rôles récupérés : ${userList.size}")
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Erreur lors de la récupération des utilisateurs et des rôles", e)
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
