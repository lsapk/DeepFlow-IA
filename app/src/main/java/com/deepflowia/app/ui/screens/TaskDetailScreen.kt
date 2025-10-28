package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.Task
import com.deepflowia.app.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?,
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel()
) {
    val isNewTask = taskId == "-1"
    val taskToEdit = if (!isNewTask) {
        taskViewModel.tasks.collectAsState().value.find { it.id == taskId }
    } else {
        null
    }

    var title by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember(taskToEdit) { mutableStateOf(taskToEdit?.description ?: "") }
    var priority by remember(taskToEdit) { mutableStateOf(taskToEdit?.priority ?: "") }
    var dueDate by remember(taskToEdit) { mutableStateOf(taskToEdit?.dueDate ?: "") }

    val coroutineScope = rememberCoroutineScope()

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
                    if (!isNewTask) {
                        IconButton(onClick = {
                            taskToEdit?.let {
                                coroutineScope.launch {
                                    taskViewModel.deleteTask(it)
                                    navController.popBackStack()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priorité (ex: faible, moyenne, haute)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Date d'échéance (ex: YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isNewTask) {
                                taskViewModel.createTask(
                                    Task(
                                        title = title,
                                        description = description.ifBlank { null },
                                        priority = priority.ifBlank { null },
                                        dueDate = dueDate.ifBlank { null },
                                        userId = "" // Will be set by ViewModel
                                    )
                                )
                            } else {
                                taskToEdit?.let {
                                    val updatedTask = it.copy(
                                        title = title,
                                        description = description.ifBlank { null },
                                        priority = priority.ifBlank { null },
                                        dueDate = dueDate.ifBlank { null }
                                    )
                                    taskViewModel.updateTask(updatedTask)
                                }
                            }
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank()
                ) {
                    Text(if (isNewTask) "Créer la Tâche" else "Enregistrer les modifications")
                }
            }
        }
    )
}
