package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Task
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        viewModelScope.launch {
            val result = SupabaseManager.client.postgrest["tasks"].select()
            _tasks.value = result.decodeList<Task>()
        }
    }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            val task = Task(
                id = 0,
                userId = SupabaseManager.client.auth.currentUserOrNull()!!.id,
                title = title,
                description = description,
                isCompleted = false,
                dueDate = ""
            )
            SupabaseManager.client.postgrest["tasks"].insert(task)
            fetchTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest["tasks"].update(task) {
                filter {
                    eq("id", task.id)
                }
            }
            fetchTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest["tasks"].delete {
                filter {
                    eq("id", task.id)
                }
            }
            fetchTasks()
        }
    }
}