package com.deepflowia.app.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.deepflowia.app.services.FocusTimerService
import java.util.concurrent.TimeUnit

@Composable
fun FocusScreen(navController: NavController) {
    val context = LocalContext.current
    var focusService by remember { mutableStateOf<FocusTimerService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    val timerState by focusService?.timerState?.collectAsState() ?: remember { mutableStateOf(FocusTimerService.TimerState.STOPPED) }
    val timeInMillis by focusService?.timeInMillis?.collectAsState() ?: remember { mutableStateOf(0L) }

    var sessionTitle by remember { mutableStateOf("") }
    val durationOptions = listOf(15L, 25L, 45L, 60L)
    var selectedDuration by remember { mutableStateOf(25L) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as FocusTimerService.LocalBinder
                focusService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                focusService = null
                isBound = false
            }
        }
    }

    DisposableEffect(Unit) {
        Intent(context, FocusTimerService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mode Focus") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if (timerState == FocusTimerService.TimerState.STOPPED) {
                SetupUI(
                    sessionTitle = sessionTitle,
                    onTitleChange = { sessionTitle = it },
                    durationOptions = durationOptions,
                    selectedDuration = selectedDuration,
                    onDurationSelected = { selectedDuration = it }
                )
            } else {
                // Affiche le titre de la session en cours si elle existe
                Text(
                    text = sessionTitle.ifBlank { "Session de concentration" },
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            TimerDisplay(if (timerState == FocusTimerService.TimerState.STOPPED) selectedDuration * 60 * 1000 else timeInMillis)

            TimerControls(
                timerState = timerState,
                onStart = {
                    val intent = Intent(context, FocusTimerService::class.java).apply {
                        action = FocusTimerService.ACTION_START
                        putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, selectedDuration)
                        putExtra(FocusTimerService.EXTRA_SESSION_TITLE, sessionTitle.ifBlank { null })
                    }
                    context.startService(intent)
                },
                onPause = { context.startService(Intent(context, FocusTimerService::class.java).apply { action = FocusTimerService.ACTION_PAUSE }) },
                onStop = { context.startService(Intent(context, FocusTimerService::class.java).apply { action = FocusTimerService.ACTION_STOP }) }
            )
        }
    }
}

@Composable
private fun SetupUI(
    sessionTitle: String,
    onTitleChange: (String) -> Unit,
    durationOptions: List<Long>,
    selectedDuration: Long,
    onDurationSelected: (Long) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = sessionTitle,
            onValueChange = onTitleChange,
            label = { Text("Titre de la session (optionnel)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Choisissez une durée :", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            durationOptions.forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    label = { Text("$duration min") }
                )
            }
        }
    }
}

@Composable
private fun TimerDisplay(timeInMillis: Long) {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 90.sp),
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

@Composable
private fun TimerControls(
    timerState: FocusTimerService.TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (timerState) {
            FocusTimerService.TimerState.STOPPED -> {
                Button(onClick = onStart, modifier = Modifier.height(48.dp)) {
                    Text("Démarrer", fontSize = 16.sp)
                }
            }
            FocusTimerService.TimerState.RUNNING -> {
                Button(onClick = onPause, modifier = Modifier.height(48.dp)) {
                    Text("Pause", fontSize = 16.sp)
                }
                OutlinedButton(onClick = onStop, modifier = Modifier.height(48.dp)) {
                    Text("Arrêter")
                }
            }
            FocusTimerService.TimerState.PAUSED -> {
                Button(onClick = onStart, modifier = Modifier.height(48.dp)) {
                    Text("Reprendre", fontSize = 16.sp)
                }
                OutlinedButton(onClick = onStop, modifier = Modifier.height(48.dp)) {
                    Text("Arrêter")
                }
            }
        }
    }
}
