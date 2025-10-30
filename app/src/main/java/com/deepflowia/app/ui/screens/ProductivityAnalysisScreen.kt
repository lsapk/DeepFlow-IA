package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.viewmodel.AIViewModel
import com.deepflowia.app.viewmodel.AIMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityAnalysisScreen(aiViewModel: AIViewModel = viewModel()) {
    val uiState by aiViewModel.uiState.collectAsState()

    // Lancer la récupération des données à la première composition
    LaunchedEffect(Unit) {
        aiViewModel.fetchProductivityAnalysis()
    }

    // Fonction d'aide pour extraire le score de la réponse de l'IA
    val productivityScore = remember(uiState.productivityAnalysis) {
        val analysisText = uiState.productivityAnalysis?.analysisData ?: ""
        if (analysisText.startsWith("SCORE:")) {
            analysisText.substringAfter("SCORE:").trim().split(" ")[0].toIntOrNull() ?: 0
        } else {
            0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyse de Productivité") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(onClick = { aiViewModel.generateAndStoreProductivityAnalysis() }) {
                    Text("Générer/Mettre à jour l'analyse")
                }
            }

            item {
                ProductivityScoreCard(score = productivityScore)
            }

            item {
                AnalysisCard(
                    title = "Analyse et Recommandations",
                    content = uiState.productivityAnalysis?.analysisData
                        ?: "Aucune analyse disponible. Cliquez sur le bouton pour en générer une."
                )
            }

            item {
                // Placeholder pour le profil psychologique
                AnalysisCard(
                    title = "Profil Psychologique (Bientôt disponible)",
                    content = "Cette section fournira une analyse de votre profil psychologique basée sur vos entrées de journal et vos réflexions pour vous aider à mieux comprendre votre santé mentale."
                )
            }

            item {
                GraphPlaceholder()
            }
        }
    }
}

@Composable
fun ProductivityScoreCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Score de Productivité Global",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AnalysisCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun GraphPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Graphiques à venir (ex: Vico)",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}