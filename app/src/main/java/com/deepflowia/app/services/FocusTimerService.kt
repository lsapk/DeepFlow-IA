package com.deepflowia.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.deepflowia.app.MainActivity
import com.deepflowia.app.R
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
    private var timeLeftInMillis = 0L

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
        const val ACTION_RESUME = "com.deepflowia.app.RESUME"
        const val ACTION_STOP = "com.deepflowia.app.STOP"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_SESSION_TITLE = "session_title"
        const val EXTRA_DISTRACTION_FREE = "distraction_free"
    }

    private var initialRingerMode: Int? = null

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
                val distractionFree = intent.getBooleanExtra(EXTRA_DISTRACTION_FREE, false)
                startTimer(durationMinutes * 60 * 1000, title, distractionFree, isNewSession = true)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(durationMillis: Long, title: String? = null, distractionFree: Boolean, isNewSession: Boolean) {
        timeLeftInMillis = durationMillis
        _timeInMillis.value = timeLeftInMillis
        _timerState.value = TimerState.RUNNING

        if (isNewSession) {
            if (distractionFree) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    initialRingerMode = audioManager.ringerMode
                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } else {
                    // Demander la permission à l'utilisateur
                    val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                currentSession = FocusSession(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    duration = (durationMillis / (60 * 1000)).toInt(),
                    startedAt = getCurrentTimestamp()
                )
            }
        }

        startForeground(NOTIFICATION_ID, createNotification().build())

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                _timeInMillis.value = timeLeftInMillis
                updateNotification()
            }

            override fun onFinish() {
                // Play sound and vibrate
                try {
                    val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(applicationContext, notificationSound)
                    r.play()
                } catch (e: Exception) {
                    Log.e("FocusTimerService", "Erreur lors de la lecture du son", e)
                }

                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(500)
                    }
                } catch (e: Exception) {
                    Log.e("FocusTimerService", "Erreur lors de la vibration", e)
                }


                stopTimer(completed = true)
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        _timerState.value = TimerState.PAUSED
        updateNotification()
    }

    private fun resumeTimer() {
        if (_timerState.value == TimerState.PAUSED) {
            // When resuming, distraction free mode is not re-applied.
            startTimer(timeLeftInMillis, currentSession?.title, distractionFree = false, isNewSession = false)
        }
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

        initialRingerMode?.let {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.ringerMode = it
            initialRingerMode = null
        }

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
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Focus Timer Channel", NotificationManager.IMPORTANCE_HIGH)
            serviceChannel.description = "Canal pour les notifications du minuteur de focus"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Session de Focus")
            .setContentText("Temps restant : ${formatTime(timeLeftInMillis)}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        when (_timerState.value) {
            TimerState.RUNNING -> {
                val pauseIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_PAUSE }
                val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(R.drawable.ic_launcher_foreground, "Pause", pausePendingIntent)
            }
            TimerState.PAUSED -> {
                val resumeIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_RESUME }
                val resumePendingIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
                builder.addAction(R.drawable.ic_launcher_foreground, "Reprendre", resumePendingIntent)
            }
            else -> {}
        }

        val stopIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.addAction(R.drawable.ic_launcher_foreground, "Arrêter", stopPendingIntent)

        return builder
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification().build())
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
