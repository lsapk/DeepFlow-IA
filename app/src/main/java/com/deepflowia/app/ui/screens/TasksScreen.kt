package com.deepflowia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val tasks by taskViewModel.tasks.collectAsState()
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
                Icon(Icons.Default.Add, contentDescription = "Ajouter une tâche")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val filteredTasks = tasks.filter { (it.completed ?: false) == showCompleted }
            LazyColumn {
                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        onTaskClicked = {
                            task.id?.let {
                                navController.navigate("task_detail/$it")
                            }
                        },
                        onTaskCompleted = { completed ->
                            taskViewModel.updateTask(task.copy(completed = completed))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: Task, onTaskClicked: () -> Unit, onTaskCompleted: (Boolean) -> Unit) {
    ListItem(
        headlineText = { Text(task.title) },
        supportingText = { task.description?.let { Text(it) } },
        leadingContent = {
            Checkbox(
                checked = task.completed ?: false,
                onCheckedChange = onTaskCompleted
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onTaskClicked)
    )
}
