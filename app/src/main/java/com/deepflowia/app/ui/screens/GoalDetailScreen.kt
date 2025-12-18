package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.app.DatePickerDialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.deepflowia.app.models.Goal
import com.deepflowia.app.models.Subobjective
import com.deepflowia.app.services.ReminderScheduler
import com.deepflowia.app.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    navController: NavController,
    goalId: String?,
    goalViewModel: GoalViewModel = viewModel()
) {
    val context = LocalContext.current
    val reminderScheduler = remember { ReminderScheduler(context) }
    val selectedGoal by goalViewModel.selectedGoal.collectAsState()
    val isEditing = goalId != null && goalId != "-1"
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var newSubobjectiveTitle by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }
    var subobjectiveToEdit by remember { mutableStateOf<Subobjective?>(null) }


    LaunchedEffect(goalId) {
        if (isEditing) {
            goalViewModel.getGoalById(goalId!!)
        } else {
            goalViewModel.getGoalById("-1") // Reset for new goal
        }
    }

    LaunchedEffect(selectedGoal) {
        // Update fields when a goal is loaded or changed
        if (isEditing && selectedGoal != null) {
            title = selectedGoal!!.title
            description = selectedGoal!!.description ?: ""
            category = selectedGoal!!.category ?: ""
            targetDate = selectedGoal!!.targetDate ?: ""
        } else {
            // Reset fields for a new goal
            title = ""
            description = ""
            category = ""
            targetDate = ""
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
                                scope.launch {
                                    val goalToSave = Goal(
                                        id = if (isEditing) selectedGoal?.id else null,
                                        title = title,
                                        description = description.ifBlank { null },
                                        category = category.ifBlank { null },
                                        targetDate = targetDate.ifBlank { null },
                                        userId = "" // Sera défini dans le ViewModel
                                    )

                                    val newGoalId = if (isEditing) {
                                        goalViewModel.updateGoal(goalToSave)
                                        goalToSave.id
                                    } else {
                                        goalViewModel.createGoal(goalToSave)
                                    }

                                    // Planifier ou annuler le rappel
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    try {
                                        val date = sdf.parse(targetDate)
                                        if (date != null && newGoalId != null) {
                                            reminderScheduler.scheduleDeadlineReminder(newGoalId, date.time, title)
                                        }
                                    } catch (e: Exception) {
                                        if (newGoalId != null) {
                                            reminderScheduler.cancelDeadlineReminder(newGoalId)
                                        }
                                    }
                                    navController.navigateUp()
                                }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de l'objectif") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie (optionnel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        targetDate = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (targetDate.isBlank()) "Sélectionner une date d'échéance" else "Échéance : $targetDate")
                }
            }

            if (isEditing && selectedGoal != null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sous-objectifs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(selectedGoal!!.subobjectives, key = { it.id!! }) { subobjective ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = subobjective.completed,
                            onCheckedChange = { isChecked ->
                                goalViewModel.updateSubobjective(subobjective.copy(completed = isChecked))
                            }
                        )
                        Text(
                            text = subobjective.title,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            subobjectiveToEdit = subobjective
                            showEditDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier le sous-objectif")
                        }
                        IconButton(onClick = { goalViewModel.deleteSubobjective(subobjective) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer le sous-objectif")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSubobjectiveTitle,
                            onValueChange = { newSubobjectiveTitle = it },
                            label = { Text("Nouveau sous-objectif") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (newSubobjectiveTitle.isNotBlank()) {
                                    val newSubobjective = Subobjective(
                                        title = newSubobjectiveTitle,
                                        description = null, // Pas de champ de description dans cet ajout rapide
                                        parentGoalId = selectedGoal!!.id!!,
                                        userId = "" // Sera défini dans le ViewModel
                                    )
                                    goalViewModel.createSubobjective(newSubobjective)
                                    newSubobjectiveTitle = "" // Clear input field
                                }
                            },
                            enabled = newSubobjectiveTitle.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Ajouter le sous-objectif")
                        }
                    }
                }
            }
        }
        if (showEditDialog && subobjectiveToEdit != null) {
            EditSubobjectiveDialog(
                subobjective = subobjectiveToEdit!!,
                onDismiss = { showEditDialog = false },
                onSave = { updatedSubobjective ->
                    goalViewModel.updateSubobjective(updatedSubobjective)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun EditSubobjectiveDialog(
    subobjective: Subobjective,
    onDismiss: () -> Unit,
    onSave: (Subobjective) -> Unit
) {
    var title by remember { mutableStateOf(subobjective.title) }
    var description by remember { mutableStateOf(subobjective.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le sous-objectif") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedSubobjective = subobjective.copy(
                        title = title,
                        description = description.ifBlank { null }
                    )
                    onSave(updatedSubobjective)
                },
                enabled = title.isNotBlank()
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
