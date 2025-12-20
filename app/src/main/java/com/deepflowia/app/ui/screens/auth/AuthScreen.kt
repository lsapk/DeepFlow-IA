package com.deepflowia.app.ui.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.R
import com.deepflowia.app.data.GoogleAuthHandler
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState

private enum class AuthMode {
    SignIn, SignUp
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit,
    googleAuthHandler: GoogleAuthHandler
) {
    var authMode by remember { mutableStateOf(AuthMode.SignIn) }
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.auth_welcome_title),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (authMode == AuthMode.SignIn) stringResource(id = R.string.auth_welcome_subtitle_signin) else stringResource(id = R.string.auth_welcome_subtitle_signup),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            AuthCard(
                authMode = authMode,
                onAuthModeChange = { authMode = it },
                authViewModel = authViewModel,
                authState = authState,
                googleAuthHandler = googleAuthHandler
            )

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthCard(
    authMode: AuthMode,
    onAuthModeChange: (AuthMode) -> Unit,
    authViewModel: AuthViewModel,
    authState: AuthState,
    googleAuthHandler: GoogleAuthHandler
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            TabRow(
                selectedTabIndex = authMode.ordinal,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = authMode == AuthMode.SignIn,
                    onClick = { onAuthModeChange(AuthMode.SignIn) },
                    text = { Text(stringResource(id = R.string.auth_tab_signin)) }
                )
                Tab(
                    selected = authMode == AuthMode.SignUp,
                    onClick = { onAuthModeChange(AuthMode.SignUp) },
                    text = { Text(stringResource(id = R.string.auth_tab_signup)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.auth_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.auth_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (authMode == AuthMode.SignIn) {
                        authViewModel.signIn(email, password)
                    } else {
                        authViewModel.signUp(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = authState != AuthState.Loading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (authMode == AuthMode.SignIn) stringResource(id = R.string.auth_button_signin) else stringResource(id = R.string.auth_button_signup), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { googleAuthHandler.signIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = "Google logo", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(id = R.string.auth_button_google), fontWeight = FontWeight.Bold)
            }
        }
    }
}
