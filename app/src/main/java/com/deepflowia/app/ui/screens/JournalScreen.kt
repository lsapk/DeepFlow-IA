package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.models.JournalEntry
import com.deepflowia.app.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    journalViewModel: JournalViewModel = viewModel()
) {
    val journalEntries = journalViewModel.journalEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add new journal entry */ }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn {
                items(journalEntries.value) { journalEntry ->
                    JournalItem(journalEntry)
                }
            }
        }
    }
}

@Composable
fun JournalItem(journalEntry: JournalEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = journalEntry.title, style = MaterialTheme.typography.titleMedium)
            Text(text = journalEntry.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}