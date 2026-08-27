package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TaqwaApplication
import com.example.data.IslamicData
import com.example.data.Surah
import com.example.data.Hadith
import com.example.data.Dua
import com.example.data.Announcement
import com.example.data.BugReport
import com.example.data.TaqwaRepository
import com.example.data.api.AladhanTimings
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.QuranChapter
import com.example.data.room.AllTimeTaskEntity
import com.example.data.room.BookmarkEntity
import com.example.data.room.TaskEntity
import com.example.data.room.UserStatsEntity
import com.example.data.room.TaqwaDatabase
import com.example.data.room.TaqwaDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.text.SimpleDateFormat
import java.io.File
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import android.content.Context
import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import androidx.glance.appwidget.updateAll

enum class TaqwaNetworkType {
    WIFI,
    CELLULAR,
    AIRPLANE,
    NONE
}

enum class TaqwaNetworkCondition {
    BAD,
    MEDIUM,
    GOOD,
    EXCELLENT
}

data class TaqwaNetworkStatusInfo(
    val type: TaqwaNetworkType = TaqwaNetworkType.NONE,
    val condition: TaqwaNetworkCondition = TaqwaNetworkCondition.GOOD,
    val signalLevel: Int = 3 // default to 3-bars initially
)

data class AppConfig(
    val isUnderMaintenance: Boolean = false,
    val message: String = "App is active",
    val enableAiChat: Boolean = false,
    val welcomeBannerMessage: String = "",
    val forceUpdateMinVersion: String = "1.0.0",
    val updateDownloadUrl: String = "",
    
    // Quran Audio Blocking
    val isQuranAudioLocked: Boolean = false,
    val quranAudioBlockedMessage: String = "Surah audio is currently blocked by administrator.",
    
    val lockedSurahIds: String = "",
    val surahBlockedMessage: String = "This Surah is completely blocked by administrator.",
    
    val lockedWordSurahIds: String = "",
    val wordSurahBlockedMessage: String = "Word by Word audio is currently blocked by administrator.",
    
    // Pages Blocking (Legacy / Category)
    val isQuranPageLocked: Boolean = false,
    val quranPageBlockedMessage: String = "The Quran page is currently blocked.",
    
    val isToolsPageLocked: Boolean = false,
    val toolsPageBlockedMessage: String = "The Tools page is currently blocked.",
    
    val isLearnPageLocked: Boolean = false,
    val learnPageBlockedMessage: String = "The Learn page is currently blocked.",

    // Module Security & Visibility Control Matrix
    // 1. Hadith
    val isHadithLocked: Boolean = false,
    val isHadithHidden: Boolean = false,
    val hadithLockReason: String = "Hadith Collection is temporarily undergoing maintenance to enhance accuracy.",
    val hadithLockCategory: String = "server_maintenance",

    // 2. Dua & Azkar
    val isDuaLocked: Boolean = false,
    val isDuaHidden: Boolean = false,
    val duaLockReason: String = "Dua & Azkar section is temporarily undergoing maintenance.",
    val duaLockCategory: String = "server_maintenance",

    // 3. Quran Reader
    val isQuranLocked: Boolean = false,
    val isQuranHidden: Boolean = false,
    val quranLockReason: String = "Quran Reader is temporarily locked for content verification.",
    val quranLockCategory: String = "server_maintenance",

    // 4. Leaderboard
    val isLeaderboardLocked: Boolean = false,
    val isLeaderboardHidden: Boolean = false,
    val leaderboardLockReason: String = "Global Leaderboard is undergoing scheduled score sync.",
    val leaderboardLockCategory: String = "server_maintenance",

    // 5. Tasks & Challenges
    val isTasksLocked: Boolean = false,
    val isTasksHidden: Boolean = false,
    val tasksLockReason: String = "Task Tracker & Daily Challenges are temporarily offline.",
    val tasksLockCategory: String = "server_maintenance",

    // 6. Tasbeeh Counter
    val isTasbeehLocked: Boolean = false,
    val isTasbeehHidden: Boolean = false,
    val tasbeehLockReason: String = "Tasbeeh Counter is undergoing maintenance.",
    val tasbeehLockCategory: String = "server_maintenance",

    // 7. Names of Allah
    val isNamesLocked: Boolean = false,
    val isNamesHidden: Boolean = false,
    val namesLockReason: String = "Names of Allah library is temporarily undergoing updates.",
    val namesLockCategory: String = "server_maintenance",

    // 8. Zakat Calculator
    val isZakatLocked: Boolean = false,
    val isZakatHidden: Boolean = false,
    val zakatLockReason: String = "Zakat Calculator is temporarily undergoing rate updates.",
    val zakatLockCategory: String = "server_maintenance",

    // 9. Qibla Finder
    val isQiblaLocked: Boolean = false,
    val isQiblaHidden: Boolean = false,
    val qiblaLockReason: String = "Qibla Finder sensor calibration is currently updating.",
    val qiblaLockCategory: String = "server_maintenance",

    // 10. Islamic Calendar
    val isCalendarLocked: Boolean = false,
    val isCalendarHidden: Boolean = false,
    val calendarLockReason: String = "Islamic Hijri Calendar is undergoing moon sighting verification.",
    val calendarLockCategory: String = "server_maintenance",

    // 11. Help & Complaints
    val isComplaintsLocked: Boolean = false,
    val isComplaintsHidden: Boolean = false,
    val complaintsLockReason: String = "Help & Complaints portal is temporarily undergoing server maintenance.",
    val complaintsLockCategory: String = "server_maintenance",

    // 12. Support & Donate
    val isDonateLocked: Boolean = false,
    val isDonateHidden: Boolean = false,
    val donateLockReason: String = "Support & Donate gateway is temporarily offline.",
    val donateLockCategory: String = "server_maintenance",
    
    // Feature/Card Blocking
    val isPrayerTimesCardLocked: Boolean = false,
    val prayerTimesBlockedMessage: String = "Prayer Times are temporarily unavailable.",
    
    val isDailyAyahCardLocked: Boolean = false,
    val dailyAyahBlockedMessage: String = "Daily Ayah is temporarily unavailable.",
    
    val isTrackerCardLocked: Boolean = false,
    val trackerBlockedMessage: String = "Progress Tracker is temporarily unavailable.",
    val welcomeBismillahMessage: String = "Welcome to TaqwaHub! This offline-first Islamic companion was built with complete devotion by a single developer. Because a single developer is human, mistakes, translation errors, or bugs might occasionally slip in. If you find any, please contact us from the settings screen so we can correct them of our own accord. Press the golden Bismillah button below to unlock your spiritual companion.",
    val donateRedirectUrl: String = "https://taqwahub.org/donate",
    val privacyPolicyUrl: String = "https://taqwahub.vercel.app/privacy.html",
    val termsOfServiceUrl: String = "https://taqwahub.vercel.app/terms.html",
    val deleteAccountUrl: String = "https://taqwahub.vercel.app/delete-account.html"
)

class TaqwaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaqwaRepository
    private var isInitialized = false

    fun getPakistanDateString(): String = repository.getPakistanDateString()
    
    var appConfig by mutableStateOf(AppConfig())
        private set

    fun isSurahAudioLocked(surahId: Int): Boolean {
        if (isAdmin) return false // Admins always bypass locks
        if (appConfig.isQuranAudioLocked) return true
        val lockedList = appConfig.lockedSurahIds.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
        return lockedList.contains(surahId)
    }

    fun getSurahAudioBlockedMessage(surahId: Int): String {
        if (appConfig.isQuranAudioLocked) return appConfig.quranAudioBlockedMessage
        return appConfig.surahBlockedMessage
    }

    fun isWordByWordAudioLocked(surahId: Int): Boolean {
        if (isAdmin) return false // Admins always bypass locks
        if (appConfig.isQuranAudioLocked) return true
        val raw = appConfig.lockedWordSurahIds.trim()
        if (raw == "*") return true
        val lockedList = raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
        return lockedList.contains(surahId)
    }

    fun getWordByWordAudioBlockedMessage(surahId: Int): String {
        if (appConfig.isQuranAudioLocked) return appConfig.quranAudioBlockedMessage
        return appConfig.wordSurahBlockedMessage
    }

    val audioPlayerHelper = com.example.util.AudioPlayerHelper(application)
    val wordAudioPlayerHelper = com.example.util.AudioPlayerHelper(application)

    // Authentication State
    var currentUser by mutableStateOf<com.google.firebase.auth.FirebaseUser?>(
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        } catch (e: Throwable) {
            Log.e("TaqwaViewModel", "Firebase Auth could not be initialized gracefully: ${e.message}")
            null
        }
    )

    private var currentUserDocListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var appConfigListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var adminsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun getResolvedUserEmail(): String {
        val fbEmail = currentUser?.email?.lowercase()?.trim()
            ?: try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.lowercase()?.trim() } catch (e: Throwable) { null }
        if (!fbEmail.isNullOrBlank()) {
            try {
                val syncPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                syncPrefs.edit().putString("saved_user_email", fbEmail).apply()
            } catch (e: Throwable) {}
            return fbEmail
        }

        val dbEmail = try { stats.value.email.lowercase().trim() } catch (e: Throwable) { "" }
        if (dbEmail.isNotBlank()) {
            try {
                val syncPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                syncPrefs.edit().putString("saved_user_email", dbEmail).apply()
            } catch (e: Throwable) {}
            return dbEmail
        }

        val sharedPrefEmail = try {
            val syncPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
            syncPrefs.getString("saved_user_email", null)?.lowercase()?.trim()
        } catch (e: Throwable) { null }

        if (!sharedPrefEmail.isNullOrBlank()) return sharedPrefEmail

        return ""
    }

    private fun loadCachedAdminEmails(): List<String> {
        return try {
            val prefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
            val json = prefs.getString("cached_admin_emails", null)
            val list = mutableListOf("kb1747038@gmail.com")
            if (!json.isNullOrBlank()) {
                val array = org.json.JSONArray(json)
                for (i in 0 until array.length()) {
                    val em = array.getString(i).lowercase().trim()
                    if (em.isNotBlank()) list.add(em)
                }
            }
            list.distinct()
        } catch (e: Exception) {
            listOf("kb1747038@gmail.com")
        }
    }

    private fun loadCachedSuperAdminEmails(): List<String> {
        return try {
            val prefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
            val json = prefs.getString("cached_super_admin_emails", null)
            val list = mutableListOf("kb1747038@gmail.com")
            if (!json.isNullOrBlank()) {
                val array = org.json.JSONArray(json)
                for (i in 0 until array.length()) {
                    val em = array.getString(i).lowercase().trim()
                    if (em.isNotBlank()) list.add(em)
                }
            }
            list.distinct()
        } catch (e: Exception) {
            listOf("kb1747038@gmail.com")
        }
    }

    private fun saveCachedAdminEmails(emails: List<String>, supers: List<String> = emptyList()) {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
            val editor = prefs.edit()
            if (emails.isNotEmpty()) {
                val array = org.json.JSONArray()
                (emails + "kb1747038@gmail.com").distinct().forEach { array.put(it.lowercase().trim()) }
                editor.putString("cached_admin_emails", array.toString())
            }
            if (supers.isNotEmpty()) {
                val array = org.json.JSONArray()
                (supers + "kb1747038@gmail.com").distinct().forEach { array.put(it.lowercase().trim()) }
                editor.putString("cached_super_admin_emails", array.toString())
            }
            editor.apply()
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Error saving cached admin emails: ${e.message}")
        }
    }

    var adminEmails by mutableStateOf<List<String>>(loadCachedAdminEmails())
    var superAdminEmails by mutableStateOf<List<String>>(loadCachedSuperAdminEmails())

    // Admin User Directory & Management State
    val adminUserList = androidx.compose.runtime.mutableStateListOf<Pair<String, UserStatsEntity>>()
    var adminTotalUserCount by mutableStateOf(0)
    var isAdminUserLoading by mutableStateOf(false)
    var isAdminUserHasMore by mutableStateOf(true)
    var adminLastUserDoc by mutableStateOf<com.google.firebase.firestore.DocumentSnapshot?>(null)
    var adminUserSearchQuery by mutableStateOf("")

    fun parseAdminUserDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Pair<String, UserStatsEntity>? {
        val uid = doc.id
        val statsMap = doc.get("userStats") as? Map<String, Any>
        val topName = (doc.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("displayName") as? String)?.takeIf { it.isNotBlank() } ?: ""
        val topUsername = (doc.get("username") as? String)?.takeIf { it.isNotBlank() } ?: ""
        val topEmail = (doc.get("email") as? String)?.takeIf { it.isNotBlank() } ?: ""
        val topGender = (doc.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sectGender") as? String)?.takeIf { it.isNotBlank() } ?: ""
        val topSect = (doc.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sect") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("cast") as? String)?.takeIf { it.isNotBlank() } ?: ""

        val stats = if (statsMap != null) {
            UserStatsEntity(
                id = uid.hashCode(),
                totalTasksCompleted = (statsMap["totalTasksCompleted"] as? Long)?.toInt() ?: 0,
                daysActive = (statsMap["daysActive"] as? Long)?.toInt() ?: 1,
                quranProgress = (statsMap["quranProgress"] as? Long)?.toInt() ?: 0,
                lastReadSurah = (statsMap["lastReadSurah"] as? Long)?.toInt() ?: 1,
                lastReadVerse = (statsMap["lastReadVerse"] as? Long)?.toInt() ?: 1,
                lastReadVerseKey = statsMap["lastReadVerseKey"] as? String ?: "1:1",
                tasbeehCount = (statsMap["tasbeehCount"] as? Long)?.toInt() ?: 0,
                lastResetDate = statsMap["lastResetDate"] as? String ?: "",
                currentStreak = (statsMap["currentStreak"] as? Long)?.toInt() ?: 0,
                streakChancesLeft = ((statsMap["streakShields"] as? Long)?.toInt() ?: ((statsMap["streakChancesLeft"] as? Long)?.toInt() ?: 0)).coerceIn(0, 2),
                longestStreak = (statsMap["longestStreak"] as? Long)?.toInt() ?: 0,
                totalXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0,
                weeklyXp = (statsMap["weeklyXp"] as? Long)?.toInt() ?: 0,
                lastActiveWeekOfYear = (statsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0,
                name = (statsMap["name"] as? String)?.takeIf { it.isNotBlank() } ?: topName,
                username = (statsMap["username"] as? String)?.takeIf { it.isNotBlank() } ?: topUsername,
                gender = (statsMap["gender"] as? String)?.takeIf { it.isNotBlank() } ?: topGender,
                sectOrCast = (statsMap["sectOrCast"] as? String)?.takeIf { it.isNotBlank() } ?: topSect,
                email = (statsMap["email"] as? String)?.takeIf { it.isNotBlank() } ?: topEmail,
                completedSurahs = statsMap["completedSurahs"] as? String ?: "",
                firstPlaceCount = (statsMap["firstPlaceCount"] as? Long)?.toInt() ?: 0,
                secondPlaceCount = (statsMap["secondPlaceCount"] as? Long)?.toInt() ?: 0,
                thirdPlaceCount = (statsMap["thirdPlaceCount"] as? Long)?.toInt() ?: 0,
                isBlocked = statsMap["isBlocked"] as? Boolean ?: false,
                isVerified = statsMap["isVerified"] as? Boolean ?: false,
                profilePictureBase64 = statsMap["profilePictureBase64"] as? String ?: "",
                lastWeekXp = (statsMap["lastWeekXp"] as? Long)?.toInt() ?: 0,
                lastWeekCode = (statsMap["lastWeekCode"] as? Long)?.toInt() ?: 0,
                lastActiveDate = statsMap["lastActiveDate"] as? String ?: "",
                streakShields = ((statsMap["streakShields"] as? Long)?.toInt() ?: ((statsMap["streakChancesLeft"] as? Long)?.toInt() ?: 0)).coerceIn(0, 2),
                maxShields = 2,
                frozenDates = statsMap["frozenDates"] as? String ?: "",
                activeDates = statsMap["activeDates"] as? String ?: "",
                lastShieldUsedDate = statsMap["lastShieldUsedDate"] as? String ?: "",
                streakRepairsAvailable = (statsMap["streakRepairsAvailable"] as? Long)?.toInt() ?: 1
            )
        } else {
            UserStatsEntity(
                id = uid.hashCode(),
                name = topName,
                username = topUsername,
                email = topEmail,
                gender = topGender,
                sectOrCast = topSect
            )
        }
        return Pair(uid, stats)
    }

    fun loadAdminUsers(reset: Boolean = false) {
        if (reset) {
            adminUserList.clear()
            adminLastUserDoc = null
            isAdminUserHasMore = true
        }
        if (isAdminUserLoading) return
        isAdminUserLoading = true

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        if (adminTotalUserCount == 0 || reset) {
            try {
                db.collection("users").count().get(com.google.firebase.firestore.AggregateSource.SERVER)
                    .addOnSuccessListener { aggregateSnapshot ->
                        if (aggregateSnapshot != null) {
                            adminTotalUserCount = aggregateSnapshot.count.toInt()
                        }
                    }
                    .addOnFailureListener {
                        // Graceful fallback with limit
                        db.collection("users").limit(100).get().addOnSuccessListener { snap ->
                            if (snap != null) adminTotalUserCount = snap.size()
                        }
                    }
            } catch (e: Throwable) {
                db.collection("users").limit(100).get().addOnSuccessListener { snap ->
                    if (snap != null) adminTotalUserCount = snap.size()
                }
            }
        }

        var query = db.collection("users").limit(20)
        val lastDoc = adminLastUserDoc
        if (!reset && lastDoc != null) {
            query = query.startAfter(lastDoc)
        }

        query.get().addOnSuccessListener { snapshot ->
            isAdminUserLoading = false
            if (snapshot != null && !snapshot.isEmpty) {
                adminLastUserDoc = snapshot.documents.last()
                val newItems = snapshot.documents.mapNotNull { doc -> parseAdminUserDoc(doc) }
                val existingUids = adminUserList.map { it.first }.toSet()
                val uniqueNewItems = newItems.filter { !existingUids.contains(it.first) }
                adminUserList.addAll(uniqueNewItems)
                if (snapshot.documents.size < 20) {
                    isAdminUserHasMore = false
                }
            } else {
                isAdminUserHasMore = false
            }
        }.addOnFailureListener {
            isAdminUserLoading = false
        }
    }

    fun searchAdminUsersRemote(queryStr: String, onResult: ((Int) -> Unit)? = null) {
        val clean = queryStr.trim().lowercase()
        if (clean.isBlank()) {
            onResult?.invoke(0)
            return
        }
        isAdminUserLoading = true
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Search recent active users up to 100 items safely
        db.collection("users").limit(100).get().addOnSuccessListener { snapshot ->
            isAdminUserLoading = false
            if (snapshot != null && !snapshot.isEmpty) {
                val matching = snapshot.documents.mapNotNull { doc -> parseAdminUserDoc(doc) }
                    .filter { (uid, stats) ->
                        stats.username.lowercase().contains(clean) ||
                        stats.name.lowercase().contains(clean) ||
                        stats.email.lowercase().contains(clean) ||
                        uid.lowercase().contains(clean)
                    }
                val existingUids = adminUserList.map { it.first }.toSet()
                val newToAdd = matching.filter { !existingUids.contains(it.first) }
                adminUserList.addAll(0, newToAdd)
                onResult?.invoke(matching.size)
            } else {
                onResult?.invoke(0)
            }
        }.addOnFailureListener {
            isAdminUserLoading = false
            onResult?.invoke(0)
        }
    }

    val authenticatedFirebaseEmail: String
        get() = getResolvedUserEmail()

    val isSuperAdmin: Boolean
        get() {
            val email = authenticatedFirebaseEmail
            if (email.isBlank()) return false
            if (email == "kb1747038@gmail.com") return true
            return superAdminEmails.any { it.lowercase().trim() == email }
        }

    val isAdmin: Boolean
        get() {
            val email = authenticatedFirebaseEmail
            if (email.isBlank()) return false
            if (email == "kb1747038@gmail.com") return true
            if (isSuperAdmin) return true
            return adminEmails.any { it.lowercase().trim() == email }
        }

    var isGuestEnabled by mutableStateOf(false)

    var isSyncingData by mutableStateOf(false)
        private set
    var hasCompletedInitialSync by mutableStateOf(false)
        private set

    private var debouncedSyncJob: kotlinx.coroutines.Job? = null

    // Thread-safe in-memory buffers to accumulate achievements earned during an active remote pull fetch
    private val pendingXpBuffer = java.util.concurrent.atomic.AtomicInteger(0)
    private val pendingWeeklyXpBuffer = java.util.concurrent.atomic.AtomicInteger(0)
    private val pendingCompletedTasksBuffer = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    private val pendingCompletedSurahsBuffer = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    private val pendingTasbeehBuffer = java.util.concurrent.atomic.AtomicInteger(0)

    private val _networkStatus = MutableStateFlow(TaqwaNetworkStatusInfo())
    val networkStatus: StateFlow<TaqwaNetworkStatusInfo> = _networkStatus.asStateFlow()
    
    // Quran Offline Downloads State
    var downloadedSurahIds by mutableStateOf<Set<Int>>(emptySet())
    var downloadingSurahIds by mutableStateOf<Set<Int>>(emptySet())
    var downloadProgress by mutableStateOf<Map<Int, Float>>(emptyMap())
    var downloadSizeStatus by mutableStateOf<Map<Int, String>>(emptyMap())
    var audioOverrides by mutableStateOf<Map<String, String>>(emptyMap())

    var userLatitude by mutableStateOf(33.6844) // Default Islamabad
    var userLongitude by mutableStateOf(73.0479) // Default Islamabad
    var hasLocationPermission by mutableStateOf(false)

    // Live Prayer timings
    private val _prayerTimes = MutableStateFlow<AladhanTimings?>(null)
    val prayerTimes: StateFlow<AladhanTimings?> = _prayerTimes.asStateFlow()

    // 15-second reactive ticker for dynamic prayer locking/unlocking transitions
    val prayerTicker: StateFlow<Long> = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(15_000L)
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Alarm Settings State variables (OFF by default for Play Policy compliance and user choice)
    var isPrayerAlarmEnabled by mutableStateOf(false)
    var isFajrAlarmEnabled by mutableStateOf(false)
    var isDhuhrAlarmEnabled by mutableStateOf(false)
    var isAsrAlarmEnabled by mutableStateOf(false)
    var isMaghribAlarmEnabled by mutableStateOf(false)
    var isIshaAlarmEnabled by mutableStateOf(false)

    // Jamat Settings State variables (backed by SharedPreferences/SecurePreferences)
    var fajrJamatOffset by mutableStateOf(25)
        private set
    var dhuhrJamatOffset by mutableStateOf(15)
        private set
    var asrJamatOffset by mutableStateOf(15)
        private set
    var maghribJamatOffset by mutableStateOf(7)
        private set
    var ishaJamatOffset by mutableStateOf(20)
        private set
    
    var customAdhanUri by mutableStateOf<String?>(null)
        private set
    
    // Quran Script Setup
    var quranScript by mutableStateOf("uthmani")
        private set

    // Reciter State
    var selectedReciterId by mutableStateOf(7) // Mishary Alafasy default

    // Security Settings & Realtime Scan States
    var isSecurityAntiSpyEnabled by mutableStateOf(false)
        private set
    var isSecurityStrictEnvBlockEnabled by mutableStateOf(false)
        private set
    var isMultiLingualGreetingEnabled by mutableStateOf(true)
        private set

    var signatureCheckResult by mutableStateOf<com.example.util.SignatureCheckResult?>(null)
        private set
    var packageCheckResult by mutableStateOf<com.example.util.PackageCheckResult?>(null)
        private set
    var rootCheckResult by mutableStateOf<com.example.util.RootCheckResult?>(null)
        private set
    var emulatorCheckResult by mutableStateOf<com.example.util.EmulatorCheckResult?>(null)
        private set

    private fun getSecurePrefs(): android.content.SharedPreferences {
        return com.example.util.SecurePreferences.getSecurePrefs(getApplication())
    }

    private val localMoshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private fun getDownloadsDir(): File {
        val app = getApplication<Application>()
        // 1. Safe internal directory fallback by default
        val internalDir = File(app.filesDir, "Taqwahub/downloads")
        
        try {
            // Try scoped external files first (doesn't require runtime storage permissions)
            val extDir = File(app.getExternalFilesDir(null), "Taqwahub/downloads")
            if (!extDir.exists()) {
                extDir.mkdirs()
            }
            if (extDir.exists()) {
                return extDir
            }
        } catch (e: Throwable) {
            // Ignore & fall through
        }

        try {
            // Try public downloads if accessible
            val publicDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "Taqwahub")
            if (!publicDir.exists()) {
                publicDir.mkdirs()
            }
            if (publicDir.exists() && publicDir.canWrite()) {
                return publicDir
            }
        } catch (e: Throwable) {
            // Ignore & fall through
        }

        try {
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
        } catch (e: Throwable) {
            // Absolute fall-safe
        }
        return internalDir
    }

    fun scanDownloadedSurahs() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = mutableSetOf<Int>()
            try {
                val dir = getDownloadsDir()
                val files = dir.listFiles()
                if (files != null) {
                    files.forEach { file ->
                        if (file.name.startsWith("verses_") && file.name.endsWith(".json")) {
                            val idStr = file.name.substringAfter("verses_").substringBefore(".json")
                            val id = idStr.toIntOrNull()
                            if (id != null) {
                                ids.add(id)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Scan downloaded surahs failed", e)
            }
            withContext(Dispatchers.Main) {
                downloadedSurahIds = ids
            }
        }
    }

    // Dynamic Hadith list state for ultra performance paging/loading
    // --- Hadith Book States ---
    var activeHadithBookKey by mutableStateOf<String>("bukhari")
    var activeHadithBookDownloaded by mutableStateOf(false)
    var isHadithBookDownloading by mutableStateOf(false)
    var isHadithBookLoading by mutableStateOf(false)
    var activeHadithList by mutableStateOf<List<com.example.data.HadithBookService.DownloadedHadith>>(emptyList())
    var activeHadithChapters by mutableStateOf<List<com.example.data.HadithBookService.HadithChapter>>(emptyList())
    var selectedHadithChapterNumber by mutableStateOf<Int?>(null)
    var currentHadithDownloadProgress by mutableStateOf<Float?>(null)

    fun selectHadithBook(bookKey: String, bookName: String) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val isDownloaded = com.example.data.HadithBookService.isBookDownloaded(app, bookKey)
            activeHadithBookDownloaded = isDownloaded
            if (activeHadithBookKey != bookKey || activeHadithList.isEmpty()) {
                activeHadithBookKey = bookKey
                selectedHadithChapterNumber = null
                if (isDownloaded) {
                    isHadithBookLoading = true
                    val loaded = com.example.data.HadithBookService.loadBook(app, bookKey, bookName)
                    activeHadithList = loaded
                    activeHadithChapters = com.example.data.HadithBookService.extractChapters(loaded)
                    isHadithBookLoading = false
                } else {
                    activeHadithList = emptyList()
                    activeHadithChapters = emptyList()
                }
            } else {
                activeHadithBookKey = bookKey
            }
        }
    }

    fun selectHadithChapter(chapterNumber: Int?) {
        selectedHadithChapterNumber = chapterNumber
    }

    fun downloadHadithBook(bookKey: String, bookName: String) {
        viewModelScope.launch {
            isHadithBookDownloading = true
            currentHadithDownloadProgress = null
            try {
                val app = getApplication<Application>()
                com.example.data.HadithBookService.downloadBook(app, bookKey)
                activeHadithBookDownloaded = true
                isHadithBookLoading = true
                selectedHadithChapterNumber = null
                val loaded = com.example.data.HadithBookService.loadBook(app, bookKey, bookName)
                activeHadithList = loaded
                activeHadithChapters = com.example.data.HadithBookService.extractChapters(loaded)
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error downloading book: $bookKey", e)
            } finally {
                isHadithBookDownloading = false
                isHadithBookLoading = false
                currentHadithDownloadProgress = null
            }
        }
    }

    var dynamicHadithList by mutableStateOf<List<Hadith>>(emptyList())

    // Dynamic Dua list state for custom additions and removals
    private val duaSyncMutex = Mutex()
    var dynamicDuaList by mutableStateOf<List<Dua>>(emptyList())
    var isDuasLoading by mutableStateOf(false)
    var duasError by mutableStateOf<String?>(null)

    // Announcements, Updates & Reminders
    var announcementsList by mutableStateOf<List<Announcement>>(emptyList())

    // User Bug Reports and Suggestions
    var bugReportsList by mutableStateOf<List<BugReport>>(emptyList())
    var userBugReportsList by mutableStateOf<List<BugReport>>(emptyList())
    var aiFeedbackList by mutableStateOf<List<com.example.data.AiFeedback>>(emptyList())

    // Active Users count
    var totalUsersCount by mutableStateOf(1)

    fun loadHadiths() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<Hadith>()
            // 1. Add static built-in hadiths
            list.addAll(IslamicData.hadiths)

            // 2. Load verified hadiths from assets file
            try {
                val app = getApplication<Application>()
                val assetManager = app.assets
                val inputStream = assetManager.open("hadiths_verified.json")
                val json = inputStream.bufferedReader().use { it.readText() }
                
                val verifiedAdapter = localMoshi.adapter<List<Hadith>>(
                    Types.newParameterizedType(List::class.java, Hadith::class.java)
                )
                val loadedVerified = verifiedAdapter.fromJson(json)
                if (loadedVerified != null) {
                    val existingIds = list.map { it.id }.toSet()
                    val partition = loadedVerified.filter { it.id !in existingIds }
                    list.addAll(partition)
                }
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Failed to load verified hadiths from assets: ${e.message}")
            }

            // Removed dummy generated hadiths to ensure accuracy and real numbering

            // 4. Load user-added custom hadiths from private filesDir (retained for backward compatibility logs)
            try {
                val app = getApplication<Application>()
                val customFile = File(app.filesDir, "hadiths_custom.json")
                if (customFile.exists()) {
                    val customJson = customFile.readText()
                    val customAdapter = localMoshi.adapter<List<Hadith>>(
                        Types.newParameterizedType(List::class.java, Hadith::class.java)
                    )
                    val loadedCustom = customAdapter.fromJson(customJson)
                    if (loadedCustom != null) {
                        val existingIds = list.map { it.id }.toSet()
                        val partition = loadedCustom.filter { it.id !in existingIds }
                        list.addAll(partition)
                    }
                }
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Failed to load custom hadiths: ${e.message}")
            }

            launch(Dispatchers.Main) {
                dynamicHadithList = list
            }

            // 5. Try live fetch if online
            fetchHadithsFromFirestore()
        }
    }

    fun loadDuas() {
        viewModelScope.launch(Dispatchers.IO) {
            isDuasLoading = true
            duasError = null
            try {
                duaSyncMutex.withLock {
                    val list = mutableListOf<Dua>()
                    // Load custom/admin Duas from local cache
                    try {
                        val app = getApplication<Application>()
                        val customFile = File(app.filesDir, "duas_custom.json")
                        if (customFile.exists()) {
                            val customJson = customFile.readText()
                            val customAdapter = localMoshi.adapter<List<Dua>>(
                                Types.newParameterizedType(List::class.java, Dua::class.java)
                            )
                            val loadedCustom = customAdapter.fromJson(customJson)
                            if (loadedCustom != null && loadedCustom.isNotEmpty()) {
                                list.addAll(loadedCustom)
                            }
                        }
                    } catch (e: Throwable) {
                        Log.e("TaqwaViewModel", "Failed to load cached custom duas: ${e.message}")
                    }

                    withContext(Dispatchers.Main) {
                        dynamicDuaList = list.toList()
                    }
                }

                // Sync live from Firestore
                fetchDuasFromFirestore()
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error in loadDuas: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) {
                    isDuasLoading = false
                }
            }
        }
    }

    fun loadAnnouncements() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<Announcement>()
            try {
                val app = getApplication<Application>()
                val customFile = File(app.filesDir, "announcements_custom.json")
                if (customFile.exists()) {
                    val customJson = customFile.readText()
                    val customAdapter = localMoshi.adapter<List<Announcement>>(
                        Types.newParameterizedType(List::class.java, Announcement::class.java)
                    )
                    val loaded = customAdapter.fromJson(customJson)
                    if (loaded != null) {
                        list.addAll(loaded)
                    }
                }
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Failed to load cached announcements: ${e.message}")
            }

            launch(Dispatchers.Main) {
                announcementsList = list.sortedByDescending { it.timestamp }
            }

            // Sync from Firestore if online
            fetchAnnouncementsFromFirestore()
        }
    }

    fun fetchDuasFromFirestore() {
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("custom_duas").get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null) {
                            val remoteDuas = mutableListOf<Dua>()
                            for (doc in snapshot.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val category = doc.getString("category") ?: "Other"
                                    val reference = doc.getString("reference") ?: ""
                                    val arabic = doc.getString("arabic") ?: ""
                                    val transliteration = doc.getString("transliteration") ?: ""
                                    val translation = doc.getString("translation") ?: ""
                                    val translationUrdu = doc.getString("translationUrdu") ?: ""
                                    
                                    remoteDuas.add(
                                        Dua(id, category, reference, arabic, transliteration, translation, translationUrdu)
                                    )
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error parsing remote dua: ${doc.id}", e)
                                }
                            }
                            
                            viewModelScope.launch(Dispatchers.IO) {
                                duaSyncMutex.withLock {
                                    saveCustomDuasLocally(remoteDuas)
                                    withContext(Dispatchers.Main) {
                                        dynamicDuaList = remoteDuas
                                    }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Firestore fetch custom duas failed: ${e.message}")
            }
        }
    }

    private fun saveCustomDuasLocally(duas: List<Dua>) {
        try {
            val app = getApplication<Application>()
            val customFile = File(app.filesDir, "duas_custom.json")
            val writeAdapter = localMoshi.adapter<List<Dua>>(
                Types.newParameterizedType(List::class.java, Dua::class.java)
            )
            val jsonToWrite = writeAdapter.toJson(duas)
            customFile.writeText(jsonToWrite)
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Failed to cache custom duas locally", e)
        }
    }

    fun fetchHadithsFromFirestore() {
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("custom_hadiths").get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null) {
                            val remoteHadiths = mutableListOf<Hadith>()
                            for (doc in snapshot.documents) {
                                try {
                                    val idStr = doc.getString("id") ?: doc.id
                                    val numericId = idStr.toIntOrNull() ?: doc.id.hashCode()
                                    val chapter = doc.getString("chapter") ?: "Worship"
                                    val narrator = doc.getString("narrator") ?: ""
                                    val source = doc.getString("source") ?: ""
                                    val text = doc.getString("text") ?: ""
                                    val arabic = doc.getString("arabic") ?: ""
                                    val translationUrdu = doc.getString("translationUrdu") ?: ""
                                    val transliteration = doc.getString("transliteration") ?: ""
                                    
                                    remoteHadiths.add(
                                        Hadith(numericId, chapter, narrator, source, text, arabic, translationUrdu, transliteration)
                                    )
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error parsing remote hadith doc", e)
                                }
                            }
                            
                            saveCustomHadithsLocally(remoteHadiths)

                            viewModelScope.launch(Dispatchers.Main) {
                                val baseList = mutableListOf<Hadith>()
                                baseList.addAll(IslamicData.hadiths)
                                try {
                                    val app = getApplication<Application>()
                                    val inputStream = app.assets.open("hadiths_verified.json")
                                    val json = inputStream.bufferedReader().use { it.readText() }
                                    val verifiedAdapter = localMoshi.adapter<List<Hadith>>(
                                        Types.newParameterizedType(List::class.java, Hadith::class.java)
                                    )
                                    val loadedVerified = verifiedAdapter.fromJson(json)
                                    if (loadedVerified != null) {
                                        val existingIds = baseList.map { it.id }.toSet()
                                        baseList.addAll(loadedVerified.filter { it.id !in existingIds })
                                    }
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Failed to read verified assets in fetch", e)
                                }
                                
                                val remoteIds = remoteHadiths.map { it.id }.toSet()
                                val filteredBaseList = baseList.filter { it.id !in remoteIds }
                                dynamicHadithList = filteredBaseList + remoteHadiths
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Firestore fetch custom hadiths failed", e)
            }
        }
    }

    private fun saveCustomHadithsLocally(hadiths: List<Hadith>) {
        try {
            val app = getApplication<Application>()
            val customFile = File(app.filesDir, "hadiths_custom.json")
            val writeAdapter = localMoshi.adapter<List<Hadith>>(
                Types.newParameterizedType(List::class.java, Hadith::class.java)
            )
            val jsonToWrite = writeAdapter.toJson(hadiths)
            customFile.writeText(jsonToWrite)
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Failed to cache custom hadiths locally-json", e)
        }
    }

    private var announcementsListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun fetchAnnouncementsFromFirestore() {
        if (announcementsListener != null) return // Active real-time listener already attached
        try {
            val db = FirebaseFirestore.getInstance()
            announcementsListener = db.collection("announcements")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaqwaViewModel", "Firestore announcements listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val remoteList = mutableListOf<Announcement>()
                        for (doc in snapshot.documents) {
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val title = doc.getString("title") ?: ""
                                val message = doc.getString("message") ?: ""
                                val type = doc.getString("type") ?: "Announcement"
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                
                                remoteList.add(Announcement(id, title, message, type, timestamp))
                            } catch (e: Exception) {
                                Log.e("TaqwaViewModel", "Error parsing remote announcement", e)
                            }
                        }
                        
                        saveAnnouncementsLocally(remoteList)

                        viewModelScope.launch(Dispatchers.Main) {
                            announcementsList = remoteList.sortedByDescending { it.timestamp }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Firestore fetch announcements failed: ${e.message}")
        }
    }

    fun submitBugReport(
        subject: String,
        description: String,
        type: String,
        imageUrl: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val id = db.collection("bug_reports").document().id
                val userId = currentUser?.uid ?: "guest"
                val userEmail = currentUser?.email ?: "guest@taqwahub.app"
                val timestamp = System.currentTimeMillis()
                val appVersion = "1.0.4"
                val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                
                val reportMap = hashMapOf(
                    "id" to id,
                    "userId" to userId,
                    "userEmail" to userEmail,
                    "subject" to subject,
                    "description" to description,
                    "type" to type,
                    "status" to "Pending",
                    "timestamp" to timestamp,
                    "appVersion" to appVersion,
                    "deviceModel" to deviceModel,
                    "imageUrl" to imageUrl,
                    "adminReply" to "",
                    "adminReplyTimestamp" to 0L
                )
                
                db.collection("bug_reports").document(id).set(reportMap)
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.Main) {
                            onResult(true, null)
                        }
                        fetchUserBugReportsFromFirestore()
                        if (isAdmin) {
                            fetchBugReportsFromFirestore()
                        }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) {
                            onResult(false, e.message)
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error submitting report", e)
                viewModelScope.launch(Dispatchers.Main) {
                    onResult(false, e.message)
                }
            }
        }
    }

    fun fetchBugReportsFromFirestore() {
        if (!isAdmin) return
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("bug_reports")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null) {
                            val remoteReports = mutableListOf<BugReport>()
                            for (doc in snapshot.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val userId = doc.getString("userId") ?: ""
                                    val userEmail = doc.getString("userEmail") ?: ""
                                    val subject = doc.getString("subject") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val type = doc.getString("type") ?: "Bug"
                                    val status = doc.getString("status") ?: "Pending"
                                    val timestamp = doc.getLong("timestamp") ?: 0L
                                    val appVersion = doc.getString("appVersion") ?: ""
                                    val deviceModel = doc.getString("deviceModel") ?: ""
                                    val imageUrl = doc.getString("imageUrl") ?: ""
                                    val adminReply = doc.getString("adminReply") ?: ""
                                    val adminReplyTimestamp = doc.getLong("adminReplyTimestamp") ?: 0L
                                    
                                    remoteReports.add(
                                        BugReport(
                                            id = id,
                                            userId = userId,
                                            userEmail = userEmail,
                                            subject = subject,
                                            description = description,
                                            type = type,
                                            status = status,
                                            timestamp = timestamp,
                                            appVersion = appVersion,
                                            deviceModel = deviceModel,
                                            imageUrl = imageUrl,
                                            adminReply = adminReply,
                                            adminReplyTimestamp = adminReplyTimestamp
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error parsing bug report", e)
                                }
                            }
                            
                            viewModelScope.launch(Dispatchers.Main) {
                                bugReportsList = remoteReports.sortedByDescending { it.timestamp }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to fetch bug reports", e)
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to fetch bug reports from firestore", e)
            }
        }
    }

    fun fetchAiFeedbacksFromFirestore() {
        if (!isAdmin) return
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("ai_responses_feedback")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null) {
                            val remoteFeedbacks = mutableListOf<com.example.data.AiFeedback>()
                            for (doc in snapshot.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val userId = doc.getString("userId") ?: ""
                                    val userEmail = doc.getString("userEmail") ?: ""
                                    val queryText = doc.getString("query") ?: ""
                                    val responseText = doc.getString("response") ?: ""
                                    val rating = doc.getString("rating") ?: "none"
                                    val reportMsg = doc.getString("reportMessage") ?: ""
                                    val ts = doc.getLong("timestamp") ?: 0L
                                    
                                    remoteFeedbacks.add(
                                        com.example.data.AiFeedback(
                                            id = id,
                                            userId = userId,
                                            userEmail = userEmail,
                                            query = queryText,
                                            response = responseText,
                                            rating = rating,
                                            reportMessage = reportMsg,
                                            timestamp = ts
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error parsing ai feedback", e)
                                }
                            }
                            viewModelScope.launch(Dispatchers.Main) {
                                aiFeedbackList = remoteFeedbacks.sortedByDescending { it.timestamp }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to fetch ai feedbacks", e)
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to fetch ai feedbacks from firestore", e)
            }
        }
    }

    fun submitAiFeedback(messageId: String, query: String, responseText: String, rating: String, reportMessage: String = "") {
        // Update local state first for immediate UI update
        val updatedMsgs = chatMessages.map { msg ->
            if (msg.id == messageId) {
                msg.copy(rating = rating, reportMessage = reportMessage)
            } else {
                msg
            }
        }
        chatMessages = updatedMsgs
        saveChatState()

        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val feedbackPayload = hashMapOf(
                    "id" to "msg_$messageId",
                    "userId" to (currentUser?.uid ?: "guest"),
                    "userEmail" to (currentUser?.email ?: "Guest User"),
                    "query" to query,
                    "response" to responseText,
                    "rating" to rating,
                    "reportMessage" to reportMessage,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("ai_responses_feedback")
                    .document("msg_$messageId")
                    .set(feedbackPayload)
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "AI response feedback submitted successfully")
                        // If admin is doing it or viewing it, refresh feedback list
                        if (isAdmin) {
                            fetchAiFeedbacksFromFirestore()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to submit AI response feedback", e)
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to submit AI response feedback to Firestore", e)
            }
        }
    }

    fun deleteAiFeedback(feedbackId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (!isAdmin) return
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("ai_responses_feedback")
                    .document(feedbackId)
                    .delete()
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.Main) {
                            fetchAiFeedbacksFromFirestore()
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) {
                            onFailure(e.message ?: "Unknown error")
                        }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onFailure(e.message ?: "Exception occurred")
                }
            }
        }
    }

    fun fetchUserBugReportsFromFirestore() {
        val user = currentUser ?: return
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("bug_reports")
                    .whereEqualTo("userId", user.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null) {
                            val remoteReports = mutableListOf<BugReport>()
                            for (doc in snapshot.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val userId = doc.getString("userId") ?: ""
                                    val userEmail = doc.getString("userEmail") ?: ""
                                    val subject = doc.getString("subject") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val type = doc.getString("type") ?: "Bug"
                                    val status = doc.getString("status") ?: "Pending"
                                    val timestamp = doc.getLong("timestamp") ?: 0L
                                    val appVersion = doc.getString("appVersion") ?: ""
                                    val deviceModel = doc.getString("deviceModel") ?: ""
                                    val imageUrl = doc.getString("imageUrl") ?: ""
                                    val adminReply = doc.getString("adminReply") ?: ""
                                    val adminReplyTimestamp = doc.getLong("adminReplyTimestamp") ?: 0L
                                    
                                    remoteReports.add(
                                        BugReport(
                                            id = id,
                                            userId = userId,
                                            userEmail = userEmail,
                                            subject = subject,
                                            description = description,
                                            type = type,
                                            status = status,
                                            timestamp = timestamp,
                                            appVersion = appVersion,
                                            deviceModel = deviceModel,
                                            imageUrl = imageUrl,
                                            adminReply = adminReply,
                                            adminReplyTimestamp = adminReplyTimestamp
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error parsing user bug report", e)
                                }
                            }
                            
                            val lastSeen = getLastSeenComplaintsTime()
                            var unreadReplyFound = false
                            for (report in remoteReports) {
                                if (report.adminReply.isNotBlank() && report.adminReplyTimestamp > lastSeen) {
                                    unreadReplyFound = true
                                    break
                                }
                            }
                            viewModelScope.launch(Dispatchers.Main) {
                                userBugReportsList = remoteReports.sortedByDescending { it.timestamp }
                                if (currentView != "user_complaints") {
                                    hasUnreadSupportReply = unreadReplyFound
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to fetch user bug reports", e)
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to fetch user bug reports from firestore", e)
            }
        }
    }

    fun replyToBugReport(reportId: String, replyText: String, onResult: (Boolean, String?) -> Unit) {
        if (!isAdmin) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val updates = hashMapOf<String, Any>(
                    "adminReply" to replyText,
                    "adminReplyTimestamp" to System.currentTimeMillis(),
                    "status" to "In Progress"
                )
                db.collection("bug_reports").document(reportId)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Successfully replied to bug report")
                        viewModelScope.launch(Dispatchers.Main) {
                            onResult(true, null)
                        }
                        fetchBugReportsFromFirestore()
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to reply to bug report", e)
                        viewModelScope.launch(Dispatchers.Main) {
                            onResult(false, e.message)
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error replying to bug report", e)
                viewModelScope.launch(Dispatchers.Main) {
                    onResult(false, e.message)
                }
            }
        }
    }

    fun updateBugReportStatus(reportId: String, newStatus: String) {
        if (!isAdmin) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("bug_reports").document(reportId)
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Successfully updated bug report status")
                        fetchBugReportsFromFirestore()
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to update bug report status", e)
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error updating bug report status", e)
            }
        }
    }

    fun deleteBugReport(reportId: String) {
        if (!isAdmin) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("bug_reports").document(reportId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Successfully deleted bug report")
                        fetchBugReportsFromFirestore()
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Failed to delete bug report", e)
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error deleting bug report", e)
            }
        }
    }

    private fun saveAnnouncementsLocally(list: List<Announcement>) {
        try {
            val app = getApplication<Application>()
            val customFile = File(app.filesDir, "announcements_custom.json")
            val writeAdapter = localMoshi.adapter<List<Announcement>>(
                Types.newParameterizedType(List::class.java, Announcement::class.java)
            )
            val jsonToWrite = writeAdapter.toJson(list)
            customFile.writeText(jsonToWrite)
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Failed to cache announcements locally", e)
        }
    }

    fun addCustomDua(dua: Dua, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to dua.id,
                    "category" to dua.category,
                    "reference" to dua.reference,
                    "arabic" to dua.arabic,
                    "transliteration" to dua.transliteration,
                    "translation" to dua.translation,
                    "translationUrdu" to dua.translationUrdu,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("custom_duas").document(dua.id)
                    .set(data)
                    .addOnSuccessListener {
                        fetchDuasFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Dua save failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal save error") }
            }
        }
    }

    fun updateDua(dua: Dua, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to dua.id,
                    "category" to dua.category,
                    "reference" to dua.reference,
                    "arabic" to dua.arabic,
                    "transliteration" to dua.transliteration,
                    "translation" to dua.translation,
                    "translationUrdu" to dua.translationUrdu,
                    "lastEdited" to System.currentTimeMillis()
                )
                db.collection("custom_duas").document(dua.id)
                    .set(data)
                    .addOnSuccessListener {
                        fetchDuasFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Dua update failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal update error") }
            }
        }
    }

    fun deleteCustomDua(duaId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("custom_duas").document(duaId)
                    .delete()
                    .addOnSuccessListener {
                        fetchDuasFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Dua delete failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal delete error") }
            }
        }
    }

    fun addCustomHadith(hadith: Hadith, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val idStr = hadith.id.toString()
                val data = hashMapOf(
                    "id" to idStr,
                    "chapter" to hadith.chapter,
                    "narrator" to hadith.narrator,
                    "source" to hadith.source,
                    "text" to hadith.text,
                    "arabic" to hadith.arabic,
                    "translationUrdu" to hadith.translationUrdu,
                    "transliteration" to hadith.transliteration,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("custom_hadiths").document(idStr)
                    .set(data)
                    .addOnSuccessListener {
                        fetchHadithsFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Hadith save failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal check error") }
            }
        }
    }

    fun deleteCustomHadith(hadithId: Int, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("custom_hadiths").document(hadithId.toString())
                    .delete()
                    .addOnSuccessListener {
                        fetchHadithsFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Hadith delete failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal delete error") }
            }
        }
    }

    fun addAnnouncement(announcement: Announcement, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "id" to announcement.id,
                    "title" to announcement.title,
                    "message" to announcement.message,
                    "type" to announcement.type,
                    "timestamp" to announcement.timestamp
                )
                db.collection("announcements").document(announcement.id)
                    .set(data)
                    .addOnSuccessListener {
                        fetchAnnouncementsFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Announcement save failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal save error") }
            }
        }
    }

    fun deleteAnnouncement(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("announcements").document(id)
                    .delete()
                    .addOnSuccessListener {
                        fetchAnnouncementsFromFirestore()
                        viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Announcement delete failed") }
                    }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Internal delete error") }
            }
        }
    }

    fun fetchTotalUsersCount() {
        if (networkStatus.value.type == TaqwaNetworkType.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null) {
                            viewModelScope.launch(Dispatchers.Main) {
                                totalUsersCount = snapshot.size()
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to fetch users count: ${e.message}")
            }
        }
    }

    init {
        val database = (application as TaqwaApplication).database
        repository = TaqwaRepository(database.taqwaDao(), application.cacheDir)
        
        // Background prefetch popular Surahs (Al-Fatihah, Yaseen, Al-Mulk, etc.) for instant loading
        viewModelScope.launch(Dispatchers.IO) {
            delay(1500)
            prefetchPopularSurahs()
        }

        // Load cached prayer times and timezone at startup
        val initialCachedTimings = com.example.widget.WidgetHelper.getPrayerTimesTimings(application)
        val initialCachedTz = com.example.widget.WidgetHelper.getPrayerTimezone(application)
        if (initialCachedTimings != null) {
            _prayerTimes.value = initialCachedTimings
            repository.cachedPrayerTimes = initialCachedTimings
            repository.cachedTimezone = initialCachedTz
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentLocalStats = repository.taqwaDao.getUserStatsDirect()
                if (currentLocalStats == null) {
                    Log.d("TaqwaViewModel", "Local stats are null. Room database was likely wiped or is brand new. Resetting last_local_update in SharedPreferences to 0L to prevent empty database push overwrites.")
                    val sharedPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().putLong("last_local_update", 0L).apply()
                }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error checking for wiped local database on startup", e)
            }
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSecurePrefs()
                val pAlarmEnabled = prefs.getBoolean("prayer_alarm_enabled", false)
                val fAlarm = prefs.getBoolean("alarm_fajr", false)
                val dAlarm = prefs.getBoolean("alarm_dhuhr", false)
                val aAlarm = prefs.getBoolean("alarm_asr", false)
                val mAlarm = prefs.getBoolean("alarm_maghrib", false)
                val iAlarm = prefs.getBoolean("alarm_isha", false)
                val adhanUri = prefs.getString("custom_adhan_uri", null)
                
                val isGuest = prefs.getBoolean("is_guest_mode", false)

                val qScript = prefs.getString("quran_script", "uthmani") ?: "uthmani"

                val spyEnabled = prefs.getBoolean("security_anti_spy", false)
                val strictEnv = prefs.getBoolean("security_strict_env_block", false)
                val multiGreeting = prefs.getBoolean("multi_lingual_greeting", true)
                
                val fOffset = prefs.getInt("jamat_offset_fajr", 25)
                val dOffset = prefs.getInt("jamat_offset_dhuhr", 15)
                val aOffset = prefs.getInt("jamat_offset_asr", 15)
                val mOffset = prefs.getInt("jamat_offset_maghrib", 7)
                val iOffset = prefs.getInt("jamat_offset_isha", 20)
                
                withContext(Dispatchers.Main) {
                    isGuestEnabled = isGuest
                    isPrayerAlarmEnabled = pAlarmEnabled
                    isFajrAlarmEnabled = fAlarm
                    isDhuhrAlarmEnabled = dAlarm
                    isAsrAlarmEnabled = aAlarm
                    isMaghribAlarmEnabled = mAlarm
                    isIshaAlarmEnabled = iAlarm
                    customAdhanUri = adhanUri
                    
                    quranScript = qScript

                    isSecurityAntiSpyEnabled = spyEnabled
                    isSecurityStrictEnvBlockEnabled = strictEnv
                    isMultiLingualGreetingEnabled = multiGreeting
                    
                    fajrJamatOffset = fOffset
                    dhuhrJamatOffset = dOffset
                    asrJamatOffset = aOffset
                    maghribJamatOffset = mOffset
                    ishaJamatOffset = iOffset
                }
                
                com.example.util.PrayerAlarmScheduler.scheduleAlarms(application)
                runSecurityScan()
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to load is_guest_mode or alarm config asynchronously", e)
            }
        }

        scanDownloadedSurahs()
        loadAllSurahProgresses()
        loadHadiths()
        loadDuas()
        loadAnnouncements()
        listenToAppConfig()
        listenToAudioOverrides()
        
        viewModelScope.launch {
            // Ensure streak shields are migrated to new 2-buffer ad system with default 0 of 2
            try {
                val prefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                val migratedV2 = prefs.getBoolean("streak_buffer_v2_migrated", false)
                val localStats = repository.taqwaDao.getUserStatsDirect()
                if (localStats != null) {
                    if (!migratedV2) {
                        val sanitized = localStats.copy(
                            streakShields = localStats.streakShields.coerceIn(0, 2),
                            streakChancesLeft = localStats.streakChancesLeft.coerceIn(0, 2),
                            maxShields = 2
                        )
                        repository.taqwaDao.insertUserStats(sanitized)
                        prefs.edit().putBoolean("streak_buffer_v2_migrated", true).apply()
                        triggerFirebaseSync(forcePull = false)
                    } else if (localStats.streakShields > 2 || localStats.maxShields != 2) {
                        val sanitized = localStats.copy(
                            streakShields = localStats.streakShields.coerceIn(0, 2),
                            streakChancesLeft = localStats.streakShields.coerceIn(0, 2),
                            maxShields = 2
                        )
                        repository.taqwaDao.insertUserStats(sanitized)
                        triggerFirebaseSync(forcePull = false)
                    }
                }
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Error migrating streak buffer state", e)
            }

            repository.checkAndInitializeTasks()
            repository.checkDailyReset()
            fetchLiveRates()
            while (true) {
                try {
                    repository.checkDailyReset()
                    repository.checkAndLogMissedPrayers()
                } catch (e: Throwable) {
                    Log.e("TaqwaViewModel", "Failed to run background tasks gracefully", e)
                }
                kotlinx.coroutines.delay(60000)
            }
        }
        startNetworkMonitoring()

        try {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val user = auth.currentUser
                val wasLoggedOut = (currentUser == null && user != null)
                val wasLoggedIn = (currentUser != null && user == null)
                currentUser = user
                if (user != null) {
                    Log.d("TaqwaViewModel", "AuthStateListener: Current user is ${user.email}. Triggering automatic sync...")
                    listenToAppConfig() // Ensure config and admin listeners are refreshed with the newly authenticated state!
                    triggerFirebaseSync(forcePull = wasLoggedOut)
                    fetchTotalUsersCount()
                    fetchDuasFromFirestore()
                    fetchHadithsFromFirestore()
                    fetchAnnouncementsFromFirestore()
                    fetchUserBugReportsFromFirestore()
                    listenToLeaderboard()
                    listenToCurrentUserDoc(user)
                    if (isAdmin) {
                        fetchBugReportsFromFirestore()
                    }
                } else if (wasLoggedIn) {
                    Log.d("TaqwaViewModel", "AuthStateListener: User signed out. Clearing local cached data and preferences...")
                    currentUserDocListener?.remove()
                    currentUserDocListener = null
                    clearLocalDataAndPreferences()
                }
            }
        } catch (e: Throwable) {
            Log.e("TaqwaViewModel", "Failed to register AuthStateListener", e)
        }

        isInitialized = true
        if (currentUser != null) {
            triggerFirebaseSync()
            fetchTotalUsersCount()
            fetchDuasFromFirestore()
            fetchHadithsFromFirestore()
            fetchAnnouncementsFromFirestore()
            fetchUserBugReportsFromFirestore()
            listenToLeaderboard()
            listenToCurrentUserDoc(currentUser!!)
            if (isAdmin) {
                fetchBugReportsFromFirestore()
            }
        }
        startAutoTracking()
    }

    fun enableGuestMode() {
        try {
            val prefs = getSecurePrefs()
            prefs.edit().putBoolean("is_guest_mode", true).apply()
            isGuestEnabled = true
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Failed to enable Guest Mode: ${e.message}")
        }
    }

    fun disableGuestMode() {
        try {
            val prefs = getSecurePrefs()
            prefs.edit().remove("is_guest_mode").apply()
            isGuestEnabled = false
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Failed to disable Guest Mode: ${e.message}")
        }
    }

    fun clearLocalDataAndPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.taqwaDao.clearTasks()
                repository.taqwaDao.clearBookmarks()
                repository.taqwaDao.clearAllTimeTasks()
                repository.taqwaDao.clearUserStats()
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to clear repository tables during logout/cleanup", e)
            }

            try {
                val prefs = getSecurePrefs()
                prefs.edit().clear().apply()
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to clear TaqwaPrefs", e)
            }

            try {
                val syncPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                syncPrefs.edit().clear().putBoolean("streak_buffer_v2_migrated", true).apply()
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Failed to clear taqwahub_sync", e)
            }

            kotlinx.coroutines.withContext(Dispatchers.Main) {
                currentUserDocListener?.remove()
                currentUserDocListener = null
                isGuestEnabled = false
                chatMessages = emptyList()
                queryCount = 0
                isChatLocked = false
                aiLockEndTime = 0L
                resetTimeRemaining = ""
            }
        }
    }

    private fun startNetworkMonitoring() {
        val context = getApplication<Application>()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        
        if (connectivityManager == null) {
            _networkStatus.value = TaqwaNetworkStatusInfo(TaqwaNetworkType.NONE, TaqwaNetworkCondition.BAD, 1)
            return
        }

        fun updateNetworkState() {
            try {
                val isAirplaneMode = Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    0
                ) != 0

                if (isAirplaneMode) {
                    _networkStatus.value = TaqwaNetworkStatusInfo(
                        type = TaqwaNetworkType.AIRPLANE,
                        condition = TaqwaNetworkCondition.BAD,
                        signalLevel = 0
                    )
                    return
                }

                val activeNetwork = connectivityManager.activeNetwork
                if (activeNetwork == null) {
                    _networkStatus.value = TaqwaNetworkStatusInfo(
                        type = TaqwaNetworkType.NONE,
                        condition = TaqwaNetworkCondition.BAD,
                        signalLevel = 0
                    )
                    return
                }

                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (capabilities == null) {
                    _networkStatus.value = TaqwaNetworkStatusInfo(
                        type = TaqwaNetworkType.NONE,
                        condition = TaqwaNetworkCondition.BAD,
                        signalLevel = 0
                    )
                    return
                }

                val type = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> TaqwaNetworkType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> TaqwaNetworkType.CELLULAR
                    else -> TaqwaNetworkType.NONE
                }

                if (type == TaqwaNetworkType.NONE) {
                    _networkStatus.value = TaqwaNetworkStatusInfo(
                        type = TaqwaNetworkType.NONE,
                        condition = TaqwaNetworkCondition.BAD,
                        signalLevel = 0
                    )
                    return
                }

                var calculatedLevel = 2
                var cond = TaqwaNetworkCondition.MEDIUM
                val speedKbps = capabilities.linkDownstreamBandwidthKbps

                if (speedKbps > 40000) {
                    cond = TaqwaNetworkCondition.EXCELLENT
                    calculatedLevel = 4
                } else if (speedKbps > 15000) {
                    cond = TaqwaNetworkCondition.GOOD
                    calculatedLevel = 3
                } else if (speedKbps > 2000) {
                    cond = TaqwaNetworkCondition.MEDIUM
                    calculatedLevel = 2
                } else {
                    cond = TaqwaNetworkCondition.BAD
                    calculatedLevel = 1
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val sig = capabilities.signalStrength
                    if (sig != -2147483648) {
                        if (type == TaqwaNetworkType.WIFI) {
                            when {
                                sig >= -55 -> { cond = TaqwaNetworkCondition.EXCELLENT; calculatedLevel = 4 }
                                sig >= -70 -> { cond = TaqwaNetworkCondition.GOOD; calculatedLevel = 3 }
                                sig >= -85 -> { cond = TaqwaNetworkCondition.MEDIUM; calculatedLevel = 2 }
                                else -> { cond = TaqwaNetworkCondition.BAD; calculatedLevel = 1 }
                            }
                        } else {
                            when {
                                sig >= -80 -> { cond = TaqwaNetworkCondition.EXCELLENT; calculatedLevel = 4 }
                                sig >= -95 -> { cond = TaqwaNetworkCondition.GOOD; calculatedLevel = 3 }
                                sig >= -110 -> { cond = TaqwaNetworkCondition.MEDIUM; calculatedLevel = 2 }
                                else -> { cond = TaqwaNetworkCondition.BAD; calculatedLevel = 1 }
                            }
                        }
                    }
                }

                _networkStatus.value = TaqwaNetworkStatusInfo(
                    type = type,
                    condition = cond,
                    signalLevel = calculatedLevel
                )
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Error checking network state", e)
            }
        }

        updateNetworkState()

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateNetworkState()
                    triggerFirebaseSync()
                    fetchPrayerTimes()
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    updateNetworkState()
                }

                override fun onLost(network: Network) {
                    updateNetworkState()
                }
            })
        } catch (e: Throwable) {
            Log.e("TaqwaViewModel", "Error registering network callback", e)
        }

        viewModelScope.launch {
            while (true) {
                delay(3000)
                updateNetworkState()
            }
        }
    }

    // Navigation Active View
    var currentView by mutableStateOf("dashboard")
    
    private val _navigationStack = mutableListOf<String>()
    
    fun navigateToView(view: String) {
        if (currentView != view) {
            _navigationStack.add(currentView)
            currentView = view
        }
        if (view == "leaderboard") {
            hasLeaderboardUpdate = false
        }
        if (view == "user_complaints") {
            hasUnreadSupportReply = false
            saveLastSeenComplaintsTime()
        }
    }
    
    fun navigateBack(): Boolean {
        if (_navigationStack.isNotEmpty()) {
            currentView = _navigationStack.removeAt(_navigationStack.size - 1)
            if (currentView == "leaderboard") {
                hasLeaderboardUpdate = false
            }
            if (currentView == "user_complaints") {
                hasUnreadSupportReply = false
                saveLastSeenComplaintsTime()
            }
            return true
        }
        return false
    }

    val showAdSimulation = MutableStateFlow(false)
    var adSimulationCallback: (() -> Unit)? = null

    // Database flow streams
    private val _smartTaskCompletedFlow = MutableSharedFlow<TaskEntity>(replay = 0, extraBufferCapacity = 1)
    val smartTaskCompletedFlow = _smartTaskCompletedFlow.asSharedFlow()

    val tasks: StateFlow<List<TaskEntity>> = repository.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimeTasks: StateFlow<List<AllTimeTaskEntity>> = repository.allTimeTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<UserStatsEntity> = repository.userStatsFlow
        .map { entity ->
            val s = entity ?: UserStatsEntity()
            s.copy(
                streakShields = s.streakShields.coerceIn(0, 2),
                streakChancesLeft = s.streakShields.coerceIn(0, 2),
                maxShields = 2
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatsEntity())

    // Geolocation details for Qibla & Prayer Times
    // Moved up to avoid NPE during initialization

    fun updateCoordinates(latitude: Double, longitude: Double) {
        userLatitude = latitude
        userLongitude = longitude
        fetchPrayerTimes()
    }

    fun fetchPrayerTimes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.fetchPrayerTimes(userLatitude, userLongitude)
                if (result != null) {
                    val timings = result.first
                    val timezone = result.second
                    _prayerTimes.value = timings
                    com.example.widget.WidgetHelper.savePrayerTimes(getApplication(), timings, timezone)
                    com.example.widget.PrayerWidget().updateAll(getApplication())
                } else {
                    val cachedTimings = com.example.widget.WidgetHelper.getPrayerTimesTimings(getApplication())
                    val cachedTz = com.example.widget.WidgetHelper.getPrayerTimezone(getApplication())
                    if (cachedTimings != null) {
                        _prayerTimes.value = cachedTimings
                        repository.cachedPrayerTimes = cachedTimings
                        repository.cachedTimezone = cachedTz
                    } else {
                        _prayerTimes.value = com.example.data.api.AladhanTimings("", "", "", "", "", "")
                    }
                }
            } catch (e: Exception) {
                val cachedTimings = com.example.widget.WidgetHelper.getPrayerTimesTimings(getApplication())
                val cachedTz = com.example.widget.WidgetHelper.getPrayerTimezone(getApplication())
                if (cachedTimings != null) {
                    _prayerTimes.value = cachedTimings
                    repository.cachedPrayerTimes = cachedTimings
                    repository.cachedTimezone = cachedTz
                } else {
                    _prayerTimes.value = com.example.data.api.AladhanTimings("", "", "", "", "", "")
                }
            }
        }
    }

    fun updatePrayerAlarmSetting(enabled: Boolean) {
        isPrayerAlarmEnabled = enabled
        val p = getSecurePrefs()
        p.edit().putBoolean("prayer_alarm_enabled", enabled).apply()
        com.example.util.PrayerAlarmScheduler.scheduleAlarms(getApplication())
    }

    fun updateIndividualAlarmSetting(prayer: String, enabled: Boolean) {
        val p = getSecurePrefs()
        val key = "alarm_${prayer.lowercase()}"
        p.edit().putBoolean(key, enabled).apply()
        when (prayer) {
            "Fajr" -> isFajrAlarmEnabled = enabled
            "Dhuhr" -> isDhuhrAlarmEnabled = enabled
            "Asr" -> isAsrAlarmEnabled = enabled
            "Maghrib" -> isMaghribAlarmEnabled = enabled
            "Isha" -> isIshaAlarmEnabled = enabled
        }
        com.example.util.PrayerAlarmScheduler.scheduleAlarms(getApplication())
    }

    fun updateJamatOffset(prayer: String, offset: Int) {
        val p = getSecurePrefs()
        p.edit().putInt("jamat_offset_${prayer.lowercase()}", offset).apply()
        when (prayer) {
            "Fajr" -> fajrJamatOffset = offset
            "Dhuhr" -> dhuhrJamatOffset = offset
            "Asr" -> asrJamatOffset = offset
            "Maghrib" -> maghribJamatOffset = offset
            "Isha" -> ishaJamatOffset = offset
        }
    }

    fun updateQuranScript(script: String) {
        if (quranScript != script) {
            quranScript = script
            val p = getSecurePrefs()
            p.edit().putString("quran_script", script).apply()
            
            // Delete cached verse files to force redownloads with the new script text
            val dir = getDownloadsDir()
            if (dir.exists()) {
                dir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("verses_") || file.name.startsWith("translations_")) {
                        file.delete()
                    }
                }
            }

            // If user is currently viewing a chapter, reload it
            val currentlyReading = selectedSurah
            if (currentlyReading != null) {
                selectChapter(currentlyReading.id)
            }
        }
    }

    fun runSecurityScan() {
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val sig = com.example.util.SecurityManager.checkAppSignature(app)
            val pkg = com.example.util.SecurityManager.checkPackageName(app)
            val root = com.example.util.SecurityManager.checkRootAccess()
            val emu = com.example.util.SecurityManager.checkEmulator()
            withContext(Dispatchers.Main) {
                signatureCheckResult = sig
                packageCheckResult = pkg
                rootCheckResult = root
                emulatorCheckResult = emu
            }
        }
    }

    fun updateSecurityAntiSpy(enabled: Boolean) {
        isSecurityAntiSpyEnabled = enabled
        val p = getSecurePrefs()
        p.edit().putBoolean("security_anti_spy", enabled).apply()
    }

    fun updateSecurityStrictEnvBlock(enabled: Boolean) {
        isSecurityStrictEnvBlockEnabled = enabled
        val p = getSecurePrefs()
        p.edit().putBoolean("security_strict_env_block", enabled).apply()
    }

    fun updateMultiLingualGreetingEnabled(enabled: Boolean) {
        isMultiLingualGreetingEnabled = enabled
        val p = getSecurePrefs()
        p.edit().putBoolean("multi_lingual_greeting", enabled).apply()
    }

    // Toggle Task complete
    fun toggleMainTask(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            if (isSyncingData) {
                val tasks = repository.taqwaDao.getAllTasksDirect()
                val t = tasks.find { it.id == taskId }
                val pts = t?.points ?: 10
                if (isCompleted) {
                    pendingXpBuffer.addAndGet(pts)
                    pendingWeeklyXpBuffer.addAndGet(pts)
                    pendingCompletedTasksBuffer.add(taskId)
                } else {
                    pendingXpBuffer.addAndGet(-pts)
                    pendingWeeklyXpBuffer.addAndGet(-pts)
                    pendingCompletedTasksBuffer.remove(taskId)
                }
            }
            repository.toggleTaskCompletion(taskId, isCompleted)
            repository.checkDailyReset() // check resets periodically
            repository.checkAndLogMissedPrayers() // evaluate dynamic misses
            markLocalUpdateAndSync()
        }
    }

    fun getTaskTimingStatus(title: String, isCompleted: Boolean): TaskTimingStatus {
        val isPrayer = title in listOf("Offer Fajr Namaz", "Offer Dhuhr Namaz", "Offer Asr Namaz", "Offer Maghrib Namaz", "Offer Isha Namaz", "Offer Jummah Prayer")
        if (!isPrayer) {
            return TaskTimingStatus(isLockedAdvance = false, isMissed = false)
        }

        val timings: AladhanTimings? = prayerTimes.value
        val f = timings?.Fajr?.ifEmpty { "04:20" } ?: "04:20"
        val s = timings?.Sunrise?.ifEmpty { "05:45" } ?: "05:45"
        val d = timings?.Dhuhr?.ifEmpty { "12:30" } ?: "12:30"
        val a = timings?.Asr?.ifEmpty { "15:45" } ?: "15:45"
        val m = timings?.Maghrib?.ifEmpty { "18:45" } ?: "18:45"
        val i = timings?.Isha?.ifEmpty { "20:15" } ?: "20:15"

        val startStr = when (title) {
            "Offer Fajr Namaz" -> f
            "Offer Dhuhr Namaz", "Offer Jummah Prayer" -> d
            "Offer Asr Namaz" -> a
            "Offer Maghrib Namaz" -> m
            "Offer Isha Namaz" -> i
            else -> ""
        }
        val endStr = when (title) {
            "Offer Fajr Namaz" -> s
            "Offer Dhuhr Namaz", "Offer Jummah Prayer" -> a
            "Offer Asr Namaz" -> m
            "Offer Maghrib Namaz" -> i
            "Offer Isha Namaz" -> f
            else -> ""
        }

        val rangeList = repository.getPrayerRanges(prayerTimes.value)
        val range = rangeList.find { it.taskTitle == title || (title == "Offer Jummah Prayer" && it.taskTitle == "Offer Dhuhr Namaz") } 
            ?: return TaskTimingStatus(isLockedAdvance = false, isMissed = false)

        val now = java.util.Date()
        val isLockedAdvance = now < range.start
        val isMissed = now > range.end && !isCompleted

        val (startFormatted, endFormatted) = try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
            val sdf12 = SimpleDateFormat("h:mm a", Locale.US)
            val sObj = sdf24.parse(startStr.replace(Regex("\\s\\(.*?\\)"), "").trim())
            val eObj = sdf24.parse(endStr.replace(Regex("\\s\\(.*?\\)"), "").trim())
            Pair(
                if (sObj != null) sdf12.format(sObj) else startStr,
                if (eObj != null) sdf12.format(eObj) else endStr
            )
        } catch (e: Exception) {
            Pair(startStr, endStr)
        }

        return TaskTimingStatus(
            isLockedAdvance = isLockedAdvance,
            isMissed = isMissed,
            startStr = startFormatted,
            endStr = if (title == "Offer Isha Namaz") "$endFormatted (Next Fajr)" else endFormatted
        )
    }

    // Add custom task
    fun addCustomTask(title: String, category: String) {
        viewModelScope.launch {
            repository.addCustomTask(title, category)
            markLocalUpdateAndSync()
        }
    }

    // Add custom task from Admin panel with full control
    fun addAdminTask(
        title: String,
        category: String,
        description: String,
        points: Int,
        tag: String,
        timerSeconds: Int,
        actionRoute: String = ""
    ) {
        viewModelScope.launch {
            repository.addAdminTask(title, category, description, points, tag, timerSeconds, actionRoute)
            markLocalUpdateAndSync()
        }
    }

    fun updateAdminTask(
        id: String,
        title: String,
        category: String,
        description: String,
        points: Int,
        tag: String,
        timerSeconds: Int,
        actionRoute: String
    ) {
        viewModelScope.launch {
            repository.updateAdminTask(id, title, category, description, points, tag, timerSeconds, actionRoute)
            markLocalUpdateAndSync()
        }
    }

    // Delete task
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            markLocalUpdateAndSync()
        }
    }

    // Reset default tasks
    fun resetDefaultTasks() {
        viewModelScope.launch {
            repository.resetDefaultTasks()
            markLocalUpdateAndSync()
        }
    }

    fun incrementDuaRead() {
        viewModelScope.launch {
            repository.updateAutoTaskProgress("DUA", 1)
            markLocalUpdateAndSync()
        }
    }

    fun incrementHadithRead() {
        viewModelScope.launch {
            repository.updateAutoTaskProgress("HADITH", 1)
            markLocalUpdateAndSync()
        }
    }

    fun incrementNameRead() {
        viewModelScope.launch {
            repository.updateAutoTaskProgress("99_NAMES", 1)
            markLocalUpdateAndSync()
        }
    }

    // Auto-progress background tracking
    private fun startAutoTracking() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val view = currentView.trim().lowercase()
                val allTasks = repository.getAllTasksDirect()
                var updatedAny = false
                val todayStr = repository.getPakistanDateString()
                val existingLogs = repository.taqwaDao.getAllTimeTasksDirect()
                
                // Smart Background Activities Evaluator
                allTasks.filter { it.isAuto && !it.completed }.forEach { task ->
                    val routeLower = task.actionRoute.trim().lowercase()
                    
                    var progressIncrement = 0
                    
                    // High-precision actions (SURAH, TASBEEH, DUA, HADITH, 99_NAMES) are tracked via real user events
                    val isHighPrecisionAuto = task.autoType in listOf("SURAH", "TASBEEH", "DUA", "HADITH", "99_NAMES")
                    
                    // General / Compass / Prayer activity route fallback tracker (timer-based)
                    if (!isHighPrecisionAuto && routeLower.isNotEmpty() && routeLower == view) {
                        progressIncrement = 1
                    }
                    
                    if (progressIncrement > 0) {
                        val target = if (task.autoTarget > 0) task.autoTarget else 15
                        val newProgress = task.autoProgress + progressIncrement
                        
                        if (newProgress >= target) {
                            val completedTask = task.copy(autoProgress = target, completed = true)
                            repository.taqwaDao.insertTask(completedTask)
                            
                            val allTimeId = "${task.id}_${todayStr}"
                            val alreadyLogged = existingLogs.any { it.id == allTimeId && it.completedAt != "MISSED" }

                            if (!alreadyLogged) {
                                val log = com.example.data.room.AllTimeTaskEntity(
                                    id = allTimeId,
                                    taskId = task.id,
                                    title = task.title,
                                    category = task.category,
                                    date = todayStr,
                                    completedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                                    }.format(java.util.Date())
                                )
                                repository.taqwaDao.insertAllTimeTask(log)
                                
                                val currentStats = repository.taqwaDao.getUserStatsDirect() ?: com.example.data.room.UserStatsEntity()
                                repository.taqwaDao.insertUserStats(
                                    currentStats.copy(
                                        totalTasksCompleted = currentStats.totalTasksCompleted + 1,
                                        totalXp = currentStats.totalXp + task.points,
                                        weeklyXp = currentStats.weeklyXp + task.points
                                    )
                                )
                            }
                            
                            updatedAny = true
                            viewModelScope.launch(Dispatchers.Main) {
                                _smartTaskCompletedFlow.emit(completedTask)
                            }
                        } else {
                            val updatedTask = task.copy(autoProgress = newProgress, isAuto = true, autoTarget = target)
                            repository.taqwaDao.insertTask(updatedTask)
                        }
                    }
                }
                
                if (updatedAny) {
                    repository.recalculateAndSaveStreak()
                    markLocalUpdateAndSync()
                }
            }
        }
    }
    fun toggleVerseBookmark(surahNumber: Int, surahName: String, verseNumber: Int, verseKey: String, isFlowMode: Boolean = false) {
        viewModelScope.launch {
            repository.toggleBookmark(surahNumber, surahName, verseNumber, verseKey, isFlowMode)
            markLocalUpdateAndSync()
        }
    }

    data class SurahProgress(
        val surahId: Int,
        val turnsCount: Int,
        val visitedVerses: Set<Int>,
        val accumulatedTimeSeconds: Long,
        val isCompleted: Boolean
    )

    var surahProgressMap by mutableStateOf<Map<Int, SurahProgress>>(emptyMap())
        private set

    private fun getProgressPrefs(): android.content.SharedPreferences {
        return getApplication<android.app.Application>().getSharedPreferences("taqwa_surah_progress", android.content.Context.MODE_PRIVATE)
    }

    fun loadAllSurahProgresses() {
        viewModelScope.launch(Dispatchers.IO) {
            val map = mutableMapOf<Int, SurahProgress>()
            val prefs = getProgressPrefs()
            for (id in 1..114) {
                val turns = prefs.getInt("turns_$id", 0)
                val time = prefs.getLong("time_$id", 0L)
                val visitedStr = prefs.getString("visited_$id", "") ?: ""
                val visited = visitedStr.split(",")
                    .filter { it.isNotEmpty() }
                    .mapNotNull { it.toIntOrNull() }
                    .toSet()
                val completed = prefs.getBoolean("completed_$id", false)
                map[id] = SurahProgress(id, turns, visited, time, completed)
            }
            withContext(Dispatchers.Main) {
                surahProgressMap = map
            }
        }
    }

    fun incrementSurahTurnCount(surahId: Int) {
        val prefs = getProgressPrefs()
        val current = prefs.getInt("turns_$surahId", 0)
        prefs.edit().putInt("turns_$surahId", current + 1).apply()
        
        val visitedStr = prefs.getString("visited_$surahId", "") ?: ""
        val visitedSet = visitedStr.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()
            
        val updatedProgress = SurahProgress(
            surahId = surahId,
            turnsCount = current + 1,
            visitedVerses = visitedSet,
            accumulatedTimeSeconds = prefs.getLong("time_$surahId", 0L),
            isCompleted = prefs.getBoolean("completed_$surahId", false)
        )
        
        viewModelScope.launch(Dispatchers.Main) {
            val currentMap = surahProgressMap.toMutableMap()
            currentMap[surahId] = updatedProgress
            surahProgressMap = currentMap
        }
    }

    var activeSurahReadingSeconds by mutableStateOf(0L)
        private set

    var activeSurahVerifiedVerses by mutableStateOf<Set<Int>>(emptySet())
        private set

    fun getTodaySurahReadingSeconds(surahId: Int): Long {
        val prefs = getProgressPrefs()
        val todayStr = repository.getPakistanDateString()
        return prefs.getLong("daily_time_${todayStr}_$surahId", 0L)
    }

    fun getTodaySurahVisitedVerses(surahId: Int): Set<Int> {
        val prefs = getProgressPrefs()
        val todayStr = repository.getPakistanDateString()
        val dailyVisitedStr = prefs.getString("daily_visited_${todayStr}_$surahId", "") ?: ""
        return dailyVisitedStr.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    fun loadTodaySurahProgress(surahId: Int) {
        activeSurahReadingSeconds = getTodaySurahReadingSeconds(surahId)
        activeSurahVerifiedVerses = getTodaySurahVisitedVerses(surahId)
    }

    fun getSurahReadingRequirement(surahId: Int, totalVerses: Int): TaqwaRepository.QuranReadingRequirement {
        return repository.getSurahReadingRequirement(surahId, totalVerses)
    }

    fun accumulateSurahTime(surahId: Int, seconds: Long, totalVerses: Int) {
        val prefs = getProgressPrefs()
        val todayStr = repository.getPakistanDateString()
        
        // 1. Lifetime Surah stats
        val currentLifetime = prefs.getLong("time_$surahId", 0L)
        val newLifetime = currentLifetime + seconds
        prefs.edit().putLong("time_$surahId", newLifetime).apply()
        
        // 2. Daily task specific reading time (isolated to today)
        val currentDaily = prefs.getLong("daily_time_${todayStr}_$surahId", 0L)
        val newDaily = currentDaily + seconds
        prefs.edit().putLong("daily_time_${todayStr}_$surahId", newDaily).apply()
        activeSurahReadingSeconds = newDaily
        
        val visitedStr = prefs.getString("visited_$surahId", "") ?: ""
        val visitedSet = visitedStr.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()
            
        val dailyVisitedStr = prefs.getString("daily_visited_${todayStr}_$surahId", "") ?: ""
        val dailyVisitedSet = dailyVisitedStr.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        activeSurahVerifiedVerses = dailyVisitedSet
            
        checkAndTriggerAutoCompletion(surahId, totalVerses, visitedSet)
        
        // Update and verify active daily Surah auto-tasks progress atomically with today's reading
        viewModelScope.launch {
            val completedTasks = repository.verifyAndCompleteQuranReading(
                surahId = surahId,
                versesVisitedCount = dailyVisitedSet.size,
                totalVerses = totalVerses,
                activeReadingSeconds = newDaily,
                hasReachedEnd = dailyVisitedSet.contains(totalVerses)
            )
            if (completedTasks.isNotEmpty()) {
                completedTasks.forEach { completedTask ->
                    _smartTaskCompletedFlow.emit(completedTask)
                }
                markLocalUpdateAndSync()
            }
        }
    }

    fun recordAudioListeningTime(surahId: Int, seconds: Long) {
        if (seconds <= 0) return
        val prefs = getProgressPrefs()
        val todayStr = repository.getPakistanDateString()
        
        val currentLifetime = prefs.getLong("audio_time_$surahId", 0L)
        val newLifetime = currentLifetime + seconds
        prefs.edit().putLong("audio_time_$surahId", newLifetime).apply()
        
        val currentDaily = prefs.getLong("daily_audio_${todayStr}_$surahId", 0L)
        val newDaily = currentDaily + seconds
        prefs.edit().putLong("daily_audio_${todayStr}_$surahId", newDaily).apply()

        viewModelScope.launch {
            val completedTasks = repository.verifyAndCompleteQuranAudio(
                surahId = surahId,
                activeAudioListenSeconds = newDaily
            )
            if (completedTasks.isNotEmpty()) {
                completedTasks.forEach { completedTask ->
                    _smartTaskCompletedFlow.emit(completedTask)
                }
                markLocalUpdateAndSync()
            }
        }
    }

    fun addVisitedVerses(surahId: Int, verseNumbers: Set<Int>, totalVerses: Int) {
        if (verseNumbers.isEmpty() || totalVerses <= 0) return
        val prefs = getProgressPrefs()
        val todayStr = repository.getPakistanDateString()
        
        // 1. Lifetime visited
        val visitedStr = prefs.getString("visited_$surahId", "") ?: ""
        val visitedSet = visitedStr.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toMutableSet()
        val newlyAddedLifetime = visitedSet.addAll(verseNumbers)
        if (newlyAddedLifetime) {
            prefs.edit().putString("visited_$surahId", visitedSet.joinToString(",")).apply()
        }
        
        // 2. Daily visited (today)
        val dailyVisitedStr = prefs.getString("daily_visited_${todayStr}_$surahId", "") ?: ""
        val dailyVisitedSet = dailyVisitedStr.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toMutableSet()
        val newlyAddedDaily = dailyVisitedSet.addAll(verseNumbers)
        if (newlyAddedDaily) {
            prefs.edit().putString("daily_visited_${todayStr}_$surahId", dailyVisitedSet.joinToString(",")).apply()
        }
        activeSurahVerifiedVerses = dailyVisitedSet
        
        checkAndTriggerAutoCompletion(surahId, totalVerses, visitedSet)
        
        val dailyAccumulatedTime = prefs.getLong("daily_time_${todayStr}_$surahId", 0L)
        viewModelScope.launch {
            val completedTasks = repository.verifyAndCompleteQuranReading(
                surahId = surahId,
                versesVisitedCount = dailyVisitedSet.size,
                totalVerses = totalVerses,
                activeReadingSeconds = dailyAccumulatedTime,
                hasReachedEnd = dailyVisitedSet.contains(totalVerses)
            )
            if (completedTasks.isNotEmpty()) {
                completedTasks.forEach { completedTask ->
                    _smartTaskCompletedFlow.emit(completedTask)
                }
                markLocalUpdateAndSync()
            }
        }
    }

    private fun checkAndTriggerAutoCompletion(surahId: Int, totalVerses: Int, visitedSet: Set<Int>) {
        val prefs = getProgressPrefs()
        val isCompletedAlready = prefs.getBoolean("completed_$surahId", false)
        if (isCompletedAlready) return
        
        val accumulatedTime = prefs.getLong("time_$surahId", 0L)
        val req = repository.getSurahReadingRequirement(surahId, totalVerses)
        
        val percentageVisited = if (totalVerses > 0) visitedSet.size.toDouble() / totalVerses else 0.0
        val visitedLastVerse = visitedSet.contains(totalVerses)
        
        if (accumulatedTime >= req.requiredReadingSeconds && visitedSet.size >= req.requiredAyahs && visitedLastVerse) {
            prefs.edit().putBoolean("completed_$surahId", true).apply()
            
            viewModelScope.launch {
                val currentStats = repository.getUserStats()
                val completedSet = currentStats.completedSurahs.split(",")
                    .filter { it.isNotEmpty() }
                    .map { it.trim() }
                    .toMutableSet()
                
                val idStr = surahId.toString()
                if (!completedSet.contains(idStr)) {
                    if (isSyncingData) {
                        pendingXpBuffer.addAndGet(25)
                        pendingWeeklyXpBuffer.addAndGet(25)
                        pendingCompletedSurahsBuffer.add(idStr)
                    }
                    completedSet.add(idStr)
                    val xpChange = 25
                    val newXp = currentStats.totalXp + xpChange
                    val newWeeklyXp = currentStats.weeklyXp + xpChange
                    
                    val updatedStats = currentStats.copy(
                        completedSurahs = completedSet.joinToString(","),
                        totalXp = newXp,
                        weeklyXp = newWeeklyXp,
                        quranProgress = completedSet.size
                    )
                    repository.saveUserStats(updatedStats)
                    markLocalUpdateAndSync()
                }
            }
        }
        
        val updatedProgress = SurahProgress(
            surahId = surahId,
            turnsCount = prefs.getInt("turns_$surahId", 0),
            visitedVerses = visitedSet,
            accumulatedTimeSeconds = accumulatedTime,
            isCompleted = prefs.getBoolean("completed_$surahId", false)
        )
        
        viewModelScope.launch(Dispatchers.Main) {
            val currentMap = surahProgressMap.toMutableMap()
            currentMap[surahId] = updatedProgress
            surahProgressMap = currentMap
        }
    }

    fun toggleSurahCompletion(surahId: Int) {
        viewModelScope.launch {
            val currentStats = repository.getUserStats()
            val completedSet = currentStats.completedSurahs.split(",")
                .filter { it.isNotEmpty() }
                .map { it.trim() }
                .toMutableSet()

            val idStr = surahId.toString()
            val wasCompleted = completedSet.contains(idStr)
            val xpChange = 25
            val newXp: Int
            val newWeeklyXp: Int

            if (wasCompleted) {
                if (isSyncingData) {
                    pendingXpBuffer.addAndGet(-xpChange)
                    pendingWeeklyXpBuffer.addAndGet(-xpChange)
                    pendingCompletedSurahsBuffer.remove(idStr)
                }
                completedSet.remove(idStr)
                newXp = (currentStats.totalXp - xpChange).coerceAtLeast(0)
                newWeeklyXp = (currentStats.weeklyXp - xpChange).coerceAtLeast(0)
            } else {
                if (isSyncingData) {
                    pendingXpBuffer.addAndGet(xpChange)
                    pendingWeeklyXpBuffer.addAndGet(xpChange)
                    pendingCompletedSurahsBuffer.add(idStr)
                }
                completedSet.add(idStr)
                newXp = currentStats.totalXp + xpChange
                newWeeklyXp = currentStats.weeklyXp + xpChange
            }

            val newCompletedStr = completedSet.joinToString(",")
            val updatedStats = currentStats.copy(
                completedSurahs = newCompletedStr,
                totalXp = newXp,
                weeklyXp = newWeeklyXp,
                quranProgress = completedSet.size
            )
            repository.saveUserStats(updatedStats)
            markLocalUpdateAndSync()
        }
    }

    fun incrementPodiumCount(place: Int) {
        viewModelScope.launch {
            if (isSyncingData) {
                val extraXp = when (place) {
                    1 -> 100
                    2 -> 50
                    3 -> 25
                    else -> 0
                }
                pendingXpBuffer.addAndGet(extraXp)
                pendingWeeklyXpBuffer.addAndGet(extraXp)
            }
            val currentStats = repository.getUserStats()
            val updatedStats = when (place) {
                1 -> currentStats.copy(firstPlaceCount = currentStats.firstPlaceCount + 1, totalXp = currentStats.totalXp + 100)
                2 -> currentStats.copy(secondPlaceCount = currentStats.secondPlaceCount + 1, totalXp = currentStats.totalXp + 50)
                3 -> currentStats.copy(thirdPlaceCount = currentStats.thirdPlaceCount + 1, totalXp = currentStats.totalXp + 25)
                else -> currentStats
            }
            repository.saveUserStats(updatedStats)
            markLocalUpdateAndSync()
        }
    }

    // Set stats info
    fun setStats(updatedStats: UserStatsEntity) {
        viewModelScope.launch {
            repository.saveUserStats(updatedStats)
            markLocalUpdateAndSync()
        }
    }

    fun updateProfilePictureBase64(base64: String) {
        viewModelScope.launch {
            val currentStats = repository.getUserStats()
            val updatedStats = currentStats.copy(profilePictureBase64 = base64)
            repository.saveUserStats(updatedStats)
            markLocalUpdateAndSync()
        }
    }

    fun isUserVerified(stats: UserStatsEntity): Boolean {
        if (stats.isVerified) return true
        val email = stats.email.lowercase().trim()
        if (email.isEmpty()) return false
        return adminEmails.any { it.lowercase().trim() == email } || 
               superAdminEmails.any { it.lowercase().trim() == email } || 
               email == "kb1747038@gmail.com"
    }

    fun checkUsernameAvailability(usernameStr: String, currentUid: String?, onResult: (Boolean) -> Unit) {
        val trimmed = usernameStr.trim().lowercase()
        if (trimmed.isEmpty()) {
            onResult(false)
            return
        }
        val regex = Regex("^[a-zA-Z0-9_]{3,20}$")
        if (!regex.matches(trimmed)) {
            onResult(false)
            return
        }
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("userStats.username", trimmed)
            .get()
            .addOnSuccessListener { snapshot ->
                val takenByStats = snapshot != null && snapshot.documents.any { doc -> doc.id != currentUid }
                if (takenByStats) {
                    onResult(false)
                } else {
                    // Check top-level username for backwards compatibility
                    db.collection("users")
                        .whereEqualTo("username", trimmed)
                        .get()
                        .addOnSuccessListener { topSnapshot ->
                            val takenByTop = topSnapshot != null && topSnapshot.documents.any { doc -> doc.id != currentUid }
                            onResult(!takenByTop)
                        }
                        .addOnFailureListener { e ->
                            Log.w("TaqwaViewModel", "Secondary username check warning: ${e.message}")
                            onResult(true)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TaqwaViewModel", "Username check failed: ${e.message}", e)
                // If Firestore query fails (e.g. offline or query error), allow registration rather than blocking the user
                onResult(true)
            }
    }

    // Tasbeeh counter state
    var selectedTasbeehId by mutableStateOf(0)
    var duaSearchQuery by mutableStateOf("")
    var namesSearchQuery by mutableStateOf("")
    var tasbeehGoal by mutableStateOf(33)

    fun incrementTasbeeh() {
        viewModelScope.launch {
            if (isSyncingData) {
                pendingTasbeehBuffer.incrementAndGet()
            }
            val currentStats = repository.getUserStats()
            val newCount = currentStats.tasbeehCount + 1
            repository.saveUserStats(currentStats.copy(tasbeehCount = newCount))
            
            // Standard general tasbeeh progress increment
            repository.updateAutoTaskProgress("TASBEEH", 1, selectedTasbeehId)
            
            // --- SMART DYNAMIC DHIKR TASK MATCHING ---
            val allTasks = repository.getAllTasksDirect()
            val todayStr = repository.getPakistanDateString()
            var updatedAny = false
            
            allTasks.filter { it.isAuto && !it.completed }.forEach { task ->
                // Skip high precision tasks that are updated via updateAutoTaskProgress to prevent duplicate increment
                if (task.autoType in listOf("SURAH", "TASBEEH", "DUA", "HADITH", "99_NAMES")) {
                    return@forEach
                }

                val titleLower = task.title.lowercase()
                val descLower = task.description.lowercase()
                val catLower = task.category.lowercase()
                
                val isGeneralDhikr = catLower.contains("dhikr") || catLower.contains("tasbeeh") ||
                                     titleLower.contains("tasbeeh") || titleLower.contains("dhikr") ||
                                     titleLower.contains("recite")
                                     
                val isSpecificMatch = when (selectedTasbeehId) {
                    1 -> titleLower.contains("subhanallah") || titleLower.contains("subhan allah")
                    2 -> titleLower.contains("alhamdulillah") || titleLower.contains("alhamdu lillah")
                    3 -> titleLower.contains("allahu akbar") || titleLower.contains("allahuakbar")
                    4 -> titleLower.contains("astagh") || titleLower.contains("forgiveness") || titleLower.contains("seek forgiveness")
                    else -> false
                }
                
                if (isSpecificMatch || isGeneralDhikr) {
                    val target = if (task.isAuto && task.autoTarget > 0) {
                        task.autoTarget
                    } else {
                        when {
                            titleLower.contains("100") -> 100
                            titleLower.contains("33") -> 33
                            else -> 33
                        }
                    }
                    
                    val newProgress = task.autoProgress + 1
                    if (newProgress >= target) {
                        val completedTask = task.copy(autoProgress = target, completed = true)
                        repository.taqwaDao.insertTask(completedTask)
                        
                        val allTimeId = "${task.id}_${todayStr}"
                        val log = com.example.data.room.AllTimeTaskEntity(
                            id = allTimeId,
                            taskId = task.id,
                            title = task.title,
                            category = task.category,
                            date = todayStr,
                            completedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }.format(java.util.Date())
                        )
                        repository.taqwaDao.insertAllTimeTask(log)
                        
                        val currentStatsEntity = repository.taqwaDao.getUserStatsDirect() ?: com.example.data.room.UserStatsEntity()
                        repository.taqwaDao.insertUserStats(
                            currentStatsEntity.copy(
                                totalTasksCompleted = currentStatsEntity.totalTasksCompleted + 1,
                                totalXp = currentStatsEntity.totalXp + task.points,
                                weeklyXp = currentStatsEntity.weeklyXp + task.points
                            )
                        )
                        
                        updatedAny = true
                        viewModelScope.launch(Dispatchers.Main) {
                            _smartTaskCompletedFlow.emit(completedTask)
                        }
                    } else {
                        val updatedTask = task.copy(autoProgress = newProgress, isAuto = true, autoTarget = target)
                        repository.taqwaDao.insertTask(updatedTask)
                    }
                }
            }
            
            if (updatedAny) {
                repository.recalculateAndSaveStreak()
            }
            markLocalUpdateAndSyncDebounced()
        }
    }

    fun resetTasbeeh() {
        viewModelScope.launch {
            val currentStats = repository.getUserStats()
            repository.saveUserStats(currentStats.copy(tasbeehCount = 0))
            markLocalUpdateAndSync()
        }
    }

    // Quran Chapter List state
    var quranChapters by mutableStateOf<List<QuranChapter>>(emptyList())
        private set
    var isChaptersLoading by mutableStateOf(false)
        private set
    var chaptersError by mutableStateOf<String?>(null)

    fun loadQuranChapters() {
        viewModelScope.launch {
            isChaptersLoading = true
            chaptersError = null
            try {
                val chapters = repository.fetchChapters()
                if (chapters.isNotEmpty()) {
                    quranChapters = chapters
                } else if (quranChapters.isEmpty()) {
                    chaptersError = "Unable to connect. Please check your internet connection."
                }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error loading chapters: ${e.message}", e)
                if (quranChapters.isEmpty()) {
                    chaptersError = "Unable to connect. Please check your internet connection."
                }
            } finally {
                isChaptersLoading = false
            }
        }
    }

    // Active Chapter screen details
    var selectedSurah by mutableStateOf<Surah?>(null)
    var isContinuousFlowMode by mutableStateOf(false)
    var requestedScrollVerseId by mutableStateOf<Int?>(null)
    var activeVerses by mutableStateOf<List<com.example.data.api.QuranVerse>>(emptyList())
        private set
    var activeTranslations by mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) // "verse_key" -> Pair<English, Urdu>
        private set
    var activeVerseAudioUrls by mutableStateOf<Map<String, String>>(emptyMap()) // "verse_key" -> "audio_url"
        private set
    var activeVerseDurations by mutableStateOf<Map<String, Int>>(emptyMap()) // "verse_key" -> duration_seconds
        private set
    var activeVerseSegments by mutableStateOf<Map<String, List<List<Int>>>>(emptyMap()) // "verse_key" -> segments list
        private set
    var isVersesLoading by mutableStateOf(false)
        private set
    var versesError by mutableStateOf<String?>(null)
        private set
    var hadithError by mutableStateOf<String?>(null)

    var activeTafsirText by mutableStateOf<Map<String, String>>(emptyMap()) // "verse_key" -> Tafsir content
        private set
    var isTafsirLoading by mutableStateOf<Map<String, Boolean>>(emptyMap()) // "verse_key" -> isLoading boolean
        private set
    var selectedTafsirId by mutableStateOf(169) // 169 (Ibn Kathir English) by default

    fun handleTaskRedirection(task: TaskEntity, context: android.content.Context, bookmarks: List<BookmarkEntity>, onNavigate: (String) -> Unit) {
        if (task.actionRoute.isEmpty()) return
        
        val routeLower = task.actionRoute.trim().lowercase()
        
        // 1. Specialized Quran routing based on task title
        if (routeLower == "quran") {
            if (task.title.contains("Al-Mulk", ignoreCase = true)) {
                val mulkBookmark = bookmarks.find { it.surahNumber == 67 }
                if (mulkBookmark != null) {
                    isContinuousFlowMode = mulkBookmark.isFlowMode
                    selectChapter(67)
                    requestedScrollVerseId = mulkBookmark.verseNumber
                } else {
                    isContinuousFlowMode = false
                    selectChapter(67)
                }
            } else if (task.title.contains("Kahf", ignoreCase = true)) {
                selectChapter(18)
            } else if (task.title.contains("Yaseen", ignoreCase = true)) {
                selectChapter(36)
            } else if (task.title.contains("Baqarah", ignoreCase = true)) {
                selectChapter(2)
            }
        }
        
        // 2. Specialized Tasbeeh routing based on task title
        if (routeLower == "tasbeeh" || routeLower == "dhikr") {
            if (task.title.contains("Astaghfar", ignoreCase = true) || task.title.contains("Astaghfirullah", ignoreCase = true) || task.title.contains("forgiveness", ignoreCase = true)) {
                selectedTasbeehId = 4
            } else if (task.title.contains("SubhanAllah", ignoreCase = true) || task.title.contains("Subhan Allah", ignoreCase = true)) {
                selectedTasbeehId = 1
            } else if (task.title.contains("Alhamdulillah", ignoreCase = true) || task.title.contains("Alhamdu lillah", ignoreCase = true)) {
                selectedTasbeehId = 2
            } else if (task.title.contains("Allahu Akbar", ignoreCase = true) || task.title.contains("Allah u Akbar", ignoreCase = true)) {
                selectedTasbeehId = 3
            } else {
                selectedTasbeehId = 0
            }
        }

        // 3. Specialized Dua routing based on task title
        if (routeLower == "dua" || routeLower == "duas") {
            val titleLower = task.title.lowercase()
            duaSearchQuery = when {
                titleLower.contains("morning") -> "morning"
                titleLower.contains("evening") -> "evening"
                titleLower.contains("sleeping") || titleLower.contains("sleep") -> "sleep"
                titleLower.contains("travel") -> "travel"
                titleLower.contains("wake") -> "wake"
                titleLower.contains("forgiveness") -> "forgiveness"
                titleLower.contains("guidance") -> "guidance"
                else -> ""
            }
        }

        // 4. Specialized 99 Names routing based on task title
        if (routeLower == "names" || routeLower == "99_names") {
            val titleLower = task.title.lowercase()
            namesSearchQuery = when {
                titleLower.contains("allah") -> "allah"
                titleLower.contains("prophet") -> "prophet"
                else -> ""
            }
        }
        
        // 5. Perform navigation
        if (routeLower.startsWith("http")) {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(task.actionRoute))
                context.startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Could not open link: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            val target = when (routeLower) {
                "home", "dashboard" -> "dashboard"
                "names", "99_names" -> "names"
                "tasbeeh", "dhikr" -> "tasbeeh"
                "hadith", "sunnah" -> "hadith"
                "dua", "duas" -> "dua"
                "donate", "charity", "sadaqah" -> "donate"
                else -> task.actionRoute
            }
            onNavigate(target)
        }
    }

    private data class SurahSessionCache(
        val verses: List<com.example.data.api.QuranVerse>,
        val translations: Map<String, Pair<String, String>>,
        val audioUrls: Map<String, String>,
        val durations: Map<String, Int>,
        val segments: Map<String, List<List<Int>>>
    )

    private val surahInMemoryCache = java.util.concurrent.ConcurrentHashMap<String, SurahSessionCache>()

    fun prefetchPopularSurahs() {
        viewModelScope.launch(Dispatchers.IO) {
            val popularSurahIds = listOf(1, 36, 55, 67, 112, 113, 114)
            val dir = getDownloadsDir()
            for (sId in popularSurahIds) {
                val cacheKey = "${sId}_$selectedReciterId"
                if (surahInMemoryCache.containsKey(cacheKey)) continue
                
                val versesFile = File(dir, "verses_$sId.json")
                if (versesFile.exists()) continue // Already cached on disk

                try {
                    coroutineScope {
                        val vDef = async(Dispatchers.IO) { repository.fetchVerses(sId, selectedReciterId) }
                        val tDef = async(Dispatchers.IO) { repository.fetchTranslations(sId) }
                        val aDef = async(Dispatchers.IO) { repository.fetchRecitation(sId, selectedReciterId) }

                        val v = vDef.await()
                        val t = tDef.await()
                        val a = aDef.await()

                        if (v.isNotEmpty()) {
                            try {
                                val vJson = localMoshi.adapter<List<com.example.data.api.QuranVerse>>(
                                    Types.newParameterizedType(List::class.java, com.example.data.api.QuranVerse::class.java)
                                ).toJson(v)
                                val tJson = localMoshi.adapter<List<com.example.data.api.TranslationVerse>>(
                                    Types.newParameterizedType(List::class.java, com.example.data.api.TranslationVerse::class.java)
                                ).toJson(t)
                                val aJson = localMoshi.adapter<List<com.example.data.api.AudioFile>>(
                                    Types.newParameterizedType(List::class.java, com.example.data.api.AudioFile::class.java)
                                ).toJson(a)

                                File(dir, "verses_$sId.json").writeText(vJson)
                                File(dir, "translations_$sId.json").writeText(tJson)
                                File(dir, "audio_$sId.json").writeText(aJson)
                            } catch (e: Exception) {
                                Log.e("TaqwaViewModel", "Error auto-saving prefetched surah $sId: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore silent prefetch errors
                }
            }
        }
    }

    fun selectChapter(surahId: Int) {
        val surah = IslamicData.surahs.find { it.id == surahId }
        selectedSurah = surah ?: islamicSurahFallback(surahId)
        
        viewModelScope.launch {
            val cacheKey = "${surahId}_$selectedReciterId"

            // 0. Immediate In-Memory Cache Check (0ms instantaneous rendering)
            surahInMemoryCache[cacheKey]?.let { cached ->
                activeVerses = cached.verses
                activeTranslations = cached.translations
                activeVerseAudioUrls = cached.audioUrls
                activeVerseDurations = cached.durations
                activeVerseSegments = cached.segments
                isVersesLoading = false
                versesError = null

                // Prefetch first 5 verse audio files
                val initialPrefetch = cached.verses.take(5).mapNotNull { cached.audioUrls[it.verse_key] }
                if (initialPrefetch.isNotEmpty()) {
                    audioPlayerHelper.prefetch(initialPrefetch)
                }
                return@launch
            }

            isVersesLoading = true
            versesError = null
            activeVerses = emptyList()
            activeTranslations = emptyMap()
            activeVerseAudioUrls = emptyMap()
            activeVerseDurations = emptyMap()
            activeVerseSegments = emptyMap()
            activeTafsirText = emptyMap()
            isTafsirLoading = emptyMap()

            try {
                var verses = emptyList<com.example.data.api.QuranVerse>()
                var translations = emptyList<com.example.data.api.TranslationVerse>()
                var audioFiles = emptyList<com.example.data.api.AudioFile>()
                var loadedFromDisk = false

                // 1. Try loading from local offline storage first if files exist
                val dir = getDownloadsDir()
                val versesFile = File(dir, "verses_$surahId.json")
                val transFile = File(dir, "translations_$surahId.json")
                val audioFileConf = File(dir, "audio_$surahId.json")
                if (versesFile.exists() && transFile.exists()) {
                    try {
                        val versesJson = versesFile.readText()
                        val transJson = transFile.readText()
                        
                        val loadedVerses = localMoshi.adapter<List<com.example.data.api.QuranVerse>>(
                            Types.newParameterizedType(List::class.java, com.example.data.api.QuranVerse::class.java)
                        ).fromJson(versesJson)
                        
                        val loadedTrans = localMoshi.adapter<List<com.example.data.api.TranslationVerse>>(
                            Types.newParameterizedType(List::class.java, com.example.data.api.TranslationVerse::class.java)
                        ).fromJson(transJson)
                        
                        if (!loadedVerses.isNullOrEmpty()) {
                            verses = loadedVerses
                            loadedFromDisk = true
                        }
                        if (!loadedTrans.isNullOrEmpty()) translations = loadedTrans
                        
                        if (audioFileConf.exists()) {
                            val audioJson = audioFileConf.readText()
                            val loadedAudio = localMoshi.adapter<List<com.example.data.api.AudioFile>>(
                                Types.newParameterizedType(List::class.java, com.example.data.api.AudioFile::class.java)
                            ).fromJson(audioJson)
                            if (loadedAudio != null) audioFiles = loadedAudio
                        }
                        Log.d("TaqwaViewModel", "Successfully loaded Surah $surahId from local disk cache.")
                    } catch (e: Exception) {
                        Log.e("TaqwaViewModel", "Error parsing local surah $surahId: ${e.message}", e)
                    }
                }

                // 2. Concurrent Parallel Network Fetching (if not cached locally)
                if (verses.isEmpty() || translations.isEmpty()) {
                    coroutineScope {
                        val versesDeferred = async(Dispatchers.IO) { repository.fetchVerses(surahId, selectedReciterId) }
                        val transDeferred = async(Dispatchers.IO) { repository.fetchTranslations(surahId) }
                        val audioDeferred = async(Dispatchers.IO) { repository.fetchRecitation(surahId, selectedReciterId) }

                        verses = versesDeferred.await()
                        translations = transDeferred.await()
                        audioFiles = audioDeferred.await()
                    }

                    // Auto-cache to local disk for 100% offline access on all future visits
                    if (verses.isNotEmpty()) {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val vJson = localMoshi.adapter<List<com.example.data.api.QuranVerse>>(
                                    Types.newParameterizedType(List::class.java, com.example.data.api.QuranVerse::class.java)
                                ).toJson(verses)
                                val tJson = localMoshi.adapter<List<com.example.data.api.TranslationVerse>>(
                                    Types.newParameterizedType(List::class.java, com.example.data.api.TranslationVerse::class.java)
                                ).toJson(translations)
                                val aJson = localMoshi.adapter<List<com.example.data.api.AudioFile>>(
                                    Types.newParameterizedType(List::class.java, com.example.data.api.AudioFile::class.java)
                                ).toJson(audioFiles)

                                File(dir, "verses_$surahId.json").writeText(vJson)
                                File(dir, "translations_$surahId.json").writeText(tJson)
                                File(dir, "audio_$surahId.json").writeText(aJson)
                                Log.d("TaqwaViewModel", "Auto-cached Surah $surahId to disk storage.")
                            } catch (e: Exception) {
                                Log.e("TaqwaViewModel", "Failed auto-caching surah $surahId: ${e.message}")
                            }
                        }
                    }
                } else if (audioFiles.isEmpty() || selectedReciterId != 7) {
                    audioFiles = repository.fetchRecitation(surahId, selectedReciterId)
                }

                // Convert translations list to easy-lookup map
                val transMap = mutableMapOf<String, Pair<String, String>>()
                translations.forEach { tv ->
                    val englishRaw = tv.translations.find { it.resource_id in listOf(17, 20, 22) }?.text
                        ?: tv.translations.find { it.resource_id == 131 }?.text
                        ?: tv.translations.firstOrNull()?.text ?: ""
                    val urduRaw = tv.translations.find { it.resource_id in listOf(158, 97, 151, 156) }?.text ?: ""
                    
                    val cleanEnglishRaw = englishRaw.replace(Regex("<sup.*?>.*?</sup>"), "").replace(Regex("<.*?>"), "")
                    val cleanUrduRaw = urduRaw.replace(Regex("<sup.*?>.*?</sup>"), "").replace(Regex("<.*?>"), "")
                    
                    val english = androidx.core.text.HtmlCompat.fromHtml(cleanEnglishRaw, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT).toString().replace(Regex("\\[\\d+\\]"), "").trim()
                    val urdu = androidx.core.text.HtmlCompat.fromHtml(cleanUrduRaw, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT).toString().replace(Regex("\\[\\d+\\]"), "").trim()

                    transMap[tv.verse_key] = Pair(english, urdu)
                }

                val audioMap = mutableMapOf<String, String>()
                val durationMap = mutableMapOf<String, Int>()
                val segmentMap = mutableMapOf<String, List<List<Int>>>()

                verses.forEach { v ->
                    v.audio?.let { va ->
                        va.url?.let { audioMap[v.verse_key] = it }
                        va.segments?.let { segments ->
                            segmentMap[v.verse_key] = segments
                            val lastSegEndMs = segments.lastOrNull()?.getOrNull(3) ?: 0
                            durationMap[v.verse_key] = (lastSegEndMs / 1000).coerceAtLeast(1)
                        }
                    }
                }

                audioFiles.forEach { af ->
                    if (af.url.isNotEmpty()) {
                        audioMap[af.verse_key] = af.url
                    }
                    if (af.duration != null && af.duration > 0) {
                        durationMap[af.verse_key] = af.duration
                    }
                    af.segments?.let { segmentMap[af.verse_key] = it }
                }

                verses.forEach { v ->
                    if (audioMap[v.verse_key].isNullOrEmpty()) {
                        audioMap[v.verse_key] = getVerseAudioUrl(v.verse_key, selectedReciterId)
                    }
                }

                // Save into in-memory session cache
                if (verses.isNotEmpty()) {
                    surahInMemoryCache[cacheKey] = SurahSessionCache(
                        verses = verses,
                        translations = transMap,
                        audioUrls = audioMap,
                        durations = durationMap,
                        segments = segmentMap
                    )
                }

                activeVerses = verses
                activeTranslations = transMap
                activeVerseAudioUrls = audioMap
                activeVerseDurations = durationMap
                activeVerseSegments = segmentMap

                val initialPrefetch = verses.take(5).mapNotNull { audioMap[it.verse_key] }
                if (initialPrefetch.isNotEmpty()) {
                    audioPlayerHelper.prefetch(initialPrefetch)
                }

                if (verses.isEmpty()) {
                    versesError = "Unable to load verses. Connect to the internet to load Surah."
                }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error fetching surah $surahId: ${e.message}", e)
                versesError = "Unable to load verses. Please check your internet connection."
            } finally {
                isVersesLoading = false
            }
        }
    }

    fun loadTafsirForVerse(verseKey: String) {
        if (activeTafsirText.containsKey(verseKey) || isTafsirLoading[verseKey] == true) return

        isTafsirLoading = isTafsirLoading + (verseKey to true)
        viewModelScope.launch {
            try {
                val tafsirData = repository.fetchTafsir(selectedTafsirId, verseKey)
                if (tafsirData != null) {
                    var cleanText = tafsirData.text ?: ""
                    
                    // Strip and format standard HTML blocks to support clean typography
                    cleanText = cleanText
                        .replace(Regex("<sup.*?>.*?</sup>"), "")
                        .replace(Regex("<br\\s*/?>"), "\n")
                        .replace(Regex("<p.*?>"), "")
                        .replace(Regex("</p>"), "\n\n")
                        .replace(Regex("<.*?>"), "") // Strip any other tags
                    
                    // Parse HTML entities
                    val decodedText = androidx.core.text.HtmlCompat.fromHtml(
                        cleanText,
                        androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
                    ).toString().trim()
                    
                    activeTafsirText = activeTafsirText + (verseKey to decodedText)
                } else {
                    activeTafsirText = activeTafsirText + (verseKey to "Tafsir content not available for this verse.")
                }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error loading tafsir for $verseKey: ${e.message}")
                activeTafsirText = activeTafsirText + (verseKey to "Failed to load Tafsir. Please check your connection.")
            } finally {
                isTafsirLoading = isTafsirLoading + (verseKey to false)
            }
        }
    }
    
    fun setTafsirResource(tafsirId: Int) {
        selectedTafsirId = tafsirId
        activeTafsirText = emptyMap() // Clear cached tafsirs to reload
    }

    fun getFullSurahAudioUrl(surahId: Int, reciterId: Int): String {
        val padded = String.format("%03d", surahId)
        val slug = when (reciterId) {
            7 -> "mishaari_raashid_al_3afasy"
            3 -> "abdurrahmaan_as_sudais"
            6 -> "maher_al_muaiqly"
            12 -> "yasser_ad-dussary"
            2 -> "abdul_basit_mujawwad"
            1 -> "abu_bakr_ash-shaatree"
            else -> "mishaari_raashid_al_3afasy"
        }
        return "https://download.quranicaudio.com/quran/$slug/$padded.mp3"
    }

    fun getVerseStartTimes(): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        var cumulativeMs = 0
        val isAlafasy = (selectedReciterId == 7)
        activeVerses.forEach { verse ->
            list.add(Pair(verse.verse_key, cumulativeMs))
            val durSeconds = activeVerseDurations[verse.verse_key] ?: 0
            val durMs = if (durSeconds > 0) durSeconds * 1000 else 5000
            val adjustedDurMs = if (isAlafasy) {
                // Alafasy typically has ~1.4s of silent padding/pauses per verse file
                (durMs - 1400).coerceAtLeast(1500)
            } else {
                durMs
            }
            cumulativeMs += adjustedDurMs
        }
        return list
    }

    fun getReciterSlug(reciterId: Int): String {
        return when (reciterId) {
            7 -> "Alafasy_128kbps"
            3 -> "Abdurrahmaan_As-Sudais_192kbps"
            6 -> "MaherAlMuaiqly128kbps"
            12 -> "Yasser_Ad-Dussary_128kbps"
            2 -> "Abdul_Basit_Murattal_192kbps"
            1 -> "Abu_Bakr_Ash-Shaatree_128kbps"
            else -> "Alafasy_128kbps"
        }
    }

    fun getVerseAudioUrl(verseKey: String, reciterId: Int): String {
        val slug = getReciterSlug(reciterId)
        val sNum = verseKey.substringBefore(":").toIntOrNull() ?: 1
        val aNum = verseKey.substringAfter(":").toIntOrNull() ?: 1
        val file = String.format("%03d%03d.mp3", sNum, aNum)
        return "https://everyayah.com/data/$slug/$file"
    }

    fun selectReciter(surahId: Int, reciterId: Int, onComplete: (() -> Unit)? = null) {
        audioPlayerHelper.stop()
        selectedReciterId = reciterId

        // 1. Instant Synchronous Pre-population: Generate direct high-speed CDN audio URLs in 0ms
        val instantAudioMap = activeVerseAudioUrls.toMutableMap()
        activeVerses.forEach { v ->
            instantAudioMap[v.verse_key] = getVerseAudioUrl(v.verse_key, reciterId)
        }
        activeVerseAudioUrls = instantAudioMap

        // 2. Trigger completion immediately so playback starts with 0ms delay
        onComplete?.invoke()

        // 3. Background Enhancement: fetch detailed segment timings without blocking playback or UI
        viewModelScope.launch {
            try {
                val audioFiles = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    repository.fetchRecitation(surahId, reciterId)
                }
                if (audioFiles.isNotEmpty() && selectedReciterId == reciterId) {
                    val updatedAudioMap = activeVerseAudioUrls.toMutableMap()
                    val durationMap = activeVerseDurations.toMutableMap()
                    val segmentMap = activeVerseSegments.toMutableMap()

                    audioFiles.forEach { af ->
                        if (af.url.isNotEmpty()) {
                            updatedAudioMap[af.verse_key] = af.url
                        }
                        if (af.duration != null && af.duration > 0) {
                            durationMap[af.verse_key] = af.duration
                        }
                        af.segments?.let { segmentMap[af.verse_key] = it }
                    }
                    activeVerseAudioUrls = updatedAudioMap
                    activeVerseDurations = durationMap
                    activeVerseSegments = segmentMap
                }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Background recitation details fetch: ${e.message}")
            }
        }
    }

    fun playWordAudio(word: com.example.data.api.QuranWord, verseKey: String, surahId: Int) {
        if (isWordByWordAudioLocked(surahId)) {
            // Cannot play, UI should show Toast
            return
        }
        val override = audioOverrides["word_${word.id}"]
        if (override != null) {
            wordAudioPlayerHelper.playAudio(url = override, playbackToken = override)
            return
        }

        // Expected playbackToken inside UI highlights:
        val token = word.audio_url ?: "${activeVerseAudioUrls[verseKey]}#word_${word.id}"

        // Bypassing verse audio segment cutting because segment boundaries are prone to overlap and echoes.
        // We instead build a clean, individual pre-recorded word-by-word path: wbw/{surah}_{ayah}_{word}.mp3 (zero-padded).
        val parts = verseKey.split(":")
        val sNum = parts.getOrNull(0)?.toIntOrNull() ?: surahId
        val aNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val wNum = word.position

        val directUrl = String.format("wbw/%03d_%03d_%03d.mp3", sNum, aNum, wNum)

        android.util.Log.d("TaqwaViewModel", "Playing clean individual pre-recorded word audio for $verseKey word $wNum: $directUrl")
        wordAudioPlayerHelper.playAudio(url = directUrl, playbackToken = token)

        // Preload next 5 contiguous word files in the background to ensure instantaneous taps
        val nextWordUrls = mutableListOf<String>()
        for (i in 1..5) {
            val nextDirectUrl = String.format("wbw/%03d_%03d_%03d.mp3", sNum, aNum, wNum + i)
            nextWordUrls.add(nextDirectUrl)
        }
        wordAudioPlayerHelper.prefetch(nextWordUrls)
    }

    fun downloadSurahOffline(surahId: Int) {
        downloadSurahOfflineWithReciter(surahId, selectedReciterId)
    }

    var isDownloadingAll by mutableStateOf(false)
    var isDownloadAllPaused by mutableStateOf(false)
    var downloadAllProgress by mutableStateOf(0f)
    var downloadAllStatusText by mutableStateOf("")
    private var downloadAllJob: kotlinx.coroutines.Job? = null

    fun pauseDownloadAll() {
        isDownloadAllPaused = true
        downloadAllStatusText = "Download paused"
    }

    fun resumeDownloadAll() {
        isDownloadAllPaused = false
        downloadAllStatusText = "Resuming download..."
    }

    fun cancelDownloadAll() {
        downloadAllJob?.cancel()
        downloadAllJob = null
        isDownloadingAll = false
        isDownloadAllPaused = false
        downloadAllProgress = 0f
        downloadAllStatusText = "Download cancelled"
    }

    fun downloadSurahOfflineWithReciter(surahId: Int, reciterId: Int) {
        if (downloadingSurahIds.contains(surahId)) return
        viewModelScope.launch(Dispatchers.IO) {
            downloadSurahOfflineSuspend(surahId, reciterId)
        }
    }

    suspend fun downloadSurahOfflineSuspend(surahId: Int, reciterId: Int) {
        if (downloadingSurahIds.contains(surahId)) return
        withContext(Dispatchers.Main) {
            downloadingSurahIds = downloadingSurahIds + surahId
            downloadProgress = downloadProgress + (surahId to 0f)
            downloadSizeStatus = downloadSizeStatus + (surahId to "0.00 MB")
        }
        try {
            val activeReciter = reciterId
            val verses = repository.fetchVerses(surahId, activeReciter)
            val translations = repository.fetchTranslations(surahId)
            val audioFiles = repository.fetchRecitation(surahId, activeReciter)
            
            if (verses.isNotEmpty() && translations.isNotEmpty()) {
                val dir = getDownloadsDir()
                val versesFile = File(dir, "verses_$surahId.json")
                val transFile = File(dir, "translations_$surahId.json")
                val audioConfFile = File(dir, "audio_$surahId.json")
                
                val versesJson = localMoshi.adapter<List<com.example.data.api.QuranVerse>>(
                    Types.newParameterizedType(List::class.java, com.example.data.api.QuranVerse::class.java)
                ).toJson(verses)
                
                val transJson = localMoshi.adapter<List<com.example.data.api.TranslationVerse>>(
                    Types.newParameterizedType(List::class.java, com.example.data.api.TranslationVerse::class.java)
                ).toJson(translations)
                
                val audioJson = localMoshi.adapter<List<com.example.data.api.AudioFile>>(
                    Types.newParameterizedType(List::class.java, com.example.data.api.AudioFile::class.java)
                ).toJson(audioFiles)
                
                versesFile.writeText(versesJson)
                transFile.writeText(transJson)
                audioConfFile.writeText(audioJson)

                val totalFiles = audioFiles.size
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                var totalDownloadedBytes = versesFile.length() + transFile.length() + audioConfFile.length()
                var completedFiles = 0

                suspend fun updateUIProgress() {
                    val currentBytes = totalDownloadedBytes
                    val currentCompleted = completedFiles
                    val sizeInMB = currentBytes.toDouble() / (1024 * 1024)
                    val formattedSize = String.format("%.2f MB", sizeInMB)
                    val progress = if (totalFiles > 0) currentCompleted.toFloat() / totalFiles else 0f
                    
                    withContext(Dispatchers.Main) {
                        downloadProgress = downloadProgress + (surahId to progress)
                        downloadSizeStatus = downloadSizeStatus + (surahId to formattedSize)
                    }
                }

                val filesToDownload = mutableListOf<Pair<String, File>>()
                audioFiles.forEach { af ->
                    val resolvedUrl = audioPlayerHelper.resolveUrl(af.url)
                    val fileName = audioPlayerHelper.getCacheFileName(resolvedUrl)
                    val file = File(dir, fileName)
                    if (file.exists() && file.length() > 0) {
                        totalDownloadedBytes += file.length()
                        completedFiles++
                    } else {
                        filesToDownload.add(resolvedUrl to file)
                    }
                }

                updateUIProgress()

                if (filesToDownload.isNotEmpty()) {
                    val semaphore = kotlinx.coroutines.sync.Semaphore(5)
                    coroutineScope {
                        val jobs = filesToDownload.map { pair ->
                            val url = pair.first
                            val file = pair.second
                            launch(Dispatchers.IO) {
                                semaphore.acquire()
                                try {
                                    while (isDownloadingAll && isDownloadAllPaused) {
                                        kotlinx.coroutines.delay(500)
                                        if (!isDownloadingAll) return@launch
                                    }
                                    if (!downloadingSurahIds.contains(surahId)) return@launch

                                    val request = okhttp3.Request.Builder()
                                        .url(url)
                                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) TaqwaHub/1.0")
                                        .build()
                                    client.newCall(request).execute().use { response ->
                                        if (response.isSuccessful) {
                                            val body = response.body
                                            if (body != null) {
                                                val tempFile = File(dir, "${file.name}.tmp")
                                                var bytesWritten = 0L
                                                body.byteStream().use { input ->
                                                    tempFile.outputStream().use { output ->
                                                        val buffer = ByteArray(8192)
                                                        var bytesRead: Int
                                                        while (input.read(buffer).also { bytesRead = it } != -1) {
                                                            while (isDownloadingAll && isDownloadAllPaused) {
                                                                kotlinx.coroutines.delay(500)
                                                                if (!isDownloadingAll) return@use
                                                            }
                                                            if (!downloadingSurahIds.contains(surahId)) return@use
                                                            output.write(buffer, 0, bytesRead)
                                                            bytesWritten += bytesRead
                                                            synchronized(this@TaqwaViewModel) {
                                                                totalDownloadedBytes += bytesRead
                                                            }
                                                        }
                                                    }
                                                }
                                                if (tempFile.exists() && tempFile.length() > 0) {
                                                    if (file.exists()) file.delete()
                                                    tempFile.renameTo(file)
                                                }
                                            }
                                        } else {
                                            Log.e("TaqwaViewModel", "HTTP error ${response.code} downloading $url")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error downloading $url: ${e.message}")
                                } finally {
                                    semaphore.release()
                                    synchronized(this@TaqwaViewModel) {
                                        completedFiles++
                                    }
                                    updateUIProgress()
                                }
                            }
                        }
                        jobs.forEach { it.join() }
                    }
                }

                withContext(Dispatchers.Main) {
                    downloadedSurahIds = downloadedSurahIds + surahId
                }
            }
        } catch (e: Exception) {
            Log.e("TaqwaViewModel", "Error downloading Surah $surahId: ${e.message}", e)
        } finally {
            withContext(Dispatchers.Main) {
                downloadingSurahIds = downloadingSurahIds - surahId
            }
        }
    }

    fun deleteSurahOffline(surahId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = getDownloadsDir()
                val versesFile = File(dir, "verses_$surahId.json")
                val transFile = File(dir, "translations_$surahId.json")
                val audioConfFile = File(dir, "audio_$surahId.json")

                if (audioConfFile.exists()) {
                    try {
                        val audioJson = audioConfFile.readText()
                        val loadedAudio = localMoshi.adapter<List<com.example.data.api.AudioFile>>(
                            Types.newParameterizedType(List::class.java, com.example.data.api.AudioFile::class.java)
                        ).fromJson(audioJson)
                        loadedAudio?.forEach { af ->
                            val resolvedUrl = audioPlayerHelper.resolveUrl(af.url)
                            val fileName = audioPlayerHelper.getCacheFileName(resolvedUrl)
                            val audioFile = File(dir, fileName)
                            if (audioFile.exists()) audioFile.delete()
                        }
                    } catch (e: Exception) {
                        Log.e("TaqwaViewModel", "Error deleting audio files for surah $surahId", e)
                    }
                }

                if (versesFile.exists()) versesFile.delete()
                if (transFile.exists()) transFile.delete()
                if (audioConfFile.exists()) audioConfFile.delete()

                withContext(Dispatchers.Main) {
                    downloadedSurahIds = downloadedSurahIds - surahId
                    downloadProgress = downloadProgress - surahId
                    downloadSizeStatus = downloadSizeStatus - surahId
                }
                Log.d("TaqwaViewModel", "Successfully deleted offline files for Surah $surahId")
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error deleting Surah $surahId offline: ${e.message}", e)
            }
        }
    }

    fun deleteAllDownloadedSurahs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = getDownloadsDir()
                if (dir.exists()) {
                    dir.listFiles()?.forEach { it.delete() }
                }
                withContext(Dispatchers.Main) {
                    downloadedSurahIds = emptySet()
                    downloadProgress = emptyMap()
                    downloadSizeStatus = emptyMap()
                }
                Log.d("TaqwaViewModel", "Successfully deleted all downloaded surahs.")
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error deleting all downloaded surahs: ${e.message}", e)
            }
        }
    }

    fun downloadAllSurahs(reciterId: Int) {
        if (isDownloadingAll) return
        isDownloadingAll = true
        isDownloadAllPaused = false
        downloadAllProgress = 0f
        downloadAllStatusText = "Preparing to download all surahs..."
        downloadAllJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val allSurahs = com.example.data.IslamicData.surahs
                val total = allSurahs.size
                var completedCount = 0

                val semaphore = kotlinx.coroutines.sync.Semaphore(3) // 3 concurrent surahs
                coroutineScope {
                    allSurahs.forEach { surah ->
                        while (isDownloadAllPaused) {
                            kotlinx.coroutines.delay(500)
                            if (!isDownloadingAll) return@coroutineScope
                        }
                        if (!isDownloadingAll) return@coroutineScope

                        if (!downloadedSurahIds.contains(surah.id)) {
                            launch(Dispatchers.IO) {
                                semaphore.acquire()
                                try {
                                    while (isDownloadAllPaused) {
                                        kotlinx.coroutines.delay(500)
                                        if (!isDownloadingAll) return@launch
                                    }
                                    if (!isDownloadingAll) return@launch

                                    downloadSurahOfflineSuspend(surah.id, reciterId)
                                } catch (e: Exception) {
                                    Log.e("TaqwaViewModel", "Error downloading surah ${surah.id}: ${e.message}")
                                } finally {
                                    semaphore.release()
                                    synchronized(this@TaqwaViewModel) {
                                        completedCount++
                                        val prog = completedCount.toFloat() / total
                                        launch(Dispatchers.Main) {
                                            downloadAllProgress = prog
                                            downloadAllStatusText = "Downloaded Surah ${surah.name} ($completedCount/$total)"
                                        }
                                    }
                                }
                            }
                        } else {
                            synchronized(this@TaqwaViewModel) {
                                completedCount++
                                val prog = completedCount.toFloat() / total
                                launch(Dispatchers.Main) {
                                    downloadAllProgress = prog
                                    downloadAllStatusText = "Surah ${surah.name} already downloaded ($completedCount/$total)"
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error in downloadAllSurahs: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) {
                    if (isDownloadingAll) {
                        isDownloadingAll = false
                        isDownloadAllPaused = false
                        downloadAllProgress = 1f
                        downloadAllStatusText = "All surahs downloaded successfully!"
                    }
                }
            }
        }
    }

    private fun islamicSurahFallback(id: Int): Surah {
        return Surah(id, "Surah $id", "سورة", 7, "Mecca")
    }

    // Chat AI (TaqwaHub AI Companion)
    data class ChatMessage(
        val role: String,
        val text: String,
        val citations: List<String> = emptyList(),
        val citationFullTexts: List<String> = emptyList(),
        val id: String = java.util.UUID.randomUUID().toString(),
        val rating: String = "none", // "like", "dislike", "none"
        val reportMessage: String = ""
    )

    var chatMessages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set
    var isChatLoading by mutableStateOf(false)
        private set
    var isAiStreaming by mutableStateOf(false)
        private set

    var learnedUserName by mutableStateOf("")
    var learnedTalkingStyle by mutableStateOf("")
    var learnedPrimaryConcern by mutableStateOf("")
    var learnedCustomKnowledge by mutableStateOf<List<String>>(emptyList())
    var chatThinkSteps by mutableStateOf<List<String>>(emptyList())
    var chatCurrentStepText by mutableStateOf("")
    var chatCurrentProgress by mutableStateOf(0f)
    var queryCount by mutableStateOf(0)
    var globalLeaderboard by mutableStateOf<List<UserStatsEntity>>(emptyList())
        private set

    var hasLeaderboardUpdate by mutableStateOf(false)
    var hasUnreadSupportReply by mutableStateOf(false)

    fun getLastSeenComplaintsTime(): Long {
        val prefs = getApplication<Application>().getSharedPreferences("taqwahub_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getLong("last_seen_complaints_time", 0L)
    }

    fun saveLastSeenComplaintsTime() {
        val prefs = getApplication<Application>().getSharedPreferences("taqwahub_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putLong("last_seen_complaints_time", System.currentTimeMillis()).apply()
    }

    fun listenToLeaderboard() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("TaqwaViewModel", "Leaderboard listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val board = mutableListOf<UserStatsEntity>()
                        val currentWeek = getLeaderboardWeekCode(getSynchronizedTime())
                        for (doc in snapshot.documents) {
                            val statsMap = doc.get("userStats") as? Map<String, Any>
                            if (statsMap != null) {
                                val rawWeeklyXp = (statsMap["weeklyXp"] as? Long)?.toInt() ?: 0
                                val rawLastActiveWeek = (statsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0
                                
                                val activeWeeklyXp = if (rawLastActiveWeek == currentWeek) rawWeeklyXp else 0

                                val stats = UserStatsEntity(
                                    id = doc.id.hashCode(),
                                    totalTasksCompleted = (statsMap["totalTasksCompleted"] as? Long)?.toInt() ?: 0,
                                    daysActive = (statsMap["daysActive"] as? Long)?.toInt() ?: 1,
                                    quranProgress = (statsMap["quranProgress"] as? Long)?.toInt() ?: 0,
                                    lastReadSurah = (statsMap["lastReadSurah"] as? Long)?.toInt() ?: 1,
                                    lastReadVerse = (statsMap["lastReadVerse"] as? Long)?.toInt() ?: 1,
                                    lastReadVerseKey = statsMap["lastReadVerseKey"] as? String ?: "1:1",
                                    tasbeehCount = (statsMap["tasbeehCount"] as? Long)?.toInt() ?: 0,
                                    lastResetDate = statsMap["lastResetDate"] as? String ?: "",
                                    currentStreak = (statsMap["currentStreak"] as? Long)?.toInt() ?: 0,
                                    streakChancesLeft = ((statsMap["streakShields"] as? Long)?.toInt() ?: ((statsMap["streakChancesLeft"] as? Long)?.toInt() ?: 0)).coerceIn(0, 2),
                                    longestStreak = (statsMap["longestStreak"] as? Long)?.toInt() ?: 0,
                                    totalXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0,
                                    weeklyXp = activeWeeklyXp,
                                    lastActiveWeekOfYear = rawLastActiveWeek,
                                    name = (statsMap["name"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("displayName") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                    username = (statsMap["username"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("username") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                    gender = (statsMap["gender"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sectGender") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                    sectOrCast = (statsMap["sectOrCast"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sect") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("cast") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                    email = statsMap["email"] as? String ?: "",
                                    completedSurahs = statsMap["completedSurahs"] as? String ?: "",
                                    firstPlaceCount = (statsMap["firstPlaceCount"] as? Long)?.toInt() ?: 0,
                                    secondPlaceCount = (statsMap["secondPlaceCount"] as? Long)?.toInt() ?: 0,
                                    thirdPlaceCount = (statsMap["thirdPlaceCount"] as? Long)?.toInt() ?: 0,
                                    isBlocked = statsMap["isBlocked"] as? Boolean ?: false,
                                    isVerified = statsMap["isVerified"] as? Boolean ?: false,
                                    profilePictureBase64 = statsMap["profilePictureBase64"] as? String ?: "",
                                    lastWeekXp = (statsMap["lastWeekXp"] as? Long)?.toInt() ?: 0,
                                    lastWeekCode = (statsMap["lastWeekCode"] as? Long)?.toInt() ?: 0
                                )
                                if (!stats.isBlocked) {
                                    board.add(stats)
                                }
                            }
                        }
                        // Sort by weeklyXp descending, if weeklyXp is tied, sort by totalXp descending
                        val sortedBoard = board.sortedWith(
                            compareByDescending<UserStatsEntity> { it.weeklyXp }
                                .thenByDescending { it.totalXp }
                        )
                        globalLeaderboard = sortedBoard
                        if (currentView != "leaderboard") {
                            hasLeaderboardUpdate = true
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e("TaqwaViewModel", "Error setting up leaderboard listener: ${e.message}")
            }
        }
    }

    fun listenToCurrentUserDoc(user: com.google.firebase.auth.FirebaseUser) {
        currentUserDocListener?.remove()
        currentUserDocListener = null
        try {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(user.uid)
            currentUserDocListener = userDocRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("TaqwaViewModel", "Current user doc listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            if (isSyncingData) {
                                Log.d("TaqwaViewModel", "Realtime update ignored: active sync is currently in progress.")
                                return@launch
                            }
                            val remoteLastUpdated = snapshot.getLong("lastUpdatedAt") ?: 0L
                            val sharedPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                            val localLastUpdated = sharedPrefs.getLong("last_local_update", 0L)
                            val currentLocalStats = repository.taqwaDao.getUserStatsDirect()
                            val isLocalEmpty = currentLocalStats == null || 
                                    (currentLocalStats.username.isBlank() && currentLocalStats.name.isBlank() && 
                                     currentLocalStats.totalTasksCompleted == 0 && currentLocalStats.totalXp == 0)
                            
                            if (remoteLastUpdated > localLastUpdated || isLocalEmpty) {
                                Log.d("TaqwaViewModel", "Realtime update: Remote user doc is newer or local is empty. Pulling remote data down to Room...")
                                val remoteStatsMap = snapshot.get("userStats") as? Map<String, Any>
                                
                                val remoteWeek = (remoteStatsMap?.get("lastActiveWeekOfYear") as? Long)?.toInt() ?: 0
                                val localWeek = currentLocalStats?.lastActiveWeekOfYear ?: 0
                                if (localWeek > 0 && remoteWeek > localWeek) {
                                    val remoteFirst = (remoteStatsMap?.get("firstPlaceCount") as? Long)?.toInt() ?: 0
                                    val localFirst = currentLocalStats?.firstPlaceCount ?: 0
                                    val remoteSecond = (remoteStatsMap?.get("secondPlaceCount") as? Long)?.toInt() ?: 0
                                    val localSecond = currentLocalStats?.secondPlaceCount ?: 0
                                    val remoteThird = (remoteStatsMap?.get("thirdPlaceCount") as? Long)?.toInt() ?: 0
                                    val localThird = currentLocalStats?.thirdPlaceCount ?: 0

                                    var awardedTrophy = 0
                                    var rank = 4
                                    var bonusXp = 0

                                    if (remoteFirst > localFirst) {
                                        awardedTrophy = 1
                                        rank = 1
                                        bonusXp = 100
                                    } else if (remoteSecond > localSecond) {
                                        awardedTrophy = 2
                                        rank = 2
                                        bonusXp = 50
                                    } else if (remoteThird > localThird) {
                                        awardedTrophy = 3
                                        rank = 3
                                        bonusXp = 25
                                    }

                                    val oldWeeklyXp = currentLocalStats?.weeklyXp ?: 0
                                    val prefs = getSecurePrefs()
                                    prefs.edit()
                                        .putBoolean("leaderboard_award_pending", true)
                                        .putInt("leaderboard_award_rank", rank)
                                        .putInt("leaderboard_award_xp", oldWeeklyXp)
                                        .putInt("leaderboard_award_trophy", awardedTrophy)
                                        .putInt("leaderboard_award_bonus_xp", bonusXp)
                                        .apply()

                                    viewModelScope.launch(Dispatchers.Main) {
                                        weeklyResetRank = rank
                                        weeklyResetXp = oldWeeklyXp
                                        weeklyResetTrophy = awardedTrophy
                                        weeklyResetBonusXp = bonusXp
                                        showWeeklyResetDialog = true
                                    }
                                }
                                val pName = (remoteStatsMap?.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("displayName") as? String)?.takeIf { it.isNotBlank() } ?: ""
                                val pUsername = (remoteStatsMap?.get("username") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("username") as? String)?.takeIf { it.isNotBlank() } ?: ""
                                val pGender = (remoteStatsMap?.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("sectGender") as? String)?.takeIf { it.isNotBlank() } ?: ""
                                val pSectOrCast = (remoteStatsMap?.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("sect") as? String)?.takeIf { it.isNotBlank() } ?: (snapshot.get("cast") as? String)?.takeIf { it.isNotBlank() } ?: ""

                                if (pUsername.isNotBlank() || pName.isNotBlank() || remoteStatsMap != null) {
                                    try {
                                        val tprefs = getSecurePrefs()
                                        tprefs.edit().putBoolean("profile_completed_${user.uid}", true).apply()
                                        val stdPrefs = getApplication<Application>().getSharedPreferences("taqwa_prefs", android.content.Context.MODE_PRIVATE)
                                        stdPrefs.edit().putBoolean("profile_completed_${user.uid}", true).apply()
                                    } catch (e: Exception) {
                                        Log.e("TaqwaViewModel", "Error saving profile sync key", e)
                                    }
                                }

                                if (remoteStatsMap != null || pName.isNotBlank() || pUsername.isNotBlank()) {
                                    val mName = pName.ifBlank { currentLocalStats?.name ?: "" }
                                    val mUsername = pUsername.ifBlank { currentLocalStats?.username ?: "" }
                                    val mGender = pGender.ifBlank { currentLocalStats?.gender ?: "" }
                                    val mSectOrCast = pSectOrCast.ifBlank { currentLocalStats?.sectOrCast ?: "" }

                                    val stats = UserStatsEntity(
                                        id = 1,
                                        totalTasksCompleted = (remoteStatsMap?.get("totalTasksCompleted") as? Long)?.toInt() ?: (currentLocalStats?.totalTasksCompleted ?: 0),
                                        daysActive = (remoteStatsMap?.get("daysActive") as? Long)?.toInt() ?: (currentLocalStats?.daysActive ?: 1),
                                        quranProgress = (remoteStatsMap?.get("quranProgress") as? Long)?.toInt() ?: (currentLocalStats?.quranProgress ?: 0),
                                        lastReadSurah = (remoteStatsMap?.get("lastReadSurah") as? Long)?.toInt() ?: (currentLocalStats?.lastReadSurah ?: 1),
                                        lastReadVerse = (remoteStatsMap?.get("lastReadVerse") as? Long)?.toInt() ?: (currentLocalStats?.lastReadVerse ?: 1),
                                        lastReadVerseKey = remoteStatsMap?.get("lastReadVerseKey") as? String ?: (currentLocalStats?.lastReadVerseKey ?: "1:1"),
                                        tasbeehCount = (remoteStatsMap?.get("tasbeehCount") as? Long)?.toInt() ?: (currentLocalStats?.tasbeehCount ?: 0),
                                        lastResetDate = remoteStatsMap?.get("lastResetDate") as? String ?: (currentLocalStats?.lastResetDate ?: ""),
                                        currentStreak = (remoteStatsMap?.get("currentStreak") as? Long)?.toInt() ?: (currentLocalStats?.currentStreak ?: 0),
                                        streakChancesLeft = ((remoteStatsMap?.get("streakShields") as? Long)?.toInt() ?: ((remoteStatsMap?.get("streakChancesLeft") as? Long)?.toInt() ?: (currentLocalStats?.streakShields ?: 0))).coerceIn(0, 2),
                                        longestStreak = (remoteStatsMap?.get("longestStreak") as? Long)?.toInt() ?: (currentLocalStats?.longestStreak ?: 0),
                                        totalXp = (remoteStatsMap?.get("totalXp") as? Long)?.toInt() ?: (currentLocalStats?.totalXp ?: 0),
                                        weeklyXp = (remoteStatsMap?.get("weeklyXp") as? Long)?.toInt() ?: (currentLocalStats?.weeklyXp ?: 0),
                                        lastActiveWeekOfYear = (remoteStatsMap?.get("lastActiveWeekOfYear") as? Long)?.toInt() ?: (currentLocalStats?.lastActiveWeekOfYear ?: 0),
                                        name = mName,
                                        username = mUsername,
                                        gender = mGender,
                                        sectOrCast = mSectOrCast,
                                        email = remoteStatsMap?.get("email") as? String ?: (user.email ?: ""),
                                        completedSurahs = remoteStatsMap?.get("completedSurahs") as? String ?: (currentLocalStats?.completedSurahs ?: ""),
                                        firstPlaceCount = (remoteStatsMap?.get("firstPlaceCount") as? Long)?.toInt() ?: (currentLocalStats?.firstPlaceCount ?: 0),
                                        secondPlaceCount = (remoteStatsMap?.get("secondPlaceCount") as? Long)?.toInt() ?: (currentLocalStats?.secondPlaceCount ?: 0),
                                        thirdPlaceCount = (remoteStatsMap?.get("thirdPlaceCount") as? Long)?.toInt() ?: (currentLocalStats?.thirdPlaceCount ?: 0),
                                        isBlocked = remoteStatsMap?.get("isBlocked") as? Boolean ?: (currentLocalStats?.isBlocked ?: false),
                                        isVerified = remoteStatsMap?.get("isVerified") as? Boolean ?: (currentLocalStats?.isVerified ?: false),
                                        profilePictureBase64 = remoteStatsMap?.get("profilePictureBase64") as? String ?: (currentLocalStats?.profilePictureBase64 ?: ""),
                                        lastWeekXp = (remoteStatsMap?.get("lastWeekXp") as? Long)?.toInt() ?: (currentLocalStats?.lastWeekXp ?: 0),
                                        lastWeekCode = (remoteStatsMap?.get("lastWeekCode") as? Long)?.toInt() ?: (currentLocalStats?.lastWeekCode ?: 0),
                                        lastActiveDate = remoteStatsMap?.get("lastActiveDate") as? String ?: (currentLocalStats?.lastActiveDate ?: ""),
                                        streakShields = ((remoteStatsMap?.get("streakShields") as? Long)?.toInt() ?: ((remoteStatsMap?.get("streakChancesLeft") as? Long)?.toInt() ?: (currentLocalStats?.streakShields ?: 0))).coerceIn(0, 2),
                                        maxShields = 2,
                                        frozenDates = remoteStatsMap?.get("frozenDates") as? String ?: (currentLocalStats?.frozenDates ?: ""),
                                        activeDates = remoteStatsMap?.get("activeDates") as? String ?: (currentLocalStats?.activeDates ?: ""),
                                        lastShieldUsedDate = remoteStatsMap?.get("lastShieldUsedDate") as? String ?: (currentLocalStats?.lastShieldUsedDate ?: ""),
                                        streakRepairsAvailable = (remoteStatsMap?.get("streakRepairsAvailable") as? Long)?.toInt() ?: (currentLocalStats?.streakRepairsAvailable ?: 1)
                                    )
                                    repository.taqwaDao.insertUserStats(stats)
                                }

                                val remoteTasksList = snapshot.get("tasks") as? List<Map<String, Any>>
                                if (remoteTasksList != null && remoteTasksList.isNotEmpty()) {
                                    val tasks = remoteTasksList.map {
                                        TaskEntity(
                                            id = it["id"] as? String ?: "",
                                            title = it["title"] as? String ?: "",
                                            completed = it["completed"] as? Boolean ?: false,
                                            category = it["category"] as? String ?: "",
                                            description = it["description"] as? String ?: "",
                                            points = (it["points"] as? Long)?.toInt() ?: 10,
                                            tag = it["tag"] as? String ?: "",
                                            timerSeconds = (it["timerSeconds"] as? Long)?.toInt() ?: 0,
                                            isSystemTask = it["isSystemTask"] as? Boolean ?: false,
                                            isAuto = it["isAuto"] as? Boolean ?: false,
                                            autoType = it["autoType"] as? String ?: "",
                                            autoTarget = (it["autoTarget"] as? Long)?.toInt() ?: 0,
                                            autoProgress = (it["autoProgress"] as? Long)?.toInt() ?: 0,
                                            targetSurahNumber = (it["targetSurahNumber"] as? Long)?.toInt(),
                                            actionRoute = it["actionRoute"] as? String ?: ""
                                        )
                                    }.filter { it.id.isNotEmpty() }
                                    if (tasks.isNotEmpty()) {
                                        repository.taqwaDao.clearTasks()
                                        repository.taqwaDao.insertAllTasks(tasks)
                                    }
                                }

                                val remoteBookmarksList = snapshot.get("bookmarks") as? List<Map<String, Any>>
                                if (remoteBookmarksList != null) {
                                    val dbBookmarks = repository.taqwaDao.getAllBookmarksDirect()
                                    for (b in dbBookmarks) {
                                        repository.taqwaDao.deleteBookmarkById(b.id)
                                    }
                                    remoteBookmarksList.forEach {
                                        val bookmark = BookmarkEntity(
                                            id = it["id"] as? String ?: "",
                                            surahNumber = (it["surahNumber"] as? Long)?.toInt() ?: 1,
                                            surahName = it["surahName"] as? String ?: "",
                                            verseNumber = (it["verseNumber"] as? Long)?.toInt() ?: 1,
                                            verseKey = it["verseKey"] as? String ?: "",
                                            timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis(),
                                            isFlowMode = it["isFlowMode"] as? Boolean ?: false
                                        )
                                        repository.taqwaDao.insertBookmark(bookmark)
                                    }
                                }

                                 val remoteAllTimeList = snapshot.get("allTimeTasks") as? List<Map<String, Any>>
                                if (remoteAllTimeList != null) {
                                    val allTimeDb = repository.taqwaDao.getAllTimeTasksDirect()
                                    allTimeDb.forEach { repository.taqwaDao.deleteAllTimeTaskById(it.id) }
                                    remoteAllTimeList.forEach {
                                        val info = AllTimeTaskEntity(
                                            id = it["id"] as? String ?: "",
                                            taskId = it["taskId"] as? String ?: "",
                                            title = it["title"] as? String ?: "",
                                            category = it["category"] as? String ?: "",
                                            date = it["date"] as? String ?: "",
                                            completedAt = it["completedAt"] as? String ?: ""
                                        )
                                        repository.taqwaDao.insertAllTimeTask(info)
                                    }
                                    repository.recalculateAndSaveStreak()
                                }

                                val remoteAiChatState = snapshot.get("aiChatState") as? Map<String, Any>
                                if (remoteAiChatState != null) {
                                    val remoteQueryCount = (remoteAiChatState["queryCount"] as? Long)?.toInt() ?: 0
                                    val remoteIsChatLocked = remoteAiChatState["isChatLocked"] as? Boolean ?: false
                                    val remoteLockEndTime = remoteAiChatState["lockEndTime"] as? Long ?: 0L
                                    
                                    val remoteChatMsgsList = remoteAiChatState["chatMessages"] as? List<Map<String, Any>>
                                    val parsedChatMsgs = remoteChatMsgsList?.map {
                                        ChatMessage(
                                            role = it["role"] as? String ?: "user",
                                            text = it["text"] as? String ?: "",
                                            citations = (it["citations"] as? List<*>)?.mapNotNull { c -> c as? String } ?: emptyList(),
                                            citationFullTexts = (it["citationFullTexts"] as? List<*>)?.mapNotNull { c -> c as? String } ?: emptyList(),
                                            id = it["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                            rating = it["rating"] as? String ?: "none",
                                            reportMessage = it["reportMessage"] as? String ?: ""
                                        )
                                    } ?: emptyList()
                                    
                                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                                        queryCount = remoteQueryCount
                                        isChatLocked = remoteIsChatLocked
                                        aiLockEndTime = remoteLockEndTime
                                        chatMessages = parsedChatMsgs
                                        
                                        try {
                                            val tprefs = getSecurePrefs()
                                            val teditor = tprefs.edit()
                                            teditor.putInt("ai_query_count", queryCount)
                                            teditor.putBoolean("ai_is_chat_locked", isChatLocked)
                                            teditor.putLong("ai_lock_end_time", aiLockEndTime)
                                            val adapter = localMoshi.adapter<List<ChatMessage>>(
                                                com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatMessage::class.java)
                                            )
                                            teditor.putString("ai_chat_messages", adapter.toJson(chatMessages))
                                            teditor.apply()
                                        } catch(e: Exception) {}
                                    }
                                }
                                
                                sharedPrefs.edit().putLong("last_local_update", maxOf(remoteLastUpdated, System.currentTimeMillis())).apply()
                                Log.d("TaqwaViewModel", "Successfully completed realtime current user doc pull.")
                            }
                        } catch (err: Exception) {
                            Log.e("TaqwaViewModel", "Error processing realtime user update: ${err.message}")
                        }
                    }
                }
            }
        } catch (err: Exception) {
            Log.e("TaqwaViewModel", "Error setting up current user doc listener: ${err.message}")
        }
    }

    var isChatLocked by mutableStateOf(false)
    var aiLockEndTime by mutableStateOf(0L)
    var resetTimeRemaining by mutableStateOf("")

    // Weekly Leaderboard Reset & Sync
    var leaderboardResetTimeRemaining by mutableStateOf("")
    var showWeeklyResetDialog by mutableStateOf(false)
    var weeklyResetRank by mutableStateOf(0)
    var weeklyResetXp by mutableStateOf(0)
    var weeklyResetTrophy by mutableStateOf(0) // 1 = Gold, 2 = Silver, 3 = Bronze
    var weeklyResetBonusXp by mutableStateOf(0)

    private var networkTimeOffset = 0L
    private var leaderboardCountdownJob: kotlinx.coroutines.Job? = null

    init {
        loadChatMemory()
        fetchNetworkTimeOffset()
        startLeaderboardCountdown()
        checkPendingWeeklyAwards()
    }

    fun fetchNetworkTimeOffset() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("https://www.google.com")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val dateHeader = connection.getHeaderField("Date")
                if (dateHeader != null) {
                    val format = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                    val serverDate = format.parse(dateHeader)
                    if (serverDate != null) {
                        val serverTime = serverDate.time
                        val localTime = System.currentTimeMillis()
                        networkTimeOffset = serverTime - localTime
                        android.util.Log.d("TaqwaViewModel", "Synced network time offset: $networkTimeOffset ms")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TaqwaViewModel", "Failed to fetch network time offset: ${e.message}")
            }
        }
    }

    fun getSynchronizedTime(): Long {
        return System.currentTimeMillis() + networkTimeOffset
    }

    fun getLeaderboardWeekCode(timeMillis: Long, timeZoneId: String = "Asia/Karachi"): Int {
        val tz = java.util.TimeZone.getTimeZone(timeZoneId)
        val c = java.util.Calendar.getInstance(tz)
        c.timeInMillis = timeMillis
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        
        val dayOfWeek = c.get(java.util.Calendar.DAY_OF_WEEK)
        val daysSinceFriday = if (dayOfWeek >= java.util.Calendar.FRIDAY) {
            dayOfWeek - java.util.Calendar.FRIDAY
        } else {
            dayOfWeek + 7 - java.util.Calendar.FRIDAY
        }
        c.add(java.util.Calendar.DAY_OF_YEAR, -daysSinceFriday)
        
        val year = c.get(java.util.Calendar.YEAR)
        val month = c.get(java.util.Calendar.MONTH) + 1
        val day = c.get(java.util.Calendar.DAY_OF_MONTH)
        
        return year * 10000 + month * 100 + day
    }

    fun getNextResetTimeMillis(timeMillis: Long, timeZoneId: String = "Asia/Karachi"): Long {
        val tz = java.util.TimeZone.getTimeZone(timeZoneId)
        val c = java.util.Calendar.getInstance(tz)
        c.timeInMillis = timeMillis
        
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        
        val dayOfWeek = c.get(java.util.Calendar.DAY_OF_WEEK)
        val daysToFriday = if (dayOfWeek < java.util.Calendar.FRIDAY) {
            java.util.Calendar.FRIDAY - dayOfWeek
        } else {
            java.util.Calendar.FRIDAY + 7 - dayOfWeek
        }
        c.add(java.util.Calendar.DAY_OF_YEAR, daysToFriday)
        return c.timeInMillis
    }

    fun startLeaderboardCountdown() {
        leaderboardCountdownJob?.cancel()
        leaderboardCountdownJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (true) {
                val nowMillis = getSynchronizedTime()
                val nextResetMillis = getNextResetTimeMillis(nowMillis)
                val diff = nextResetMillis - nowMillis
                
                if (diff > 0) {
                    val days = diff / (1000 * 60 * 60 * 24)
                    val hours = (diff / (1000 * 60 * 60)) % 24
                    val minutes = (diff / (1000 * 60)) % 60
                    val seconds = (diff / 1000) % 60
                    
                    val formatted = if (days > 0) {
                        String.format("%dd %dh %dm", days, hours, minutes)
                    } else {
                        String.format("%dh %dm %ds", hours, minutes, seconds)
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        leaderboardResetTimeRemaining = formatted
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        leaderboardResetTimeRemaining = "00h 00m 00s"
                        checkAndProcessWeeklyReset()
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun checkAndProcessWeeklyReset() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentTime = getSynchronizedTime()
            val currentWeekCode = getLeaderboardWeekCode(currentTime)
            
            val currentStats = repository.taqwaDao.getUserStatsDirect() ?: return@launch
            val userLastWeekCode = currentStats.lastActiveWeekOfYear
            
            if (userLastWeekCode > 0 && userLastWeekCode < currentWeekCode) {
                // To guarantee correct ranking, we fetch all users from Firestore.
                // We reconstruct the leaderboard specifically for the target week (userLastWeekCode).
                var boardList = emptyList<UserStatsEntity>()
                try {
                    val db = FirebaseFirestore.getInstance()
                    val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("users").get())
                    val board = mutableListOf<UserStatsEntity>()
                    for (doc in snapshot.documents) {
                        val statsMap = doc.get("userStats") as? Map<String, Any>
                        if (statsMap != null) {
                            val rawWeeklyXp = (statsMap["weeklyXp"] as? Long)?.toInt() ?: 0
                            val rawLastActiveWeek = (statsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0
                            val lastWeekXp = (statsMap["lastWeekXp"] as? Long)?.toInt() ?: 0
                            val lastWeekCode = (statsMap["lastWeekCode"] as? Long)?.toInt() ?: 0
                            val isBlocked = statsMap["isBlocked"] as? Boolean ?: false
                            
                            if (isBlocked) continue

                            // Calculate target week XP
                            val activeWeeklyXp = if (rawLastActiveWeek == userLastWeekCode) {
                                rawWeeklyXp
                            } else if (lastWeekCode == userLastWeekCode) {
                                lastWeekXp
                            } else {
                                0
                            }

                            val stats = UserStatsEntity(
                                id = doc.id.hashCode(),
                                totalTasksCompleted = (statsMap["totalTasksCompleted"] as? Long)?.toInt() ?: 0,
                                daysActive = (statsMap["daysActive"] as? Long)?.toInt() ?: 1,
                                quranProgress = (statsMap["quranProgress"] as? Long)?.toInt() ?: 0,
                                lastReadSurah = (statsMap["lastReadSurah"] as? Long)?.toInt() ?: 1,
                                lastReadVerse = (statsMap["lastReadVerse"] as? Long)?.toInt() ?: 1,
                                lastReadVerseKey = statsMap["lastReadVerseKey"] as? String ?: "1:1",
                                tasbeehCount = (statsMap["tasbeehCount"] as? Long)?.toInt() ?: 0,
                                lastResetDate = statsMap["lastResetDate"] as? String ?: "",
                                currentStreak = (statsMap["currentStreak"] as? Long)?.toInt() ?: 0,
                                streakChancesLeft = ((statsMap["streakShields"] as? Long)?.toInt() ?: ((statsMap["streakChancesLeft"] as? Long)?.toInt() ?: 0)).coerceIn(0, 2),
                                streakShields = ((statsMap["streakShields"] as? Long)?.toInt() ?: ((statsMap["streakChancesLeft"] as? Long)?.toInt() ?: 0)).coerceIn(0, 2),
                                longestStreak = (statsMap["longestStreak"] as? Long)?.toInt() ?: 0,
                                totalXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0,
                                weeklyXp = activeWeeklyXp,
                                lastActiveWeekOfYear = rawLastActiveWeek,
                                name = (statsMap["name"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("displayName") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                username = (statsMap["username"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("username") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                gender = (statsMap["gender"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sectGender") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                sectOrCast = (statsMap["sectOrCast"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("sect") as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("cast") as? String)?.takeIf { it.isNotBlank() } ?: "",
                                email = statsMap["email"] as? String ?: "",
                                completedSurahs = statsMap["completedSurahs"] as? String ?: "",
                                firstPlaceCount = (statsMap["firstPlaceCount"] as? Long)?.toInt() ?: 0,
                                secondPlaceCount = (statsMap["secondPlaceCount"] as? Long)?.toInt() ?: 0,
                                thirdPlaceCount = (statsMap["thirdPlaceCount"] as? Long)?.toInt() ?: 0,
                                isBlocked = isBlocked,
                                isVerified = statsMap["isVerified"] as? Boolean ?: false,
                                profilePictureBase64 = statsMap["profilePictureBase64"] as? String ?: "",
                                lastWeekXp = lastWeekXp,
                                lastWeekCode = lastWeekCode
                            )
                            board.add(stats)
                        }
                    }
                    boardList = board.sortedWith(
                        compareByDescending<UserStatsEntity> { it.weeklyXp }
                            .thenByDescending { it.totalXp }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("TaqwaViewModel", "Direct Firestore fetch for weekly reset ranking failed, falling back to cached list.", e)
                    boardList = globalLeaderboard
                }

                // If the board list is completely empty (e.g. offline and no cache), we MUST defer the reset
                // to avoid wiping weekly stats with no ranking possible.
                if (boardList.isEmpty()) {
                    android.util.Log.w("TaqwaViewModel", "Leaderboard data is empty. Deferring weekly reset until data is available.")
                    return@launch
                }

                val currentEmail = currentStats.email.trim().lowercase()
                val currentUsername = currentStats.username.trim().lowercase()
                val currentName = currentStats.name.trim().lowercase()

                val myIndex = boardList.indexOfFirst { row ->
                    val rowEmail = row.email.trim().lowercase()
                    val rowUsername = row.username.trim().lowercase()
                    val rowName = row.name.trim().lowercase()
                    
                    if (currentEmail.isNotBlank() && rowEmail.isNotBlank()) {
                        rowEmail == currentEmail
                    } else if (currentUsername.isNotBlank() && rowUsername.isNotBlank()) {
                        rowUsername == currentUsername
                    } else {
                        rowName == currentName && rowName != "servant of allah"
                    }
                }

                val myWeeklyXp = currentStats.weeklyXp
                var myRank = 0
                if (myIndex >= 0) {
                    // Competition Dense Ranking (ties share same rank)
                    val userRanks = IntArray(boardList.size)
                    var currentRank = 1
                    for (i in boardList.indices) {
                        if (i > 0) {
                            val prev = boardList[i - 1]
                            val curr = boardList[i]
                            if (curr.weeklyXp != prev.weeklyXp || curr.totalXp != prev.totalXp) {
                                currentRank = i + 1
                            }
                        }
                        userRanks[i] = currentRank
                    }
                    myRank = userRanks[myIndex]
                }

                var awardedTrophyPlace = 0
                var bonusXp = 0
                
                if (myWeeklyXp > 0 && myRank in 1..3) {
                    awardedTrophyPlace = myRank
                    bonusXp = when (myRank) {
                        1 -> 100
                        2 -> 50
                        else -> 25
                    }
                }
                
                val updatedStats = when (awardedTrophyPlace) {
                    1 -> currentStats.copy(
                        firstPlaceCount = currentStats.firstPlaceCount + 1,
                        totalXp = currentStats.totalXp + bonusXp,
                        lastWeekXp = currentStats.weeklyXp,
                        lastWeekCode = currentStats.lastActiveWeekOfYear,
                        weeklyXp = 0,
                        lastActiveWeekOfYear = currentWeekCode
                    )
                    2 -> currentStats.copy(
                        secondPlaceCount = currentStats.secondPlaceCount + 1,
                        totalXp = currentStats.totalXp + bonusXp,
                        lastWeekXp = currentStats.weeklyXp,
                        lastWeekCode = currentStats.lastActiveWeekOfYear,
                        weeklyXp = 0,
                        lastActiveWeekOfYear = currentWeekCode
                    )
                    3 -> currentStats.copy(
                        thirdPlaceCount = currentStats.thirdPlaceCount + 1,
                        totalXp = currentStats.totalXp + bonusXp,
                        lastWeekXp = currentStats.weeklyXp,
                        lastWeekCode = currentStats.lastActiveWeekOfYear,
                        weeklyXp = 0,
                        lastActiveWeekOfYear = currentWeekCode
                    )
                    else -> currentStats.copy(
                        lastWeekXp = currentStats.weeklyXp,
                        lastWeekCode = currentStats.lastActiveWeekOfYear,
                        weeklyXp = 0,
                        lastActiveWeekOfYear = currentWeekCode
                    )
                }
                
                repository.saveUserStats(updatedStats)
                markLocalUpdateAndSync()
                
                val prefs = getSecurePrefs()
                prefs.edit()
                    .putBoolean("leaderboard_award_pending", true)
                    .putInt("leaderboard_award_rank", myRank)
                    .putInt("leaderboard_award_xp", myWeeklyXp)
                    .putInt("leaderboard_award_trophy", awardedTrophyPlace)
                    .putInt("leaderboard_award_bonus_xp", bonusXp)
                    .apply()
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    weeklyResetRank = myRank
                    weeklyResetXp = myWeeklyXp
                    weeklyResetTrophy = awardedTrophyPlace
                    weeklyResetBonusXp = bonusXp
                    showWeeklyResetDialog = true
                }
                
                android.util.Log.d("TaqwaViewModel", "Weekly reset processed! Earned rank: $myRank, Weekly XP: $myWeeklyXp, Trophy Place: $awardedTrophyPlace")
            } else if (userLastWeekCode == 0) {
                val updatedStats = currentStats.copy(lastActiveWeekOfYear = currentWeekCode)
                repository.saveUserStats(updatedStats)
                markLocalUpdateAndSync()
            }
            runGlobalWeeklyProcessor(currentWeekCode)
        }
    }

    fun runGlobalWeeklyProcessor(currentWeekCode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = com.google.android.gms.tasks.Tasks.await(db.collection("users").get())
                if (snapshot == null || snapshot.isEmpty) return@launch

                val usersPendingReset = mutableListOf<DocumentSnapshot>()
                val weeksToProcess = mutableSetOf<Int>()

                for (doc in snapshot.documents) {
                    val statsMap = doc.get("userStats") as? Map<String, Any> ?: continue
                    val rawLastActiveWeek = (statsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0
                    if (rawLastActiveWeek > 0 && rawLastActiveWeek < currentWeekCode) {
                        usersPendingReset.add(doc)
                        weeksToProcess.add(rawLastActiveWeek)
                    }
                }

                if (usersPendingReset.isEmpty()) {
                    Log.d("WeeklyProcessor", "All global users are already up-to-date. No weekly resets to process.")
                    return@launch
                }

                Log.d("WeeklyProcessor", "Processing weekly resets for ${usersPendingReset.size} users across weeks: $weeksToProcess")

                for (targetWeek in weeksToProcess) {
                    val board = mutableListOf<UserStatsEntity>()
                    for (doc in snapshot.documents) {
                        val statsMap = doc.get("userStats") as? Map<String, Any> ?: continue
                        val rawWeeklyXp = (statsMap["weeklyXp"] as? Long)?.toInt() ?: 0
                        val rawLastActiveWeek = (statsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0
                        val lastWeekXp = (statsMap["lastWeekXp"] as? Long)?.toInt() ?: 0
                        val lastWeekCode = (statsMap["lastWeekCode"] as? Long)?.toInt() ?: 0
                        val isBlocked = statsMap["isBlocked"] as? Boolean ?: false

                        if (isBlocked) continue

                        val activeWeeklyXp = if (rawLastActiveWeek == targetWeek) {
                            rawWeeklyXp
                        } else if (lastWeekCode == targetWeek) {
                            lastWeekXp
                        } else {
                            0
                        }

                        board.add(UserStatsEntity(
                            id = doc.id.hashCode(),
                            email = statsMap["email"] as? String ?: "",
                            username = statsMap["username"] as? String ?: "",
                            name = (statsMap["name"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("name") as? String) ?: "",
                            weeklyXp = activeWeeklyXp,
                            totalXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0,
                            firstPlaceCount = (statsMap["firstPlaceCount"] as? Long)?.toInt() ?: 0,
                            secondPlaceCount = (statsMap["secondPlaceCount"] as? Long)?.toInt() ?: 0,
                            thirdPlaceCount = (statsMap["thirdPlaceCount"] as? Long)?.toInt() ?: 0,
                            lastActiveWeekOfYear = rawLastActiveWeek
                        ))
                    }

                    val sortedBoard = board.sortedWith(
                        compareByDescending<UserStatsEntity> { it.weeklyXp }
                            .thenByDescending { it.totalXp }
                    )

                    val userRanks = IntArray(sortedBoard.size)
                    var currentRank = 1
                    for (i in sortedBoard.indices) {
                        if (i > 0) {
                            val prev = sortedBoard[i - 1]
                            val curr = sortedBoard[i]
                            if (curr.weeklyXp != prev.weeklyXp || curr.totalXp != prev.totalXp) {
                                currentRank = i + 1
                            }
                        }
                        userRanks[i] = currentRank
                    }

                    for (doc in usersPendingReset) {
                        val statsMap = doc.get("userStats") as? Map<String, Any> ?: continue
                        val rawLastActiveWeek = (statsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0
                        if (rawLastActiveWeek != targetWeek) continue

                        val userEmail = (statsMap["email"] as? String ?: "").trim().lowercase()
                        val userUsername = (statsMap["username"] as? String ?: "").trim().lowercase()
                        val userName = ((statsMap["name"] as? String)?.takeIf { it.isNotBlank() } ?: (doc.get("name") as? String) ?: "").trim().lowercase()

                        val idx = sortedBoard.indexOfFirst { row ->
                            val rEmail = row.email.trim().lowercase()
                            val rUsername = row.username.trim().lowercase()
                            val rName = row.name.trim().lowercase()
                            if (userEmail.isNotBlank() && rEmail.isNotBlank()) {
                                rEmail == userEmail
                            } else if (userUsername.isNotBlank() && rUsername.isNotBlank()) {
                                rUsername == userUsername
                            } else {
                                rName == userName && rName != "servant of allah"
                            }
                        }

                        val myRank = if (idx >= 0) userRanks[idx] else 0
                        val myWeeklyXp = (statsMap["weeklyXp"] as? Long)?.toInt() ?: 0

                        var awardedTrophyPlace = 0
                        var bonusXp = 0
                        if (myWeeklyXp > 0 && myRank in 1..3) {
                            awardedTrophyPlace = myRank
                            bonusXp = when (myRank) {
                                1 -> 100
                                2 -> 50
                                else -> 25
                            }
                        }

                        val updatedMap = statsMap.toMutableMap()
                        updatedMap["lastWeekXp"] = myWeeklyXp
                        updatedMap["lastWeekCode"] = rawLastActiveWeek
                        updatedMap["weeklyXp"] = 0
                        updatedMap["lastActiveWeekOfYear"] = currentWeekCode

                        if (awardedTrophyPlace == 1) {
                            val currCount = (statsMap["firstPlaceCount"] as? Long)?.toInt() ?: 0
                            updatedMap["firstPlaceCount"] = currCount + 1
                            val currXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0
                            updatedMap["totalXp"] = currXp + bonusXp
                        } else if (awardedTrophyPlace == 2) {
                            val currCount = (statsMap["secondPlaceCount"] as? Long)?.toInt() ?: 0
                            updatedMap["secondPlaceCount"] = currCount + 1
                            val currXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0
                            updatedMap["totalXp"] = currXp + bonusXp
                        } else if (awardedTrophyPlace == 3) {
                            val currCount = (statsMap["thirdPlaceCount"] as? Long)?.toInt() ?: 0
                            updatedMap["thirdPlaceCount"] = currCount + 1
                            val currXp = (statsMap["totalXp"] as? Long)?.toInt() ?: 0
                            updatedMap["totalXp"] = currXp + bonusXp
                        }

                        db.collection("users").document(doc.id)
                            .update("userStats", updatedMap)
                            .addOnSuccessListener {
                                Log.d("WeeklyProcessor", "Successfully processed weekly reset and trophies globally for offline user ID: ${doc.id}")
                            }
                    }
                }
            } catch (e: Exception) {
                Log.e("WeeklyProcessor", "Error running global weekly processor: ${e.message}", e)
            }
        }
    }

    fun checkPendingWeeklyAwards() {
        val prefs = getSecurePrefs()
        if (prefs.getBoolean("leaderboard_award_pending", false)) {
            weeklyResetRank = prefs.getInt("leaderboard_award_rank", 0)
            weeklyResetXp = prefs.getInt("leaderboard_award_xp", 0)
            weeklyResetTrophy = prefs.getInt("leaderboard_award_trophy", 0)
            weeklyResetBonusXp = prefs.getInt("leaderboard_award_bonus_xp", 0)
            showWeeklyResetDialog = true
        }
    }

    fun dismissWeeklyAwardDialog() {
        showWeeklyResetDialog = false
        val prefs = getSecurePrefs()
        prefs.edit().remove("leaderboard_award_pending").apply()
    }

    private fun getNextMidnightMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun performSpiritualIndexSearch(query: String): Pair<List<String>, List<String>> {
        val lowercaseQuery = query.lowercase()
        val tokens = lowercaseQuery
            .replace(Regex("[^a-zA-Z0-9 \\u0600-\\u06FF]"), "")
            .split(" ")
            .filter { it.isNotEmpty() }
        
        val matchedSourcesSummary = mutableListOf<String>()
        val matchedContentsForContext = mutableListOf<String>()
        
        if (tokens.isEmpty()) return Pair(emptyList(), emptyList())
        
        val matchedCategories = mutableSetOf<String>()
        
        // Comprehensive bilingual/multilingual check
        val distressWords = listOf(
            "anxiety", "anxious", "sad", "depression", "depressed", "grief", "sorrow", "stress", "stressed", "worried", "trouble", "pain", "distress", "fear", "scared", "fearful", "paralyzed", "difficult", "difficulty", "unhappy", "problem", "crisis", "difficulties", "sick", "ill", "cure", "shifa", "tension", "dar", "khauf", "behisi", "bimari", "mushkil", "pareshan", "pareshani", "mayusi", "ranj", "gham",
            "پریشانی", "پریشان", "مشکل", "غم", "اداس", "اداسی", "درد", "بیماری", "شفا", "خوف", "ڈر", "مایوسی", "مصیبت", "تکلیف"
        )
        val morningWords = listOf(
            "morning", "wake", "day", "sunrise", "subah", "sobha", "fajr",
            "صبح", "فجر"
        )
        val sleepWords = listOf(
            "sleep", "night", "bed", "dream", "sleeping", "soye", "raat", "soya", "sone", "ishaa", "isha", "shaaam", "shaam", "evening",
            "رات", "سونا", "نیند", "شام"
        )
        val foodWords = listOf(
            "food", "eat", "drink", "hungry", "meal", "eating", "khana", "peena", "bhook", "rizq", "sustenance", "wealth", "paisa", "money",
            "رِزق", "رزق", "کھانا", "بھوک", "دولت"
        )
        val knowledgeWords = listOf(
            "knowledge", "study", "exam", "learn", "wisdom", "ilm", "ilam", "padhna", "padhein", "parhna", "parhne", "imtihan", "zehn", "memory", "exam", "exams", "hafiza",
            "حافظہ", "امتحان", "علم", "پڑھنا"
        )
        val salahWords = listOf(
            "salah", "namaz", "prayer", "mosque", "wudu", "masjid", "wudhu", "wazo", "tahajjud",
            "نماز", "مسجد", "وضو"
        )
        val travelWords = listOf(
            "travel", "journey", "trip", "car", "fly", "plane", "safar", "musafir",
            "سفر", "مسافر"
        )

        // Check full lowercase query contains keywords
        if (distressWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Distress")
        if (morningWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Morning")
        if (sleepWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Sleep")
        if (foodWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Food")
        if (knowledgeWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Knowledge")
        if (salahWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Salah")
        if (travelWords.any { lowercaseQuery.contains(it) }) matchedCategories.add("Travel")

        // Explicit Recitation, Wazifa, Dhikr context triggering
        val recitationKeywords = listOf("recite", "recitation", "parhna", "parhne", "read", "dua", "duas", "suggest", "wazifa", "vazifa", "zikr", "dhikr", "tilawat", "tasbih", "tasbeeh", "azkar", "wazaif", "وظیفہ", "تسبیح", "ذکر", "تلاوت", "دعا", "پڑھیں", "پڑھنے", "وظائف")
        val isRecitationRequested = recitationKeywords.any { lowercaseQuery.contains(it) }
        
        if (isRecitationRequested) {
            // Recitation request is broad; matching Distress, Knowledge, Quran and Salah gives the most beautiful duas
            matchedCategories.add("Distress")
            matchedCategories.add("Knowledge")
            matchedCategories.add("Quran")
            matchedCategories.add("Salah")
        }

        // 1. Search Duas
        val allDuas = com.example.data.IslamicData.duas
        var count = 0
        allDuas.forEach { dua ->
            var isMatch = matchedCategories.contains(dua.category)
            if (!isMatch) {
                for (token in tokens) {
                    if (token.length > 2 && (
                        dua.translation.lowercase().contains(token) || 
                        dua.translationUrdu.lowercase().contains(token) || 
                        dua.category.lowercase().contains(token) ||
                        dua.transliteration.lowercase().contains(token) ||
                        dua.reference.lowercase().contains(token)
                    )) {
                        isMatch = true
                        break
                    }
                }
            }
            if (isMatch && count < 4) {
                count++
                matchedSourcesSummary.add("Dua of ${dua.reference} (${dua.category})")
                val record = """
                - TYPE: DUA | ID: ${dua.id} | CATEGORY: ${dua.category}
                  REFERENCE: ${dua.reference}
                  ARABIC: ${dua.arabic}
                  TRANSLITERATION: ${dua.transliteration}
                  TRANSLATION_EN: ${dua.translation}
                  TRANSLATION_UR: ${dua.translationUrdu}
                """.trimIndent()
                matchedContentsForContext.add(record)
            }
        }
        
        // 2. Search Hadiths
        val allHadiths = com.example.data.IslamicData.hadiths
        var hCount = 0
        allHadiths.forEach { hadith ->
            var isMatch = false
            for (token in tokens) {
                if (token.length > 2 && (
                    hadith.text.lowercase().contains(token) || 
                    hadith.chapter.lowercase().contains(token) || 
                    hadith.narrator.lowercase().contains(token) || 
                    hadith.translationUrdu.lowercase().contains(token) || 
                    hadith.source.lowercase().contains(token)
                )) {
                    isMatch = true
                    break
                }
            }
            if (isMatch && hCount < 2) {
                hCount++
                matchedSourcesSummary.add("Hadith: ${hadith.source}")
                val record = """
                - TYPE: HADITH | ID: ${hadith.id} | CHAPTER: ${hadith.chapter}
                  SOURCE: ${hadith.source} | NARRATOR: ${hadith.narrator}
                  ARABIC: ${hadith.arabic}
                  TRANSLITERATION: ${hadith.transliteration}
                  TEXT_EN: ${hadith.text}
                  TRANSLATION_UR: ${hadith.translationUrdu}
                """.trimIndent()
                matchedContentsForContext.add(record)
            }
        }
        
        // 3. Search Surah names
        val allSurahs = com.example.data.IslamicData.surahs
        var sCount = 0
        allSurahs.forEach { surah ->
            val isMatch = com.example.data.IslamicData.matchesSurah(surah, lowercaseQuery)
            if (isMatch && sCount < 2) {
                sCount++
                matchedSourcesSummary.add("Surah ${surah.id} (${surah.name})")
                val record = """
                - TYPE: SURAH | SURAH_ID: ${surah.id}
                  NAME: ${surah.name} | ARABIC_NAME: ${surah.nameArabic}
                  VERSES: ${surah.versesCount} | REVELATION_PLACE: ${surah.revelationType}
                """.trimIndent()
                matchedContentsForContext.add(record)
            }
        }
        
        // 4. Search Names of Allah
        val namesOfAllah = com.example.data.IslamicData.namesOfAllah
        var nCount = 0
        namesOfAllah.forEach { name ->
            var isMatch = false
            for (token in tokens) {
                if (token.length > 2 && (
                    name.englishName.lowercase().contains(token) || 
                    name.meaning.lowercase().contains(token) || 
                    name.name.lowercase().contains(token)
                )) {
                    isMatch = true
                    break
                }
            }
            if (isMatch && nCount < 3) {
                nCount++
                matchedSourcesSummary.add("Name of Allah: ${name.englishName}")
                val record = """
                - TYPE: NAME_OF_ALLAH | INDEX: ${name.id}
                  ARABIC: ${name.name} | TRANSLITERATION: ${name.englishName}
                  MEANING: ${name.meaning}
                """.trimIndent()
                matchedContentsForContext.add(record)
            }
        }
        
        // Ensure that we always have abundant authentic content loaded if the matched lists are small or empty,
        // specifically when user is asking for general suggestions or recitations.
        if (matchedContentsForContext.size < 2 || isRecitationRequested) {
            // Keep adding comforting Quran/Dua fallbacks
            if (!matchedSourcesSummary.any { it.contains("quran_3") }) {
                matchedSourcesSummary.add("Dua of Quran 2:201")
                matchedContentsForContext.add("""
                    - TYPE: DUA | ID: quran_3 | CATEGORY: Quran
                      REFERENCE: Quran 2:201
                      ARABIC: رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ
                      TRANSLITERATION: Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar
                      TRANSLATION_EN: Our Lord, give us in this world [that which is] good and in the Hereafter [that which is] good and protect us from the punishment of the Fire.
                      TRANSLATION_UR: اے ہمارے رب! ہمیں دنیا میں بھی بھلائی عطا فرما اور آخرت میں بھی بھلائی عطا فرما اور ہمیں عذابِ قبر (دوزخ) سے بچا۔
                """.trimIndent())
            }
            if (!matchedSourcesSummary.any { it.contains("hadith_8") }) {
                matchedSourcesSummary.add("Dua of Quran 21:87")
                matchedContentsForContext.add("""
                    - TYPE: DUA | ID: hadith_8 | CATEGORY: Distress
                      REFERENCE: Quran 21:87 (Prophet Yunus's Prayer in Distress)
                      ARABIC: لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ
                      TRANSLITERATION: La ilaha illa Anta subhanaka inni kuntu minaz-zalimin
                      TRANSLATION_EN: There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.
                      TRANSLATION_UR: تیرے سوا کوئی معبود نہیں، تو پاک ہے، بے شک میں ہی قصورواروں میں سے تھا۔
                """.trimIndent())
            }
            if (!matchedSourcesSummary.any { it.contains("hadith_5") }) {
                matchedSourcesSummary.add("Dua of Quran 20:114")
                matchedContentsForContext.add("""
                    - TYPE: DUA | ID: hadith_5 | CATEGORY: Knowledge
                      REFERENCE: Quran 20:114
                      ARABIC: رَّبِّ زِدْنِي عِلْمًا
                      TRANSLITERATION: Rabbi zidni 'ilman
                      TRANSLATION_EN: My Lord, increase me in knowledge.
                      TRANSLATION_UR: اے میرے رب! میرے علم میں اضافہ فرما۔
                """.trimIndent())
            }
            if (matchedSourcesSummary.size < 4) {
                matchedSourcesSummary.add("Surah 1 (Al-Fatihah)")
                matchedContentsForContext.add("""
                    - TYPE: SURAH | SURAH_ID: 1
                      NAME: Al-Fatihah | ARABIC_NAME: الفاتحة
                      VERSES: 7 | REVELATION_PLACE: Mecca
                      DESCRIPTION: The Opening - the foundational, greatest Surah of the Holy Quran, recited in every unit of prayer, seeking guidance, healing (Shifa), and divine light.
                """.trimIndent())
            }
        }
        
        return Pair(matchedSourcesSummary, matchedContentsForContext)
    }

    private fun loadChatMemory() {
        loadLearnedProfile()
        val prefs = getSecurePrefs()
        queryCount = prefs.getInt("ai_query_count", 0)
        isChatLocked = prefs.getBoolean("ai_is_chat_locked", false)
        aiLockEndTime = prefs.getLong("ai_lock_end_time", 0L)

        val chatJson = prefs.getString("ai_chat_messages", null)
        if (chatJson != null) {
            try {
                val adapter = localMoshi.adapter<List<ChatMessage>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatMessage::class.java)
                )
                chatMessages = adapter.fromJson(chatJson) ?: emptyList()
            } catch(e: Exception) {}
        }

        val nowMillis = System.currentTimeMillis()
        if (aiLockEndTime > 0 && nowMillis >= aiLockEndTime) {
            isChatLocked = false
            queryCount = 0
            aiLockEndTime = getNextMidnightMillis()
            saveChatState()
        } else if (aiLockEndTime == 0L) {
            aiLockEndTime = getNextMidnightMillis()
            saveChatState()
        }
        
        if (isChatLocked) {
            startLockTimeRemainingCountdown(aiLockEndTime)
        }
    }

    private fun saveChatState(lockEndTime: Long = 0L) {
        if (lockEndTime > 0L) {
            aiLockEndTime = lockEndTime
        }

        // --- DUAL-PRUNING OPTIMIZATION ---
        // 1. Maintain a clean history thread ceiling to prevent database size bloat (max 24 messages)
        val maxMessagesLimit = 24
        var prunedMessages = chatMessages
        if (prunedMessages.size > maxMessagesLimit) {
            prunedMessages = prunedMessages.takeLast(maxMessagesLimit)
        }

        // 2. Clear extremely heavy raw source citation structures for older messages (more than 4 entries back),
        // keeping only the recent ones. This leaves the conversational text and brief headers intact
        // but reduces the payload size by over 97%!
        prunedMessages = prunedMessages.mapIndexed { idx, msg ->
            if (idx < prunedMessages.size - 4) {
                msg.copy(citationFullTexts = emptyList())
            } else {
                msg
            }
        }
        
        chatMessages = prunedMessages

        val prefs = getSecurePrefs()
        val editor = prefs.edit()
        
        editor.putInt("ai_query_count", queryCount)
        editor.putBoolean("ai_is_chat_locked", isChatLocked)
        editor.putLong("ai_lock_end_time", aiLockEndTime)
        
        try {
            val adapter = localMoshi.adapter<List<ChatMessage>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatMessage::class.java)
            )
            editor.putString("ai_chat_messages", adapter.toJson(chatMessages))
        } catch(e: Exception) {}
        editor.apply()

        // Sync with Firebase Firestore so changes survive clearing data, logging out, etc.
        markLocalUpdateAndSync()
    }

    private val TAQWA_APP_CONTEXT = """
You are 'TaqwaHub AI', an intelligent, respectful, and articulate Islamic spiritual companion created by Malik Usman.

Core Guidelines:
1. Identity & Developer: You are TaqwaHub AI, developed by Malik Usman. If asked who created you, say: "I was developed by Malik Usman." If asked about your underlying AI model or technical code, simply state that you do not know.
2. Scope & Focus (STRICT): You are strictly an Islamic companion. Your knowledge covers Islam, Quran, Sunnah, Hadith, Islamic history, Anbiya, Sahaba, Duas, and Islamic jurisprudence. If a user asks non-Islamic general secular questions (such as general crypto, coding, or secular trivia), politely state: "My focus is strictly on Islamic knowledge and spiritual guidance." (Or explain the topic strictly from an Islamic perspective where relevant, e.g. Islamic ruling on cryptocurrency).
3. Tone & Character: Warm, polite, scholarly, and natural.
4. Grammar & Natural Phrasing (CRITICAL):
   - Speak with grammatically correct, natural, conversational flow. Avoid awkward word-for-word literal translations from English.
   - Standard greetings (e.g. "kese ho", "hello"):
     * Urdu Script: "وعلیکم السلام! الحمدللہ، میں بالکل ٹھیک ہوں۔ آپ سنائیں، میں آپ کی کیا مدد کر سکتا ہوں؟"
     * Roman Urdu: "Walaikum Assalam! Alhamdulillah, main bilkul theek hoon. Aap sunayein, main aap ki kya madad kar sakta hoon?"
     * English: "Walaikum Assalam! Alhamdulillah, I am doing well. How can I assist you today?"
5. Vocabulary & Language:
   - In Urdu or Roman Urdu, use pure Urdu vocabulary. Strictly avoid Hindi terms (such as "پریوار", "چنتا", "شانتی", "پرارتھنا", "سمسیا", "وشواس").
6. Formatting: Do not use Markdown symbols like **, *, #, or backticks. Output plain, clean text. Do not use emojis.
"""

    private fun detectLanguageOfQuery(query: String): String {
        val trimmed = query.trim()
        val lowercaseQuery = trimmed.lowercase()
        
        // Explicit requests first
        if (lowercaseQuery.contains("roman urdu") || lowercaseQuery.contains("roman to") || lowercaseQuery.contains("roman me") || lowercaseQuery.contains("roman mein") || lowercaseQuery.contains("roman main")) {
            return "romanurdu"
        }
        if (lowercaseQuery.contains("urdu script") || lowercaseQuery.contains("urdu rasm") || lowercaseQuery.contains("urdu me likh") || lowercaseQuery.contains("urdu mein likh") || lowercaseQuery.contains("urdu font")) {
            return "urduscript"
        }
        if (lowercaseQuery.contains("in english") || lowercaseQuery.contains("english style") || lowercaseQuery.contains("english translator")) {
            return "english"
        }

        // Check if contains Urdu/Arabic characters
        val containsUrduScript = trimmed.any { it in '\u0600'..'\u06FF' }
        if (containsUrduScript) {
            return "urduscript"
        }

        // Convert the string to smooth lowercase alphabetic tokens
        val tokens = lowercaseQuery.replace(Regex("[^a-z ]"), "").split(" ").filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return "english"

        val romanUrduUniqueWords = setOf(
            "hai", "hain", "kya", "kese", "kaise", "ho", "ko", "mein", "me", "se", "ki", "ka", "ke", "bhi", "toh", "to", 
            "aur", "ya", "kuch", "kuchh", "parhna", "parhne", "wazifa", "wazaif", "mujhe", "mera", "meri", "mere", "tum", 
            "aap", "ap", "kahan", "kyun", "kyon", "liye", "leeye", "kar", "karein", "karna", "karne", "raha", "rahe", 
            "rahi", "rha", "rhe", "rhi", "chahiye", "batayein", "hoga", "hogi", "hoge", "nahi", "nahin", "karo", "guzar", 
            "parha", "samajh", "chal", "bol", "taur", "tarah", "gaya", "gaye", "gayi", "islaam", "yaad", "likha", 
            "dikhao", "likhein", "parhein", "batao", "bata", "bhai", "shuru", "kijiye", "kam", "lo", "karlo", "karliya",
            "nahin", "naa", "mat", "dost", "ranj", "gham", "pareshani", "pareshan", "mushkil", "mushkilat", "dua", "duae", "duayein"
        )

        val englishUniqueWords = setOf(
            "the", "is", "are", "am", "you", "your", "what", "where", "when", "why", "how", "who", "which", "please", 
            "should", "can", "have", "has", "had", "would", "will", "about", "with", "from", "this", "that", "these", 
            "those", "some", "many", "any", "under", "above", "tell", "give", "suggest", "read", "recite", "recitation", 
            "prayer", "prayers", "god", "lord"
        )

        var romanCount = 0
        var englishCount = 0

        for (token in tokens) {
            if (romanUrduUniqueWords.contains(token)) {
                romanCount++
            }
            if (englishUniqueWords.contains(token)) {
                englishCount++
            }
        }

        return if (romanCount > 0 && romanCount >= englishCount) {
            "romanurdu"
        } else {
            "english"
        }
    }

    private fun detectAndSaveUserInsights(query: String) {
        val trimmed = query.trim()
        val lowercaseQuery = trimmed.lowercase()

        // 1. Detect User Name
        val nameRegexes = listOf(
            Regex("(?:mera naam|my name is|i am|mujhe|mujhse|main hoon|im|i'm)\\s+([a-zA-Z\\u0600-\\u06FF]{3,15})", RegexOption.IGNORE_CASE),
            Regex("([a-zA-Z\\u0600-\\u06FF]{3,15})\\s+(?:bol raha hoon|bol rha hu|bol raha hu)", RegexOption.IGNORE_CASE)
        )
        var foundName = ""
        for (regex in nameRegexes) {
            val match = regex.find(trimmed)
            if (match != null) {
                val candidate = match.groupValues[1]
                val stopWords = setOf("naam", "name", "mera", "meri", "mere", "main", "hoon", "bold", "raha", "baat", "kya", "kese", "kaise", "wazifa", "karo", "karna", "nahi")
                if (!stopWords.contains(candidate.lowercase())) {
                    foundName = candidate
                    break
                }
            }
        }
        if (foundName.isNotEmpty()) {
            learnedUserName = foundName
        }

        // 2. Detect User Concern / Stress
        val isGreeting = listOf("kese ho", "kaise ho", "kaise hain", "assalamu alaikum", "assalam o alaikum", "hi", "hello", "kya haal hai", "kese hain").any { lowercaseQuery.contains(it) }
        if (!isGreeting) {
            val concernWords = mapOf(
                "livelihood" to listOf("job", "naukri", "rizq", "paisa", "pese", "money", "karobar", "business", "gareebi", "financial", "wealth", "rozgar"),
                "academic" to listOf("exam", "exams", "study", "paper", "parhai", "padhai", "imtihan", "fail", "result", "grade", "test", "school", "college"),
                "health" to listOf("sick", "ill", "bimari", "pain", "dard", "cure", "shifa", "hospital", "health", "sehat", "bimar"),
                "distress/anxiety" to listOf("anxiety", "anxious", "sad", "depression", "grief", "stress", "tension", "pareshani", "pareshan", "gham", "dar", "fear", "scared"),
                "marriage/family" to listOf("shadi", "shaadi", "marriage", "family", "walid", "ammi", "abbu", "bhai", "behen", "bache", "biwi", "shohar", "husband", "wife")
            )
            for ((concern, keywords) in concernWords) {
                if (keywords.any { lowercaseQuery.contains(it) }) {
                    learnedPrimaryConcern = concern
                    break
                }
            }
        }

        // 3. Detect custom verified spiritual facts they mention
        if (trimmed.length in 15..120) {
            val isCustomKnowledge = lowercaseQuery.contains("suna hai") || 
                                    lowercaseQuery.contains("fav") || 
                                    lowercaseQuery.contains("pasand") || 
                                    lowercaseQuery.contains("mujhe pata hai") ||
                                    lowercaseQuery.contains("parhta hoon") ||
                                    lowercaseQuery.contains("karta hoon") ||
                                    lowercaseQuery.contains("parta hu") ||
                                    lowercaseQuery.contains("karta hu") ||
                                    lowercaseQuery.contains("reminds me") ||
                                    lowercaseQuery.contains("meaning is")
            if (isCustomKnowledge) {
                val updatedKnowledge = learnedCustomKnowledge.toMutableList()
                if (!updatedKnowledge.contains(trimmed) && updatedKnowledge.size < 5) {
                    updatedKnowledge.add(trimmed)
                    learnedCustomKnowledge = updatedKnowledge
                }
            }
        }

        // 4. Detect Preferred style
        if (lowercaseQuery.contains("short") || lowercaseQuery.contains("chota") || lowercaseQuery.contains("mukhtasar") || lowercaseQuery.contains("shortly")) {
            learnedTalkingStyle = "extremely short and precise"
        } else if (lowercaseQuery.contains("detail") || lowercaseQuery.contains("tafseel") || lowercaseQuery.contains("deep")) {
            learnedTalkingStyle = "detailed yet concise"
        }

        saveLearnedProfile()
    }

    private fun loadLearnedProfile() {
        val prefs = getSecurePrefs()
        learnedUserName = prefs.getString("learned_user_name", "") ?: ""
        learnedTalkingStyle = prefs.getString("learned_talking_style", "") ?: ""
        learnedPrimaryConcern = prefs.getString("learned_primary_concern", "") ?: ""
        val customKnowledgeJson = prefs.getString("learned_custom_knowledge", null)
        if (customKnowledgeJson != null) {
            try {
                val adapter = localMoshi.adapter<List<String>>(
                    com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                )
                learnedCustomKnowledge = adapter.fromJson(customKnowledgeJson) ?: emptyList()
            } catch (e: Exception) {}
        }
    }

    private fun saveLearnedProfile() {
        val prefs = getSecurePrefs()
        val editor = prefs.edit()
        editor.putString("learned_user_name", learnedUserName)
        editor.putString("learned_talking_style", learnedTalkingStyle)
        editor.putString("learned_primary_concern", learnedPrimaryConcern)
        try {
            val adapter = localMoshi.adapter<List<String>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
            )
            editor.putString("learned_custom_knowledge", adapter.toJson(learnedCustomKnowledge))
        } catch (e: Exception) {}
        editor.apply()
    }

    fun sendMessage(query: String) {
        if (query.trim().isEmpty() || isChatLoading || isAiStreaming || (isChatLocked && !isAdmin)) return

        detectAndSaveUserInsights(query)

        if (isSecurityStrictEnvBlockEnabled) {
            val isRooted = rootCheckResult?.isRooted ?: false
            val isEmulator = emulatorCheckResult?.isEmulator ?: false
            if (isRooted || isEmulator) {
                val userMsg = ChatMessage("user", query)
                val errMsg = "⚠️ Security Shield Status: Blocked. TaqwaHub AI features are locked on rooted or simulated device configurations because 'Strict Environment Guard' is active on your profile settings."
                chatMessages = chatMessages + userMsg + ChatMessage("model", errMsg)
                return
            }
        }

        val nowMillis = System.currentTimeMillis()
        if (aiLockEndTime > 0 && nowMillis >= aiLockEndTime) {
            isChatLocked = false
            queryCount = 0
            aiLockEndTime = getNextMidnightMillis()
            saveChatState()
        }

        if (!isAdmin && queryCount >= 10) {
            isChatLocked = true
            startLockTimeRemainingCountdown(aiLockEndTime)
            return
        }

        val userMsg = ChatMessage("user", query)
        chatMessages = chatMessages + userMsg
        isChatLoading = true

        // Form history context for API call
        val historyContents = chatMessages.map {
            GeminiContent(
                role = if (it.role == "user") "user" else "model",
                parts = listOf(GeminiPart(it.text))
            )
        }

        // Live thoughts generation & Search offline data
        chatCurrentStepText = "Searching Quran Database..."
        chatCurrentProgress = 0.05f

        viewModelScope.launch {
            val (sourcesBrief, contentsFull) = performSpiritualIndexSearch(query)
            
            // Smoothly animate searching Quran database
            for (p in 6..35) {
                delay(12)
                chatCurrentProgress = p / 100f
            }
            
            chatCurrentStepText = "Retrieving Hadith..."
            for (p in 36..68) {
                delay(15)
                chatCurrentProgress = p / 100f
            }
            
            chatCurrentStepText = "Synthesizing response..."
            for (p in 69..92) {
                delay(12)
                chatCurrentProgress = p / 100f
            }

            val additionalContextText = if (contentsFull.isNotEmpty()) {
                """
                
                **CRITICAL OFFLINE SPIRITUAL CONTEXT**
                The user's query matches the following official spiritual knowledge from the Quran, Hadith, and Duas database.
                You should use this context, spiritual insights, Arabic, and Urdu translations to offer deep, comprehensive, and beautiful answers.
                
                **STRICT RULE: REMOVE ALL REFERENCE FOOTERS & CITATION LABELS**
                You are STRICTLY FORBIDDEN from printing any "Reference Sources", "References", "App Reference", or source footers/sections at the end of your response, nor should you output explicit reference citations, resource codes, or footnotes. Simply integrate the wisdom, translations, and insights naturally and purely as conversational text. Do not cite the sources or display lists of references at the bottom.
                
                RETRIEVED RECORDS:
                ${contentsFull.joinToString("\n\n")}
                """
            } else ""

            val currentNetwork = networkStatus.value
            if (currentNetwork.type == TaqwaNetworkType.NONE || currentNetwork.type == TaqwaNetworkType.AIRPLANE) {
                // Smoothly clear thinking animation and present offline message
                chatCurrentStepText = "Connection lost..."
                for (p in 93..100) {
                    delay(10)
                    chatCurrentProgress = p / 100f
                }
                isChatLoading = false
                isAiStreaming = true
                
                val placeholderMsg = ChatMessage(
                    role = "model",
                    text = "●"
                )
                chatMessages = chatMessages + placeholderMsg
                
                val offlineFriendlyMsg = "It seems you are currently offline. TaqwaHub AI requires an active internet connection to synthesize responses from our spiritual databases. Please check your connectivity and try again when you are back online.\n\nایسا لگتا ہے کہ آپ اس وقت آف لائن ہیں۔ تقویٰ ہب اے آئی کو جوابات تیار کرنے کے لیے ایک فعال انٹرنیٹ کنکشن کی ضرورت ہے۔ براہ کرم اپنا انٹرنیٹ کنکشن چیک کریں اور آن لائن ہونے پر دوبارہ کوشش کریں۔"
                val offlineWords = offlineFriendlyMsg.split(" ")
                var currentTypedOffline = ""
                for ((index, word) in offlineWords.withIndex()) {
                    currentTypedOffline += (if (index == 0) "" else " ") + word
                    chatMessages = chatMessages.toMutableList().apply {
                        val lastMsgIdx = size - 1
                        if (lastMsgIdx >= 0) {
                            set(lastMsgIdx, get(lastMsgIdx).copy(text = "$currentTypedOffline ▌"))
                        }
                    }
                    delay(if (word.endsWith(".") || word.endsWith(",") || word.endsWith("?") || word.endsWith("!")) 140L else 30L)
                }
                
                chatMessages = chatMessages.toMutableList().apply {
                    val lastMsgIdx = size - 1
                    if (lastMsgIdx >= 0) {
                        set(lastMsgIdx, get(lastMsgIdx).copy(text = offlineFriendlyMsg))
                    }
                }
                isAiStreaming = false
                saveChatState()
                return@launch
            }

            var responseText = ""
            var hasThrownException = false
            try {
                val learnerInstructions = buildString {
                    if (learnedUserName.isNotEmpty()) {
                        append("User Name: $learnedUserName. ")
                    }
                    if (learnedTalkingStyle.isNotEmpty()) {
                        append("User Preference: $learnedTalkingStyle. ")
                    }
                }

                val detectedLanguage = detectLanguageOfQuery(query)
                val languageInstruction = when (detectedLanguage) {
                    "urduscript" -> "Reply strictly in clean, beautiful Urdu Script (اردو نستعلیق). Use standard, authentic Urdu vocabulary ( خاندَان, اہل بیت, مسئلہ, یقین, دعا, سکون ). Do not use Hindi words."
                    "romanurdu" -> "Reply strictly in natural, clean Roman Urdu (Latin script). Do not use Hindi words."
                    else -> "Reply in clean, fluent, natural, professional English."
                }

                responseText = repository.queryGeminiAI(
                    query = query,
                    history = historyContents,
                    systemInstructionText = TAQWA_APP_CONTEXT + "\n" + languageInstruction + "\n" + learnerInstructions + "\n" + additionalContextText
                )
            } catch (e: Exception) {
                e.printStackTrace()
                hasThrownException = true
            }

            if (hasThrownException || responseText.isEmpty() || responseText.startsWith("Divine connectivity") || responseText.contains("Please configure your custom Groq/OpenRouter keys") || responseText.contains("Code -1")) {
                responseText = "Our spiritual knowledge synthesis engines are currently encountering a brief service connection issue or experiencing heavy traffic. Please rest assured we are resolving this. In the meantime, you can continue exploring the Quran, Hadith, and Duas database completely offline on TaqwaHub!\n\nہمارے سرورز پر اس وقت زیادہ رش یا عارضی خرابی ہے۔ یقین رکھیں کہ ہم اسے جلد ٹھیک کرنے کی کوشش کر رہے ہیں۔ اس دوران، آپ تقویٰ ہب پر قرآن، حدیث اور دعاؤں کے آف لائن ڈیٹا بیس کو استعمال کرنا جاری رکھ سکتے ہیں۔"
            }

            chatCurrentStepText = "Completing response..."
            for (p in 93..100) {
                delay(8)
                chatCurrentProgress = p / 100f
            }

            // Immediately clear the searching/thinking indicator
            isChatLoading = false

            // Set streaming mode on
            isAiStreaming = true

            // Add placeholder model bubble
            val placeholderMsg = ChatMessage(
                role = "model",
                text = "",
                citations = sourcesBrief,
                citationFullTexts = contentsFull
            )
            chatMessages = chatMessages + placeholderMsg

            // Smooth chunked streaming animation (prevents Nastaliq font reflow glitches)
            val words = responseText.split(" ")
            var currentTypedText = ""
            var chunkBuffer = ""

            for ((index, word) in words.withIndex()) {
                chunkBuffer += (if (chunkBuffer.isEmpty()) "" else " ") + word
                val isPunctuation = word.endsWith(".") || word.endsWith("،") || word.endsWith("؟") || word.endsWith("!") || word.endsWith("\n")
                if (isPunctuation || chunkBuffer.length >= 12 || index == words.size - 1) {
                    currentTypedText += (if (currentTypedText.isEmpty()) "" else " ") + chunkBuffer
                    chunkBuffer = ""
                    chatMessages = chatMessages.toMutableList().apply {
                        val lastMsgIdx = size - 1
                        if (lastMsgIdx >= 0) {
                            set(lastMsgIdx, get(lastMsgIdx).copy(text = currentTypedText))
                        }
                    }
                    delay(if (isPunctuation) 60L else 20L)
                }
            }

            // Lock in final response
            chatMessages = chatMessages.toMutableList().apply {
                val lastMsgIdx = size - 1
                if (lastMsgIdx >= 0) {
                    set(lastMsgIdx, get(lastMsgIdx).copy(text = responseText))
                }
            }
            isAiStreaming = false

            if (!responseText.startsWith("Divine connectivity")) {
                if (!isAdmin) {
                    queryCount++
                }
            }
            
            if (!isAdmin && queryCount >= 10) {
                isChatLocked = true
                startLockTimeRemainingCountdown(aiLockEndTime)
            }
            saveChatState()
        }
    }

    private var countdownJob: kotlinx.coroutines.Job? = null

    private fun startLockTimeRemainingCountdown(endTimeMillis: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val nowMillis = System.currentTimeMillis()
                val diff = endTimeMillis - nowMillis
                
                if (diff > 0) {
                    val h = (diff / (1000 * 60 * 60)) % 24
                    val m = (diff / (1000 * 60)) % 60
                    val s = (diff / 1000) % 60
                    
                    val formatted = String.format("%02dh %02dm %02ds", h, m, s)
                    resetTimeRemaining = formatted
                } else {
                    resetTimeRemaining = "00h 00m 00s"
                    isChatLocked = false
                    queryCount = 0
                    aiLockEndTime = 0L
                    saveChatState()
                    break
                }
                delay(1000)
            }
        }
    }

    // Zakat Calculator
    var zakatMetalBasis by mutableStateOf("")  // "silver", "gold" or ""
    var zakatCurrencySymbol by mutableStateOf("₨")
    var zakatCurrencyCode by mutableStateOf("PKR")
    var goldGramRate by mutableStateOf(21500.0) // fallback average rates
    var silverGramRate by mutableStateOf(260.0)
    
    var baseGoldUsdGramRate: Double? = null
    var baseSilverUsdGramRate: Double? = null
    
    var currencyRates = mutableMapOf<String, Double>("USD" to 1.0, "PKR" to 278.0, "SAR" to 3.75, "AED" to 3.67, "TRY" to 32.0, "IDR" to 16000.0)

    val currencyOptions = listOf(
        Pair("PKR", "₨"),
        Pair("USD", "$"),
        Pair("SAR", "SR"),
        Pair("AED", "Dh"),
        Pair("TRY", "₺"),
        Pair("IDR", "Rp")
    )

    fun selectZakatCurrency(index: Int) {
        val curr = currencyOptions[index]
        zakatCurrencyCode = curr.first
        zakatCurrencySymbol = curr.second
        
        // Reapply multiplier instantly based on new currency
        val multiplier = currencyRates[zakatCurrencyCode] ?: 1.0
        baseGoldUsdGramRate?.let { goldGramRate = it * multiplier }
        baseSilverUsdGramRate?.let { silverGramRate = it * multiplier }
        updateRateInputs()
    }

    var cashInput by mutableStateOf("")
    var goldInput by mutableStateOf("")
    var silverInput by mutableStateOf("")
    var investmentsInput by mutableStateOf("")
    var businessInput by mutableStateOf("")
    var debtsInput by mutableStateOf("")

    var goldTolaRateInput by mutableStateOf("")
    var silverTolaRateInput by mutableStateOf("")

    // Computed properties for Tola rates in the selected currency
    fun currentGoldTolaRate(): Double = goldTolaRateInput.toDoubleOrNull() ?: (goldGramRate * 11.6638)
    fun currentSilverTolaRate(): Double = silverTolaRateInput.toDoubleOrNull() ?: (silverGramRate * 11.6638)
    
    private fun updateRateInputs() {
        goldTolaRateInput = (goldGramRate * 11.6638).toLong().toString()
        silverTolaRateInput = (silverGramRate * 11.6638).toLong().toString()
    }

    fun calculateZakat(): ZakatResults {
        val cash = cashInput.toDoubleOrNull() ?: 0.0
        val goldTolas = goldInput.toDoubleOrNull() ?: 0.0
        val silverTolas = silverInput.toDoubleOrNull() ?: 0.0
        
        val goldValueInCurrency = goldTolas * currentGoldTolaRate()
        val silverValueInCurrency = silverTolas * currentSilverTolaRate()
        
        val investments = investmentsInput.toDoubleOrNull() ?: 0.0
        val business = businessInput.toDoubleOrNull() ?: 0.0
        val debts = debtsInput.toDoubleOrNull() ?: 0.0

        val nisabLimit = if (zakatMetalBasis == "silver") {
            52.5 * currentSilverTolaRate()
        } else {
            7.5 * currentGoldTolaRate()
        }

        val totalAssets = cash + goldValueInCurrency + silverValueInCurrency + investments + business
        val netWorth = kotlin.math.max(0.0, totalAssets - debts)
        val exceedsNisab = netWorth >= nisabLimit
        val zakatPayable = if (exceedsNisab) netWorth * 0.025 else 0.0

        return ZakatResults(
            totalAssets = totalAssets,
            netWorth = netWorth,
            nisabLimit = nisabLimit,
            exceedsNisab = exceedsNisab,
            zakatPayable = zakatPayable,
            progressPercent = if (nisabLimit > 0) kotlin.math.min(100.0, (netWorth / nisabLimit) * 100.0).toInt() else 0
        )
    }

    data class ZakatResults(
        val totalAssets: Double,
        val netWorth: Double,
        val nisabLimit: Double,
        val exceedsNisab: Boolean,
        val zakatPayable: Double,
        val progressPercent: Int
    )

    // Qibla heading manual simulator
    var manualQiblaSliderHeading by mutableStateOf(0.0)

    // Hijri month detail overlay
    var selectedHijriMonthDetails by mutableStateOf<String?>(null)
    
    // Fetch live market data (Yahoo Finance + Exchangerates API)
    private suspend fun fetchLiveRates() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Fetch Base Exchange Rates
                val exchangeJson = java.net.URL("https://open.er-api.com/v6/latest/USD").readText()
                val exchangeObject = org.json.JSONObject(exchangeJson)
                if (exchangeObject.getString("result") == "success") {
                    val ratesObj = exchangeObject.getJSONObject("rates")
                    val updatedRates = mutableMapOf<String, Double>()
                    listOf("USD", "PKR", "SAR", "AED", "TRY", "IDR").forEach {
                        updatedRates[it] = ratesObj.optDouble(it, currencyRates[it] ?: 1.0)
                    }
                    currencyRates = updatedRates
                }

                // Parse Gold Futures Price (USD/Oz) // Spot rate is more accurate
                val goldConn = java.net.URL("https://query1.finance.yahoo.com/v8/finance/chart/XAUUSD=X").openConnection() as java.net.HttpURLConnection
                goldConn.setRequestProperty("User-Agent", "Mozilla/5.0")
                val goldJson = goldConn.inputStream.bufferedReader().use { it.readText() }
                val goldUsdOz = org.json.JSONObject(goldJson)
                    .getJSONObject("chart").getJSONArray("result")
                    .getJSONObject(0).getJSONObject("meta")
                    .getDouble("regularMarketPrice")

                // Parse Silver Futures Price (USD/Oz) // Spot rate is more accurate
                val silverConn = java.net.URL("https://query1.finance.yahoo.com/v8/finance/chart/XAGUSD=X").openConnection() as java.net.HttpURLConnection
                silverConn.setRequestProperty("User-Agent", "Mozilla/5.0")
                val silverJson = silverConn.inputStream.bufferedReader().use { it.readText() }
                val silverUsdOz = org.json.JSONObject(silverJson)
                    .getJSONObject("chart").getJSONArray("result")
                    .getJSONObject(0).getJSONObject("meta")
                    .getDouble("regularMarketPrice")

                // 1 Troy Oz = 31.1034768 Grams
                val baseGoldGramUsd = goldUsdOz / 31.1034768
                val baseSilverGramUsd = silverUsdOz / 31.1034768

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    baseGoldUsdGramRate = baseGoldGramUsd
                    baseSilverUsdGramRate = baseSilverGramUsd
                    
                    val multiplier = currencyRates[zakatCurrencyCode] ?: 1.0
                    goldGramRate = baseGoldGramUsd * multiplier
                    silverGramRate = baseSilverGramUsd * multiplier
                    updateRateInputs()
                }
            } catch (e: Exception) {
                // Ignore API failures and preserve fallback rates
            }
        }
    }

    fun listenToAudioOverrides() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("audio_overrides")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("TaqwaViewModel", "Listen failed for audio_overrides.", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val newOverrides = mutableMapOf<String, String>()
                            for (doc in snapshot.documents) {
                                val url = doc.getString("url")
                                if (url != null) {
                                    newOverrides[doc.id] = url
                                }
                            }
                            audioOverrides = newOverrides
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error setting up audio overrides listener", e)
            }
        }
    }

    fun saveAudioOverride(id: String, url: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                if (url.isBlank()) {
                    db.collection("audio_overrides").document(id).delete()
                        .addOnSuccessListener {
                            viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                        }
                        .addOnFailureListener { e ->
                            viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Failed to delete") }
                        }
                } else {
                    db.collection("audio_overrides").document(id)
                        .set(hashMapOf("url" to url))
                        .addOnSuccessListener {
                            viewModelScope.launch(Dispatchers.Main) { onSuccess() }
                        }
                        .addOnFailureListener { e ->
                            viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Failed to save") }
                        }
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) { onFailure(e.message ?: "Unknown error") }
            }
        }
    }

    fun listenToAppConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("TaqwaViewModel", "Registering Firestore app config listener...")
                val db = FirebaseFirestore.getInstance()
                
                // Clear previous registrations if any
                appConfigListenerRegistration?.remove()
                adminsListenerRegistration?.remove()

                appConfigListenerRegistration = db.collection("system").document("maintenance")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("TaqwaViewModel", "AppConfig snapshot listener failed: ${e.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val isMaint = snapshot.getBoolean("isUnderMaintenance") ?: false
                            val maintMsg = snapshot.getString("message") ?: "TaqwaHub is currently undergoing scheduled updates. Please stand by."
                            val aiChat = snapshot.getBoolean("enableAiChat") ?: false
                            val welcomeMsg = snapshot.getString("welcomeBannerMessage") ?: ""
                            val minVerRaw = snapshot.get("forceUpdateMinVersion")
                            val minVer = when (minVerRaw) {
                                is Number -> minVerRaw.toLong().toString()
                                is String -> minVerRaw
                                else -> "1.0.0"
                            }
                            val updateUrl = snapshot.getString("updateDownloadUrl") ?: ""
                            val isAudioLocked = snapshot.getBoolean("isQuranAudioLocked") ?: false
                            val quranAudioMsg = snapshot.getString("quranAudioBlockedMessage") ?: "Surah audio is currently blocked by administrator."
                            val lockedSurahIds = snapshot.getString("lockedSurahIds") ?: ""
                            val surahBlockedMsg = snapshot.getString("surahBlockedMessage") ?: "This Surah is completely blocked by administrator."
                            val lockedWordSurahIds = snapshot.getString("lockedWordSurahIds") ?: ""
                            val wordSurahMsg = snapshot.getString("wordSurahBlockedMessage") ?: "Word by Word audio is currently blocked by administrator."
                            
                            val isQuranPageLocked = snapshot.getBoolean("isQuranPageLocked") ?: false
                            val quranPageMsg = snapshot.getString("quranPageBlockedMessage") ?: "The Quran page is currently blocked."
                            val isToolsPageLocked = snapshot.getBoolean("isToolsPageLocked") ?: false
                            val toolsPageMsg = snapshot.getString("toolsPageBlockedMessage") ?: "The Tools page is currently blocked."
                            val isLearnPageLocked = snapshot.getBoolean("isLearnPageLocked") ?: false
                            val learnPageMsg = snapshot.getString("learnPageBlockedMessage") ?: "The Learn page is currently blocked."

                            // Module Matrix Properties
                            val isHadithLocked = snapshot.getBoolean("isHadithLocked") ?: false
                            val isHadithHidden = snapshot.getBoolean("isHadithHidden") ?: false
                            val hadithLockReason = snapshot.getString("hadithLockReason") ?: "Hadith Collection is temporarily undergoing maintenance to enhance accuracy."
                            val hadithLockCategory = snapshot.getString("hadithLockCategory") ?: "server_maintenance"

                            val isDuaLocked = snapshot.getBoolean("isDuaLocked") ?: false
                            val isDuaHidden = snapshot.getBoolean("isDuaHidden") ?: false
                            val duaLockReason = snapshot.getString("duaLockReason") ?: "Dua & Azkar section is temporarily undergoing maintenance."
                            val duaLockCategory = snapshot.getString("duaLockCategory") ?: "server_maintenance"

                            val isQuranLocked = snapshot.getBoolean("isQuranLocked") ?: false
                            val isQuranHidden = snapshot.getBoolean("isQuranHidden") ?: false
                            val quranLockReason = snapshot.getString("quranLockReason") ?: "Quran Reader is temporarily locked for content verification."
                            val quranLockCategory = snapshot.getString("quranLockCategory") ?: "server_maintenance"

                            val isLeaderboardLocked = snapshot.getBoolean("isLeaderboardLocked") ?: false
                            val isLeaderboardHidden = snapshot.getBoolean("isLeaderboardHidden") ?: false
                            val leaderboardLockReason = snapshot.getString("leaderboardLockReason") ?: "Global Leaderboard is undergoing scheduled score sync."
                            val leaderboardLockCategory = snapshot.getString("leaderboardLockCategory") ?: "server_maintenance"

                            val isTasksLocked = snapshot.getBoolean("isTasksLocked") ?: false
                            val isTasksHidden = snapshot.getBoolean("isTasksHidden") ?: false
                            val tasksLockReason = snapshot.getString("tasksLockReason") ?: "Task Tracker & Daily Challenges are temporarily offline."
                            val tasksLockCategory = snapshot.getString("tasksLockCategory") ?: "server_maintenance"

                            val isTasbeehLocked = snapshot.getBoolean("isTasbeehLocked") ?: false
                            val isTasbeehHidden = snapshot.getBoolean("isTasbeehHidden") ?: false
                            val tasbeehLockReason = snapshot.getString("tasbeehLockReason") ?: "Tasbeeh Counter is undergoing maintenance."
                            val tasbeehLockCategory = snapshot.getString("tasbeehLockCategory") ?: "server_maintenance"

                            val isNamesLocked = snapshot.getBoolean("isNamesLocked") ?: false
                            val isNamesHidden = snapshot.getBoolean("isNamesHidden") ?: false
                            val namesLockReason = snapshot.getString("namesLockReason") ?: "Names of Allah library is temporarily undergoing updates."
                            val namesLockCategory = snapshot.getString("namesLockCategory") ?: "server_maintenance"

                            val isZakatLocked = snapshot.getBoolean("isZakatLocked") ?: false
                            val isZakatHidden = snapshot.getBoolean("isZakatHidden") ?: false
                            val zakatLockReason = snapshot.getString("zakatLockReason") ?: "Zakat Calculator is temporarily undergoing rate updates."
                            val zakatLockCategory = snapshot.getString("zakatLockCategory") ?: "server_maintenance"

                            val isQiblaLocked = snapshot.getBoolean("isQiblaLocked") ?: false
                            val isQiblaHidden = snapshot.getBoolean("isQiblaHidden") ?: false
                            val qiblaLockReason = snapshot.getString("qiblaLockReason") ?: "Qibla Finder sensor calibration is currently updating."
                            val qiblaLockCategory = snapshot.getString("qiblaLockCategory") ?: "server_maintenance"

                            val isCalendarLocked = snapshot.getBoolean("isCalendarLocked") ?: false
                            val isCalendarHidden = snapshot.getBoolean("isCalendarHidden") ?: false
                            val calendarLockReason = snapshot.getString("calendarLockReason") ?: "Islamic Hijri Calendar is undergoing moon sighting verification."
                            val calendarLockCategory = snapshot.getString("calendarLockCategory") ?: "server_maintenance"

                            val isComplaintsLocked = snapshot.getBoolean("isComplaintsLocked") ?: false
                            val isComplaintsHidden = snapshot.getBoolean("isComplaintsHidden") ?: false
                            val complaintsLockReason = snapshot.getString("complaintsLockReason") ?: "Help & Complaints portal is temporarily undergoing server maintenance."
                            val complaintsLockCategory = snapshot.getString("complaintsLockCategory") ?: "server_maintenance"

                            val isDonateLocked = snapshot.getBoolean("isDonateLocked") ?: false
                            val isDonateHidden = snapshot.getBoolean("isDonateHidden") ?: false
                            val donateLockReason = snapshot.getString("donateLockReason") ?: "Support & Donate gateway is temporarily offline."
                            val donateLockCategory = snapshot.getString("donateLockCategory") ?: "server_maintenance"
                            
                            val isPrayerTimesLocked = snapshot.getBoolean("isPrayerTimesCardLocked") ?: false
                            val prayerTimesMsg = snapshot.getString("prayerTimesBlockedMessage") ?: "Prayer Times are temporarily unavailable."
                            val isDailyAyahLocked = snapshot.getBoolean("isDailyAyahCardLocked") ?: false
                            val dailyAyahMsg = snapshot.getString("dailyAyahBlockedMessage") ?: "Daily Ayah is temporarily unavailable."
                            val isTrackerLocked = snapshot.getBoolean("isTrackerCardLocked") ?: false
                            val trackerMsg = snapshot.getString("trackerBlockedMessage") ?: "Progress Tracker is temporarily unavailable."
                            val bismillahWelcomeMsg = snapshot.getString("welcomeBismillahMessage") ?: "Welcome to TaqwaHub! This offline-first Islamic companion was built with complete devotion by a single developer. Because a single developer is human, mistakes, translation errors, or bugs might occasionally slip in. If you find any, please contact us from the settings screen so we can correct them of our own accord. Press the golden Bismillah button below to unlock your spiritual companion."
                            val donateUrl = snapshot.getString("donateRedirectUrl") ?: "https://taqwahub.org/donate"
                            val privacyUrl = snapshot.getString("privacyPolicyUrl") ?: "https://taqwahub.vercel.app/privacy.html"
                            val termsUrl = snapshot.getString("termsOfServiceUrl") ?: "https://taqwahub.vercel.app/terms.html"
                            val deleteUrl = snapshot.getString("deleteAccountUrl") ?: "https://taqwahub.vercel.app/delete-account.html"

                            viewModelScope.launch(Dispatchers.Main) {
                                appConfig = AppConfig(
                                    isUnderMaintenance = isMaint,
                                    message = maintMsg,
                                    enableAiChat = aiChat,
                                    welcomeBannerMessage = welcomeMsg,
                                    forceUpdateMinVersion = minVer,
                                    updateDownloadUrl = updateUrl,
                                    isQuranAudioLocked = isAudioLocked,
                                    quranAudioBlockedMessage = quranAudioMsg,
                                    lockedSurahIds = lockedSurahIds,
                                    surahBlockedMessage = surahBlockedMsg,
                                    lockedWordSurahIds = lockedWordSurahIds,
                                    wordSurahBlockedMessage = wordSurahMsg,
                                    isQuranPageLocked = isQuranPageLocked,
                                    quranPageBlockedMessage = quranPageMsg,
                                    isToolsPageLocked = isToolsPageLocked,
                                    toolsPageBlockedMessage = toolsPageMsg,
                                    isLearnPageLocked = isLearnPageLocked,
                                    learnPageBlockedMessage = learnPageMsg,

                                    isHadithLocked = isHadithLocked,
                                    isHadithHidden = isHadithHidden,
                                    hadithLockReason = hadithLockReason,
                                    hadithLockCategory = hadithLockCategory,

                                    isDuaLocked = isDuaLocked,
                                    isDuaHidden = isDuaHidden,
                                    duaLockReason = duaLockReason,
                                    duaLockCategory = duaLockCategory,

                                    isQuranLocked = isQuranLocked,
                                    isQuranHidden = isQuranHidden,
                                    quranLockReason = quranLockReason,
                                    quranLockCategory = quranLockCategory,

                                    isLeaderboardLocked = isLeaderboardLocked,
                                    isLeaderboardHidden = isLeaderboardHidden,
                                    leaderboardLockReason = leaderboardLockReason,
                                    leaderboardLockCategory = leaderboardLockCategory,

                                    isTasksLocked = isTasksLocked,
                                    isTasksHidden = isTasksHidden,
                                    tasksLockReason = tasksLockReason,
                                    tasksLockCategory = tasksLockCategory,

                                    isTasbeehLocked = isTasbeehLocked,
                                    isTasbeehHidden = isTasbeehHidden,
                                    tasbeehLockReason = tasbeehLockReason,
                                    tasbeehLockCategory = tasbeehLockCategory,

                                    isNamesLocked = isNamesLocked,
                                    isNamesHidden = isNamesHidden,
                                    namesLockReason = namesLockReason,
                                    namesLockCategory = namesLockCategory,

                                    isZakatLocked = isZakatLocked,
                                    isZakatHidden = isZakatHidden,
                                    zakatLockReason = zakatLockReason,
                                    zakatLockCategory = zakatLockCategory,

                                    isQiblaLocked = isQiblaLocked,
                                    isQiblaHidden = isQiblaHidden,
                                    qiblaLockReason = qiblaLockReason,
                                    qiblaLockCategory = qiblaLockCategory,

                                    isCalendarLocked = isCalendarLocked,
                                    isCalendarHidden = isCalendarHidden,
                                    calendarLockReason = calendarLockReason,
                                    calendarLockCategory = calendarLockCategory,

                                    isComplaintsLocked = isComplaintsLocked,
                                    isComplaintsHidden = isComplaintsHidden,
                                    complaintsLockReason = complaintsLockReason,
                                    complaintsLockCategory = complaintsLockCategory,

                                    isDonateLocked = isDonateLocked,
                                    isDonateHidden = isDonateHidden,
                                    donateLockReason = donateLockReason,
                                    donateLockCategory = donateLockCategory,

                                    isPrayerTimesCardLocked = isPrayerTimesLocked,
                                    prayerTimesBlockedMessage = prayerTimesMsg,
                                    isDailyAyahCardLocked = isDailyAyahLocked,
                                    dailyAyahBlockedMessage = dailyAyahMsg,
                                    isTrackerCardLocked = isTrackerLocked,
                                    trackerBlockedMessage = trackerMsg,
                                    welcomeBismillahMessage = bismillahWelcomeMsg,
                                    donateRedirectUrl = donateUrl,
                                    privacyPolicyUrl = privacyUrl,
                                    termsOfServiceUrl = termsUrl,
                                    deleteAccountUrl = deleteUrl
                                )
                                Log.d("TaqwaViewModel", "Realtime AppConfig updated inside ViewModel: $appConfig")
                            }
                        }
                    }

                // Register Firestore admins listener
                adminsListenerRegistration = db.collection("system").document("admins")
                    .addSnapshotListener { snapshot, e2 ->
                        if (e2 != null) {
                            Log.w("TaqwaViewModel", "Admins snapshot listener failed: ${e2.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val rawEmails = (snapshot.get("emails") as? List<*>)
                                ?: (snapshot.get("admins") as? List<*>)
                            val rawSuperEmails = (snapshot.get("superAdmins") as? List<*>)
                                ?: (snapshot.get("super_admins") as? List<*>)
                            
                            val cleanedEmails = (rawEmails?.mapNotNull { it?.toString()?.lowercase()?.trim() }?.filter { it.isNotBlank() } ?: emptyList())
                                .plus("kb1747038@gmail.com")
                                .distinct()

                            val cleanedSuperEmails = (rawSuperEmails?.mapNotNull { it?.toString()?.lowercase()?.trim() }?.filter { it.isNotBlank() } ?: emptyList())
                                .plus("kb1747038@gmail.com")
                                .distinct()

                            val mergedAdminEmails = (cleanedEmails + cleanedSuperEmails).distinct()

                            viewModelScope.launch(Dispatchers.Main) {
                                adminEmails = mergedAdminEmails
                                superAdminEmails = cleanedSuperEmails
                                saveCachedAdminEmails(mergedAdminEmails, cleanedSuperEmails)
                                Log.d("TaqwaViewModel", "Realtime Admin emails updated: $adminEmails, Supers: $superAdminEmails")
                            }
                        }
                    }
            } catch (err: Throwable) {
                Log.e("TaqwaViewModel", "Firestore could not be initialized or loaded: ${err.message}")
            }
        }
    }

    fun saveAppConfig(newConfig: AppConfig, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val timestampStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())

                val data = hashMapOf(
                    "isUnderMaintenance" to newConfig.isUnderMaintenance,
                    "message" to newConfig.message,
                    "enableAiChat" to newConfig.enableAiChat,
                    "welcomeBannerMessage" to newConfig.welcomeBannerMessage,
                    "forceUpdateMinVersion" to newConfig.forceUpdateMinVersion,
                    "updateDownloadUrl" to newConfig.updateDownloadUrl,
                    "isQuranAudioLocked" to newConfig.isQuranAudioLocked,
                    "quranAudioBlockedMessage" to newConfig.quranAudioBlockedMessage,
                    "lockedSurahIds" to newConfig.lockedSurahIds,
                    "surahBlockedMessage" to newConfig.surahBlockedMessage,
                    "lockedWordSurahIds" to newConfig.lockedWordSurahIds,
                    "wordSurahBlockedMessage" to newConfig.wordSurahBlockedMessage,
                    "isQuranPageLocked" to newConfig.isQuranPageLocked,
                    "quranPageBlockedMessage" to newConfig.quranPageBlockedMessage,
                    "isToolsPageLocked" to newConfig.isToolsPageLocked,
                    "toolsPageBlockedMessage" to newConfig.toolsPageBlockedMessage,
                    "isLearnPageLocked" to newConfig.isLearnPageLocked,
                    "learnPageBlockedMessage" to newConfig.learnPageBlockedMessage,

                    "isHadithLocked" to newConfig.isHadithLocked,
                    "isHadithHidden" to newConfig.isHadithHidden,
                    "hadithLockReason" to newConfig.hadithLockReason,
                    "hadithLockCategory" to newConfig.hadithLockCategory,

                    "isDuaLocked" to newConfig.isDuaLocked,
                    "isDuaHidden" to newConfig.isDuaHidden,
                    "duaLockReason" to newConfig.duaLockReason,
                    "duaLockCategory" to newConfig.duaLockCategory,

                    "isQuranLocked" to newConfig.isQuranLocked,
                    "isQuranHidden" to newConfig.isQuranHidden,
                    "quranLockReason" to newConfig.quranLockReason,
                    "quranLockCategory" to newConfig.quranLockCategory,

                    "isLeaderboardLocked" to newConfig.isLeaderboardLocked,
                    "isLeaderboardHidden" to newConfig.isLeaderboardHidden,
                    "leaderboardLockReason" to newConfig.leaderboardLockReason,
                    "leaderboardLockCategory" to newConfig.leaderboardLockCategory,

                    "isTasksLocked" to newConfig.isTasksLocked,
                    "isTasksHidden" to newConfig.isTasksHidden,
                    "tasksLockReason" to newConfig.tasksLockReason,
                    "tasksLockCategory" to newConfig.tasksLockCategory,

                    "isTasbeehLocked" to newConfig.isTasbeehLocked,
                    "isTasbeehHidden" to newConfig.isTasbeehHidden,
                    "tasbeehLockReason" to newConfig.tasbeehLockReason,
                    "tasbeehLockCategory" to newConfig.tasbeehLockCategory,

                    "isNamesLocked" to newConfig.isNamesLocked,
                    "isNamesHidden" to newConfig.isNamesHidden,
                    "namesLockReason" to newConfig.namesLockReason,
                    "namesLockCategory" to newConfig.namesLockCategory,

                    "isZakatLocked" to newConfig.isZakatLocked,
                    "isZakatHidden" to newConfig.isZakatHidden,
                    "zakatLockReason" to newConfig.zakatLockReason,
                    "zakatLockCategory" to newConfig.zakatLockCategory,

                    "isQiblaLocked" to newConfig.isQiblaLocked,
                    "isQiblaHidden" to newConfig.isQiblaHidden,
                    "qiblaLockReason" to newConfig.qiblaLockReason,
                    "qiblaLockCategory" to newConfig.qiblaLockCategory,

                    "isCalendarLocked" to newConfig.isCalendarLocked,
                    "isCalendarHidden" to newConfig.isCalendarHidden,
                    "calendarLockReason" to newConfig.calendarLockReason,
                    "calendarLockCategory" to newConfig.calendarLockCategory,

                    "isComplaintsLocked" to newConfig.isComplaintsLocked,
                    "isComplaintsHidden" to newConfig.isComplaintsHidden,
                    "complaintsLockReason" to newConfig.complaintsLockReason,
                    "complaintsLockCategory" to newConfig.complaintsLockCategory,

                    "isDonateLocked" to newConfig.isDonateLocked,
                    "isDonateHidden" to newConfig.isDonateHidden,
                    "donateLockReason" to newConfig.donateLockReason,
                    "donateLockCategory" to newConfig.donateLockCategory,

                    "isPrayerTimesCardLocked" to newConfig.isPrayerTimesCardLocked,
                    "prayerTimesBlockedMessage" to newConfig.prayerTimesBlockedMessage,
                    "isDailyAyahCardLocked" to newConfig.isDailyAyahCardLocked,
                    "dailyAyahBlockedMessage" to newConfig.dailyAyahBlockedMessage,
                    "isTrackerCardLocked" to newConfig.isTrackerCardLocked,
                    "trackerBlockedMessage" to newConfig.trackerBlockedMessage,
                    "welcomeBismillahMessage" to newConfig.welcomeBismillahMessage,
                    "donateRedirectUrl" to newConfig.donateRedirectUrl,
                    "privacyPolicyUrl" to newConfig.privacyPolicyUrl,
                    "termsOfServiceUrl" to newConfig.termsOfServiceUrl,
                    "deleteAccountUrl" to newConfig.deleteAccountUrl,
                    "updatedBy" to (currentUser?.email ?: "kb1747038@gmail.com"),
                    "updatedAt" to timestampStr
                )
                db.collection("system").document("maintenance")
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Firestore AppConfig saved successfully with merge!")
                        viewModelScope.launch(Dispatchers.Main) {
                            appConfig = newConfig
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Firestore AppConfig save failed: ${e.message}")
                        viewModelScope.launch(Dispatchers.Main) {
                            onFailure(e.message ?: "Unknown error")
                        }
                    }
            } catch (err: Throwable) {
                Log.e("TaqwaViewModel", "Firestore AppConfig save triggered throwable: ${err.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    onFailure(err.message ?: "System initialization error")
                }
            }
        }
    }

    fun saveAppConfigPartial(updates: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val timestampStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())

                val mutableData = updates.toMutableMap()
                mutableData["updatedBy"] = (currentUser?.email ?: "kb1747038@gmail.com")
                mutableData["updatedAt"] = timestampStr

                db.collection("system").document("maintenance")
                    .set(mutableData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Firestore AppConfig partial update saved successfully!")
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Firestore AppConfig partial save failed: ${e.message}")
                        viewModelScope.launch(Dispatchers.Main) {
                            onFailure(e.message ?: "Unknown error")
                        }
                    }
            } catch (err: Throwable) {
                Log.e("TaqwaViewModel", "Firestore AppConfig partial save error: ${err.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    onFailure(err.message ?: "System initialization error")
                }
            }
        }
    }

    fun updateAdminEmails(newEmails: List<String>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (!isAdmin) {
            onFailure("Unauthorized: Only Admins can modify the admin access list.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val cleanedEmails = newEmails.map { it.lowercase().trim() }.filter { it.isNotBlank() }.distinct()
                val timestampStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())

                val data = hashMapOf(
                    "emails" to cleanedEmails,
                    "updatedBy" to (currentUser?.email ?: "kb1747038@gmail.com"),
                    "updatedAt" to timestampStr
                )
                db.collection("system").document("admins")
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Firestore Admins updated successfully!")
                        viewModelScope.launch(Dispatchers.Main) {
                            adminEmails = cleanedEmails
                            saveCachedAdminEmails(cleanedEmails, superAdminEmails)
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Firestore Admins save failed: ${e.message}")
                        viewModelScope.launch(Dispatchers.Main) {
                            onFailure(e.message ?: "Unknown error")
                        }
                    }
            } catch (err: Throwable) {
                Log.e("TaqwaViewModel", "Firestore Admins save error: ${err.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    onFailure(err.message ?: "Initial database error")
                }
            }
        }
    }

    fun updateSuperAdminEmails(newSuperEmails: List<String>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (!isSuperAdmin) {
            onFailure("Unauthorized: Only Super Admins can update Super Admin roles.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                val cleanedSupers = newSuperEmails.map { it.lowercase().trim() }.filter { it.isNotBlank() }.distinct()
                val timestampStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())

                val data = hashMapOf(
                    "superAdmins" to cleanedSupers,
                    "updatedBy" to (currentUser?.email ?: "kb1747038@gmail.com"),
                    "updatedAt" to timestampStr
                )
                db.collection("system").document("admins")
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("TaqwaViewModel", "Firestore Super Admins updated successfully!")
                        viewModelScope.launch(Dispatchers.Main) {
                            superAdminEmails = cleanedSupers
                            saveCachedAdminEmails(adminEmails, cleanedSupers)
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TaqwaViewModel", "Firestore Super Admins save failed: ${e.message}")
                        viewModelScope.launch(Dispatchers.Main) {
                            onFailure(e.message ?: "Unknown error")
                        }
                    }
            } catch (err: Throwable) {
                Log.e("TaqwaViewModel", "Firestore Super Admins save error: ${err.message}")
                viewModelScope.launch(Dispatchers.Main) {
                    onFailure(err.message ?: "Initial database error")
                }
            }
        }
    }

    fun triggerFirebaseSync(forcePull: Boolean = false) {
        if (!isInitialized) return
        val user = currentUser ?: return
        if (networkStatus.value.type == TaqwaNetworkType.NONE) {
            Log.d("TaqwaViewModel", "Firebase sync skipped: Offline")
            hasCompletedInitialSync = true
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                viewModelScope.launch(Dispatchers.Main) { isSyncingData = true }
                Log.d("TaqwaViewModel", "Beginning bi-directional Firebase sync for user: ${user.email}")
                
                injectTaqwaHubProfilePictureRemote()
                
                val db = FirebaseFirestore.getInstance()
                val userDocRef = db.collection("users").document(user.uid)

                var remoteDocExists = false
                var remoteLastUpdated: Long = 0
                
                val sharedPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
                val localLastUpdatedOnStart = sharedPrefs.getLong("last_local_update", 0L)

                val taskSource = com.google.android.gms.tasks.Tasks.await(userDocRef.get())
                
                // Re-read localLastUpdated immediately after the network call finishes to include any local updates made while waiting
                val localLastUpdated = sharedPrefs.getLong("last_local_update", 0L)
                val remoteStatsMap = if (taskSource.exists()) taskSource.get("userStats") as? Map<String, Any> else null
                val remoteUsername = (remoteStatsMap?.get("username") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("username") as? String)?.takeIf { it.isNotBlank() }
                val remoteName = (remoteStatsMap?.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("displayName") as? String)?.takeIf { it.isNotBlank() }

                if (taskSource.exists()) {
                    remoteDocExists = true
                    remoteLastUpdated = taskSource.getLong("lastUpdatedAt") ?: 0L
                    if (remoteStatsMap != null || !remoteUsername.isNullOrBlank() || !remoteName.isNullOrBlank()) {
                        try {
                            val tprefs = getSecurePrefs()
                            tprefs.edit().putBoolean("profile_completed_${user.uid}", true).apply()
                            val stdPrefs = getApplication<Application>().getSharedPreferences("taqwa_prefs", android.content.Context.MODE_PRIVATE)
                            stdPrefs.edit().putBoolean("profile_completed_${user.uid}", true).apply()
                            Log.d("TaqwaViewModel", "Bypassing profile setup flag set to true from Firestore existence.")
                        } catch (e: Exception) {
                            Log.e("TaqwaViewModel", "Error saving profile sync exists key", e)
                        }
                    }
                }

                val remoteAdminUpdated = if (taskSource.exists()) taskSource.getLong("adminUpdatedTimestamp") ?: 0L else 0L
                val currentLocalStats = repository.taqwaDao.getUserStatsDirect()
                val localTasks = repository.taqwaDao.getAllTasksDirect()
                val isLocalDataEmpty = currentLocalStats == null || 
                        (currentLocalStats.username.isBlank() && currentLocalStats.name.isBlank() && 
                         currentLocalStats.totalTasksCompleted == 0 && currentLocalStats.totalXp == 0 && localTasks.none { it.completed })

                val hasRemoteProfileData = remoteDocExists && (remoteStatsMap != null || !remoteUsername.isNullOrBlank() || !remoteName.isNullOrBlank())
                val shouldPull = remoteDocExists && (
                    remoteLastUpdated > localLastUpdated || 
                    remoteAdminUpdated > localLastUpdated || 
                    forcePull || 
                    isLocalDataEmpty || 
                    (hasRemoteProfileData && (
                        (!remoteUsername.isNullOrBlank() && currentLocalStats?.username.isNullOrBlank()) || 
                        (!remoteName.isNullOrBlank() && currentLocalStats?.name.isNullOrBlank())
                    ))
                )

                Log.d("TaqwaViewModel", "Sync check: localLastUpdated=$localLastUpdated, remoteLastUpdated=$remoteLastUpdated, remoteAdminUpdated=$remoteAdminUpdated, isLocalDataEmpty=$isLocalDataEmpty, shouldPull=$shouldPull")

                if (shouldPull) {
                    Log.d("TaqwaViewModel", "Firebase has data or is newer. Pulling remote data down to Room database...")
                    
                    if (remoteStatsMap != null || !remoteUsername.isNullOrBlank() || !remoteName.isNullOrBlank()) {
                        val pName = (remoteStatsMap?.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("name") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("displayName") as? String)?.takeIf { it.isNotBlank() } ?: ""
                        val pUsername = (remoteStatsMap?.get("username") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("username") as? String)?.takeIf { it.isNotBlank() } ?: ""
                        val pGender = (remoteStatsMap?.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("gender") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("sectGender") as? String)?.takeIf { it.isNotBlank() } ?: ""
                        val pSectOrCast = (remoteStatsMap?.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("sectOrCast") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("sect") as? String)?.takeIf { it.isNotBlank() } ?: (taskSource.get("cast") as? String)?.takeIf { it.isNotBlank() } ?: ""

                        val mName = pName.ifBlank { currentLocalStats?.name ?: "" }
                        val mUsername = pUsername.ifBlank { currentLocalStats?.username ?: "" }
                        val mGender = pGender.ifBlank { currentLocalStats?.gender ?: "" }
                        val mSectOrCast = pSectOrCast.ifBlank { currentLocalStats?.sectOrCast ?: "" }

                        val stats = UserStatsEntity(
                            id = 1,
                            totalTasksCompleted = ((remoteStatsMap?.get("totalTasksCompleted") as? Long)?.toInt() ?: (currentLocalStats?.totalTasksCompleted ?: 0)) + pendingCompletedTasksBuffer.size,
                            daysActive = (remoteStatsMap?.get("daysActive") as? Long)?.toInt() ?: (currentLocalStats?.daysActive ?: 1),
                            quranProgress = (remoteStatsMap?.get("quranProgress") as? Long)?.toInt() ?: (currentLocalStats?.quranProgress ?: 0),
                            lastReadSurah = (remoteStatsMap?.get("lastReadSurah") as? Long)?.toInt() ?: (currentLocalStats?.lastReadSurah ?: 1),
                            lastReadVerse = (remoteStatsMap?.get("lastReadVerse") as? Long)?.toInt() ?: (currentLocalStats?.lastReadVerse ?: 1),
                            lastReadVerseKey = remoteStatsMap?.get("lastReadVerseKey") as? String ?: (currentLocalStats?.lastReadVerseKey ?: "1:1"),
                            tasbeehCount = ((remoteStatsMap?.get("tasbeehCount") as? Long)?.toInt() ?: (currentLocalStats?.tasbeehCount ?: 0)) + pendingTasbeehBuffer.get(),
                            lastResetDate = remoteStatsMap?.get("lastResetDate") as? String ?: (currentLocalStats?.lastResetDate ?: ""),
                            currentStreak = (remoteStatsMap?.get("currentStreak") as? Long)?.toInt() ?: (currentLocalStats?.currentStreak ?: 0),
                            streakChancesLeft = ((remoteStatsMap?.get("streakShields") as? Long)?.toInt() ?: ((remoteStatsMap?.get("streakChancesLeft") as? Long)?.toInt() ?: (currentLocalStats?.streakShields ?: 0))).coerceIn(0, 2),
                            longestStreak = (remoteStatsMap?.get("longestStreak") as? Long)?.toInt() ?: (currentLocalStats?.longestStreak ?: 0),
                            totalXp = ((remoteStatsMap?.get("totalXp") as? Long)?.toInt() ?: (currentLocalStats?.totalXp ?: 0)) + pendingXpBuffer.get(),
                            weeklyXp = ((remoteStatsMap?.get("weeklyXp") as? Long)?.toInt() ?: (currentLocalStats?.weeklyXp ?: 0)) + pendingWeeklyXpBuffer.get(),
                            lastActiveWeekOfYear = (remoteStatsMap?.get("lastActiveWeekOfYear") as? Long)?.toInt() ?: (currentLocalStats?.lastActiveWeekOfYear ?: 0),
                            name = mName,
                            username = mUsername,
                            gender = mGender,
                            sectOrCast = mSectOrCast,
                            email = remoteStatsMap?.get("email") as? String ?: (user.email ?: ""),
                            completedSurahs = run {
                                val remoteSurahsStr = remoteStatsMap?.get("completedSurahs") as? String ?: (currentLocalStats?.completedSurahs ?: "")
                                val remoteSet = remoteSurahsStr.split(",").filter { it.isNotEmpty() }.map { it.trim() }.toMutableSet()
                                remoteSet.addAll(pendingCompletedSurahsBuffer)
                                remoteSet.joinToString(",")
                            },
                            firstPlaceCount = (remoteStatsMap?.get("firstPlaceCount") as? Long)?.toInt() ?: (currentLocalStats?.firstPlaceCount ?: 0),
                            secondPlaceCount = (remoteStatsMap?.get("secondPlaceCount") as? Long)?.toInt() ?: (currentLocalStats?.secondPlaceCount ?: 0),
                            thirdPlaceCount = (remoteStatsMap?.get("thirdPlaceCount") as? Long)?.toInt() ?: (currentLocalStats?.thirdPlaceCount ?: 0),
                            isBlocked = remoteStatsMap?.get("isBlocked") as? Boolean ?: (currentLocalStats?.isBlocked ?: false),
                            isVerified = remoteStatsMap?.get("isVerified") as? Boolean ?: (currentLocalStats?.isVerified ?: false),
                            profilePictureBase64 = remoteStatsMap?.get("profilePictureBase64") as? String ?: (currentLocalStats?.profilePictureBase64 ?: ""),
                            lastWeekXp = (remoteStatsMap?.get("lastWeekXp") as? Long)?.toInt() ?: (currentLocalStats?.lastWeekXp ?: 0),
                            lastWeekCode = (remoteStatsMap?.get("lastWeekCode") as? Long)?.toInt() ?: (currentLocalStats?.lastWeekCode ?: 0),
                            lastActiveDate = remoteStatsMap?.get("lastActiveDate") as? String ?: (currentLocalStats?.lastActiveDate ?: ""),
                            streakShields = ((remoteStatsMap?.get("streakShields") as? Long)?.toInt() ?: ((remoteStatsMap?.get("streakChancesLeft") as? Long)?.toInt() ?: (currentLocalStats?.streakShields ?: 0))).coerceIn(0, 2),
                            maxShields = 2,
                            frozenDates = remoteStatsMap?.get("frozenDates") as? String ?: (currentLocalStats?.frozenDates ?: ""),
                            activeDates = remoteStatsMap?.get("activeDates") as? String ?: (currentLocalStats?.activeDates ?: ""),
                            lastShieldUsedDate = remoteStatsMap?.get("lastShieldUsedDate") as? String ?: (currentLocalStats?.lastShieldUsedDate ?: ""),
                            streakRepairsAvailable = (remoteStatsMap?.get("streakRepairsAvailable") as? Long)?.toInt() ?: (currentLocalStats?.streakRepairsAvailable ?: 1)
                        )
                        repository.taqwaDao.insertUserStats(stats)
                    }
 
                    val remoteTasksList = taskSource.get("tasks") as? List<Map<String, Any>>
                    if (remoteTasksList != null && remoteTasksList.isNotEmpty()) {
                        val tasks = remoteTasksList.map {
                            val tid = it["id"] as? String ?: ""
                            val isBufferedCompleted = pendingCompletedTasksBuffer.contains(tid)
                            TaskEntity(
                                id = tid,
                                title = it["title"] as? String ?: "",
                                completed = isBufferedCompleted || (it["completed"] as? Boolean ?: false),
                                category = it["category"] as? String ?: "",
                                description = it["description"] as? String ?: "",
                                points = (it["points"] as? Long)?.toInt() ?: 10,
                                tag = it["tag"] as? String ?: "",
                                timerSeconds = (it["timerSeconds"] as? Long)?.toInt() ?: 0,
                                isSystemTask = it["isSystemTask"] as? Boolean ?: false,
                                isAuto = it["isAuto"] as? Boolean ?: false,
                                autoType = it["autoType"] as? String ?: "",
                                autoTarget = (it["autoTarget"] as? Long)?.toInt() ?: 0,
                                autoProgress = if (isBufferedCompleted) ((it["autoTarget"] as? Long)?.toInt() ?: 0) else ((it["autoProgress"] as? Long)?.toInt() ?: 0),
                                targetSurahNumber = (it["targetSurahNumber"] as? Long)?.toInt(),
                                actionRoute = it["actionRoute"] as? String ?: ""
                            )
                        }.filter { it.id.isNotEmpty() }
                        if (tasks.isNotEmpty()) {
                            repository.taqwaDao.clearTasks()
                            repository.taqwaDao.insertAllTasks(tasks)
                        }
                    }

                    val remoteBookmarksList = taskSource.get("bookmarks") as? List<Map<String, Any>>
                    if (remoteBookmarksList != null) {
                        val dbBookmarks = repository.taqwaDao.getAllBookmarksDirect()
                        for (b in dbBookmarks) {
                            repository.taqwaDao.deleteBookmarkById(b.id)
                        }
                        remoteBookmarksList.forEach {
                            val bookmark = BookmarkEntity(
                                id = it["id"] as? String ?: "",
                                surahNumber = (it["surahNumber"] as? Long)?.toInt() ?: 1,
                                surahName = it["surahName"] as? String ?: "",
                                verseNumber = (it["verseNumber"] as? Long)?.toInt() ?: 1,
                                verseKey = it["verseKey"] as? String ?: "",
                                timestamp = it["timestamp"] as? Long ?: System.currentTimeMillis(),
                                isFlowMode = it["isFlowMode"] as? Boolean ?: false
                            )
                            repository.taqwaDao.insertBookmark(bookmark)
                        }
                    }

                    val remoteAllTimeList = taskSource.get("allTimeTasks") as? List<Map<String, Any>>
                    if (remoteAllTimeList != null) {
                        val allTimeDb = repository.taqwaDao.getAllTimeTasksDirect()
                        allTimeDb.forEach { repository.taqwaDao.deleteAllTimeTaskById(it.id) }
                        remoteAllTimeList.forEach {
                            val info = AllTimeTaskEntity(
                                id = it["id"] as? String ?: "",
                                taskId = it["taskId"] as? String ?: "",
                                title = it["title"] as? String ?: "",
                                category = it["category"] as? String ?: "",
                                date = it["date"] as? String ?: "",
                                completedAt = it["completedAt"] as? String ?: ""
                            )
                            repository.taqwaDao.insertAllTimeTask(info)
                        }
                        val streakRes = repository.recalculateAndSaveStreak()
                        if (streakRes.shieldConsumedForDate != null) {
                            withContext(Dispatchers.Main) {
                                shieldSavedDateStr = streakRes.shieldConsumedForDate
                                showShieldActivatedCelebration = true
                            }
                        }
                    }

                    val remoteAiChatState = taskSource.get("aiChatState") as? Map<String, Any>
                    if (remoteAiChatState != null) {
                        val remoteQueryCount = (remoteAiChatState["queryCount"] as? Long)?.toInt() ?: 0
                        val remoteIsChatLocked = remoteAiChatState["isChatLocked"] as? Boolean ?: false
                        val remoteLockEndTime = remoteAiChatState["lockEndTime"] as? Long ?: 0L
                        
                        val remoteChatMsgsList = remoteAiChatState["chatMessages"] as? List<Map<String, Any>>
                        val parsedChatMsgs = remoteChatMsgsList?.map {
                            ChatMessage(
                                role = it["role"] as? String ?: "user",
                                text = it["text"] as? String ?: "",
                                citations = (it["citations"] as? List<*>)?.mapNotNull { c -> c as? String } ?: emptyList(),
                                citationFullTexts = (it["citationFullTexts"] as? List<*>)?.mapNotNull { c -> c as? String } ?: emptyList(),
                                id = it["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                rating = it["rating"] as? String ?: "none",
                                reportMessage = it["reportMessage"] as? String ?: ""
                            )
                        } ?: emptyList()
                        
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            queryCount = remoteQueryCount
                            isChatLocked = remoteIsChatLocked
                            aiLockEndTime = remoteLockEndTime
                            chatMessages = parsedChatMsgs
                            
                            val tprefs = getSecurePrefs()
                            val teditor = tprefs.edit()
                            teditor.putInt("ai_query_count", queryCount)
                            teditor.putBoolean("ai_is_chat_locked", isChatLocked)
                            teditor.putLong("ai_lock_end_time", aiLockEndTime)
                            try {
                                val adapter = localMoshi.adapter<List<ChatMessage>>(
                                    com.squareup.moshi.Types.newParameterizedType(List::class.java, ChatMessage::class.java)
                                )
                                teditor.putString("ai_chat_messages", adapter.toJson(chatMessages))
                            } catch(e: Exception) {}
                            teditor.apply()
                            
                            if (isChatLocked) {
                                val nowMillis = System.currentTimeMillis()
                                if (nowMillis >= aiLockEndTime) {
                                    isChatLocked = false
                                    queryCount = 0
                                    aiLockEndTime = 0L
                                    val teditor2 = tprefs.edit()
                                    teditor2.putInt("ai_query_count", 0)
                                    teditor2.putBoolean("ai_is_chat_locked", false)
                                    teditor2.putLong("ai_lock_end_time", 0L)
                                    teditor2.apply()
                                } else {
                                    startLockTimeRemainingCountdown(aiLockEndTime)
                                }
                            }
                        }
                    }

                    sharedPrefs.edit().putLong("last_local_update", maxOf(remoteLastUpdated, System.currentTimeMillis())).apply()
                    Log.d("TaqwaViewModel", "Room Database successfully in sync with Firestore!")
                    
                    // Clear the buffers atomically since they are now fully merged!
                    pendingXpBuffer.set(0)
                    pendingWeeklyXpBuffer.set(0)
                    pendingTasbeehBuffer.set(0)
                    pendingCompletedTasksBuffer.clear()
                    pendingCompletedSurahsBuffer.clear()
                } else {
                    Log.d("TaqwaViewModel", "Local data is newer. Pushing up to Firestore...")
                    
                    var stats = repository.getUserStats()
                    if (stats.email.isBlank() && !user.email.isNullOrBlank()) {
                        stats = stats.copy(email = user.email!!)
                        repository.saveUserStats(stats)
                    }

                    // Preserve remote username/name if local is blank
                    val safeName = if (stats.name.isNotBlank()) stats.name else ((remoteStatsMap?.get("name") as? String) ?: (taskSource.get("name") as? String) ?: "")
                    val safeUsername = if (stats.username.isNotBlank()) stats.username else ((remoteStatsMap?.get("username") as? String) ?: (taskSource.get("username") as? String) ?: "")
                    val safeGender = if (stats.gender.isNotBlank()) stats.gender else ((remoteStatsMap?.get("gender") as? String) ?: (taskSource.get("gender") as? String) ?: "")
                    val safeSect = if (stats.sectOrCast.isNotBlank()) stats.sectOrCast else ((remoteStatsMap?.get("sectOrCast") as? String) ?: (taskSource.get("sectOrCast") as? String) ?: "")

                    if (stats.name != safeName || stats.username != safeUsername) {
                        stats = stats.copy(
                            name = safeName,
                            username = safeUsername,
                            gender = safeGender,
                            sectOrCast = safeSect
                        )
                        repository.taqwaDao.insertUserStats(stats)
                    }

                    val tasksList = repository.taqwaDao.getAllTasksDirect()
                    val bookmarksList = repository.taqwaDao.getAllBookmarksDirect()
                    val allTimeList = repository.taqwaDao.getAllTimeTasksDirect()

                    val firebaseTimestamp = System.currentTimeMillis()

                    val statsMap = hashMapOf(
                        "totalTasksCompleted" to stats.totalTasksCompleted,
                        "daysActive" to stats.daysActive,
                        "quranProgress" to stats.quranProgress,
                        "lastReadSurah" to stats.lastReadSurah,
                        "lastReadVerse" to stats.lastReadVerse,
                        "lastReadVerseKey" to stats.lastReadVerseKey,
                        "tasbeehCount" to stats.tasbeehCount,
                        "lastResetDate" to stats.lastResetDate,
                        "currentStreak" to stats.currentStreak,
                        "streakChancesLeft" to stats.streakShields,
                        "streakShields" to stats.streakShields,
                        "maxShields" to stats.maxShields,
                        "frozenDates" to stats.frozenDates,
                        "activeDates" to stats.activeDates,
                        "lastActiveDate" to stats.lastActiveDate,
                        "lastShieldUsedDate" to stats.lastShieldUsedDate,
                        "streakRepairsAvailable" to stats.streakRepairsAvailable,
                        "longestStreak" to stats.longestStreak,
                        "totalXp" to stats.totalXp,
                        "weeklyXp" to stats.weeklyXp,
                        "lastActiveWeekOfYear" to stats.lastActiveWeekOfYear,
                        "name" to stats.name,
                        "username" to stats.username,
                        "gender" to stats.gender,
                        "sectOrCast" to stats.sectOrCast,
                        "email" to stats.email,
                        "completedSurahs" to stats.completedSurahs,
                        "firstPlaceCount" to stats.firstPlaceCount,
                        "secondPlaceCount" to stats.secondPlaceCount,
                        "thirdPlaceCount" to stats.thirdPlaceCount,
                        "isBlocked" to stats.isBlocked,
                        "isVerified" to stats.isVerified,
                        "profilePictureBase64" to stats.profilePictureBase64,
                        "lastWeekXp" to stats.lastWeekXp,
                        "lastWeekCode" to stats.lastWeekCode
                    )

                    val tasksSerialized = tasksList.map {
                        hashMapOf(
                            "id" to it.id,
                            "title" to it.title,
                            "completed" to it.completed,
                            "category" to it.category,
                            "description" to it.description,
                            "points" to it.points,
                            "tag" to it.tag,
                            "timerSeconds" to it.timerSeconds,
                            "isSystemTask" to it.isSystemTask,
                            "isAuto" to it.isAuto,
                            "autoType" to it.autoType,
                            "autoTarget" to it.autoTarget,
                            "autoProgress" to it.autoProgress,
                            "targetSurahNumber" to it.targetSurahNumber,
                            "actionRoute" to it.actionRoute
                        )
                    }

                    val bookmarksSerialized = bookmarksList.map {
                        hashMapOf(
                            "id" to it.id,
                            "surahNumber" to it.surahNumber,
                            "surahName" to it.surahName,
                            "verseNumber" to it.verseNumber,
                            "verseKey" to it.verseKey,
                            "timestamp" to it.timestamp,
                            "isFlowMode" to it.isFlowMode
                        )
                    }

                    val allTimeSerialized = allTimeList.map {
                        hashMapOf(
                            "id" to it.id,
                            "taskId" to it.taskId,
                            "title" to it.title,
                            "category" to it.category,
                            "date" to it.date,
                            "completedAt" to it.completedAt
                        )
                    }

                    val chatMessagesSerialized = chatMessages.map {
                        hashMapOf(
                            "role" to it.role,
                            "text" to it.text,
                            "citations" to it.citations,
                            "citationFullTexts" to it.citationFullTexts,
                            "id" to it.id,
                            "rating" to it.rating,
                            "reportMessage" to it.reportMessage
                        )
                    }

                    val aiChatStateMap = hashMapOf(
                        "queryCount" to queryCount,
                        "isChatLocked" to isChatLocked,
                        "lockEndTime" to aiLockEndTime,
                        "chatMessages" to chatMessagesSerialized
                    )

                    val dataPayload = hashMapOf(
                        "uid" to user.uid,
                        "email" to (user.email ?: ""),
                        "name" to stats.name,
                        "username" to stats.username,
                        "gender" to stats.gender,
                        "sectOrCast" to stats.sectOrCast,
                        "lastUpdatedAt" to firebaseTimestamp,
                        "userStats" to statsMap,
                        "tasks" to tasksSerialized,
                        "bookmarks" to bookmarksSerialized,
                        "allTimeTasks" to allTimeSerialized,
                        "aiChatState" to aiChatStateMap
                    )

                    // Update last_local_update BEFORE the network call to prevent snapshot listener race conditions
                    sharedPrefs.edit().putLong("last_local_update", firebaseTimestamp).apply()

                    com.google.android.gms.tasks.Tasks.await(userDocRef.set(dataPayload, com.google.firebase.firestore.SetOptions.merge()))
                    Log.d("TaqwaViewModel", "Local database state pushed successfully to Firestore!")
                }
            } catch (err: Throwable) {
                Log.e("TaqwaViewModel", "Error running bi-directional Firebase sync: ${err.message}")
            } finally {
                viewModelScope.launch(Dispatchers.Main) {
                    isSyncingData = false
                    hasCompletedInitialSync = true
                }
                checkAndProcessWeeklyReset()
            }
        }
    }

    fun markLocalUpdateAndSync() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong("last_local_update", System.currentTimeMillis()).apply()
        triggerFirebaseSync()
    }

    fun markLocalUpdateAndSyncDebounced() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("taqwahub_sync", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong("last_local_update", System.currentTimeMillis()).apply()
        
        debouncedSyncJob?.cancel()
        debouncedSyncJob = viewModelScope.launch(Dispatchers.IO) {
            delay(4000)
            triggerFirebaseSync()
        }
    }

    fun updateCustomAdhanUri(uri: String?) {
        customAdhanUri = uri
        val prefs = getSecurePrefs()
        prefs.edit().putString("custom_adhan_uri", uri).apply()
    }

    private fun injectTaqwaHubProfilePictureRemote() {
        if (!isSuperAdmin) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .whereEqualTo("userStats.email", "taqwahub.ai@gmail.com")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null && !snapshot.isEmpty) {
                            val targetDoc = snapshot.documents[0]
                            val targetUid = targetDoc.id
                            val currentStats = targetDoc.get("userStats") as? Map<String, Any>
                            
                            val profilePic = currentStats?.get("profilePictureBase64") as? String ?: ""
                            if (profilePic.isEmpty()) {
                                val context = getApplication<Application>()
                                val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.mosque_hex_022c22_1780678493967)
                                if (bitmap != null) {
                                    val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 256, 256, true)
                                    val baos = java.io.ByteArrayOutputStream()
                                    scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos)
                                    val base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT)
                                    
                                    val updatedMap = currentStats?.toMutableMap() ?: mutableMapOf()
                                    updatedMap["profilePictureBase64"] = base64
                                    db.collection("users").document(targetUid)
                                        .update("userStats", updatedMap)
                                        .addOnSuccessListener {
                                            Log.d("TaqwaViewModel", "Successfully injected taqwahub.ai profile picture remotely")
                                        }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("TaqwaViewModel", "Error injecting admin profile picture", e)
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(uid).delete().addOnCompleteListener {
                user.delete().addOnCompleteListener { task ->
                    clearLocalDataAndPreferences()
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        // Even if remote auth delete has edge-case failure, local data was wiped & logged out safely
                        onSuccess()
                    }
                }
            }
        } else {
            clearLocalDataAndPreferences()
            onSuccess()
        }
    }

    fun isModuleLocked(moduleId: String): Boolean {
        return when (moduleId) {
            "quran" -> appConfig.isQuranLocked || appConfig.isQuranPageLocked
            "hadith" -> appConfig.isHadithLocked || appConfig.isLearnPageLocked
            "dua" -> appConfig.isDuaLocked || appConfig.isLearnPageLocked
            "names" -> appConfig.isNamesLocked || appConfig.isLearnPageLocked
            "leaderboard" -> appConfig.isLeaderboardLocked
            "tasks" -> appConfig.isTasksLocked || appConfig.isToolsPageLocked
            "tasbeeh" -> appConfig.isTasbeehLocked || appConfig.isToolsPageLocked
            "zakat" -> appConfig.isZakatLocked || appConfig.isToolsPageLocked
            "qibla" -> appConfig.isQiblaLocked || appConfig.isToolsPageLocked
            "calendar" -> appConfig.isCalendarLocked || appConfig.isToolsPageLocked
            "user_complaints" -> appConfig.isComplaintsLocked
            "donate" -> appConfig.isDonateLocked
            else -> false
        }
    }

    fun isModuleHidden(moduleId: String): Boolean {
        return when (moduleId) {
            "quran" -> appConfig.isQuranHidden
            "hadith" -> appConfig.isHadithHidden
            "dua" -> appConfig.isDuaHidden
            "names" -> appConfig.isNamesHidden
            "leaderboard" -> appConfig.isLeaderboardHidden
            "tasks" -> appConfig.isTasksHidden
            "tasbeeh" -> appConfig.isTasbeehHidden
            "zakat" -> appConfig.isZakatHidden
            "qibla" -> appConfig.isQiblaHidden
            "calendar" -> appConfig.isCalendarHidden
            "user_complaints" -> appConfig.isComplaintsHidden
            "donate" -> appConfig.isDonateHidden
            else -> false
        }
    }

    fun getModuleLockReason(moduleId: String): String {
        return when (moduleId) {
            "quran" -> appConfig.quranLockReason.ifBlank { appConfig.quranPageBlockedMessage }
            "hadith" -> appConfig.hadithLockReason.ifBlank { appConfig.learnPageBlockedMessage }
            "dua" -> appConfig.duaLockReason.ifBlank { appConfig.learnPageBlockedMessage }
            "names" -> appConfig.namesLockReason.ifBlank { appConfig.learnPageBlockedMessage }
            "leaderboard" -> appConfig.leaderboardLockReason
            "tasks" -> appConfig.tasksLockReason.ifBlank { appConfig.toolsPageBlockedMessage }
            "tasbeeh" -> appConfig.tasbeehLockReason.ifBlank { appConfig.toolsPageBlockedMessage }
            "zakat" -> appConfig.zakatLockReason.ifBlank { appConfig.toolsPageBlockedMessage }
            "qibla" -> appConfig.qiblaLockReason.ifBlank { appConfig.toolsPageBlockedMessage }
            "calendar" -> appConfig.calendarLockReason.ifBlank { appConfig.toolsPageBlockedMessage }
            "user_complaints" -> appConfig.complaintsLockReason
            "donate" -> appConfig.donateLockReason
            else -> "This module is currently locked by the administrator."
        }
    }

    fun getModuleLockCategory(moduleId: String): String {
        return when (moduleId) {
            "quran" -> appConfig.quranLockCategory
            "hadith" -> appConfig.hadithLockCategory
            "dua" -> appConfig.duaLockCategory
            "names" -> appConfig.namesLockCategory
            "leaderboard" -> appConfig.leaderboardLockCategory
            "tasks" -> appConfig.tasksLockCategory
            "tasbeeh" -> appConfig.tasbeehLockCategory
            "zakat" -> appConfig.zakatLockCategory
            "qibla" -> appConfig.qiblaLockCategory
            "calendar" -> appConfig.calendarLockCategory
            "user_complaints" -> appConfig.complaintsLockCategory
            "donate" -> appConfig.donateLockCategory
            else -> "server_maintenance"
        }
    }

    fun getModuleTitle(moduleId: String): String {
        return when (moduleId) {
            "quran" -> "Quran Reader"
            "hadith" -> "Hadith Explorer"
            "dua" -> "Dua & Azkar Library"
            "names" -> "99 Names of Allah"
            "leaderboard" -> "Global Leaderboard"
            "tasks" -> "Task Tracker & Challenges"
            "tasbeeh" -> "Digital Tasbeeh Counter"
            "zakat" -> "Zakat Calculator"
            "qibla" -> "Qibla Finder"
            "calendar" -> "Islamic Hijri Calendar"
            "user_complaints" -> "Help & Complaints Portal"
            "donate" -> "Support & Donate"
            else -> "App Feature"
        }
    }

    // Duolingo-Grade Streak Engine State & Controls
    var showStreakModal by mutableStateOf(false)
    var showShieldActivatedCelebration by mutableStateOf(false)
    var shieldSavedDateStr by mutableStateOf("")

    fun openStreakModal() {
        showStreakModal = true
    }

    fun closeStreakModal() {
        showStreakModal = false
    }

    fun dismissShieldCelebration() {
        showShieldActivatedCelebration = false
    }

    fun repairBrokenStreak(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = stats.value
            if (s.streakRepairsAvailable <= 0) {
                withContext(Dispatchers.Main) {
                    onFailure("No streak repairs remaining!")
                }
                return@launch
            }
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)

            val frozenList = if (s.frozenDates.isNotBlank()) s.frozenDates.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList() else mutableListOf()
            if (!frozenList.contains(yesterdayStr)) {
                frozenList.add(yesterdayStr)
            }
            val updated = s.copy(
                frozenDates = frozenList.joinToString(","),
                streakRepairsAvailable = (s.streakRepairsAvailable - 1).coerceAtLeast(0),
                streakShields = (s.streakShields + 1).coerceAtMost(s.maxShields)
            )
            repository.taqwaDao.insertUserStats(updated)
            repository.recalculateAndSaveStreak()
            markLocalUpdateAndSync()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun refillTaqwaShield(onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = stats.value
            val newCount = (s.streakShields + 1).coerceAtMost(2)
            val updated = s.copy(streakShields = newCount, streakChancesLeft = newCount, maxShields = 2)
            repository.taqwaDao.insertUserStats(updated)
            markLocalUpdateAndSync()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun watchAdForShield(
        activity: Activity,
        onRewardEarned: (newShieldCount: Int) -> Unit,
        onFailure: (String) -> Unit = {}
    ) {
        val s = stats.value
        if (s.streakShields >= 2) {
            onFailure("Maximum 2 Safeguard Buffers already earned!")
            return
        }

        val triggerFallback = {
            adSimulationCallback = {
                viewModelScope.launch(Dispatchers.IO) {
                    val currentStats = stats.value
                    val newCount = (currentStats.streakShields + 1).coerceAtMost(2)
                    val updated = currentStats.copy(
                        streakShields = newCount,
                        streakChancesLeft = newCount,
                        maxShields = 2
                    )
                    repository.taqwaDao.insertUserStats(updated)
                    repository.recalculateAndSaveStreak()
                    markLocalUpdateAndSync()
                    withContext(Dispatchers.Main) {
                        onRewardEarned(newCount)
                    }
                }
            }
            showAdSimulation.value = true
        }

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            "ca-app-pub-4731751183401071/9941180563", // Production Streak Safety Buffer Rewarded Ad Unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    var isRewardVerified = false

                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            if (isRewardVerified) {
                                viewModelScope.launch(Dispatchers.IO) {
                                    val currentStats = stats.value
                                    val newCount = (currentStats.streakShields + 1).coerceAtMost(2)
                                    val updated = currentStats.copy(
                                        streakShields = newCount,
                                        streakChancesLeft = newCount,
                                        maxShields = 2
                                    )
                                    repository.taqwaDao.insertUserStats(updated)
                                    repository.recalculateAndSaveStreak()
                                    markLocalUpdateAndSync()
                                    withContext(Dispatchers.Main) {
                                        onRewardEarned(newCount)
                                    }
                                }
                            } else {
                                onFailure("Ad closed before completion. Safety buffer not granted.")
                            }
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("TaqwaViewModel", "Ad failed to show: ${adError.message}. Triggering fallback...")
                            triggerFallback()
                        }
                    }

                    rewardedAd.show(activity, OnUserEarnedRewardListener {
                        isRewardVerified = true
                    })
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TaqwaViewModel", "Ad failed to load: ${loadAdError.message}. Triggering fallback...")
                    triggerFallback()
                }
            }
        )
    }

    fun watchAdForDailyXp(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onFailure: (String) -> Unit = {}
    ) {
        val todayStr = getPakistanDateString()
        val taskId = "task_daily_support_ad"

        viewModelScope.launch(Dispatchers.IO) {
            val allTimeId = "${taskId}_${todayStr}"
            val existingLogs = repository.taqwaDao.getAllTimeTasksDirect()
            val alreadyCompleted = existingLogs.any { it.id == allTimeId && it.completedAt != "MISSED" }
            if (alreadyCompleted) {
                withContext(Dispatchers.Main) {
                    onFailure("Daily Support Reward already claimed for today!")
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                val triggerFallback = {
                    adSimulationCallback = {
                        viewModelScope.launch(Dispatchers.IO) {
                            val success = repository.toggleTaskCompletion(taskId, true)
                            if (success) {
                                markLocalUpdateAndSync()
                                withContext(Dispatchers.Main) {
                                    onRewardEarned()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onFailure("Failed to record task completion.")
                                }
                            }
                        }
                    }
                    showAdSimulation.value = true
                }

                val adRequest = AdRequest.Builder().build()
                RewardedAd.load(
                    activity,
                    "ca-app-pub-4731751183401071/3888681609", // Production Daily XP Rewarded Ad Unit ID
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(rewardedAd: RewardedAd) {
                            var isRewardVerified = false

                            rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    if (isRewardVerified) {
                                        viewModelScope.launch(Dispatchers.IO) {
                                            val success = repository.toggleTaskCompletion(taskId, true)
                                            if (success) {
                                                markLocalUpdateAndSync()
                                                withContext(Dispatchers.Main) {
                                                    onRewardEarned()
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    onFailure("Failed to record task completion.")
                                                }
                                            }
                                        }
                                    } else {
                                        onFailure("Ad closed before completion. Bonus XP not granted.")
                                    }
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    Log.e("TaqwaViewModel", "Ad failed to show: ${adError.message}. Triggering fallback...")
                                    triggerFallback()
                                }
                            }

                            rewardedAd.show(activity, OnUserEarnedRewardListener {
                                isRewardVerified = true
                            })
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e("TaqwaViewModel", "Ad failed to load: ${loadAdError.message}. Triggering fallback...")
                            triggerFallback()
                        }
                    }
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerHelper.release()
        wordAudioPlayerHelper.release()
        currentUserDocListener?.remove()
        appConfigListenerRegistration?.remove()
        adminsListenerRegistration?.remove()
    }
}

data class TaskTimingStatus(
    val isLockedAdvance: Boolean,
    val isMissed: Boolean,
    val startStr: String = "",
    val endStr: String = ""
)
