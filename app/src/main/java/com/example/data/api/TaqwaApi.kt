package com.example.data.api

import retrofit2.http.*
import retrofit2.Response

// Generic models for Quran API
data class QuranChaptersResponse(val chapters: List<QuranChapter>)
data class QuranChapter(
    val id: Int,
    val name_simple: String,
    val name_arabic: String,
    val verses_count: Int,
    val revelation_place: String
)

data class QuranVersesResponse(val verses: List<QuranVerse>)
data class VerseAudio(
    val url: String?,
    val segments: List<List<Int>>? = null
)

data class QuranVerse(
    val id: Int,
    val verse_key: String,
    val text_uthmani: String?,
    val text_indopak: String?,
    val text_uthmani_tajweed: String? = null,
    val words: List<QuranWord>?,
    val audio: VerseAudio? = null
)

data class QuranWord(
    val id: Int,
    val position: Int,
    val text_uthmani: String?,
    val text_indopak: String?,
    val translation: QuranWordInfo?,
    val transliteration: QuranWordInfo?,
    val audio_url: String? = null,
    val char_type_name: String? = null
)

data class QuranWordInfo(val text: String?)

data class TranslationResponse(val verses: List<TranslationVerse>)
data class TranslationVerse(
    val id: Int,
    val verse_key: String,
    val translations: List<QuranTranslation>
)

data class QuranTranslation(
    val resource_id: Int,
    val text: String
)

data class TafsirResponse(val tafsirs: List<TafsirData>?)
data class TafsirData(
    val id: Int?,
    val resource_id: Int?,
    val text: String?,
    val resource_name: String?
)

data class RecitationResponse(val audio_files: List<AudioFile>)
data class AudioFile(
    val verse_key: String,
    val url: String,
    val duration: Int? = null,
    val segments: List<List<Int>>? = null
)

// Models for Aladhan Prayer Times API
data class AladhanResponse(val code: Int, val status: String, val data: AladhanData?)
<<<<<<< HEAD
data class AladhanData(val timings: AladhanTimings?)
=======
data class AladhanData(val timings: AladhanTimings?, val meta: AladhanMeta? = null)
data class AladhanMeta(val timezone: String?)
>>>>>>> 6e834ed (Update Taqwahub)
data class AladhanTimings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

// Models for Gemini API
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiSystemInstruction? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val role: String, // "user" or "model"
    val parts: List<GeminiPart>
)

data class GeminiPart(val text: String)

data class GeminiSystemInstruction(
    val parts: List<GeminiPart>
)

data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 1000
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

interface QuranApiService {
    @GET("chapters")
    suspend fun getChapters(
        @Query("language") language: String = "en"
    ): QuranChaptersResponse

    @GET("verses/by_chapter/{id}")
    suspend fun getVersesWithWords(
        @Path("id") surahId: Int,
        @Query("words") words: Boolean = true,
        @Query("fields") fields: String = "text_uthmani,text_indopak,text_uthmani_tajweed",
        @Query("word_fields") wordFields: String = "text_uthmani,text_indopak,translation,transliteration,audio_url,char_type_name",
        @Query("word_translation_language") wordTransLang: String = "en",
        @Query("per_page") perPage: Int = 300,
        @Query("audio") audio: Int? = null
    ): QuranVersesResponse

    @GET("verses/by_chapter/{id}")
    suspend fun getChapterTranslations(
        @Path("id") surahId: Int,
        @Query("translations") translations: String = "17,20,22,131,158,97,151,156", // 17 (Muhsin Khan), 20 (Saheeh), 22 (Yusuf Ali), 131 (Clear Quran)
        @Query("per_page") perPage: Int = 300
    ): TranslationResponse

    @GET("recitations/{reciter_id}/by_chapter/{id}")
    suspend fun getChapterRecitation(
        @Path("id") surahId: Int,
        @Path("reciter_id") reciterId: Int = 7, // Mishary by default
        @Query("per_page") perPage: Int = 300,
        @Query("segments") segments: Boolean = true
    ): RecitationResponse

    @GET("quran/tafsirs/{tafsir_id}")
    suspend fun getTafsir(
        @Path("tafsir_id") tafsirId: Int,
        @Query("verse_key") verseKey: String
    ): TafsirResponse
}

interface AladhanApiService {
    @GET("timings")
    suspend fun getTimings(
        @Query("latitude") lat: Double,
        @Query("longitude") lng: Double,
        @Query("method") method: Int = 2
    ): AladhanResponse
}

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

// Models for OpenRouter and Groq (OpenAI chat completions standard)
data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 1000
)

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiResponse(
    val choices: List<OpenAiChoice>?
)

data class OpenAiChoice(
    val message: OpenAiMessage?
)

interface GroqApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): Response<OpenAiResponse>
}

interface OpenRouterApiService {
    @POST("chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): Response<OpenAiResponse>
}

