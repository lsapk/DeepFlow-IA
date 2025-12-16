package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.Subtask
import com.deepflowia.app.models.Task
import com.deepflowia.app.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?,
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel()
) {
    val isNewTask = taskId == null || taskId == "-1"
    val selectedTask by taskViewModel.selectedTask.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") } // Default value
    var dueDate by remember { mutableStateOf("") }
    var newSubtaskTitle by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }
    var subtaskToEdit by remember { mutableStateOf<Subtask?>(null) }

    // Effect to fetch the task when taskId changes
    LaunchedEffect(taskId) {
        if (!isNewTask) {
            taskViewModel.getTaskById(taskId!!)
        } else {
            taskViewModel.getTaskById("-1") // Reset selected task
        }
    }

    // Effect to update local state when the selected task is loaded
    LaunchedEffect(selectedTask) {
        if (!isNewTask && selectedTask != null) {
            title = selectedTask!!.title
            description = selectedTask!!.description ?: ""
            priority = selectedTask!!.priority ?: "medium"
            dueDate = selectedTask!!.dueDate ?: ""
        } else {
            // Reset for new task creation
            title = ""
            description = ""
            priority = "medium"
            dueDate = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewTask) "Nouvelle Tâche" else "Détail de la Tâche", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            val taskToSave = if (isNewTask) {
                                Task(
                                    title = title,
                                    description = description.ifBlank { null },
                                    priority = priority.ifBlank { "medium" },
                                    dueDate = dueDate.ifBlank { null },
                                    userId = "" // Will be set in ViewModel
                                )
                            } else {
                                selectedTask!!.copy(
                                    title = title,
                                    description = description.ifBlank { null },
                                    priority = priority.ifBlank { "medium" },
                                    dueDate = dueDate.ifBlank { null }
                                )
                            }

                            if (isNewTask) {
                                taskViewModel.createTask(taskToSave)
                            } else {
                                taskViewModel.updateTask(taskToSave)
                            }
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Sauvegarder")
                    }
                    if (!isNewTask) {
                        IconButton(onClick = {
                            selectedTask?.let {
                                taskViewModel.deleteTask(it)
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                item {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priorité (faible, moyenne, haute)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Date d'échéance (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!isNewTask && selectedTask != null) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sous-tâches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(selectedTask!!.subtasks, key = { it.id!! }) { subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = subtask.completed,
                                onCheckedChange = { isChecked ->
                                    taskViewModel.updateSubtask(subtask.copy(completed = isChecked))
                                }
                            )
                            Text(text = subtask.title, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                subtaskToEdit = subtask
                                showEditDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier la sous-tâche")
                            }
                            IconButton(onClick = { taskViewModel.deleteSubtask(subtask) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer la sous-tâche")
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newSubtaskTitle,
                                onValueChange = { newSubtaskTitle = it },
                                label = { Text("Nouvelle sous-tâche") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (newSubtaskTitle.isNotBlank()) {
                                        val newSubtask = Subtask(
                                            title = newSubtaskTitle,
                                            parentTaskId = selectedTask!!.id!!,
                                            userId = "" // ViewModel handles this
                                        )
                                        taskViewModel.createSubtask(newSubtask)
                                        newSubtaskTitle = "" // Clear field
                                    }
                                },
                                enabled = newSubtaskTitle.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter la sous-tâche")
                            }
                        }
                    }
                }
            }
        }
    )

    if (showEditDialog && subtaskToEdit != null) {
        EditSubtaskDialog(
            subtask = subtaskToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedSubtask ->
                taskViewModel.updateSubtask(updatedSubtask)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditSubtaskDialog(
    subtask: Subtask,
    onDismiss: () -> Unit,
    onSave: (Subtask) -> Unit
) {
    var title by remember { mutableStateOf(subtask.title) }
    var description by remember { mutableStateOf(subtask.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la sous-tâche") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedSubtask = subtask.copy(
                        title = title,
                        description = description.ifBlank { null }
                    )
                    onSave(updatedSubtask)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Sauvegarder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
