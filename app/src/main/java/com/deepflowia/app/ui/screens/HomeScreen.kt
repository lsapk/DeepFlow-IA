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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Feature(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToReflection: () -> Unit,
    onNavigateToProductivityAnalysis: () -> Unit,
) {
    val features = listOf(
        Feature("Tâches", Icons.Filled.List, Color(0xFF6A1B9A), onNavigateToTasks),
        Feature("Habitudes", Icons.Default.SyncAlt, Color(0xFF0277BD), onNavigateToHabits),
        Feature("Objectifs", Icons.Default.CheckCircleOutline, Color(0xFF2E7D32), onNavigateToGoals),
        Feature("Journal", Icons.Default.Book, Color(0xFFD84315), onNavigateToJournal),
        Feature("Focus", Icons.Default.CenterFocusStrong, Color(0xFFC62828), onNavigateToFocus),
        Feature("Réflexion", Icons.Default.SelfImprovement, Color(0xFF283593), onNavigateToReflection),
        Feature("Analyse IA", Icons.Default.Analytics, Color(0xFF00695C), onNavigateToProductivityAnalysis)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accueil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            ReportCard()
            Spacer(modifier = Modifier.height(24.dp))
            FeaturesGrid(features = features, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ReportCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rapport",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Suivi des 7 derniers jours - Objectif atteint à 85%",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[0], modifier = Modifier.weight(1f))
            FeatureCard(feature = features[1], modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[2], modifier = Modifier.weight(1f))
            FeatureCard(feature = features[3], modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[4], modifier = Modifier.weight(1f))
            FeatureCard(feature = features[5], modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(feature = features[6], modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f)) // Pour garder l'alignement
        }
    }
}

@Composable
fun FeatureCard(feature: Feature, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Désactive l'effet d'ondulation
                onClick = feature.onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(feature.color.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = feature.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
