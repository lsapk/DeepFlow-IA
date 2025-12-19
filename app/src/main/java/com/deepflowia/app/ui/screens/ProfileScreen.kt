package com.deepflowia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.content.Intent
import android.net.Uri
import com.deepflowia.app.viewmodel.AIViewModel
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.ThemeViewModel
import com.deepflowia.app.ui.theme.color_green
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
                AccountSection(navController)
                Spacer(modifier = Modifier.height(24.dp))
                PreferencesSection(navController, isDarkTheme, { themeViewModel.toggleTheme() }, notifications)
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
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userEmail ?: "Utilisateur",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun AccountSection(navController: NavController) {
    SettingsGroup(title = "COMPTE") {
        SettingsItem(
            icon = Icons.Outlined.Edit,
            title = "Modifier le profil",
            onClick = { navController.navigate("edit_profile") }
        )
    }
}

@Composable
fun PreferencesSection(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    notifications: MutableState<Boolean>
) {
    SettingsGroup(title = "PRÉFÉRENCES DE L'APPLICATION") {
        SettingsItem(
            icon = Icons.Outlined.Brightness4,
            title = "Mode sombre",
            trailingContent = {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = color_green,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            onClick = { onThemeToggle() }
        )
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(
            icon = Icons.Outlined.NotificationsNone,
            title = "Notifications",
            onClick = { navController.navigate("notification_settings") }
        )
    }
}

@Composable
fun SupportSection(navController: NavController) {
    val context = LocalContext.current
    SettingsGroup(title = "SUPPORT") {
        SettingsItem(icon = Icons.Outlined.HelpOutline, title = "Aide/FAQ", onClick = { navController.navigate("help") })
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(icon = Icons.Outlined.MailOutline, title = "Contact", onClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:Deepflow.ia@gmail.com")
            }
            context.startActivity(intent)
        })
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(
            icon = Icons.Outlined.Language,
            title = "Notre Site Web",
            onClick = {
                val url = "https://deepflow.fr"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            }
        )
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingsItem(icon = Icons.Outlined.ReceiptLong, title = "Conditions d'utilisation", onClick = { navController.navigate("terms") })
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                content()
            }
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
