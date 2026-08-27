package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.*
import com.example.data.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
<<<<<<< HEAD
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaqwaRepository(val taqwaDao: TaqwaDao) {

    // OkHttp Client with interceptor and timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
=======
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.io.File

class TaqwaRepository(val taqwaDao: TaqwaDao, cacheDir: File? = null) {

    private val taskMutex = Mutex()

    // OkHttp Client with HTTP caching, connection pooling, and optimized timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .apply {
            if (cacheDir != null) {
                try {
                    val httpCacheDir = File(cacheDir, "http_cache")
                    cache(okhttp3.Cache(httpCacheDir, 50L * 1024L * 1024L)) // 50MB HTTP cache
                } catch (e: Exception) {
                    Log.e("TaqwaRepository", "Error creating HTTP cache: ${e.message}")
                }
            }
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
>>>>>>> 6e834ed (Update Taqwahub)
        })
        .build()

    // Configured Moshi with Kotlin reflection support
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Service initializations
    private val quranApi: QuranApiService = Retrofit.Builder()
        .baseUrl("https://api.quran.com/api/v4/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(QuranApiService::class.java)

    private val aladhanApi: AladhanApiService = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AladhanApiService::class.java)

    private val geminiApi: GeminiApiService = Retrofit.Builder()
        .baseUrl("https://taqwa-api-g8re.vercel.app/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GeminiApiService::class.java)

    private val groqApi: GroqApiService = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/v1/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GroqApiService::class.java)

    private val openRouterApi: OpenRouterApiService = Retrofit.Builder()
        .baseUrl("https://openrouter.ai/api/v1/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenRouterApiService::class.java)

    var cachedPrayerTimes: AladhanTimings? = null
<<<<<<< HEAD
=======
    var cachedTimezone: String? = null
>>>>>>> 6e834ed (Update Taqwahub)

    // Flow properties for reactive UI binds
    val tasksFlow: Flow<List<TaskEntity>> = taqwaDao.getAllTasksFlow()
        .map { list ->
            list.sortedWith(compareBy<TaskEntity> {
                it.id.toLongOrNull() ?: Long.MAX_VALUE
            }.thenBy { it.id })
        }
        .flowOn(Dispatchers.IO)
    val bookmarksFlow: Flow<List<BookmarkEntity>> = taqwaDao.getAllBookmarksFlow().flowOn(Dispatchers.IO)
    val allTimeTasksFlow: Flow<List<AllTimeTaskEntity>> = taqwaDao.getAllTimeTasksFlow().flowOn(Dispatchers.IO)
    val userStatsFlow: Flow<UserStatsEntity?> = taqwaDao.getUserStatsFlow().flowOn(Dispatchers.IO)

    suspend fun getAllTasksDirect(): List<TaskEntity> = withContext(Dispatchers.IO) {
        taqwaDao.getAllTasksDirect()
    }

    // Utility to get current date string in Pakistan Time (PKT, UTC+5)
    fun getPakistanDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Karachi")
        return sdf.format(Date())
    }

    // Initialize Default Tasks if DB is empty
    suspend fun checkAndInitializeTasks() = withContext(Dispatchers.IO) {
        val currentTasks = taqwaDao.getAllTasksDirect()
        if (currentTasks.isEmpty()) {
            val defaults = TaskGenerator.generateTasksForToday()
            taqwaDao.insertAllTasks(defaults)
<<<<<<< HEAD
=======
        } else {
            val defaults = TaskGenerator.generateTasksForToday()
            val missingTasks = defaults.filter { defaultTask ->
                currentTasks.none { it.id == defaultTask.id }
            }
            if (missingTasks.isNotEmpty()) {
                taqwaDao.insertAllTasks(missingTasks)
            }
>>>>>>> 6e834ed (Update Taqwahub)
        }
        recalculateAndSaveStreak()
    }

    // Add a custom task (with specialized auto-generated id)
    suspend fun addCustomTask(title: String, category: String) = withContext(Dispatchers.IO) {
        val id = "custom_${System.currentTimeMillis()}"
        val newTask = TaskEntity(
            id = id,
            title = title,
            completed = false,
            category = category,
            description = "Custom user-defined checklist item.",
            points = 0,
            tag = "CUSTOM",
            timerSeconds = 0,
            isSystemTask = false
        )
        taqwaDao.insertTask(newTask)
    }

    // Add custom task from Admin panel with full control
    suspend fun addAdminTask(
        title: String,
        category: String,
        description: String,
        points: Int,
        tag: String,
        timerSeconds: Int,
        actionRoute: String = ""
    ) = withContext(Dispatchers.IO) {
        val id = "admin_${System.currentTimeMillis()}"
        val routeLower = actionRoute.trim().lowercase()
        val tagUpper = tag.trim().uppercase()
        val isPrayerTask = title.lowercase().contains("namaz") || title.lowercase().contains("prayer") || title.lowercase().contains("salah")
        val isAutoTask = !isPrayerTask && (tagUpper == "AUTO" || tagUpper == "AUTOMATIC" || routeLower.contains("quran") || routeLower.contains("dua") || routeLower.contains("hadith") || routeLower.contains("tasbeeh") || routeLower.contains("names"))
        val computedAutoType = when {
            routeLower.contains("tasbeeh") || (tagUpper == "AUTO" && category.lowercase().contains("dhikr")) -> "TASBEEH"
            routeLower.contains("quran") || (tagUpper == "AUTO" && category.lowercase().contains("quran")) -> "SURAH"
            routeLower.contains("hadith") || (tagUpper == "AUTO" && category.lowercase().contains("hadith")) -> "HADITH"
            routeLower.contains("dua") || (tagUpper == "AUTO" && category.lowercase().contains("dua")) -> "DUA"
            routeLower.contains("names") || (tagUpper == "AUTO" && category.lowercase().contains("names")) -> "99_NAMES"
            else -> ""
        }
        val computedAutoTarget = if (timerSeconds > 0) timerSeconds else when (computedAutoType) {
            "TASBEEH" -> 33
            "SURAH" -> 300 // 5 minutes
            "HADITH" -> 3
            "DUA" -> 3
            "99_NAMES" -> 5
            else -> 0
        }

        val newTask = TaskEntity(
            id = id,
            title = title,
            completed = false,
            category = category,
            description = description,
            points = points,
            tag = tag,
            timerSeconds = if (isAutoTask) 0 else timerSeconds, // timer seconds is only used for manual tasks timers
            isSystemTask = false,
            actionRoute = actionRoute,
            isAuto = isAutoTask,
            autoType = computedAutoType,
            autoTarget = computedAutoTarget
        )
        taqwaDao.insertTask(newTask)
    }

    suspend fun updateAdminTask(
        id: String,
        title: String,
        category: String,
        description: String,
        points: Int,
        tag: String,
        timerSeconds: Int,
        actionRoute: String
    ) = withContext(Dispatchers.IO) {
        val existing = taqwaDao.getAllTasksDirect().find { it.id == id }
        val routeLower = actionRoute.trim().lowercase()
        val tagUpper = tag.trim().uppercase()
        val isPrayerTask = title.lowercase().contains("namaz") || title.lowercase().contains("prayer") || title.lowercase().contains("salah")
        val isAutoTask = !isPrayerTask && (tagUpper == "AUTO" || tagUpper == "AUTOMATIC" || routeLower.contains("quran") || routeLower.contains("dua") || routeLower.contains("hadith") || routeLower.contains("tasbeeh") || routeLower.contains("names"))
        val computedAutoType = when {
            routeLower.contains("tasbeeh") || (tagUpper == "AUTO" && category.lowercase().contains("dhikr")) -> "TASBEEH"
            routeLower.contains("quran") || (tagUpper == "AUTO" && category.lowercase().contains("quran")) -> "SURAH"
            routeLower.contains("hadith") || (tagUpper == "AUTO" && category.lowercase().contains("hadith")) -> "HADITH"
            routeLower.contains("dua") || (tagUpper == "AUTO" && category.lowercase().contains("dua")) -> "DUA"
            routeLower.contains("names") || (tagUpper == "AUTO" && category.lowercase().contains("names")) -> "99_NAMES"
            else -> ""
        }
        val computedAutoTarget = if (timerSeconds > 0) timerSeconds else when (computedAutoType) {
            "TASBEEH" -> 33
            "SURAH" -> 300 // 5 minutes
            "HADITH" -> 3
            "DUA" -> 3
            "99_NAMES" -> 5
            else -> 0
        }

        val updatedTask = if (existing != null) {
            existing.copy(
                title = title,
                category = category,
                description = description,
                points = points,
                tag = tag,
                timerSeconds = if (isAutoTask) 0 else timerSeconds,
                actionRoute = actionRoute,
                isAuto = isAutoTask,
                autoType = computedAutoType,
                autoTarget = if (timerSeconds > 0) timerSeconds else if (isAutoTask && existing.autoTarget > 0) existing.autoTarget else computedAutoTarget
            )
        } else {
            TaskEntity(
                id = id,
                title = title,
                completed = false,
                category = category,
                description = description,
                points = points,
                tag = tag,
                timerSeconds = if (isAutoTask) 0 else timerSeconds,
                isSystemTask = false,
                isAuto = isAutoTask,
                autoType = computedAutoType,
                autoTarget = computedAutoTarget,
                actionRoute = actionRoute
            )
        }
        taqwaDao.insertTask(updatedTask)
    }

    suspend fun incrementSpecificTaskProgress(taskId: String, amount: Int): Boolean = withContext(Dispatchers.IO) {
        val task = taqwaDao.getAllTasksDirect().find { it.id == taskId } ?: return@withContext false
        if (task.completed) return@withContext false
        val newProgress = task.autoProgress + amount
        if (newProgress >= task.autoTarget) {
            // Completed!
            taqwaDao.insertTask(task.copy(autoProgress = task.autoTarget, completed = true))
            
            val todayStr = getPakistanDateString()
            val allTimeId = "${task.id}_${todayStr}"
            val log = AllTimeTaskEntity(
                id = allTimeId,
                taskId = task.id,
                title = task.title,
                category = task.category,
                date = todayStr,
                completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())
            )
            taqwaDao.insertAllTimeTask(log)

            // Add XP
            val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
            taqwaDao.insertUserStats(
                currentStats.copy(
                    totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                    totalXp = currentStats.totalXp + task.points,
                    weeklyXp = currentStats.weeklyXp + task.points
                )
            )
            return@withContext true
        } else {
            taqwaDao.insertTask(task.copy(autoProgress = newProgress))
            return@withContext false
        }
    }

    // Delete a task (including from today's active completions to maintain consistency)
    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        taqwaDao.deleteTaskById(taskId)
        val todayStr = getPakistanDateString()
        taqwaDao.deleteAllTimeTaskById("${taskId}_${todayStr}")
        recalculateAndSaveStreak()
    }

    // Reset default tasks to initial premium spiritual checklist
    suspend fun resetDefaultTasks() = withContext(Dispatchers.IO) {
        taqwaDao.clearTasks()
        val defaults = TaskGenerator.generateTasksForToday()
        taqwaDao.insertAllTasks(defaults)
    }

