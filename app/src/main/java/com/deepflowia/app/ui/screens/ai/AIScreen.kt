package com.deepflowia.app.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.ChatMessage
import com.deepflowia.app.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel(),
    goalViewModel: GoalViewModel = viewModel(),
    focusViewModel: FocusViewModel = viewModel()
) {
    val factory = AIViewModel.AIViewModelFactory(taskViewModel, habitViewModel, goalViewModel, focusViewModel)
    val aiViewModel: AIViewModel = viewModel(factory = factory)
    val uiState by aiViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to the bottom when a new message arrives
    LaunchedEffect(uiState.conversation.size) {
        if (uiState.conversation.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.conversation.size - 1)
            }
        }
    }

    // Show confirmation dialog when a suggested action is available
    if (uiState.suggestedAction != null) {
        ConfirmationDialog(
            action = uiState.suggestedAction!!,
            onConfirm = { aiViewModel.confirmSuggestedAction() },
            onDismiss = { aiViewModel.clearSuggestedAction() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistant IA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ModeSelector(
                    currentMode = uiState.currentMode,
                    onModeSelected = { aiViewModel.setMode(it) }
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    items(uiState.conversation) { message ->
                        MessageBubble(message)
                    }
                }
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                       CircularProgressIndicator()
                    }
                }
                MessageInput(onSendMessage = { message ->
                    aiViewModel.sendMessage(message)
                })
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(currentMode: AIMode, onModeSelected: (AIMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AIMode.values().forEach { mode ->
            FilterChip(
                selected = mode == currentMode,
                onClick = { onModeSelected(mode) },
                label = { Text(mode.name.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}


@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isFromUser) 0.dp else 16.dp
                    )
                )
                .background(if (message.isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    action: com.deepflowia.app.models.SuggestedAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer une nouvelle ${action.type} ?") },
        text = {
            Column {
                Text("Titre : ${action.titre}", style = MaterialTheme.typography.bodyLarge)
                action.details?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Détails : $it", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
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

@Composable
fun MessageInput(onSendMessage: (String) -> Unit) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Votre message...") },
            modifier = Modifier.weight(1f),
            maxLines = 5
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.text.isNotBlank()) {
                    onSendMessage(text.text)
                    text = TextFieldValue("")
                }
            },
            enabled = text.text.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
        }
    }
}
