package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

<<<<<<< HEAD
@Entity(tableName = "downloaded_hadiths", indices = [Index(value = ["bookKey"])])
=======
@Entity(tableName = "downloaded_hadiths", indices = [Index(value = ["bookKey"]), Index(value = ["bookKey", "chapterNumber"])])
>>>>>>> 6e834ed (Update Taqwahub)
data class HadithEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookKey: String,
    val hadithNumber: Int,
<<<<<<< HEAD
=======
    val bookNumber: Int = 1,
    val chapterNumber: Int = 1,
    val chapterNameEng: String = "General",
    val chapterNameAra: String = "عام",
    val chapterNameUrd: String = "عام",
>>>>>>> 6e834ed (Update Taqwahub)
    val arabic: String,
    val english: String,
    val urdu: String,
    val narrator: String,
    val chapter: String,
<<<<<<< HEAD
=======
    val grade: String = "Sahih",
>>>>>>> 6e834ed (Update Taqwahub)
    val source: String
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val completed: Boolean,
    val category: String, // "Salah" | "Dhikr" | "Reading" | "Other" | "Auto"
    val description: String = "",
    val points: Int = 10,
    val tag: String = "", // "HOT", "RECOMMENDED", "TIMER", "DAILY"
    val timerSeconds: Int = 0, // countdown timer if > 0
    val isSystemTask: Boolean = false,
    
    // Auto-task features
    val isAuto: Boolean = false,
    val autoType: String = "", // "TASBEEH", "SURAH", "HADITH", "DUA", "99_NAMES"
    val autoTarget: Int = 0, // e.g., 100 for tasbeeh, 600 for surah time (seconds)
    val autoProgress: Int = 0,
<<<<<<< HEAD
=======
    val targetSurahNumber: Int? = null, // Specific Surah number (e.g., 67 for Al-Mulk, 18 for Al-Kahf) or null for Universal tasks
>>>>>>> 6e834ed (Update Taqwahub)
    val actionRoute: String = "" // Navigation route if the user clicks the task, e.g., "tasbeeh", "quran", "dua"
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // "surahNumber_verseNumber"
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val verseKey: String,
    val timestamp: Long,
    val isFlowMode: Boolean = false
)

@Entity(tableName = "all_time_tasks")
data class AllTimeTaskEntity(
    @PrimaryKey val id: String, // "taskId_dateString"
    val taskId: String,
    val title: String,
    val category: String,
    val date: String, // "YYYY-MM-DD"
    val completedAt: String
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalTasksCompleted: Int = 0,
    val daysActive: Int = 1,
    val quranProgress: Int = 0,
    val lastReadSurah: Int = 1,
    val lastReadVerse: Int = 1,
    val lastReadVerseKey: String = "1:1",
    val tasbeehCount: Int = 0,
    val lastResetDate: String = "",
    val currentStreak: Int = 0,
<<<<<<< HEAD
    val name: String = "Servant of Allah",
    val username: String = "",
    val gender: String = "Male",
    val sectOrCast: String = "Sunni",
    val email: String = "",
    val streakChancesLeft: Int = 2,
=======
    val name: String = "",
    val username: String = "",
    val gender: String = "",
    val sectOrCast: String = "",
    val email: String = "",
    val streakChancesLeft: Int = 0,
>>>>>>> 6e834ed (Update Taqwahub)
    val longestStreak: Int = 0,
    val totalXp: Int = 0,
    val weeklyXp: Int = 0,
    val lastActiveWeekOfYear: Int = 0,
    val completedSurahs: String = "",
    val firstPlaceCount: Int = 0,
    val secondPlaceCount: Int = 0,
    val thirdPlaceCount: Int = 0,
    val isBlocked: Boolean = false,
    val isVerified: Boolean = false,
    val profilePictureBase64: String = "",
    val lastWeekXp: Int = 0,
<<<<<<< HEAD
    val lastWeekCode: Int = 0
=======
    val lastWeekCode: Int = 0,
    val lastActiveDate: String = "",
    val streakShields: Int = 0,
    val maxShields: Int = 2,
    val frozenDates: String = "",
    val activeDates: String = "",
    val lastShieldUsedDate: String = "",
    val streakRepairsAvailable: Int = 1
>>>>>>> 6e834ed (Update Taqwahub)
)
