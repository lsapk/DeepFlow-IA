package com.deepflowia.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Accueil")
    object AI : BottomNavItem("ai", Icons.Filled.SmartToy, "IA")
    object Profile : BottomNavItem("profile", Icons.Filled.AccountCircle, "Profil")
}