package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.viewmodel.AIMode
import com.deepflowia.app.viewmodel.AIViewModel
import com.deepflowia.app.viewmodel.ChatMessage

@Composable
fun AICompanionScreen(aiViewModel: AIViewModel = viewModel()) {
    val conversationHistory by aiViewModel.conversationHistory.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    val errorMessage by aiViewModel.errorMessage.collectAsState()
    val currentMode by aiViewModel.aiMode.collectAsState()

    var textState by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistant IA") },
                actions = {
                    ModeSelector(selectedMode = currentMode, onModeSelected = { aiViewModel.setAIMode(it) })
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Zone de conversation
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = true
                ) {
                    items(conversationHistory.reversed()) { message ->
                        ChatMessageItem(message)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Champ de saisie
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Posez votre question...") },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (textState.isNotBlank()) {
                                aiViewModel.sendMessage(textState)
                                textState = ""
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Envoyer")
                        }
                    }
                }

                // Affichage des erreurs
                errorMessage?.let {
                    Snackbar(
                        action = {
                            Button(onClick = { aiViewModel.clearError() }) {
                                Text("OK")
                            }
                        }
                    ) { Text(it) }
                }
            }
        }
    )
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(selectedMode: AIMode, onModeSelected: (AIMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedMode.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AIMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name) },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}
