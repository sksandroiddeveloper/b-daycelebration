package com.birthday.celebrate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.birthday.celebrate.data.BirthdayViewModel
import com.birthday.celebrate.music.MusicPlayerBar
import com.birthday.celebrate.ui.components.BirthdaySlideCard
import kotlinx.coroutines.delay

@Composable
fun SlideshowScreen(
    viewModel: BirthdayViewModel,
    onEditClick: () -> Unit
) {
    val data           by viewModel.birthdayData.collectAsStateWithLifecycle()
    val currentIndex   by viewModel.currentSlideIndex.collectAsStateWithLifecycle()
    val isPlaying      by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val musicTitle     by viewModel.musicTitle.collectAsStateWithLifecycle()
    val musicVolume    by viewModel.musicVolume.collectAsStateWithLifecycle()

    var dragOffset by remember { mutableStateOf(0f) }
    var goForward  by remember { mutableStateOf(true) }

    // Auto-play timer
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(3500)
            goForward = true
            viewModel.nextSlide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffset < -80f -> { goForward = true;  viewModel.nextSlide() }
                            dragOffset > 80f  -> { goForward = false; viewModel.prevSlide() }
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragOffset += delta }
                )
            }
    ) {
        // ── Slide content ──────────────────────────────────────────
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                if (goForward)
                    slideInHorizontally { it }  + fadeIn(tween(300)) togetherWith slideOutHorizontally { -it } + fadeOut(tween(300))
                else
                    slideInHorizontally { -it } + fadeIn(tween(300)) togetherWith slideOutHorizontally { it }  + fadeOut(tween(300))
            },
            label = "slideshow",
            modifier = Modifier.fillMaxSize()
        ) { index ->
            val slides = data.slides
            if (slides.isNotEmpty() && index < slides.size) {
                BirthdaySlideCard(
                    slide = slides[index],
                    childName = data.childName,
                    age = data.age,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ── 🎵 Music bar — top of screen ──────────────────────────
        MusicPlayerBar(
            isPlaying      = isMusicPlaying,
            songTitle      = musicTitle,
            volume         = musicVolume,
            hasMusic       = data.musicUri != null,
            onPlayPause    = { viewModel.toggleMusicPlayPause() },
            onStop         = { viewModel.stopMusic() },
            onVolumeChange = { viewModel.setMusicVolume(it) },
            modifier       = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        )

        // ── Slide counter — top right ──────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 100.dp, end = 16.dp)   // below music bar
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("${currentIndex + 1} / ${data.slides.size}", color = Color.White)
        }

        // ── Control bar — bottom ───────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { goForward = false; viewModel.prevSlide() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = Color.White)
            }
            IconButton(
                onClick = { viewModel.togglePlaying() },
                modifier = Modifier
                    .size(52.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { goForward = true; viewModel.nextSlide() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
        }
    }
}
