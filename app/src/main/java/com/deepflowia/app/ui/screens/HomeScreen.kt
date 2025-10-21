package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToJournal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rapport: Suvi des 7 derniers jours - Objectif atteint à 85%")
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onNavigateToTasks) {
                Text("TÂCHES")
            }
            Button(onClick = onNavigateToHabits) {
                Text("Habitudes")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onNavigateToGoals) {
                Text("OBJECTIFS")
            }
            Button(onClick = onNavigateToJournal) {
                Text("JOURNAL")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* TODO */ }) {
                Text("FOCUS")
            }
            Button(onClick = { /* TODO */ }) {
                Text("RÉFLEXION")
            }
        }
    }
}