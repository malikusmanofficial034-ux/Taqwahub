package com.example.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class PrayerAlarmService : Service() {
    companion object {
        private const val TAG = "PrayerAlarmService"
        private const val CHANNEL_ID = "taqwa_prayer_alarm_channel"
        private const val NOTIFICATION_ID = 4001

        const val ACTION_DISMISS = "com.example.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    @Volatile private var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val prayerName = intent?.getStringExtra("prayer_name") ?: "Prayer"
        Log.d(TAG, "PrayerAlarmService onStartCommand action: $action, prayer: $prayerName")

        if (action == ACTION_DISMISS) {
            handleStopAlarm()
            return START_NOT_STICKY
        } else if (action == ACTION_SNOOZE) {
            handleStopAlarm()
            PrayerAlarmReceiver.snoozeAlarm(this, prayerName)
            return START_NOT_STICKY
        }

        handleStartAlarm(prayerName)
        return START_NOT_STICKY
    }

    private fun handleStartAlarm(prayerName: String) {
        stopAdhan()
        stopVibration()

        // 1. Build foreground notification FIRST so service is immediately foregrounded
        val notification = buildAlarmNotification(prayerName)
        startForeground(NOTIFICATION_ID, notification)

        // 2. Trigger vibration
        startContinuousVibration()

        // 3. Play Adhan
        playAdhan()
    }

    private fun handleStopAlarm() {
        stopAdhan()
        stopVibration()

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground", e)
        }

        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling notification", e)
        }

        stopSelf()
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying service, stopping sound and vibration")
        stopAdhan()
        stopVibration()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun playAdhan() {
        if (isPlaying) return
        isPlaying = true

        try {
            val mediaContext = applicationContext
            val prefs = SecurePreferences.getSecurePrefs(this)
            val customUriStr = prefs.getString("custom_adhan_uri", null)
            val customUri = customUriStr?.let { Uri.parse(it) }

            var player: MediaPlayer? = null

            if (customUri != null) {
                try {
                    player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                        )
                        setDataSource(mediaContext, customUri)
                        setOnPreparedListener {
                            if (isPlaying) {
                                it.start()
                                Log.d(TAG, "Custom Adhan prepared and started playing.")
                            } else {
                                it.stop()
                                it.release()
                            }
                        }
                        setOnCompletionListener { handleStopAlarm() }
                        setOnErrorListener { _, what, extra ->
                            Log.e(TAG, "Custom Adhan failed (what=$what, extra=$extra).")
                            true
                        }
                        prepareAsync()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play custom URI, falling back to default adhan", e)
                    player?.release()
                    player = null
                }
            }

            if (player == null) {
                player = MediaPlayer.create(mediaContext, com.example.R.raw.adhan)
                if (player != null) {
                    player.setOnCompletionListener { handleStopAlarm() }
                    player.setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "Default Adhan failed (what=$what, extra=$extra). Falling back to system alarm ringtone.")
                        playFallbackSystemAlarm(mediaContext)
                        true
                    }
                    if (isPlaying) {
                        player.start()
                        Log.d(TAG, "Default Adhan started playing via MediaPlayer.create()")
                    } else {
                        player.release()
                        player = null
                    }
                } else {
                    Log.e(TAG, "MediaPlayer.create returned null. Falling back to system alarm ringtone.")
                    playFallbackSystemAlarm(mediaContext)
                }
            }

            mediaPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating Adhan stream, falling back directly.")
            val mediaContext = applicationContext
            playFallbackSystemAlarm(mediaContext)
        }
    }

    private fun playFallbackSystemAlarm(mediaContext: Context) {
        if (!isPlaying) return
        try {
            stopAdhan()
            isPlaying = true

            val alarmUri: Uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(mediaContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                if (isPlaying) {
                    start()
                    Log.d(TAG, "Fallback system alarm is sounding.")
                } else {
                    reset()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing fallback system alarm", e)
        }
    }

    private fun stopAdhan() {
        isPlaying = false
        val mp = mediaPlayer
        mediaPlayer = null
        if (mp != null) {
            try {
                mp.setVolume(0f, 0f)
                if (mp.isPlaying) {
                    mp.pause()
                }
                mp.reset()
                mp.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing media player", e)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun startContinuousVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let { v ->
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 1000, 500)
                    val indexRepeat = 1
                    v.vibrate(VibrationEffect.createWaveform(pattern, indexRepeat))
                } else {
                    val pattern = longArrayOf(0, 1000, 500)
                    v.vibrate(pattern, 1)
                }
                Log.d(TAG, "Continuous vibration pattern started.")
            }
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration", e)
        } finally {
            vibrator = null
        }
    }

    private fun buildAlarmNotification(prayerName: String): Notification {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, PrayerAlarmService::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                2001,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                2001,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val snoozeIntent = Intent(this, PrayerAlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("prayer_name", prayerName)
        }
        val snoozePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this,
                2002,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this,
                2002,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Adhan: $prayerName Time")
            .setContentText("Hurry to Prayer! Offer $prayerName Namaz now.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.mipmap.ic_launcher, "SNOOZE (5 mins)", snoozePendingIntent)
            .addAction(R.mipmap.ic_launcher, "DISMISS", dismissPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming Adhan Alarm trigger screen notifications"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

