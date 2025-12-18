package com.deepflowia.app.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleDailyReminder(type: String, hour: Int, minute: Int) {
        val (notificationId, title, message) = when (type) {
            "habit" -> Triple(1, "Rappel des habitudes", "N'oubliez pas de compléter vos habitudes aujourd'hui !")
            "journal" -> Triple(2, "Rappel du journal", "Prenez un moment pour écrire dans votre journal.")
            "reflection" -> Triple(3, "Rappel des réflexions", "C'est l'heure de votre réflexion quotidienne.")
            else -> return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder(type: String) {
        val notificationId = when (type) {
            "habit" -> 1
            "journal" -> 2
            "reflection" -> 3
            else -> return
        }

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun scheduleDeadlineReminder(itemId: String, dueDate: Long, title: String) {
        val notificationId = itemId.hashCode() // ID unique basé sur l'ID de l'item

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra("title", "Échéance proche")
            putExtra("message", "Votre tâche '$title' arrive à échéance bientôt.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Planifier la notification 1 heure avant l'échéance
        val triggerAtMillis = dueDate - AlarmManager.INTERVAL_HOUR

        if (triggerAtMillis > System.currentTimeMillis()) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelDeadlineReminder(itemId: String) {
        val notificationId = itemId.hashCode()
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
