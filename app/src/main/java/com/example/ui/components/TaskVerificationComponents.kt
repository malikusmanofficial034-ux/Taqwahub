package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicData
import com.example.data.room.BookmarkEntity
import com.example.data.room.TaskEntity
import com.example.ui.theme.*
import com.example.viewmodel.TaqwaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Robust, crash-proof Task Action Engine to route task clicks safely.
 */
object TaskActionEngine {

    fun safeNavigateTask(
        task: TaskEntity,
        context: Context,
        viewModel: TaqwaViewModel,
        bookmarks: List<BookmarkEntity>,
        onNavigate: (String) -> Unit,
        onShowDetailSheet: ((TaskEntity) -> Unit)? = null
    ) {
        try {
            val route = task.actionRoute.trim()
            val routeLower = route.lowercase()

            // Web link redirection
            if (routeLower.startsWith("http://") || routeLower.startsWith("https://")) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(route)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("TaskActionEngine", "Failed to open external link: ${task.actionRoute}", e)
                    Toast.makeText(context, "Could not open external link.", Toast.LENGTH_SHORT).show()
                }
                return
            }

            when (routeLower) {
                "quran" -> {
                    prepareQuranNavigation(task, viewModel, bookmarks)
                    onNavigate("quran")
                }
                "tasbeeh", "dhikr" -> {
                    prepareTasbeehNavigation(task, viewModel)
                    onNavigate("tasbeeh")
                }
                "hadith", "sunnah", "hadiths" -> {
                    viewModel.activeHadithBookKey = "bukhari"
                    onNavigate("hadith")
                }
                "dua", "duas", "supplication" -> {
                    prepareDuaNavigation(task, viewModel)
                    onNavigate("dua")
                }
                "names", "99_names", "asmaulhusna" -> {
                    onNavigate("names")
                }
                "donate", "charity", "sadaqah" -> {
                    onNavigate("donate")
                }
                "dashboard", "home" -> {
                    onNavigate("dashboard")
                }
                else -> {
                    if (route.isNotEmpty()) {
                        onNavigate(route)
                    } else if (onShowDetailSheet != null) {
                        onShowDetailSheet(task)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TaskActionEngine", "Safe navigation failed for task: ${task.title}", e)
            Toast.makeText(context, "Navigating to: ${task.title}", Toast.LENGTH_SHORT).show()
            onNavigate("dashboard")
        }
    }

    private fun prepareQuranNavigation(
        task: TaskEntity,
        viewModel: TaqwaViewModel,
        bookmarks: List<BookmarkEntity>
    ) {
        try {
            val titleLower = task.title.lowercase()
            when {
                titleLower.contains("mulk") -> {
                    val bookmark = bookmarks.find { it.surahNumber == 67 }
                    if (bookmark != null) {
                        viewModel.isContinuousFlowMode = bookmark.isFlowMode
                        viewModel.selectChapter(67)
                        viewModel.requestedScrollVerseId = bookmark.verseNumber
                    } else {
                        viewModel.isContinuousFlowMode = true
                        viewModel.selectChapter(67)
                    }
                }
                titleLower.contains("kahf") -> {
                    viewModel.isContinuousFlowMode = true
                    viewModel.selectChapter(18)
                }
                titleLower.contains("yaseen") || titleLower.contains("yasin") -> {
                    viewModel.isContinuousFlowMode = true
                    viewModel.selectChapter(36)
                }
                titleLower.contains("baqarah") -> {
                    viewModel.isContinuousFlowMode = true
                    viewModel.selectChapter(2)
                }
                else -> {
                    if (viewModel.selectedSurah == null) {
                        val firstBookmark = bookmarks.maxByOrNull { it.timestamp }
                        if (firstBookmark != null) {
                            viewModel.selectChapter(firstBookmark.surahNumber)
                            viewModel.requestedScrollVerseId = firstBookmark.verseNumber
                        } else {
                            viewModel.selectChapter(1) // Default to Al-Fatiha
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TaskActionEngine", "Error preparing Quran chapter for task: ${task.title}", e)
            viewModel.selectChapter(1)
        }
    }

    private fun prepareTasbeehNavigation(task: TaskEntity, viewModel: TaqwaViewModel) {
        try {
            val titleLower = task.title.lowercase()
            viewModel.selectedTasbeehId = when {
                titleLower.contains("astaghfar") || titleLower.contains("astaghfirullah") || titleLower.contains("forgiveness") -> 4
                titleLower.contains("subhanallah") || titleLower.contains("subhan allah") -> 1
                titleLower.contains("alhamdulillah") || titleLower.contains("alhamdu lillah") -> 2
                titleLower.contains("allahu akbar") || titleLower.contains("allah u akbar") -> 3
                titleLower.contains("durood") || titleLower.contains("salawat") || titleLower.contains("blessings") -> 5
                titleLower.contains("tahlil") || titleLower.contains("kalimah") || titleLower.contains("la ilaha") -> 0
                else -> 0
            }
        } catch (e: Exception) {
            Log.e("TaskActionEngine", "Error selecting Tasbeeh preset", e)
            viewModel.selectedTasbeehId = 0
        }
    }

    private fun prepareDuaNavigation(task: TaskEntity, viewModel: TaqwaViewModel) {
        try {
            val titleLower = task.title.lowercase()
            viewModel.duaSearchQuery = when {
                titleLower.contains("morning") -> "morning"
                titleLower.contains("evening") -> "evening"
                titleLower.contains("sleep") || titleLower.contains("sleeping") -> "sleep"
                titleLower.contains("travel") -> "travel"
                titleLower.contains("forgive") || titleLower.contains("forgiveness") -> "forgiveness"
                titleLower.contains("guidance") -> "guidance"
                else -> ""
            }
        } catch (e: Exception) {
            Log.e("TaskActionEngine", "Error preparing Dua search", e)
            viewModel.duaSearchQuery = ""
        }
    }
}

/**
 * Unified, crash-proof Task Card Item for Dashboards and Tasks Lists.
 */
@Composable
fun TaskCardItem(
    task: TaskEntity,
    viewModel: TaqwaViewModel,
    bookmarks: List<BookmarkEntity>,
    onNavigate: (String) -> Unit,
    onOpenDetailSheet: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tickerState by viewModel.prayerTicker.collectAsState()
    val timing = remember(task.title, task.completed, tickerState) {
        try {
            viewModel.getTaskTimingStatus(task.title, task.completed)
        } catch (e: Exception) {
            com.example.viewmodel.TaskTimingStatus(isLockedAdvance = false, isMissed = false)
        }
    }

    val isPrayer = task.category.equals("Salah", ignoreCase = true) || task.title in listOf(
        "Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz",
        "Offer Maghrib Namaz", "Offer Isha Namaz", "Offer Jummah Prayer"
    )
    val isQuranTask = task.category.equals("Quran", ignoreCase = true) ||
            task.title.contains("Quran", ignoreCase = true) ||
            task.title.contains("Surah", ignoreCase = true)

    val isLockedAdvance = isPrayer && timing.isLockedAdvance
    val isMissed = isPrayer && timing.isMissed
    val isCharityOrDeed = task.category.equals("Deeds", ignoreCase = true) ||
            task.id.startsWith("manual_charity") ||
            task.title.contains("Charity", ignoreCase = true) ||
            task.title.contains("Sadaqah", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                // Tapping anywhere on the card opens the rich, verified task detail sheet safely
                onOpenDetailSheet(task)
            }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isMissed && !task.completed -> Color(0xFFEF4444).copy(alpha = 0.08f)
                isLockedAdvance && !task.completed -> EmeraldCard.copy(alpha = 0.45f)
                task.completed -> EmeraldCard.copy(alpha = 0.7f)
                else -> EmeraldCard
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isMissed && !task.completed -> Color(0xFFEF4444).copy(alpha = 0.3f)
                task.completed -> GoldPrimary.copy(alpha = 0.35f)
                isLockedAdvance -> Color.White.copy(alpha = 0.06f)
                else -> GoldPrimary.copy(alpha = 0.12f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Status Icon & Title
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Checkbox / State indicator
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    task.completed -> GoldPrimary
                                    isLockedAdvance -> Color.White.copy(alpha = 0.05f)
                                    isMissed -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                    else -> Color.White.copy(alpha = 0.06f)
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    task.completed -> GoldPrimary
                                    isLockedAdvance -> Color.White.copy(alpha = 0.2f)
                                    isMissed -> Color(0xFFEF4444).copy(alpha = 0.5f)
                                    else -> GoldPrimary.copy(alpha = 0.6f)
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                if (isLockedAdvance) {
                                    Toast.makeText(context, "🔒 Locked until ${timing.startStr}", Toast.LENGTH_SHORT).show()
                                } else if ((isPrayer || isQuranTask) && task.completed) {
                                    Toast.makeText(context, "✓ Completed and securely logged for today.", Toast.LENGTH_SHORT).show()
                                } else {
                                    onOpenDetailSheet(task)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            task.completed -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = OnGoldText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            isLockedAdvance -> {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked until prayer time",
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            isMissed -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Missed prayer window",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Tap to verify and complete",
                                    tint = GoldPrimary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = task.title,
                                color = if (task.completed) TextGray else if (isLockedAdvance) Color.White.copy(alpha = 0.6f) else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (task.tag.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (task.tag) {
                                                "OBLIGATORY" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                                "HOT" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                "RECOMMENDED" -> Color(0xFFFBBF24).copy(alpha = 0.15f)
                                                "TIMER" -> Color(0xFF06B6D4).copy(alpha = 0.15f)
                                                "AUTO", "AUTOMATIC" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                                else -> Color.White.copy(alpha = 0.1f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.tag,
                                        color = when (task.tag) {
                                            "OBLIGATORY" -> Color(0xFF34D399)
                                            "HOT" -> Color(0xFFF87171)
                                            "RECOMMENDED" -> Color(0xFFFBBF24)
                                            "TIMER" -> Color(0xFF22D3EE)
                                            "AUTO", "AUTOMATIC" -> Color(0xFFA78BFA)
                                            else -> Color.White
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        if (isLockedAdvance) {
                            Text(
                                text = "Begins at ${timing.startStr}",
                                color = GoldPrimary.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        } else if (isMissed && !task.completed) {
                            Text(
                                text = "Window passed (${timing.endStr}) • Tap to record Qaza",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (task.description.isNotEmpty() && !task.completed) {
                            Text(
                                text = task.description,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right side: XP Badge & Quick Action Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.points > 0) {
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "+${task.points} XP",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = GoldPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Real-time verification progress bar for automatic & counter tasks
            if (task.isAuto && task.autoTarget > 0 && !task.completed && !isLockedAdvance) {
                val progressFraction = (task.autoProgress.toFloat() / task.autoTarget.toFloat()).coerceIn(0f, 1f)
                val progressLabel = when (task.autoType) {
                    "SURAH" -> {
                        val progressMinutes = task.autoProgress / 60
                        val progressSeconds = task.autoProgress % 60
                        val targetMinutes = task.autoTarget / 60
                        val targetSeconds = task.autoTarget % 60
                        val curStr = if (progressMinutes > 0) "${progressMinutes}m" else "${progressSeconds}s"
                        val tgtStr = if (targetMinutes > 0) "${targetMinutes}m" else "${targetSeconds}s"
                        "$curStr / $tgtStr read"
                    }
                    else -> "${task.autoProgress} / ${task.autoTarget}"
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = progressLabel,
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = GoldPrimary,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

/**
 * Interactive, robust Task Verification Bottom Sheet.
 * Provides intentional confirmation, time-window gating, interactive focus timers, and direct action routing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskVerificationBottomSheet(
    task: TaskEntity?,
    viewModel: TaqwaViewModel,
    bookmarks: List<BookmarkEntity>,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    if (task == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tickerState by viewModel.prayerTicker.collectAsState()

    val timing = remember(task.title, task.completed, tickerState) {
        try {
            viewModel.getTaskTimingStatus(task.title, task.completed)
        } catch (e: Exception) {
            com.example.viewmodel.TaskTimingStatus(isLockedAdvance = false, isMissed = false)
        }
    }

    val isPrayer = task.category.equals("Salah", ignoreCase = true) || task.title in listOf(
        "Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz",
        "Offer Maghrib Namaz", "Offer Isha Namaz", "Offer Jummah Prayer"
    )
    val isQuranTask = task.category.equals("Quran", ignoreCase = true) ||
            task.title.contains("Quran", ignoreCase = true) ||
            task.title.contains("Surah", ignoreCase = true)
    val isTasbeehTask = task.category.equals("Dhikr", ignoreCase = true) ||
            task.title.contains("Tasbeeh", ignoreCase = true) ||
            task.title.contains("Astaghfirullah", ignoreCase = true) ||
            task.title.contains("SubhanAllah", ignoreCase = true) ||
            task.title.contains("Alhamdulillah", ignoreCase = true) ||
            task.title.contains("Allahu Akbar", ignoreCase = true)
    val isKnowledgeTask = task.category.equals("Knowledge", ignoreCase = true) ||
            task.category.equals("Supplication", ignoreCase = true) ||
            task.title.contains("Hadith", ignoreCase = true) ||
            task.title.contains("Dua", ignoreCase = true) ||
            task.title.contains("Names", ignoreCase = true)
    val isDeedOrCharity = task.category.equals("Deeds", ignoreCase = true) ||
            task.id.startsWith("manual_charity") ||
            task.title.contains("Charity", ignoreCase = true) ||
            task.title.contains("Sadaqah", ignoreCase = true)
    val isRewardAdTask = task.id == "task_daily_support_ad" || task.actionRoute == "reward_ad"

    val isLockedAdvance = isPrayer && timing.isLockedAdvance
    val isMissed = isPrayer && timing.isMissed

    // Local intention checkbox for self-reported deeds
    var intentionConfirmed by remember { mutableStateOf(false) }

    // Focus Timer state if the task has a duration/timer
    var isTimerRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableStateOf(if (task.timerSeconds > 0) task.timerSeconds else 60) }

    LaunchedEffect(isTimerRunning, secondsRemaining) {
        if (isTimerRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
            if (secondsRemaining == 0) {
                isTimerRunning = false
                viewModel.toggleMainTask(task.id, true)
                Toast.makeText(context, "🎉 Challenge Completed: ${task.title}! +${task.points} XP", Toast.LENGTH_LONG).show()
                coroutineScope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = EmeraldDark,
        tonalElevation = 16.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = GoldPrimary.copy(alpha = 0.5f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Category Pill, XP Badge & Title
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
                            .background(GoldPrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.category.uppercase(),
                            color = OnGoldText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (task.tag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = task.tag,
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (task.points > 0) {
                    Box(
                        modifier = Modifier
                            .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${task.points} XP",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Task Title
            Text(
                text = task.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Description / Spiritual Benefit
            if (task.description.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Spiritual Context",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = task.description,
                            color = OnEmeraldText.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Dynamic Verification Status / Interactive Controller
            when {
                // Task is already completed
                task.completed -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Completed & Verified Today",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Alhamdulillah! Reward of +${task.points} XP is secured in your streak.",
                                    color = OnEmeraldText.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Salah - Locked in advance
                isLockedAdvance -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Prayer Window Locked",
                                    color = GoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "This prayer cannot be marked in advance. The valid prayer window begins at ${timing.startStr}.",
                                color = TextGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Salah - Window passed (Missed / Qaza)
                isMissed -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1212)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Missed",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Prayer Window Passed (${timing.endStr})",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "The scheduled time for this prayer has passed. If you performed it or intend to offer it as Qaza with sincere repentance, you may record it below.",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { intentionConfirmed = !intentionConfirmed },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = intentionConfirmed,
                                    onCheckedChange = { intentionConfirmed = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GoldPrimary,
                                        uncheckedColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )
                                Text(
                                    text = "I have offered / completed this prayer with sincere intention",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.toggleMainTask(task.id, true)
                                    Toast.makeText(context, "✓ Recorded ${task.title}. May Allah accept your prayer.", Toast.LENGTH_SHORT).show()
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                },
                                enabled = intentionConfirmed,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                Text("Record Qaza / Completed", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Salah - Currently active window
                isPrayer -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Active Prayer Window",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Time window: ${timing.startStr} — ${timing.endStr}. Offer your prayer in Jama'at or at home with tranquility (Khushoo).",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { intentionConfirmed = !intentionConfirmed },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = intentionConfirmed,
                                    onCheckedChange = { intentionConfirmed = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GoldPrimary,
                                        uncheckedColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )
                                Text(
                                    text = "I have offered this prayer (Alhamdulillah)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.toggleMainTask(task.id, true)
                                    Toast.makeText(context, "🎉 ${task.title} Completed! +${task.points} XP Earned!", Toast.LENGTH_SHORT).show()
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                },
                                enabled = intentionConfirmed,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                Text("Mark as Prayed (+${task.points} XP)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Quran Tasks - Progress & Launcher
                isQuranTask -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (task.autoTarget > 0) {
                                val progressFraction = (task.autoProgress.toFloat() / task.autoTarget.toFloat()).coerceIn(0f, 1f)
                                val curMin = task.autoProgress / 60
                                val curSec = task.autoProgress % 60
                                val tgtMin = task.autoTarget / 60
                                val tgtSec = task.autoTarget % 60
                                val curStr = if (curMin > 0) "${curMin}m ${curSec}s" else "${curSec}s"
                                val tgtStr = if (tgtMin > 0) "${tgtMin}m ${tgtSec}s" else "${tgtSec}s"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Reading Verification", color = TextGray, fontSize = 12.sp)
                                    Text("$curStr / $tgtStr", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = GoldPrimary,
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }

                            Text(
                                text = "Read attentively with translation. The app automatically verifies your reading time and verse scrolling in real time.",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                        TaskActionEngine.safeNavigateTask(task, context, viewModel, bookmarks, onNavigate)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open in Quran Reader", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Tasbeeh Tasks - Progress & Direct preset launcher
                isTasbeehTask -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (task.autoTarget > 0) {
                                val progressFraction = (task.autoProgress.toFloat() / task.autoTarget.toFloat()).coerceIn(0f, 1f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Recitation Progress", color = TextGray, fontSize = 12.sp)
                                    Text("${task.autoProgress} / ${task.autoTarget}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = GoldPrimary,
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                        TaskActionEngine.safeNavigateTask(task, context, viewModel, bookmarks, onNavigate)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch Tasbeeh Counter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Knowledge Tasks (Hadith / Duas / 99 Names)
                isKnowledgeTask -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (task.autoTarget > 0) {
                                val progressFraction = (task.autoProgress.toFloat() / task.autoTarget.toFloat()).coerceIn(0f, 1f)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Reflected / Read Items", color = TextGray, fontSize = 12.sp)
                                    Text("${task.autoProgress} / ${task.autoTarget}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = GoldPrimary,
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                        TaskActionEngine.safeNavigateTask(task, context, viewModel, bookmarks, onNavigate)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Explore & Learn Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Self-reported Deeds & Sadaqah
                isDeedOrCharity -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { intentionConfirmed = !intentionConfirmed },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = intentionConfirmed,
                                    onCheckedChange = { intentionConfirmed = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GoldPrimary,
                                        uncheckedColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )
                                Text(
                                    text = "I have performed this noble deed today for the sake of Allah",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.toggleMainTask(task.id, true)
                                    Toast.makeText(context, "🎉 Deed Logged! +${task.points} XP Earned!", Toast.LENGTH_SHORT).show()
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                },
                                enabled = intentionConfirmed,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                Text("Confirm Deed (+${task.points} XP)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Rewarded Ad Support Task
                isRewardAdTask -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Support Taqwa Development",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your support helps keep our servers running and services completely free for everyone. Watch a short video ad to claim your +110 XP daily bonus!",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            var isWatchingAd by remember { mutableStateOf(false) }

                            Button(
                                onClick = {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        isWatchingAd = true
                                        viewModel.watchAdForDailyXp(
                                            activity = activity,
                                            onRewardEarned = {
                                                isWatchingAd = false
                                                Toast.makeText(context, "🎉 Support Reward Earned! +110 XP", Toast.LENGTH_LONG).show()
                                                coroutineScope.launch {
                                                    sheetState.hide()
                                                    onDismiss()
                                                }
                                            },
                                            onFailure = { error ->
                                                isWatchingAd = false
                                                Toast.makeText(context, "Support Ad: $error", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        Toast.makeText(context, "Unable to load ad helper.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isWatchingAd,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = OnGoldText
                                )
                            ) {
                                if (isWatchingAd) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnGoldText, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Loading Sponsor...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Watch & Claim Bonus (+110 XP)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Timed Challenge or Generic Custom Task
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (task.timerSeconds > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Focus Countdown Timer", color = TextGray, fontSize = 12.sp)
                                    Text(
                                        text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60),
                                        color = GoldPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Button(
                                    onClick = { isTimerRunning = !isTimerRunning },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isTimerRunning) Color(0xFFEF4444) else GoldPrimary,
                                        contentColor = if (isTimerRunning) Color.White else OnGoldText
                                    )
                                ) {
                                    Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isTimerRunning) "Pause Timer" else "Start Focus Challenge", fontWeight = FontWeight.Bold)
                                }
                            } else if (task.actionRoute.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            onDismiss()
                                            TaskActionEngine.safeNavigateTask(task, context, viewModel, bookmarks, onNavigate)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = OnGoldText
                                    )
                                ) {
                                    Text("Open Task Activity", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.toggleMainTask(task.id, true)
                                        Toast.makeText(context, "🎉 Task Completed! +${task.points} XP Earned!", Toast.LENGTH_SHORT).show()
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = OnGoldText
                                    )
                                ) {
                                    Text("Mark as Completed (+${task.points} XP)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