<<<<<<< HEAD
    // Toggle Task completion and save to stats
    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Boolean = withContext(Dispatchers.IO) {
        val currentTasks = taqwaDao.getAllTasksDirect()
        val task = currentTasks.find { it.id == taskId } ?: return@withContext false

        // Advance check & missed checks enforcement for prayer tasks
        val isPrayerTask = task.title in listOf("Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz", "Offer Maghrib Namaz", "Offer Isha Namaz")
        if (isPrayerTask) {
            val ranges = getPrayerRanges(cachedPrayerTimes)
            val range = ranges.find { it.taskTitle == task.title }
            if (range != null) {
                val now = Date()
                if (now < range.start) {
                    return@withContext false // Locked (Advance)
                }
                if (now > range.end) {
                    return@withContext false // Locked (Missed)
                }
            }
        }

        val updatedTask = task.copy(completed = isCompleted)
        taqwaDao.insertTask(updatedTask)

        val todayStr = getPakistanDateString()
        val allTimeId = "${taskId}_${todayStr}"

        if (isCompleted) {
            // Save to historical completions log
            val log = AllTimeTaskEntity(
                id = allTimeId,
                taskId = taskId,
                title = task.title,
                category = task.category,
                date = todayStr,
                completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())
            )
            taqwaDao.insertAllTimeTask(log)
        } else {
            taqwaDao.deleteAllTimeTaskById(allTimeId)
            taqwaDao.deleteAllTimeTaskById("${taskId}_${todayStr}_MISSED")
        }

        // Update overall total tasks completed count & XP points
        val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
        val newTotalCompleted = if (isCompleted) {
            currentStats.totalTasksCompleted + 1
        } else {
            kotlin.math.max(0, currentStats.totalTasksCompleted - 1)
        }

        val pointsToChange = if (isCompleted) task.points else -task.points
        val newTotalXp = kotlin.math.max(0, currentStats.totalXp + pointsToChange)
        val newWeeklyXp = kotlin.math.max(0, currentStats.weeklyXp + pointsToChange)

        taqwaDao.insertUserStats(
            currentStats.copy(
                totalTasksCompleted = newTotalCompleted,
                totalXp = newTotalXp,
                weeklyXp = newWeeklyXp
            )
        )
        recalculateAndSaveStreak()
        return@withContext true
=======
    // Toggle Task completion and save to stats (100% idempotent, thread-safe & non-blocking)
    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Boolean = withContext(Dispatchers.IO) {
        taskMutex.withLock {
            val currentTasks = taqwaDao.getAllTasksDirect()
            val task = currentTasks.find { it.id == taskId } ?: return@withLock false

            // Strict lock on upcoming prayers: cannot mark before start time
            val isPrayer = task.category == "Salah" || task.title in listOf(
                "Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz", 
                "Offer Maghrib Namaz", "Offer Isha Namaz", "Offer Jummah Prayer"
            )
            val isQuranTask = task.category == "Quran" || task.title.lowercase().contains("surah") || task.title.lowercase().contains("quran")
            if ((isPrayer || isQuranTask) && !isCompleted && task.completed) {
                Log.d("TaqwaRepository", "Task ${task.title} is already completed and cannot be unchecked.")
                return@withLock false
            }
            if (isPrayer && isCompleted) {
                val ranges = getPrayerRanges(cachedPrayerTimes)
                val range = ranges.find { it.taskTitle == task.title || (task.title == "Offer Jummah Prayer" && it.taskTitle == "Offer Dhuhr Namaz") }
                if (range != null && Date() < range.start) {
                    Log.d("TaqwaRepository", "Prayer ${task.title} is locked in advance (starts at ${range.start}). Rejecting early completion.")
                    return@withLock false
                }
            }

            val todayStr = getPakistanDateString()
            val allTimeId = "${taskId}_${todayStr}"
            val existingLogs = taqwaDao.getAllTimeTasksDirect()
            val alreadyLogged = existingLogs.any { it.id == allTimeId && it.completedAt != "MISSED" }

            val updatedTask = task.copy(completed = isCompleted, autoProgress = if (isCompleted) task.autoTarget else 0)
            taqwaDao.insertTask(updatedTask)

            if (isCompleted) {
                if (!alreadyLogged) {
                    // Save to historical completions log
                    val log = AllTimeTaskEntity(
                        id = allTimeId,
                        taskId = taskId,
                        title = task.title,
                        category = task.category,
                        date = todayStr,
                        completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date())
                    )
                    taqwaDao.insertAllTimeTask(log)

                    // Add XP strictly once
                    val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
                    taqwaDao.insertUserStats(
                        currentStats.copy(
                            totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                            totalXp = currentStats.totalXp + task.points,
                            weeklyXp = currentStats.weeklyXp + task.points
                        )
                    )
                }
                taqwaDao.deleteAllTimeTaskById("${taskId}_${todayStr}_MISSED")
            } else {
                if (alreadyLogged) {
                    taqwaDao.deleteAllTimeTaskById(allTimeId)

                    // Deduct XP on uncheck
                    val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
                    taqwaDao.insertUserStats(
                        currentStats.copy(
                            totalTasksCompleted = kotlin.math.max(0, currentStats.totalTasksCompleted - 1),
                            totalXp = kotlin.math.max(0, currentStats.totalXp - task.points),
                            weeklyXp = kotlin.math.max(0, currentStats.weeklyXp - task.points)
                        )
                    )
                }
            }

            recalculateAndSaveStreak()
            return@withLock true
        }
