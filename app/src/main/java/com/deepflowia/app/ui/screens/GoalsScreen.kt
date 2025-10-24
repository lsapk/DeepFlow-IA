package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.models.Goal
import com.deepflowia.app.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    goalViewModel: GoalViewModel = viewModel()
) {
    val goals = goalViewModel.goals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objectifs") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add new goal */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add goal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn {
                items(goals.value) { goal ->
                    GoalItem(
                        goal = goal,
                        onGoalCompleted = { goalToUpdate, completed ->
                            goalViewModel.updateGoal(goalToUpdate.copy(isCompleted = completed))
                        },
                        onIncreaseProgress = { goalToUpdate ->
                            val newProgress = ((goalToUpdate.progress ?: 0) + 10).coerceAtMost(100)
                            goalViewModel.updateGoal(goalToUpdate.copy(progress = newProgress))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GoalItem(
    goal: Goal,
    onGoalCompleted: (Goal, Boolean) -> Unit,
    onIncreaseProgress: (Goal) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = goal.isCompleted ?: false,
                    onCheckedChange = { onGoalCompleted(goal, it) }
                )
                Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
            }
            Text(text = goal.description ?: "", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (goal.progress ?: 0) / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onIncreaseProgress(goal) }) {
                Text("+10%")
            }
        }
    }
}