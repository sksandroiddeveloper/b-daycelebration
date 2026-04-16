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
 * MusicController — bridges ViewModel ↔ MusicService.
 *
 * Lifecycle:
 *  bind()   called in Activity.onStart()
 *  unbind() called in Activity.onStop()
 *
 * The Service itself keeps running (foreground) after unbind — music
 * continues in background. Re-binding on next onStart() re-syncs state.
 */
class MusicController(private val context: Context) {

    private var service: MusicService? = null
    private var bound = false

    private val _isPlaying  = MutableStateFlow(false)
    private val _songTitle  = MutableStateFlow("No song selected")
    private val _musicUri   = MutableStateFlow<String?>(null)
    private val _volume     = MutableStateFlow(0.8f)

    val isPlaying : StateFlow<Boolean>  = _isPlaying.asStateFlow()
    val songTitle : StateFlow<String>   = _songTitle.asStateFlow()
    val musicUri  : StateFlow<String?>  = _musicUri.asStateFlow()
    val volume    : StateFlow<Float>    = _volume.asStateFlow()

    val hasMusic: Boolean get() = _musicUri.value != null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as MusicService.MusicBinder).getService()
            bound   = true
            // Sync UI state from the running service (e.g., after rotation)
            _isPlaying.value = service?.isPlaying ?: false
            _songTitle.value = service?.songTitle ?: "No song selected"
            _musicUri.value  = service?.currentMusicUri
            // Get live updates from service
            service?.onStateChanged = { playing, title ->
                _isPlaying.value = playing
                _songTitle.value = title
                // If service stopped itself (Close button), clear URI
                if (!playing && title == "No song selected") _musicUri.value = null
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null; bound = false
        }
    }

    fun bind() {
        val intent = Intent(context, MusicService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (bound) {
            service?.onStateChanged = null
            context.unbindService(connection)
            bound = false
        }
    }

    fun playMusic(uri: String, title: String) {
        _musicUri.value  = uri
        _songTitle.value = title
        service?.playMusic(uri, title)
    }

    fun togglePlayPause() = service?.togglePlayPause()

    /** Stop music, remove notification, clear state. */
    fun stopMusic() {
        service?.stopAndDismiss()
        _musicUri.value  = null
        _isPlaying.value = false
        _songTitle.value = "No song selected"
    }

    fun setVolume(vol: Float) {
        _volume.value = vol
        service?.setVolume(vol)
    }
}
