package com.birthday.celebrate.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.birthday.celebrate.music.MusicController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BirthdayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BirthdayRepository(application)

    // 🎵 Music controller — manages service binding and playback
    val musicController = MusicController(application)

    private val _birthdayData = MutableStateFlow(BirthdayData())
    val birthdayData: StateFlow<BirthdayData> = _birthdayData.asStateFlow()

    private val _currentSlideIndex = MutableStateFlow(0)
    val currentSlideIndex: StateFlow<Int> = _currentSlideIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Expose music state from controller for UI
    val isMusicPlaying = musicController.isPlaying
    val musicTitle     = musicController.songTitle
    val musicVolume    = musicController.volume

    init {
        viewModelScope.launch {
            repository.birthdayDataFlow.collect { data ->
                _birthdayData.value = data
                // If a music URI was previously saved, restore it to controller
                if (data.musicUri != null && !musicController.hasMusic) {
                    musicController.playMusic(data.musicUri, data.musicTitle)
                }
            }
        }
    }

    // ── Slideshow controls ────────────────────────────────────────
    fun nextSlide() {
        val total = _birthdayData.value.slides.size
        _currentSlideIndex.value = (_currentSlideIndex.value + 1) % total
    }

    fun prevSlide() {
        val total = _birthdayData.value.slides.size
        _currentSlideIndex.value = if (_currentSlideIndex.value == 0) total - 1
                                   else _currentSlideIndex.value - 1
    }

    fun togglePlaying() { _isPlaying.value = !_isPlaying.value }

    fun navigateToSlide(index: Int) {
        if (index in 0 until _birthdayData.value.slides.size)
            _currentSlideIndex.value = index
    }

    // ── Data updates ──────────────────────────────────────────────
    fun updateChildName(name: String)        = viewModelScope.launch { repository.updateChildName(name) }
    fun updateAge(age: Int)                  = viewModelScope.launch { repository.updateAge(age) }
    fun updateParentMessage(msg: String)     = viewModelScope.launch { repository.updateParentMessage(msg) }
    fun updateSlidePhoto(id: Int, uri: String?) = viewModelScope.launch { repository.updateSlidePhoto(id, uri) }
    fun updateSlideMessage(id: Int, msg: String) = viewModelScope.launch { repository.updateSlideMessage(id, msg) }
    fun resetToDefaults()                    = viewModelScope.launch { repository.resetToDefaults() }

    // ── 🎵 Music controls ─────────────────────────────────────────
    fun pickMusic(uri: String, title: String) {
        musicController.playMusic(uri, title)
        // Save to DataStore so it restores on next app open
        viewModelScope.launch { repository.updateMusic(uri, title) }
    }

    fun toggleMusicPlayPause() = musicController.togglePlayPause()

    fun stopMusic() {
        musicController.stopMusic()
        viewModelScope.launch { repository.updateMusic(null, "No song selected") }
    }

    fun setMusicVolume(volume: Float) = musicController.setVolume(volume)

    // ── Cleanup ───────────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        // Don't stop music here — user may still want it after config change
        // MusicService keeps running independently
    }
}
