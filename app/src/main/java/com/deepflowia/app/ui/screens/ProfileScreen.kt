package com.deepflowia.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.AIViewModel
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.ThemeViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel(),
    aiViewModel: AIViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val notifications = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres & Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
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
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader()
            Spacer(modifier = Modifier.height(24.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                AIProfileSection(aiViewModel)
                Spacer(modifier = Modifier.height(24.dp))
                AccountSection(navController)
                Spacer(modifier = Modifier.height(24.dp))
                PreferencesSection(isDarkTheme, { themeViewModel.toggleTheme() }, notifications)
                Spacer(modifier = Modifier.height(24.dp))
                AdminSection(navController, authViewModel)
                SupportSection(navController)
                Spacer(modifier = Modifier.height(32.dp))
                LogoutButton(
                    onClick = {
                        authViewModel.signOut()
                        onNavigateToLogin()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AdminSection(navController: NavController, authViewModel: AuthViewModel) {
    val userRole by authViewModel.userRole.collectAsState()

    if (userRole == "admin") {
        Column {
            Text(
                "ADMINISTRATION",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Panneau d'administration",
                    onClick = { navController.navigate("admin_panel") }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileHeader(authViewModel: AuthViewModel = viewModel()) {
    val userEmail by authViewModel.userEmail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userEmail ?: "Utilisateur",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AIProfileSection(aiViewModel: AIViewModel) {
    val aiState by aiViewModel.uiState.collectAsState()
    val profile = aiState.personalityProfile

    // Helper to parse title/description from JSON string
    fun parseProfileData(jsonString: String): Pair<String, String> {
        return try {
            val json = Json.parseToJsonElement(jsonString).jsonObject
            val title = json["titre"]?.jsonPrimitive?.content ?: "Profil IA"
            val description = json["description"]?.jsonPrimitive?.content ?: "Générez votre profil pour en savoir plus."
            Pair(title, description)
        } catch (e: Exception) {
            Pair("Profil IA", "Générez votre profil pour en savoir plus.")
        }
    }

    val (title, description) = if (profile != null) {
        parseProfileData(profile.profileData)
    } else {
        Pair("Profil de Productivité", "Générez votre profil pour découvrir votre style de productivité.")
    }

    Column {
        Text("PROFIL IA", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { aiViewModel.generateAndStorePersonalityProfile() },
                    enabled = !aiState.isLoading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (aiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (profile == null) "Générer mon profil" else "Rafraîchir")
                    }
                }
            }
        }
    }
}


@Composable
fun AccountSection(navController: NavController) {
    Column {
        Text("COMPTE", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Modifier le profil",
                onClick = { navController.navigate("edit_profile") }
            )
        }
    }
}

@Composable
fun PreferencesSection(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    notifications: MutableState<Boolean>
) {
    Column {
        Text("PRÉFÉRENCES DE L'APPLICATION", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingsItem(
                icon = Icons.Default.Brightness4,
                title = "Mode sombre",
                trailingContent = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeToggle() }
                    )
                },
                onClick = { onThemeToggle() }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(
                icon = Icons.Default.NotificationsNone,
                title = "Notifications",
                trailingContent = {
                    Switch(
                        checked = notifications.value,
                        onCheckedChange = { notifications.value = it }
                    )
                },
                onClick = { notifications.value = !notifications.value }
            )
        }
    }
}

@Composable
fun SupportSection(navController: NavController) {
    val context = LocalContext.current
    Column {
        Text("SUPPORT", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingsItem(icon = Icons.Filled.HelpOutline, title = "Aide/FAQ", onClick = { navController.navigate("help") })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(icon = Icons.Default.MailOutline, title = "Contact", onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:Deepflow.ia@gmail.com")
                }
                context.startActivity(intent)
            })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Notre Site Web",
                onClick = {
                    val url = "https://gemini-flow-pwa.onrender.com"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                }
            )
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(icon = Icons.Filled.ReceiptLong, title = "Conditions d'utilisation", onClick = { navController.navigate("terms") })
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    trailingContent: @Composable (() -> Unit)? = {
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    },
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Désactive l'effet d'ondulation
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f))
        trailingContent?.let { it() }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)) // Garder une couleur distincte pour la déconnexion
    ) {
        Icon(Icons.Filled.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Se déconnecter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}