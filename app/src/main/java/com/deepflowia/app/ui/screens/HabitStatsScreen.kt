package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.Habit
import com.deepflowia.app.ui.components.StatChip
import com.deepflowia.app.viewmodel.HabitStats
import com.deepflowia.app.viewmodel.HabitStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitStatsScreen(
    navController: NavController,
    habitStatsViewModel: HabitStatsViewModel = viewModel()
) {
    val stats by habitStatsViewModel.habitStats.collectAsState()
    val habits by habitStatsViewModel.allHabits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques des Habitudes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(habits) { habit ->
                HabitStatsItem(habit = habit, stats = stats[habit.id] ?: HabitStats())
            }
        }
    }
}

@Composable
fun HabitStatsItem(habit: Habit, stats: HabitStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(habit.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatChip(label = "Total", value = stats.totalCompletions.toString())
                StatChip(label = "Série", value = stats.currentStreak.toString())
                StatChip(label = "Record", value = stats.longestStreak.toString())
            }
        }
    }
}
