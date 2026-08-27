package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object HadithBookService {

    data class DownloadedHadith(
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
        val source: String
    )

=======
        val grade: String = "Sahih",
        val source: String
    )

    data class HadithChapter(
        val chapterNumber: Int,
        val nameEng: String,
        val nameAra: String,
        val nameUrd: String,
        val hadithCount: Int,
        val startHadithNumber: Int,
        val endHadithNumber: Int
    )

    fun sanitizeText(text: String): String {
        if (text.isBlank()) return ""
        return text
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\(\\s*ﷺ\\s*\\)"), " ﷺ ")
            .replace(Regex("\\(\\s*pbuh\\s*\\)", RegexOption.IGNORE_CASE), " ﷺ ")
            .replace(Regex("\\(\\s*S\\.A\\.W\\.?\\s*\\)", RegexOption.IGNORE_CASE), " ﷺ ")
            .replace(Regex("صلى الله عليه وسلم"), " ﷺ ")
            .replace(Regex("  +"), " ")
            .trim()
    }

>>>>>>> 6e834ed (Update Taqwahub)
    // Book key to display name with approximate counts
    val supportedBooks = listOf(
        "bukhari" to "1. Sahih al-Bukhari (~7563)",
        "abudawud" to "2. Sunan Abu Dawud (~5274)",
        "tirmidhi" to "3. Jami` at-Tirmidhi (~3956)",
        "nasai" to "4. Sunan an-Nasai (~5758)",
        "ibnmajah" to "5. Sunan Ibn Majah (~4341)"
    )

    private suspend fun checkAndMigrateHadithCache(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val sharedPrefs = context.getSharedPreferences("taqwa_prefs", Context.MODE_PRIVATE)
                val cacheVersion = sharedPrefs.getInt("hadith_cache_version_v2", 0)
                if (cacheVersion < 1) {
                    val db = com.example.data.room.TaqwaDatabase.getDatabase(context)
                    supportedBooks.forEach { (bookKey, _) ->
                        try {
                            db.taqwaDao().clearHadithsForBook(bookKey)
                        } catch (e: Exception) {
                            Log.e("HadithBookService", "Error clearing book key $bookKey during migration", e)
                        }
                        try {
                            File(context.filesDir, "hadith_${bookKey}_eng.json").delete()
                            File(context.filesDir, "hadith_${bookKey}_ara.json").delete()
                            File(context.filesDir, "hadith_${bookKey}_urd.json").delete()
                        } catch (e: Exception) {
                            Log.e("HadithBookService", "Error deleting local json files for $bookKey", e)
                        }
                    }
                    sharedPrefs.edit().putInt("hadith_cache_version_v2", 1).apply()
                    Log.i("HadithBookService", "Migrated hadith cache to v2 successfully")
                }
            } catch (e: Exception) {
                Log.e("HadithBookService", "Failed to run checkAndMigrateHadithCache", e)
            }
        }
    }

    suspend fun isBookDownloaded(context: Context, bookKey: String): Boolean {
        checkAndMigrateHadithCache(context)
        return withContext(Dispatchers.IO) {
            try {
                val db = com.example.data.room.TaqwaDatabase.getDatabase(context)
                val count = db.taqwaDao().getHadithCountForBook(bookKey)
                if (count > 0) {
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.e("HadithBookService", "Error checking DB status for book $bookKey", e)
            }
            val engFile = File(context.filesDir, "hadith_${bookKey}_eng.json")
            val araFile = File(context.filesDir, "hadith_${bookKey}_ara.json")
            engFile.exists() && araFile.exists() && engFile.length() > 100L && araFile.length() > 100L
        }
    }

    suspend fun downloadBook(context: Context, bookKey: String) {
        checkAndMigrateHadithCache(context)
        withContext(Dispatchers.IO) {
            val engPreferred = "eng-${bookKey}"
            val araPreferred = "ara-${bookKey}"
            val urdPreferred = "urd-${bookKey}"

            val engUrls = listOf(
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/${engPreferred}.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-${bookKey}.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-${bookKey}1.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-${bookKey}2.min.json"
            ).distinct()

            val araUrls = listOf(
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/${araPreferred}.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-${bookKey}.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-${bookKey}1.min.json"
            ).distinct()

            val urdUrls = listOf(
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/${urdPreferred}.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-${bookKey}.min.json",
                "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-${bookKey}1.min.json"
            ).distinct()

            downloadFileWithFallback(engUrls, File(context.filesDir, "hadith_${bookKey}_eng.json"))
            downloadFileWithFallback(araUrls, File(context.filesDir, "hadith_${bookKey}_ara.json"))
            try {
                downloadFileWithFallback(urdUrls, File(context.filesDir, "hadith_${bookKey}_urd.json"))
            } catch (e: Exception) {
                // If urdu fails, still keep English and Arabic so we don't crash, but log it
                Log.w("HadithBookService", "Urdu fallback download failed for $bookKey, continuing with English/Arabic only", e)
            }
        }
    }

    private fun downloadFileWithFallback(urls: List<String>, destFile: File) {
        var lastError: Exception? = null
        for (urlString in urls) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 60000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (destFile.exists() && destFile.length() > 200L) {
                        return // Success!
                    }
                } else {
                    lastError = Exception("HTTP response code $responseCode")
                }
            } catch (e: Exception) {
                lastError = e
                Log.w("HadithBookService", "Failed download attempt for URL $urlString: ${e.message}")
            }
        }
        if (destFile.exists()) {
            destFile.delete()
        }
        throw lastError ?: Exception("Failed to download from all URL options")
    }

    private fun downloadFile(urlString: String, destFile: File) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 60000

            connection.inputStream.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("HadithBookService", "Failed to download $urlString", e)
            if (destFile.exists()) {
                destFile.delete()
            }
            throw e
        }
    }

    private data class EngHadithTemp(
        val hadithNumStr: String,
        val hadithNumInt: Int,
        val text: String,
<<<<<<< HEAD
        val chapter: String
    )

