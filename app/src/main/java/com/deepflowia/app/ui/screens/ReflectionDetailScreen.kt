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
import com.deepflowia.app.models.DailyReflection
import com.deepflowia.app.viewmodel.ReflectionViewModel
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionDetailScreen(
    navController: NavController,
    reflectionIdOrQuestion: String?,
    reflectionViewModel: ReflectionViewModel = viewModel()
) {
    val selectedReflection by reflectionViewModel.selectedReflection.collectAsState()
    val isEditing = selectedReflection != null

    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    LaunchedEffect(reflectionIdOrQuestion) {
        if (reflectionIdOrQuestion != null && reflectionIdOrQuestion != "-1") {
            try {
                // Essayer de décoder comme ID UUID (modification)
                reflectionViewModel.getReflectionById(reflectionIdOrQuestion)
            } catch (e: Exception) {
                // Sinon, traiter comme une question (création)
                question = URLDecoder.decode(reflectionIdOrQuestion, "UTF-8")
                reflectionViewModel.getReflectionById("-1") // Réinitialiser la sélection
            }
        } else {
            reflectionViewModel.getReflectionById("-1") // Cas de la création libre
        }
    }

    LaunchedEffect(selectedReflection) {
        if (selectedReflection != null) {
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
                                    reflectionViewModel.addReflection(question.ifEmpty { "Réflexion Libre" }, answer)
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
            if (isEditing || question.isNotEmpty()) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isEditing // On ne peut pas modifier la question d'une réflexion existante
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
