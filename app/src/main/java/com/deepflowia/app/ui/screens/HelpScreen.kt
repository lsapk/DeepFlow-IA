package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Column
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
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aide/FAQ", fontWeight = FontWeight.Bold) },
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
            Text("Foire aux questions", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            HelpItem("Comment puis-je réinitialiser mon mot de passe ?", "Pour réinitialiser votre mot de passe, allez dans les paramètres du compte et sélectionnez 'Changer le mot de passe'. Suivez ensuite les instructions à l'écran.")
            HelpItem("Comment fonctionne le suivi des habitudes ?", "Dans la section 'Habitudes', vous pouvez ajouter de nouvelles habitudes à suivre. Chaque jour, vous pouvez marquer une habitude comme 'complétée' pour suivre votre progression.")
            HelpItem("Où puis-je voir mes objectifs ?", "La section 'Objectifs' vous permet de définir des objectifs à long terme et de les diviser en sous-objectifs plus petits. Vous pouvez y suivre votre progression et marquer les objectifs comme atteints.")
            HelpItem("À quoi sert le journal ?", "Le journal est un espace personnel où vous pouvez noter vos pensées, vos réflexions et vos idées. C'est un excellent moyen de suivre votre état d'esprit et votre progression personnelle.")
        }
    }
}

@Composable
fun HelpItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(question, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(answer, fontSize = 16.sp)
    }
}
