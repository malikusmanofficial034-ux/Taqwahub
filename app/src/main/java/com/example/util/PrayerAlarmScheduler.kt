package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.widget.WidgetHelper
import java.text.SimpleDateFormat
import java.util.*

object PrayerAlarmScheduler {
    private const val TAG = "PrayerAlarmScheduler"
    
    fun scheduleAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // 1. Check if alarm system is globally enabled
        val sharedPrefs = SecurePreferences.getSecurePrefs(context)
        val isGloballyEnabled = sharedPrefs.getBoolean("prayer_alarm_enabled", false)
        
        // Always cancel existing alarms first to avoid duplicate firing or stale schedules
        cancelAllAlarms(context)
        
        if (!isGloballyEnabled) {
            Log.d(TAG, "Prayer alarm system is globally disabled. Cancelled all alarms.")
            return
        }

        // 2. Fetch prayer times from SharedPreferences via WidgetHelper
        val timings = WidgetHelper.getPrayerTimes(context)
        val enabledPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()
        
        for (prayerName in enabledPrayers) {
            val isPrayerEnabled = sharedPrefs.getBoolean("alarm_${prayerName.lowercase()}", false)
            if (!isPrayerEnabled) {
                Log.d(TAG, "$prayerName alarm is disabled by user.")
                continue
            }
            
            val timeStr = timings[prayerName] ?: ""
            // Clean time string - Aladhan returns e.g. "05:43 (EEST)" or just "05:43"
            val cleanTimeStr = timeStr.substringBefore(" ").trim()
            if (cleanTimeStr.isEmpty()) {
                Log.d(TAG, "No time stored for $prayerName.")
                continue
            }
            
            try {
                val parsedDate = sdf.parse(cleanTimeStr) ?: continue
                val parsedCal = Calendar.getInstance().apply { time = parsedDate }
                
                val alarmTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // If the scheduled prayer is in the past for today, schedule it for tomorrow
                if (alarmTime.before(now)) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = "com.example.ACTION_PRAYER_ALARM"
                    putExtra("prayer_name", prayerName)
                }
                
                // Unique request code per prayer to avoid collisions
                val requestCode = getRequestCode(prayerName)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                scheduleAlarmCompat(alarmManager, alarmTime.timeInMillis, pendingIntent)
                Log.d(TAG, "Scheduled alarm for $prayerName at ${alarmTime.time}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule alarm for $prayerName at $timeStr", e)
            }
        }
    }
    
    fun scheduleAlarmCompat(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent
    ) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while scheduling exact alarm: falling back to inexact.", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Critical failure scheduling fallback alarm", ex)
            }
        }
    }
    
    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val enabledPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        
        for (prayerName in enabledPrayers) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "com.example.ACTION_PRAYER_ALARM"
            }
            val requestCode = getRequestCode(prayerName)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for $prayerName")
            }
        }
    }
    
    private fun getRequestCode(prayerName: String): Int {
        return when (prayerName) {
            "Fajr" -> 1001
            "Dhuhr" -> 1002
            "Asr" -> 1003
            "Maghrib" -> 1004
            "Isha" -> 1005
            else -> 1000
        }
    }
}
