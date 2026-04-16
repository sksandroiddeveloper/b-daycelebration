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

/**
 * MusicService — Background-capable music playback.
 *
 * Behaviour:
 *  • When playing  → runs as a FOREGROUND service with a persistent notification
 *                    that shows song title + Play/Pause + ✕ CLOSE buttons.
 *  • When paused   → stays foreground so the OS doesn't kill it, but user can
 *                    tap ✕ in the notification to fully stop & dismiss it.
 *  • When stopped  → removes foreground, removes notification, stops itself.
 *
 * The notification CLOSE button calls ACTION_STOP which:
 *   1. Stops MediaPlayer
 *   2. Calls stopForeground(STOP_FOREGROUND_REMOVE) → removes notification
 *   3. Calls stopSelf() → service is fully gone
 */
class MusicService : Service() {

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private val binder      = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    var isPlaying: Boolean      = false ; private set
    var currentMusicUri: String? = null ; private set
    var songTitle: String       = "No song selected" ; private set

    var onStateChanged: ((isPlaying: Boolean, title: String) -> Unit)? = null

    // ── Lifecycle ────────────────────────────────────────────────
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_STOP       -> stopAndDismiss()   // ← notification ✕ button
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    // ── Public API ───────────────────────────────────────────────

    /** Start playing a song from device storage URI. Shows foreground notification. */
    fun playMusic(uri: String, title: String) {
        currentMusicUri = uri
        songTitle       = title
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
                isLooping = true
                prepare()
                start()
            }
            isPlaying = true
            // Promote to foreground so Android doesn't kill us
            startForeground(NOTIFICATION_ID, buildNotification(songTitle, true))
            onStateChanged?.invoke(true, songTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
            onStateChanged?.invoke(false, "Error loading song")
        }
    }

    /** Toggle play / pause. Updates the notification button icon. */
    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            isPlaying = false
            // Stay foreground while paused so OS doesn't kill service,
            // but update notification to show Play button
            updateNotification(songTitle, false)
        } else {
            player.start()
            isPlaying = true
            updateNotification(songTitle, true)
        }
        onStateChanged?.invoke(isPlaying, songTitle)
    }

    /**
     * Fully stop music, remove the foreground notification, and destroy the service.
     * Called from:
     *   • The ✕ CLOSE button in the notification
     *   • ViewModel.stopMusic() from the UI
     */
    fun stopAndDismiss() {
        releasePlayer()
        isPlaying       = false
        currentMusicUri = null
        songTitle       = "No song selected"

        // Remove notification AND stop foreground in one call
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()                          // Service is now fully gone

        onStateChanged?.invoke(false, "No song selected")
    }

    /** Legacy alias kept so ViewModel.stopMusic() still compiles. */
    fun stopMusic() = stopAndDismiss()

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun releasePlayer() {
        mediaPlayer?.apply {
            try { if (isPlaying) stop() } catch (_: Exception) {}
            reset()
            release()
        }
        mediaPlayer = null
    }

    // ── Notification ─────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Birthday Music",
            NotificationManager.IMPORTANCE_LOW   // silent — no heads-up popup
        ).apply {
            description = "Birthday background music controls"
            setSound(null, null)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(title: String, playing: Boolean): Notification {
        // Tap notification → open app
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, com.birthday.celebrate.MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play / Pause button
        val playPausePi = PendingIntent.getService(
            this, 1,
            Intent(this, MusicService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✕ CLOSE/STOP button — fully stops service and removes notification
        val stopPi = PendingIntent.getService(
            this, 2,
            Intent(this, MusicService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎂 Birthday Celebrate")
            .setContentText(if (playing) "🎵 $title" else "⏸ $title (paused)")
            .setSubText("Birthday Music")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openApp)
            // ── Action 1: Play or Pause ──────────────────────────
            .addAction(
                NotificationCompat.Action.Builder(
                    if (playing) android.R.drawable.ic_media_pause
                    else         android.R.drawable.ic_media_play,
                    if (playing) "Pause" else "Play",
                    playPausePi
                ).build()
            )
            // ── Action 2: ✕ Close / Stop ────────────────────────
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Close",          // shown as "Close" under notification
                    stopPi
                ).build()
            )
            // Ongoing = user cannot swipe-dismiss (must tap Close button)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(title: String, playing: Boolean) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(title, playing))
    }

    companion object {
        const val CHANNEL_ID        = "birthday_music_channel"
        const val NOTIFICATION_ID   = 1001
        const val ACTION_PLAY_PAUSE = "com.birthday.celebrate.ACTION_PLAY_PAUSE"
        const val ACTION_STOP       = "com.birthday.celebrate.ACTION_STOP"
    }
}
