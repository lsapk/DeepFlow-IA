package com.deepflowia.app.services

import android.content.Context

class NotificationPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    fun isReminderEnabled(type: String): Boolean {
        return sharedPreferences.getBoolean("${type}_reminder_enabled", false)
    }

    fun setReminderEnabled(type: String, isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("${type}_reminder_enabled", isEnabled).apply()
    }

    fun getReminderTime(type: String): Pair<Int, Int> {
        val time = sharedPreferences.getString("${type}_reminder_time", "09:00") ?: "09:00"
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return Pair(hour, minute)
    }

    fun setReminderTime(type: String, hour: Int, minute: Int) {
        val time = String.format("%02d:%02d", hour, minute)
        sharedPreferences.edit().putString("${type}_reminder_time", time).apply()
    }
}
