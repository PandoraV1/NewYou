package com.AidenLiriano.newyou.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.AidenLiriano.newyou.R

class WorkoutTrackingService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP  = "ACTION_STOP"
        const val CHANNEL_ID   = "workout_tracking_channel"

        const val EXTRA_TRACK_STEPS     = "track_steps"
        const val EXTRA_TRACK_ELEVATION = "track_elevation"
        const val EXTRA_TRACK_GPS       = "track_gps"

        var sensorHelper: SensorManagerHelper? = null
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                createNotificationChannel()
                startForeground(1, buildNotification())
                acquireWakeLock()

                val trackSteps     = intent.getBooleanExtra(EXTRA_TRACK_STEPS, false)
                val trackElevation = intent.getBooleanExtra(EXTRA_TRACK_ELEVATION, false)
                val trackGps       = intent.getBooleanExtra(EXTRA_TRACK_GPS, false)

                sensorHelper?.startTracking(
                    trackHeartRate  = true,
                    trackSteps      = trackSteps,
                    trackElevation  = trackElevation,
                    trackGps        = trackGps
                )
            }
            ACTION_STOP -> {
                sensorHelper?.stopTracking()
                releaseWakeLock()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorHelper?.stopTracking()
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NewYou::WorkoutWakeLock"
        )
        wakeLock?.acquire(3 * 60 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        wakeLock = null
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Workout in Progress")
            .setContentText("New You is tracking your workout")
            .setSmallIcon(R.drawable.ic_running)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
            ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps workout tracking active while the screen is off"
        }
        manager.createNotificationChannel(channel)
    }
}