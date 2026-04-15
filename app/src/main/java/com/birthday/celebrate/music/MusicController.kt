package com.birthday.celebrate.music

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MusicController handles the ServiceConnection lifecycle.
 * ViewModel creates this, Activity keeps it alive.
 * Exposes StateFlows so Compose UI reacts automatically.
 */
class MusicController(private val context: Context) {

    private var musicService: MusicService? = null
    private var bound = false

    // ── Exposed state (ViewModel/UI observes these) ───────────────
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _songTitle = MutableStateFlow("No song selected")
    val songTitle: StateFlow<String> = _songTitle.asStateFlow()

    private val _musicUri = MutableStateFlow<String?>(null)
    val musicUri: StateFlow<String?> = _musicUri.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    // ── ServiceConnection ─────────────────────────────────────────
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            bound = true

            // Sync state from service (e.g., after config change)
            _isPlaying.value = musicService?.isPlaying ?: false
            _songTitle.value = musicService?.songTitle ?: "No song selected"
            _musicUri.value = musicService?.currentMusicUri

            // Register callback so service notifies us of changes
            musicService?.onStateChanged = { playing, title ->
                _isPlaying.value = playing
                _songTitle.value = title
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            bound = false
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────
    fun bind() {
        val intent = Intent(context, MusicService::class.java)
        context.startService(intent)   // Start so it persists if unbound briefly
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (bound) {
            musicService?.onStateChanged = null
            context.unbindService(connection)
            bound = false
        }
    }

    // ── Controls (called from ViewModel) ─────────────────────────

    fun playMusic(uri: String, title: String) {
        _musicUri.value = uri
        _songTitle.value = title
        musicService?.playMusic(uri, title)
    }

    fun togglePlayPause() {
        musicService?.togglePlayPause()
    }

    fun stopMusic() {
        _musicUri.value = null
        musicService?.stopMusic()
    }

    fun setVolume(volume: Float) {
        _volume.value = volume
        musicService?.setVolume(volume)
    }

    val hasMusic: Boolean get() = _musicUri.value != null
}
