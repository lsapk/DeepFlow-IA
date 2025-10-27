package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conditions d'utilisation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Conditions d'utilisation", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "En utilisant DeepFlow-IA, vous acceptez d'être lié par les présentes conditions d'utilisation. Si vous n'êtes pas d'accord avec une partie de ces conditions, vous ne devez pas utiliser l'application.\n\n" +
                "1. Utilisation de l'application\n" +
                "Vous êtes autorisé à utiliser l'application à des fins personnelles et non commerciales. Vous ne devez pas utiliser l'application à des fins illégales ou non autorisées.\n\n" +
                "2. Compte utilisateur\n" +
                "Vous êtes responsable de la confidentialité de votre compte et de votre mot de passe. Vous acceptez d'informer immédiatement DeepFlow-IA de toute utilisation non autorisée de votre compte.\n\n" +
                "3. Contenu\n" +
                "Vous êtes seul responsable du contenu que vous créez, téléchargez ou partagez via l'application. Vous ne devez pas télécharger de contenu illégal, offensant ou qui viole les droits d'autrui.\n\n" +
                "4. Propriété intellectuelle\n" +
                "L'application et son contenu original, ses caractéristiques et ses fonctionnalités sont la propriété de DeepFlow-IA et sont protégés par les lois internationales sur le droit d'auteur, les marques de commerce et autres lois sur la propriété intellectuelle.\n\n" +
                "5. Limitation de responsabilité\n" +
                "DeepFlow-IA ne sera pas responsable des dommages directs, indirects, accessoires, spéciaux ou consécutifs résultant de l'utilisation ou de l'impossibilité d'utiliser l'application.\n\n" +
                "6. Modifications des conditions\n" +
                "DeepFlow-IA se réserve le droit de modifier ces conditions à tout moment. Nous vous informerons de tout changement en publiant les nouvelles conditions sur cette page."
            )
        }
    }
}
