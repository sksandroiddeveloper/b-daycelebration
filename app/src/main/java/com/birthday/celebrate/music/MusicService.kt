package com.birthday.celebrate.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.birthday.celebrate.MainActivity

/**
 * Foreground Service that keeps music playing even when the screen is active.
 * Uses MediaPlayer for local file playback from phone storage.
 * Bound service — Activity binds to it to control play/pause/stop.
 */
class MusicService : Service() {

    // ── Binder so Activity can call methods directly ──────────────
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    var isPlaying: Boolean = false
        private set

    var currentMusicUri: String? = null
        private set

    var songTitle: String = "No song selected"
        private set

    // Callback so ViewModel can react to state changes
    var onStateChanged: ((isPlaying: Boolean, title: String) -> Unit)? = null

    // ── Service lifecycle ─────────────────────────────────────────
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_STOP -> stopMusic()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    // ── Public API (called from ViewModel via bound service) ──────

    /**
     * Load a music URI from device storage and start playing.
     * uri: content://media/external/audio/... from the system music picker
     */
    fun playMusic(uri: String, title: String) {
        currentMusicUri = uri
        songTitle = title
        releasePlayer()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(this@MusicService, Uri.parse(uri))
                isLooping = true          // Loop music throughout the slideshow
                prepare()
                start()
            }
            isPlaying = true
            startForeground(NOTIFICATION_ID, buildNotification(songTitle, true))
            onStateChanged?.invoke(true, songTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
            onStateChanged?.invoke(false, "Error loading song")
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            isPlaying = false
        } else {
            player.start()
            isPlaying = true
        }
        // Update notification button icon
        updateNotification(songTitle, isPlaying)
        onStateChanged?.invoke(isPlaying, songTitle)
    }

    fun stopMusic() {
        releasePlayer()
        isPlaying = false
        currentMusicUri = null
        songTitle = "No song selected"
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        onStateChanged?.invoke(false, "No song selected")
    }

    fun setVolume(volume: Float) {
        // volume: 0.0f (silent) to 1.0f (full)
        mediaPlayer?.setVolume(volume, volume)
    }

    private fun releasePlayer() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    // ── Notification (required for foreground service) ────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Birthday Music",
            NotificationManager.IMPORTANCE_LOW   // LOW = silent, no popup
        ).apply {
            description = "Shows currently playing birthday song"
            setSound(null, null)
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, playing: Boolean): Notification {
        val activityIntent = Intent(this, MainActivity::class.java)
        val contentPi = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePi = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPi = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎂 Birthday Celebrate")
            .setContentText("🎵 $title")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentPi)
            .addAction(
                if (playing) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play,
                if (playing) "Pause" else "Play",
                playPausePi
            )
            .addAction(android.R.drawable.ic_delete, "Stop", stopPi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(title: String, playing: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(title, playing))
    }

    companion object {
        const val CHANNEL_ID = "birthday_music_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY_PAUSE = "com.birthday.celebrate.PLAY_PAUSE"
        const val ACTION_STOP = "com.birthday.celebrate.STOP"
    }
}
