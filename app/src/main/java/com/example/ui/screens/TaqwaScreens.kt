package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.api.AladhanTimings
import com.example.data.api.QuranWord
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.data.room.BookmarkEntity
import com.example.data.room.TaskEntity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.data.room.UserStatsEntity
import com.example.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.viewmodel.TaqwaViewModel
import com.example.util.FontHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.temporal.ChronoField
import java.util.*
import kotlin.math.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

// ==========================================
// 1. BRAND VECTOR CHASSIS: TAQWALOGO
// ==========================================
@Composable
fun TaqwaLogo(modifier: Modifier = Modifier, noText: Boolean = false) {
    val defaultSize = if (noText) 38.dp else 120.dp
    val goldYellow = Color(0xFFFBBF24)
    
    Box(
        modifier = modifier
            .size(defaultSize),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mosque_hex_022c22_1780678493967),
            contentDescription = "TaqwaHub Logo",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

fun getFormattedHijriAndEasvi(date: Date): Pair<String, String> {
    try {
        val tzIcu = android.icu.util.TimeZone.getTimeZone("Asia/Karachi")
        val cal = android.icu.util.IslamicCalendar(tzIcu)
        cal.time = date
        val d = cal.get(android.icu.util.Calendar.DATE)
        val mValue = cal.get(android.icu.util.Calendar.MONTH) // 0-based
        val y = cal.get(android.icu.util.Calendar.YEAR)
        
        val hijriMonths = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah"
        )
        val mName = hijriMonths.getOrElse(mValue) { "Dhu al-Hijjah" }
        
        val hijriFormatted = "$d $mName, $y AH"
        
        val easviFormatter = SimpleDateFormat("EEEE, d MMMM yyyy 'CE'", Locale.US)
        easviFormatter.timeZone = TimeZone.getTimeZone("Asia/Karachi")
        val easviFormatted = easviFormatter.format(date)
        
        return Pair(hijriFormatted, easviFormatted)
    } catch (e: Throwable) {
        val easviFormatter = SimpleDateFormat("EEEE, d MMMM yyyy 'CE'", Locale.US)
        easviFormatter.timeZone = TimeZone.getTimeZone("Asia/Karachi")
        val easviFormat = easviFormatter.format(date)
        return Pair("07 Dhu al-Hijjah 1447 AH", easviFormat)
    }
}

data class PrayerTimingInfo(
    val name: String,
    val date: Date,
    val isOngoing: Boolean,
    val isUpcoming: Boolean,
    val countdownStr: String
)

fun parsePrayerTime(timeStr: String, baseDate: Date): Date? {
    try {
        if (timeStr.isBlank()) return null
        val cleanTime = timeStr.trim().split(" ")[0].trim()
        val parts = cleanTime.split(":")
        if (parts.size < 2) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
        calendar.time = baseDate
        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    } catch (e: Exception) {
        return null
    }
}

object PrayerTimeCache {
    private var lastDayStr = ""
    private var lastTimings: AladhanTimings? = null
    var cachedOccurrences = listOf<Pair<String, Date>>()
    var cachedTodayPrayers = listOf<Pair<String, Date>>()
    
