package com.example.data

data class Surah(
    val id: Int,
    val name: String,
    val nameArabic: String,
    val versesCount: Int,
    val revelationType: String
)

data class Hadith(
    val id: Int,
    val chapter: String,
    val narrator: String,
    val source: String,
    val text: String,
    val arabic: String,
    val translationUrdu: String,
    val transliteration: String
)

data class Dua(
    val id: String,
    val category: String,
    val reference: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val translationUrdu: String
)

data class NameOfAllah(
    val id: Int,
    val name: String, // Arabic
    val englishName: String,
    val meaning: String,
    val meaningUrdu: String = ""
)

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "Announcement", "Update", "Reminder"
    val timestamp: Long
)

data class BugReport(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val subject: String = "",
    val description: String = "",
    val type: String = "Bug", // "Bug", "Suggestion", "Other"
    val status: String = "Pending", // "Pending", "In Progress", "Resolved"
    val timestamp: Long = 0,
    val appVersion: String = "",
    val deviceModel: String = "",
    val imageUrl: String = "",
    val adminReply: String = "",
    val adminReplyTimestamp: Long = 0L
)

data class AiFeedback(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val query: String = "",
    val response: String = "",
    val rating: String = "none", // "like", "dislike", "none"
    val reportMessage: String = "",
    val timestamp: Long = 0
)



