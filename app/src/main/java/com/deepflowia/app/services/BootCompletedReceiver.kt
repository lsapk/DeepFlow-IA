package com.deepflowia.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.Goal
import com.deepflowia.app.models.Task
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                rescheduleAlarms(context)
            }
        }
    }

    private suspend fun rescheduleAlarms(context: Context) {
        val preferences = NotificationPreferences(context)
        val scheduler = ReminderScheduler(context)
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        // 1. Replanifier les rappels quotidiens
        val reminderTypes = listOf("habit", "journal", "reflection")
        reminderTypes.forEach { type ->
            if (preferences.isReminderEnabled(type)) {
                val (hour, minute) = preferences.getReminderTime(type)
                scheduler.scheduleDailyReminder(type, hour, minute)
            }
        }

        // 2. Replanifier les rappels d'échéances pour les tâches
        try {
            val tasks = SupabaseManager.client.postgrest.from("tasks").select {
                filter {
                    eq("user_id", userId)
                    neq("due_date", "null") // Filtrer les tâches avec une date d'échéance
                }
            }.decodeList<Task>()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            tasks.forEach { task ->
                task.dueDate?.let { dateString ->
                    try {
                        val date = sdf.parse(dateString)
                        if (date != null && date.time > System.currentTimeMillis()) {
                            task.id?.let { scheduler.scheduleDeadlineReminder(it, date.time, task.title) }
                        }
                    } catch (e: Exception) {
                        // Ignorer les dates mal formatées
                    }
                }
            }
        } catch (e: Exception) {
            // Gérer les erreurs de récupération des tâches
        }

        // 3. Replanifier les rappels d'échéances pour les objectifs
        try {
            val goals = SupabaseManager.client.postgrest.from("goals").select {
                filter {
                    eq("user_id", userId)
                    neq("target_date", "null") // Filtrer les objectifs avec une date d'échéance
                }
            }.decodeList<Goal>()

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            goals.forEach { goal ->
                goal.targetDate?.let { dateString ->
                    try {
                        val date = sdf.parse(dateString)
                        if (date != null && date.time > System.currentTimeMillis()) {
                            goal.id?.let { scheduler.scheduleDeadlineReminder(it, date.time, goal.title) }
                        }
                    } catch (e: Exception) {
                        // Ignorer les dates mal formatées
                    }
                }
            }
        } catch (e: Exception) {
            // Gérer les erreurs de récupération des objectifs
        }
    }
}
