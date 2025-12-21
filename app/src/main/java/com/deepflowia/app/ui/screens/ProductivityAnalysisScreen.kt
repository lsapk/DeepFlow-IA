package com.deepflowia.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.viewmodel.AIViewModel
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.FocusViewModel
import com.deepflowia.app.viewmodel.GoalViewModel
import com.deepflowia.app.viewmodel.HabitViewModel
import com.deepflowia.app.viewmodel.JournalViewModel
import com.deepflowia.app.viewmodel.SettingsViewModel
import com.deepflowia.app.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityAnalysisScreen(
    taskViewModel: TaskViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel(),
    goalViewModel: GoalViewModel = viewModel(),
    focusViewModel: FocusViewModel = viewModel(),
    journalViewModel: JournalViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val factory = AIViewModel.AIViewModelFactory(
        taskViewModel = taskViewModel,
        habitViewModel = habitViewModel,
        goalViewModel = goalViewModel,
        focusViewModel = focusViewModel,
        journalViewModel = journalViewModel,
        settingsViewModel = settingsViewModel,
        authViewModel = authViewModel
    )
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
fun CustomBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val maxValue = 100f // Productivité en pourcentage

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Hauteur fixe pour le graphique
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (label, value) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .fillMaxHeight(fraction = value / maxValue)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        // Ligne de l'axe X
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            drawLine(
                color = Color.Gray,
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = size.width, y = 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun ProductivityChart() {
    // Données de démonstration pour le graphique
    val demoData = remember {
        (0..6).map { day ->
            val date = LocalDate.now().minusDays(day.toLong())
            val label = date.format(DateTimeFormatter.ofPattern("d MMM"))
            val value = Random.nextInt(20, 101).toFloat()
            label to value
        }.reversed()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Productivité des 7 derniers jours", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            CustomBarChart(data = demoData, modifier = Modifier.fillMaxWidth())
        }
    }
}
