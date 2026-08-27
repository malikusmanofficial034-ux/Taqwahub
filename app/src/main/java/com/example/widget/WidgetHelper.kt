package com.example.widget

import android.content.Context
import com.example.data.api.AladhanTimings

object WidgetHelper {
    private const val PREFS_NAME = "taqwahub_widget_prefs"

<<<<<<< HEAD
    fun savePrayerTimes(context: Context, timings: AladhanTimings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
=======
    fun savePrayerTimes(context: Context, timings: AladhanTimings, timezone: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
>>>>>>> 6e834ed (Update Taqwahub)
            .putString("fajr", timings.Fajr)
            .putString("sunrise", timings.Sunrise)
            .putString("dhuhr", timings.Dhuhr)
            .putString("asr", timings.Asr)
            .putString("maghrib", timings.Maghrib)
            .putString("isha", timings.Isha)
<<<<<<< HEAD
            .apply()
=======
        if (timezone != null) {
            editor.putString("timezone", timezone)
        }
        editor.apply()
    }

    fun getPrayerTimezone(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("timezone", "") ?: ""
>>>>>>> 6e834ed (Update Taqwahub)
    }

    fun getPrayerTimes(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "Fajr" to (prefs.getString("fajr", "") ?: ""),
            "Sunrise" to (prefs.getString("sunrise", "") ?: ""),
            "Dhuhr" to (prefs.getString("dhuhr", "") ?: ""),
            "Asr" to (prefs.getString("asr", "") ?: ""),
            "Maghrib" to (prefs.getString("maghrib", "") ?: ""),
            "Isha" to (prefs.getString("isha", "") ?: "")
        )
    }
<<<<<<< HEAD
=======

    fun getPrayerTimesTimings(context: Context): AladhanTimings? {
        val map = getPrayerTimes(context)
        val f = map["Fajr"] ?: ""
        if (f.isEmpty()) return null
        return AladhanTimings(
            Fajr = f,
            Sunrise = map["Sunrise"] ?: "",
            Dhuhr = map["Dhuhr"] ?: "",
            Asr = map["Asr"] ?: "",
            Maghrib = map["Maghrib"] ?: "",
            Isha = map["Isha"] ?: ""
        )
    }
>>>>>>> 6e834ed (Update Taqwahub)
}
