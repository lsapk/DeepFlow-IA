package com.deepflowia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.Task
import com.deepflowia.app.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel()
) {
    val tasks = taskViewModel.tasks.collectAsState()
    var showCompleted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tâches") },
                actions = {
                    TextButton(onClick = { showCompleted = !showCompleted }) {
                        Text(if (showCompleted) "Voir actives" else "Voir complétées")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add new task */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val filteredTasks = tasks.value.filter { it.isCompleted == showCompleted }
            LazyColumn {
                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClicked = {
                            navController.navigate("task_detail/${task.id}")
                        },
                        onTaskCompleted = { taskToUpdate, completed ->
                            taskViewModel.updateTask(taskToUpdate.copy(isCompleted = completed))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onTaskClicked: (Task) -> Unit, onTaskCompleted: (Task, Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onTaskClicked(task) }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted ?: false,
                onCheckedChange = { onTaskCompleted(task, it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                Text(text = task.description ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}