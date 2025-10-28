package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.JournalEntry
import com.deepflowia.app.viewmodel.JournalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    journalId: String?,
    navController: NavController,
    journalViewModel: JournalViewModel = viewModel()
) {
    val isNewJournal = journalId == "-1"
    val journalToEdit = if (!isNewJournal) {
        journalViewModel.journalEntries.collectAsState().value.find { it.id == journalId }
    } else {
        null
    }

    var title by remember(journalToEdit) { mutableStateOf(journalToEdit?.title ?: "") }
    var content by remember(journalToEdit) { mutableStateOf(journalToEdit?.content ?: "") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNewJournal) "Nouvelle Entrée" else "Modifier l'entrée", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!isNewJournal) {
                        IconButton(onClick = {
                            journalToEdit?.let {
                                coroutineScope.launch {
                                    journalViewModel.deleteJournalEntry(it)
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
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenu") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 10
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (isNewJournal) {
                            journalViewModel.createJournalEntry(
                                JournalEntry(
                                    title = title,
                                    content = content,
                                    userId = "" // Will be set by ViewModel
                                )
                            )
                        } else {
                            journalToEdit?.let {
                                val updatedJournal = it.copy(
                                    title = title,
                                    content = content
                                )
                                journalViewModel.updateJournalEntry(updatedJournal)
                            }
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text(if (isNewJournal) "Créer l'entrée" else "Enregistrer les modifications")
            }
        }
    }
}
