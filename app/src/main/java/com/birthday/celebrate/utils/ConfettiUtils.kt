package com.birthday.celebrate.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val speedX: Float,
    val speedY: Float,
    val shape: Int  // 0=circle, 1=rect, 2=diamond
)

val confettiColors = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
    Color(0xFF45B7D1), Color(0xFFFF6B9D), Color(0xFFA8E6CF),
    Color(0xFFFF8B94), Color(0xFFFFD93D), Color(0xFF6C5CE7),
    Color(0xFFAD7BFF), Color(0xFFFF9A3C), Color(0xFF00B894)
)

@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier, active: Boolean = true) {
    var particles by remember { mutableStateOf(generateParticles()) }

    LaunchedEffect(active) {
        if (active) {
            while (true) {
                delay(50)
                particles = particles.map { p ->
                    var ny = p.y + p.speedY
                    var nx = p.x + p.speedX + sin(ny * 0.05f) * 1.5f
                    if (ny > 1.1f) ny = -0.05f
                    if (nx > 1.1f) nx = -0.05f
                    if (nx < -0.1f) nx = 1.05f
                    p.copy(x = nx, y = ny, rotation = (p.rotation + 3f) % 360f)
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        if (!active) return@Canvas
        particles.forEach { p ->
            val px = p.x * size.width
            val py = p.y * size.height
            rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                when (p.shape) {
                    0 -> drawCircle(p.color, p.size, Offset(px, py))
                    1 -> drawRect(
                        p.color,
                        topLeft = Offset(px - p.size, py - p.size / 2),
                        size = androidx.compose.ui.geometry.Size(p.size * 2, p.size)
                    )
                    else -> drawCircle(p.color, p.size * 0.6f, Offset(px, py))
                }
            }
        }
    }
}

private fun generateParticles(count: Int = 60): List<ConfettiParticle> {
    return (0 until count).map {
        ConfettiParticle(
            x = Random.nextFloat(),
            y = Random.nextFloat(),
            color = confettiColors.random(),
            size = Random.nextFloat() * 10f + 5f,
            rotation = Random.nextFloat() * 360f,
            speedX = (Random.nextFloat() - 0.5f) * 0.003f,
            speedY = Random.nextFloat() * 0.006f + 0.002f,
            shape = Random.nextInt(3)
        )
    }
}
