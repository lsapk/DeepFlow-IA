package com.deepflowia.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.DailyReflection
import com.deepflowia.app.viewmodel.ReflectionViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(
    navController: NavController,
    reflectionViewModel: ReflectionViewModel = viewModel()
) {
    val reflections by reflectionViewModel.reflections.collectAsState()
    val questions by reflectionViewModel.questions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réflexion Quotidienne") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("reflection_stats") }) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Statistiques")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("reflection_detail/-1") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle Réflexion")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            QuestionPager(
                questions = questions,
                onQuestionSelected = { question ->
                    val encodedQuestion = URLEncoder.encode(question, "UTF-8")
                    navController.navigate("reflection_detail/$encodedQuestion")
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Mes Réflexions", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reflections) { reflection ->
                    ReflectionItem(
                        reflection = reflection,
                        onClick = {
                            navController.navigate("reflection_detail/${reflection.id}")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionPager(questions: List<String>, onQuestionSelected: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { questions.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Désactive l'effet d'ondulation
                    onClick = { onQuestionSelected(questions[page]) }
                ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = questions[page],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReflectionItem(reflection: DailyReflection, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Désactive l'effet d'ondulation
                onClick = { onClick() }
            ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(reflection.question, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(reflection.answer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
