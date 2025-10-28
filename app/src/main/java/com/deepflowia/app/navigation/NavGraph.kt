package com.deepflowia.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.deepflowia.app.ui.screens.AIScreen
import com.deepflowia.app.ui.screens.GoalDetailScreen
import com.deepflowia.app.ui.screens.GoalsScreen
import com.deepflowia.app.ui.screens.HabitsScreen
import com.deepflowia.app.ui.screens.HelpScreen
import com.deepflowia.app.ui.screens.HomeScreen
import com.deepflowia.app.ui.screens.JournalScreen
import com.deepflowia.app.ui.screens.ProfileScreen
import com.deepflowia.app.ui.screens.TaskDetailScreen
import com.deepflowia.app.ui.screens.TermsScreen
import com.deepflowia.app.ui.screens.TasksScreen
import com.deepflowia.app.ui.screens.auth.LoginScreen
import com.deepflowia.app.ui.screens.auth.SignupScreen
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState
import com.deepflowia.app.viewmodel.TaskViewModel
import com.deepflowia.app.viewmodel.ThemeViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val authState = authViewModel.authState.collectAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.SignedIn -> {
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthState.SignedOut -> {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen() }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { /* La navigation est gérée par LaunchedEffect */ },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignupScreen(
                onSignupSuccess = { /* La navigation est gérée par LaunchedEffect */ },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onNavigateToTasks = { navController.navigate("tasks") },
                onNavigateToHabits = { navController.navigate("habits") },
                onNavigateToGoals = { navController.navigate("goals") },
                onNavigateToJournal = { navController.navigate("journal") },
                onNavigateToProfile = { navController.navigate(BottomNavItem.Profile.route) }
            )
        }
        composable("tasks") {
            TasksScreen(navController = navController)
        }
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(taskId = taskId, navController = navController)
        }
        composable("habits") {
            HabitsScreen(navController = navController)
        }
        composable("goals") {
            GoalsScreen(navController = navController)
        }
        composable("goal_detail/{goalId}") { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")
            GoalDetailScreen(navController = navController, goalId = goalId)
        }
        composable("journal") {
            JournalScreen(navController = navController)
        }
        composable(BottomNavItem.AI.route) {
            AIScreen()
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                navController = navController,
                themeViewModel = themeViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable("help") {
            HelpScreen(navController = navController)
        }
        composable("terms") {
            TermsScreen(navController = navController)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
