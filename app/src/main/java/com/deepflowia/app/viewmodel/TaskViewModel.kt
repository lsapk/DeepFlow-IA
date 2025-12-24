package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.TaskRepository
import com.deepflowia.app.models.Subtask
import com.deepflowia.app.models.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.GoTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val auth: GoTrue
) : ViewModel() {

    private val currentUserId: String? get() = auth.currentUserOrNull()?.id

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTaskId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            // Combine la tâche et ses sous-tâches dans un seul flow
            val taskFlow = allTasks.map { list -> list.find { it.id == id } }
            val subtasksFlow = taskRepository.getSubtasksForTask(id)
            combine(taskFlow, subtasksFlow) { task, subtasks ->
                task?.copy(subtasks = subtasks)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    init {
        observeTasks()
        refreshTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                taskRepository.getAllTasks(userId).collect { tasks ->
                    _allTasks.value = tasks.sortedWith(compareBy { task ->
                        when (task.priority) {
                            "high" -> 0
                            "medium" -> 1
                            "low" -> 2
                            else -> 3
                        }
                    })
                }
            }
        }
    }

    fun refreshTasks() {
        viewModelScope.launch {
            currentUserId?.let {
                taskRepository.refreshTasks(it)
                    .onFailure { e -> _error.value = "Erreur de synchronisation des tâches." }
            }
        }
    }

    fun getTaskById(taskId: String) {
        if (taskId == "-1") {
            _selectedTaskId.value = null
        } else {
            _selectedTaskId.value = taskId
            viewModelScope.launch {
                taskRepository.refreshSubtasks(taskId)
            }
        }
    }

    fun createTask(task: Task, callback: (String?) -> Unit) {
        viewModelScope.launch {
            currentUserId?.let {
                val result = taskRepository.createTask(task.copy(userId = it))
                result.onSuccess { createdTask -> callback(createdTask.id) }
                result.onFailure {
                    _error.value = "Impossible de créer la tâche."
                    callback(null)
                }
            } ?: callback(null)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
                .onFailure { _error.value = "Impossible de mettre à jour la tâche." }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
                .onFailure { _error.value = "Impossible de supprimer la tâche." }
        }
    }

    fun createSubtask(subtask: Subtask) {
        viewModelScope.launch {
            currentUserId?.let {
                taskRepository.createSubtask(subtask.copy(userId = it))
                    .onFailure { _error.value = "Impossible de créer la sous-tâche." }
            }
        }
    }

    fun updateSubtask(subtask: Subtask) {
        viewModelScope.launch {
            taskRepository.updateSubtask(subtask)
                .onFailure { _error.value = "Impossible de mettre à jour la sous-tâche." }
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            taskRepository.deleteSubtask(subtask)
                .onFailure { _error.value = "Impossible de supprimer la sous-tâche." }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
