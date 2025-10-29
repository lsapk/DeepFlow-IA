package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.Goal
import com.deepflowia.app.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    navController: NavController,
    goalId: String?,
    goalViewModel: GoalViewModel = viewModel()
) {
    val selectedGoal by goalViewModel.selectedGoal.collectAsState()
    val isEditing = goalId != null && goalId != "-1"

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    LaunchedEffect(goalId) {
        if (isEditing) {
            goalViewModel.getGoalById(goalId!!)
        } else {
            goalViewModel.getGoalById("-1") // Reset
        }
    }

    LaunchedEffect(selectedGoal) {
        if (isEditing && selectedGoal != null) {
            title = selectedGoal!!.title
            description = selectedGoal!!.description ?: ""
            category = selectedGoal!!.category ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Modifier l'Objectif" else "Nouvel Objectif") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                if (isEditing) {
                                    val updatedGoal = selectedGoal!!.copy(
                                        title = title,
                                        description = description,
                                        category = category
                                    )
                                    goalViewModel.updateGoal(updatedGoal)
                                } else {
                                    goalViewModel.addGoal(title, description, category)
                                }
                                navController.navigateUp()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Sauvegarder")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre de l'objectif") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Catégorie (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
