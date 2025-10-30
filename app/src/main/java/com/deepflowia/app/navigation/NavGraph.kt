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
import com.deepflowia.app.ui.screens.*
import com.deepflowia.app.ui.screens.auth.LoginScreen
import com.deepflowia.app.ui.screens.auth.SignupScreen
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState
import com.deepflowia.app.viewmodel.ThemeViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.SignedIn -> BottomNavItem.Home.route
        is AuthState.SignedOut -> "login"
        else -> "loading"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("loading") { LoadingScreen() }
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
                onNavigateToJournal = { navController.navigate("journal") },
                onNavigateToProfile = { navController.navigate(BottomNavItem.Profile.route) },
                onNavigateToFocus = { navController.navigate("focus") },
                onNavigateToReflection = { navController.navigate("reflection") }
            )
        }
        composable("tasks") { TasksScreen(navController = navController) }
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(taskId = taskId, navController = navController)
        }
        composable("habits") { HabitsScreen(navController = navController) }
        composable("habit_detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitDetailScreen(habitId = habitId, navController = navController)
        }
        composable("goals") { GoalsScreen(navController = navController) }
        composable("goal_detail/{goalId}") { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")
            GoalDetailScreen(navController = navController, goalId = goalId)
        }
        composable("journal") { JournalScreen(navController = navController) }
        composable("journal_detail/{journalId}") { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("journalId")
            JournalDetailScreen(journalId = journalId, navController = navController)
        }
        composable(BottomNavItem.AI.route) { AIScreen() }
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
        composable("help") { HelpScreen(navController = navController) }
        composable("terms") { TermsScreen(navController = navController) }
        composable("focus") { FocusScreen(navController = navController) }
        composable("reflection") { ReflectionScreen(navController = navController) }
        composable("reflection_detail/{reflectionIdOrQuestion}") { backStackEntry ->
            val reflectionIdOrQuestion = backStackEntry.arguments?.getString("reflectionIdOrQuestion")
            ReflectionDetailScreen(navController = navController, reflectionIdOrQuestion = reflectionIdOrQuestion)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
