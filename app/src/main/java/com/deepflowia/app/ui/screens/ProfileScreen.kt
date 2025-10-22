package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    val darkMode = remember { mutableStateOf(false) }
    val notifications = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Informations utilisateur")
        // Add user info fields here

        Spacer(modifier = Modifier.height(16.dp))
        Text("COMPTE")
        Button(onClick = { /* TODO: Change password */ }) {
            Text("Changer le mot de passe")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("PRÉFÉRENCES L'APPLICATION")
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mode sombre")
            Switch(
                checked = darkMode.value,
                onCheckedChange = { darkMode.value = it }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifications")
            Switch(
                checked = notifications.value,
                onCheckedChange = { notifications.value = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("SUPPORT")
        Button(onClick = { /* TODO: Navigate to Help/FAQ */ }) {
            Text("Aide/FAQ")
        }
        Button(onClick = { /* TODO: Navigate to Contact */ }) {
            Text("Contact")
        }
        Button(onClick = { /* TODO: Navigate to Terms of Use */ }) {
            Text("Conditions d'utilisation")
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                authViewModel.signOut()
                onNavigateToLogin()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter")
        }
    }
}