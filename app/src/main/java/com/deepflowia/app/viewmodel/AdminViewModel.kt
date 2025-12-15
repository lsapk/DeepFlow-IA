package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.AdminUser
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
                val userList = SupabaseManager.client.postgrest
                    .from("user")
                    .select()
                    .decodeList<AdminUser>()
                _users.value = userList
                Log.d("AdminViewModel", "Utilisateurs récupérés : ${userList.size}")
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Erreur lors de la récupération des utilisateurs", e)
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
