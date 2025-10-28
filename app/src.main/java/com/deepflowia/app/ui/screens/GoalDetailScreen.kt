package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.GoalViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    navController: NavController,
    goalId: String?,
    goalViewModel: GoalViewModel = viewModel()
) {
    val goals by goalViewModel.goals.collectAsState()
    val isNewGoal = goalId == "-1"
    val goal = if (isNewGoal) null else goals.find { it.id == goalId }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }

    // Met à jour l'état local une fois que l'objectif est chargé
    LaunchedEffect(goal) {
        if (goal != null) {
            title = goal.title
            description = goal.description ?: ""
            category = goal.category ?: ""
            progress = goal.progress?.toFloat() ?: 0f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewGoal) "Nouvel Objectif" else "Modifier l'Objectif", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isNewGoal && goal == null) {
            // Affiche un indicateur de chargement pendant la récupération des données
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Affiche le formulaire une fois les données disponibles ou pour un nouvel objectif
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Progression: ${progress.roundToInt()}%")
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..100f,
                    steps = 99,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val progressInt = progress.roundToInt()
                        if (isNewGoal) {
                            goalViewModel.addGoal(title, description, category)
                        } else {
                            goal?.let {
                                goalViewModel.updateGoal(
                                    it.copy(
                                        title = title,
                                        description = description,
                                        category = category,
                                        progress = progressInt,
                                        completed = progressInt == 100
                                    )
                                )
                            }
                        }
                        navController.navigateUp()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank()
                ) {
                    Text("Sauvegarder")
                }
            }
        }
    }
}
