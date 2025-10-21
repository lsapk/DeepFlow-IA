package com.deepflowia.app.navigation

import com.deepflowia.app.R

sealed class BottomNavItem(val route: String, val icon: Int, val title: String) {
    object Home : BottomNavItem("home", R.drawable.ic_menu_home, "Accueil")
    object AI : BottomNavItem("ai", R.drawable.ic_menu_info_details, "IA")
    object Profile : BottomNavItem("profile", R.drawable.ic_menu_myplaces, "Profil")
}