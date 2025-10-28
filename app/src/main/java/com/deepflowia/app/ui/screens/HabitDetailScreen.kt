package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.Habit
import com.deepflowia.app.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String?,
    navController: NavController,
    habitViewModel: HabitViewModel = viewModel()
) {
    val isNewHabit = habitId == "-1"
    val habitToEdit = if (!isNewHabit) {
        habitViewModel.habits.collectAsState().value.find { it.id == habitId }
    } else {
        null
    }

    var title by remember(habitToEdit) { mutableStateOf(habitToEdit?.title ?: "") }
    var description by remember(habitToEdit) { mutableStateOf(habitToEdit?.description ?: "") }
    var target by remember(habitToEdit) { mutableStateOf(habitToEdit?.target?.toString() ?: "") }
    var category by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: "") }
    var frequency by remember(habitToEdit) { mutableStateOf(habitToEdit?.frequency ?: "") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewHabit) "Nouvelle Habitude" else "Modifier l'habitude", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!isNewHabit) {
                        IconButton(onClick = {
                            habitToEdit?.let {
                                coroutineScope.launch {
                                    habitViewModel.deleteHabit(it)
                                    navController.popBackStack()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
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
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("Objectif (ex: 5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Catégorie") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Fréquence (ex: journalier, hebdomadaire)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (isNewHabit) {
                            habitViewModel.createHabit(
                                Habit(
                                    title = title,
                                    description = description.ifBlank { null },
                                    target = target.toIntOrNull(),
                                    category = category.ifBlank { null },
                                    frequency = frequency.ifBlank { null },
                                    userId = "" // Will be set by ViewModel
                                )
                            )
                        } else {
                            habitToEdit?.let {
                                val updatedHabit = it.copy(
                                    title = title,
                                    description = description.ifBlank { null },
                                    target = target.toIntOrNull(),
                                    category = category.ifBlank { null },
                                    frequency = frequency.ifBlank { null }
                                )
                                habitViewModel.updateHabit(updatedHabit)
                            }
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(if (isNewHabit) "Créer l'habitude" else "Enregistrer les modifications")
            }
        }
    }
}
