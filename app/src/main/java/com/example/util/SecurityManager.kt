package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.lang.StringBuilder
import java.security.MessageDigest

object SecurityManager {

    private const val TAG = "SecurityManager"

    // SHA-256 fingerprint constants of the official keys in upper case, colon-separated format
    const val RELEASE_SHA256 = "10:53:98:40:9A:82:E7:91:D7:27:8D:69:1A:43:F5:73:68:A6:E4:31:E5:2C:84:CD:42:80:20:05:26:88:C8:34"
    const val DEBUG_SHA256 = "A6:DD:13:AF:10:AB:C9:D0:CA:52:08:A2:68:34:C2:E2:23:82:BD:F8:97:B9:1A:2F:FE:15:6F:73:FC:96:1D:46"

    // Valid package names
    private val VALID_PACKAGE_IDS = listOf("com.taqwahub.app", "com.example")

    /**
     * Verifies the app signature matches the official release or debug credentials.
     * Prevents repackaged, decompiled, or modified code modifications.
     */
    fun checkAppSignature(context: Context): SignatureCheckResult {
        try {
            val packageName = context.packageName
            val packageManager = context.packageManager

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                return SignatureCheckResult(
                    isMatch = false,
                    fingerprint = "No signature found",
                    isOfficialRelease = false,
                    isOfficialDebug = false
                )
            }

            // Get first signature SHA-256
            val signature = signatures[0]
            val certBytes = signature.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(certBytes)
            val computedFingerprint = bytesToHexWithColons(digest)

            val isOfficialRelease = computedFingerprint.equals(RELEASE_SHA256, ignoreCase = true)
            val isOfficialDebug = computedFingerprint.equals(DEBUG_SHA256, ignoreCase = true)
            val isMatch = isOfficialRelease || isOfficialDebug

            return SignatureCheckResult(
                isMatch = isMatch,
                fingerprint = computedFingerprint,
                isOfficialRelease = isOfficialRelease,
                isOfficialDebug = isOfficialDebug
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking signatures", e)
            return SignatureCheckResult(
                isMatch = false,
                fingerprint = "Error: ${e.message}",
                isOfficialRelease = false,
                isOfficialDebug = false
            )
        }
    }

    /**
     * Verifies the runtime package ID has not been customized or spoofed.
     */
    fun checkPackageName(context: Context): PackageCheckResult {
        val packageName = context.packageName
        val isExpected = VALID_PACKAGE_IDS.contains(packageName)
        return PackageCheckResult(
            isExpected = isExpected,
            actualPackage = packageName
        )
    }

    /**
     * Diagnostic check for Root access. Look for common root binaries.
     */
    fun checkRootAccess(): RootCheckResult {
        val rootPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        var hasSuBinary = false
        var foundPath = ""
        for (path in rootPaths) {
            try {
                if (File(path).exists()) {
                    hasSuBinary = true
                    foundPath = path
                    break
                }
            } catch (e: Throwable) {
                // Ignore security exceptions
            }
        }

        val buildTags = Build.TAGS
        val hasTestKeys = buildTags != null && buildTags.contains("test-keys")

        // Runtime executor command su verification
        var canExecuteSu = false
        try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val exitValue = process.waitFor()
            if (exitValue == 0) {
                canExecuteSu = true
            }
        } catch (e: Throwable) {
            // ignore
        }

        val isRooted = hasSuBinary || hasTestKeys || canExecuteSu

        return RootCheckResult(
            isRooted = isRooted,
            hasSuBinary = hasSuBinary,
            foundPath = foundPath,
            hasTestKeys = hasTestKeys,
            canExecuteSu = canExecuteSu
        )
    }

    /**
     * Detects if the app is currently running inside an Android emulator.
     */
    fun checkEmulator(): EmulatorCheckResult {
        val finger = Build.FINGERPRINT ?: "unknown"
        val model = Build.MODEL ?: "unknown"
        val hardware = Build.HARDWARE ?: "unknown"
        
        var isEmulator = false
        try {
            isEmulator = (finger.startsWith("generic")
                    || finger.startsWith("unknown")
                    || model.contains("google_sdk")
                    || model.contains("Emulator")
                    || model.contains("Android SDK built for x86")
                    || hardware.contains("goldfish")
                    || hardware.contains("ranchu")
                    || (Build.BRAND ?: "unknown").startsWith("generic") && (Build.DEVICE ?: "unknown").startsWith("generic")
                    || "google_sdk" == Build.PRODUCT)
        } catch (e: Throwable) {
            // ignore
        }

        return EmulatorCheckResult(
            isEmulator = isEmulator,
            fingerprint = finger,
            model = model,
            hardware = hardware
        )
    }

    private fun bytesToHexWithColons(bytes: ByteArray): String {
        val builder = StringBuilder()
        for (i in bytes.indices) {
            val byteStr = String.format("%02X", bytes[i])
            builder.append(byteStr)
            if (i < bytes.size - 1) {
                builder.append(":")
            }
        }
        return builder.toString()
    }
}

data class SignatureCheckResult(
    val isMatch: Boolean,
    val fingerprint: String,
    val isOfficialRelease: Boolean,
    val isOfficialDebug: Boolean
)

data class PackageCheckResult(
    val isExpected: Boolean,
    val actualPackage: String
)

data class RootCheckResult(
    val isRooted: Boolean,
    val hasSuBinary: Boolean,
    val foundPath: String,
    val hasTestKeys: Boolean,
    val canExecuteSu: Boolean
)

data class EmulatorCheckResult(
    val isEmulator: Boolean,
    val fingerprint: String,
    val model: String,
    val hardware: String
)