>>>>>>> 6e834ed (Update Taqwahub)
    }

    private fun isTaskForSurah(taskTitle: String, surahId: Int): Boolean {
        val titleLower = taskTitle.lowercase()
        val surah = com.example.data.IslamicData.surahs.find { it.id == surahId } ?: return false
        val englishNameClean = surah.name.lowercase()
            .replace("al-", "")
            .replace("at-", "")
            .replace("an-", "")
            .replace("ash-", "")
            .replace("ar-", "")
            .replace("as-", "")
            .replace("ad-", "")
            .replace("az-", "")
            .replace("ay-", "")
            .replace("aj-", "")
            .replace("ah-", "")
            .replace("ai-", "")
            .replace("ak-", "")
            .replace("'", "")
            .replace("-", "")
            .trim()
        
        if (titleLower.contains(englishNameClean)) return true
        
        // Specialized common aliases or spellings
        if (surahId == 67 && (titleLower.contains("mulk") || titleLower.contains("moolk"))) return true
        if (surahId == 18 && (titleLower.contains("kahf") || titleLower.contains("kahaf"))) return true
        if (surahId == 36 && (titleLower.contains("yaseen") || titleLower.contains("yasin") || titleLower.contains("ya-sin"))) return true
        if (surahId == 55 && (titleLower.contains("rahman") || titleLower.contains("rehman"))) return true
        if (surahId == 56 && (titleLower.contains("waqiah") || titleLower.contains("waqiya"))) return true
        if (surahId == 2 && (titleLower.contains("baqarah") || titleLower.contains("baqara"))) return true
        
        return false
    }

    private fun isTaskForTasbeeh(taskTitle: String, tasbeehId: Int): Boolean {
        val titleLower = taskTitle.lowercase()
        return when (tasbeehId) {
            1 -> titleLower.contains("subhanallah") || titleLower.contains("subhan allah")
            2 -> titleLower.contains("alhamdulillah") || titleLower.contains("alhamdu lillah")
            3 -> titleLower.contains("allahu akbar") || titleLower.contains("allahuakbar")
            4 -> titleLower.contains("astagh") || titleLower.contains("forgiveness") || titleLower.contains("seek forgiveness")
            else -> false
        }
    }

    suspend fun updateAutoTaskProgress(autoType: String, amount: Int, contextId: Int? = null): Boolean = withContext(Dispatchers.IO) {
<<<<<<< HEAD
        val currentTasks = taqwaDao.getAllTasksDirect()
        var completedAny = false
        currentTasks.filter { it.isAuto && !it.completed && it.autoType == autoType }.forEach { task ->
            if (autoType == "SURAH" && contextId != null) {
                if (!isTaskForSurah(task.title, contextId)) {
                    return@forEach
                }
            }
            if (autoType == "TASBEEH" && contextId != null) {
                if (!isTaskForTasbeeh(task.title, contextId)) {
                    return@forEach
                }
            }

            val newProgress = task.autoProgress + amount
            if (newProgress >= task.autoTarget) {
                // Completed!
                taqwaDao.insertTask(task.copy(autoProgress = task.autoTarget, completed = true))
                
                val todayStr = getPakistanDateString()
                val allTimeId = "${task.id}_${todayStr}"
                val log = AllTimeTaskEntity(
                    id = allTimeId,
                    taskId = task.id,
                    title = task.title,
                    category = task.category,
                    date = todayStr,
                    completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date())
                )
                taqwaDao.insertAllTimeTask(log)

                // Add XP
                val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
                taqwaDao.insertUserStats(
                    currentStats.copy(
                        totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                        totalXp = currentStats.totalXp + task.points,
                        weeklyXp = currentStats.weeklyXp + task.points
                    )
                )
                completedAny = true
            } else {
                taqwaDao.insertTask(task.copy(autoProgress = newProgress))
            }
        }
        if (completedAny) recalculateAndSaveStreak()
        return@withContext completedAny
