package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"

    sealed class UpdateState {
        object Idle : UpdateState()
        object Checking : UpdateState()
        data class UpdateAvailable(
            val versionCode: Int,
            val versionName: String,
            val downloadUrl: String,
            val changelog: String
        ) : UpdateState()
        object UpToDate : UpdateState()
        data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : UpdateState()
        data class ReadyToInstall(val file: File) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    private val client = OkHttpClient()

    /**
     * Helper to compare if the current version is lower than the required/target version.
     * Supports both integer version codes and dotted version names (e.g. "1.0.4").
     */
    fun isVersionLower(context: Context, required: String): Boolean {
        val cleanRequired = required.replace("v", "").trim()
        if (cleanRequired.isEmpty()) return false

        val currentName = getCurrentVersionName(context)
        val currentCode = getCurrentVersionCode(context)

        // If required is a pure integer, compare against current version code!
        val reqInt = cleanRequired.toIntOrNull()
        if (reqInt != null) {
            return currentCode < reqInt
        }

        // Otherwise, compare as semantic version names component-wise
        val cleanCurrentName = currentName.replace("v", "").trim()
        val currentParts = cleanCurrentName.split(".").mapNotNull { it.toIntOrNull() }
        val requiredParts = cleanRequired.split(".").mapNotNull { it.toIntOrNull() }

        if (currentParts.isEmpty() || requiredParts.isEmpty()) {
            return cleanCurrentName < cleanRequired
        }

        val maxLength = maxOf(currentParts.size, requiredParts.size)
        for (i in 0 until maxLength) {
            val currVal = currentParts.getOrNull(i) ?: 0
            val reqVal = requiredParts.getOrNull(i) ?: 0
            if (currVal < reqVal) return true
            if (currVal > reqVal) return false
        }
        return false
    }

    /**
     * Checks if a newer version is available.
     * Compares the app's current version against the target version.
     */
    fun checkUpdate(context: Context, targetVersion: String, downloadUrl: String) {
        val currentName = getCurrentVersionName(context)
        val currentCode = getCurrentVersionCode(context)
        Log.d(TAG, "Checking update: current=$currentName ($currentCode), target=$targetVersion")
        
        if (isVersionLower(context, targetVersion)) {
            _updateState.value = UpdateState.UpdateAvailable(
                versionCode = targetVersion.replace(".", "").toIntOrNull() ?: currentCode,
                versionName = if (targetVersion.contains(".")) targetVersion else "v$targetVersion",
                downloadUrl = downloadUrl,
                changelog = "This critical update contains security enhancements, stability improvements, and new features."
            )
        } else {
            _updateState.value = UpdateState.UpToDate
        }
    }

    /**
     * Downloads the APK file to the application's secure cache directory.
     * Provides real-time downloading progress.
     */
    suspend fun downloadApk(context: Context, downloadUrl: String) {
        _updateState.value = UpdateState.Downloading(0f, 0L, 0L)
        
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(downloadUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        _updateState.value = UpdateState.Error("Server returned code ${response.code}")
                        return@withContext
                    }
                    
                    val body = response.body
                    if (body == null) {
                        _updateState.value = UpdateState.Error("Response body is empty")
                        return@withContext
                    }
                    
                    val totalBytes = body.contentLength()
                    // Store APK safely in internal cache (no external permission required)
                    val apkFile = File(context.cacheDir, "taqwahub_update.apk")
                    if (apkFile.exists()) {
                        apkFile.delete()
                    }
                    
                    body.byteStream().use { input ->
                        apkFile.outputStream().use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var totalRead = 0L
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalRead += bytesRead
                                
                                val progress = if (totalBytes > 0) totalRead.toFloat() / totalBytes else 0f
                                _updateState.value = UpdateState.Downloading(progress, totalRead, totalBytes)
                            }
                        }
                    }
                    
                    Log.d(TAG, "APK download completed: ${apkFile.absolutePath} (${apkFile.length()} bytes)")
                    _updateState.value = UpdateState.ReadyToInstall(apkFile)
                }
            } catch (e: IOException) {
                Log.e(TAG, "APK download failed", e)
                _updateState.value = UpdateState.Error("Download failed: ${e.localizedMessage}")
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during APK download", e)
                _updateState.value = UpdateState.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Prompts the system to install the downloaded APK file.
     * Uses FileProvider to securely grant temporary permissions to the system installer.
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Toast.makeText(context, "Update file not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (!canRequestPackageInstalls(context)) {
            Toast.makeText(context, "Please grant permission to install updates", Toast.LENGTH_LONG).show()
            openInstallPermissionSettings(context)
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Installation prompt failed", e)
            Toast.makeText(context, "Failed to start installer: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens the Google Play Store app listing page directly or custom URL if provided in Admin Panel.
     */
    fun openPlayStore(context: Context, customUrl: String? = null) {
        val targetUrl = customUrl?.trim()
        if (!targetUrl.isNullOrBlank() && (targetUrl.startsWith("http://") || targetUrl.startsWith("https://") || targetUrl.startsWith("market://"))) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch custom update URL: $targetUrl", e)
            }
        }

        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Helper to check if the app has permission to install unknown apps (Android 8.0+)
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens the system settings screen for granting unknown app install permission
     */
    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open install settings", e)
                Toast.makeText(context, "Please enable unknown app installations in Settings", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Resets the update state back to Idle
     */
    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    /**
     * Fetches the current application version code dynamically.
     */
    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Fetches the current application version name dynamically.
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
