package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.viewmodel.ProductivityAnalysisViewModel

@Composable
fun ProductivityAnalysisScreen(viewModel: ProductivityAnalysisViewModel = viewModel()) {
    val state by viewModel.analysisState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProductivityAnalysis()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Analyse de Productivité") })
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.errorMessage != null) {
                    // Afficher l'erreur
                    Text(
                        text = "Erreur: ${state.errorMessage}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Afficher les données
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ProductivityScoreCard(score = state.productivityScore)
                        }

                        item {
                            InsightsCard(insights = state.insights)
                        }

                        item {
                            Text("Recommandations", style = MaterialTheme.typography.titleLarge)
                        }
                        items(state.recommendations) { recommendation ->
                            RecommendationCard(recommendation = recommendation)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ProductivityScoreCard(score: Float) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Votre Score de Productivité", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${score.toInt()}", style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
fun InsightsCard(insights: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Aperçus de l'IA", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(insights)
        }
    }
}

@Composable
fun RecommendationCard(recommendation: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(text = recommendation, modifier = Modifier.padding(16.dp))
    }
}
