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
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask
    init {
        fetchTasks()
    }

    fun getTaskById(taskId: String) {
        viewModelScope.launch {
            if (taskId == "-1") {
                _selectedTask.value = null
                return@launch
            }
            try {
                val taskResult = SupabaseManager.client.postgrest.from("tasks")
                    .select { filter { eq("id", taskId) } }
                    .decodeSingleOrNull<Task>()

                if (taskResult != null) {
                    val subtasksResult = SupabaseManager.client.postgrest.from("subtasks")
                        .select { filter { eq("parent_task_id", taskId) } }
                        .decodeList<Subtask>()
                    _selectedTask.value = taskResult.copy(subtasks = subtasksResult)
                } else {
                    _selectedTask.value = null
                }
            } catch (e: Exception) {
               // Log error
                _selectedTask.value = null
            }
        }
    }

    fun fetchTasks() {
        viewModelScope.launch {
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
            val tasksResult = SupabaseManager.client.postgrest.from("tasks").select {
                filter { eq("user_id", userId) }
            }.decodeList<Task>()
            val subtasksResult = SupabaseManager.client.postgrest.from("subtasks").select {
                filter { eq("user_id", userId) }
            }.decodeList<Subtask>()
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
            try {
                val user = SupabaseManager.client.auth.currentUserOrNull() ?: return@launch
                val newSubtask = subtask.copy(userId = user.id)
                SupabaseManager.client.postgrest.from("subtasks").insert(newSubtask)
                getTaskById(subtask.parentTaskId) // Refresh selected task
                fetchTasks() // Also refresh main list
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun updateSubtask(subtask: Subtask) {
        viewModelScope.launch {
            subtask.id?.let {
                try {
                    SupabaseManager.client.postgrest.from("subtasks").update({
                        set("title", subtask.title)
                        set("completed", subtask.completed)
                        set("description", subtask.description)
                        set("priority", subtask.priority)
                    }) {
                        filter {
                            eq("id", it)
                        }
                    }
                    getTaskById(subtask.parentTaskId) // Refresh selected task
                    fetchTasks() // Also refresh main list
                } catch (e: Exception) {
                    // Log error
                }
            }
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            subtask.id?.let {
                try {
                    SupabaseManager.client.postgrest.from("subtasks").delete {
                        filter {
                            eq("id", it)
                        }
                    }
                    getTaskById(subtask.parentTaskId) // Refresh selected task
                    fetchTasks() // Also refresh main list
                } catch (e: Exception) {
                    // Log error
                }
            }
        }
    }
}
