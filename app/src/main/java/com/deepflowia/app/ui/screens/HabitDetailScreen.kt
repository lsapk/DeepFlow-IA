package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val allHabits by habitViewModel.allHabits.collectAsState()
    val habitToEdit = if (!isNewHabit) {
        allHabits.find { it.id == habitId }
    } else {
        null
    }

    var title by remember(habitToEdit) { mutableStateOf(habitToEdit?.title ?: "") }
    var description by remember(habitToEdit) { mutableStateOf(habitToEdit?.description ?: "") }
    var target by remember(habitToEdit) { mutableStateOf(habitToEdit?.target?.toString() ?: "") }
    var category by remember(habitToEdit) { mutableStateOf(habitToEdit?.category ?: "") }
    var frequency by remember(habitToEdit) { mutableStateOf(habitToEdit?.frequency ?: "") }
    val selectedDays = remember(habitToEdit) {
        mutableStateListOf<Int>().apply {
            habitToEdit?.daysOfWeek?.let { addAll(it) }
        }
    }

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
                .verticalScroll(rememberScrollState())
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
            val frequencyOptions = listOf("Journalier", "Hebdomadaire", "Mensuel")
            val frequencyMapping = mapOf(
                "Journalier" to "daily",
                "Hebdomadaire" to "weekly",
                "Mensuel" to "monthly"
            )
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = frequencyMapping.entries.find { it.value == frequency }?.key ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fréquence") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    frequencyOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                frequency = frequencyMapping[selectionOption]!!
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Quels jours de la semaine ? (laisser vide pour tous les jours)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("L", "Ma", "Me", "J", "V", "S", "D")
                days.forEachIndexed { index, day ->
                    val dayIndex = index + 1 // Lundi = 1, ..., Dimanche = 7
                    FilterChip(
                        selected = selectedDays.contains(dayIndex),
                        onClick = {
                            if (selectedDays.contains(dayIndex)) {
                                selectedDays.remove(dayIndex)
                            } else {
                                selectedDays.add(dayIndex)
                            }
                        },
                        label = { Text(day) }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val finalTarget = target.toIntOrNull()
                    if (finalTarget != null) {
                        coroutineScope.launch {
                            if (isNewHabit) {
                                habitViewModel.createHabit(
                                    Habit(
                                        title = title,
                                        description = description.ifBlank { null },
                                        target = finalTarget,
                                        category = category.ifBlank { null },
                                        frequency = frequency,
                                        daysOfWeek = selectedDays.toList(),
                                        userId = "" // Will be set by ViewModel
                                    )
                                )
                            } else {
                                habitToEdit?.let {
                                    val updatedHabit = it.copy(
                                        title = title,
                                        description = description.ifBlank { null },
                                        target = finalTarget,
                                        category = category.ifBlank { null },
                                        frequency = frequency,
                                        daysOfWeek = selectedDays.toList()
                                    )
                                    habitViewModel.updateHabit(updatedHabit)
                                }
                            }
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && frequency.isNotBlank() && target.toIntOrNull() != null
            ) {
                Text(if (isNewHabit) "Créer l'habitude" else "Enregistrer les modifications")
            }
        }
    }
}