=======
        taskMutex.withLock {
            val currentTasks = taqwaDao.getAllTasksDirect()
            var completedAny = false
            val todayStr = getPakistanDateString()
            val existingLogs = taqwaDao.getAllTimeTasksDirect()

            currentTasks.filter { it.isAuto && !it.completed && it.autoType == autoType }.forEach { task ->
                if (autoType == "SURAH" && contextId != null) {
                    val isUniversal = task.targetSurahNumber == null && (task.id == TaskGenerator.ID_QURAN_GENERAL || task.title.lowercase().contains("daily") || task.title.lowercase().contains("holy quran"))
                    val isSpecificMatching = (task.targetSurahNumber != null && task.targetSurahNumber == contextId) || (task.targetSurahNumber == null && !isUniversal && isTaskForSurah(task.title, contextId))
                    if (!isUniversal && !isSpecificMatching) {
                        return@forEach
                    }
                }
                if (autoType == "TASBEEH" && contextId != null) {
                    if (!isTaskForTasbeeh(task.title, contextId)) {
                        return@forEach
                    }
                }

                val newProgress = task.autoProgress + amount
                if (newProgress >= task.autoTarget) {
                    // Completed!
                    taqwaDao.insertTask(task.copy(autoProgress = task.autoTarget, completed = true))
                    
                    val allTimeId = "${task.id}_${todayStr}"
                    val alreadyLogged = existingLogs.any { it.id == allTimeId && it.completedAt != "MISSED" }

                    if (!alreadyLogged) {
                        val log = AllTimeTaskEntity(
                            id = allTimeId,
                            taskId = task.id,
                            title = task.title,
                            category = task.category,
                            date = todayStr,
                            completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())
                        )
                        taqwaDao.insertAllTimeTask(log)

                        // Add XP strictly once
                        val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
                        taqwaDao.insertUserStats(
                            currentStats.copy(
                                totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                                totalXp = currentStats.totalXp + task.points,
                                weeklyXp = currentStats.weeklyXp + task.points
                            )
                        )
                    }
                    completedAny = true
                } else {
                    taqwaDao.insertTask(task.copy(autoProgress = newProgress))
                }
            }
            if (completedAny) recalculateAndSaveStreak()
            return@withLock completedAny
        }
    }

    data class QuranReadingRequirement(
        val requiredAyahs: Int,
        val requiredReadingSeconds: Long,
        val categoryName: String,
        val minDwellPerAyahSec: Int = 2
    )

    fun getSurahReadingRequirement(surahId: Int, totalVerses: Int): QuranReadingRequirement {
        return when {
            // Specific key Surahs with explicit parameters
            surahId == 67 -> { // Surah Al-Mulk (30 ayahs - Medium)
                QuranReadingRequirement(
                    requiredAyahs = 27, // 90%
                    requiredReadingSeconds = 240L, // 4 minutes
                    categoryName = "Medium Surah"
                )
            }
            surahId == 18 -> { // Surah Al-Kahf (110 ayahs - Long)
                QuranReadingRequirement(
                    requiredAyahs = 94, // 85%
                    requiredReadingSeconds = 480L, // 8 minutes
                    categoryName = "Long Surah"
                )
            }
            surahId == 36 -> { // Surah Yaseen (83 ayahs - Medium)
                QuranReadingRequirement(
                    requiredAyahs = 75, // 90%
                    requiredReadingSeconds = 300L, // 5 minutes
                    categoryName = "Medium Surah"
                )
            }
            surahId == 55 -> { // Surah Ar-Rahman (78 ayahs - Medium)
                QuranReadingRequirement(
                    requiredAyahs = 70, // 90%
                    requiredReadingSeconds = 300L, // 5 minutes
                    categoryName = "Medium Surah"
                )
            }
            surahId == 56 -> { // Surah Al-Waqi'ah (96 ayahs - Medium)
                QuranReadingRequirement(
                    requiredAyahs = 86, // 90%
                    requiredReadingSeconds = 300L, // 5 minutes
                    categoryName = "Medium Surah"
                )
            }
            surahId == 2 -> { // Surah Al-Baqarah (286 ayahs - Long)
                QuranReadingRequirement(
                    requiredAyahs = 243, // 85%
                    requiredReadingSeconds = 600L, // 10 minutes
                    categoryName = "Long Surah"
                )
            }
            // General rules based on Ayah length
            totalVerses <= 15 -> { // Short Surahs (1-15 ayahs, e.g. Al-Fatiha, Al-Ikhlas, Al-Falaq, An-Nas)
                QuranReadingRequirement(
                    requiredAyahs = totalVerses, // 100%
                    requiredReadingSeconds = 90L, // 1.5 minutes
                    categoryName = "Short Surah"
                )
            }
            totalVerses <= 99 -> { // Medium Surahs (16-99 ayahs)
                val targetAyahs = kotlin.math.max(1, (totalVerses * 0.90).toInt())
                QuranReadingRequirement(
                    requiredAyahs = targetAyahs, // 90%
                    requiredReadingSeconds = 240L, // 4 minutes
                    categoryName = "Medium Surah"
                )
            }
            else -> { // Long Surahs (100+ ayahs)
                val targetAyahs = kotlin.math.max(1, (totalVerses * 0.85).toInt())
                QuranReadingRequirement(
                    requiredAyahs = targetAyahs, // 85%
                    requiredReadingSeconds = 480L, // 8 minutes
                    categoryName = "Long Surah"
                )
            }
        }
    }

    /**
     * Atomically verifies and completes any active Quran reading tasks for today.
     * Prevents fast-scrolling, enforces dual-key (ayahs + time) verification,
     * awards XP idempotently once, and locks the task from being undone.
     */
    suspend fun verifyAndCompleteQuranReading(
        surahId: Int,
        versesVisitedCount: Int,
        totalVerses: Int,
        activeReadingSeconds: Long,
        hasReachedEnd: Boolean
    ): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskMutex.withLock {
            val requirement = getSurahReadingRequirement(surahId, totalVerses)
            val currentTasks = taqwaDao.getAllTasksDirect()
            val todayStr = getPakistanDateString()
            val existingLogs = taqwaDao.getAllTimeTasksDirect()
            val completedTasks = mutableListOf<TaskEntity>()

            // 1. Surah-specific criteria check
            val isSpecificSurahSatisfied = versesVisitedCount >= requirement.requiredAyahs &&
                    activeReadingSeconds >= requirement.requiredReadingSeconds

            // 2. General Quran reflection criteria check (at least 15 verses + at least 3 minutes active reading)
            val isGeneralQuranSatisfied = versesVisitedCount >= 15 && activeReadingSeconds >= 180L

            currentTasks.filter { !it.completed && (it.category == "Quran" || it.autoType == "SURAH" || it.title.lowercase().contains("quran") || it.title.lowercase().contains("surah")) }.forEach { task ->
                val isUniversal = task.targetSurahNumber == null && (task.id == TaskGenerator.ID_QURAN_GENERAL || task.title.lowercase().contains("daily") || task.title.lowercase().contains("holy quran"))
                val isMatchingSurah = (task.targetSurahNumber != null && task.targetSurahNumber == surahId) ||
                        (task.targetSurahNumber == null && !isUniversal && isTaskForSurah(task.title, surahId))

                val qualifies = (isMatchingSurah && isSpecificSurahSatisfied) || (isUniversal && isGeneralQuranSatisfied)

                if (qualifies) {
                    val allTimeId = "${task.id}_${todayStr}"
                    val alreadyLogged = existingLogs.any { it.id == allTimeId && it.completedAt != "MISSED" }

                    val completedTask = task.copy(
                        completed = true,
                        autoProgress = if (task.autoTarget > 0) task.autoTarget else if (isMatchingSurah) requirement.requiredReadingSeconds.toInt() else 180
                    )
                    taqwaDao.insertTask(completedTask)

                    if (!alreadyLogged) {
                        val log = AllTimeTaskEntity(
                            id = allTimeId,
                            taskId = task.id,
                            title = task.title,
                            category = task.category,
                            date = todayStr,
                            completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())
                        )
                        taqwaDao.insertAllTimeTask(log)

                        val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
                        taqwaDao.insertUserStats(
                            currentStats.copy(
                                totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                                totalXp = currentStats.totalXp + task.points,
                                weeklyXp = currentStats.weeklyXp + task.points
                            )
                        )
                    }
                    completedTasks.add(completedTask)
                } else if (isMatchingSurah || isUniversal) {
                    val target = if (task.autoTarget > 0) task.autoTarget else if (isMatchingSurah) requirement.requiredReadingSeconds.toInt() else 180
                    val currentProg = activeReadingSeconds.toInt().coerceIn(0, target)
                    if (task.autoProgress != currentProg) {
                        taqwaDao.insertTask(task.copy(autoProgress = currentProg))
                    }
                }
            }

            if (completedTasks.isNotEmpty()) {
                recalculateAndSaveStreak()
            }
            return@withLock completedTasks
        }
    }

    /**
     * Verifies continuous audio recitation streaming.
     * Completes audio tasks if active streaming threshold (at least 240s) is met.
     */
    suspend fun verifyAndCompleteQuranAudio(
        surahId: Int,
        activeAudioListenSeconds: Long
    ): List<TaskEntity> = withContext(Dispatchers.IO) {
        taskMutex.withLock {
            val currentTasks = taqwaDao.getAllTasksDirect()
            val todayStr = getPakistanDateString()
            val existingLogs = taqwaDao.getAllTimeTasksDirect()
            val completedTasks = mutableListOf<TaskEntity>()

            currentTasks.filter { !it.completed && (it.title.lowercase().contains("listen") || (it.category == "Quran" && it.isAuto)) }.forEach { task ->
                val target = if (task.autoTarget > 0) task.autoTarget else 240
                val qualifies = activeAudioListenSeconds >= target

                if (qualifies) {
                    val allTimeId = "${task.id}_${todayStr}"
                    val alreadyLogged = existingLogs.any { it.id == allTimeId && it.completedAt != "MISSED" }

                    val completedTask = task.copy(
                        completed = true,
                        autoProgress = target
                    )
                    taqwaDao.insertTask(completedTask)

                    if (!alreadyLogged) {
                        val log = AllTimeTaskEntity(
                            id = allTimeId,
                            taskId = task.id,
                            title = task.title,
                            category = task.category,
                            date = todayStr,
                            completedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())
                        )
                        taqwaDao.insertAllTimeTask(log)

                        val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
                        taqwaDao.insertUserStats(
                            currentStats.copy(
                                totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                                totalXp = currentStats.totalXp + task.points,
                                weeklyXp = currentStats.weeklyXp + task.points
                            )
                        )
                    }
                    completedTasks.add(completedTask)
                } else {
                    val currentProg = activeAudioListenSeconds.toInt().coerceIn(0, target)
                    if (task.autoProgress != currentProg) {
                        taqwaDao.insertTask(task.copy(autoProgress = currentProg))
                    }
                }
            }

            if (completedTasks.isNotEmpty()) {
                recalculateAndSaveStreak()
            }
            return@withLock completedTasks
        }
