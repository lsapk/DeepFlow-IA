package com.deepflowia.app.data

import com.deepflowia.app.models.Subtask
import com.deepflowia.app.models.Task
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val supabasePostgrest: Postgrest
) {

    // --- Tâches ---

    fun getAllTasks(userId: String): Flow<List<Task>> = taskDao.getTasks(userId)

    suspend fun refreshTasks(userId: String): Result<Unit> = runCatching {
        val remoteTasks = supabasePostgrest.from("tasks")
            .select { filter("user_id", "eq", userId) }
            .decodeList<Task>()
        taskDao.insertAll(remoteTasks)
    }

    suspend fun createTask(task: Task): Result<Task> = runCatching {
        val result = supabasePostgrest.from("tasks")
            .insert(task, returning = Returning.REPRESENTATION)
            .decodeSingle<Task>()
        taskDao.insertAll(listOf(result))
        result
    }

    suspend fun updateTask(task: Task): Result<Unit> = runCatching {
        task.id?.let {
            val result = supabasePostgrest.from("tasks").update({
                set("title", task.title)
                set("description", task.description)
                set("completed", task.completed)
            }) { filter { eq("id", it) } }.decodeSingle<Task>()
            taskDao.insertAll(listOf(result))
        }
    }

    suspend fun deleteTask(task: Task): Result<Unit> = runCatching {
        task.id?.let { taskId ->
            supabasePostgrest.from("tasks").delete { filter { eq("id", taskId) } }
            taskDao.deleteTask(taskId) // La suppression en cascade devrait gérer les sous-tâches
        }
    }

    // --- Sous-tâches ---

    fun getSubtasksForTask(taskId: String): Flow<List<Subtask>> = subtaskDao.getSubtasksForTask(taskId)

    suspend fun refreshSubtasks(taskId: String): Result<Unit> = runCatching {
        val remoteSubtasks = supabasePostgrest.from("subtasks")
            .select { filter("parent_task_id", "eq", taskId) }
            .decodeList<Subtask>()
        subtaskDao.insertAll(remoteSubtasks)
    }

    suspend fun createSubtask(subtask: Subtask): Result<Unit> = runCatching {
        val result = supabasePostgrest.from("subtasks").insert(subtask).decodeSingle<Subtask>()
        subtaskDao.insertAll(listOf(result))
    }

    suspend fun updateSubtask(subtask: Subtask): Result<Unit> = runCatching {
        subtask.id?.let {
            val result = supabasePostgrest.from("subtasks").update({
                set("title", subtask.title)
                set("completed", subtask.completed)
            }) { filter { eq("id", it) } }.decodeSingle<Subtask>()
            subtaskDao.insertAll(listOf(result))
        }
    }

    suspend fun deleteSubtask(subtask: Subtask): Result<Unit> = runCatching {
        subtask.id?.let { subtaskId ->
            supabasePostgrest.from("subtasks").delete { filter { eq("id", subtaskId) } }
            subtaskDao.deleteSubtask(subtaskId)
        }
    }
}
