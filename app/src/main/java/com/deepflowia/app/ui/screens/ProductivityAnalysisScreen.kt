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
import com.deepflowia.app.viewmodel.FocusViewModel
import com.deepflowia.app.viewmodel.GoalViewModel
import com.deepflowia.app.viewmodel.HabitViewModel
import com.deepflowia.app.viewmodel.TaskViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.compose.chart.entry.rememberChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.datetime.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityAnalysisScreen(
    taskViewModel: TaskViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel(),
    goalViewModel: GoalViewModel = viewModel(),
    focusViewModel: FocusViewModel = viewModel()
) {
    val factory = AIViewModel.AIViewModelFactory(taskViewModel, habitViewModel, goalViewModel, focusViewModel)
    val aiViewModel: AIViewModel = viewModel(factory = factory)
    val uiState by aiViewModel.uiState.collectAsState()

    // Lancer la récupération des données à la première composition
    LaunchedEffect(Unit) {
        aiViewModel.fetchLatestProductivityAnalysis()
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
                Button(
                    onClick = { aiViewModel.generateAndStoreProductivityAnalysis() },
                    enabled = !uiState.isAnalysisLoading
                ) {
                    if (uiState.isAnalysisLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Générer/Mettre à jour l'analyse")
                    }
                }
            }

            if (uiState.parsedAnalysis != null) {
                item {
                    ProductivityScoreCard(score = uiState.parsedAnalysis!!.score)
                }
                item {
                    AnalysisCard(
                        title = "Recommandations",
                        content = uiState.parsedAnalysis!!.recommendations
                    )
                }
                item {
                    AnalysisCard(
                        title = "Insights",
                        content = uiState.parsedAnalysis!!.insights
                    )
                }
            } else if (!uiState.isAnalysisLoading) {
                item {
                    Text("Aucune analyse disponible. Cliquez sur le bouton pour en générer une.")
                }
            }

            item {
                // Placeholder pour le profil psychologique
                AnalysisCard(
                    title = "Profil Psychologique (Bientôt disponible)",
                    content = "Cette section fournira une analyse de votre profil psychologique basée sur vos entrées de journal et vos réflexions pour vous aider à mieux comprendre votre santé mentale."
                )
            }

            item {
                ProductivityChart()
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
fun ProductivityChart() {
    // Données de démonstration pour le graphique
    val chartEntryModelProducer = rememberChartEntryModelProducer()

    LaunchedEffect(Unit) {
        // Simule des données pour les 7 derniers jours
        val entries = (0..6).map { day ->
            val date = LocalDate.now().minusDays(day.toLong())
            entryOf(date.toEpochDay().toFloat(), Random.nextInt(20, 101))
        }.reversed()
        chartEntryModelProducer.setEntries(entries)
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        LocalDate.ofEpochDay(value.toLong()).format(DateTimeFormatter.ofPattern("d MMM"))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Productivité des 7 derniers jours", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Chart(
                chart = columnChart(),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
            )
        }
    }
}
