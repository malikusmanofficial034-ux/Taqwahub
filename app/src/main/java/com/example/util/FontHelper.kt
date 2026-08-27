package com.example.util

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
<<<<<<< HEAD
import androidx.compose.ui.text.font.FontFamily
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
=======
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
>>>>>>> 6e834ed (Update Taqwahub)

object FontHelper {
    private const val TAG = "FontHelper"

<<<<<<< HEAD
    // Dynamic state variables. Jetpack Compose will automatically recompose whenever these change.
=======
    // Authentic Quranic fonts with safe default fallbacks
>>>>>>> 6e834ed (Update Taqwahub)
    var uthmaniFontFamily: FontFamily by mutableStateOf(FontFamily.Serif)
        private set

    var indopakFontFamily: FontFamily by mutableStateOf(FontFamily.Serif)
        private set

<<<<<<< HEAD
=======
    var cinzelFontFamily: FontFamily by mutableStateOf(FontFamily.Serif)
        private set

>>>>>>> 6e834ed (Update Taqwahub)
    var isDownloading by mutableStateOf(false)
        private set

    /**
<<<<<<< HEAD
     * Initializes pre-cached fonts or starts background download for the beautiful Quranic fonts.
     */
    fun checkAndLoadFonts(context: Context) {
        val fontDir = File(context.filesDir, "quran_fonts")
        if (!fontDir.exists()) {
            fontDir.mkdirs()
        }

        val amiriFile = File(fontDir, "Amiri-Regular.ttf")
        val scheherazadeFile = File(fontDir, "ScheheradeNew-Regular.ttf")

        var loadedUthmani = false
        var loadedIndopak = false

        // 1. Try loading cached fonts immediately
        try {
            if (amiriFile.exists() && amiriFile.length() > 50000) {
                val tf = Typeface.createFromFile(amiriFile)
                uthmaniFontFamily = FontFamily(tf)
                loadedUthmani = true
                Log.d(TAG, "Successfully loaded cached Amiri font natively.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading cached Amiri: ${e.message}")
        }

        try {
            if (scheherazadeFile.exists() && scheherazadeFile.length() > 50000) {
                val tf = Typeface.createFromFile(scheherazadeFile)
                indopakFontFamily = FontFamily(tf)
                loadedIndopak = true
                Log.d(TAG, "Successfully loaded cached Scheherazade New font natively.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading cached Scheherazade: ${e.message}")
        }

        // 2. If any of the fonts are missing, initiate background download
        if (!loadedUthmani || !loadedIndopak) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    isDownloading = true
                    
                    if (!loadedUthmani) {
                        Log.d(TAG, "Amiri font is missing. Downloading...")
                        downloadFontFile(
                            "https://github.com/google/fonts/raw/main/ofl/amiri/Amiri-Regular.ttf",
                            amiriFile
                        )
                        if (amiriFile.exists() && amiriFile.length() > 50000) {
                            val tf = Typeface.createFromFile(amiriFile)
                            uthmaniFontFamily = FontFamily(tf)
                            Log.d(TAG, "Amiri font loaded after download success.")
                        }
                    }

                    if (!loadedIndopak) {
                        Log.d(TAG, "Scheherazade New font is missing. Downloading...")
                        // Public stable Raw release repository URL from Google Fonts raw GitHub CDN
                        downloadFontFile(
                            "https://github.com/google/fonts/raw/main/ofl/scheherazadenew/ScheherazadeNew-Regular.ttf",
                            scheherazadeFile
                        )
                        if (scheherazadeFile.exists() && scheherazadeFile.length() > 50000) {
                            val tf = Typeface.createFromFile(scheherazadeFile)
                            indopakFontFamily = FontFamily(tf)
                            Log.d(TAG, "Scheherazade New font loaded after download success.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in fonts loading / downloading flow: ${e.message}")
                } finally {
                    isDownloading = false
                }
            }
        }
    }

    private fun downloadFontFile(urlString: String, destinationFile: File) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.instanceFollowRedirects = true
            conn.useCaches = false
            conn.connect()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Downloaded font file size: ${destinationFile.length()} bytes")
            } else {
                Log.e(TAG, "Failed downloading font. HTTP status code: ${conn.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download connection error: ${e.message}")
            if (destinationFile.exists()) {
                destinationFile.delete() // cleanup bad download
            }
        } finally {
            conn?.disconnect()
=======
     * Initializes bundled Quranic fonts immediately from assets with 100% offline availability.
     * Uses Typeface.createFromAsset to guarantee in-memory typeface resolution and prevent
     * runtime Compose layout font resolution exceptions.
     */
    fun checkAndLoadFonts(context: Context) {
        // 1. Load Amiri Quran font (Uthmani) from assets
        try {
            val amiriTf = Typeface.createFromAsset(context.assets, "fonts/amiri_quran.ttf")
            uthmaniFontFamily = FontFamily(amiriTf)
            Log.d(TAG, "Successfully loaded asset Amiri Quran font.")
        } catch (ex: Throwable) {
            Log.w(TAG, "Failed loading bundled Amiri Quran font: ${ex.message}. Using Serif fallback.")
            uthmaniFontFamily = FontFamily.Serif
        }

        // 2. Load Scheherazade New font (IndoPak) from assets
        try {
            val scheherazadeTf = Typeface.createFromAsset(context.assets, "fonts/scheherazade_new.ttf")
            indopakFontFamily = FontFamily(scheherazadeTf)
            Log.d(TAG, "Successfully loaded asset Scheherazade New font.")
        } catch (ex: Throwable) {
            Log.w(TAG, "Failed loading bundled Scheherazade New font: ${ex.message}. Using Serif fallback.")
            indopakFontFamily = FontFamily.Serif
        }

        // 3. Load Cinzel Bold font from assets
        try {
            val cinzelTf = Typeface.createFromAsset(context.assets, "fonts/cinzel_bold.ttf")
            cinzelFontFamily = FontFamily(cinzelTf)
            Log.d(TAG, "Successfully loaded asset Cinzel Bold font.")
        } catch (ex: Throwable) {
            Log.w(TAG, "Failed loading bundled Cinzel Bold font: ${ex.message}. Using Serif fallback.")
            cinzelFontFamily = FontFamily.Serif
>>>>>>> 6e834ed (Update Taqwahub)
        }
    }

    /**
     * Resolves beautiful fonts depending on whether user toggles indopak or uthmani script style.
     */
    fun getFontForScript(script: String): FontFamily {
        return if (script == "indopak") indopakFontFamily else uthmaniFontFamily
    }
<<<<<<< HEAD
=======

    /**
     * Creates a robust TextStyle optimized for Arabic scripts with proper BiDi direction,
     * GPOS glyph vertical alignment, and unclipped diacritic padding.
     */
    @Suppress("DEPRECATION")
    fun getArabicTextStyle(
        script: String = "uthmani",
        fontSize: TextUnit = 22.sp,
        lineHeight: TextUnit = (fontSize.value * 2.1f).sp,
        color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White
    ): TextStyle {
        return TextStyle(
            color = color,
            fontSize = fontSize,
            fontFamily = getFontForScript(script),
            lineHeight = lineHeight,
            textDirection = TextDirection.Rtl,
            platformStyle = PlatformTextStyle(
                includeFontPadding = true
            ),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
            )
        )
    }

    /**
     * Wraps Arabic text in Unicode Right-To-Left Isolate (RLI ... PDI) markers
     * to prevent Android Bidi shaper from flipping symbols, brackets, or numbers.
     */
    fun formatArabicText(text: String): String {
        if (text.isBlank()) return text
        val rli = "\u2067" // Right-To-Left Isolate
        val pdi = "\u2069" // Pop Directional Isolate
        return if (!text.startsWith(rli)) "$rli$text$pdi" else text
    }
}


fun toArabicDigits(str: String): String {
    return str.map { char ->
        when (char) {
            '0' -> '٠'
            '1' -> '١'
            '2' -> '٢'
            '3' -> '٣'
            '4' -> '٤'
            '5' -> '٥'
            '6' -> '٦'
            '7' -> '٧'
            '8' -> '٨'
            '9' -> '٩'
            else -> char
        }
    }.joinToString("")
>>>>>>> 6e834ed (Update Taqwahub)
}
