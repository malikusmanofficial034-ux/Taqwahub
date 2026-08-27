package com.example.util

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ConcurrentHashMap

class AudioPlayerHelper(private val context: Context) {
    private val mediaContext: Context = context
    private var mediaPlayer: MediaPlayer? = null
    
    private val _currentlyPlayingUrl = MutableStateFlow<String?>(null)
    val currentlyPlayingUrl: StateFlow<String?> = _currentlyPlayingUrl

    private val _playbackProgressMs = MutableStateFlow<Int>(0)
    val playbackProgressMs: StateFlow<Int> = _playbackProgressMs

    private val _playbackDurationMs = MutableStateFlow<Int>(0)
    val playbackDurationMs: StateFlow<Int> = _playbackDurationMs

    private val _isAudioPlaying = MutableStateFlow<Boolean>(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying

    private var currentPlayingUrlToken: String? = null
    private var currentSessionId: Long = 0L
    var onTrackFinished: (() -> Unit)? = null

    // Prefetching and caching thread safety
    private val prefetchExecutor = Executors.newFixedThreadPool(4)
    private val currentlyDownloading = ConcurrentHashMap.newKeySet<String>()

    fun resolveUrl(url: String): String {
        var resolved = url.trim()
        return when {
            resolved.startsWith("http://") -> resolved.replace("http://", "https://")
            resolved.startsWith("https://") -> resolved
            resolved.startsWith("//") -> "https:$resolved"
            resolved.contains("everyayah.com") -> "https://$resolved"
            resolved.contains("quranicaudio.com") -> "https://$resolved"
            else -> {
                val prefix = if (resolved.startsWith("/")) "https://audio.qurancdn.com" else "https://audio.qurancdn.com/"
                "$prefix$resolved"
            }
        }
    }

    fun getCacheFileName(resolvedUrl: String): String {
        return resolvedUrl.replace("https://", "").replace("http://", "").replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".mp3"
    }

    fun getLocalFile(url: String): File? {
        val resolvedUrl = resolveUrl(url)
        val fileName = getCacheFileName(resolvedUrl)
        if (fileName.isEmpty()) return null

        // 1. External Download folder
        val extDir = File(context.getExternalFilesDir(null), "Taqwahub/downloads")
        val extFile = File(extDir, fileName)
        if (extFile.exists() && extFile.length() > 0) return extFile

        // 2. Internal Download folder
        val intDir = File(context.filesDir, "Taqwahub/downloads")
        val intFile = File(intDir, fileName)
        if (intFile.exists() && intFile.length() > 0) return intFile

        // 3. Dynamic Audio Cache folder
        val cacheDir = File(context.cacheDir, "TaqwaAudioCache_v2")
        val cacheFile = File(cacheDir, fileName)
        if (cacheFile.exists() && cacheFile.length() > 0) return cacheFile

        // 4. Public Downloads folder
        try {
            val publicDir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "Taqwahub")
            val publicFile = File(publicDir, fileName)
            if (publicFile.exists() && publicFile.length() > 0) return publicFile
        } catch (e: Throwable) {}

        // 5. Legacy surah_downloads folder
        val surahDir = File(context.filesDir, "surah_downloads")
        val surahFile = File(surahDir, fileName)
        if (surahFile.exists() && surahFile.length() > 0) return surahFile

        return null
    }

    fun prefetch(urls: List<String>) {
        for (url in urls) {
            val resolvedUrl = resolveUrl(url)
            val fileName = getCacheFileName(resolvedUrl)
            if (fileName.isEmpty()) continue

            // Skip if already in local cache
            val localFile = getLocalFile(url)
            if (localFile != null && localFile.exists() && localFile.length() > 0) {
                continue
            }

            // Skip if downloading currently
            if (currentlyDownloading.contains(resolvedUrl)) {
                continue
            }

            currentlyDownloading.add(resolvedUrl)
            prefetchExecutor.execute {
                try {
                    downloadToCache(resolvedUrl)
                } catch (e: Exception) {
                    Log.e("AudioPlayerHelper", "Prefetch failed for $resolvedUrl: ${e.message}")
                } finally {
                    currentlyDownloading.remove(resolvedUrl)
                }
            }
        }
    }