>>>>>>> 6e834ed (Update Taqwahub)
    }

    data class PrayerRange(val taskTitle: String, val start: Date, val end: Date)

    fun getPrayerRanges(timings: AladhanTimings?): List<PrayerRange> {
        val f = timings?.Fajr?.ifEmpty { "04:20" } ?: "04:20"
        val s = timings?.Sunrise?.ifEmpty { "05:45" } ?: "05:45"
        val d = timings?.Dhuhr?.ifEmpty { "12:30" } ?: "12:30"
        val a = timings?.Asr?.ifEmpty { "15:45" } ?: "15:45"
        val m = timings?.Maghrib?.ifEmpty { "18:45" } ?: "18:45"
        val i = timings?.Isha?.ifEmpty { "20:15" } ?: "20:15"

<<<<<<< HEAD
        val todayDateStr = getPakistanDateString()

        val todayFajr = parseTimeStr(todayDateStr, f)
        val todaySunrise = parseTimeStr(todayDateStr, s)
        val todayDhuhr = parseTimeStr(todayDateStr, d)
        val todayAsr = parseTimeStr(todayDateStr, a)
        val todayMaghrib = parseTimeStr(todayDateStr, m)
        val todayIsha = parseTimeStr(todayDateStr, i)

        val tomorrowCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Karachi")
        }.format(tomorrowCal.time)
        val tomorrowFajr = parseTimeStr(tomorrowDateStr, f)
=======
        val tzString = cachedTimezone
        val tz = if (!tzString.isNullOrEmpty()) {
            TimeZone.getTimeZone(tzString)
        } else {
            TimeZone.getDefault() ?: TimeZone.getTimeZone("Asia/Karachi")
        }
        val todayCal = Calendar.getInstance(tz)
        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = tz
        }.format(todayCal.time)

        val todayFajr = parseTimeStr(todayDateStr, f, tz)
        val todaySunrise = parseTimeStr(todayDateStr, s, tz)
        val todayDhuhr = parseTimeStr(todayDateStr, d, tz)
        val todayAsr = parseTimeStr(todayDateStr, a, tz)
        val todayMaghrib = parseTimeStr(todayDateStr, m, tz)
        val todayIsha = parseTimeStr(todayDateStr, i, tz)

        val tomorrowCal = Calendar.getInstance(tz).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = tz
        }.format(tomorrowCal.time)
        val tomorrowFajr = parseTimeStr(tomorrowDateStr, f, tz)
>>>>>>> 6e834ed (Update Taqwahub)

        return listOf(
            PrayerRange("Offer Fajr Namaz", todayFajr, todaySunrise),
            PrayerRange("Offer Dhuhr Namaz", todayDhuhr, todayAsr),
<<<<<<< HEAD
=======
            PrayerRange("Offer Jummah Prayer", todayDhuhr, todayAsr),
>>>>>>> 6e834ed (Update Taqwahub)
            PrayerRange("Offer Asr Namaz", todayAsr, todayMaghrib),
            PrayerRange("Offer Maghrib Namaz", todayMaghrib, todayIsha),
            PrayerRange("Offer Isha Namaz", todayIsha, tomorrowFajr)
        )
    }

<<<<<<< HEAD
    private fun parseTimeStr(dateStr: String, timeStr: String): Date {
        val cleanTime = timeStr.replace(Regex("\\s\\(.*?\\)"), "")
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Karachi")
        }
        return try {
            format.parse("$dateStr $cleanTime") ?: Date()
=======
    private fun parseTimeStr(dateStr: String, timeStr: String, tz: TimeZone = TimeZone.getDefault() ?: TimeZone.getTimeZone("Asia/Karachi")): Date {
        val cleanTime = timeStr.replace(Regex("\\s\\(.*?\\)"), "").trim()
        val format24 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            timeZone = tz
        }
        val format12 = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).apply {
            timeZone = tz
        }
        return try {
            if (cleanTime.contains("AM", ignoreCase = true) || cleanTime.contains("PM", ignoreCase = true)) {
                format12.parse("$dateStr $cleanTime") ?: Date()
            } else {
                format24.parse("$dateStr $cleanTime") ?: Date()
            }
>>>>>>> 6e834ed (Update Taqwahub)
        } catch (e: Exception) {
            Date()
        }
    }

    suspend fun checkAndLogMissedPrayers() = withContext(Dispatchers.IO) {
        val todayStr = getPakistanDateString()
        val allTasks = taqwaDao.getAllTasksDirect()
        val prayerTasks = allTasks.filter {
<<<<<<< HEAD
            it.title in listOf("Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz", "Offer Maghrib Namaz", "Offer Isha Namaz")
=======
            it.title in listOf("Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz", "Offer Maghrib Namaz", "Offer Isha Namaz", "Offer Jummah Prayer")
>>>>>>> 6e834ed (Update Taqwahub)
        }

        val ranges = getPrayerRanges(cachedPrayerTimes)
        val now = Date()

        prayerTasks.forEach { task ->
<<<<<<< HEAD
            val range = ranges.find { it.taskTitle == task.title }
=======
            val range = ranges.find { it.taskTitle == task.title || (task.title == "Offer Jummah Prayer" && it.taskTitle == "Offer Dhuhr Namaz") }
>>>>>>> 6e834ed (Update Taqwahub)
            if (range != null) {
                if (now > range.end && !task.completed) {
                    val missedLogId = "${task.id}_${todayStr}_MISSED"
                    val normalLogId = "${task.id}_${todayStr}"

                    val existingCompletions = taqwaDao.getAllTimeTasksDirect()
                    val hasCompleted = existingCompletions.any { it.id == normalLogId }
                    val hasMissed = existingCompletions.any { it.id == missedLogId }

                    if (!hasCompleted && !hasMissed) {
                        val missedLog = AllTimeTaskEntity(
                            id = missedLogId,
                            taskId = task.id,
                            title = task.title,
                            category = "Salah",
                            date = todayStr,
                            completedAt = "MISSED"
                        )
                        taqwaDao.insertAllTimeTask(missedLog)
                    }
                }
            }
        }
    }

    // Perform daily reset check
    suspend fun checkDailyReset(): Boolean = withContext(Dispatchers.IO) {
        val stats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
        val todayStr = getPakistanDateString()

        if (stats.lastResetDate != todayStr) {
            // Update last reset date and increment continuous active days
            val updatedDays = if (stats.lastResetDate.isNotEmpty()) stats.daysActive + 1 else stats.daysActive
            taqwaDao.insertUserStats(
                stats.copy(
                    lastResetDate = todayStr,
                    daysActive = updatedDays
                )
            )

<<<<<<< HEAD
            // Clear old system tasks and add today's system tasks
            taqwaDao.clearSystemTasks()
            val newSystemTasks = TaskGenerator.generateTasksForToday()
            taqwaDao.insertAllTasks(newSystemTasks)

            // Reset manual tasks completions (that the user created)
            val tasks = taqwaDao.getAllTasksDirect()
            val resetTasks = tasks.map { it.copy(completed = false, autoProgress = 0) }
            taqwaDao.insertAllTasks(resetTasks)
=======
            // Extract existing user-created custom tasks
            val existingManualTasks = taqwaDao.getAllTasksDirect().filter { !it.isSystemTask }
            val resetManualTasks = existingManualTasks.map { it.copy(completed = false, autoProgress = 0) }

            // Clear old tasks and populate with today's master system tasks + reset manual tasks
            taqwaDao.clearTasks()
            val newSystemTasks = TaskGenerator.generateTasksForToday()
            val combinedTasks = (newSystemTasks + resetManualTasks).distinctBy { it.id }
            taqwaDao.insertAllTasks(combinedTasks)
>>>>>>> 6e834ed (Update Taqwahub)
            recalculateAndSaveStreak()
            return@withContext true
        }
        recalculateAndSaveStreak() // Recalculate anyway to ensure consistency if dates mismatch or updated ofline
        return@withContext false
    }

