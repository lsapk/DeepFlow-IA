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

import com.deepflowia.app.models.Task
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
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.*

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
                val tasks by taskViewModel.tasks.collectAsState()
                ProductivityChart(tasks = tasks)
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
fun ProductivityChart(tasks: List<Task>) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val weekData = (0..6).map { dayIndex ->
        val date = today.minus(dayIndex, DateTimeUnit.DAY)
        val count = tasks.count { task ->
            task.completed && task.updatedAt?.let {
                Instant.parse(it).toLocalDateTime(TimeZone.UTC).date == date
            } ?: false
        }
        entryOf(6 - dayIndex, count)
    }

    val chartEntryModelProducer = ChartEntryModelProducer(weekData)

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val daysAgo = 6 - value.toInt()
        val date = today.minus(daysAgo, DateTimeUnit.DAY)
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH)
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tâches complétées (7 derniers jours)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (weekData.any { it.y > 0 }) {
                 Chart(
                    chart = columnChart(),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune donnée de tâche pour la semaine.")
                }
            }
        }
    }
}