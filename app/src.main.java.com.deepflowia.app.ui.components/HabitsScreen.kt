package com.deepflowia.app.ui.screens

import androidx.compose.foundation.clickable
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
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(habits) { habit ->
                HabitItem(
                    habit = habit,
                    isCompleted = habit.id in completedHabitIds,
                    onHabitCompleted = { isChecked ->
                        habitViewModel.toggleHabitCompletion(habit, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onHabitCompleted: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(text = habit.title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(text = habit.description ?: "", style = MaterialTheme.typography.bodyMedium) },
        leadingContent = {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = null // Clicking the row handles completion
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHabitCompleted(!isCompleted) }
            .padding(vertical = 8.dp)
    )
}
