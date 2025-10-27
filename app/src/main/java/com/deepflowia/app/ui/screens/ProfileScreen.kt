package com.deepflowia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val darkMode = remember { mutableStateOf(false) }
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
                    containerColor = Color(0xFFF0F0F0)
                )
            )
        },
        containerColor = Color(0xFFF0F0F0)
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
                AccountSection()
                Spacer(modifier = Modifier.height(24.dp))
                PreferencesSection(darkMode, notifications)
                Spacer(modifier = Modifier.height(24.dp))
                SupportSection()
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
fun ProfileHeader() {
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
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Utilisateur",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AccountSection() {
    Column {
        Text("COMPTE", fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Changer le mot de passe",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun PreferencesSection(
    darkMode: MutableState<Boolean>,
    notifications: MutableState<Boolean>
) {
    Column {
        Text("PRÉFÉRENCES DE L'APPLICATION", fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            SettingsItem(
                icon = Icons.Default.Brightness4,
                title = "Mode sombre",
                trailingContent = {
                    Switch(
                        checked = darkMode.value,
                        onCheckedChange = { darkMode.value = it }
                    )
                },
                onClick = { darkMode.value = !darkMode.value }
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
fun SupportSection() {
    Column {
        Text("SUPPORT", fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            SettingsItem(icon = Icons.Filled.HelpOutline, title = "Aide/FAQ", onClick = { /* TODO */ })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(icon = Icons.Default.MailOutline, title = "Contact", onClick = { /* TODO */ })
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(icon = Icons.Filled.ReceiptLong, title = "Conditions d'utilisation", onClick = { /* TODO */ })
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    trailingContent: @Composable (() -> Unit)? = {
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    },
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
    ) {
        Icon(Icons.Filled.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Se déconnecter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
