package com.birthday.celebrate.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Floating music bar shown on the slideshow screen.
 * Shows album art placeholder (animated vinyl), song title, play/pause, volume slider.
 */
@Composable
fun MusicPlayerBar(
    isPlaying: Boolean,
    songTitle: String,
    volume: Float,
    hasMusic: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showVolume by remember { mutableStateOf(false) }

    // Vinyl record rotation animation
    val vinylRotation by rememberInfiniteTransition(label = "vinyl").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRot"
    )

    // Music note bounce
    val noteAnim by rememberInfiniteTransition(label = "note").animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noteBounce"
    )

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { -it } + fadeIn(),
        exit  = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1A0A2E).copy(alpha = 0.95f), Color(0xFF2D1554).copy(alpha = 0.95f))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFE84393))),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vinyl record icon (animated when playing)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .rotate(if (isPlaying) vinylRotation else 0f)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF6C5CE7), Color(0xFF2D1554), Color(0xFFE84393).copy(alpha = 0.6f))
                            ),
                            CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Centre hole of the vinyl
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF1A0A2E), CircleShape)
                    )
                }

                // Song info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasMusic) "🎵 Now Playing" else "🎵 No Music",
                        color = Color(0xFFAD7BFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = songTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Volume toggle
                IconButton(
                    onClick = { showVolume = !showVolume },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (volume > 0.5f) Icons.Default.VolumeUp
                                      else if (volume > 0f) Icons.Default.VolumeDown
                                      else Icons.Default.VolumeOff,
                        contentDescription = "Volume",
                        tint = Color(0xFFAD7BFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF6C5CE7), Color(0xFFE84393))),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Stop / remove music
                if (hasMusic) {
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove music",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Volume slider (collapsible)
            AnimatedVisibility(visible = showVolume) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = Color(0xFFAD7BFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE84393),
                            activeTrackColor = Color(0xFF6C5CE7),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFFAD7BFF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