    fun getOccurrences(timings: AladhanTimings, nowTime: Long): Pair<List<Pair<String, Date>>, List<Pair<String, Date>>> {
        val tz = TimeZone.getTimeZone("Asia/Karachi")
        val f = SimpleDateFormat("yyyyMMdd", Locale.US)
        f.timeZone = tz
        val dayStr = f.format(Date(nowTime))
        
        if (dayStr == lastDayStr && timings == lastTimings) {
            return cachedOccurrences to cachedTodayPrayers
        }
        
        lastDayStr = dayStr
        lastTimings = timings
        
        val currentDate = Date(nowTime)
        
        val prayerKeys = listOf(
            "Fajr" to timings.Fajr,
            "Dhuhr" to timings.Dhuhr,
            "Asr" to timings.Asr,
            "Maghrib" to timings.Maghrib,
            "Isha" to timings.Isha
        )
        
        val yesterday = Calendar.getInstance(tz).apply {
            time = currentDate
            add(Calendar.DAY_OF_YEAR, -1)
        }.time
        
        val today = currentDate
        
        val tomorrow = Calendar.getInstance(tz).apply {
            time = currentDate
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
        
        val occurrences = mutableListOf<Pair<String, Date>>()
        for (base in listOf(yesterday, today, tomorrow)) {
            for (p in prayerKeys) {
                val pDate = parsePrayerTime(p.second, base)
                if (pDate != null) occurrences.add(p.first to pDate)
            }
        }
        occurrences.sortBy { it.second.time }
        
        val todayPrayers = prayerKeys.mapNotNull {
            val pd = parsePrayerTime(it.second, today)
            if (pd != null) it.first to pd else null 
        }
        
        cachedOccurrences = occurrences
        cachedTodayPrayers = todayPrayers
        return occurrences to todayPrayers
    }
}

fun calculateDayPrayers(timings: AladhanTimings, currentDate: Date): List<PrayerTimingInfo> {
    val nowTime = currentDate.time
    val ONGOING_DURATION = 45 * 60 * 1000 // 45 minutes
    
    val (occurrences, todayPrayers) = PrayerTimeCache.getOccurrences(timings, nowTime)
    
    // Find active ongoing prayer
    val ongoingOcc = occurrences.find { occ ->
        val onset = occ.second.time
        nowTime >= onset && nowTime < (onset + ONGOING_DURATION)
    }
    
    // Find next upcoming prayer
    val upcomingOcc = occurrences.find { occ ->
        nowTime < occ.second.time
    }
    
    // Build list representing the 5 prayers of "Today"
    val result = todayPrayers.map { (name, pDate) ->
        val matchesUpcoming = upcomingOcc != null && upcomingOcc.first == name
        val finalIsUpcoming = matchesUpcoming // Remove ongoingOcc == null restriction
        
        var finalCountdownStr = ""
        if (finalIsUpcoming && upcomingOcc != null) {
            val diffMs = upcomingOcc.second.time - nowTime
            if (diffMs > 0) {
                val totalSecs = diffMs / 1000
                val h = totalSecs / 3600
                val m = (totalSecs % 3600) / 60
                val s = totalSecs % 60
                val hStr = if (h < 10) "0$h" else "$h"
                val mStr = if (m < 10) "0$m" else "$m"
                val sStr = if (s < 10) "0$s" else "$s"
                finalCountdownStr = if (h > 0) "${hStr}:${mStr}:${sStr}" else "${mStr}:${sStr}"
            }
        }
        
        val finalIsOngoing = ongoingOcc != null && ongoingOcc.first == name
        
        PrayerTimingInfo(
            name = name,
            date = pDate,
            isOngoing = finalIsOngoing,
            isUpcoming = finalIsUpcoming,
            countdownStr = finalCountdownStr
        )
    }
    
    return result
}

// ==========================================
// 2. DASHBOARD / HOME SCREEN
// ==========================================

data class GreetingItem(
    val phrase: String,
    val language: String,
    val isArabicOrUrdu: Boolean = false
)

@Composable
fun DashboardWelcomeHeader(
    stats: UserStatsEntity,
    viewModel: TaqwaViewModel,
    onNavigate: (String) -> Unit
) {
    var baseDayString by remember { mutableStateOf("") }
    var hijriDateStr by remember { mutableStateOf("") }
    var easviDateStr by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val tz = TimeZone.getTimeZone("Asia/Karachi")
        val f = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = tz }
        while (true) {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val newDayStr = f.format(date)
            
            if (newDayStr != baseDayString) {
                val pair = getFormattedHijriAndEasvi(date)
                baseDayString = newDayStr
                hijriDateStr = pair.first
                easviDateStr = pair.second
            }
            kotlinx.coroutines.delay(1000 * 60)
        }
    }

    val greetings = remember {
        listOf(
            GreetingItem("Assalamu\nAlaikum", "ENGLISH DEFAULT"),
            GreetingItem("السَّلَامُ\nعَلَيْكُمْ", "ARABIC", isArabicOrUrdu = true),
            GreetingItem("السلام\nعلیکم", "URDU / PUNJABI", isArabicOrUrdu = true),
            GreetingItem("Selamün\nAleyküm", "TURKISH"),
            GreetingItem("Assalamu\nalaikum", "INDONESIAN / MALAY"),
            GreetingItem("আসসালামু\nআলাইকুম", "BENGALI"),
            GreetingItem("سلام\nعلیکم", "PERSIAN", isArabicOrUrdu = true),
            GreetingItem("As-salamu\nalaykum", "SWAHILI"),
            GreetingItem("Selamun\nalejkum", "BOSNIAN / ALBANIAN")
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    LaunchedEffect(viewModel.isMultiLingualGreetingEnabled) {
        if (viewModel.isMultiLingualGreetingEnabled) {
            while (true) {
                kotlinx.coroutines.delay(3500)
                currentIndex = (currentIndex + 1) % greetings.size
            }
        } else {
            currentIndex = 0
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF064E3B), Color(0xFF022C22))))
                .padding(32.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(32.dp).height(1.dp).background(GoldPrimary.copy(alpha = 0.8f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = easviDateStr.uppercase(Locale.US),
                        color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = hijriDateStr.uppercase(Locale.US),
                    color = Color(0xFFA7F3D0), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 40.dp, top = 4.dp)) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFACC15), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stats.currentStreak} DAY${if (stats.currentStreak == 1) "" else "S"} STREAK",
                        color = Color(0xFFFACC15), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                if (viewModel.isMultiLingualGreetingEnabled) {
                    val currentGreeting = greetings[currentIndex]
                    Crossfade(
                        targetState = currentGreeting,
                        animationSpec = tween(durationMillis = 800),
                        label = "greeting_crossfade"
                    ) { greeting ->
                        Column(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 115.dp)) {
                            Text(
                                text = greeting.phrase,
                                color = Color(0xFFECFDF5),
                                fontSize = if (greeting.isArabicOrUrdu) 38.sp else 34.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = if (greeting.isArabicOrUrdu) 46.sp else 40.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = greeting.language,
                                color = GoldPrimary.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Assalamu\nAlaikum",
                        color = Color(0xFFECFDF5),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 42.sp,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "\"Verily, in the remembrance of Allah do hearts find rest.\" (13:28)",
                    color = Color(0xFFD1FAE5).copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onNavigate("leaderboard") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = BorderStroke(1.dp, Color(0xFFD1FAE5).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp), modifier = Modifier.weight(1f).height(48.dp).background(Color(0xFFD1FAE5).copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(imageVector = Icons.Default.Leaderboard, contentDescription = "Leaderboard", tint = Color(0xFF6EE7B7), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Leaderboard", color = Color(0xFFECFDF5), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { onNavigate("dua") }, 
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.weight(1f).height(48.dp).background(brush = Brush.linearGradient(colors = listOf(Color(0xFFFBBF24), Color(0xFFD4AF37))), shape = RoundedCornerShape(16.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(imageVector = Icons.Default.AutoStories, contentDescription = "Duas", tint = Color(0xFF042F2E), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Duas", color = Color(0xFF042F2E), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardPrayerTimesGrid(viewModel: TaqwaViewModel, defaultTimings: AladhanTimings) {
    var currentTimeString by remember { mutableStateOf("") }
    var computedPrayers by remember { mutableStateOf<List<PrayerTimingInfo>>(emptyList()) }
    var showJamatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(defaultTimings) {
        val tz = TimeZone.getTimeZone("Asia/Karachi")
        val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.US).apply { timeZone = tz }
        while (true) {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val newTimeStr = timeFormatter.format(date)
            val updatedPrayers = calculateDayPrayers(defaultTimings, date)

            currentTimeString = newTimeStr
            computedPrayers = updatedPrayers

            kotlinx.coroutines.delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "Prayer & Jamat", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Auto-updates based on location", color = TextGray, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable { showJamatDialog = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Edit offsets", tint = GoldPrimary, modifier = Modifier.size(14.dp))
                    Text("Configure Jamat", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        val prayerIcons = mapOf(
            "Fajr" to Icons.Default.WbTwilight, "Dhuhr" to Icons.Default.WbSunny, "Asr" to Icons.Default.FilterDrama,
            "Maghrib" to Icons.Default.WbCloudy, "Isha" to Icons.Default.NightsStay
        )
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            computedPrayers.forEach { prayerInfo ->
                val icon = prayerIcons[prayerInfo.name] ?: Icons.Default.WbSunny
                val prayerHadeeths = mapOf(
                    "Fajr" to "The 2 Sunnah rak'ats of Fajr are better than the world and all it contains. (Muslim)",
                    "Dhuhr" to "A time when the gates of heaven are opened. (Tirmidhi)",
                    "Asr" to "He who misses Asr, it is as if he has lost his family and property. (Bukhari)",
                    "Maghrib" to "Hasten to pray Maghrib before the stars appear. (Ahmad)",
                    "Isha" to "Whoever prays Isha in congregation, it is as if he spent half the night in prayer. (Muslim)"
                )
                val hadeethStr = prayerHadeeths[prayerInfo.name] ?: ""
                val timingRawStr = when (prayerInfo.name) {
                    "Fajr" -> defaultTimings.Fajr "Dhuhr" -> defaultTimings.Dhuhr "Asr" -> defaultTimings.Asr
                    "Maghrib" -> defaultTimings.Maghrib "Isha" -> defaultTimings.Isha else -> ""
                }
                val offset = when (prayerInfo.name) {
                    "Fajr" -> viewModel.fajrJamatOffset
                    "Dhuhr" -> viewModel.dhuhrJamatOffset
                    "Asr" -> viewModel.asrJamatOffset
                    "Maghrib" -> viewModel.maghribJamatOffset
                    "Isha" -> viewModel.ishaJamatOffset
                    else -> 15
                }
                val timingStr = remember(timingRawStr) {
                    try {
                        val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
                        val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
                        val dateObj = sdf24.parse(timingRawStr.replace(Regex("\\s\\(.*?\\)"), ""))
                        if (dateObj != null) sdf12.format(dateObj) else timingRawStr
                    } catch (e: Exception) { timingRawStr }
                }
                val jamatStr = remember(timingRawStr, offset) {
                    try {
                        val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
                        val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
                        val dateObj = sdf24.parse(timingRawStr.replace(Regex("\\s\\(.*?\\)"), ""))
                        if (dateObj != null) {
                            val cal = Calendar.getInstance().apply {
                                time = dateObj
                                add(Calendar.MINUTE, offset)
                            }
                            sdf12.format(cal.time)
                        } else {
                            timingRawStr
                        }
                    } catch (e: Exception) { timingRawStr }
                }
                Card(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = EmeraldCard), border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                        Text(text = "ص", fontSize = 80.sp, color = GoldPrimary.copy(alpha = 0.03f), modifier = Modifier.align(Alignment.Center), fontFamily = FontFamily.Serif)
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(GoldPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = prayerInfo.name, tint = GoldPrimary, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = prayerInfo.name, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("AZAAN BEGINS", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = timingStr, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(GoldPrimary.copy(alpha = 0.2f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("JAMAT TIME", fontSize = 10.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = jamatStr, fontSize = 14.sp, color = GoldPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "(+$offset mins offset)", fontSize = 10.sp, color = GoldPrimary.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = hadeethStr, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, lineHeight = 16.sp)
                            if (prayerInfo.isOngoing) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.size(6.dp).background(GoldPrimary, CircleShape))
                                        Text(text = "ONGOING", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    }
                                }
                            } else if (prayerInfo.isUpcoming && prayerInfo.countdownStr.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Text(text = "- ${prayerInfo.countdownStr}", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showJamatDialog) {
        AlertDialog(
            onDismissRequest = { showJamatDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Jamat Settings",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jamat Time Configuration", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Jamat times are calculated as an offset (in minutes) after Azaan. They automatically shift dynamically when you change locations! Set the offset in minutes for each of the 5 prayers below:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                    prayers.forEach { name ->
                        val currentOffset = when(name) {
                            "Fajr" -> viewModel.fajrJamatOffset
                            "Dhuhr" -> viewModel.dhuhrJamatOffset
                            "Asr" -> viewModel.asrJamatOffset
                            "Maghrib" -> viewModel.maghribJamatOffset
                            "Isha" -> viewModel.ishaJamatOffset
                            else -> 15
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                androidx.compose.material3.IconButton(
                                    onClick = { 
                                        if (currentOffset >= 5) {
                                            viewModel.updateJamatOffset(name, currentOffset - 5)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "+$currentOffset min",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                androidx.compose.material3.IconButton(
                                    onClick = { 
                                        if (currentOffset <= 90) {
                                            viewModel.updateJamatOffset(name, currentOffset + 5)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showJamatDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save & Close", color = Color(0xFF042F2E), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = EmeraldCard
        )
    }
}

@Composable
fun HomeAnnouncementsFeed(viewModel: TaqwaViewModel) {
    val announcements = viewModel.announcementsList
    if (announcements.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Announcements Feed",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Announcements & Updates",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        announcements.forEach { announce ->
            val icon = when (announce.type) {
                "Update" -> Icons.Default.NewReleases
                "Reminder" -> Icons.Default.NotificationsActive
                else -> Icons.Default.Campaign
            }
            val tintColor = when (announce.type) {
                "Update" -> Color(0xFF6EE7B7) // bright emerald
                "Reminder" -> Color(0xFFFBBF24) // sunny yellow
                else -> GoldPrimary
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF063327)),
                border = BorderStroke(1.dp, tintColor.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(tintColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = announce.type,
                                tint = tintColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = announce.title.ifEmpty { announce.type },
                                color = tintColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val formattedDate = try {
                                java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.US)
                                    .format(java.util.Date(announce.timestamp))
                            } catch (e: Exception) {
                                ""
                            }
                            if (formattedDate.isNotEmpty()) {
                                Text(
                                    text = formattedDate,
                                    color = Color(0xFFA7F3D0).copy(alpha = 0.4f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LoopingAnnouncementText(
                        text = announce.message,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun LoopingAnnouncementText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: androidx.compose.ui.unit.TextUnit = 18.sp
) {
    val isUrdu = remember(text) {
        text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF' }
    }

    val duration = remember(text) {
        (text.length * 150 + 4000).coerceIn(6000, 25000)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "announcement_marquee")
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(clip = true),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthDp = maxWidth
        val density = androidx.compose.ui.platform.LocalDensity.current
        
        val textWidthPx = remember(text, density) {
            with(density) { (text.length * 8.5f).dp.toPx() }
        }
        val widthPx = remember(widthDp, density) {
            with(density) { widthDp.toPx() }
        }

        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            lineHeight = lineHeight,
            maxLines = 1,
            modifier = Modifier
                .offset {
                    val xOffset = if (isUrdu) {
                        // Right to Left animation: start at right outside, move to left outside
                        val startX = widthPx
                        val endX = -textWidthPx
                        startX + (endX - startX) * progress
                    } else {
                        // Left to Right animation: start at left outside, move to right outside
                        val startX = -textWidthPx
                        val endX = widthPx
                        startX + (endX - startX) * progress
                    }
                    androidx.compose.ui.unit.IntOffset(xOffset.toInt(), 0)
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaqwaViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()

    val completedCount = tasks.filter { it.completed }.size
    val totalCount = tasks.size
    val defaultTimings = prayerTimes ?: AladhanTimings(
        "04:20", "05:45", "12:30", "15:45", "18:45", "20:15"
    )

    val progress = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header Profile Row
        item {
            DashboardWelcomeHeader(stats = stats, viewModel = viewModel, onNavigate = onNavigate)
        }

        if (viewModel.appConfig.welcomeBannerMessage.isNotEmpty()) {
            item {
                AnnouncementBanner(message = viewModel.appConfig.welcomeBannerMessage)
            }
        }

        if (viewModel.announcementsList.isNotEmpty()) {
            item {
                HomeAnnouncementsFeed(viewModel = viewModel)
            }
        }


        // Resume Reading Bookmark cards (Multiple Surah Readings)
        val sortedBookmarks = bookmarks.sortedByDescending { it.timestamp }
        
        items(sortedBookmarks) { bookmark ->
            val displaySurahId = bookmark.surahNumber
            val displayVerseKey = bookmark.verseKey
            val displayVerseNum = bookmark.verseNumber

            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    viewModel.isContinuousFlowMode = bookmark.isFlowMode
                    viewModel.selectChapter(displaySurahId)
                    viewModel.requestedScrollVerseId = displayVerseNum
                    onNavigate("quran")
                },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Book, contentDescription = "Book", tint = GoldPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "RESUME READING",
                                color = GoldPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            val cachedSurah = IslamicData.surahs.find { it.id == displaySurahId }
                            Text(
                                text = cachedSurah?.name ?: "Surah $displaySurahId",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ayah $displayVerseNum",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = GoldPrimary
                    )
                }
            }
        }

        // Prayer Timings Dashboard Grid
        // Prayer Times block
        item {
            if (viewModel.appConfig.isPrayerTimesCardLocked && !viewModel.isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = GoldPrimary.copy(alpha=0.5f), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Prayer Times Locked", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(viewModel.appConfig.prayerTimesBlockedMessage, color = TextGray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                if (viewModel.hasLocationPermission) {
                    DashboardPrayerTimesGrid(viewModel = viewModel, defaultTimings = defaultTimings)
                } else {
                    LocationPermissionCard(viewModel = viewModel, onPermissionGranted = {})
                }
            }
        }

        // Daily Checklist goals Card
        item {
            if (viewModel.appConfig.isTrackerCardLocked && !viewModel.isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = GoldPrimary.copy(alpha=0.5f), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Daily Tracker Locked", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(viewModel.appConfig.trackerBlockedMessage, color = TextGray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Checklist",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount/$totalCount Completed Today",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = GoldPrimary.copy(alpha = 0.2f),
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                    drawArc(
                                        color = GoldPrimary,
                                        startAngle = -90f,
                                        sweepAngle = progress * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Progress slider line
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldPrimary,
                        trackColor = GoldPrimary.copy(alpha = 0.1f)
                    )

                    // Show top 3 default tasks quickly
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        tasks.take(3).forEach { task ->
                            val timing = viewModel.getTaskTimingStatus(task.title, task.completed)
                            val isLockedAdvance = timing.isLockedAdvance
                            val isMissed = timing.isMissed
                            val isPrayerOrCharity = task.category == "Salah" || 
                                                    task.category == "Deeds" || 
                                                    task.id.startsWith("manual_charity") ||
                                                    task.title.contains("Charity", ignoreCase = true) || 
                                                    task.title.contains("Sadaqah", ignoreCase = true) || 
                                                    task.title.contains("Zakat", ignoreCase = true)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isMissed) Color(0xFFEF4444).copy(alpha = 0.05f)
                                        else if (isLockedAdvance) EmeraldMedium.copy(alpha = 0.15f)
                                        else EmeraldMedium.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isMissed) Color(0xFFEF4444).copy(alpha = 0.25f)
                                                else if (task.completed) GoldPrimary.copy(alpha = 0.2f)
                                                else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isPrayerOrCharity) {
                                            if (isLockedAdvance) {
                                                android.widget.Toast.makeText(context, "Prayer starts at ${timing.startStr}", android.widget.Toast.LENGTH_SHORT).show()
                                            } else if (isMissed) {
                                                android.widget.Toast.makeText(context, "Prayer is Missed/Forgotten", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.toggleMainTask(task.id, !task.completed)
                                            }
                                        } else {
                                            viewModel.handleTaskRedirection(task, context, bookmarks, onNavigate)
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (task.completed) GoldPrimary
                                                else if (isLockedAdvance) Color.White.copy(alpha = 0.05f)
                                                else if (isMissed) Color(0xFFEF4444).copy(alpha = 0.1f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = if (task.completed) GoldPrimary
                                                        else if (isLockedAdvance) Color.White.copy(alpha = 0.15f)
                                                        else if (isMissed) Color(0xFFEF4444).copy(alpha = 0.4f)
                                                        else GoldPrimary.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                if (isPrayerOrCharity) {
                                                    if (isLockedAdvance) {
                                                        android.widget.Toast.makeText(context, "Prayer starts at ${timing.startStr}", android.widget.Toast.LENGTH_SHORT).show()
                                                    } else if (isMissed) {
                                                        android.widget.Toast.makeText(context, "Prayer is Missed/Forgotten", android.widget.Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.toggleMainTask(task.id, !task.completed)
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(context, "Automatic: Tap on card to visit page! 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                                    viewModel.handleTaskRedirection(task, context, bookmarks, onNavigate)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (task.completed) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = OnGoldText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else if (!isPrayerOrCharity) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Auto",
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        } else if (isLockedAdvance) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.3f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        } else if (isMissed) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Missed",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = task.title,
                                            color = if (task.completed) TextGray else if (isLockedAdvance) Color.White.copy(alpha = 0.5f) else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (isLockedAdvance) {
                                            Text(
                                                text = "Begins at ${timing.startStr}",
                                                color = GoldPrimary.copy(alpha = 0.6f),
                                                fontSize = 10.sp
                                            )
                                        } else if (isMissed) {
                                            Text(
                                                text = "Missed & Forgotten (Ended at ${timing.endStr})",
                                                color = Color(0xFFEF4444),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isMissed) "Missed" else task.category,
                                    color = if (isMissed) Color(0xFFEF4444) else GoldPrimary.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .background(
                                            if (isMissed) Color(0xFFEF4444).copy(alpha = 0.1f) else GoldPrimary.copy(alpha = 0.1f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Button to go to checklist
                    OutlinedButton(
                        onClick = { onNavigate("tasks") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Text("VIEW ALL TASKS", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                }
            }
            }
        }

        // Blessed Jumu'ah Friday Countdowns Card
        item {
            if (viewModel.appConfig.isDailyAyahCardLocked && !viewModel.isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = GoldPrimary.copy(alpha=0.5f), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Jumu'ah Countdown Locked", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(viewModel.appConfig.dailyAyahBlockedMessage, color = TextGray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                DashboardJumuahCard()
            }
        }
    }
}

@Composable
fun DashboardJumuahCard() {
    var jumuahStatus by remember { mutableStateOf("countdown") } // "countdown" or "live"
    var days by remember { mutableStateOf("00") }
    var hours by remember { mutableStateOf("00") }
    var mins by remember { mutableStateOf("00") }
    var secs by remember { mutableStateOf("00") }

    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ping_alpha"
    )
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ping_scale"
    )

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
            val nowMillis = calendar.timeInMillis
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            
            val isFriday = dayOfWeek == Calendar.FRIDAY
            val isLiveTime = isFriday && (hourOfDay == 12 && minute >= 45 || hourOfDay == 13 || hourOfDay == 14 && minute <= 30)

            if (isLiveTime) {
                jumuahStatus = "live"
            } else {
                jumuahStatus = "countdown"
                // Find next Friday 13:15 (1:15 PM)
                val targetCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
                targetCalendar.set(Calendar.HOUR_OF_DAY, 13)
                targetCalendar.set(Calendar.MINUTE, 15)
                targetCalendar.set(Calendar.SECOND, 0)
                targetCalendar.set(Calendar.MILLISECOND, 0)
                
                if (dayOfWeek == Calendar.FRIDAY && (hourOfDay > 14 || (hourOfDay == 14 && minute > 30))) {
                    targetCalendar.add(Calendar.DAY_OF_YEAR, 7)
                } else {
                    val daysToAdd = (Calendar.FRIDAY - dayOfWeek + 7) % 7
                    if (daysToAdd > 0) {
                        targetCalendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    }
                }

                val diff = targetCalendar.timeInMillis - nowMillis
                if (diff > 0) {
                    val d = diff / (1000 * 60 * 60 * 24)
                    val h = (diff / (1000 * 60 * 60)) % 24
                    val m = (diff / (1000 * 60)) % 60
                    val s = (diff / 1000) % 60

                    days = d.toString().padStart(2, '0')
                    hours = h.toString().padStart(2, '0')
                    mins = m.toString().padStart(2, '0')
                    secs = s.toString().padStart(2, '0')
                }
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.20f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldCard)
                .drawBehind {
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.06f),
                        radius = size.width * 0.4f,
                        center = Offset(size.width, 0f)
                    )
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.05f),
                        radius = size.width * 0.3f,
                        center = Offset(0f, size.height)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(1.dp, Color(0xFFFDE047).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Blessed Weekly Sunnah",
                            tint = Color(0xFF022C22),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "BLESSED WEEKLY SUNNAH",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.8.sp
                        )
                        Text(
                            text = "Jumu'ah Friday Prayer",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                val isLive = jumuahStatus == "live"
                val quoteText = if (isLive) {
                    "\"Jumu'ah Mubarak! The weekly Friday service and Khutbah is currently in session! Send abundant Salawat and blessings upon the Holy Prophet (PBUH) during this holy hour.\""
                } else {
                    "\"Friday is the best day on which the sun has risen; on it Adam was created, and on it he was entered into Paradise.\" (Sahih Muslim). Prepare early for the weekly sermon."
                }
                
                Text(
                    text = quoteText,
                    color = if (isLive) GoldPrimary else Color(0xFFA7F3D0).copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sunnahs = listOf(
                        "🚿 Ghusl Bath" to "Ghusl",
                        "📿 Salawat" to "Salawat",
                        "📖 Surah Al-Kahf" to "Al-Kahf"
                    )
                    sunnahs.forEach { (text, _) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF022C22).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                color = Color(0xFFA7F3D0).copy(alpha = 0.9f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF022C22).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                        .padding(16.dp)
                ) {
                    if (isLive) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer(scaleX = pingScale, scaleY = pingScale)
                                        .background(Color(0xFF34D399).copy(alpha = pingAlpha * 0.4f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF34D399), CircleShape)
                                )
                            }

                            Text(
                                text = "LIVE SERVICE ONGOING",
                                color = Color(0xFF34D399),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "In session till Friday 2:30 PM (14:30)",
                                color = Color(0xFFA7F3D0).copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "NEXT CONGREGATION",
                                color = GoldPrimary.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.5.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    days to "Days",
                                    hours to "Hours",
                                    mins to "Mins",
                                    secs to "Secs"
                                ).forEachIndexed { index, (value, label) ->
                                    if (index > 0) {
                                        Text(
                                            text = ":",
                                            color = GoldPrimary,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                    }
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF011D17).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                                .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                .size(width = 50.dp, height = 44.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = value,
                                                color = if (label == "Secs") GoldPrimary else Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Text(
                                            text = label.uppercase(Locale.US),
                                            color = Color(0xFFA7F3D0).copy(alpha = 0.4f),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Target Time: Friday 1:15 PM",
                                color = Color(0xFFA7F3D0).copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getSpokenWordIdAtTime(
    elapsedMs: Long,
    totalDurationMs: Long,
    spokenWords: List<com.example.data.api.QuranWord>
): Int? {
    if (spokenWords.isEmpty() || totalDurationMs <= 0) return null
    
    // Reserve 12% of the verse duration for the ending pause/breath, max 1200ms, min 300ms
    val endingPauseMs = (totalDurationMs * 0.12).toLong().coerceIn(300, 1200)
    val speakingDurationMs = (totalDurationMs - endingPauseMs).coerceAtLeast((totalDurationMs * 0.7).toLong())
    
    if (elapsedMs >= speakingDurationMs) {
        // We are in the trailing pause/breath, so no word is highlighted
        return null
    }
    
    val wordLengths = spokenWords.map { word ->
        val text = word.text_uthmani ?: word.text_indopak ?: ""
        text.length.coerceAtLeast(2)
    }
    val totalChars = wordLengths.sum()
    if (totalChars <= 0) return null
    
    var cumulativeChars = 0
    for (i in spokenWords.indices) {
        val wordLen = wordLengths[i]
        val startMs = (cumulativeChars.toDouble() / totalChars * speakingDurationMs).toLong()
        val endMs = ((cumulativeChars + wordLen).toDouble() / totalChars * speakingDurationMs).toLong()
        
        if (elapsedMs in startMs until endMs) {
            return spokenWords[i].id
        }
        cumulativeChars += wordLen
    }
    
    return spokenWords.lastOrNull()?.id
}

// ==========================================
// 3. NOBLE QURAN CONTAINER (QURANREADER)
// ==========================================
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun QuranReaderScreen(
    viewModel: TaqwaViewModel,
    onNavigate: (String) -> Unit
) {
    val chapters = viewModel.quranChapters
    val activeVerses = viewModel.activeVerses
    val activeTranslations = viewModel.activeTranslations
    val isChaptersLoading = viewModel.isChaptersLoading
    val isVersesLoading = viewModel.isVersesLoading
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var expandedAyahKey by remember { mutableStateOf<String?>(null) }
    var selectedWordMeaning by remember { mutableStateOf<QuranWord?>(null) }
    var isContinuousPlaying by remember { mutableStateOf(false) }
    var currentPlayingVerseIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(viewModel.selectedReciterId) {
        isContinuousPlaying = false
        currentPlayingVerseIndex = null
    }

    androidx.activity.compose.BackHandler(enabled = viewModel.selectedSurah != null) {
        viewModel.audioPlayerHelper.stop()
        isContinuousPlaying = false
        currentPlayingVerseIndex = null
        viewModel.selectedSurah = null
    }

    LaunchedEffect(Unit) {
        if (chapters.isEmpty()) {
            viewModel.loadQuranChapters()
        }
    }

    if (viewModel.selectedSurah != null) {
        // Render Active Chapter View Interface
        val surah = viewModel.selectedSurah!!
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        val isContinuousFlowMode = viewModel.isContinuousFlowMode
        var isCompactCardMode by remember { mutableStateOf(false) }
        var isSettingsMenuExpanded by remember { mutableStateOf(false) }
        val reciters = listOf(
            Pair(7, "Mishary Alafasy"),
            Pair(3, "Abdul Rahman Al-Sudais"),
            Pair(6, "Maher Al-Muaiqly"),
            Pair(12, "Yasser Al-Dosari"),
            Pair(2, "Abdul Basit (Classic)"),
            Pair(1, "Abu Bakr Al-Shatri")
        )

        val playbackProgressMs by viewModel.audioPlayerHelper.playbackProgressMs.collectAsState(initial = 0)
        val playbackDurationMs by viewModel.audioPlayerHelper.playbackDurationMs.collectAsState(initial = 0)
        val isAudioPlaying by viewModel.audioPlayerHelper.isAudioPlaying.collectAsState(initial = false)
        val currentPlayingUrl by viewModel.audioPlayerHelper.currentlyPlayingUrl.collectAsState(initial = null)

        val activeVerseKey = remember(currentPlayingUrl, isContinuousPlaying, currentPlayingVerseIndex) {
            if (isContinuousPlaying) {
                currentPlayingVerseIndex?.let { idx ->
                    if (idx in activeVerses.indices) activeVerses[idx].verse_key else null
                }
            } else {
                val playingUrl = currentPlayingUrl
                if (playingUrl != null) {
                    if (playingUrl.startsWith("verse_")) {
                        playingUrl.removePrefix("verse_")
                    } else {
                        val matchIndex = activeVerses.indexOfFirst { verse ->
                            val verseAudio = viewModel.audioOverrides["verse_${verse.verse_key}"] ?: viewModel.activeVerseAudioUrls[verse.verse_key]
                            verseAudio != null && (playingUrl == verseAudio || playingUrl.endsWith(verseAudio) || playingUrl.contains(verseAudio) || viewModel.audioPlayerHelper.resolveUrl(playingUrl) == viewModel.audioPlayerHelper.resolveUrl(verseAudio))
                        }
                        if (matchIndex != -1) activeVerses[matchIndex].verse_key else null
                    }
                } else {
                    null
                }
            }
        }

        LaunchedEffect(surah.id, activeVerseKey, isAudioPlaying) {
            var elapsedSecondsInSession = 0L
            var turnIncremented = false
            val dwellMap = mutableMapOf<Int, Int>() // maps verseNumber to seconds spent
            
            while (true) {
                kotlinx.coroutines.delay(1000)
                elapsedSecondsInSession++
                
                // 1. Get visible verses from listState
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                val visibleVerseNumbers = mutableSetOf<Int>()
                for (item in visibleItems) {
                    val keyStr = item.key?.toString() ?: ""
                    val verseNum = when {
                        keyStr.startsWith("flow_") && keyStr.contains(":") -> {
                            keyStr.substringAfter(":").toIntOrNull()
                        }
                        keyStr.contains(":") -> {
                            keyStr.substringAfter(":").toIntOrNull()
                        }
                        else -> null
                    }
                    if (verseNum != null) {
                        visibleVerseNumbers.add(verseNum)
                    }
                }
                
                // 2. Identify the currently playing verse
                val playingVerseNum = if (isAudioPlaying && activeVerseKey != null && activeVerseKey.contains(":")) {
                    activeVerseKey.substringAfter(":").toIntOrNull()
                } else null
                
                // 3. Prevent rapid-scrolling abuse (anti-cheat)
                // If more than 5 verses are visible on screen in a single second, consider it quick scroll skipping.
                val isScrollingFast = visibleVerseNumbers.size > 5 && playingVerseNum == null
                
                if (!isScrollingFast) {
                    // Accumulate reading/reciting time persistently
                    viewModel.accumulateSurahTime(surah.id, 1L, surah.versesCount)
                    
                    // Register a turn after 5 seconds of active engagement
                    if (elapsedSecondsInSession >= 5 && !turnIncremented) {
                        viewModel.incrementSurahTurnCount(surah.id)
                        turnIncremented = true
                    }
                    
                    val versesToMark = mutableSetOf<Int>()
                    
                    // If audio is playing, that verse is verified immediately
                    if (playingVerseNum != null) {
                        versesToMark.add(playingVerseNum)
                    }
                    
                    // For visible verses, require at least 2 seconds of continuous focus (dwell time)
                    for (vNum in visibleVerseNumbers) {
                        val currentDwell = dwellMap.getOrDefault(vNum, 0) + 1
                        dwellMap[vNum] = currentDwell
                        if (currentDwell >= 2) {
                            versesToMark.add(vNum)
                        }
                    }
                    
                    // Clean up dwellMap for verses that are no longer visible or playing
                    val keysToRemove = dwellMap.keys.filter { it !in visibleVerseNumbers && it != playingVerseNum }
                    keysToRemove.forEach { dwellMap.remove(it) }
                    
                    if (versesToMark.isNotEmpty()) {
                        viewModel.addVisitedVerses(surah.id, versesToMark, surah.versesCount)
                    }
                } else {
                    // If scrolling fast, clear dwellMap to reset progress
                    dwellMap.clear()
                }
            }
        }

        LaunchedEffect(activeVerseKey) {
            if (activeVerseKey != null) {
                val index = activeVerses.indexOfFirst { it.verse_key == activeVerseKey }
                if (index != -1) {
                    val headerOffset = if (surah.id != 9 && surah.id != 1) 2 else 1
                    listState.animateScrollToItem(index + headerOffset)
                }
            }
        }

        val playVerseAtIndex: (Int) -> Unit = { index ->
            executePlayVerseAtIndex(
                index = index,
                activeVerses = activeVerses,
                surah = surah,
                viewModel = viewModel,
                context = context,
                coroutineScope = coroutineScope,
                listState = listState,
                onSetContinuousPlaying = { isContinuousPlaying = it },
                onSetCurrentPlayingVerseIndex = { currentPlayingVerseIndex = it }
            )
        }


        LaunchedEffect(currentPlayingUrl, isContinuousPlaying) {
            if (isContinuousPlaying) {
                if (currentPlayingUrl == null) {
                    currentPlayingVerseIndex?.let { currentIndex ->
                        kotlinx.coroutines.delay(50)
                        val nextIndex = currentIndex + 1
                        if (nextIndex < activeVerses.size) {
                            playVerseAtIndex(nextIndex)
                        } else {
                            isContinuousPlaying = false
                            currentPlayingVerseIndex = null
                        }
                    }
                } else {
                    val playingUrl = currentPlayingUrl
                    if (playingUrl != null) {
                        val matchIndex = activeVerses.indexOfFirst { verse ->
                            val verseAudio = viewModel.audioOverrides["verse_${verse.verse_key}"] ?: viewModel.activeVerseAudioUrls[verse.verse_key]
                            verseAudio != null && (playingUrl.endsWith(verseAudio) || playingUrl.contains(verseAudio))
                        }
                        if (matchIndex != -1 && matchIndex != currentPlayingVerseIndex) {
                            currentPlayingVerseIndex = matchIndex
                        }
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                if (isContinuousPlaying) {
                    viewModel.audioPlayerHelper.stop()
                    isContinuousPlaying = false
                    currentPlayingVerseIndex = null
                }
            }
        }

        val density = LocalDensity.current
        LaunchedEffect(viewModel.requestedScrollVerseId, activeVerses) {
            val targetId = viewModel.requestedScrollVerseId
            if (targetId != null && activeVerses.isNotEmpty()) {
                val index = activeVerses.indexOfFirst { it.id == targetId }
                if (index != -1) {
                    val headerOffset = if (surah.id != 9 && surah.id != 1) 2 else 1
                    val offsetPx = with(density) { -24.dp.roundToPx() }
                    listState.animateScrollToItem(index + headerOffset, scrollOffset = offsetPx)
                    viewModel.requestedScrollVerseId = null
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EmeraldBackground)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(if (isCompactCardMode) 10.dp else 16.dp)
            ) {
            // Item 0: Surah Header & Controls & Arabic Name & Compact View Chips
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.audioPlayerHelper.stop()
                                    isContinuousPlaying = false
                                    currentPlayingVerseIndex = null
                                    viewModel.selectedSurah = null
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = surah.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${surah.versesCount} Ayahs • ${surah.revelationType}",
                                    color = TextGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (isContinuousPlaying) {
                                        isContinuousPlaying = false
                                        viewModel.audioPlayerHelper.stop()
                                    } else {
                                        isContinuousPlaying = true
                                        val resolvedStart = when {
                                            currentPlayingVerseIndex != null && currentPlayingVerseIndex!! in activeVerses.indices -> currentPlayingVerseIndex!!
                                            else -> {
                                                val startIndex = activeVerses.indexOfFirst { verse ->
                                                    val verseAudio = viewModel.audioOverrides["verse_${verse.verse_key}"] ?: viewModel.activeVerseAudioUrls[verse.verse_key]
                                                    verseAudio != null && currentPlayingUrl?.let { url -> url.endsWith(verseAudio) || url.contains(verseAudio) } == true
                                                }
                                                if (startIndex != -1) startIndex else 0
                                            }
                                        }
                                        playVerseAtIndex(resolvedStart)
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isContinuousPlaying) GoldPrimary else EmeraldCard,
                                    contentColor = if (isContinuousPlaying) EmeraldCard else GoldPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isContinuousPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Continuous Playback",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isContinuousPlaying) "Playing" else "Auto Play",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { isSettingsMenuExpanded = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Settings",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = isSettingsMenuExpanded,
                                    onDismissRequest = { isSettingsMenuExpanded = false },
                                    modifier = Modifier.background(EmeraldCard)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isCompactCardMode) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Compact Cards", color = Color.White, fontSize = 13.sp)
                                            }
                                        },
                                        onClick = {
                                            isCompactCardMode = !isCompactCardMode
                                            isSettingsMenuExpanded = false
                                        }
                                    )

                                    androidx.compose.material3.HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f))

                                    DropdownMenuItem(
                                        text = { Text("Select Audio Reciter:", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        onClick = {},
                                        enabled = false
                                    )

                                    reciters.forEach { reciter ->
                                        val isSelected = (reciter.first == viewModel.selectedReciterId)
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.VolumeUp,
                                                        contentDescription = null,
                                                        tint = if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = reciter.second,
                                                        color = if (isSelected) GoldPrimary else Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectReciter(surah.id, reciter.first)
                                                isSettingsMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = surah.nameArabic,
                                color = GoldPrimary,
                                fontSize = 20.sp,
                                fontFamily = FontHelper.getFontForScript("uthmani")
                            )
                        }
                    }

                    // Compact Continuous Flow / Standard View Layout Selection Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.SuggestionChip(
                            onClick = { 
                                if (viewModel.isContinuousFlowMode) {
                                    viewModel.isContinuousFlowMode = false
                                }
                            },
                            label = { Text("Standard View", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (!isContinuousFlowMode) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                labelColor = if (!isContinuousFlowMode) GoldPrimary else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (!isContinuousFlowMode) GoldPrimary else GoldPrimary.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                        
                        androidx.compose.material3.SuggestionChip(
                            onClick = { 
                                if (!viewModel.isContinuousFlowMode) {
                                    viewModel.isContinuousFlowMode = true
                                }
                            },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Flow View", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(GoldPrimary, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    ) {
                                        Text("BETA", fontSize = 7.sp, color = EmeraldCard, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isContinuousFlowMode) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                labelColor = if (isContinuousFlowMode) GoldPrimary else Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isContinuousFlowMode) GoldPrimary else GoldPrimary.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            // Beautiful Bismillah Header inside every chapter (except Surah Tawbah, id = 9 and Surah Al-Fatihah, id = 1)
            if (surah.id != 9 && surah.id != 1) {
                item {
                    var isBismillahVisible by remember(surah.id) { mutableStateOf(false) }
                    LaunchedEffect(surah.id) {
                        isBismillahVisible = true
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isBismillahVisible,
                        enter = fadeIn(animationSpec = tween(700)) + slideInVertically(
                            initialOffsetY = { -it / 3 },
                            animationSpec = tween(700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                color = GoldPrimary,
                                fontSize = 24.sp,
                                fontFamily = FontHelper.getFontForScript(viewModel.quranScript),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

                    if (isVersesLoading) {
                        items(5) {
                            VerseSkeletonCard()
                        }
                    } else if (isContinuousFlowMode) {
                        itemsIndexed(activeVerses, key = { _, verse -> "flow_${verse.verse_key}" }) { index, verse ->
                            val isVersePlaying = (activeVerseKey == verse.verse_key)
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .background(
                                        if (isVersePlaying) GoldPrimary.copy(alpha = 0.05f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = verse.verse_key,
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                    
                                    // Play button for flow mode!
                                    val isPlaying = isContinuousPlaying && (activeVerseKey == verse.verse_key)
                                    IconButton(
                                        onClick = {
                                            if (isPlaying) {
                                                isContinuousPlaying = false
                                                viewModel.audioPlayerHelper.stop()
                                            } else {
                                                isContinuousPlaying = true
                                                playVerseAtIndex(index)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Stop recitation" else "Play from here",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    val isBookmarked = bookmarks.any { it.surahNumber == surah.id && it.verseNumber == verse.id }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(
                                                if (isBookmarked) GoldPrimary else GoldPrimary.copy(alpha = 0.15f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isBookmarked) GoldPrimary else GoldPrimary.copy(alpha = 0.3f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .clickable {
                                                viewModel.toggleVerseBookmark(
                                                    surah.id,
                                                    surah.name,
                                                    verse.id,
                                                    verse.verse_key,
                                                    isFlowMode = true
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${verse.id}",
                                            color = if (isBookmarked) EmeraldCard else GoldPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Absolute.Right,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val sortedWords = verse.words?.sortedBy { it.position } ?: emptyList()
                                    val spokenWords = sortedWords.filter { it.char_type_name == "word" }
                                    val spokenCount = spokenWords.size
                                    val currentDurationMs = playbackDurationMs
                                    val verseDurMs = if (currentDurationMs > 0) currentDurationMs else (viewModel.activeVerseDurations[verse.verse_key] ?: 5) * 1000
                                    val rawElapsedInVerse = playbackProgressMs
                                    val elapsedInVerse = rawElapsedInVerse

                                    sortedWords.forEach { word ->
                                        if (word.char_type_name != "word") {
                                            Text(
                                                text = if (viewModel.quranScript == "indopak") (word.text_indopak ?: word.text_uthmani ?: "") else (word.text_uthmani ?: ""),
                                                fontSize = 26.sp,
                                                color = GoldPrimary,
                                                fontFamily = FontHelper.getFontForScript(viewModel.quranScript),
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        } else {
                                            val isWordHighlighted = if (isVersePlaying && isAudioPlaying) {
                                                val segments = viewModel.activeVerseSegments[verse.verse_key]
                                                if (segments != null && segments.isNotEmpty()) {
                                                    val matchingSegment = segments.find { seg ->
                                                        seg.size >= 4 && elapsedInVerse >= seg[2] && elapsedInVerse <= seg[3]
                                                    }
                                                    if (matchingSegment != null) {
                                                        matchingSegment[1] == word.position
                                                    } else {
                                                        val lastSeg = segments.lastOrNull()
                                                        if (lastSeg != null && lastSeg.size >= 4 && elapsedInVerse > lastSeg[3]) {
                                                            lastSeg[1] == word.position
                                                        } else {
                                                            false
                                                        }
                                                    }
                                                } else {
                                                    val spokenWordId = getSpokenWordIdAtTime(
                                                        elapsedMs = elapsedInVerse.toLong(),
                                                        totalDurationMs = verseDurMs.toLong(),
                                                        spokenWords = spokenWords
                                                    )
                                                    spokenWordId == word.id
                                                }
                                            } else {
                                                false
                                            }
                                            
                                            val animatedTextColor by animateColorAsState(
                                                targetValue = if (isWordHighlighted) GoldPrimary else if (isVersePlaying) Color.White else Color.White.copy(alpha = 0.8f),
                                                animationSpec = tween(durationMillis = 200)
                                            )
                                            
                                            val animatedBgColor by animateColorAsState(
                                                targetValue = if (isWordHighlighted) GoldPrimary.copy(alpha = 0.25f) else Color.Transparent,
                                                animationSpec = tween(durationMillis = 200)
                                            )
                                            
                                            val bringIntoViewRequester = remember { BringIntoViewRequester() }
                                            LaunchedEffect(isWordHighlighted) {
                                                if (isWordHighlighted) {
                                                    try {
                                                        bringIntoViewRequester.bringIntoView()
                                                    } catch (e: Exception) {
                                                        // fail-safe
                                                    }
                                                }
                                            }
                                            
                                            Text(
                                                text = if (viewModel.quranScript == "indopak") (word.text_indopak ?: word.text_uthmani ?: "") else (word.text_uthmani ?: ""),
                                                fontSize = 26.sp,
                                                color = animatedTextColor,
                                                fontFamily = FontHelper.getFontForScript(viewModel.quranScript),
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier
                                                    .bringIntoViewRequester(bringIntoViewRequester)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(animatedBgColor)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    .clickable(enabled = true) {
                                                        selectedWordMeaning = if (selectedWordMeaning?.id == word.id) null else word
                                                        if (selectedWordMeaning != null) {
                                                            viewModel.playWordAudio(word, verse.verse_key, surah.id)
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                                
                                if (isVersePlaying) {
                                    val translations = activeTranslations[verse.verse_key] ?: Pair("Loading English translation...", "")
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(EmeraldCard, RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = translations.first,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                        if (translations.second.isNotEmpty()) {
                                            Text(
                                                text = translations.second,
                                                color = TextGray,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(activeVerses, key = { _, verse -> verse.verse_key }) { index, verse ->
                        val isBookmarked = bookmarks.any { it.surahNumber == surah.id && it.verseNumber == verse.id }
                        val translations = activeTranslations[verse.verse_key] ?: Pair("Loading English translation...", "")
                        val isExpanded = expandedAyahKey == verse.verse_key
 
                        val isVersePlaying = (activeVerseKey == verse.verse_key)
                        val activeSpokenWordId: Int? = if (isVersePlaying && isAudioPlaying) {
                            val currentDurationMs = playbackDurationMs
                            val verseDurMs = if (currentDurationMs > 0) currentDurationMs else (viewModel.activeVerseDurations[verse.verse_key] ?: 5) * 1000
                            val rawElapsedInVerse = playbackProgressMs
                            val elapsedInVerse = rawElapsedInVerse
                            val segments = viewModel.activeVerseSegments[verse.verse_key]
                            if (segments != null && segments.isNotEmpty()) {
                                val matchingSegment = segments.find { seg ->
                                    seg.size >= 4 && elapsedInVerse >= seg[2] && elapsedInVerse <= seg[3]
                                }
                                if (matchingSegment != null) {
                                    val wordPosition = matchingSegment[1]
                                    val sortedWords = verse.words?.sortedBy { it.position } ?: emptyList()
                                    sortedWords.find { it.position == wordPosition }?.id
                                } else {
                                    val lastSeg = segments.lastOrNull()
                                    if (lastSeg != null && lastSeg.size >= 4 && elapsedInVerse > lastSeg[3]) {
                                        val wordPosition = lastSeg[1]
                                        val sortedWords = verse.words?.sortedBy { it.position } ?: emptyList()
                                        sortedWords.find { it.position == wordPosition }?.id
                                    } else {
                                        null
                                    }
                                }
                            } else {
                                val sortedWords = verse.words?.sortedBy { it.position } ?: emptyList()
                                val spokenWords = sortedWords.filter { word -> word.char_type_name == "word" }
                                getSpokenWordIdAtTime(
                                    elapsedMs = elapsedInVerse.toLong(),
                                    totalDurationMs = verseDurMs.toLong(),
                                    spokenWords = spokenWords
                                )
                            }
                        } else {
                            null
                        }

                        val cardShape = if (isCompactCardMode) RoundedCornerShape(12.dp) else RoundedCornerShape(24.dp)
                        val cardPadding = if (isCompactCardMode) 10.dp else 20.dp
                        val cardSpacing = if (isCompactCardMode) 10.dp else 16.dp

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ayah_card_${verse.id}"),
                            shape = cardShape,
                            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                            border = BorderStroke(
                                1.dp,
                                if (isBookmarked) GoldPrimary.copy(alpha = 0.3f) else GoldPrimary.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(cardPadding),
                                verticalArrangement = Arrangement.spacedBy(cardSpacing)
                            ) {
                                // Verse control row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = verse.verse_key,
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val verseAudio = viewModel.audioOverrides["verse_${verse.verse_key}"] ?: viewModel.activeVerseAudioUrls[verse.verse_key]
                                        if (verseAudio != null) {
                                            val currentPlayingUrl by viewModel.audioPlayerHelper.currentlyPlayingUrl.collectAsState(initial = null)
                                            val isPlaying = currentPlayingUrl?.toString()?.endsWith(verseAudio) == true || 
                                                 currentPlayingUrl?.toString()?.contains(verseAudio) == true ||
                                                 (verseAudio.substringAfterLast("/").isNotEmpty() && currentPlayingUrl?.toString()?.contains(verseAudio.substringAfterLast("/")) == true)
                                             val isSurahAudioLocked = viewModel.isSurahAudioLocked(surah.id)

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                IconButton(
                                                    onClick = {
                                                        if (isPlaying) {
                                                            viewModel.audioPlayerHelper.stop()
                                                        } else {
                                                            val isSurahAudioLockedState = viewModel.isSurahAudioLocked(surah.id)
                                                        if (isSurahAudioLockedState) {
                                                                val msg = viewModel.getSurahAudioBlockedMessage(surah.id)
                                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                            } else {
                                                                viewModel.audioPlayerHelper.playAudio(verseAudio, playbackToken = "verse_${verse.verse_key}")
                                                                val idx = activeVerses.indexOfFirst { it.verse_key == verse.verse_key }
                                                                if (idx != -1) {
                                                                    val prefetchUrls = mutableListOf<String>()
                                                                    for (i in 1..5) {
                                                                        val nextIdx = idx + i
                                                                        if (nextIdx < activeVerses.size) {
                                                                            val nv = activeVerses[nextIdx]
                                                                            val nvAudio = viewModel.audioOverrides["verse_${nv.verse_key}"] ?: viewModel.activeVerseAudioUrls[nv.verse_key]
                                                                            if (nvAudio != null) {
                                                                                prefetchUrls.add(nvAudio)
                                                                            }
                                                                        }
                                                                    }
                                                                    if (prefetchUrls.isNotEmpty()) {
                                                                        viewModel.audioPlayerHelper.prefetch(prefetchUrls)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSurahAudioLocked) Icons.Default.Lock else if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                        contentDescription = if (isSurahAudioLocked) "Surah Audio Locked" else if (isPlaying) "Stop Verse" else "Play Verse",
                                                        tint = if (isSurahAudioLocked) Color.White.copy(alpha = 0.5f) else GoldPrimary
                                                    )
                                                }
                                                if (viewModel.isAdmin) {
                                                    Text("v_${verse.verse_key}", color = Color.Red.copy(alpha=0.8f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                expandedAyahKey = if (isExpanded) null else verse.verse_key
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Expand translation",
                                                tint = GoldPrimary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.toggleVerseBookmark(
                                                    surah.id,
                                                    surah.name,
                                                    verse.id,
                                                    verse.verse_key
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder
                                                 ,
                                                 contentDescription = "Bookmark",
                                                 tint = GoldPrimary
                                             )
                                         }
                                     }
                                 }

                                 FlowRow(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.Absolute.Right,
                                     verticalArrangement = Arrangement.spacedBy(if (isCompactCardMode) 4.dp else 8.dp)
                                 ) {
                                     val sortedWords = verse.words?.sortedBy { it.position } ?: emptyList()
                                     sortedWords.forEach { word ->
                                         val arabicFontSize = if (isCompactCardMode) 22.sp else 28.sp
                                         if (word.char_type_name != "word") {
                                             Text(
                                                 text = if (viewModel.quranScript == "indopak") (word.text_indopak ?: word.text_uthmani ?: "") else (word.text_uthmani ?: ""),
                                                 fontSize = arabicFontSize,
                                                 color = GoldPrimary,
                                                 fontFamily = FontHelper.getFontForScript(viewModel.quranScript),
                                                 textAlign = TextAlign.Right,
                                                 modifier = Modifier.padding(horizontal = if (isCompactCardMode) 2.dp else 4.dp, vertical = if (isCompactCardMode) 3.dp else 6.dp)
                                             )
                                         } else {
                                             val isCurrentWordSpoken = isVersePlaying && isAudioPlaying && word.id == activeSpokenWordId
                                             val isWordSelected = selectedWordMeaning?.id == word.id
                                             val isWordHighlighted = isCurrentWordSpoken || isWordSelected

                                             val bringIntoViewRequester = remember { BringIntoViewRequester() }
                                             LaunchedEffect(isCurrentWordSpoken) {
                                                 if (isCurrentWordSpoken) {
                                                     try {
                                                         bringIntoViewRequester.bringIntoView()
                                                     } catch (e: Exception) {
                                                         // fail-safe
                                                     }
                                                 }
                                             }

                                             val animatedBgColor by animateColorAsState(
                                                 targetValue = if (isCurrentWordSpoken) {
                                                     GoldPrimary.copy(alpha = 0.35f)
                                                 } else if (isWordSelected) {
                                                     GoldPrimary.copy(alpha = 0.15f)
                                                 } else {
                                                     Color.Transparent
                                                 },
                                                 animationSpec = tween(durationMillis = 200)
                                             )

                                             val animatedTextColor by animateColorAsState(
                                                 targetValue = if (isWordHighlighted) GoldPrimary else Color.White,
                                                 animationSpec = tween(durationMillis = 200)
                                             )

                                             Column(
                                                 horizontalAlignment = Alignment.CenterHorizontally,
                                                 modifier = Modifier
                                                     .bringIntoViewRequester(bringIntoViewRequester)
                                                     .clip(RoundedCornerShape(6.dp))
                                                     .background(animatedBgColor)
                                                     .clickable(enabled = true) {
                                                         selectedWordMeaning = if (selectedWordMeaning?.id == word.id) null else word
                                                         if (selectedWordMeaning != null) {
                                                             viewModel.playWordAudio(word, verse.verse_key, surah.id)
                                                         }
                                                     }
                                                     .padding(horizontal = if (isCompactCardMode) 4.dp else 6.dp, vertical = if (isCompactCardMode) 2.dp else 4.dp)
                                             ) {
                                                 if (viewModel.isAdmin) {
                                                     Text(
                                                         text = "ID: ${word.id}",
                                                         color = Color.Red.copy(alpha = 0.8f),
                                                         fontSize = 8.sp,
                                                         fontWeight = FontWeight.Bold
                                                     )
                                                 }
                                                 Text(
                                                     text = if (viewModel.quranScript == "indopak") (word.text_indopak ?: word.text_uthmani ?: "") else (word.text_uthmani ?: ""),
                                                     fontSize = arabicFontSize,
                                                     color = animatedTextColor,
                                                     fontFamily = FontHelper.getFontForScript(viewModel.quranScript),
                                                     textAlign = TextAlign.Right,
                                                     modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                 )
                                             }
                                         }
                                     }
                                 }

                                // Selected word meanings popup
                                val isVisible = selectedWordMeaning != null && verse.words?.any { it.id == selectedWordMeaning?.id } == true
                                
                                // Cache the word so the animation can gracefully fade out after it's set to null
                                var cachedWord by remember { mutableStateOf<com.example.data.api.QuranWord?>(null) }
                                LaunchedEffect(selectedWordMeaning) {
                                    if (selectedWordMeaning != null && verse.words?.any { it.id == selectedWordMeaning?.id } == true) cachedWord = selectedWordMeaning
                                }
                                
                                AnimatedVisibility(
                                    visible = isVisible
                                ) {
                                    val word = cachedWord
                                    if (word != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = EmeraldMedium.copy(alpha = 0.4f)),
                                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "WORD TRANSLATION",
                                                        color = GoldPrimary,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = word.translation?.text ?: "No meaning found",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    val translit = word.transliteration?.text ?: ""
                                                    if (translit.isNotEmpty()) {
                                                        Text(
                                                            text = translit,
                                                            color = TextGray,
                                                            fontSize = 11.sp,
                                                            fontStyle = FontStyle.Italic
                                                        )
                                                    }
                                                }
                                                Row {
                                                    val currentPlayingUrl by viewModel.audioPlayerHelper.currentlyPlayingUrl.collectAsState(initial = null)
                                                    val token = word.audio_url ?: "${viewModel.activeVerseAudioUrls[verse.verse_key]}#word_${word.id}"
                                                    val isPlaying = currentPlayingUrl == token || currentPlayingUrl == viewModel.audioOverrides["word_${word.id}"]
                                                    val isWordLocked = viewModel.isWordByWordAudioLocked(surah.id)

                                                    if (true) {
                                                        IconButton(onClick = { 
                                                            if (isWordLocked) {
                                                                val msg = viewModel.getWordByWordAudioBlockedMessage(surah.id)
                                                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                                            } else if (isPlaying) {
                                                                viewModel.audioPlayerHelper.stop()
                                                            } else {
                                                                viewModel.playWordAudio(word, verse.verse_key, surah.id)
                                                            }
                                                        }) {
                                                            Icon(
                                                                if (isWordLocked) Icons.Default.Lock else if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                                contentDescription = if (isWordLocked) "Word Audio Locked" else if (isPlaying) "Stop" else "Play Word",
                                                                tint = if (isWordLocked) Color.White.copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.8f)
                                                            )
                                                        }
                                                    }
                                                    IconButton(onClick = { selectedWordMeaning = null }) {
                                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GoldPrimary.copy(alpha = 0.5f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive translation body expansion panel
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "ENGLISH",
                                                color = GoldPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = translations.first,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp
                                            )
                                        }

                                        if (translations.second.isNotEmpty()) {
                                            Column {
                                                Text(
                                                    text = "URDU / اردو",
                                                    color = GoldPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Text(
                                                    text = translations.second,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    lineHeight = 28.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    

                }
            }

            // Sliding Top Header: Appears and slides down on very small scrolls up when the main header is out of viewport
            var isScrollingUp by remember { mutableStateOf(false) }
            var previousIndex by remember { mutableStateOf(listState.firstVisibleItemIndex) }
            var previousOffset by remember { mutableStateOf(listState.firstVisibleItemScrollOffset) }

            LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
                val currentIndex = listState.firstVisibleItemIndex
                val currentOffset = listState.firstVisibleItemScrollOffset
                if (currentIndex < previousIndex || (currentIndex == previousIndex && currentOffset < previousOffset)) {
                    isScrollingUp = true
                } else if (currentIndex > previousIndex || (currentIndex == previousIndex && currentOffset > previousOffset)) {
                    isScrollingUp = false
                }
                previousIndex = currentIndex
                previousOffset = currentOffset
            }

            val isHeaderOut = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100
            val showSlidingHeader = isHeaderOut && isScrollingUp

            AnimatedVisibility(
                visible = showSlidingHeader,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f),
                enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = EmeraldCard.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.audioPlayerHelper.stop()
                                    isContinuousPlaying = false
                                    currentPlayingVerseIndex = null
                                    viewModel.selectedSurah = null
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = surah.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = surah.nameArabic,
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontHelper.getFontForScript("uthmani")
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (isContinuousPlaying) {
                                        isContinuousPlaying = false
                                        viewModel.audioPlayerHelper.stop()
                                    } else {
                                        isContinuousPlaying = true
                                        val resolvedStart = when {
                                            currentPlayingVerseIndex != null && currentPlayingVerseIndex!! in activeVerses.indices -> currentPlayingVerseIndex!!
                                            else -> {
                                                val startIndex = activeVerses.indexOfFirst { verse ->
                                                    val verseAudio = viewModel.audioOverrides["verse_${verse.verse_key}"] ?: viewModel.activeVerseAudioUrls[verse.verse_key]
                                                    verseAudio != null && currentPlayingUrl?.let { url -> url.endsWith(verseAudio) || url.contains(verseAudio) } == true
                                                }
                                                if (startIndex != -1) startIndex else 0
                                            }
                                        }
                                        playVerseAtIndex(resolvedStart)
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isContinuousPlaying) GoldPrimary else EmeraldBackground,
                                    contentColor = if (isContinuousPlaying) EmeraldCard else GoldPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isContinuousPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Stop/Play",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isContinuousPlaying) "Stop" else "Autoplay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Render Chapters Listing screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(EmeraldBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Surah Chapters",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                // Search filter field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("quran_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search Surah names...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary) },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = EmeraldCard,
                        unfocusedContainerColor = EmeraldCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            if (isChaptersLoading) {
                items(6) {
                    SurahListSkeletonCard()
                }
            } else {
                val filtered = if (searchQuery.isEmpty()) {
                    IslamicData.surahs
                } else {
                    IslamicData.surahs.filter {
                        IslamicData.matchesSurah(it, searchQuery)
                    }
                }

                items(filtered, key = { it.id }) { surah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectChapter(surah.id) }
                                .testTag("surah_card_${surah.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(GoldPrimary.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = surah.id.toString(),
                                            color = GoldPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = surah.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${surah.versesCount} Verses • ${surah.revelationType}",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = surah.nameArabic,
                                    color = GoldPrimary,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                    }
                }
            }
        }
    }

// FlowRow implementation for older Compose versions (simple linear grid fallbacks)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
    ) {
        Box(modifier = modifier) {
            // Render word lists inside wrapping grids
            androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
                val placeables = measurables.map { it.measure(constraints) }
                val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
                var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
                var currentRowWidth = 0

                placeables.forEach { placeable ->
                    if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                        rows.add(currentRow)
                        currentRow = mutableListOf()
                        currentRowWidth = 0
                    }
                    currentRow.add(placeable)
                    currentRowWidth += placeable.width
                }
                if (currentRow.isNotEmpty()) rows.add(currentRow)

                val height = rows.sumOf { row -> row.maxOfOrNull { it.height } ?: 0 } + ((rows.size - 1).coerceAtLeast(0) * 12)
                layout(constraints.maxWidth, height) {
                    var y = 0
                    rows.forEach { row ->
                        val rowHeight = row.maxOfOrNull { it.height } ?: 0
                        var x = constraints.maxWidth // Start from right edge
                        row.forEach { placeable ->
                            x -= placeable.width
                            placeable.place(x, y)
                        }
                        y += rowHeight + 12
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. NOBLE HADITH LISTS (HADITHEXPLORER)
// ==========================================
@Composable
fun HadithExplorerScreen(viewModel: com.example.viewmodel.TaqwaViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sharedPrefs = remember { context.getSharedPreferences("taqwa_hadith_prefs", Context.MODE_PRIVATE) }
    var pinnedIds by remember {
        mutableStateOf(sharedPrefs.getStringSet("pinned_hadith_ids", emptySet()) ?: emptySet())
    }

    // Books logic instead of static hadiths
    val supportedBooks = com.example.data.HadithBookService.supportedBooks
    
    // Initial fetch trigger
    LaunchedEffect(viewModel.activeHadithBookKey) {
        val currentName = supportedBooks.find { it.first == viewModel.activeHadithBookKey }?.second ?: "Book"
        viewModel.selectHadithBook(viewModel.activeHadithBookKey, currentName)
    }

    val hadiths = viewModel.activeHadithList
    var searchQuery by remember { mutableStateOf("") }
    var sortedAndFiltered by remember { mutableStateOf<List<com.example.data.HadithBookService.DownloadedHadith>>(emptyList()) }

    LaunchedEffect(viewModel.activeHadithBookKey, searchQuery, hadiths, pinnedIds) {
        if (searchQuery.trim().isNotEmpty()) {
            delay(180)
        }
        val sorted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val trimmedQuery = searchQuery.trim()
            val queryTokens = trimmedQuery.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            
            val filtered = hadiths.filter {
                if (queryTokens.isEmpty()) return@filter true
                
                queryTokens.all { token ->
                    it.english.contains(token, ignoreCase = true) || 
                    it.arabic.contains(token) || 
                    it.chapter.contains(token, ignoreCase = true)
                }
            }
            
            val pinnedList = mutableListOf<com.example.data.HadithBookService.DownloadedHadith>()
            val unpinnedList = mutableListOf<com.example.data.HadithBookService.DownloadedHadith>()
            for (item in filtered) {
                if (pinnedIds.contains(item.hadithNumber.toString())) {
                    pinnedList.add(item)
                } else {
                    unpinnedList.add(item)
                }
            }
            
            pinnedList.sortedBy { it.hadithNumber } + unpinnedList.sortedBy { it.hadithNumber }
        }
        sortedAndFiltered = sorted
    }

    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Text Title (scrollable)
        item {
            Text(
                text = "Noble Hadith",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // 2. Search input (scrollable - outside stickyHeader)
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .testTag("hadith_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search Hadith...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = GoldPrimary.copy(alpha = 0.5f))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = EmeraldCard,
                        unfocusedContainerColor = EmeraldCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }

        // 3. Books Category Header
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldBackground)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().testTag("hadith_categories_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(supportedBooks) { (bookKey, bookName) ->
                        val isSelected = viewModel.activeHadithBookKey == bookKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldPrimary else EmeraldCard)
                                .border(1.dp, if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectHadithBook(bookKey, bookName) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("hadith_category_$bookKey")
                        ) {
                            Text(
                                text = bookName,
                                color = if (isSelected) OnGoldText else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Content Area
        if (viewModel.isHadithBookDownloading || viewModel.isHadithBookLoading) {
            items(5) {
                HadithSkeletonCard()
            }
        } else if (viewModel.hadithError != null) {
            item {
                OfflineErrorCard(
                    title = "Unable to Download Hadith Collection",
                    message = viewModel.hadithError ?: "Please check your internet connection to download this Hadith book.",
                    onRetry = {
                        val currentName = supportedBooks.find { it.first == viewModel.activeHadithBookKey }?.second ?: "Book"
                        viewModel.downloadHadithBook(viewModel.activeHadithBookKey, currentName)
                    }
                )
            }
        } else if (!viewModel.activeHadithBookDownloaded) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).testTag("download_book_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Download Offline Dataset",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "To strictly minimize app size while ensuring incredibly fast loading, this authentic collection is downloaded securely once on-demand. After that, it functions entirely offline.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val currentName = supportedBooks.find { it.first == viewModel.activeHadithBookKey }?.second ?: "Book"
                                viewModel.downloadHadithBook(viewModel.activeHadithBookKey, currentName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGoldText),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (sortedAndFiltered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching Hadiths found.", color = TextGray)
                }
            }
        } else {
            items(sortedAndFiltered, key = { it.hadithNumber }) { hadith ->
                LaunchedEffect(hadith.hadithNumber) {
                    kotlinx.coroutines.delay(4000)
                    viewModel.incrementHadithRead()
                }
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("hadith_card_${hadith.hadithNumber}"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "CHAPTER: ${hadith.chapter.uppercase(java.util.Locale.ROOT)}",
                                    color = GoldPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (pinnedIds.contains(hadith.hadithNumber.toString())) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GoldPrimary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .testTag("pinned_badge_${hadith.hadithNumber}")
                                    ) {
                                        Text(
                                            text = "PINNED",
                                            color = GoldPrimary,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${hadith.source} #${hadith.hadithNumber}",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val isPinned = pinnedIds.contains(hadith.hadithNumber.toString())
                                IconButton(
                                    onClick = {
                                        val nextPinned = if (isPinned) {
                                            pinnedIds - hadith.hadithNumber.toString()
                                        } else {
                                            pinnedIds + hadith.hadithNumber.toString()
                                        }
                                        pinnedIds = nextPinned
                                        sharedPrefs.edit().putStringSet("pinned_hadith_ids", nextPinned).apply()
                                    },
                                    modifier = Modifier.size(24.dp).testTag("pin_hadith_${hadith.hadithNumber}")
                                ) {
                                    Icon(
                                        imageVector = if (isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = if (isPinned) "Unpin" else "Pin",
                                        tint = if (isPinned) GoldPrimary else TextGray.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = hadith.arabic,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 32.sp
                        )

                        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.05f))

                        if (hadith.narrator != "Unknown" && hadith.narrator.isNotEmpty()) {
                            Text(
                                text = "Narrated ${hadith.narrator}:",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Text(
                            text = "\"${hadith.english}\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontStyle = FontStyle.Italic
                        )

                        if (hadith.urdu.isNotEmpty()) {
                            HorizontalDivider(color = GoldPrimary.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = hadith.urdu,
                                color = Color.White,
                                fontSize = 16.sp,
                                lineHeight = 28.sp,
                                fontFamily = FontHelper.getFontForScript("indopak"),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SUPPLICATIONS COMPILATIONS (DUALIBRARY)
// ==========================================
@Composable
fun DuaLibraryScreen(viewModel: TaqwaViewModel) {
    val focusManager = LocalFocusManager.current
    val duas = viewModel.dynamicDuaList
    val categories = remember(duas) {
        val customCats = duas.map { it.category }.distinct().filter { 
            it.isNotEmpty() && 
            !it.contains("Rabbana", ignoreCase = true) && 
            !it.contains("Quranic", ignoreCase = true) && 
            it != "Quran" && it != "Hadith" && it != "All" 
        }.sorted()
        listOf("All", "Rabbana Duas", "Quran Supplications", "Hisn al-Muslim (Hadith)") + customCats
    }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf(viewModel.duaSearchQuery) }
    LaunchedEffect(viewModel.duaSearchQuery) {
        searchQuery = viewModel.duaSearchQuery
    }

    var sortedAndFiltered by remember { mutableStateOf<List<com.example.data.Dua>>(emptyList()) }
    var categoryCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(selectedCategory, searchQuery, duas) {
        if (searchQuery.trim().isNotEmpty()) {
            kotlinx.coroutines.delay(180)
        }
        val (filteredList, counts) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val filtered = duas.filter {
                val matchesCategory = when (selectedCategory) {
                    "All" -> true
                    "Rabbana Duas" -> it.id.startsWith("quran_r") || it.category.contains("Rabbana", ignoreCase = true) || it.arabic.contains("رَبَّنَا") || it.arabic.contains("ربنا")
                    "Quran Supplications", "Quran" -> it.id.startsWith("quran_") || it.category.contains("Quran", ignoreCase = true) || it.reference.lowercase().contains("quran") || it.reference.lowercase().contains("surah")
                    "Hisn al-Muslim (Hadith)", "Hisn al-Muslim", "Hadith" -> it.id.startsWith("hisn_") || (!it.id.startsWith("quran_") && !it.category.contains("Quran", ignoreCase = true) && !it.reference.lowercase().contains("surah"))
                    else -> it.category.equals(selectedCategory, ignoreCase = true) || it.category.contains(selectedCategory, ignoreCase = true)
                }
                matchesCategory && (
                    it.translation.contains(searchQuery, ignoreCase = true) || 
                    it.arabic.contains(searchQuery) || 
                    it.transliteration.contains(searchQuery, ignoreCase = true) ||
                    it.translationUrdu.contains(searchQuery) ||
                    it.reference.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
                )
            }
            
            val countsMap = categories.associateWith { cat ->
                when (cat) {
                    "All" -> duas.size
                    "Rabbana Duas" -> duas.count { it.id.startsWith("quran_r") || it.category.contains("Rabbana", ignoreCase = true) || it.arabic.contains("رَبَّنَا") || it.arabic.contains("ربنا") }
                    "Quran Supplications", "Quran" -> duas.count { it.id.startsWith("quran_") || it.category.contains("Quran", ignoreCase = true) || it.reference.lowercase().contains("surah") }
                    "Hisn al-Muslim (Hadith)", "Hisn al-Muslim", "Hadith" -> duas.count { it.id.startsWith("hisn_") || (!it.id.startsWith("quran_") && !it.category.contains("Quran", ignoreCase = true) && !it.reference.lowercase().contains("surah")) }
                    else -> duas.count { it.category.equals(cat, ignoreCase = true) || it.category.contains(cat, ignoreCase = true) }
                }
            }
            Pair(filtered, countsMap)
        }
        sortedAndFiltered = filteredList
        categoryCounts = counts
    }

    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Text Title (scrollable)
        item {
            Text(
                text = "Islamic Supplications",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        // 2. Search Box (scrollable - outside stickyHeader to prevent focus crashes)
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .testTag("dua_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search supplications...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = GoldPrimary.copy(alpha = 0.5f))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = EmeraldCard,
                        unfocusedContainerColor = EmeraldCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }

        // 3. STICKY HEADER containing ONLY Categories LazyRow
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldBackground)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filters horizontal row with dynamic precalculated counts
                LazyRow(
                    modifier = Modifier.fillMaxWidth().testTag("dua_categories_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(categories) { cat ->
                        val count = categoryCounts[cat] ?: 0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedCategory == cat) GoldPrimary else EmeraldCard)
                                .border(1.dp, if (selectedCategory == cat) GoldPrimary else GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("dua_category_$cat")
                        ) {
                            Text(
                                text = "$cat ($count)",
                                color = if (selectedCategory == cat) OnGoldText else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. Dua Items List
        if (viewModel.isDuasLoading) {
            items(5) {
                DuaSkeletonCard()
            }
        } else if (sortedAndFiltered.isEmpty()) {
            item {
                if (viewModel.duasError != null) {
                    OfflineErrorCard(
                        title = "Unable to Load Supplications",
                        message = viewModel.duasError ?: "Please check your network connection and try again.",
                        onRetry = { viewModel.loadDuas() }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No supplications found.", color = TextGray)
                    }
                }
            }
        } else {
            items(sortedAndFiltered, key = { it.id }) { dua ->
                LaunchedEffect(dua.id) {
                    kotlinx.coroutines.delay(4000)
                    viewModel.incrementDuaRead()
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATEGORY: ${dua.category.uppercase()}",
                                color = GoldPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = dua.reference,
                                color = TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = dua.arabic,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = FontHelper.getFontForScript("uthmani"),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 32.sp
                        )

                        Divider(color = GoldPrimary.copy(alpha = 0.05f))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "TRANSLITERATION",
                                color = GoldPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = dua.transliteration,
                                color = TextGray,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "TRANSLATION",
                                color = GoldPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = dua.translation,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                        if (dua.translationUrdu.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "URDU TRANSLATION",
                                    color = GoldPrimary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = dua.translationUrdu,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    fontFamily = FontHelper.getFontForScript("indopak"),
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. TASBEEH DECATIVE COUNTER (TASBEEHCOUNTER)
// ==========================================
@Composable
fun TasbeehCounterScreen(viewModel: TaqwaViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { com.example.util.SecurePreferences.getSecurePrefs(context) }
    var isVibrationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("tasbeeh_vibration_enabled", true)) }
    
    // Daily Dhikr Tasks
    val dhikrTasks = remember {
        mutableStateListOf(
            TasbeehTask(0, "Free Tasbeeh", "سبحة حرة", 0),
            TasbeehTask(1, "SubhanAllah", "سُبْحَانَ الله", 33),
            TasbeehTask(2, "Alhamdulillah", "الْحَمْدُ لله", 33),
            TasbeehTask(3, "Allahu Akbar", "اللهُ أَكْبَر", 33),
            TasbeehTask(4, "Astaghfirullah", "أَسْتَغْفِرُ الله", 100)
        )
    }
    
    // Use an index or similar to track selection to trigger recomposition properly
    val selectedTaskId = viewModel.selectedTasbeehId
    val selectedTask = dhikrTasks.firstOrNull { it.id == selectedTaskId } ?: dhikrTasks.first()

    // Pulsing click animation using Animatable for flawless rapid clicking
    val coroutineScope = rememberCoroutineScope()
    val pulseScale = remember { androidx.compose.animation.core.Animatable(1f) }
    val performVibration = {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tasbeeh",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedTask.target == 0) "MODE: FREE" else "TARGET: ${selectedTask.target}",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Vibration Toggle Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(EmeraldMedium.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isVibrationEnabled) Icons.Default.Vibration else Icons.Default.FilterCenterFocus,
                    contentDescription = "Vibration Mode",
                    tint = if (isVibrationEnabled) GoldPrimary else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                androidx.compose.material3.Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = { 
                        isVibrationEnabled = it
                        sharedPrefs.edit().putBoolean("tasbeeh_vibration_enabled", it).apply()
                    },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = EmeraldBackground,
                        checkedTrackColor = GoldPrimary,
                        uncheckedThumbColor = EmeraldCard,
                        uncheckedTrackColor = EmeraldMedium,
                        uncheckedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.graphicsLayer { scaleX = 0.8f; scaleY = 0.8f } 
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Circular Tapper Widget
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    scaleX = pulseScale.value
                    scaleY = pulseScale.value
                }
                .clip(CircleShape)
                .background(EmeraldCard.copy(alpha = 0.3f))
                .border(3.dp, GoldPrimary, CircleShape)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null // Disable standard ripple for custom pulse
                ) {
                    coroutineScope.launch {
                        pulseScale.snapTo(1.08f)
                        pulseScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f))
                    }
                    
                    if (selectedTask.target == 0 || selectedTask.current.value < selectedTask.target) {
                        selectedTask.current.value++
                    }
                    viewModel.incrementTasbeeh()

                    // Safe cross-platform Haptic feedback
                    if (isVibrationEnabled) {
                        performVibration()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Draw progress circle ring if not free
            if (selectedTask.target > 0) {
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val progress = selectedTask.current.value.toFloat() / selectedTask.target
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.1f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawArc(
                        color = GoldPrimary,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedTask.name,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = selectedTask.current.value.toString(),
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 56.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Tasks List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dhikrTasks) { task ->
                val isSelected = selectedTaskId == task.id
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).clickable { viewModel.selectedTasbeehId = task.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) EmeraldMedium.copy(alpha = 0.6f) else EmeraldCard.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(task.arabicName, color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (task.target == 0) "∞" else "${task.target}", 
                            color = GoldPrimary, 
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { 
                selectedTask.current.value = 0 
                viewModel.resetTasbeeh() 
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp).height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.8f))
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("RESET TASBEEH", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

data class TasbeehTask(
    val id: Int,
    val name: String,
    val arabicName: String,
    val target: Int,
    val current: MutableState<Int> = mutableStateOf(0)
)

// ==========================================
// 7. QIBLA COMPASS BEARING (QIBLAFINDER)
// ==========================================
@Composable
fun QiblaFinderScreen(viewModel: TaqwaViewModel) {
    if (!viewModel.hasLocationPermission) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LocationPermissionCard(viewModel = viewModel, onPermissionGranted = {})
        }
        return
    }

    val context = LocalContext.current
    var azimuthHeading by remember { mutableStateOf(0f) }

    // Geolocation values
    val meccaLat = 21.4225
    val meccaLng = 39.8262

    // Continuous Tilt-Compensated Sensor Implementation
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                try {
                    if (event != null && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        
                        // Remap coordinate system based on screen rotation context
                        val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            try { context.display } catch (e: Throwable) { null }
                        } else {
                            @Suppress("DEPRECATION")
                            (context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager)?.defaultDisplay
                        }
                        val rotation = display?.rotation ?: android.view.Surface.ROTATION_0
                        
                        var axisX = SensorManager.AXIS_X
                        var axisY = SensorManager.AXIS_Y
                        when (rotation) {
                            android.view.Surface.ROTATION_90 -> {
                                axisX = SensorManager.AXIS_Y
                                axisY = SensorManager.AXIS_MINUS_X
                            }
                            android.view.Surface.ROTATION_180 -> {
                                axisX = SensorManager.AXIS_MINUS_X
                                axisY = SensorManager.AXIS_MINUS_Y
                            }
                            android.view.Surface.ROTATION_270 -> {
                                axisX = SensorManager.AXIS_MINUS_Y
                                axisY = SensorManager.AXIS_X
                            }
                            else -> {
                                axisX = SensorManager.AXIS_X
                                axisY = SensorManager.AXIS_Y
                            }
                        }
                        
                        val remappedMatrix = FloatArray(9)
                        SensorManager.remapCoordinateSystem(rotMatrix, axisX, axisY, remappedMatrix)
                        
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(remappedMatrix, orientation)
                        val pitch = orientation[1] // Roll-Pitch-Yaw
                        
                        // Continuous tilt calculation projection
                        val cosPitch = cos(pitch)
                        val sinPitch = sin(pitch)
                        
                        // Project device's current pointing vector onto horizontal plane
                        val xWorld = remappedMatrix[1] * cosPitch + remappedMatrix[2] * sinPitch
                        val yWorld = remappedMatrix[4] * cosPitch + remappedMatrix[5] * sinPitch
                        
                        val azimuth = Math.toDegrees(atan2(xWorld, yWorld).toDouble()).toFloat()
                        val currentAzimuth = (azimuth + 360f) % 360f
                        
                        // Low Pass Filter to smooth out needle glitching
                        val ALPHA = 0.10f
                        var filteredAzimuth = azimuthHeading
                        if (filteredAzimuth < 0) {
                            filteredAzimuth = currentAzimuth
                        } else {
                            // Find shortest path to avoid spinning
                            var diff = currentAzimuth - filteredAzimuth
                            if (diff < -180f) diff += 360f
                            else if (diff > 180f) diff -= 360f
                            filteredAzimuth += ALPHA * diff
                        }
                        azimuthHeading = (filteredAzimuth + 360f) % 360f
                    }
                } catch (e: Throwable) {
                    azimuthHeading = -1f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && rotSensor != null) {
            sensorManager.registerListener(sensorListener, rotSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Simulator sliding fallback check
            azimuthHeading = -1f
        }
        
        onDispose {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    val finalHeading = if (azimuthHeading < 0) {
        viewModel.manualQiblaSliderHeading.toFloat()
    } else {
        azimuthHeading
    }

    // Mathematical Kaaba bearing formula:
    // Qibla = atan2(sin(Δλ), cos(φ1)tan(φ2) - sin(φ1)cos(Δλ))
    val lat1Rad = Math.toRadians(viewModel.userLatitude)
    val lng1Rad = Math.toRadians(viewModel.userLongitude)
    val lat2Rad = Math.toRadians(meccaLat)
    val lng2Rad = Math.toRadians(meccaLng)
    val dLon = lng2Rad - lng1Rad

    val y = sin(dLon) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
    var qiblaAngle = Math.toDegrees(atan2(y, x))
    qiblaAngle = (qiblaAngle + 360.0) % 360.0

    val relativeAngle = (qiblaAngle - finalHeading + 360.0) % 360.0
    val isAligned = relativeAngle < 4.0 || relativeAngle > 356.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Qibla Finder",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isAligned) "🕋 PERFECTLY ALIGNED" else "ROTATE DEVICE TO KAABA",
                color = if (isAligned) AlertGreen else GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Compass Canvas Drawing
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    // Draw compass circle
                    drawCircle(
                        color = if (isAligned) AlertGreen.copy(alpha = 0.15f) else GoldSecondary.copy(alpha = 0.05f),
                        radius = size.minDimension / 2.05f
                    )
                    drawCircle(
                        color = if (isAligned) AlertGreen else GoldSecondary,
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 3.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Compass background dials
            val compassAnimateRotation by animateFloatAsState(targetValue = -finalHeading)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = compassAnimateRotation),
                contentAlignment = Alignment.Center
            ) {
                // North Dial
                Box(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("N", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Dynamic Qibla needle pointer
                val qiblaAnimateRotation by animateFloatAsState(targetValue = qiblaAngle.toFloat())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(rotationZ = qiblaAnimateRotation),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Canvas(
                            modifier = Modifier
                                .size(40.dp)
                                .graphicsLayer(shadowElevation = 4f)
                        ) {
                            val path = Path().apply {
                                // Draw a beautiful royal golden spearhead/arrow
                                moveTo(size.width / 2f, 0f) // Point of the arrow
                                lineTo(size.width * 0.8f, size.height * 0.85f) // Right outer tail
                                lineTo(size.width / 2f, size.height * 0.65f) // Inner notch
                                lineTo(size.width * 0.2f, size.height * 0.85f) // Left outer tail
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFDF00), // Bright Royal gold
                                        GoldPrimary // Deep Gold Accent
                                    )
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(GoldPrimary, Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            }

            // Center static pin
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isAligned) AlertGreen else GoldPrimary)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sensors fallback sliding controller
        if (azimuthHeading < 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MANUAL COMPASS SIMULATOR",
                        color = GoldPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Rotating compass vector manually (slider controls degrees):",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = viewModel.manualQiblaSliderHeading.toFloat(),
                        onValueChange = { viewModel.manualQiblaSliderHeading = it.toDouble() },
                        valueRange = 0f..359f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = GoldPrimary.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        text = "Device Heading: ${viewModel.manualQiblaSliderHeading.toInt()}° • Kaaba: ${qiblaAngle.toInt()}°",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        } else {
            // General indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("HEADING", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${finalHeading.toInt()}°",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("KAABA BEARING", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${qiblaAngle.toInt()}°",
                            color = GoldPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. 99 NAMES OF ALLAH (NAMESOFALLAH)
// ==========================================
@Composable
fun NamesOfAllahScreen(viewModel: TaqwaViewModel) {
    val focusManager = LocalFocusManager.current
    var isProphetNamesSelected by remember { mutableStateOf(false) }
    val names = if (isProphetNamesSelected) IslamicData.namesOfProphet else IslamicData.namesOfAllah
    var searchQuery by remember { mutableStateOf(viewModel.namesSearchQuery) }
    LaunchedEffect(viewModel.namesSearchQuery) {
        searchQuery = viewModel.namesSearchQuery
    }

    val filtered = names.filter {
        it.englishName.contains(searchQuery, ignoreCase = true) ||
        it.meaning.contains(searchQuery, ignoreCase = true) ||
        it.meaningUrdu.contains(searchQuery, ignoreCase = true) ||
        it.name.contains(searchQuery)
    }

    val cardContent: @Composable (com.example.data.NameOfAllah) -> Unit = { name ->
        LaunchedEffect(name.id) {
            kotlinx.coroutines.delay(2000)
            viewModel.incrementNameRead()
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name.id.toString(),
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(GoldPrimary.copy(alpha = 0.05f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = name.name,
                    color = GoldPrimary,
                    fontSize = 28.sp,
                    fontFamily = FontHelper.getFontForScript("uthmani"),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = name.englishName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = name.meaning,
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (name.meaningUrdu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name.meaningUrdu,
                        color = GoldPrimary.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontFamily = FontHelper.getFontForScript("indopak"),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    val pairs = filtered.chunked(2)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (isProphetNamesSelected) "99 Names of Hazrat Muhammad (ﷺ)" else "99 Names of Allah",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Smooth elegant toggle switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldCard, RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isProphetNamesSelected) GoldPrimary else Color.Transparent)
                            .clickable { isProphetNamesSelected = false }
                            .padding(vertical = 12.dp)
                            .testTag("toggle_allah_names"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Names of Allah",
                            color = if (!isProphetNamesSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isProphetNamesSelected) GoldPrimary else Color.Transparent)
                            .clickable { isProphetNamesSelected = true }
                            .padding(vertical = 12.dp)
                            .testTag("toggle_prophet_names"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Names of Prophet",
                            color = if (isProphetNamesSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("names_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search names, English or Urdu meanings...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary) },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = EmeraldCard,
                        unfocusedContainerColor = EmeraldCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
        
        items(pairs) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (pair.size == 2) {
                    // Second item (higher index) on the Left
                    Box(modifier = Modifier.weight(1f)) {
                        cardContent(pair[1])
                    }
                    // First item (lower index) on the Right
                    Box(modifier = Modifier.weight(1f)) {
                        cardContent(pair[0])
                    }
                } else {
                    // Singular odd item: spacer on left, item on right
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.weight(1f)) {
                        cardContent(pair[0])
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. PURIFYING ZAKAT SYSTEM (ZAKATCALCULATOR)
// ==========================================
@Composable
fun ZakatCalculatorScreen(viewModel: TaqwaViewModel) {
    val results = viewModel.calculateZakat()
    val isSilver = viewModel.zakatMetalBasis == "silver"
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMetalDetails by remember { mutableStateOf(false) }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Zakat Calculator",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GoldPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Zakat Info",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Understanding Zakat",
                                color = GoldPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.1f))

                        // What & Why
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("What & Why it is given?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Zakat is the 3rd pillar of Islam, an obligatory charity on wealth that reaches the Nisab (minimum threshold) and is held for one lunar year. It purifies wealth and helps those in need.",
                                color = TextGray, fontSize = 13.sp, lineHeight = 20.sp
                            )
                        }

                        // How it is calculated
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("How is it calculated?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "It is calculated as 2.5% of your total qualifying assets (cash, gold, silver, investments, trade goods) minus your immediate liabilities.",
                                color = TextGray, fontSize = 13.sp, lineHeight = 20.sp
                            )
                        }

                        // Hadith
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldMedium.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "\"Whoever pays the Zakat on his wealth, its evil is removed from him.\"",
                                    color = Color.White, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                )
                                Text(
                                    "— Al-Tabarani",
                                    color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Basis metals toggle Selection
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SELECT METALS THRESHOLD",
                            color = GoldPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    if (viewModel.zakatMetalBasis == "silver") {
                                        viewModel.zakatMetalBasis = ""
                                        showMetalDetails = false
                                    } else {
                                        viewModel.zakatMetalBasis = "silver"
                                        showMetalDetails = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(200)
                                            listState.animateScrollToItem(1) // Scroll to toggle area
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.zakatMetalBasis == "silver") GoldPrimary else EmeraldMedium.copy(alpha = 0.3f)
                                )
                            ) {
                                Text("Silver Base", color = if (viewModel.zakatMetalBasis == "silver") OnGoldText else Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { 
                                    if (viewModel.zakatMetalBasis == "gold") {
                                        viewModel.zakatMetalBasis = ""
                                        showMetalDetails = false
                                    } else {
                                        viewModel.zakatMetalBasis = "gold"
                                        showMetalDetails = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(200)
                                            listState.animateScrollToItem(1) // Scroll to toggle area
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.zakatMetalBasis == "gold") GoldPrimary else EmeraldMedium.copy(alpha = 0.3f)
                                )
                            ) {
                                Text("Gold Base", color = if (viewModel.zakatMetalBasis == "gold") OnGoldText else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Currency Selector dropdown chips row
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(viewModel.currencyOptions) { idx, item ->
                                val isSelected = viewModel.zakatCurrencyCode == item.first
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else EmeraldMedium.copy(alpha = 0.1f))
                                        .clickable { viewModel.selectZakatCurrency(idx) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = item.first,
                                        color = if (isSelected) OnGoldText else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Animated details Card based on Basis metals toggle
                androidx.compose.animation.AnimatedVisibility(
                    visible = showMetalDetails && viewModel.zakatMetalBasis.isNotEmpty(),
                    enter = androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(500)) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Details",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (viewModel.zakatMetalBasis == "silver") "Silver-Based Nisab (52.5 Tolas)" else "Gold-Based Nisab (7.5 Tolas)",
                                    color = GoldPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = if (viewModel.zakatMetalBasis == "silver")
                                    "Choosing the silver Nisab is more beneficial for the poor, as its lower threshold means more people become eligible to pay Zakat, purifying the wealth of more believers."
                                else
                                    "The gold Nisab is higher. If your total wealth is less than the gold threshold but exceeds the silver threshold, scholars recommend using the silver standard to benefit those in need.",
                                color = TextGray, fontSize = 13.sp, lineHeight = 20.sp
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EmeraldMedium.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        if (viewModel.zakatMetalBasis == "silver")
                                            "\"There is no Zakat on silver until it reaches five Uqiyah (approx 52.5 Tolas)...\""
                                        else
                                            "\"There is no Zakat on you for gold until it reaches twenty Dinars (approx 7.5 Tolas)...\"",
                                        color = Color.White, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontWeight = FontWeight.Medium,
                                        lineHeight = 20.sp
                                    )
                                    Text(
                                        if (isSilver) "— Sahih Al-Bukhari" else "— Abu Dawud",
                                        color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Numerical input forms
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = viewModel.zakatMetalBasis.isNotEmpty(),
                enter = androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(500)) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ADD YOUR WEALTH DETAILS",
                            color = GoldPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ZakatInput(
                                    label = "Gold Price/Tola",
                                    value = viewModel.goldTolaRateInput,
                                    currency = viewModel.zakatCurrencySymbol,
                                    onValueChange = { viewModel.goldTolaRateInput = it }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ZakatInput(
                                    label = "Silver Price/Tola",
                                    value = viewModel.silverTolaRateInput,
                                    currency = viewModel.zakatCurrencySymbol,
                                    onValueChange = { viewModel.silverTolaRateInput = it }
                                )
                            }
                        }

                        androidx.compose.material3.HorizontalDivider(color = GoldPrimary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                        ZakatInput(
                            label = "Cash & Bank Balances",
                            value = viewModel.cashInput,
                            currency = viewModel.zakatCurrencySymbol,
                            onValueChange = { viewModel.cashInput = it }
                        )

                        val goldHelper = viewModel.goldInput.toDoubleOrNull()?.let {
                            "≈ ${viewModel.zakatCurrencySymbol} ${String.format(java.util.Locale.US, "%,.0f", it * viewModel.currentGoldTolaRate())}"
                        }
                        val silverHelper = viewModel.silverInput.toDoubleOrNull()?.let {
                            "≈ ${viewModel.zakatCurrencySymbol} ${String.format(java.util.Locale.US, "%,.0f", it * viewModel.currentSilverTolaRate())}"
                        }

                        if (viewModel.zakatMetalBasis == "silver") {
                            ZakatInput(
                                label = "Silver Holdings (in Tolas)",
                                value = viewModel.silverInput,
                                currency = "",
                                onValueChange = { viewModel.silverInput = it },
                                helperText = silverHelper
                            )
                            ZakatInput(
                                label = "Gold Holdings (in Tolas)",
                                value = viewModel.goldInput,
                                currency = "",
                                onValueChange = { viewModel.goldInput = it },
                                helperText = goldHelper
                            )
                        } else {
                            ZakatInput(
                                label = "Gold Holdings (in Tolas)",
                                value = viewModel.goldInput,
                                currency = "",
                                onValueChange = { viewModel.goldInput = it },
                                helperText = goldHelper
                            )
                            ZakatInput(
                                label = "Silver Holdings (in Tolas)",
                                value = viewModel.silverInput,
                                currency = "",
                                onValueChange = { viewModel.silverInput = it },
                                helperText = silverHelper
                            )
                        }

                        ZakatInput(
                            label = "Passive Assets & Stocks",
                            value = viewModel.investmentsInput,
                            currency = viewModel.zakatCurrencySymbol,
                            onValueChange = { viewModel.investmentsInput = it }
                        )

                        ZakatInput(
                            label = "Debts & Expenses (Subtract)",
                            value = viewModel.debtsInput,
                            currency = viewModel.zakatCurrencySymbol,
                            onValueChange = { viewModel.debtsInput = it }
                        )
                    }
                }
            }
        }

        // Calculated results panel
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = viewModel.zakatMetalBasis.isNotEmpty(),
                enter = androidx.compose.animation.expandVertically(animationSpec = androidx.compose.animation.core.tween(500)) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldMedium.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ZAKAT PAYABLE (2.5%)",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "${viewModel.zakatCurrencySymbol}${String.format(Locale.US, "%,.2f", results.zakatPayable)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (results.exceedsNisab) GoldPrimary.copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (results.exceedsNisab) GoldPrimary else GoldPrimary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (results.exceedsNisab) "🌟 ZAKAT IS MANDATORY (FARZ)" else "BELOW THE NISAB THRESHOLD",
                            color = if (results.exceedsNisab) GoldPrimary else TextGray,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResultRow("Total Sum Assets", results.totalAssets, viewModel.zakatCurrencySymbol)
                        ResultRow("Calculated Net Worth", results.netWorth, viewModel.zakatCurrencySymbol)
                        ResultRow("Chosen Nisab Limit (${if (isSilver) "612.36g Silver" else "87.48g Gold"})", results.nisabLimit, viewModel.zakatCurrencySymbol)
                    }

                    // Progress to Nisab Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progress to Nisab", color = TextGray, fontSize = 11.sp)
                            Text("${results.progressPercent}%", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { results.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = GoldPrimary,
                            trackColor = GoldPrimary.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun ZakatInput(
    label: String,
    value: String,
    currency: String,
    onValueChange: (String) -> Unit,
    helperText: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (helperText != null) {
                Text(helperText, color = GoldPrimary, fontSize = 11.sp)
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("0.00", color = TextGray) },
            prefix = { if (currency.isNotEmpty()) Text(currency, color = GoldPrimary, fontWeight = FontWeight.Black) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = EmeraldMedium.copy(alpha = 0.2f),
                unfocusedContainerColor = EmeraldMedium.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun ResultRow(label: String, amount: Double, symbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray, fontSize = 12.sp)
        Text(
            text = "$symbol${String.format(Locale.US, "%,.2f", amount)}",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ==========================================
// 10. DAILY REMINDERS (TASKS)
// ==========================================
@Composable
fun TaskTrackerScreen(
    viewModel: TaqwaViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val allTimeTasks by viewModel.allTimeTasks.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    val completedCount = tasks.filter { it.completed }.size
    val totalCount = tasks.size

    val completedOnly = remember(allTimeTasks) {
        allTimeTasks.filter { it.completedAt != "MISSED" }
    }

    val groupedHistory = remember(completedOnly) {
        completedOnly.groupBy { it.date }
    }

    var selectedTab by remember { mutableStateOf("checklist") } // "checklist", "streak", "leaderboard", "history"
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog form state
    var newTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Salah") }
    val categoriesList = listOf("Salah", "Quran", "Hadith", "Duas", "Dhikr", "Other")

    // Active countdown timer states
    var activeTimerTaskId by remember { mutableStateOf<String?>(null) }
    var timeLeftSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(activeTimerTaskId, timeLeftSeconds) {
        if (activeTimerTaskId != null && timeLeftSeconds > 0) {
            delay(1000L)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) {
                val completedTask = tasks.find { it.id == activeTimerTaskId }
                if (completedTask != null) {
                    viewModel.toggleMainTask(completedTask.id, true)
                    val msg = if (completedTask.points > 0) {
                        "🎉 Challenge Completed: ${completedTask.title}! Earned ${completedTask.points} XP!"
                    } else {
                        "🎉 Challenge Completed: ${completedTask.title}!"
                    }
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
                activeTimerTaskId = null
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Spiritual Journey",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Your daily habits, streak, and competition",
                    color = TextGray,
                    fontSize = 12.sp,
                )
            }
            
            // Add custom task button
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .background(GoldPrimary, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Custom Deed",
                    tint = OnGoldText
                )
            }
        }

        // ACTIVE TIMER OVERLAY IF RUNNING
        if (activeTimerTaskId != null) {
            val activeTask = tasks.find { it.id == activeTimerTaskId }
            if (activeTask != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Active Timer",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "ACTIVE CHALLENGE",
                                    color = GoldPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = activeTask.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = String.format("%02d:%02d", timeLeftSeconds / 60, timeLeftSeconds % 60),
                            color = GoldPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // STATS SUMMARY HERO CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFACC15).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "CONSISTENCY STREAK",
                                color = Color(0xFFFACC15),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "${stats.currentStreak} DAY${if (stats.currentStreak == 1) "" else "S"} STREAK",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (stats.streakChancesLeft) {
                                    2 -> "🛡️ Streak Protected (2 chances left)"
                                    1 -> "⚠️ Streak Saved: 1 chance left!"
                                    else -> "❌ No grace chances remaining today"
                                },
                                color = when (stats.streakChancesLeft) {
                                    2 -> Color(0xFFA7F3D0)
                                    1 -> Color(0xFFFDBA74)
                                    else -> Color(0xFFFCA5A5)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Progress circular count badge
                    Box(
                        modifier = Modifier
                            .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$completedCount / $totalCount DONE",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Completion Progress Bar
                val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily Spiritual Habit Reward",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressFraction)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(GoldSecondary, GoldPrimary)
                                    )
                                )
                        )
                    }
                }

                // 7-DAY PAST CONSISTENCY BAR (QURAN.COM STYLE)
                Divider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "LAST 7 DAYS CONSISTENCY",
                        color = TextGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    val pastDays = remember(allTimeTasks) {
                        val list = mutableListOf<Pair<String, String>>()
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Karachi"))
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("Asia/Karachi")
                        }
                        val dayFormat = java.text.SimpleDateFormat("E", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("Asia/Karachi")
                        }
                        
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
                        for (i in 0 until 7) {
                            list.add(Pair(sdf.format(cal.time), dayFormat.format(cal.time).firstOrNull()?.toString() ?: ""))
                            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                        list
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        pastDays.forEach { (dateStr, dayInitial) ->
                            val hasActivity = allTimeTasks.any { it.date == dateStr && it.completedAt != "MISSED" }
                            val isToday = dateStr == viewModel.getPakistanDateString()
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = dayInitial,
                                    color = if (isToday) GoldPrimary else TextGray,
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (hasActivity) GoldPrimary
                                            else if (isToday) GoldPrimary.copy(alpha = 0.15f)
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (hasActivity) GoldPrimary
                                                    else if (isToday) GoldPrimary
                                                    else Color.White.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasActivity) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = OnGoldText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    } else if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(GoldPrimary, CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Protected",
                                            tint = Color.White.copy(alpha = 0.2f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // NAVIGATION TABS TRAY (4 TABS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                Triple("checklist", "Checklist", Icons.Default.CheckCircle),
                Triple("streak", "Streak", Icons.Default.LocalFireDepartment),
                Triple("history", "History", Icons.Default.History)
            ).forEach { (tabKey, label, icon) ->
                val isSelected = selectedTab == tabKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GoldPrimary else Color.Transparent)
                        .clickable { selectedTab = tabKey }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) OnGoldText else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            color = if (isSelected) OnGoldText else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
            } // Close Column
        } // Close item

        // SELECTED TAB RENDER CONTENT
        if (selectedTab == "checklist") {
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your Spiritual Checklist is Empty",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    val timing = viewModel.getTaskTimingStatus(task.title, task.completed)
                        val isLockedAdvance = timing.isLockedAdvance
                        val isMissed = timing.isMissed
                        val isPrayerOrCharity = task.category == "Salah" || 
                                                task.category == "Deeds" || 
                                                task.id.startsWith("manual_charity") ||
                                                task.title.contains("Charity", ignoreCase = true) || 
                                                task.title.contains("Sadaqah", ignoreCase = true) || 
                                                task.title.contains("Zakat", ignoreCase = true)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isMissed) Color(0xFFEF4444).copy(alpha = 0.05f)
                                    else if (isLockedAdvance) EmeraldCard.copy(alpha = 0.5f)
                                    else EmeraldCard
                                )
                                .border(
                                    1.dp,
                                    if (isMissed) Color(0xFFEF4444).copy(alpha = 0.25f)
                                    else if (task.completed) GoldPrimary.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.04f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (isPrayerOrCharity) {
                                        if (isLockedAdvance) {
                                            android.widget.Toast.makeText(context, "Prayer starts at ${timing.startStr}", android.widget.Toast.LENGTH_SHORT).show()
                                        } else if (isMissed) {
                                            android.widget.Toast.makeText(context, "Prayer is Missed/Forgotten", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.toggleMainTask(task.id, !task.completed)
                                        }
                                    } else {
                                        viewModel.handleTaskRedirection(task, context, bookmarks, onNavigate)
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Animated Check Circle Box
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (task.completed) GoldPrimary
                                            else if (isLockedAdvance) Color.White.copy(alpha = 0.05f)
                                            else if (isMissed) Color(0xFFEF4444).copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (task.completed) GoldPrimary
                                                    else if (isLockedAdvance) Color.White.copy(alpha = 0.15f)
                                                    else if (isMissed) Color(0xFFEF4444).copy(alpha = 0.4f)
                                                    else GoldPrimary.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            if (isPrayerOrCharity) {
                                                if (isLockedAdvance) {
                                                    android.widget.Toast.makeText(context, "Prayer starts at ${timing.startStr}", android.widget.Toast.LENGTH_SHORT).show()
                                                } else if (isMissed) {
                                                    android.widget.Toast.makeText(context, "Prayer is Missed/Forgotten", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.toggleMainTask(task.id, !task.completed)
                                                }
                                            } else {
                                                android.widget.Toast.makeText(context, "Automatic: Tap on card to visit page! 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                                viewModel.handleTaskRedirection(task, context, bookmarks, onNavigate)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.completed) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = OnGoldText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else if (!isPrayerOrCharity) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Auto",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    } else if (isLockedAdvance) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    } else if (isMissed) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Missed",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            color = if (task.completed) TextGray else if (isLockedAdvance) Color.White.copy(alpha = 0.5f) else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (task.tag.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = when(task.tag) {
                                                            "OBLIGATORY" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                                            "HOT" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                            "RECOMMENDED" -> Color(0xFFFBBF24).copy(alpha = 0.15f)
                                                            "TIMER" -> Color(0xFF06B6D4).copy(alpha = 0.15f)
                                                            "AUTO", "AUTOMATIC" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                                            else -> Color.White.copy(alpha = 0.1f)
                                                        },
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = task.tag,
                                                    color = when(task.tag) {
                                                        "OBLIGATORY" -> Color(0xFF34D399)
                                                        "HOT" -> Color(0xFFF87171)
                                                        "RECOMMENDED" -> Color(0xFFFBBF24)
                                                        "TIMER" -> Color(0xFF22D3EE)
                                                        "AUTO", "AUTOMATIC" -> Color(0xFFA78BFA)
                                                        else -> Color.White
                                                    },
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }

                                    if (task.description.isNotEmpty() && !task.completed) {
                                        Text(
                                            text = task.description,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Real-time automatic progress verification bar
                                    if (task.isAuto && task.autoTarget > 0 && !task.completed && !isLockedAdvance && !isMissed) {
                                        val progressPercentage = (task.autoProgress.toFloat() / task.autoTarget.toFloat()).coerceIn(0f, 1f)
                                        val progressLabel = when (task.autoType) {
                                            "SURAH" -> {
                                                val progressMinutes = task.autoProgress / 60
                                                val targetMinutes = task.autoTarget / 60
                                                val progressSeconds = task.autoProgress % 60
                                                val targetSeconds = task.autoTarget % 60
                                                if (targetMinutes > 0) {
                                                    "${progressMinutes}m / ${targetMinutes}m"
                                                } else {
                                                    "${progressSeconds}s / ${targetSeconds}s"
                                                }
                                            }
                                            else -> "${task.autoProgress} / ${task.autoTarget}"
                                        }
                                        
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp, bottom = 2.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Verification Progress",
                                                    color = TextGray,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = progressLabel,
                                                    color = GoldPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            LinearProgressIndicator(
                                                progress = progressPercentage,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = GoldPrimary,
                                                trackColor = Color.White.copy(alpha = 0.08f)
                                            )
                                        }
                                    }

                                    if (isLockedAdvance) {
                                        Text(
                                            text = "Begins at ${timing.startStr}",
                                            color = GoldPrimary.copy(alpha = 0.6f),
                                            fontSize = 10.sp
                                        )
                                    } else if (isMissed) {
                                        Text(
                                            text = "Missed & Forgotten (Ended at ${timing.endStr})",
                                            color = Color(0xFFEF4444).copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Points Badge Reward
                                if (task.points > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "+${task.points} XP",
                                            color = GoldPrimary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                // Interactive Inline Challenge Timer
                                if (task.timerSeconds > 0 && !task.completed && !isLockedAdvance && !isMissed) {
                                    val isRunning = activeTimerTaskId == task.id
                                    IconButton(
                                        onClick = {
                                            if (isRunning) {
                                                activeTimerTaskId = null
                                            } else {
                                                activeTimerTaskId = task.id
                                                timeLeftSeconds = task.timerSeconds
                                            }
                                        },
                                        modifier = Modifier
                                            .background(if (isRunning) Color(0xFFEF4444).copy(alpha = 0.15f) else GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isRunning) "Pause" else "Start Timer",
                                            tint = if (isRunning) Color(0xFFEF4444) else GoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // Category Pill
                                Box(
                                    modifier = Modifier
                                        .background(GoldPrimary, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = task.category,
                                        color = OnGoldText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
            }
        } else if (selectedTab == "streak") {
            // QURAN.COM STYLE STREAK CALENDAR HEATMAP & DETAILS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "STREAK STATISTICS",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Current Streak", color = TextGray, fontSize = 11.sp)
                                    Text("${stats.currentStreak} Days", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Longest Streak", color = TextGray, fontSize = 11.sp)
                                    Text("${stats.longestStreak} Days", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total XP Earned", color = TextGray, fontSize = 11.sp)
                                    Text("${stats.totalXp} XP", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Weekly XP Earned", color = TextGray, fontSize = 11.sp)
                                    Text("${stats.weeklyXp} XP", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                            Text(
                                text = "Streak Protection Buffers: ${stats.streakChancesLeft} left. Every day you complete at least one devotional challenge, your streak is extended. If you forget, we use one of your safety buffers to protect your progress!",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // HEATMAP CALENDAR
                item {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
                    val currentMonthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) ?: "Month"
                    val year = calendar.get(Calendar.YEAR)
                    val monthStr = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
                    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "$currentMonthName $year - Habit Heatmap",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Calendar Grid 7 Columns
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Day Name Row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { dName ->
                                        Text(
                                            text = dName,
                                            color = TextGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Divider(color = Color.White.copy(alpha = 0.04f))

                                // Days chunked in 7s
                                val daysList = (1..maxDays).toList()
                                val weeks = daysList.chunked(7)

                                weeks.forEach { week ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        week.forEach { day ->
                                            val dayStr = String.format("%02d", day)
                                            val fullDateStr = "$year-$monthStr-$dayStr"
                                            val isActive = allTimeTasks.any { it.date == fullDateStr && it.completedAt != "MISSED" }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isActive) GoldPrimary
                                                        else if (day == calendar.get(Calendar.DAY_OF_MONTH)) GoldPrimary.copy(alpha = 0.15f)
                                                        else Color.White.copy(alpha = 0.04f)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isActive) GoldPrimary
                                                                else if (day == calendar.get(Calendar.DAY_OF_MONTH)) GoldPrimary
                                                                else Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.toString(),
                                                    color = if (isActive) OnGoldText else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isActive || day == calendar.get(Calendar.DAY_OF_MONTH)) FontWeight.Black else FontWeight.Normal
                                                )
                                            }
                                        }
                                        // Pad out final week if less than 7 days
                                        if (week.size < 7) {
                                            repeat(7 - week.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        } else {
            // ALL-TIME HISTORY DEEDS VIEW
            if (groupedHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No completed deeds logged yet.", color = TextGray, fontSize = 13.sp)
                            Text("Complete a checklist item to start writing history!", color = TextGray.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                }
            } else {
                item {
                    val categoryStats = remember(completedOnly) {
                        completedOnly.groupBy { it.category }.mapValues { it.value.size }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Historical Overview",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoryStats.entries.toList()) { (cat, count) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = EmeraldCard.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(6.dp).background(GoldPrimary, CircleShape)
                                        )
                                        Text(
                                            text = "$cat: $count",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chronologically Grouped Feed
                groupedHistory.forEach { (date, logsList) ->
                        item {
                            val isHighlyProductive = logsList.size >= 7

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EmeraldCard.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Date",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = date,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }

                                    // Perfect / Highly successful day badge
                                    if (isHighlyProductive) {
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Blessed Day 🌟",
                                                color = GoldPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${logsList.size} deeds",
                                                color = TextGray,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Timeline list items for the date
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    logsList.forEach { log ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                // Category specific dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(GoldPrimary, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = log.title,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = log.category.uppercase(Locale.US),
                                                color = GoldPrimary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    // ADD NEW CUSTOM DEED DIALOG
    if (showAddDialog) {
        Dialog(
            onDismissRequest = { showAddDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldBackground),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Add Daily Spiritual Deed",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Title Input Box
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Deed Title",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Recite Surah Al-Kahf", color = TextGray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = EmeraldCard.copy(alpha = 0.3f),
                                unfocusedContainerColor = EmeraldCard.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Selection category row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Category",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoriesList) { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) GoldPrimary else EmeraldCard)
                                        .border(
                                            1.dp, 
                                            if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.05f), 
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) OnGoldText else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dialog Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldCard)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (newTitle.trim().isNotEmpty()) {
                                    viewModel.addCustomTask(newTitle.trim(), selectedCategory)
                                    newTitle = ""
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Add Deed", color = OnGoldText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 11. PROFILE INTENSITIES & BADGES (PROFILE)
// ==========================================
enum class BadgeTier {
    BRONZE, SILVER, GOLD
}

data class DevotionBadge(
    val name: String,
    val description: String,
    val tier: BadgeTier,
    val icon: ImageVector,
    val isEarned: Boolean,
    val currentProgress: Int,
    val targetProgress: Int,
    val progressString: String
)

@Composable
fun BadgeIconItem(
    badge: DevotionBadge,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isEarned = badge.isEarned
    val tier = badge.tier
    
    val baseColor = when (tier) {
        BadgeTier.BRONZE -> Color(0xFFCD7F32) // Bronze
        BadgeTier.SILVER -> Color(0xFFC0C0C0) // Silver
        BadgeTier.GOLD -> Color(0xFFFACC15)   // Gold
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    
    // Premium shining sweep animation (No hovering zoom animation as requested)
    val shineProgress by if (isEarned) {
        when (tier) {
            BadgeTier.GOLD -> infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 4000 // Shines once every 4 seconds
                        0.0f at 0 with LinearEasing
                        1.0f at 1200 with LinearEasing // Elegant smooth sweep in 1.2s
                        1.0f at 4000 with LinearEasing // Silent resting phase
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            BadgeTier.SILVER -> infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 5000 // Shines once every 5 seconds
                        0.0f at 0 with LinearEasing
                        1.0f at 1500 with LinearEasing // Sweeps in 1.5s
                        1.0f at 5000 with LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            BadgeTier.BRONZE -> infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 6000 // Shines once every 6 seconds
                        0.0f at 0 with LinearEasing
                        1.0f at 1800 with LinearEasing // Sweeps in 1.8s
                        1.0f at 6000 with LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    } else {
        remember { mutableStateOf(0f) }
    }
    
    val glowAlpha by if (isEarned) {
        when (tier) {
            BadgeTier.GOLD -> infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            BadgeTier.SILVER -> infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            BadgeTier.BRONZE -> remember { mutableStateOf(1.0f) }
        }
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isEarned) {
                        baseColor.copy(alpha = 0.12f)
                    } else {
                        Color.White.copy(alpha = 0.03f)
                    }
                )
                .border(
                    width = if (isEarned) 2.dp else 1.dp,
                    color = if (isEarned) baseColor.copy(alpha = glowAlpha) else Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
                .drawWithContent {
                    drawContent()
                    if (isEarned) {
                        val width = size.width
                        val height = size.height
                        
                        // Golden and Silver shine color intensities (Gold shines more proudly and boldly)
                        val (shineColor, beamWidth) = when (tier) {
                            BadgeTier.GOLD -> Pair(Color.White.copy(alpha = 0.85f), width * 0.35f)
                            BadgeTier.SILVER -> Pair(Color.White.copy(alpha = 0.55f), width * 0.25f)
                            BadgeTier.BRONZE -> Pair(Color.White.copy(alpha = 0.35f), width * 0.18f)
                        }
                        
                        // Mathematically perfect 45-degree diagonal sweep that starts completely outside and ends completely outside
                        val maxDimension = if (width > height) width else height
                        val sweepRange = maxDimension * 2.5f + beamWidth * 3f
                        val centerPos = -maxDimension - beamWidth + sweepRange * shineProgress
                        
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                shineColor.copy(alpha = 0.03f),
                                shineColor,
                                shineColor.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            start = Offset(centerPos - beamWidth, centerPos - beamWidth),
                            end = Offset(centerPos + beamWidth, centerPos + beamWidth)
                        )
                        
                        drawRect(
                            brush = brush,
                            size = size
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isEarned && tier == BadgeTier.GOLD) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .border(
                            width = 1.dp,
                            color = baseColor.copy(alpha = glowAlpha * 0.4f),
                            shape = CircleShape
                        )
                )
            }
            
            Icon(
                imageVector = badge.icon,
                contentDescription = badge.name,
                tint = if (isEarned) baseColor else Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = badge.name,
            color = if (isEarned) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(68.dp)
        )
    }
}

fun suggestThemeUsername(): String {
    val prefixes = listOf(
        "servant_of_allah",
        "taqwa_seeker",
        "quran_soul",
        "mumin_heart",
        "sincere_servant",
        "nur_soul",
        "jannah_aspirant",
        "dhikr_soul",
        "spirit_of_sabr",
        "all_for_islam",
        "deen_follower",
        "servant_heart",
        "humble_servant",
        "guided_soul"
    )
    val randomNum = (100..999).random()
    return "${prefixes.random()}_$randomNum"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: TaqwaViewModel,
    onSignOut: () -> Unit
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val allTimeTasks by viewModel.allTimeTasks.collectAsStateWithLifecycle()

    var editName by remember { mutableStateOf(stats.name) }
    var editUsername by remember { mutableStateOf(stats.username) }
    var editGender by remember { mutableStateOf(stats.gender) }
    var editSect by remember { mutableStateOf(stats.sectOrCast) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBadgeForDetail by remember { mutableStateOf<DevotionBadge?>(null) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var isDeletingAccount by remember { mutableStateOf(false) }

    LaunchedEffect(showDeleteAccountDialog) {
        if (showDeleteAccountDialog) {
            countdownSeconds = 5
            isDeletingAccount = false
            while (countdownSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                countdownSeconds--
            }
        }
    }

    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            editName = stats.name
            editUsername = stats.username
            editGender = stats.gender
            editSect = stats.sectOrCast
        }
    }

    val completedCount = remember(stats.completedSurahs) {
        stats.completedSurahs.split(",")
            .filter { it.isNotEmpty() }
            .size
    }

    val currentFirebaseEmail = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email }
    val displayEmail = if (!currentFirebaseEmail.isNullOrBlank()) {
        currentFirebaseEmail
    } else if (stats.email.isNotBlank()) {
        stats.email
    } else {
        "Not Signed In"
    }

    val devotionBadges = remember(stats, completedCount) {
        listOf(
            DevotionBadge(
                name = "Devoted Disciple",
                description = "Complete your first daily task or prayer action to begin your journey.",
                tier = BadgeTier.BRONZE,
                icon = Icons.Default.Star,
                isEarned = stats.totalTasksCompleted >= 1,
                currentProgress = stats.totalTasksCompleted,
                targetProgress = 1,
                progressString = "${min(stats.totalTasksCompleted, 1)} / 1 action"
            ),
            DevotionBadge(
                name = "Ignited Constancy",
                description = "Maintain an active spiritual streak of 3 days or more in the tracker.",
                tier = BadgeTier.BRONZE,
                icon = Icons.Default.LocalFireDepartment,
                isEarned = stats.currentStreak >= 3,
                currentProgress = stats.currentStreak,
                targetProgress = 3,
                progressString = "${min(stats.currentStreak, 3)} / 3 days streak"
            ),
            DevotionBadge(
                name = "Identity of Faith",
                description = "Claim your customizable servant name in settings to establish your unique identity.",
                tier = BadgeTier.BRONZE,
                icon = Icons.Default.Fingerprint,
                isEarned = stats.name != "Servant of Allah",
                currentProgress = if (stats.name != "Servant of Allah") 1 else 0,
                targetProgress = 1,
                progressString = if (stats.name != "Servant of Allah") "1 / 1 claimed" else "0 / 1 claimed"
            ),
            DevotionBadge(
                name = "Steadfast Servant",
                description = "Log over 10 total actions in your spiritual history.",
                tier = BadgeTier.SILVER,
                icon = Icons.Default.DoneAll,
                isEarned = stats.totalTasksCompleted >= 10,
                currentProgress = stats.totalTasksCompleted,
                targetProgress = 10,
                progressString = "${min(stats.totalTasksCompleted, 10)} / 10 actions"
            ),
            DevotionBadge(
                name = "Dhikr Master",
                description = "Pledge and count 100+ total Tasbeeh counters.",
                tier = BadgeTier.SILVER,
                icon = Icons.Default.VolunteerActivism,
                isEarned = stats.tasbeehCount >= 100,
                currentProgress = stats.tasbeehCount,
                targetProgress = 100,
                progressString = "${min(stats.tasbeehCount, 100)} / 100 dhikr"
            ),
            DevotionBadge(
                name = "Quran Scholar",
                description = "Complete the reading or recitation of 5 or more Surahs.",
                tier = BadgeTier.SILVER,
                icon = Icons.Default.MenuBook,
                isEarned = completedCount >= 5,
                currentProgress = completedCount,
                targetProgress = 5,
                progressString = "${min(completedCount, 5)} / 5 Surahs"
            ),
            DevotionBadge(
                name = "Streak Warrior",
                description = "Unlock the ultimate constancy by achieving a streak of 15 days or more.",
                tier = BadgeTier.GOLD,
                icon = Icons.Default.WorkspacePremium,
                isEarned = stats.longestStreak >= 15,
                currentProgress = stats.longestStreak,
                targetProgress = 15,
                progressString = "${min(stats.longestStreak, 15)} / 15 days streak"
            ),
            DevotionBadge(
                name = "Hifz Al-Quran",
                description = "Recite all 114 Surahs completely in the application for the ultimate spiritual honor.",
                tier = BadgeTier.GOLD,
                icon = Icons.Default.AutoAwesome,
                isEarned = completedCount >= 114,
                currentProgress = completedCount,
                targetProgress = 114,
                progressString = "${min(completedCount, 114)} / 114 Surahs"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Premium Visual Profile Header with Avatar and User Name Tag
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Profile Avatar Container with click-to-upload or edit icon
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(EmeraldMedium.copy(alpha = 0.5f), EmeraldCard)
                                )
                            )
                            .border(2.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(stats.profilePictureBase64) {
                            if (stats.profilePictureBase64.isNotEmpty()) {
                                decodeBase64ToBitmap(stats.profilePictureBase64)
                            } else null
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            WhatsAppPlaceholderAvatar(modifier = Modifier.fillMaxSize())
                        }
                    }

                    // Upload photo button launcher
                    val context = LocalContext.current
                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        uri?.let {
                            try {
                                val inputStream = context.contentResolver.openInputStream(it)
                                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                if (originalBitmap != null) {
                                    // Compress and resize image to fit in Firestore safely (max 160px width/height)
                                    val sizeLimit = 160
                                    val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                                    val (width, height) = if (ratio > 1) {
                                        Pair(sizeLimit, (sizeLimit / ratio).toInt())
                                    } else {
                                        Pair((sizeLimit * ratio).toInt(), sizeLimit)
                                    }
                                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                                    val outputStream = java.io.ByteArrayOutputStream()
                                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                                    val bytes = outputStream.toByteArray()
                                    val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                                    viewModel.updateProfilePictureBase64(base64String)
                                    Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("TaqwaScreens", "Error processing selected image", e)
                                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                            .border(1.dp, EmeraldCard, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Upload Profile Picture",
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stats.name,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (viewModel.isUserVerified(stats)) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Servant",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Username Tag perfectly centered using equal spacing trick, with minimal pencil icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Invisible spacer to balance the pencil button on the right for absolute pixel-perfect centering
                        Spacer(modifier = Modifier.width(30.dp))

                        Box(
                            modifier = Modifier
                                .background(EmeraldMedium.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "@" + stats.username.ifEmpty { "servant_of_allah" },
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile Info",
                                tint = GoldPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Stats summary card row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(EmeraldMedium.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL DONE", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(stats.totalTasksCompleted.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(EmeraldMedium.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ACTIVE STREAK", color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${stats.currentStreak} DAY${if (stats.currentStreak == 1) "" else "S"}", color = Color(0xFFFACC15), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stats.streakChancesLeft} safety buffers",
                            color = if (stats.streakChancesLeft == 2) Color(0xFFA7F3D0) else Color(0xFFFDBA74),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Activity Graph section
        item {
            GoldenActivityGraph(allTimeTasks = allTimeTasks)
        }

        // 5. Leaderboard trophies section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Weekly Leaderboard Trophies",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileTrophyBadge(
                            placeName = "Champion",
                            count = stats.firstPlaceCount,
                            color = Color(0xFFFBBF24),
                            tintColor = Color(0xFFFBBF24),
                            placeNum = 1,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTrophyBadge(
                            placeName = "Contender",
                            count = stats.secondPlaceCount,
                            color = Color(0xFF9CA3AF),
                            tintColor = Color(0xFF9CA3AF),
                            placeNum = 2,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTrophyBadge(
                            placeName = "Elite",
                            count = stats.thirdPlaceCount,
                            color = Color(0xFFD97706),
                            tintColor = Color(0xFFD97706),
                            placeNum = 3,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 6. Upgraded Spiritual Honors Badges Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Spiritual Honors",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap on any honor badge to view details & target progress",
                            color = TextGray.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    // Display as a beautiful layout of 2 rows of 4 columns
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val chunks = devotionBadges.chunked(4)
                        chunks.forEach { rowBadges ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowBadges.forEach { badge ->
                                    BadgeIconItem(
                                        badge = badge,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedBadgeForDetail = badge }
                                    )
                                }
                                repeat(4 - rowBadges.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Signout Button
        item {
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(50.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIGN OUT ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // 8. Delete Account Button (Google Play Compliance & Privacy)
        item {
            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(50.dp)
                    .testTag("delete_account_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account", tint = AlertRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DELETE ACCOUNT & DATA", color = AlertRed, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Interactive Wifi-like detail dialog popover
    if (selectedBadgeForDetail != null) {
        val badge = selectedBadgeForDetail!!
        val isEarned = badge.isEarned
        val tier = badge.tier
        val baseColor = when (tier) {
            BadgeTier.BRONZE -> Color(0xFFCD7F32)
            BadgeTier.SILVER -> Color(0xFFC0C0C0)
            BadgeTier.GOLD -> Color(0xFFFACC15)
        }
        
        Dialog(
            onDismissRequest = { selectedBadgeForDetail = null }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(28.dp)),
                color = EmeraldCard,
                border = BorderStroke(2.dp, baseColor.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(baseColor.copy(alpha = 0.15f))
                            .border(2.dp, baseColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = baseColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = badge.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tier.name} MEDAL",
                            color = baseColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Text(
                        text = badge.description,
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    val progressFraction = remember(badge.currentProgress, badge.targetProgress) {
                        if (badge.targetProgress > 0) {
                            (badge.currentProgress.toFloat() / badge.targetProgress.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Requirement Progress",
                                color = TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = badge.progressString,
                                color = baseColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = baseColor,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                    
                    Text(
                        text = if (isEarned) "🎉 Honor Unlocked & Awarded!" else "🔒 Currently Locked",
                        color = if (isEarned) Color(0xFF10B981) else TextGray.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = { selectedBadgeForDetail = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium.copy(alpha = 0.2f))
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        Dialog(
            onDismissRequest = { showEditDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp)),
                color = EmeraldCard,
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Profile & Credentials",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Live Digital Servant Card Preview
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldMedium.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TAQWA SERVANT CARD",
                                        color = GoldPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    if (viewModel.isUserVerified(stats)) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ProfileInfoRow(Icons.Default.Person, "Servant Name", editName.ifEmpty { stats.name })
                                    ProfileInfoRow(Icons.Default.AlternateEmail, "Username", "@" + editUsername.ifEmpty { "not_set" })
                                    ProfileInfoRow(Icons.Default.Face, "Gender", editGender.ifEmpty { "Not Specified" })
                                    ProfileInfoRow(Icons.Default.Category, "Cast / Sect", editSect.ifEmpty { "Not Specified" })
                                    ProfileInfoRow(Icons.Default.Email, "Email Account", displayEmail)
                                    ProfileInfoRow(Icons.Default.DateRange, "Active Days", "${stats.daysActive} Days")
                                }
                            }
                        }

                        Text(
                            text = "EDIT DETAILS",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                            label = { Text("Name", color = GoldPrimary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = editGender,
                            onValueChange = { editGender = it },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_gender"),
                            label = { Text("Gender", color = GoldPrimary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = editSect,
                            onValueChange = { editSect = it },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_sect"),
                            label = { Text("Cast / Sect", color = GoldPrimary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showEditDialog = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium.copy(alpha = 0.2f))
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.setStats(
                                    stats.copy(
                                        name = editName,
                                        username = stats.username,
                                        gender = editGender,
                                        sectOrCast = editSect
                                    )
                                )
                                showEditDialog = false
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Save", color = OnGoldText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteAccountDialog) {
        val context = LocalContext.current
        Dialog(
            onDismissRequest = {
                if (!isDeletingAccount) showDeleteAccountDialog = false
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(24.dp)),
                color = EmeraldCard,
                border = BorderStroke(1.5.dp, AlertRed.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AlertRed.copy(alpha = 0.15f))
                            .border(1.5.dp, AlertRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = AlertRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Delete Account & Data?",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PERMANENT ACTION",
                            color = AlertRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "This action cannot be undone. All your local and cloud data, streak history, badges, completed surahs, and custom settings will be permanently erased.",
                        color = TextGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    TextButton(
                        onClick = {
                            val targetUrl = viewModel.appConfig.deleteAccountUrl.ifBlank { "https://taqwahub.vercel.app/delete-account.html" }
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening: $targetUrl", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WEB ACCOUNT DELETION PORTAL", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (countdownSeconds > 0) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Unlocking delete button in ${countdownSeconds}s...",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            LinearProgressIndicator(
                                progress = { (5 - countdownSeconds) / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GoldPrimary,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showDeleteAccountDialog = false },
                            enabled = !isDeletingAccount,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium.copy(alpha = 0.3f))
                        ) {
                            Text("Cancel", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                isDeletingAccount = true
                                viewModel.deleteAccount(
                                    onSuccess = {
                                        isDeletingAccount = false
                                        showDeleteAccountDialog = false
                                        Toast.makeText(context, "Account & data successfully deleted", Toast.LENGTH_SHORT).show()
                                        onSignOut()
                                    },
                                    onError = { err ->
                                        isDeletingAccount = false
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = countdownSeconds == 0 && !isDeletingAccount,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("confirm_delete_account_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                disabledContainerColor = AlertRed.copy(alpha = 0.25f)
                            )
                        ) {
                            if (isDeletingAccount) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (countdownSeconds > 0) "Delete (${countdownSeconds}s)" else "Delete Account",
                                    color = if (countdownSeconds > 0) Color.White.copy(alpha = 0.5f) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldMedium.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                color = TextGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// 12. HIJRI/LUNAR DETAILED CALENDAR (CALENDAR)
// ==========================================
@Composable
fun IslamicCalendarScreen(viewModel: TaqwaViewModel) {
data class HijriMonthInfo(val name: String, val meaning: String, val significance: String, val spiritualFocus: String, val icon: ImageVector)

val hijriMonthDetails = listOf(
    HijriMonthInfo("Muharram", "The Forbidden month.", "Mark Islamic New year and Ashura.", "Seeking Istighfar and reflection.", Icons.Default.DateRange),
    HijriMonthInfo("Safar", "Void month.", "Re-instating regular prayers.", "Mindfulness and consistency.", Icons.Default.Notifications),
    HijriMonthInfo("Rabi' al-Awwal", "First Spring Chapter.", "Birth of our Prophet (PBUH).", "Reflecting on the Seerah.", Icons.Default.Favorite),
    HijriMonthInfo("Rabi' ath-Thani", "Second Spring Chapter.", "Continuing spiritual growth.", "Deepening personal devotion.", Icons.Default.PlayArrow),
    HijriMonthInfo("Jumada al-Ula", "Dry Land chapter.", "Deepening Quran devotion.", "Reading and studying the Quran.", Icons.Default.MenuBook),
    HijriMonthInfo("Jumada al-Akhirah", "Preparing hearts.", "Preparing for holy months.", "Reflection and self-improvement.", Icons.Default.Settings),
    HijriMonthInfo("Rajab", "Honor Month.", "Recalling Al-Mi'raj.", "Seeking Istighfar and preparation.", Icons.Default.Star),
    HijriMonthInfo("Sha'ban", "Increasing optional fasts.", "Pre-Ramadan preparation.", "Increasing voluntary fasts.", Icons.Default.Face),
    HijriMonthInfo("Ramadan", "Holy scorch fasting.", "Revelation of the Quran.", "Fasting, Quran, and Laylat al-Qadr.", Icons.Default.Lock),
    HijriMonthInfo("Shawwal", "Eid al-Fitr greetings.", "Post-Ramadan gratitude.", "Voluntary Shawwal fasts.", Icons.Default.Check),
    HijriMonthInfo("Dhu al-Qi'dah", "Truce Month.", "Commencing Hajj preparations.", "Travel preparations and patience.", Icons.Default.Place),
    HijriMonthInfo("Dhu al-Hijjah", "Pilgrimage.", "Best 10 days of the year.", "Pilgrimage and Eid al-Adha.", Icons.Default.AccountCircle)
)
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val hijriDate = HijrahChronology.INSTANCE.date(currentDate)
    val hijriMonthNames = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' ath-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
    
    // Cosmic Emerald colors
    val emerald900 = Color(0xFF022C22)
    val emerald950 = Color(0xFF021612)
    
    val monthIndex = hijriDate.get(ChronoField.MONTH_OF_YEAR) - 1
    val monthName = hijriMonthNames.getOrElse(monthIndex) { "" }

    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val calendarBlinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "blink"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(emerald900)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. MONTH SELECTOR GRID (JAN-DEC)
        val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            months.chunked(4).forEach { rowMonths ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowMonths.forEach { monthLabel ->
                        val isSelected = monthLabel == currentDate.month.name.substring(0, 3)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GoldPrimary else EmeraldCard.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = monthLabel,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. HEADER
        Text(
            text = "${currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentDate.year}",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$monthName ${hijriDate.get(ChronoField.YEAR_OF_ERA)} AH",
            color = GoldPrimary,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )

        // Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { currentDate = LocalDate.now() }, colors = ButtonDefaults.buttonColors(containerColor = EmeraldCard)) {
                Text("TODAY")
            }
            Row {
                IconButton(onClick = { currentDate = currentDate.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, "Prev", tint = Color.White) }
                IconButton(onClick = { currentDate = currentDate.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, "Next", tint = Color.White) }
            }
        }

        // Calendar Grid Placeholder (Days of week + Circular days)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header row of days of week
                val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { dayOfWeek ->
                        Text(
                            text = dayOfWeek,
                            color = GoldPrimary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Days of month (31 days, padded to fit grid if needed)
                val daysList = (1..31).toList()
                daysList.chunked(7).forEach { weekDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        if (day == currentDate.dayOfMonth) GoldPrimary else Color.Transparent,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    color = if (day == currentDate.dayOfMonth) Color.Black else Color.White
                                )
                            }
                        }
                        
                        // Pad the last row with empty items to keep weight layout regular!
                        if (weekDays.size < 7) {
                            repeat(7 - weekDays.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        
        // 3. Info Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Lunar Cycle", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("The Hijri calendar is based on the moon's phases, making it 10-11 days shorter than the solar year.", color = TextGray, fontSize = 14.sp)
            }
        }

        Text("Hijri Months", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        hijriMonthDetails.forEachIndexed { index, monthInfo ->
            val isOngoing = index == monthIndex
            val cardColor = if (isOngoing) GoldPrimary.copy(alpha = 0.2f) else EmeraldCard.copy(alpha = 0.3f)
            val borderColor = if (isOngoing) GoldPrimary else GoldPrimary.copy(alpha = 0.1f)

            Card(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.selectedHijriMonthDetails = monthInfo.name },
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(monthInfo.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(monthInfo.meaning, color = TextGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (isOngoing) {
                        Box(Modifier.background(GoldPrimary, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp).graphicsLayer(alpha = calendarBlinkAlpha)) {
                            Text("ONGOING", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    // Month Detail Popover implementation
    if (viewModel.selectedHijriMonthDetails != null) {
        val selectedMonth = hijriMonthDetails.find { it.name == viewModel.selectedHijriMonthDetails }
        if (selectedMonth != null) {
            Dialog(onDismissRequest = { viewModel.selectedHijriMonthDetails = null }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(24.dp)),
                    color = EmeraldCard.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(selectedMonth.icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(64.dp))
                        }
                        Text(text = selectedMonth.name.uppercase(), color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(text = selectedMonth.meaning, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Significance: ${selectedMonth.significance}", color = TextGray, fontSize = 14.sp)
                        Text(text = "Spiritual Focus: ${selectedMonth.spiritualFocus}", color = TextGray, fontSize = 14.sp)
                        Button(
                            onClick = { viewModel.selectedHijriMonthDetails = null },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("DONE", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

    // ==========================================
    // 13. SADQAYE JARIYA LEAD CARDS (DONATE)
    // ==========================================
    @Composable
    fun DonateScreen(viewModel: TaqwaViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Beautiful Hero Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_donation_banner_1782729098739),
                    contentDescription = "Sadqah Jariyah Header Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter
                )
                
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sadqah Jariyah",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = "“The example of those who spend their wealth in the way of Allah is like a seed [of grain] which grows seven spikes; in each spike is a hundred grains. And Allah multiplies [His reward] for whom He wills.”\n(Surah Al-Baqarah 2:261)",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // 2. Impact/Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "WHY YOUR SUPPORT MATTERS",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Detail 1
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Infrastructure",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Server Infrastructure",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Keeping Quran audio streaming, daily app content databases, translation feeds, and API endpoints online 24/7.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Detail 2
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Ad-Free",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "100% Free & Ad-Free Experience",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Our platform is completely ad-free. We never monetize sacred learning with distracting commercial promotions.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Detail 3
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Deen",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Propagating Authentic Deen Knowledge",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your contribution directly empowers modern digital tools for Quran translation, Hadith libraries, and verified prayers calculators.",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 3. Official Donation Portal (Safe Web Redirection)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "OFFICIAL DONATION PORTAL",
                    color = TextGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "All contributions and support for TaqwaHub are hosted securely on our official web portal in full compliance with Google Play guidelines.",
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        val targetUrl = viewModel.appConfig.donateRedirectUrl.ifBlank { "https://taqwahub.org/donate" }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open browser. Please visit: $targetUrl", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Donate Web Portal",
                        tint = OnGoldText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SUPPORT TAQWAHUB ON WEBSITE",
                        color = OnGoldText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Opens in your secure system web browser",
                    color = TextGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 4. Contact and Verification section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CONTACT & INQUIRIES",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Have questions about TaqwaHub or need assistance with your support? Contact our administrative team directly.",
                    color = TextGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:taqwahub.ai@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "TaqwaHub Support & Inquiries")
                        }
                        try {
                            context.startActivity(emailIntent)
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.Mail, contentDescription = "Email", tint = OnGoldText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONTACT: taqwahub.ai@gmail.com", color = OnGoldText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun parseCitationFullText(raw: String): List<Pair<String, String>> {
    val sections = mutableListOf<Pair<String, String>>()
    val lines = raw.split("\n")
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        if (trimmed.startsWith("- TYPE:")) {
            sections.add("Type" to trimmed.substringAfter("- TYPE:").trim())
        } else if (trimmed.startsWith("REFERENCE:")) {
            sections.add("Reference" to trimmed.substringAfter("REFERENCE:").trim())
        } else if (trimmed.startsWith("SOURCE:")) {
            sections.add("Source" to trimmed.substringAfter("SOURCE:").trim())
        } else if (trimmed.startsWith("NARRATOR:")) {
            sections.add("Narrator" to trimmed.substringAfter("NARRATOR:").trim())
        } else if (trimmed.startsWith("CHAPTER:")) {
            sections.add("Chapter" to trimmed.substringAfter("CHAPTER:").trim())
        } else if (trimmed.startsWith("ARABIC:")) {
            sections.add("Arabic" to trimmed.substringAfter("ARABIC:").trim())
        } else if (trimmed.startsWith("TRANSLITERATION:")) {
            sections.add("Transliteration" to trimmed.substringAfter("TRANSLITERATION:").trim())
        } else if (trimmed.startsWith("TRANSLATION_EN:") || trimmed.startsWith("TEXT_EN:")) {
            val label = if (trimmed.startsWith("TEXT_EN:")) "TEXT_EN:" else "TRANSLATION_EN:"
            sections.add("English Translation" to trimmed.substringAfter(label).trim())
        } else if (trimmed.startsWith("TRANSLATION_UR:")) {
            sections.add("Urdu Translation" to trimmed.substringAfter("TRANSLATION_UR:").trim())
        } else if (trimmed.startsWith("MEANING:")) {
            sections.add("Meaning" to trimmed.substringAfter("MEANING:").trim())
        } else if (trimmed.startsWith("VERSES:")) {
            sections.add("Verses" to trimmed.substringAfter("VERSES:").trim())
        } else {
            if (sections.isNotEmpty()) {
                val last = sections.last()
                sections[sections.size - 1] = last.first to (last.second + "\n" + trimmed)
            } else {
                sections.add("Details" to trimmed)
            }
        }
    }
    return sections
}

// ==========================================
// 14. TAQWAHUB AI CONCURRENT AGENT CHAT (REMOVED)
// ==========================================
@Composable
fun AiAssistantDialog(
    viewModel: TaqwaViewModel,
    onDismiss: () -> Unit
) {
    // AI Chat completely disabled / removed
}
/*
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFFFFFFFF).copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = "Wisdom Icon",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = "Welcome to Islamic Wisdom",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "\"He who follows a path in search of knowledge, Allah will make easy for him the path to Paradise.\" (Sahih Muslim)",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(messages) { msg ->
                                    val isUser = msg.role == "user"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        if (!isUser) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(GoldPrimary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "AI Logo",
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        
                                        Column(
                                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                                        ) {
                                            Box(
                                            modifier = Modifier
                                                .widthIn(max = 280.dp)
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomEnd = if (isUser) 4.dp else 16.dp,
                                                        bottomStart = if (isUser) 16.dp else 4.dp
                                                    )
                                                )
                                                .background(
                                                    if (isUser) GoldPrimary else Color(0xFF03412E)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isUser) GoldPrimary else GoldPrimary.copy(alpha = 0.1f),
                                                    RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomEnd = if (isUser) 4.dp else 16.dp,
                                                        bottomStart = if (isUser) 16.dp else 4.dp
                                                    )
                                                )
                                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                        ) {
                                            val isRtl = msg.text.any { it in '\u0600'..'\u06FF' }
                                            if (false) {
                                                val showCitations = false
                                                Column {
                                                    Text(
                                                        text = msg.text,
                                                        color = if (isUser) OnGoldText else Color.White,
                                                        fontSize = 13.sp,
                                                        lineHeight = 18.sp,
                                                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                                                        textAlign = if (isRtl) TextAlign.End else TextAlign.Start,
                                                        style = androidx.compose.ui.text.TextStyle(
                                                            textDirection = androidx.compose.ui.text.style.TextDirection.Content
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF012419))
                                                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                            .clickable { }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MenuBook,
                                                            contentDescription = "Source Icon",
                                                            tint = GoldPrimary,
                                                            modifier = Modifier.size(11.dp)
                                                         )
                                                         Spacer(modifier = Modifier.width(6.dp))
                                                         Text(
                                                             text = if (showCitations) "Hide Sources" else "Source (${msg.citations.size})",
                                                             color = GoldPrimary,
                                                             fontSize = 10.sp,
                                                             fontWeight = FontWeight.Bold
                                                         )
                                                    }
                                                    
                                                     if (showCitations) {
                                                         Spacer(modifier = Modifier.height(8.dp))
                                                         Column(
                                                             modifier = Modifier
                                                                 .fillMaxWidth()
                                                                 .clip(RoundedCornerShape(8.dp))
                                                                 .background(Color(0xFF011C13))
                                                                 .border(0.8.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                                 .padding(8.dp),
                                                             verticalArrangement = Arrangement.spacedBy(6.dp)
                                                         ) {
                                                             Text(
                                                                 text = "Referenced Sources",
                                                                 color = GoldPrimary,
                                                                 fontSize = 9.sp,
                                                                 fontWeight = FontWeight.Black
                                                             )
                                                             
                                                             Box(
                                                                 modifier = Modifier
                                                                     .fillMaxWidth()
                                                                     .height(0.8.dp)
                                                                     .background(GoldPrimary.copy(alpha = 0.12f))
                                                             )
                                                             
                                                             msg.citations.forEachIndexed { idx, citation ->
                                                                 val fullText = msg.citationFullTexts.getOrNull(idx) ?: ""
                                                                 Column(modifier = Modifier.fillMaxWidth()) {
                                                                     Text(
                                                                         text = "${idx + 1}. $citation",
                                                                         color = Color.White.copy(alpha = 0.85f),
                                                                         fontSize = 11.sp,
                                                                         lineHeight = 15.sp
                                                                     )
                                                                     if (fullText.isNotEmpty()) {
                                                                         Spacer(modifier = Modifier.height(2.dp))
                                                                         Text(
                                                                             text = "Read Full Text",
                                                                             color = GoldPrimary,
                                                                             fontSize = 10.sp,
                                                                             fontWeight = FontWeight.Bold,
                                                                             style = androidx.compose.ui.text.TextStyle(
                                                                                 textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                                                             ),
                                                                             modifier = Modifier
                                                                                 .clickable {
                                                                                     activeCitationToShow = Pair(citation, fullText)
                                                                                 }
                                                                                 .padding(vertical = 2.dp)
                                                                         )
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     }
                                                }
                                            } else {
                                                Text(
                                                    text = msg.text,
                                                    color = if (isUser) OnGoldText else Color.White,
                                                    fontSize = 13.sp,
                                                    lineHeight = 18.sp,
                                                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                                                    textAlign = if (isRtl) TextAlign.End else TextAlign.Start,
                                                    style = androidx.compose.ui.text.TextStyle(
                                                        textDirection = androidx.compose.ui.text.style.TextDirection.Content
                                                    )
                                                )
                                            }
                                        }
                                        
                                        if (!isUser) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(start = 4.dp)
                                            ) {
                                                // Like Action
                                                val isLiked = msg.rating == "like"
                                                val likeScale by animateFloatAsState(
                                                    targetValue = if (isLiked) 1.25f else 1.0f,
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val index = messages.indexOf(msg)
                                                        val queryText = if (index > 0) messages[index - 1].text else "General Question"
                                                        val nextRating = if (isLiked) "none" else "like"
                                                        viewModel.submitAiFeedback(msg.id, queryText, msg.text, nextRating, msg.reportMessage)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                                        contentDescription = "Like response",
                                                        tint = if (isLiked) GoldPrimary else Color.White.copy(alpha = 0.45f),
                                                        modifier = Modifier
                                                            .size(13.dp)
                                                            .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                                                    )
                                                }
                                                
                                                // Dislike Action
                                                val isDisliked = msg.rating == "dislike"
                                                val dislikeScale by animateFloatAsState(
                                                    targetValue = if (isDisliked) 1.25f else 1.0f,
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        val index = messages.indexOf(msg)
                                                        val queryText = if (index > 0) messages[index - 1].text else "General Question"
                                                        val nextRating = if (isDisliked) "none" else "dislike"
                                                        viewModel.submitAiFeedback(msg.id, queryText, msg.text, nextRating, msg.reportMessage)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                                                        contentDescription = "Dislike response",
                                                        tint = if (isDisliked) Color(0xFFEF4444) else Color.White.copy(alpha = 0.45f),
                                                        modifier = Modifier
                                                            .size(13.dp)
                                                            .graphicsLayer(scaleX = dislikeScale, scaleY = dislikeScale)
                                                    )
                                                }
                                                
                                                // Report Action
                                                val isReported = msg.reportMessage.isNotEmpty()
                                                val reportScale by animateFloatAsState(
                                                    targetValue = if (isReported) 1.25f else 1.0f,
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        messageToReport = msg
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isReported) Icons.Default.Feedback else Icons.Outlined.Feedback,
                                                        contentDescription = "Report/Suggest improvements",
                                                        tint = if (isReported) GoldPrimary else Color.White.copy(alpha = 0.45f),
                                                        modifier = Modifier
                                                            .size(13.dp)
                                                            .graphicsLayer(scaleX = reportScale, scaleY = reportScale)
                                                    )
                                                }
                                                
                                                if (isReported) {
                                                    Text(
                                                        text = "Suggestion Sent",
                                                        color = GoldPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        fontStyle = FontStyle.Italic,
                                                        modifier = Modifier.padding(start = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    }
                                }

                                if (isChatLoading) {
                                    item {
                                        val infiniteTransition = rememberInfiniteTransition(label = "branded_chat_loading")

                                        val emblemScale by infiniteTransition.animateFloat(
                                            initialValue = 0.9f,
                                            targetValue = 1.15f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "emblem_scale"
                                        )

                                        val emblemAlpha by infiniteTransition.animateFloat(
                                            initialValue = 0.4f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "emblem_alpha"
                                        )

                                        val dotAlpha1 by infiniteTransition.animateFloat(
                                            initialValue = 0.2f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(650, delayMillis = 0, easing = androidx.compose.animation.core.LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "dot_1"
                                        )
                                        val dotAlpha2 by infiniteTransition.animateFloat(
                                            initialValue = 0.2f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(650, delayMillis = 150, easing = androidx.compose.animation.core.LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "dot_2"
                                        )
                                        val dotAlpha3 by infiniteTransition.animateFloat(
                                            initialValue = 0.2f,
                                            targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(650, delayMillis = 300, easing = androidx.compose.animation.core.LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "dot_3"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.Start,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier.size(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .graphicsLayer {
                                                            scaleX = emblemScale
                                                            scaleY = emblemScale
                                                            alpha = emblemAlpha * 0.25f
                                                        }
                                                        .clip(CircleShape)
                                                        .background(GoldPrimary)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF012419)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = "AI Loading",
                                                        tint = GoldPrimary,
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .graphicsLayer {
                                                                alpha = emblemAlpha
                                                            }
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(
                                                modifier = Modifier
                                                    .widthIn(max = 280.dp)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                                                    .background(Color(0xFF023625))
                                                    .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .graphicsLayer {
                                                                alpha = dotAlpha1
                                                                scaleX = dotAlpha1
                                                                scaleY = dotAlpha1
                                                            }
                                                            .clip(CircleShape)
                                                            .background(GoldPrimary)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .graphicsLayer {
                                                                alpha = dotAlpha2
                                                                scaleX = dotAlpha2
                                                                scaleY = dotAlpha2
                                                            }
                                                            .clip(CircleShape)
                                                            .background(GoldPrimary)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .graphicsLayer {
                                                                alpha = dotAlpha3
                                                                scaleX = dotAlpha3
                                                                scaleY = dotAlpha3
                                                            }
                                                            .clip(CircleShape)
                                                            .background(GoldPrimary)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input & Controls section
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF01241A))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = "Resting",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(32.dp).padding(bottom = 8.dp)
                                )
                                Text(
                                    "TaqwaHub AI is resting",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Your 10 daily queries have been completed.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AlertRedBg.copy(alpha = 0.3f))
                                        .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Returns in: $countdown",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Symmetrical bottom input fields
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF012C1E))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputQuery,
                                onValueChange = { inputQuery = it },
                                enabled = !isChatLoading && !isAiStreaming,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_chat_input"),
                                shape = RoundedCornerShape(22.dp),
                                placeholder = {
                                    Text(
                                        text = if (isChatLoading || isAiStreaming) "AI is typing..." else "Ask a question...",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 13.sp
                                    )
                                },
                                keyboardActions = KeyboardActions(onSend = {
                                    if (inputQuery.isNotEmpty() && !isChatLoading && !isAiStreaming) {
                                        viewModel.sendMessage(inputQuery)
                                        inputQuery = ""
                                        kbController?.hide()
                                        focusManager.clearFocus()
                                    }
                                }),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Send,
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = GoldPrimary.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF012419),
                                    unfocusedContainerColor = Color(0xFF012419),
                                    disabledContainerColor = Color(0xFF011C13),
                                    disabledTextColor = Color.White.copy(alpha = 0.4f),
                                    disabledBorderColor = GoldPrimary.copy(alpha = 0.1f),
                                    cursorColor = GoldPrimary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )

                            // Dynamic styled Send button based on whether user has input query or not
                            val hasInput = inputQuery.isNotEmpty() && !isChatLoading && !isAiStreaming
                            val sendBtnContainerColor = if (hasInput) GoldPrimary else Color(0xFF1E3A31)
                            val sendBtnTextColor = if (hasInput) OnGoldText else Color.White.copy(alpha = 0.3f)

                            Button(
                                onClick = {
                                    if (hasInput) {
                                        viewModel.sendMessage(inputQuery)
                                        inputQuery = ""
                                        kbController?.hide()
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = hasInput,
                                modifier = Modifier.height(44.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sendBtnContainerColor,
                                    disabledContainerColor = Color(0xFF1E3A31)
                                )
                            ) {
                                Text(
                                    text = "SEND",
                                    color = sendBtnTextColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Symmetrical details footer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF012C1E))
                                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TaqwaHub AI Assistant",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (viewModel.isAdmin) "Unlimited queries (Admin Access)" else "${10 - viewModel.queryCount} / 10 queries remaining today",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (activeCitationToShow != null) {
            val title = activeCitationToShow!!.first
            val rawContent = activeCitationToShow!!.second
            Dialog(
                onDismissRequest = { activeCitationToShow = null },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { activeCitationToShow = null },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                // Prevent click propagation from closing the dialog
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF012C1E)),
                        border = BorderStroke(1.3.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    color = GoldPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable { activeCitationToShow = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val parsedSections = remember(rawContent) { parseCitationFullText(rawContent) }
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                parsedSections.forEach { (label, content) ->
                                    if (label == "Arabic") {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF012419), RoundedCornerShape(12.dp))
                                                .border(0.8.dp, GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = content,
                                                color = GoldPrimary,
                                                fontSize = 20.sp,
                                                lineHeight = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.End,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
                                                )
                                            )
                                        }
                                    } else if (label == "Urdu Translation") {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "اردو ترجمہ",
                                                color = GoldPrimary.copy(alpha = 0.7f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = content,
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                textAlign = TextAlign.End,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
                                                )
                                            )
                                        }
                                    } else {
                                        Column {
                                            Text(
                                                text = label.uppercase(),
                                                color = GoldPrimary.copy(alpha = 0.7f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = content,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { activeCitationToShow = null },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Dismiss",
                                    color = OnGoldText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (messageToReport != null) {
            val msg = messageToReport!!
            var selectedTag by remember { mutableStateOf("Incorrect translation") }
            var customDetail by remember { mutableStateOf("") }
            
            val tagsList = listOf(
                "Incorrect translation",
                "Historical inaccuracy",
                "Unclear explanation",
                "Formatting issue",
                "Other suggestion/feedback"
            )

            Dialog(
                onDismissRequest = { messageToReport = null },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { messageToReport = null },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {},
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF012C1E)),
                        border = BorderStroke(1.3.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Suggest Improvement",
                                    color = GoldPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable { messageToReport = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Select a label that best describes your feedback:",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                tagsList.forEach { tag ->
                                    val isSelectedTag = selectedTag == tag
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelectedTag) GoldPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelectedTag) GoldPrimary else Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedTag = tag }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelectedTag,
                                            onClick = { selectedTag = tag },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = GoldPrimary,
                                                unselectedColor = Color.White.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = tag,
                                            color = if (isSelectedTag) GoldPrimary else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Additional Details (Optional):",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            OutlinedTextField(
                                value = customDetail,
                                onValueChange = { customDetail = it },
                                placeholder = { Text("Write details or suggest corrections...", color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                    cursorColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { messageToReport = null },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Discard", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        val index = messages.indexOf(msg)
                                        val queryText = if (index > 0) messages[index - 1].text else "General Question"
                                        val finalReportMsg = if (customDetail.isNotBlank()) "$selectedTag: $customDetail" else selectedTag
                                        
                                        viewModel.submitAiFeedback(
                                            messageId = msg.id,
                                            query = queryText,
                                            responseText = msg.text,
                                            rating = msg.rating,
                                            reportMessage = finalReportMsg
                                        )
                                        messageToReport = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            tint = OnGoldText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Submit", color = OnGoldText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
*/

@Composable
fun OfflineDownloadDialog(
    viewModel: com.example.viewmodel.TaqwaViewModel,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var showDownloadAllConfirm by remember { mutableStateOf(false) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var selectedDownloadReciterId by remember { mutableStateOf(viewModel.selectedReciterId) }
    var surahToDownload by remember { mutableStateOf<com.example.data.Surah?>(null) }
    var individualDownloadReciterId by remember { mutableStateOf(viewModel.selectedReciterId) }

    val surahs = com.example.data.IslamicData.surahs
    val downloaded = viewModel.downloadedSurahIds
    val downloading = viewModel.downloadingSurahIds
    val isDownloadingAll = viewModel.isDownloadingAll
    val downloadAllProgress = viewModel.downloadAllProgress
    val downloadAllStatusText = viewModel.downloadAllStatusText

    val reciters = listOf(
        Pair(7, "Mishary Alafasy"),
        Pair(3, "Abdul Rahman Al-Sudais"),
        Pair(6, "Maher Al-Muaiqly"),
        Pair(12, "Yasser Al-Dosari"),
        Pair(2, "Abdul Basit (Classic)"),
        Pair(1, "Abu Bakr Al-Shatri")
    )
    val currentReciterName = reciters.find { it.first == viewModel.selectedReciterId }?.second ?: "Mishary Alafasy"

    val filteredSurahs = remember(searchQuery, selectedFilter, downloaded, downloading) {
        surahs.filter { surah ->
            val matchesQuery = if (searchQuery.isEmpty()) true else com.example.data.IslamicData.matchesSurah(surah, searchQuery)
            val matchesFilter = when (selectedFilter) {
                "downloaded" -> downloaded.contains(surah.id)
                "downloading" -> downloading.contains(surah.id)
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = EmeraldBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldPrimary)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OnGoldText),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Offline Downloader",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Offline Quran Manager", color = OnGoldText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Reciter: $currentReciterName", color = OnGoldText.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OnGoldText)
                    }
                }

                // Info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Offline Mode Information",
                                tint = GoldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Standalone Offline Mode",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Download surahs locally for reading and word-by-word playback offline. ${downloaded.size} of 114 saved.",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Download All / Delete All Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isPaused = viewModel.isDownloadAllPaused
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldPrimary.copy(alpha = if (isDownloadingAll) 0.25f else 1f))
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isDownloadingAll) {
                                            if (isPaused) {
                                                viewModel.resumeDownloadAll()
                                            } else {
                                                viewModel.pauseDownloadAll()
                                            }
                                        } else {
                                            showDownloadAllConfirm = true
                                        }
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (isDownloadingAll) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(downloadAllProgress.coerceIn(0f, 1f))
                                            .background(GoldPrimary.copy(alpha = 0.4f))
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (!isDownloadingAll) Icons.Default.Download else if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = null,
                                            tint = if (isDownloadingAll) GoldPrimary else OnGoldText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (!isDownloadingAll) {
                                                "Download All"
                                            } else if (isPaused) {
                                                "Paused • Tap to Resume (${(downloadAllProgress * 100).toInt()}%)"
                                            } else {
                                                "$downloadAllStatusText (${(downloadAllProgress * 100).toInt()}%)"
                                            },
                                            color = if (isDownloadingAll) Color.White else OnGoldText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            if (isDownloadingAll) {
                                Button(
                                    onClick = { viewModel.cancelDownloadAll() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                                    modifier = Modifier.height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cancel", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { showDeleteAllConfirm = true },
                                    enabled = downloaded.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete All", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Search Box TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("surah_download_search"),
                    placeholder = { Text("Search by name of surah...", color = GoldPrimary.copy(alpha = 0.4f)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = GoldPrimary.copy(alpha = 0.5f))
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = EmeraldCard,
                        unfocusedContainerColor = EmeraldCard,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Filter Quick Selection Box Pills Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf(
                        "all" to "All",
                        "downloaded" to "Saved (${downloaded.size})",
                        "downloading" to "Downloading (${downloading.size})"
                    )

                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GoldPrimary else EmeraldCard)
                                .clickable { selectedFilter = filter.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.second,
                                color = if (isSelected) OnGoldText else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Surah listing LazyColumn block
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (filteredSurahs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No surah matching this filter.",
                                    color = TextGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        items(filteredSurahs) { surah ->
                            val isDownloaded = downloaded.contains(surah.id)
                            val isDownloading = downloading.contains(surah.id)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = surah.id.toString(),
                                                color = GoldPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = surah.name,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${surah.nameArabic})",
                                                    color = GoldPrimary.copy(alpha = 0.7f),
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Text(
                                                text = "${surah.versesCount} verses • ${surah.revelationType}",
                                                color = TextGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isDownloaded) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(GoldPrimary.copy(alpha = 0.12f))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Downloaded Status Success",
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "SAVED",
                                                    color = GoldPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteSurahOffline(surah.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Surah",
                                                    tint = Color.Red.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else if (isDownloading) {
                                            val progress = viewModel.downloadProgress[surah.id] ?: 0f
                                            val sizeStr = viewModel.downloadSizeStatus[surah.id] ?: "0.00 MB"
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        progress = { progress },
                                                        modifier = Modifier.size(34.dp),
                                                        color = GoldPrimary,
                                                        strokeWidth = 2.5.dp,
                                                        trackColor = GoldPrimary.copy(alpha = 0.15f)
                                                    )
                                                    Text(
                                                        text = "${(progress * 100).toInt()}%",
                                                        color = GoldPrimary,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                     surahToDownload = surah
                                                     individualDownloadReciterId = viewModel.selectedReciterId
                                                 },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Begin Download",
                                                    tint = OnGoldText,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "GET",
                                                    color = OnGoldText,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDownloadAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDownloadAllConfirm = false },
            containerColor = EmeraldCard,
            title = { Text("Download All Surahs", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Reciter for offline download:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    reciters.forEach { reciter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedDownloadReciterId == reciter.first) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedDownloadReciterId = reciter.first }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(reciter.second, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            if (selectedDownloadReciterId == reciter.first) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("This will download all 114 Surahs offline. Ensure you have sufficient storage space and stable internet connection.", color = TextGray, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDownloadAllConfirm = false
                        viewModel.downloadAllSurahs(selectedDownloadReciterId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Start Download All", color = OnGoldText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadAllConfirm = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    if (surahToDownload != null) {
        val s = surahToDownload!!
        AlertDialog(
            onDismissRequest = { surahToDownload = null },
            containerColor = EmeraldCard,
            title = { Text("Download Surah ${s.name}", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Reciter for Surah ${s.id} (${s.name}):", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    reciters.forEach { reciter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (individualDownloadReciterId == reciter.first) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { individualDownloadReciterId = reciter.first }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(reciter.second, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            if (individualDownloadReciterId == reciter.first) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetSurah = s.id
                        val recId = individualDownloadReciterId
                        surahToDownload = null
                        viewModel.downloadSurahOfflineWithReciter(targetSurah, recId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Download", color = OnGoldText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { surahToDownload = null }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            containerColor = EmeraldCard,
            title = { Text("Delete All Downloaded Surahs", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete all saved offline surahs and audio files? This action cannot be undone.", color = Color.White, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllConfirm = false
                        viewModel.deleteAllDownloadedSurahs()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

// ==========================================
// 12. LOCATION PERMISSION HANDLER
// ==========================================
@Composable
fun LocationPermissionCard(viewModel: TaqwaViewModel, onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            viewModel.hasLocationPermission = true
            try {
                val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        viewModel.updateCoordinates(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) { }
            onPermissionGranted()
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Access Needed",
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Location Access Disclosure",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "TaqwaHub requires device location permission to calculate accurate local Islamic prayer times (Fajr, Dhuhr, Asr, Maghrib, Isha) and determine the exact Qibla compass direction for your current position. Location data is processed locally on your device.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.hasLocationPermission = true
                        viewModel.fetchPrayerTimes()
                    },
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Use Default", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { 
                        launcher.launch(arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Allow Access", color = OnGoldText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SurahCompletionSection(
    viewModel: TaqwaViewModel,
    surahId: Int,
    totalVerses: Int,
    context: android.content.Context
) {
    val progressMap = viewModel.surahProgressMap
    val progress = progressMap[surahId] ?: TaqwaViewModel.SurahProgress(
        surahId = surahId,
        turnsCount = 0,
        visitedVerses = emptySet(),
        accumulatedTimeSeconds = 0L,
        isCompleted = false
    )

    val stats by viewModel.stats.collectAsState(initial = com.example.data.room.UserStatsEntity())
    val dbCompletedSet = remember(stats.completedSurahs) {
        stats.completedSurahs.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }
    // Cross-check: is it marked completed in SharedPreferences or the Room DB
    val isCompleted = progress.isCompleted || dbCompletedSet.contains(surahId)

    val visitedCount = progress.visitedVerses.size.coerceAtMost(totalVerses)
    val percentage = if (totalVerses > 0) {
        (visitedCount.toFloat() / totalVerses.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val minutes = progress.accumulatedTimeSeconds / 60
    val seconds = progress.accumulatedTimeSeconds % 60
    val timeFormatted = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) GoldPrimary.copy(alpha = 0.08f) else EmeraldCard
        ),
        border = BorderStroke(
            1.dp,
            if (isCompleted) GoldPrimary.copy(alpha = 0.4f) else GoldPrimary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) GoldPrimary else EmeraldMedium.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isCompleted) EmeraldDark else GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Recitation Tracking Analytics",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isCompleted) "Status: Recitation Completed" else "Status: Automated Verification",
                        color = if (isCompleted) GoldPrimary else TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Active Turns",
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${progress.turnsCount} turns",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Time Spent",
                        color = TextGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormatted,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verses Recited / Listened",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$visitedCount / $totalVerses (${(percentage * 100).toInt()}%)",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = percentage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GoldPrimary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // Information/Status card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isCompleted) GoldPrimary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = if (isCompleted) {
                        "✨ Auto-Verified! You spent the required recitation time and completed all verses. This Surah is recorded towards your Hifz Al-Quran milestones."
                    } else {
                        "📖 In Progress: Read or play audio. The background process automatically validates your recitation turn-by-turn. No manual clicks required!"
                    },
                    color = if (isCompleted) GoldPrimary else Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ProfileTrophyBadge(
    placeName: String,
    count: Int,
    color: Color,
    tintColor: Color,
    placeNum: Int,
    modifier: Modifier
) {
    val isEarned = count > 0

    val infiniteTransition = rememberInfiniteTransition()

    // Premium shining sweep progress
    val shineProgress by if (isEarned) {
        when (placeNum) {
            1 -> infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 4000
                        0.0f at 0 with LinearEasing
                        1.0f at 1200 with LinearEasing
                        1.0f at 4000 with LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            2 -> infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 5000
                        0.0f at 0 with LinearEasing
                        1.0f at 1500 with LinearEasing
                        1.0f at 5000 with LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
            else -> infiniteTransition.animateFloat(
                initialValue = 0.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 6000
                        0.0f at 0 with LinearEasing
                        1.0f at 1800 with LinearEasing
                        1.0f at 6000 with LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    } else {
        remember { mutableStateOf(0f) }
    }

    val glowAlpha by if (isEarned) {
        when (placeNum) {
            1 -> infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            2 -> infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            else -> remember { mutableStateOf(1.0f) }
        }
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isEarned) {
                        color.copy(alpha = 0.12f)
                    } else {
                        Color.White.copy(alpha = 0.03f)
                    }
                )
                .border(
                    width = if (isEarned) 2.dp else 1.dp,
                    color = if (isEarned) color.copy(alpha = glowAlpha) else Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
                .drawWithContent {
                    drawContent()
                    if (isEarned) {
                        val width = size.width
                        val height = size.height
                        
                        val shineColor = Color.White.copy(alpha = if (placeNum == 1) 0.85f else if (placeNum == 2) 0.55f else 0.35f)
                        val beamWidth = width * (if (placeNum == 1) 0.35f else if (placeNum == 2) 0.25f else 0.18f)
                        
                        val maxDimension = if (width > height) width else height
                        val sweepRange = maxDimension * 2.5f + beamWidth * 3f
                        val centerPos = -maxDimension - beamWidth + sweepRange * shineProgress
                        
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                shineColor.copy(alpha = 0.03f),
                                shineColor,
                                shineColor.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            start = Offset(centerPos - beamWidth, centerPos - beamWidth),
                            end = Offset(centerPos + beamWidth, centerPos + beamWidth)
                        )
                        
                        drawRect(
                            brush = brush,
                            size = size
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isEarned && placeNum == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.15f), Color.Transparent),
                                radius = 90f
                            )
                        )
                )
            }
            
            Icon(
                imageVector = when (placeNum) {
                    1 -> Icons.Default.EmojiEvents
                    2 -> Icons.Default.MilitaryTech
                    else -> Icons.Default.WorkspacePremium
                },
                contentDescription = null,
                tint = if (isEarned) tintColor else TextGray.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = placeName,
            color = if (isEarned) Color.White else TextGray.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
        
        Text(
            text = if (isEarned) "${count}x Earned" else "Locked",
            color = if (isEarned) GoldPrimary else TextGray.copy(alpha = 0.4f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    return try {
        val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun WhatsAppPlaceholderAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F2C20))
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            drawCircle(
                color = Color(0xFF428063),
                radius = width * 0.18f,
                center = androidx.compose.ui.geometry.Offset(width / 2f, height * 0.38f)
            )
            
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(width * 0.15f, height)
                quadraticTo(
                    width * 0.15f, height * 0.7f,
                    width * 0.3f, height * 0.68f
                )
                lineTo(width * 0.7f, height * 0.68f)
                quadraticTo(
                    width * 0.85f, height * 0.7f,
                    width * 0.85f, height
                )
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFF428063)
            )
        }
    }
}

private fun executePlayVerseAtIndex(
    index: Int,
    activeVerses: List<com.example.data.api.QuranVerse>,
    surah: com.example.data.Surah,
    viewModel: TaqwaViewModel,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSetContinuousPlaying: (Boolean) -> Unit,
    onSetCurrentPlayingVerseIndex: (Int?) -> Unit
) {
    if (index >= 0 && index < activeVerses.size) {
        val verse = activeVerses[index]
        val verseAudio = viewModel.audioOverrides["verse_${verse.verse_key}"] ?: viewModel.activeVerseAudioUrls[verse.verse_key]
        if (verseAudio != null) {
            val isSurahAudioLocked = viewModel.isSurahAudioLocked(surah.id)
            if (isSurahAudioLocked) {
                val msg = viewModel.getSurahAudioBlockedMessage(surah.id)
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                onSetContinuousPlaying(false)
                onSetCurrentPlayingVerseIndex(null)
            } else {
                onSetCurrentPlayingVerseIndex(index)
                viewModel.audioPlayerHelper.playAudio(verseAudio)
                
                val prefetchUrls = mutableListOf<String>()
                for (i in 1..5) {
                    val nextIdx = index + i
                    if (nextIdx < activeVerses.size) {
                        val nv = activeVerses[nextIdx]
                        val nvAudio = viewModel.audioOverrides["verse_${nv.verse_key}"] ?: viewModel.activeVerseAudioUrls[nv.verse_key]
                        if (nvAudio != null) {
                            prefetchUrls.add(nvAudio)
                        }
                    }
                }
                if (prefetchUrls.isNotEmpty()) {
                    viewModel.audioPlayerHelper.prefetch(prefetchUrls)
                }

                coroutineScope.launch {
                    val headerOffset = if (surah.id != 9 && surah.id != 1) 2 else 1
                    listState.animateScrollToItem(index + headerOffset)
                }
            }
        } else {
            val nextIndex = index + 1
            if (nextIndex < activeVerses.size) {
                executePlayVerseAtIndex(
                    nextIndex, activeVerses, surah, viewModel, context, coroutineScope, listState,
                    onSetContinuousPlaying, onSetCurrentPlayingVerseIndex
                )
            } else {
                onSetContinuousPlaying(false)
                onSetCurrentPlayingVerseIndex(null)
            }
        }
    } else {
        onSetContinuousPlaying(false)
        onSetCurrentPlayingVerseIndex(null)
    }
}

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        GoldPrimary.copy(alpha = 0.06f),
        GoldPrimary.copy(alpha = 0.22f),
        GoldPrimary.copy(alpha = 0.06f)
    )
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation.value - 300f, y = translateAnimation.value - 300f),
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

@Composable
fun VerseSkeletonCard() {
    val brush = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp, 22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(brush))
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(brush))
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(brush))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun SurahListSkeletonCard() {
    val brush = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun HadithSkeletonCard() {
    val brush = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun DuaSkeletonCard() {
    val brush = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun OfflineErrorCard(
    modifier: Modifier = Modifier,
    title: String = "No Internet Connection",
    message: String = "Please check your network connection and try again.",
    icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Default.WifiOff,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Offline Error",
                    tint = GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = message,
                color = TextGray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 19.sp
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = OnGoldText),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("offline_retry_button")
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry Connection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


