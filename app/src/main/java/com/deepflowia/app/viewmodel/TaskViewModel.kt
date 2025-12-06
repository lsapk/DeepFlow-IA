package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Subtask
import com.deepflowia.app.models.Task
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks
    private val _subtasks = MutableStateFlow<List<Subtask>>(emptyList())

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        viewModelScope.launch {
            val tasksResult = SupabaseManager.client.postgrest.from("tasks").select().decodeList<Task>()
            val subtasksResult = SupabaseManager.client.postgrest.from("subtasks").select().decodeList<Subtask>()
            _subtasks.value = subtasksResult
            val tasksWithSubtasks = tasksResult.map { task ->
                task.copy(subtasks = subtasksResult.filter { it.parentTaskId == task.id })
            }
            _tasks.value = tasksWithSubtasks.sortedWith(compareBy { task ->
                when (task.priority) {
                    "high" -> 0
                    "medium" -> 1
                    "low" -> 2
                    else -> 3
                }
            })
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val newTask = task.copy(userId = user.id)
                SupabaseManager.client.postgrest.from("tasks").insert(newTask)
                fetchTasks()
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            task.id?.let {
                SupabaseManager.client.postgrest.from("tasks").update({
                    set("title", task.title)
                    set("description", task.description)
                    set("completed", task.completed)
                    set("due_date", task.dueDate)
                    set("priority", task.priority)
                }) {
                    filter {
                        eq("id", it)
                    }
                }
                fetchTasks()
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            task.id?.let {
                SupabaseManager.client.postgrest.from("tasks").delete {
                    filter {
                        eq("id", it)
                    }
                }
                fetchTasks()
            }
        }
    }

    fun createSubtask(subtask: Subtask) {
        viewModelScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val newSubtask = subtask.copy(userId = user.id)
                SupabaseManager.client.postgrest.from("subtasks").insert(newSubtask)
                fetchTasks() // Refresh all tasks to update subtask lists
            }
        }
    }

    fun updateSubtask(subtask: Subtask) {
        viewModelScope.launch {
            subtask.id?.let {
                SupabaseManager.client.postgrest.from("subtasks").update({
                    set("title", subtask.title)
                    set("completed", subtask.completed)
                }) {
                    filter {
                        eq("id", it)
                    }
                }
                fetchTasks()
            }
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            subtask.id?.let {
                SupabaseManager.client.postgrest.from("subtasks").delete {
                    filter {
                        eq("id", it)
                    }
                }
                fetchTasks()
            }
        }
    }
}
