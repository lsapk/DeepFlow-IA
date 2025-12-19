package com.deepflowia.app.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.deepflowia.app.R
import com.deepflowia.app.ui.components.glassmorphism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISelectionScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mesh_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Fonctionnalités IA") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                        .shadow(
                            elevation = 24.dp,
                            shape = MaterialTheme.shapes.large,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                    icon = Icons.Outlined.Chat,
                    title = "Assistant IA",
                    description = "Discutez, brainstormez et créez des tâches avec votre assistant personnel.",
                    onClick = { navController.navigate("ai_chat") }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Analytics,
                        title = "Analyse de Productivité",
                        description = "Obtenez un score et des recommandations.",
                        onClick = { navController.navigate("productivity_analysis") }
                    )
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Psychology,
                        title = "Profil Psychologique",
                        description = "Bientôt disponible.",
                        onClick = { /* No action */ },
                        enabled = false
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardColor = if (enabled) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    } else {
        Color.Transparent // Pas de couleur de fond pour la carte désactivée
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .glassmorphism(
                color = cardColor
            )
            .let {
                if (!enabled) {
                    it.background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6A11CB).copy(alpha = 0.3f),
                                Color(0xFF2575FC).copy(alpha = 0.3f)
                            )
                        )
                    )
                } else {
                    it
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
