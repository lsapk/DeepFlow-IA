package com.deepflowia.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.deepflowia.app.MainActivity
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.FocusSession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class FocusTimerService : Service() {

    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timer: CountDownTimer? = null
    private var currentSession: FocusSession? = null

    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> get() = _timerState

    private val _timeInMillis = MutableStateFlow(0L)
    val timeInMillis: StateFlow<Long> get() = _timeInMillis

    enum class TimerState { RUNNING, PAUSED, STOPPED }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "FocusTimerChannel"
        const val ACTION_START = "com.deepflowia.app.START"
        const val ACTION_PAUSE = "com.deepflowia.app.PAUSE"
        const val ACTION_STOP = "com.deepflowia.app.STOP"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_SESSION_TITLE = "session_title"
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMinutes = intent.getLongExtra(EXTRA_DURATION_MINUTES, 25L)
                val title = intent.getStringExtra(EXTRA_SESSION_TITLE)
                startTimer(durationMinutes, title)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationMinutes: Long, title: String?) {
        val durationMillis = durationMinutes * 60 * 1000
        _timeInMillis.value = durationMillis
        _timerState.value = TimerState.RUNNING

        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
        if (userId != null && currentSession == null) {
            currentSession = FocusSession(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                duration = durationMinutes.toInt(),
                startedAt = getCurrentTimestamp()
            )
        }

        startForeground(NOTIFICATION_ID, createNotification(durationMillis))

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeInMillis.value = millisUntilFinished
                updateNotification(millisUntilFinished)
            }

            override fun onFinish() {
                stopTimer(completed = true)
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        _timerState.value = TimerState.PAUSED
    }

    private fun stopTimer(completed: Boolean = false) {
        timer?.cancel()
        _timerState.value = TimerState.STOPPED
        _timeInMillis.value = 0L

        currentSession?.let {
            val finalSession = if (completed) it.copy(completedAt = getCurrentTimestamp()) else it
            saveSession(finalSession)
        }
        currentSession = null

        stopForeground(true)
        stopSelf()
    }

    private fun saveSession(session: FocusSession) {
        serviceScope.launch {
            try {
                SupabaseManager.client.postgrest.from("focus_sessions").insert(session)
            } catch (e: Exception) {
                Log.e("FocusTimerService", "Erreur lors de la sauvegarde de la session", e)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Focus Timer Channel", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(timeLeft: Long) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Session de Focus")
        .setContentText("Temps restant : ${formatTime(timeLeft)}")
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
        .setOngoing(true)
        .build()

    private fun updateNotification(timeLeft: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(timeLeft))
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