    fun playAudio(url: String, startMs: Int? = null, endMs: Int? = null, playbackToken: String = url) {
        val resolvedUrl = resolveUrl(url)
        Log.d("AudioPlayerHelper", "Requested playing: $url (start: $startMs, end: $endMs) -> Resolved: $resolvedUrl")
        
        val sessionId = ++currentSessionId
        currentPlayingUrlToken = playbackToken
        _currentlyPlayingUrl.value = playbackToken
        _isAudioPlaying.value = false

        // Check if file is cached locally
        val localFile = getLocalFile(url)
        if (localFile != null) {
            Log.d("AudioPlayerHelper", "Local file found! Playing instantly: ${localFile.absolutePath}")
            playLocalFile(localFile.absolutePath, playbackToken, startMs, endMs, sessionId)
        } else {
            Log.d("AudioPlayerHelper", "No local cache. Direct-streaming instantly for 0s lag while buffering background cache...")
            // 1. Start direct streaming immediately on Foreground Player so user hears sound within milliseconds
            playDirectStreamUrl(resolvedUrl, playbackToken, startMs, endMs, sessionId)

            // 2. Download to cache concurrently in background so next time we read from disk
            if (!currentlyDownloading.contains(resolvedUrl)) {
                currentlyDownloading.add(resolvedUrl)
                prefetchExecutor.execute {
                    try {
                        downloadToCache(resolvedUrl)
                    } catch (e: Exception) {
                        Log.e("AudioPlayerHelper", "Concurrently download-to-cache failed for $resolvedUrl: ${e.message}")
                    } finally {
                        currentlyDownloading.remove(resolvedUrl)
                    }
                }
            }
        }
    }

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private fun runOnMainThread(action: () -> Unit) {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    private var stopHandler: android.os.Handler? = null
    private var stopRunnable: Runnable? = null

    private fun scheduleStop(mediaPlayer: MediaPlayer, endMs: Int, sessionId: Long) {
        if (stopHandler == null) {
            stopHandler = android.os.Handler(android.os.Looper.getMainLooper())
        }
        stopRunnable?.let { stopHandler?.removeCallbacks(it) }
        
        val runnable = Runnable {
            try {
                if (currentSessionId == sessionId && mediaPlayer.isPlaying && mediaPlayer.currentPosition >= endMs) {
                    stop()
                    onTrackFinished?.invoke()
                } else if (currentSessionId == sessionId && mediaPlayer.isPlaying) {
                    // Check again in 20ms if we haven't reached endMs
                    stopRunnable?.let { stopHandler?.postDelayed(it, 20) }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        stopRunnable = runnable
        // Start polling a bit earlier
        stopHandler?.postDelayed(runnable, 50)
    }

    private var completionPollRunnable: Runnable? = null

    private fun startCompletionPolling(player: MediaPlayer, sessionId: Long) {
        completionPollRunnable?.let { mainHandler.removeCallbacks(it) }
        var consecutiveNotPlayingCount = 0
        val runnable = object : Runnable {
            override fun run() {
                try {
                    if (currentSessionId == sessionId && mediaPlayer == player) {
                        if (!player.isPlaying) {
                            consecutiveNotPlayingCount++
                            _isAudioPlaying.value = false
                            if (consecutiveNotPlayingCount > 10) { // grace of ~300ms
                                val wasNearEnd = player.duration > 0 && player.currentPosition >= (player.duration - 800)
                                Log.d("AudioPlayerHelper", "Polling detected player stopped. Near end: $wasNearEnd")
                                stop()
                                if (wasNearEnd) {
                                    onTrackFinished?.invoke()
                                }
                            } else {
                                mainHandler.postDelayed(this, 30)
                            }
                        } else {
                            consecutiveNotPlayingCount = 0
                            _isAudioPlaying.value = true
                            _playbackProgressMs.value = player.currentPosition
                            _playbackDurationMs.value = player.duration
                            mainHandler.postDelayed(this, 30)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("AudioPlayerHelper", "Polling exception (player likely released or errored). Stopping.")
                    if (currentSessionId == sessionId) {
                        stop()
                    }
                }
            }
        }
        completionPollRunnable = runnable
        mainHandler.postDelayed(runnable, 100)
    }

    private fun playLocalFile(filePath: String, originalUrl: String, startMs: Int? = null, endMs: Int? = null, sessionId: Long) {
        runOnMainThread {
            try {
                stopOnly()
                if (currentSessionId != sessionId) return@runOnMainThread
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(mediaContext, android.net.Uri.fromFile(java.io.File(filePath)))
                    setOnPreparedListener {
                        if (currentSessionId != sessionId) {
                            try { release() } catch (e: Exception) {}
                            return@setOnPreparedListener
                        }
                        start()
                        if (startMs != null && startMs > 0) {
                            try {
                                seekTo(startMs)
                            } catch (se: Exception) {
                                Log.e("AudioPlayerHelper", "Local play prepare seek failed: ${se.message}")
                            }
                        }
                        _currentlyPlayingUrl.value = originalUrl
                        _isAudioPlaying.value = true
                        if (endMs != null) {
                            scheduleStop(this, endMs, sessionId)
                        }
                        startCompletionPolling(this, sessionId)
                    }
                    setOnCompletionListener {
                        if (currentSessionId == sessionId) {
                            stop()
                            onTrackFinished?.invoke()
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("AudioPlayerHelper", "MediaPlayer local file play error: what=$what, extra=$extra. Retrying with streaming...")
                        if (currentSessionId == sessionId) {
                            playDirectStreamUrl(resolveUrl(originalUrl), originalUrl, startMs, endMs, sessionId)
                        }
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerHelper", "Error playing local file: $filePath", e)
                if (currentSessionId == sessionId) {
                    playDirectStreamUrl(resolveUrl(originalUrl), originalUrl, startMs, endMs, sessionId)
                }
            }
        }
    }

    private fun playDirectStreamUrl(streamUrl: String, originalUrl: String, startMs: Int? = null, endMs: Int? = null, sessionId: Long) {
        runOnMainThread {
            try {
                stopOnly()
                if (currentSessionId != sessionId) return@runOnMainThread
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(streamUrl)
                    setOnPreparedListener {
                        if (currentSessionId != sessionId) {
                            try { release() } catch (e: Exception) {}
                            return@setOnPreparedListener
                        }
                        start()
                        if (startMs != null && startMs > 0) {
                            try {
                                seekTo(startMs)
                            } catch (se: Exception) {
                                Log.e("AudioPlayerHelper", "Stream play prepare seek failed: ${se.message}")
                            }
                        }
                        _currentlyPlayingUrl.value = originalUrl
                        _isAudioPlaying.value = true
                        if (endMs != null) {
                            scheduleStop(this, endMs, sessionId)
                        }
                        startCompletionPolling(this, sessionId)
                    }
                    setOnCompletionListener {
                        if (currentSessionId == sessionId) {
                            stop()
                            onTrackFinished?.invoke()
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("AudioPlayerHelper", "MediaPlayer streaming error: what=$what, extra=$extra")
                        if (currentSessionId == sessionId) {
                            stop()
                        }
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerHelper", "Error playing streaming URL: $streamUrl", e)
                if (currentSessionId == sessionId) {
                    stop()
                }
            }
        }
    }

    private fun downloadToCache(resolvedUrl: String): File? {
        var connection: HttpURLConnection? = null
        try {
            val fileName = getCacheFileName(resolvedUrl)
            if (fileName.isEmpty()) return null

            val cacheDir = File(context.cacheDir, "TaqwaAudioCache_v2")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val cacheFile = File(cacheDir, fileName)
            val tempFile = File(cacheDir, "$fileName.tmp")

            if (tempFile.exists()) {
                tempFile.delete()
            }

            val urlObj = URL(resolvedUrl)
            connection = urlObj.openConnection() as HttpURLConnection
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.useCaches = true
            connection.connect()

            if (connection.responseCode !in 200..299) {
                Log.e("AudioPlayerHelper", "Server returned HTTP error: ${connection.responseCode} ${connection.responseMessage}")
                return null
            }

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
                tempFile.renameTo(cacheFile)
                return cacheFile
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Failed downloading $resolvedUrl to local cache: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun stopOnly() {
        try {
            stopRunnable?.let { stopHandler?.removeCallbacks(it) }
            completionPollRunnable?.let { mainHandler.removeCallbacks(it) }
            _playbackProgressMs.value = 0
            _playbackDurationMs.value = 0
            _isAudioPlaying.value = false
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "Error stopping player only", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun stop() {
        runOnMainThread {
            currentSessionId++
            currentPlayingUrlToken = null
            stopOnly()
            _currentlyPlayingUrl.value = null
        }
    }

    fun seekTo(ms: Int) {
        runOnMainThread {
            try {
                mediaPlayer?.let { player ->
                    player.seekTo(ms)
                    _playbackProgressMs.value = ms
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerHelper", "Error seeking player to $ms", e)
            }
        }
    }

    fun release() {
        stop()
    }
}
