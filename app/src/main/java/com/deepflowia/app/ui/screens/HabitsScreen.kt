package com.deepflowia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitsScreen(
    navController: NavController,
    habitViewModel: HabitViewModel = viewModel()
) {
    val habits by habitViewModel.habits.collectAsState()
    val completedHabitIds by habitViewModel.habitCompletions.collectAsState()
    var showEditDialog by remember { mutableStateOf<Habit?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habitudes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new habit */ },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une habitude", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(habits, key = { it.id!! }) { habit ->
                val isCompleted = completedHabitIds.contains(habit.id)
                HabitItem(
                    habit = habit,
                    isCompleted = isCompleted,
                    onHabitCompleted = { checked ->
                        habit.id?.let { id ->
                            if (checked) {
                                habitViewModel.completeHabit(id)
                            } else {
                                habitViewModel.uncompleteHabit(id)
                            }
                        }
                    },
                    onEdit = { showEditDialog = habit },
                    onDelete = { showDeleteDialog = habit }
                )
            }
        }

        showEditDialog?.let { habit ->
            EditHabitDialog(
                habit = habit,
                onDismiss = { showEditDialog = null },
                onConfirm = { updatedHabit ->
                    habitViewModel.updateHabit(updatedHabit)
                    showEditDialog = null
                }
            )
        }

        showDeleteDialog?.let { habit ->
            DeleteHabitDialog(
                habit = habit,
                onDismiss = { showDeleteDialog = null },
                onConfirm = {
                    habitViewModel.deleteHabit(habit)
                    showDeleteDialog = null
                }
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onHabitCompleted: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onHabitCompleted(!isCompleted) }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onHabitCompleted
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.title, style = MaterialTheme.typography.titleMedium)
                habit.description?.let {
                    if (it.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Modifier") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Supprimer") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteHabitDialog(habit: Habit, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer l'habitude") },
        text = { Text("Êtes-vous sûr de vouloir supprimer l'habitude \"${habit.title}\" ? Cette action est irréversible.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditHabitDialog(habit: Habit, onDismiss: () -> Unit, onConfirm: (Habit) -> Unit) {
    var description by remember { mutableStateOf(habit.description ?: "") }
    var target by remember { mutableStateOf(habit.target?.toString() ?: "") }
    var category by remember { mutableStateOf(habit.category ?: "") }
    val daysOfWeek = listOf("L", "M", "M", "J", "V", "S", "D")
    val dayCodes = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
    var selectedDays by remember { mutableStateOf(habit.daysOfWeek?.toSet() ?: emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier l'habitude") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Objectif numérique") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Jours de la semaine :", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysOfWeek.forEachIndexed { index, day ->
                        val dayCode = dayCodes[index]
                        FilterChip(
                            selected = selectedDays.contains(dayCode),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayCode)) {
                                    selectedDays - dayCode
                                } else {
                                    selectedDays + dayCode
                                }
                            },
                            label = { Text(day) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedHabit = habit.copy(
                        description = description.ifBlank { null },
                        target = target.toIntOrNull(),
                        category = category.ifBlank { null },
                        daysOfWeek = selectedDays.toList().sorted()
                    )
                    onConfirm(updatedHabit)
                }
            ) {
                Text("Sauvegarder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
