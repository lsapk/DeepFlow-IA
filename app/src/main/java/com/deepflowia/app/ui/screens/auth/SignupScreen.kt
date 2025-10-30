package com.deepflowia.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    var passwordError by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) {
            onSignupSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                password.value = it
                passwordError = false
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword.value,
            onValueChange = {
                confirmPassword.value = it
                passwordError = false
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError
        )
        if (passwordError) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (password.value == confirmPassword.value) {
                    authViewModel.signUp(email.value, password.value)
                } else {
                    passwordError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState != AuthState.Loading
        ) {
            if (authState == AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Sign in")
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}