<<<<<<< HEAD
    data class StreakInfo(val currentStreak: Int, val chancesUsed: Int)

    fun calculateStreakInfo(completedDates: List<String>, maxChancesAvailable: Int = 2): StreakInfo {
        if (completedDates.isEmpty()) return StreakInfo(0, 0)
        val uniqueDatesSet = completedDates.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        if (uniqueDatesSet.isEmpty()) return StreakInfo(0, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Karachi")
=======
    data class StreakInfo(
        val currentStreak: Int,
        val longestStreak: Int,
        val shieldsRemaining: Int,
        val frozenDates: List<String>,
        val isTodayCompleted: Boolean,
        val shieldConsumedForDate: String? = null
    )

    fun calculateStreakInfo(
        completedDates: List<String>,
        existingFrozenDates: List<String> = emptyList(),
        currentShieldsAvailable: Int = 0
    ): StreakInfo {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Karachi")
        }
>>>>>>> 6e834ed (Update Taqwahub)

        val todayCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
        val todayStr = sdf.format(todayCal.time)

        val yesterdayCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayStr = sdf.format(yesterdayCal.time)

<<<<<<< HEAD
        var currentCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
        var currentDateStr = sdf.format(currentCal.time)
        
        // If today is not completed, check if yesterday was completed.
        // If yesterday was completed, we start from yesterday (ongoing active streak is preserved).
        // Otherwise, if both are missed, start today and consume chances for both.
        if (!uniqueDatesSet.contains(currentDateStr) && uniqueDatesSet.contains(yesterdayStr)) {
            currentCal.add(Calendar.DAY_OF_YEAR, -1)
            currentDateStr = sdf.format(currentCal.time)
        }

        var streak = 0
        var chancesUsed = 0
        var consecutiveMissedDays = 0

        for (day in 0..365) {
            val dateStr = sdf.format(currentCal.time)
            if (uniqueDatesSet.contains(dateStr)) {
                streak++
                consecutiveMissedDays = 0
            } else {
                if (chancesUsed < maxChancesAvailable) {
                    chancesUsed++
                    consecutiveMissedDays++
                } else {
                    break
                }
            }

            if (consecutiveMissedDays > maxChancesAvailable) {
                break
            }

            currentCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return StreakInfo(streak, chancesUsed)
    }

    suspend fun recalculateAndSaveStreak() = withContext(Dispatchers.IO) {
        val allTimeLogs = taqwaDao.getAllTimeTasksDirect()
        val completedDates = allTimeLogs.filter { it.completedAt != "MISSED" }.map { it.date }
        val info = calculateStreakInfo(completedDates)

        val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
        val newLongest = kotlin.math.max(currentStats.longestStreak, info.currentStreak)
        taqwaDao.insertUserStats(
            currentStats.copy(
                currentStreak = info.currentStreak,
                longestStreak = newLongest,
                streakChancesLeft = maxOf(0, 2 - info.chancesUsed)
            )
        )
=======
        val uniqueCompletedSet = completedDates.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val frozenSet = existingFrozenDates.map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()

        val isTodayCompleted = uniqueCompletedSet.contains(todayStr)
        var shieldsLeft = currentShieldsAvailable.coerceIn(0, 2)
        var shieldConsumedForDate: String? = null

        // Check for missed days going backward from yesterday until the last active/protected date
        val evalCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi")).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val gapDates = mutableListOf<String>()
        var foundPreviousActive = false

        for (dayOffset in 1..60) {
            val dateStr = sdf.format(evalCal.time)
            if (uniqueCompletedSet.contains(dateStr) || frozenSet.contains(dateStr)) {
                foundPreviousActive = true
                break
            } else {
                gapDates.add(dateStr)
            }
            evalCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        if (foundPreviousActive && gapDates.isNotEmpty()) {
            val gapSize = gapDates.size
            if (gapSize <= shieldsLeft) {
                // All missed days in the gap are protected by available safety buffers
                for (missedDate in gapDates) {
                    frozenSet.add(missedDate)
                }
                shieldsLeft -= gapSize
                shieldConsumedForDate = gapDates.first()
            }
        }

        // Active/Protected timeline set
        val activeOrProtectedSet = uniqueCompletedSet + frozenSet
        if (activeOrProtectedSet.isEmpty()) {
            return StreakInfo(0, 0, shieldsLeft, frozenSet.toList(), isTodayCompleted, shieldConsumedForDate)
        }

        var currentCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
        if (!isTodayCompleted) {
            // If today not finished yet, start evaluating from yesterday backwards
            currentCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        var streak = 0
        for (day in 0..365) {
            val dateStr = sdf.format(currentCal.time)
            if (activeOrProtectedSet.contains(dateStr)) {
                streak++
            } else {
                break
            }
            currentCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return StreakInfo(
            currentStreak = streak,
            longestStreak = streak,
            shieldsRemaining = shieldsLeft,
            frozenDates = frozenSet.toList(),
            isTodayCompleted = isTodayCompleted,
            shieldConsumedForDate = shieldConsumedForDate
        )
    }

    suspend fun recalculateAndSaveStreak(): StreakInfo = withContext(Dispatchers.IO) {
        val allTimeLogs = taqwaDao.getAllTimeTasksDirect()
        val logCompletedDates = allTimeLogs.filter { it.completedAt != "MISSED" }.map { it.date }
        
        val currentStats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
        val parsedActiveDates = currentStats.activeDates.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val parsedFrozenDates = currentStats.frozenDates.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val allCompletedDates = (logCompletedDates + parsedActiveDates).distinct()

        // Guard against race condition on cold launch when allTimeLogs are not loaded/synced yet:
        if (allCompletedDates.isEmpty() && currentStats.currentStreak > 0) {
            return@withContext StreakInfo(
                currentStats.currentStreak,
                currentStats.longestStreak,
                currentStats.streakShields,
                parsedFrozenDates,
                false
            )
        }

        val info = calculateStreakInfo(
            completedDates = allCompletedDates,
            existingFrozenDates = parsedFrozenDates,
            currentShieldsAvailable = currentStats.streakShields
        )

        val finalStreak = if (info.currentStreak == 0 && allCompletedDates.isEmpty()) {
            currentStats.currentStreak
        } else {
            info.currentStreak
        }

        val newLongest = maxOf(currentStats.longestStreak, finalStreak)
        val todayStr = getPakistanDateString()
        val lastActive = if (info.isTodayCompleted) todayStr else currentStats.lastActiveDate

        val updatedStats = currentStats.copy(
            currentStreak = finalStreak,
            longestStreak = newLongest,
            streakShields = info.shieldsRemaining,
            streakChancesLeft = info.shieldsRemaining,
            frozenDates = info.frozenDates.joinToString(","),
            activeDates = allCompletedDates.takeLast(60).joinToString(","),
            lastActiveDate = lastActive,
            lastShieldUsedDate = info.shieldConsumedForDate ?: currentStats.lastShieldUsedDate
        )

        taqwaDao.insertUserStats(updatedStats)
        return@withContext info
>>>>>>> 6e834ed (Update Taqwahub)
    }

    // Save and load general statistics
    suspend fun saveUserStats(stats: UserStatsEntity) = withContext(Dispatchers.IO) {
        taqwaDao.insertUserStats(stats)
    }

    suspend fun getUserStats(): UserStatsEntity = withContext(Dispatchers.IO) {
        return@withContext taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
    }

    // Adding or removing bookmark
    suspend fun toggleBookmark(surahNumber: Int, surahName: String, verseNumber: Int, verseKey: String, isFlowMode: Boolean = false) = withContext(Dispatchers.IO) {
        val id = "${surahNumber}_${verseNumber}"
        val allBookmarks = taqwaDao.getAllBookmarksFlow().firstOrNull() ?: emptyList()
        val existingExact = allBookmarks.find { it.id == id }
        
        if (existingExact != null) {
            taqwaDao.deleteBookmarkById(id)
        } else {
            // Enforce single bookmark per surah by clearing previous bookmarks in the same surah
            allBookmarks.filter { it.surahNumber == surahNumber }.forEach { 
                taqwaDao.deleteBookmarkById(it.id)
            }
            
            val bookmark = BookmarkEntity(
                id = id,
                surahNumber = surahNumber,
                surahName = surahName,
                verseNumber = verseNumber,
                verseKey = verseKey,
                timestamp = System.currentTimeMillis(),
                isFlowMode = isFlowMode
            )
            taqwaDao.insertBookmark(bookmark)

            // Update stats last read position
            val stats = taqwaDao.getUserStatsDirect() ?: UserStatsEntity()
            taqwaDao.insertUserStats(
                stats.copy(
                    lastReadSurah = surahNumber,
                    lastReadVerse = verseNumber,
                    lastReadVerseKey = verseKey
                )
            )
        }
    }

    // Quran Live API fetchers
    suspend fun fetchChapters(): List<QuranChapter> {
        return try {
            quranApi.getChapters().chapters
        } catch (e: Exception) {
            Log.e("TaqwaRepository", "Failed to fetch chapters: ${e.message}")
            emptyList()
        }
    }

    // Fetches Uthmani text with word meanings and English translation for a chapter
    suspend fun fetchVerses(surahId: Int, reciterId: Int? = null): List<QuranVerse> {
        return try {
            quranApi.getVersesWithWords(surahId, audio = reciterId).verses
        } catch (e: Exception) {
            Log.e("TaqwaRepository", "Failed to fetch verses: ${e.message}")
            emptyList()
        }
    }

    // Fetches translations (Saheeh, Maududi, Jalandhari, etc.) to extract English and Urdu text
    suspend fun fetchTranslations(surahId: Int): List<TranslationVerse> {
        return try {
            quranApi.getChapterTranslations(surahId).verses
        } catch (e: Exception) {
            Log.e("TaqwaRepository", "Failed to fetch translations: ${e.message}")
            emptyList()
        }
    }

    // Fetches audio recitation files for active media players
    suspend fun fetchRecitation(surahId: Int, reciterId: Int = 7): List<AudioFile> {
        return try {
            quranApi.getChapterRecitation(surahId, reciterId).audio_files
        } catch (e: Exception) {
            Log.e("TaqwaRepository", "Failed to fetch recitation for reciter $reciterId: ${e.message}")
            emptyList()
        }
    }

    // Fetches Tafsir data for an ayah by key
    suspend fun fetchTafsir(tafsirId: Int, verseKey: String): TafsirData? {
        return try {
            quranApi.getTafsir(tafsirId, verseKey).tafsirs?.firstOrNull()
        } catch (e: Exception) {
            Log.e("TaqwaRepository", "Failed to fetch tafsir $tafsirId for $verseKey: ${e.message}")
            null
        }
    }

    // Fetch Prayer Times
<<<<<<< HEAD
    suspend fun fetchPrayerTimes(lat: Double, lng: Double): AladhanTimings? {
        return try {
            val response = aladhanApi.getTimings(lat, lng)
            if (response.code == 200) {
                response.data?.timings
=======
    suspend fun fetchPrayerTimes(lat: Double, lng: Double): Pair<AladhanTimings, String>? {
        return try {
            val response = aladhanApi.getTimings(lat, lng)
            if (response.code == 200) {
                val timings = response.data?.timings
                val timezone = response.data?.meta?.timezone ?: "Asia/Karachi"
                if (timings != null) {
                    cachedPrayerTimes = timings
                    cachedTimezone = timezone
                }
                if (timings != null) Pair(timings, timezone) else null
>>>>>>> 6e834ed (Update Taqwahub)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TaqwaRepository", "Failed to fetch prayer times: ${e.message}")
            null
        }
    }

    enum class ProviderType { GEMINI, GROQ, OPENROUTER }
    
    data class LLMCallStrategy(
        val provider: ProviderType,
        val model: String,
        val apiKey: String,
        val displayName: String
    )

    // Chat with TaqwaHub AI (Multi-Provider with Auto-Switching Failover)
    suspend fun queryGeminiAI(
        query: String,
        history: List<GeminiContent>,
        systemInstructionText: String
    ): String {
        return withContext(Dispatchers.IO) {
            val strategies = mutableListOf<LLMCallStrategy>()

            // Read API Keys from environment Fallback / BuildConfig
            val groqKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
            val orKey = try { BuildConfig.OPENROUTER_API_KEY } catch (e: Exception) { "" }

            val isGroqConfigured = groqKey.isNotEmpty() && 
                    !groqKey.contains("YOUR_") && 
                    !groqKey.contains("your_") && 
                    !groqKey.contains("DEFAULT")

            val isOpenRouterConfigured = orKey.isNotEmpty() && 
                    !orKey.contains("YOUR_") && 
                    !orKey.contains("your_") && 
                    !orKey.contains("DEFAULT")

            // Arrange strategies: If Groq or OpenRouter are explicitly configured, prioritize them for lower latency/higher quotas.
            // Otherwise, failover silently to Gemini Proxy or the active provider.
            if (isGroqConfigured) {
                strategies.add(LLMCallStrategy(ProviderType.GROQ, "llama-3.3-70b-versatile", groqKey, "Groq (Llama-3.3 70B)"))
                strategies.add(LLMCallStrategy(ProviderType.GROQ, "mixtral-8x7b-32768", groqKey, "Groq (Mixtral 8x7B)"))
            }

            if (isOpenRouterConfigured) {
                strategies.add(LLMCallStrategy(ProviderType.OPENROUTER, "meta-llama/llama-3.3-70b-instruct:free", orKey, "OpenRouter (Llama-3.3 70B Free)"))
                strategies.add(LLMCallStrategy(ProviderType.OPENROUTER, "google/gemini-2.5-flash:free", orKey, "OpenRouter (Gemini 2.5 Flash Free)"))
            }

            // High-Performance Gemini Proxy (Internal / Default Option)
            strategies.add(LLMCallStrategy(ProviderType.GEMINI, "gemini-2.0-flash", "", "Gemini Proxy (Flash 2.0)"))
            strategies.add(LLMCallStrategy(ProviderType.GEMINI, "gemini-1.5-flash", "", "Gemini Proxy (Flash 1.5)"))
            strategies.add(LLMCallStrategy(ProviderType.GEMINI, "gemini-1.5-pro", "", "Gemini Proxy (Pro 1.5)"))

            // Secondary Fallback if primary keys are not configured yet, just in case
            if (!isGroqConfigured) {
                // If the user hasn't added the keys yet, we can't call them, but we keep the proxy
            }

            var lastErrorMsg = "Unknown error"
            var lastCode = 0

            for (strategy in strategies) {
                try {
                    Log.d("TaqwaRepository", "Attempting query with provider: ${strategy.displayName}, Model: ${strategy.model}")
                    
                    when (strategy.provider) {
                        ProviderType.GEMINI -> {
                            val contents = history.toList()
                            val sysInstruction = GeminiSystemInstruction(listOf(GeminiPart(systemInstructionText)))
                            val requestBody = GeminiRequest(
                                contents = contents,
                                systemInstruction = sysInstruction,
                                generationConfig = GeminiGenerationConfig()
                            )

                            val response = geminiApi.generateContent(strategy.model, requestBody)
                            if (response.isSuccessful && response.body() != null) {
                                val rawText = response.body()!!.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                                val cleanedResponse = cleanLlmResponse(rawText)
                                Log.d("TaqwaRepository", "Successfully queried ${strategy.displayName}")
                                return@withContext cleanedResponse
                            } else {
                                lastCode = response.code()
                                lastErrorMsg = response.errorBody()?.string() ?: "Unknown error"
                                Log.e("TaqwaRepository", "${strategy.displayName} rejected with code $lastCode: $lastErrorMsg")
                            }
                        }
                        
                        ProviderType.GROQ -> {
                            val openAiMsgList = mutableListOf<OpenAiMessage>()
                            openAiMsgList.add(OpenAiMessage("system", systemInstructionText))
                            history.forEach { content ->
                                val roleName = if (content.role == "user") "user" else "assistant"
                                content.parts.forEach { part ->
                                    openAiMsgList.add(OpenAiMessage(roleName, part.text))
                                }
                            }
                            if (history.isEmpty() || history.lastOrNull()?.parts?.firstOrNull()?.text != query) {
                                openAiMsgList.add(OpenAiMessage("user", query))
                            }

                            val openAiRequest = OpenAiRequest(
                                model = strategy.model,
                                messages = openAiMsgList
                            )

                            val response = groqApi.generateContent("Bearer ${strategy.apiKey}", openAiRequest)
                            if (response.isSuccessful && response.body() != null) {
                                val rawText = response.body()!!.choices?.firstOrNull()?.message?.content ?: ""
                                val cleanedResponse = cleanLlmResponse(rawText)
                                Log.d("TaqwaRepository", "Successfully queried ${strategy.displayName}")
                                return@withContext cleanedResponse
                            } else {
                                lastCode = response.code()
                                lastErrorMsg = response.errorBody()?.string() ?: "Unknown error"
                                Log.e("TaqwaRepository", "${strategy.displayName} rejected with code $lastCode: $lastErrorMsg")
                            }
                        }

                        ProviderType.OPENROUTER -> {
                            val openAiMsgList = mutableListOf<OpenAiMessage>()
                            openAiMsgList.add(OpenAiMessage("system", systemInstructionText))
                            history.forEach { content ->
                                val roleName = if (content.role == "user") "user" else "assistant"
                                content.parts.forEach { part ->
                                    openAiMsgList.add(OpenAiMessage(roleName, part.text))
                                }
                            }
                            if (history.isEmpty() || history.lastOrNull()?.parts?.firstOrNull()?.text != query) {
                                openAiMsgList.add(OpenAiMessage("user", query))
                            }

                            val openAiRequest = OpenAiRequest(
                                model = strategy.model,
                                messages = openAiMsgList
                            )

                            val response = openRouterApi.generateContent("Bearer ${strategy.apiKey}", openAiRequest)
                            if (response.isSuccessful && response.body() != null) {
                                val rawText = response.body()!!.choices?.firstOrNull()?.message?.content ?: ""
                                val cleanedResponse = cleanLlmResponse(rawText)
                                Log.d("TaqwaRepository", "Successfully queried ${strategy.displayName}")
                                return@withContext cleanedResponse
                            } else {
                                lastCode = response.code()
                                lastErrorMsg = response.errorBody()?.string() ?: "Unknown error"
                                Log.e("TaqwaRepository", "${strategy.displayName} rejected with code $lastCode: $lastErrorMsg")
                            }
                        }
                    }
                } catch (e: Exception) {
                    lastCode = -1
                    lastErrorMsg = e.message ?: "Exception"
                    Log.e("TaqwaRepository", "Strategy ${strategy.displayName} failed: $lastErrorMsg")
                }
            }

            return@withContext "Divine connectivity is currently undergoing heavy load. Please configure your custom Groq/OpenRouter keys in the edit panel or try again in a moment. Code $lastCode"
        }
    }

    private fun cleanLlmResponse(rawText: String): String {
        var text = rawText
            .replace("**", "")
            .replace("*", "")
            .replace("`", "")
            .trim()

        // 1. Programmatic Hindi word replacements in Roman Urdu
        val replacements = mapOf(
            "parivar" to "khandan",
            "parivaar" to "khandan",
            "Parivar" to "Khandan",
            "Parivaar" to "Khandan",
            "chinta" to "pareshani",
            "Chinta" to "Pareshani",
            "shanti" to "sukoon",
            "Shanti" to "Sukoon",
            "pavitra" to "pakeeza",
            "Pavitra" to "Pakeeza",
            "prarthana" to "dua",
            "Prarthana" to "Dua",
            "bhagwan" to "Allah",
            "Bhagwan" to "Allah",
            "sundar" to "khubsoorat",
            "Sundar" to "Khubsoorat",
            "samasya" to "masla",
            "Samasya" to "Masla",
            "vishwas" to "yakeen",
            "vishwaas" to "yakeen",
            "Vishwas" to "Yakeen",
            "Vishwaas" to "Yakeen",
            "shakti" to "taqat",
            "Shakti" to "Taqat",
            "kripa" to "rahmat",
            "Kripa" to "Rahmat",
            "krpa" to "rahmat",
            "Krpa" to "Rahmat",
            "daan" to "sadqa",
            "Daan" to "Sadqa",
            "prem" to "muhabbat",
            "Prem" to "Muhabbat",
            "grah" to "ghar",
            "Grah" to "Ghar",
            "krodh" to "gussa",
            "Krodh" to "Gussa",
            "jeevan" to "zindagi",
            "Jeevan" to "Zindagi",
            "mrityu" to "maut",
            "Mrityu" to "Maut",
            "gyan" to "ilm",
            "Gyan" to "Ilm",
            "shuruat" to "shuru",
            "Shuruat" to "Shuru"
        )

        for ((hindi, urdu) in replacements) {
            // Match whole words to avoid partial replacement issues
            text = text.replace(Regex("\\b$hindi\\b"), urdu)
        }

        // 2. Programmatic Hindi word replacements in Urdu Script (Nastaliq)
        val urduScriptReplacements = mapOf(
            "پریوار" to "خاندان",
            "چنتا" to "پریشانی",
            "شانتی" to "سکون",
            "پوتر" to "پاکیزہ",
            "پرارتھنا" to "دعا",
            "سمسیا" to "مسئلہ",
            "وشواس" to "یقین",
            "بھگوان" to "اللہ",
            "کرپا" to "رحمت",
            "سندر" to "خوبصورت",
            "دان" to "صدقہ"
        )

        for ((hindiScript, urduScript) in urduScriptReplacements) {
            text = text.replace(hindiScript, urduScript)
        }

        // 3. Programmatic Pronunciation/Grammar fixes (Ka vs Ko) for specific common mistranslations
        val grammarFixes = mapOf(
            "Allah ko fazal" to "Allah ka fazal",
            "Allah ko rehmat" to "Allah ki rahmat",
            "Allah ko rahmat" to "Allah ki rahmat",
            "Allah ko hukum" to "Allah ka hukum",
            "Allah ko hukam" to "Allah ka hukum",
            "Allah ko raza" to "Allah ki raza",
            "Aap ko khandan" to "Aap ka khandan",
            "zindagi ko masla" to "zindagi ka masla",
            "Zindagi ko masla" to "Zindagi ka masla",
            "zindagi ko pareshani" to "zindagi ki pareshani",
            "Zindagi ko pareshani" to "Zindagi ki pareshani",
            "Islam ko rasta" to "Islam ka rasta",
            "islam ko rasta" to "islam ka rasta",
            "deen ko rasta" to "deen ka rasta",
            "Deen ko rasta" to "Deen ka rasta"
        )

        for ((wrongPhrase, rightPhrase) in grammarFixes) {
            text = text.replace(Regex("(?i)\\b$wrongPhrase\\b"), rightPhrase)
        }

        return text
    }
}
