package com.deepflowia.app.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.deepflowia.app.services.NotificationPreferences
import com.deepflowia.app.services.ReminderScheduler
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val notificationPreferences = remember { NotificationPreferences(context) }
    val reminderScheduler = remember { ReminderScheduler(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres de notification", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Gérez ici les rappels que vous souhaitez recevoir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ReminderSection(
                title = "Rappels Journaliers",
                description = "Recevez un rappel chaque jour pour ne pas oublier vos habitudes, votre journal et vos réflexions.",
                preferences = notificationPreferences,
                scheduler = reminderScheduler
            )
        }
    }
}

@Composable
fun ReminderSection(
    title: String,
    description: String,
    preferences: NotificationPreferences,
    scheduler: ReminderScheduler
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            NotificationToggle(
                label = "Rappel des habitudes",
                type = "habit",
                preferences = preferences,
                scheduler = scheduler
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NotificationToggle(
                label = "Rappel du journal",
                type = "journal",
                preferences = preferences,
                scheduler = scheduler
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NotificationToggle(
                label = "Rappel des réflexions",
                type = "reflection",
                preferences = preferences,
                scheduler = scheduler
            )
        }
    }
}

@Composable
fun NotificationToggle(
    label: String,
    type: String,
    preferences: NotificationPreferences,
    scheduler: ReminderScheduler
) {
    val context = LocalContext.current
    var isChecked by remember { mutableStateOf(preferences.isReminderEnabled(type)) }
    var reminderTime by remember { mutableStateOf(preferences.getReminderTime(type)) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            reminderTime = Pair(hourOfDay, minute)
            preferences.setReminderTime(type, hourOfDay, minute)
            if (isChecked) {
                scheduler.scheduleDailyReminder(type, hourOfDay, minute)
            }
        },
        reminderTime.first,
        reminderTime.second,
        true
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Switch(
                checked = isChecked,
                onCheckedChange = { enabled ->
                    isChecked = enabled
                    preferences.setReminderEnabled(type, enabled)
                    if (enabled) {
                        scheduler.scheduleDailyReminder(type, reminderTime.first, reminderTime.second)
                    } else {
                        scheduler.cancelDailyReminder(type)
                    }
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable(
                    enabled = isChecked,
                    onClick = { timePickerDialog.show() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Heure du rappel : ${String.format("%02d:%02d", reminderTime.first, reminderTime.second)}",
                color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
