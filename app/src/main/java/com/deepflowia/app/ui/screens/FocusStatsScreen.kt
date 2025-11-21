package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deepflowia.app.models.FocusSession
import com.deepflowia.app.ui.components.StatChip
import com.deepflowia.app.viewmodel.FocusViewModel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusStatsScreen(
    navController: NavController,
    focusViewModel: FocusViewModel = viewModel()
) {
    val sessions by focusViewModel.focusSessions.collectAsState()
    val isLoading by focusViewModel.isLoading.collectAsState()
    val errorMessage by focusViewModel.errorMessage.collectAsState()
    val todayMinutes by focusViewModel.statsTodayMinutes.collectAsState()
    val weekMinutes by focusViewModel.statsWeekMinutes.collectAsState()
    val monthMinutes by focusViewModel.statsMonthMinutes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques de Focus") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            StatsSummary(todayMinutes, weekMinutes, monthMinutes)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Historique des sessions", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sessions) { session ->
                    FocusSessionItem(session)
                }
            }
        }
    }
}

@Composable
fun StatsSummary(today: Long, week: Long, month: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Temps de concentration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatChip("Aujourd'hui", "$today min")
                StatChip("Cette semaine", "$week min")
                StatChip("Ce mois", "$month min")
            }
        }
    }
}

@Composable
fun FocusSessionItem(session: FocusSession) {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
    val startDate = session.startedAt?.let {
        OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    }

    ListItem(
        headlineContent = {
            Text(session.title ?: "Session sans titre")
        },
        supportingContent = {
            Text(startDate?.format(formatter) ?: "Date inconnue")
        },
        trailingContent = {
            Text("${session.duration} min", color = if (session.completedAt != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    )
}
