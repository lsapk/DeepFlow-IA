package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.ReflectionViewModel
import java.net.URLDecoder
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionDetailScreen(
    navController: NavController,
    reflectionIdOrQuestion: String?,
    reflectionViewModel: ReflectionViewModel = viewModel()
) {
    val selectedReflection by reflectionViewModel.selectedReflection.collectAsState()

    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(reflectionIdOrQuestion) {
        if (reflectionIdOrQuestion != null && reflectionIdOrQuestion != "-1") {
            try {
                // Essayer de parser comme UUID pour voir si c'est un ID
                UUID.fromString(reflectionIdOrQuestion)
                isEditing = true
                reflectionViewModel.getReflectionById(reflectionIdOrQuestion)
            } catch (e: IllegalArgumentException) {
                // Sinon, c'est une nouvelle question
                isEditing = false
                question = URLDecoder.decode(reflectionIdOrQuestion, "UTF-8")
                answer = ""
                reflectionViewModel.getReflectionById("-1")
            }
        } else {
            // Création libre
            isEditing = false
            question = "Réflexion Libre"
            answer = ""
            reflectionViewModel.getReflectionById("-1")
        }
    }

    LaunchedEffect(selectedReflection) {
        if (isEditing && selectedReflection != null) {
            question = selectedReflection!!.question
            answer = selectedReflection!!.answer
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Modifier la Réflexion" else "Nouvelle Réflexion") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            reflectionViewModel.deleteReflection(selectedReflection!!)
                            navController.navigateUp()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                        }
                    }
                    IconButton(
                        onClick = {
                            if (answer.isNotBlank()) {
                                if (isEditing) {
                                    val updatedReflection = selectedReflection!!.copy(question = question, answer = answer)
                                    reflectionViewModel.updateReflection(updatedReflection)
                                } else {
                                    reflectionViewModel.addReflection(question, answer)
                                }
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
            OutlinedTextField(
                value = question,
                onValueChange = { if (!isEditing) question = it },
                label = { Text("Question") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = isEditing
            )
            Spacer(modifier = Modifier.height(16.dp))
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
