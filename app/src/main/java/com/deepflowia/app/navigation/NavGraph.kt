package com.deepflowia.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.deepflowia.app.ui.screens.*
import com.deepflowia.app.ui.screens.ai.AIScreen
import com.deepflowia.app.ui.screens.NotificationSettingsScreen
import com.deepflowia.app.ui.screens.ai.AISelectionScreen
import com.deepflowia.app.data.GoogleAuthHandler
import com.deepflowia.app.ui.screens.auth.AuthScreen
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.AuthState
import com.deepflowia.app.viewmodel.ThemeViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel,
    googleAuthHandler: GoogleAuthHandler
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState, navController) {
        when (authState) {
            is AuthState.SignedIn -> {
                if (navController.currentDestination?.route == "loading" ||
                    navController.currentDestination?.route == "auth") {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            is AuthState.SignedOut -> {
                if (navController.currentDestination?.route != "auth") {
                    navController.navigate("auth") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
            else -> Unit // Gère Initializing et Loading
        }
    }

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen() }
        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                googleAuthHandler = googleAuthHandler
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
        composable("task_stats") { TaskStatsScreen(navController = navController) }
        composable("habits") { HabitsScreen(navController = navController) }
        composable("habit_detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            HabitDetailScreen(habitId = habitId, navController = navController)
        }
        composable("habit_stats") { HabitStatsScreen(navController = navController) }
        composable("goals") { GoalsScreen(navController = navController) }
        composable("goal_detail/{goalId}") { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")
            GoalDetailScreen(navController = navController, goalId = goalId)
        }
        composable("goal_stats") { GoalStatsScreen(navController = navController) }
        composable("journal") { JournalScreen(navController = navController) }
        composable("journal_detail/{journalId}") { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("journalId")
            JournalDetailScreen(journalId = journalId, navController = navController)
        }
        composable(BottomNavItem.AI.route) { AISelectionScreen(navController = navController) }
        composable("ai_chat") { AIScreen(navController = navController) }
        composable("productivity_analysis") { ProductivityAnalysisScreen() }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }
        composable("help") { HelpScreen(navController = navController) }
        composable("terms") { TermsScreen(navController = navController) }
        composable("focus") { FocusScreen(navController = navController) }
        composable("focus_stats") { FocusStatsScreen(navController = navController) }
        composable("reflection") { ReflectionScreen(navController = navController) }
        composable("reflection_detail/{reflectionIdOrQuestion}") { backStackEntry ->
            val reflectionIdOrQuestion = backStackEntry.arguments?.getString("reflectionIdOrQuestion")
            ReflectionDetailScreen(navController = navController, reflectionIdOrQuestion = reflectionIdOrQuestion)
        }
        composable("reflection_stats") { ReflectionStatsScreen(navController = navController) }
        composable("journal_stats") { JournalStatsScreen(navController = navController) }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
        composable("admin_panel") { AdminScreen(navController = navController) }
        composable("notification_settings") { NotificationSettingsScreen(navController = navController) }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}