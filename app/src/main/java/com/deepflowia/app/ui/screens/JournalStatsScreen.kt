package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.JournalStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalStatsScreen(navController: NavController, journalStatsViewModel: JournalStatsViewModel = viewModel()) {
    val stats by journalStatsViewModel.journalStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques du Journal") },
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCard(title = "Total d'Entrées", value = stats.totalEntries.toString())
            }

            if (stats.moodFrequency.isNotEmpty()) {
                item {
                    Text("Humeurs Fréquentes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                }
                items(stats.moodFrequency.toList().sortedByDescending { it.second }) { (mood, count) ->
                    StatCard(title = mood, value = count.toString())
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp)
        }
    }
}
