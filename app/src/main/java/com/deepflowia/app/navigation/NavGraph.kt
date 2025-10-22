package com.deepflowia.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.deepflowia.app.ui.screens.AIScreen
import com.deepflowia.app.ui.screens.GoalsScreen
import com.deepflowia.app.ui.screens.HabitsScreen
import com.deepflowia.app.ui.screens.HomeScreen
import com.deepflowia.app.ui.screens.JournalScreen
import com.deepflowia.app.ui.screens.ProfileScreen
import com.deepflowia.app.ui.screens.TasksScreen
import com.deepflowia.app.ui.screens.auth.LoginScreen
import com.deepflowia.app.ui.screens.auth.SignupScreen
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState = authViewModel.authState.collectAsState()

    val startDestination = when (authState.value) {
        is AuthState.SignedIn -> BottomNavItem.Home.route
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onNavigateToTasks = { navController.navigate("tasks") },
                onNavigateToHabits = { navController.navigate("habits") },
                onNavigateToGoals = { navController.navigate("goals") },
                onNavigateToJournal = { navController.navigate("journal") }
            )
        }
        composable("goals") {
            com.deepflowia.app.ui.screens.GoalsScreen()
        }
        composable("journal") {
            com.deepflowia.app.ui.screens.JournalScreen()
        }
        composable("habits") {
            com.deepflowia.app.ui.screens.HabitsScreen()
        }
        composable("tasks") {
            com.deepflowia.app.ui.screens.TasksScreen()
        }
        composable(BottomNavItem.AI.route) {
            AIScreen()
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}