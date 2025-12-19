package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.deepflowia.app.navigation.BottomNavItem
import com.deepflowia.app.navigation.NavGraph
import com.deepflowia.app.ui.components.glassmorphism
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent // La couleur sera gérée par le glassmorphism
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .glassmorphism(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                    containerColor = Color.Transparent
                ) {
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
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(navController = navController, themeViewModel = themeViewModel)
        }
    }
}