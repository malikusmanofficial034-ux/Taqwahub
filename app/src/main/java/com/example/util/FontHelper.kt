package com.example.util

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object FontHelper {
    private const val TAG = "FontHelper"

    // Authentic Quranic fonts with safe default fallbacks
    var uthmaniFontFamily: FontFamily by mutableStateOf(FontFamily.Serif)
        private set

    var indopakFontFamily: FontFamily by mutableStateOf(FontFamily.Serif)
        private set

    var cinzelFontFamily: FontFamily by mutableStateOf(FontFamily.Serif)
        private set

    var isDownloading by mutableStateOf(false)
        private set

    /**
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
        }
    }

    /**
     * Resolves beautiful fonts depending on whether user toggles indopak or uthmani script style.
     */
    fun getFontForScript(script: String): FontFamily {
        return if (script == "indopak") indopakFontFamily else uthmaniFontFamily
    }

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
}
