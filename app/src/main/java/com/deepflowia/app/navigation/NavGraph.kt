package com.deepflowia.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.deepflowia.app.ui.screens.LoadingScreen
import com.deepflowia.app.ui.screens.AIScreen
import com.deepflowia.app.ui.screens.GoalsScreen
import com.deepflowia.app.ui.screens.HabitsScreen
import com.deepflowia.app.ui.screens.HomeScreen
import com.deepflowia.app.ui.screens.JournalScreen
import com.deepflowia.app.ui.screens.ProfileScreen
import com.deepflowia.app.ui.screens.TaskDetailScreen
import com.deepflowia.app.ui.screens.TasksScreen
import com.deepflowia.app.ui.screens.auth.LoginScreen
import com.deepflowia.app.ui.screens.auth.SignupScreen
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState
import com.deepflowia.app.viewmodel.TaskViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") {
            LoadingScreen()
        }
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
        composable("tasks") {
            TasksScreen(navController = navController)
        }
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val taskViewModel: TaskViewModel = viewModel()
            val task = taskViewModel.tasks.collectAsState().value.find { it.id == taskId }
            if (task != null) {
                TaskDetailScreen(task = task)
            }
        }
        composable("habits") {
            HabitsScreen()
        }
        composable("goals") {
            GoalsScreen()
        }
        composable("journal") {
            JournalScreen()
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

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignedIn -> {
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo("loading") { inclusive = true }
                    popUpTo("login") { inclusive = true }
                    popUpTo("signup") { inclusive = true }
                }
            }
            is AuthState.SignedOut -> {
                navController.navigate("login") {
                    popUpTo("loading") { inclusive = true }
                    popUpTo(BottomNavItem.Home.route) { inclusive = true }
                }
            }
            else -> Unit
        }
    }
}
