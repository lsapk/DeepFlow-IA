package com.deepflowia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.R
import com.deepflowia.app.ui.components.ActivityRing
import com.deepflowia.app.ui.components.glassmorphism
import com.deepflowia.app.ui.theme.color_blue
import com.deepflowia.app.ui.theme.color_green
import com.deepflowia.app.ui.theme.color_orange
import com.deepflowia.app.ui.theme.color_teal
import com.deepflowia.app.ui.theme.color_yellow
import com.deepflowia.app.viewmodel.HomeReportState
import com.deepflowia.app.viewmodel.HomeViewModel

data class Feature(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToReflection: () -> Unit,
) {
    val reportState by homeViewModel.reportState.collectAsState()

    val features = listOf(
        Feature("Tâches", Icons.Outlined.Checklist, Color(0xFF0A84FF), onNavigateToTasks),
        Feature("Habitudes", Icons.Outlined.AllInclusive, Color(0xFF34C759), onNavigateToHabits),
        Feature("Objectifs", Icons.Outlined.Flag, Color(0xFFFF9500), onNavigateToGoals),
        Feature("Journal", Icons.Outlined.AutoStories, Color(0xFFFF3B30), onNavigateToJournal),
        Feature("Focus", Icons.Outlined.Tune, Color(0xFF5856D6), onNavigateToFocus),
        Feature("Réflexion", Icons.Outlined.Psychology, Color(0xFFFF2D55), onNavigateToReflection)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mesh_background), // Assurez-vous d'avoir une image de fond
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Accueil", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Profil",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent // Fond transparent
                    )
                )
            },
            containerColor = Color.Transparent, // Fond transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                ReportSection(state = reportState)
                Spacer(modifier = Modifier.height(24.dp))
                FeaturesGrid(features = features, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ReportSection(state: HomeReportState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Rapport Quotidien",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActivityRing(
                progress = (state.focusMinutesToday / 60f).coerceIn(0f, 1f),
                label = "Focus",
                value = "${state.focusMinutesToday}m",
                gradient = listOf(color_orange, color_yellow)
            )
            ActivityRing(
                progress = (state.tasksCompletedToday / 5f).coerceIn(0f, 1f),
                label = "Tâches",
                value = "${state.tasksCompletedToday}",
                gradient = listOf(color_blue, color_teal)
            )
            ActivityRing(
                progress = (state.habitsCompletedToday / 5f).coerceIn(0f, 1f),
                label = "Habitudes",
                value = "${state.habitsCompletedToday}",
                gradient = listOf(color_green, color_green.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
fun FeaturesGrid(features: List<Feature>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.weight(1.5f), // Ligne plus grande
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[0], modifier = Modifier.weight(1f))
            FeatureCard(feature = features[1], modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f), // Ligne plus petite
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[2], modifier = Modifier.weight(1f))
            FeatureCard(feature = features[3], modifier = Modifier.weight(1f))
            FeatureCard(feature = features[4], modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f), // Ligne plus petite
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[5], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun FeatureCard(feature: Feature, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .glassmorphism()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = feature.onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                tint = feature.color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}