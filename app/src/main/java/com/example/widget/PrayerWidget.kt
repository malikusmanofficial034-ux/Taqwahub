package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import androidx.glance.appwidget.appWidgetBackground

class PrayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val timings = WidgetHelper.getPrayerTimes(context)
            val schedule = calculatePrayerSchedule(timings)

            // Responsive sizing metrics - upgraded for increased sizes & negative space
            val size = LocalSize.current
            val isSmall = size.width < 140.dp || size.height < 140.dp
            
            val titleSize = if (isSmall) 12.sp else 16.sp
            val ongoingTextSize = if (isSmall) 13.sp else 16.sp
            val upcomingTextSize = if (isSmall) 11.sp else 14.sp
            val timerTextSize = if (isSmall) 11.sp else 14.sp

            val containerPadding = if (isSmall) 8.dp else 14.dp
            val innerPadding = if (isSmall) 8.dp else 12.dp
            val blockSpacing = if (isSmall) 6.dp else 10.dp
            val glassMarginHorizontal = if (isSmall) 10.dp else 22.dp

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ImageProvider(R.drawable.widget_background))
                    .padding(containerPadding)
                    .clickable(actionRunCallback<PrayerRefreshAction>()),
                contentAlignment = Alignment.Center
            ) {
                if (timings.values.all { it.isEmpty() }) {
                    Text(
                        text = "Please open the main app to synchronize prayer times.",
                        style = TextStyle(
                            color = ColorProvider(Color.White), 
                            fontSize = if (isSmall) 12.sp else 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, 
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⏰ Prayer Tracker",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFFBBF24)), // GoldPrimary
                                fontSize = titleSize,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(GlanceModifier.height(blockSpacing))
                        
                        // 1. Ongoing Prayer Golden liquid glass card
                        Box(
                            modifier = GlanceModifier
                                .padding(horizontal = glassMarginHorizontal)
                                .background(ImageProvider(R.drawable.widget_gold_glass_card))
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ongoing: ${schedule.ongoingName}",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFFBBF24)), // Highlighted Gold
                                    fontSize = ongoingTextSize,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = GlanceModifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(GlanceModifier.height(blockSpacing))
                        
                        // 2. Upcoming Prayer Green liquid glass card
                        Box(
                            modifier = GlanceModifier
                                .padding(horizontal = glassMarginHorizontal)
                                .background(ImageProvider(R.drawable.widget_glass_card))
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Upcoming: ${schedule.upcomingName} (${schedule.upcomingTime})",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = upcomingTextSize,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = GlanceModifier.fillMaxWidth()
                            )
                        }

                        Spacer(GlanceModifier.height(blockSpacing))

                        // 3. Dynamic countdown Timer card
                        Box(
                            modifier = GlanceModifier
                                .padding(horizontal = glassMarginHorizontal)
                                .background(ImageProvider(R.drawable.widget_glass_card))
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏳ ${schedule.timeLeft}",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFA7F3D0)), // Light Emerald/mint tint
                                    fontSize = timerTextSize,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = GlanceModifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    private data class WidgetSchedule(
        val ongoingName: String,
        val upcomingName: String,
        val upcomingTime: String,
        val timeLeft: String
    )

    private fun calculatePrayerSchedule(timings: Map<String, String>): WidgetSchedule {
        val f = timings["Fajr"]?.ifEmpty { "04:20" } ?: "04:20"
        val s = timings["Sunrise"]?.ifEmpty { "05:45" } ?: "05:45"
        val d = timings["Dhuhr"]?.ifEmpty { "12:30" } ?: "12:30"
        val a = timings["Asr"]?.ifEmpty { "15:45" } ?: "15:45"
        val m = timings["Maghrib"]?.ifEmpty { "18:45" } ?: "18:45"
        val i = timings["Isha"]?.ifEmpty { "20:15" } ?: "20:15"

        val tz = TimeZone.getTimeZone("Asia/Karachi")
        val now = Calendar.getInstance(tz)

        // Date Formatters for parsing values
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }
        val todayStr = sdfDate.format(now.time)

        val tomorrowCal = Calendar.getInstance(tz).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = sdfDate.format(tomorrowCal.time)

        // Helper parser
        fun parseToCal(dateStr: String, timeStr: String): Calendar {
            val cleanTime = timeStr.replace(Regex("\\s\\(.*?\\)"), "").trim()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { timeZone = tz }
            val parsedDate = try {
                sdf.parse("$dateStr $cleanTime") ?: Date()
            } catch (e: Exception) {
                Date()
            }
            return Calendar.getInstance(tz).apply { time = parsedDate }
        }

        val todayFajr = parseToCal(todayStr, f)
        val todaySunrise = parseToCal(todayStr, s)
        val todayDhuhr = parseToCal(todayStr, d)
        val todayAsr = parseToCal(todayStr, a)
        val todayMaghrib = parseToCal(todayStr, m)
        val todayIsha = parseToCal(todayStr, i)

        val tomorrowFajr = parseToCal(tomorrowStr, f)

        // Formatting utilities for presentation times (12h format e.g. "5:45 AM")
        fun formatTo12H(timeStr: String): String {
            val clean = timeStr.replace(Regex("\\s\\(.*?\\)"), "").trim()
            return try {
                val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
                val sdf12 = SimpleDateFormat("h:mm a", Locale.US)
                val date = sdf24.parse(clean)
                if (date != null) sdf12.format(date) else clean
            } catch (e: Exception) {
                clean
            }
        }

        var ongoingName = "Isha"
        var upcomingName = "Fajr"
        var upcomingTime = formatTo12H(f)
        var targetCal = todayFajr

        if (now.before(todayFajr)) {
            ongoingName = "Isha"
            upcomingName = "Fajr"
            upcomingTime = formatTo12H(f)
            targetCal = todayFajr
        } else if (now.after(todayFajr) && now.before(todaySunrise)) {
            ongoingName = "Fajr"
            upcomingName = "Dhuhr"
            upcomingTime = formatTo12H(d)
            targetCal = todayDhuhr
        } else if (now.after(todaySunrise) && now.before(todayDhuhr)) {
            ongoingName = "Sunrise (Rest)"
            upcomingName = "Dhuhr"
            upcomingTime = formatTo12H(d)
            targetCal = todayDhuhr
        } else if (now.after(todayDhuhr) && now.before(todayAsr)) {
            ongoingName = "Dhuhr"
            upcomingName = "Asr"
            upcomingTime = formatTo12H(a)
            targetCal = todayAsr
        } else if (now.after(todayAsr) && now.before(todayMaghrib)) {
            ongoingName = "Asr"
            upcomingName = "Maghrib"
            upcomingTime = formatTo12H(m)
            targetCal = todayMaghrib
        } else if (now.after(todayMaghrib) && now.before(todayIsha)) {
            ongoingName = "Maghrib"
            upcomingName = "Isha"
            upcomingTime = formatTo12H(i)
            targetCal = todayIsha
        } else {
            // Isha active during the night until tomorrow's Fajr
            ongoingName = "Isha"
            upcomingName = "Fajr"
            upcomingTime = formatTo12H(f)
            targetCal = tomorrowFajr
        }

        // Calculate exact remaining countdown
        val diffMs = targetCal.timeInMillis - now.timeInMillis
        val diffHours = diffMs / (1000 * 60 * 60)
        val diffMins = (diffMs / (1000 * 60)) % 60

        val timeLeftStr = if (diffHours > 0) {
            "starts in ${diffHours}h ${diffMins}m"
        } else {
            "starts in ${diffMins}m"
        }

        return WidgetSchedule(
            ongoingName = ongoingName,
            upcomingName = upcomingName,
            upcomingTime = upcomingTime,
            timeLeft = timeLeftStr
        )
    }
}

class PrayerRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        PrayerWidget().update(context, glanceId)
    }
}

class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerWidget()
}
