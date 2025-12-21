package com.deepflowia.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.R
import com.deepflowia.app.ui.components.glassmorphism
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil & Paramètres", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(authViewModel)
            Spacer(modifier = Modifier.height(24.dp))

            AccountSection(navController, authViewModel)
            Spacer(modifier = Modifier.height(16.dp))

            PreferencesSection(navController, isDarkTheme, { themeViewModel.toggleTheme() })
            Spacer(modifier = Modifier.height(16.dp))

            SupportSection(navController)
            Spacer(modifier = Modifier.height(16.dp))

            LogoutButton(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = {
                    authViewModel.signOut()
                }
            )
        }
    }
}

@Composable
fun ProfileHeader(authViewModel: AuthViewModel) {
    val userEmail by authViewModel.userEmail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Avatar de l'utilisateur",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userEmail ?: "Utilisateur",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "DeepFlow IA Pro",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AccountSection(navController: NavController, authViewModel: AuthViewModel) {
    val userRole by authViewModel.userRole.collectAsState()

    SettingsCard(title = "Compte") {
        SettingsItem(
            icon = Icons.Outlined.AccountCircle,
            title = "Modifier le profil",
            onClick = { navController.navigate("edit_profile") }
        )
        if (userRole == "admin") {
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            SettingsItem(
                icon = Icons.Filled.AdminPanelSettings,
                title = "Panneau d'administration",
                onClick = { navController.navigate("admin_panel") }
            )
        }
    }
}

@Composable
fun PreferencesSection(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    SettingsCard(title = "Préférences") {
        SettingsItem(
            icon = Icons.Outlined.DarkMode,
            title = "Mode sombre",
            trailingContent = {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            },
            onClick = { onThemeToggle() }
        )
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.Outlined.Notifications,
            title = "Notifications",
            onClick = { navController.navigate("notification_settings") }
        )
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.Outlined.PrivacyTip,
            title = "Confidentialité IA",
            onClick = { navController.navigate("privacy_settings") }
        )
    }
}

@Composable
fun SupportSection(navController: NavController) {
    val context = LocalContext.current
    SettingsCard("Support & Infos") {
        SettingsItem(
            icon = Icons.Outlined.HelpOutline,
            title = "Aide & FAQ",
            onClick = { navController.navigate("help") }
        )
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.Outlined.MailOutline,
            title = "Nous contacter",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:Deepflow.ia@gmail.com")
                }
                context.startActivity(intent)
            }
        )
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.Outlined.Language,
            title = "Notre Site Web",
            onClick = {
                val url = "https://deepflowia.lovable.app/"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            }
        )
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            title = "Conditions d'utilisation",
            onClick = { navController.navigate("terms") }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(28.dp), clip = true)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    trailingContent: @Composable (() -> Unit)? = {
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    },
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        trailingContent?.let { it() }
    }
}


@Composable
fun LogoutButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.error
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Déconnexion")
            Spacer(modifier = Modifier.width(12.dp))
            Text("Se déconnecter", fontWeight = FontWeight.Bold)
        }
    }
}
