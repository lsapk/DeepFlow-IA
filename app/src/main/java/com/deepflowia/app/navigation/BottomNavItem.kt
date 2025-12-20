package com.deepflowia.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val iconOutlined: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, Icons.Outlined.Home, "Accueil")
    object AI : BottomNavItem("ai", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "IA")
    object Profile : BottomNavItem("profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "Profil")
    object Admin : BottomNavItem("admin_panel", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, "Admin")
}
