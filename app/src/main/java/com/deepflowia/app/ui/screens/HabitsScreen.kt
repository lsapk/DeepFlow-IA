package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.models.Habit
import com.deepflowia.app.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habitViewModel: HabitViewModel = viewModel()
) {
    val habits by habitViewModel.habits.collectAsState()
    val completedHabitIds by habitViewModel.habitCompletions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habitudes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add new habit */ }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une habitude")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn {
                items(habits) { habit ->
                    val isCompleted = completedHabitIds.contains(habit.id)
                    HabitItem(
                        habit = habit,
                        isCompleted = isCompleted,
                        onHabitCompleted = { checked ->
                            habit.id?.let { id ->
                                if (checked) {
                                    habitViewModel.completeHabit(id)
                                } else {
                                    habitViewModel.uncompleteHabit(id)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, isCompleted: Boolean, onHabitCompleted: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onHabitCompleted(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = habit.title, style = MaterialTheme.typography.titleMedium)
                Text(text = habit.description ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
