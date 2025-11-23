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
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val habits by habitViewModel.filteredHabits.collectAsState()
    val completedHabitIds by habitViewModel.habitCompletions.collectAsState()
    val showArchived by habitViewModel.showArchived.collectAsState()
    val showAll by habitViewModel.showAllHabits.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habitudes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("habit_stats") }) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Statistiques")
                    }
                    TextButton(onClick = { habitViewModel.toggleShowArchived() }) {
                        Text(if (showArchived) "Voir actives" else "Voir archivées")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("habit_detail/-1") },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une habitude", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FilterChip(
                    selected = !showAll,
                    onClick = { habitViewModel.toggleShowAllHabits() },
                    label = { Text(if (showAll) "Voir seulement aujourd'hui" else "Voir toutes") }
                )
            }
            val allHabitsCompleted = habits.isNotEmpty() && habits.all { completedHabitIds.contains(it.id) }
            if (allHabitsCompleted && !showAll) { // Only show congratulations if viewing today's habits
                Text(
                    text = "Félicitations ! Vous avez complété toutes vos habitudes du jour.",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
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
                        onEdit = { navController.navigate("habit_detail/${habit.id}") },
                        onDelete = { habitViewModel.deleteHabit(habit) },
                        onArchive = { habitViewModel.toggleHabitArchived(habit) }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onHabitCompleted: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Désactive l'effet d'ondulation
                onClick = onEdit
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        text = { Text(if (habit.isArchived) "Désarchiver" else "Archiver") },
                        onClick = {
                            onArchive()
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