=======
        val chapterNumber: Int,
        val chapterName: String,
        val grade: String
    )

    private fun parseMetadataSections(file: File): Map<Int, Pair<String, Pair<Int, Int>>> {
        val map = mutableMapOf<Int, Pair<String, Pair<Int, Int>>>()
        if (!file.exists() || file.length() < 100L) return map
        try {
            val json = JSONObject(file.readText())
            if (json.has("metadata")) {
                val meta = json.getJSONObject("metadata")
                val sections = if (meta.has("sections")) meta.optJSONObject("sections") else null
                val details = if (meta.has("section_details")) meta.optJSONObject("section_details") else null

                if (sections != null) {
                    val keys = sections.keys()
                    while (keys.hasNext()) {
                        val keyStr = keys.next()
                        val chNum = keyStr.toIntOrNull() ?: continue
                        val title = sections.optString(keyStr, "Chapter $chNum")
                        var startNum = 0
                        var endNum = 0
                        if (details != null && details.has(keyStr)) {
                            val detailObj = details.optJSONObject(keyStr)
                            if (detailObj != null) {
                                startNum = detailObj.optInt("hadithnumber_first", 0)
                                endNum = detailObj.optInt("hadithnumber_last", 0)
                            }
                        }
                        map[chNum] = Pair(title, Pair(startNum, endNum))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HadithBookService", "Error parsing metadata sections for ${file.name}", e)
        }
        return map
    }

>>>>>>> 6e834ed (Update Taqwahub)
    private fun parseHadithTextMap(file: File): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (!file.exists() || file.length() < 100L) return map
        try {
            val reader = android.util.JsonReader(file.reader())
            reader.beginObject()
            var hadithsFound = false
            while (reader.hasNext()) {
                if (reader.nextName() == "hadiths") {
                    hadithsFound = true
                    reader.beginArray()
                    break
                } else {
                    reader.skipValue()
                }
            }
            if (hadithsFound) {
                while (reader.hasNext()) {
                    var hadithNumStr = ""
                    var textVal = ""
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "hadithnumber" -> {
                                val token = reader.peek()
                                hadithNumStr = when (token) {
                                    android.util.JsonToken.NUMBER -> {
                                        val num = reader.nextDouble()
                                        if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
                                    }
                                    android.util.JsonToken.STRING -> {
                                        val s = reader.nextString()
                                        val d = s.toDoubleOrNull()
                                        if (d != null && d % 1.0 == 0.0) d.toInt().toString() else s
                                    }
                                    else -> {
                                        reader.skipValue()
                                        ""
                                    }
                                }
                            }
                            "text" -> {
                                if (reader.peek() == android.util.JsonToken.STRING) {
<<<<<<< HEAD
                                    textVal = reader.nextString()
=======
                                    textVal = sanitizeText(reader.nextString())
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    reader.skipValue()
                                }
                            }
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                    if (hadithNumStr.isNotEmpty()) {
                        map[hadithNumStr] = textVal
                    }
                }
                reader.endArray()
            }
            reader.close()
<<<<<<< HEAD
        } catch (e: java.lang.Exception) {
=======
        } catch (e: Exception) {
>>>>>>> 6e834ed (Update Taqwahub)
            Log.e("HadithBookService", "Error parsing map for file ${file.name}", e)
        }
        return map
    }

<<<<<<< HEAD
    private fun parseEnglishHadiths(file: File): List<EngHadithTemp> {
        val list = mutableListOf<EngHadithTemp>()
        if (!file.exists() || file.length() < 100L) return list
        try {
            val reader = android.util.JsonReader(file.reader())
            reader.beginObject()
            var hadithsFound = false
            while (reader.hasNext()) {
                if (reader.nextName() == "hadiths") {
                    hadithsFound = true
                    reader.beginArray()
                    break
                } else {
                    reader.skipValue()
                }
            }
            if (hadithsFound) {
                while (reader.hasNext()) {
                    var hadithNumStr = ""
                    var hadithNumInt = 0
                    var textVal = ""
                    var chapterName = "Book 1"
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "hadithnumber" -> {
                                val token = reader.peek()
                                if (token == android.util.JsonToken.NUMBER) {
                                    val num = reader.nextDouble()
                                    hadithNumInt = num.toInt()
                                    hadithNumStr = if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
                                } else if (token == android.util.JsonToken.STRING) {
                                    val s = reader.nextString()
                                    val d = s.toDoubleOrNull()
                                    hadithNumInt = d?.toInt() ?: 0
                                    hadithNumStr = if (d != null && d % 1.0 == 0.0) d.toInt().toString() else s
                                } else {
                                    reader.skipValue()
                                }
                            }
                            "text" -> {
                                if (reader.peek() == android.util.JsonToken.STRING) {
                                    textVal = reader.nextString()
                                } else {
                                    reader.skipValue()
                                }
                            }
                            "reference" -> {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    if (reader.nextName() == "book") {
                                        val token = reader.peek()
                                        chapterName = if (token == android.util.JsonToken.NUMBER) {
                                            "Book ${reader.nextInt()}"
                                        } else if (token == android.util.JsonToken.STRING) {
                                            reader.nextString()
                                        } else {
                                            reader.skipValue()
                                            "Book 1"
                                        }
                                    } else {
                                        reader.skipValue()
                                    }
                                }
                                reader.endObject()
                            }
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                    if (hadithNumStr.isNotEmpty()) {
                        list.add(EngHadithTemp(hadithNumStr, hadithNumInt, textVal, chapterName))
                    }
                }
                reader.endArray()
            }
            reader.close()
=======
    private fun parseEnglishHadiths(file: File, defaultBookName: String): List<EngHadithTemp> {
        val list = mutableListOf<EngHadithTemp>()
        if (!file.exists() || file.length() < 100L) return list
        try {
            val fileText = file.readText()
            val rootObj = JSONObject(fileText)
            val hadithsArr = rootObj.optJSONArray("hadiths") ?: return list

            for (i in 0 until hadithsArr.length()) {
                val hObj = hadithsArr.optJSONObject(i) ?: continue
                val hadithNumInt = hObj.optInt("hadithnumber", 0)
                val hadithNumStr = if (hadithNumInt > 0) hadithNumInt.toString() else hObj.optString("hadithnumber", "")
                val rawText = hObj.optString("text", "")
                val cleanedText = sanitizeText(rawText)

                var chapterNum = 1
                var chapterName = "General"
                val refObj = hObj.optJSONObject("reference")
                if (refObj != null && refObj.has("book")) {
                    chapterNum = refObj.optInt("book", 1)
                    if (chapterNum <= 0) chapterNum = 1
                    chapterName = "Book $chapterNum"
                }

                // Evaluate Authenticity Grade
                var grade = if (defaultBookName.contains("Bukhari", ignoreCase = true) || defaultBookName.contains("Muslim", ignoreCase = true)) "Sahih" else "Hasan"
                val gradesArr = hObj.optJSONArray("grades")
                if (gradesArr != null && gradesArr.length() > 0) {
                    var foundGrade = ""
                    for (g in 0 until gradesArr.length()) {
                        val gObj = gradesArr.optJSONObject(g) ?: continue
                        val gText = gObj.optString("grade", "")
                        if (gText.contains("Sahih", ignoreCase = true)) {
                            foundGrade = "Sahih"
                            break
                        } else if (gText.contains("Hasan", ignoreCase = true)) {
                            foundGrade = "Hasan"
                        } else if (gText.contains("Daif", ignoreCase = true) || gText.contains("Da'if", ignoreCase = true) || gText.contains("Weak", ignoreCase = true)) {
                            foundGrade = "Da'if"
                        }
                    }
                    if (foundGrade.isNotEmpty()) grade = foundGrade
                }

                if (hadithNumStr.isNotEmpty()) {
                    list.add(EngHadithTemp(hadithNumStr, hadithNumInt, cleanedText, chapterNum, chapterName, grade))
                }
            }
>>>>>>> 6e834ed (Update Taqwahub)
        } catch (e: Exception) {
            Log.e("HadithBookService", "Error parsing English file", e)
        }
        return list
    }

    suspend fun loadBook(context: Context, bookKey: String, bookName: String): List<DownloadedHadith> {
        checkAndMigrateHadithCache(context)
        return withContext(Dispatchers.IO) {
            try {
                // Check database first!
                val db = com.example.data.room.TaqwaDatabase.getDatabase(context)
                val cached = db.taqwaDao().getHadithsForBook(bookKey)
                if (cached.isNotEmpty()) {
                    val blankCount = cached.count { it.arabic.isBlank() || it.english.isBlank() }
                    val isCorrupted = blankCount > cached.size * 0.5
                    if (!isCorrupted) {
                        return@withContext cached.map {
                            DownloadedHadith(
                                hadithNumber = it.hadithNumber,
<<<<<<< HEAD
                                arabic = it.arabic,
                                english = it.english,
                                urdu = it.urdu,
                                narrator = it.narrator,
                                chapter = it.chapter,
=======
                                bookNumber = it.bookNumber,
                                chapterNumber = it.chapterNumber,
                                chapterNameEng = sanitizeText(it.chapterNameEng),
                                chapterNameAra = sanitizeText(it.chapterNameAra),
                                chapterNameUrd = sanitizeText(it.chapterNameUrd),
                                arabic = sanitizeText(it.arabic),
                                english = sanitizeText(it.english),
                                urdu = sanitizeText(it.urdu),
                                narrator = sanitizeText(it.narrator),
                                chapter = sanitizeText(it.chapter),
                                grade = if (it.grade.isBlank()) "Sahih" else it.grade,
>>>>>>> 6e834ed (Update Taqwahub)
                                source = it.source
                            )
                        }
                    } else {
                        Log.w("HadithBookService", "Cache for book $bookKey contains mostly blank content or wrong size ($blankCount / ${cached.size}). Clearing and triggering reload...")
                        db.taqwaDao().clearHadithsForBook(bookKey)
                    }
                }

                // If not in database, compile from files if they exist
                val engFile = File(context.filesDir, "hadith_${bookKey}_eng.json")
                val araFile = File(context.filesDir, "hadith_${bookKey}_ara.json")
                val urdFile = File(context.filesDir, "hadith_${bookKey}_urd.json")

                if (!engFile.exists() || !araFile.exists() || engFile.length() < 100L || araFile.length() < 100L) {
                    return@withContext emptyList()
                }

<<<<<<< HEAD
                val engList = parseEnglishHadiths(engFile)
=======
                val engSections = parseMetadataSections(engFile)
                val araSections = parseMetadataSections(araFile)
                val urdSections = parseMetadataSections(urdFile)

                val engList = parseEnglishHadiths(engFile, bookName)
>>>>>>> 6e834ed (Update Taqwahub)
                val araMap = parseHadithTextMap(araFile)
                val urdMap = parseHadithTextMap(urdFile)

                val resultList = ArrayList<DownloadedHadith>(engList.size)
                for (eng in engList) {
                    val araText = araMap[eng.hadithNumStr] ?: ""
                    val urdText = urdMap[eng.hadithNumStr] ?: ""

<<<<<<< HEAD
=======
                    // Chapter details
                    val chNum = eng.chapterNumber
                    val chNameEng = engSections[chNum]?.first ?: "Book $chNum"
                    val chNameAra = araSections[chNum]?.first ?: "كتاب $chNum"
                    val chNameUrd = urdSections[chNum]?.first ?: "باب $chNum"

>>>>>>> 6e834ed (Update Taqwahub)
                    // Extract Narrator
                    var narrator = "Unknown"
                    var cleanedEngText = eng.text
                    val narratedRegex = Regex("(?i)^(Narrated by|Narrated)\\s+([^:]+):\\s*(.*)")
                    val match = narratedRegex.find(eng.text)
                    if (match != null) {
                        narrator = match.groupValues[2].trim()
                        cleanedEngText = match.groupValues[3].trim()
                    }

                    resultList.add(
                        DownloadedHadith(
                            hadithNumber = eng.hadithNumInt,
<<<<<<< HEAD
                            arabic = araText,
                            english = cleanedEngText,
                            urdu = urdText,
                            narrator = narrator,
                            chapter = eng.chapter,
=======
                            bookNumber = 1,
                            chapterNumber = chNum,
                            chapterNameEng = sanitizeText(chNameEng),
                            chapterNameAra = sanitizeText(chNameAra),
                            chapterNameUrd = sanitizeText(chNameUrd),
                            arabic = sanitizeText(araText),
                            english = sanitizeText(cleanedEngText),
                            urdu = sanitizeText(urdText),
                            narrator = sanitizeText(narrator),
                            chapter = sanitizeText(chNameEng),
                            grade = eng.grade,
>>>>>>> 6e834ed (Update Taqwahub)
                            source = bookName
                        )
                    )
                }

                if (resultList.isNotEmpty()) {
                    // Export compile list to Room table as entities in background chunks
                    val dbEntities = resultList.map {
                        com.example.data.room.HadithEntity(
                            bookKey = bookKey,
                            hadithNumber = it.hadithNumber,
<<<<<<< HEAD
=======
                            bookNumber = it.bookNumber,
                            chapterNumber = it.chapterNumber,
                            chapterNameEng = it.chapterNameEng,
                            chapterNameAra = it.chapterNameAra,
                            chapterNameUrd = it.chapterNameUrd,
>>>>>>> 6e834ed (Update Taqwahub)
                            arabic = it.arabic,
                            english = it.english,
                            urdu = it.urdu,
                            narrator = it.narrator,
<<<<<<< HEAD
                            chapter = it.chapter,
=======
                            chapter = it.chapterNameEng,
                            grade = it.grade,
>>>>>>> 6e834ed (Update Taqwahub)
                            source = it.source
                        )
                    }
                    db.taqwaDao().clearHadithsForBook(bookKey)
                    dbEntities.chunked(1000).forEach { chunk ->
                        db.taqwaDao().insertHadiths(chunk)
                    }

<<<<<<< HEAD
                    // Delete the large temporary files immediately to keep storage minimal!
=======
                    // Delete temporary json files
>>>>>>> 6e834ed (Update Taqwahub)
                    try {
                        engFile.delete()
                        araFile.delete()
                        if (urdFile.exists()) {
                            urdFile.delete()
                        }
                    } catch (e: Exception) {
                        Log.e("HadithBookService", "Failed to clean up temp downloads files", e)
                    }
                }

                resultList
            } catch (e: Exception) {
                Log.e("HadithBookService", "Failed to load book $bookKey", e)
                emptyList()
            }
        }
    }
<<<<<<< HEAD
=======

    fun extractChapters(hadiths: List<DownloadedHadith>): List<HadithChapter> {
        if (hadiths.isEmpty()) return emptyList()
        return hadiths
            .groupBy { it.chapterNumber }
            .map { (chNum, list) ->
                val first = list.first()
                HadithChapter(
                    chapterNumber = chNum,
                    nameEng = first.chapterNameEng,
                    nameAra = first.chapterNameAra,
                    nameUrd = first.chapterNameUrd,
                    hadithCount = list.size,
                    startHadithNumber = list.minOf { it.hadithNumber },
                    endHadithNumber = list.maxOf { it.hadithNumber }
                )
            }
            .sortedBy { it.chapterNumber }
    }
>>>>>>> 6e834ed (Update Taqwahub)
}
