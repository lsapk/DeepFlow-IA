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
import com.deepflowia.app.viewmodel.GoalStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalStatsScreen(navController: NavController, goalStatsViewModel: GoalStatsViewModel = viewModel()) {
    val stats by goalStatsViewModel.goalStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques des Objectifs") },
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
                StatCard(title = "Total des Objectifs", value = stats.totalGoals.toString())
            }
            item {
                StatCard(title = "Objectifs Terminés", value = stats.completedGoals.toString())
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Taux de Réussite", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = stats.completionRate / 100f,
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${"%.2f".format(stats.completionRate)}%",
                            modifier = Modifier.align(Alignment.End),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            if (stats.goalsByCategory.isNotEmpty()) {
                item {
                    Text("Objectifs par Catégorie", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                }
                items(stats.goalsByCategory.toList()) { (category, count) ->
                    StatCard(title = category, value = count.toString())
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
