package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class PrayerAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "PrayerAlarmReceiver"
        const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
        const val ACTION_DISMISS = "com.example.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"

        fun snoozeAlarm(context: Context, prayerName: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val snoozeTimeMs = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
                putExtra("prayer_name", prayerName)
            }

            // Request code 9999 for snooze to distinguish from normal prayer scheduler
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            PrayerAlarmScheduler.scheduleAlarmCompat(alarmManager, snoozeTimeMs, pendingIntent)
            Log.d(TAG, "Scheduled snooze for $prayerName in 5 minutes.")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        Log.d(TAG, "PrayerAlarmReceiver received action: $action for $prayerName")

        when (action) {
            ACTION_PRAYER_ALARM -> {
                Log.d(TAG, "Triggering alarm for $prayerName")

                val serviceIntent = Intent(context, PrayerAlarmService::class.java).apply {
                    putExtra("prayer_name", prayerName)
                }

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start PrayerAlarmService from background", e)
                }
            }
            ACTION_DISMISS -> {
                Log.d(TAG, "Dismiss action clicked")
                val serviceIntent = Intent(context, PrayerAlarmService::class.java).apply {
                    this.action = PrayerAlarmService.ACTION_DISMISS
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    context.stopService(serviceIntent)
                }
            }
            ACTION_SNOOZE -> {
                Log.d(TAG, "Snooze action clicked for $prayerName")
                val serviceIntent = Intent(context, PrayerAlarmService::class.java).apply {
                    this.action = PrayerAlarmService.ACTION_SNOOZE
                    putExtra("prayer_name", prayerName)
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    context.stopService(serviceIntent)
                    snoozeAlarm(context, prayerName)
                }
            }
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Device rebooted. Rescheduling all active alarms.")
                PrayerAlarmScheduler.scheduleAlarms(context)
            }
        }
    }
}

