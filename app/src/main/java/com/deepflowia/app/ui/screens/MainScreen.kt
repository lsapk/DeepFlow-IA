package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.deepflowia.app.navigation.BottomNavItem
import com.deepflowia.app.navigation.NavGraph
import com.deepflowia.app.viewmodel.AuthViewModel
import com.deepflowia.app.viewmodel.ThemeViewModel

@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val userRole by authViewModel.userRole.collectAsState()

    val items = mutableListOf(
        BottomNavItem.Home,
        BottomNavItem.AI,
        BottomNavItem.Profile
    )
    if (userRole == "admin") {
        items.add(BottomNavItem.Admin)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(navController = navController, themeViewModel = themeViewModel)
        }
    }
}