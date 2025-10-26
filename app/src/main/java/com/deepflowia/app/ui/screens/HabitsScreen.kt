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
import com.deepflowia.app.models.Habit
import com.deepflowia.app.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habitViewModel: HabitViewModel = viewModel()
) {
    val habits = habitViewModel.habits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habitudes") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add new habit */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn {
                items(habits.value) { habit ->
                    HabitItem(
                        habit = habit,
                        onHabitCompleted = { habitToUpdate, completed ->
                            habitViewModel.updateHabit(habitToUpdate.copy(isCompleted = completed))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onHabitCompleted: (Habit, Boolean) -> Unit) {
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
                checked = habit.isCompleted ?: false,
                onCheckedChange = { onHabitCompleted(habit, it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = habit.title, style = MaterialTheme.typography.titleMedium)
                Text(text = habit.description ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}