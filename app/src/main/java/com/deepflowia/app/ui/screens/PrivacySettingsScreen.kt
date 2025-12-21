package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres de confidentialité IA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .padding(16.dp)
        ) {
            item {
                Text(
                    "Contrôlez quelles données l'assistant IA peut utiliser pour personnaliser ses réponses et analyses.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            item {
                PermissionSwitch(
                    title = "Accès aux Tâches",
                    subtitle = "Inclure les tâches en cours et terminées.",
                    checked = settings.canAccessTasks,
                    onCheckedChange = { settingsViewModel.setCanAccessTasks(it) }
                )
            }
             item {
                PermissionSwitch(
                    title = "Accès aux Habitudes",
                    subtitle = "Inclure les habitudes actives et archivées.",
                    checked = settings.canAccessHabits,
                    onCheckedChange = { settingsViewModel.setCanAccessHabits(it) }
                )
            }
             item {
                PermissionSwitch(
                    title = "Accès aux Objectifs",
                    subtitle = "Inclure les objectifs en cours et terminés.",
                    checked = settings.canAccessGoals,
                    onCheckedChange = { settingsViewModel.setCanAccessGoals(it) }
                )
            }
             item {
                PermissionSwitch(
                    title = "Accès au Journal",
                    subtitle = "Inclure les entrées de journal et réflexions.",
                    checked = settings.canAccessJournal,
                    onCheckedChange = { settingsViewModel.setCanAccessJournal(it) }
                )
            }
             item {
                PermissionSwitch(
                    title = "Accès aux Sessions de Focus",
                    subtitle = "Inclure l'historique des sessions de concentration.",
                    checked = settings.canAccessFocus,
                    onCheckedChange = { settingsViewModel.setCanAccessFocus(it) }
                )
            }
             item {
                PermissionSwitch(
                    title = "Accès aux Données Personnelles",
                    subtitle = "Inclure prénom, âge, etc. (si renseigné).",
                    checked = settings.canAccessPersonalInfo,
                    onCheckedChange = { settingsViewModel.setCanAccessPersonalInfo(it) }
                )
            }
        }
    }
}

@Composable
fun PermissionSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
