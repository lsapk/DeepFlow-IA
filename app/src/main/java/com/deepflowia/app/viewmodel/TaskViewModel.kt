package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Task
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
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
            val result = SupabaseManager.client.postgrest.from("tasks").select().decodeList<Task>()
            _tasks.value = result
        }
    }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val task = Task(
                    userId = user.id,
                    title = title,
                    description = description
                )
                SupabaseManager.client.postgrest.from("tasks").insert(task)
                fetchTasks()
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest.from("tasks").update({
                set("title", task.title)
                set("description", task.description)
                set("is_completed", task.isCompleted)
            }) {
                filter {
                    eq("id", task.id)
                }
            }
            fetchTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            SupabaseManager.client.postgrest.from("tasks").delete {
                filter {
                    eq("id", task.id)
                }
            }
            fetchTasks()
        }
    }
}
