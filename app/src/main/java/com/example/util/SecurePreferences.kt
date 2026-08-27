package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferences {
    private const val TAG = "SecurePreferences"
    private const val SECURE_PREFS_NAME = "TaqwaPrefs_secure"
    private const val LEGACY_PREFS_NAME = "TaqwaPrefs"
    private const val MIGRATION_COMPLETE_KEY = "prefs_secured_migration_done"

    /**
     * Initializes and returns an EncryptedSharedPreferences store.
     * Integrates automatic fallback to legacy private storage if KeyStore is unavailable.
     * Performs automatic, safe migration of pre-existing plain text preferences.
     */
    fun getSecurePrefs(context: Context): SharedPreferences {
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        
        val securePrefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences initialization failed. Falling back to plain text private preferences.", e)
            return legacyPrefs
        }

        // Execute dynamic one-time migration of plain text items to the secure sandbox
        try {
            val isMigrated = securePrefs.getBoolean(MIGRATION_COMPLETE_KEY, false)
            if (!isMigrated) {
                val legacyAll = legacyPrefs.all
                if (legacyAll.isNotEmpty()) {
                    val editor = securePrefs.edit()
                    for ((key, value) in legacyAll) {
                        if (key == MIGRATION_COMPLETE_KEY) continue
                        when (value) {
                            is Boolean -> editor.putBoolean(key, value)
                            is Float -> editor.putFloat(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is String -> editor.putString(key, value)
                        }
                    }
                    editor.putBoolean(MIGRATION_COMPLETE_KEY, true)
                    editor.apply()
                    
                    // Clear plain-text legacy entries to reduce attack surface on device
                    legacyPrefs.edit().clear().apply()
                    Log.i(TAG, "Legacy SharedPreferences successfully migrated to AES-256 Encrypted profile.")
                } else {
                    securePrefs.edit().putBoolean(MIGRATION_COMPLETE_KEY, true).apply()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered during security migration", e)
        }

        return securePrefs
    }
}
