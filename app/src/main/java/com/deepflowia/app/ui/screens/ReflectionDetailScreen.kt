package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.ReflectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionDetailScreen(
    navController: NavController,
    question: String?,
    reflectionViewModel: ReflectionViewModel = viewModel()
) {
    var answer by remember { mutableStateOf("") }
    val decodedQuestion = remember(question) {
        question?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (decodedQuestion.isNotEmpty()) "Nouvelle Réflexion" else "Réflexion Libre") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (answer.isNotBlank()) {
                                reflectionViewModel.addReflection(decodedQuestion.ifEmpty { "Réflexion Libre" }, answer)
                                navController.navigateUp()
                            }
                        },
                        enabled = answer.isNotBlank()
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Sauvegarder")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (decodedQuestion.isNotEmpty()) {
                Text(
                    text = decodedQuestion,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Votre réponse...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 15
            )
        }
    }
}
