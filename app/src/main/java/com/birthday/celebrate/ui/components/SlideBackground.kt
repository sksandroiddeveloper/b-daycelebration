package com.birthday.celebrate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.birthday.celebrate.data.BackgroundType
import com.birthday.celebrate.data.BirthdaySlide
import com.birthday.celebrate.ui.theme.toComposeColor
import kotlin.math.*

@Composable
fun SlideBackground(slide: BirthdaySlide, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val bg = slide.backgroundColor.toComposeColor()
    val accent = slide.accentColor.toComposeColor()

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgAnim"
    )
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                when (slide.backgroundType) {
                    BackgroundType.GRADIENT -> drawGradientBackground(bg, accent, animValue)
                    BackgroundType.SPARKLE -> drawSparkleBackground(bg, accent, waveAnim)
                    BackgroundType.PATTERN -> drawPatternBackground(bg, accent, waveAnim)
                    BackgroundType.SOLID -> drawRect(bg)
                }
            }
    ) {
        content()
    }
}

private fun DrawScope.drawGradientBackground(bg: Color, accent: Color, anim: Float) {
    val sweepAngle = anim * 45f
    drawRect(
        brush = Brush.sweepGradient(
            colors = listOf(bg, accent, bg.copy(alpha = 0.8f), accent.copy(alpha = 0.7f), bg),
            center = Offset(size.width * (0.3f + anim * 0.4f), size.height * 0.4f)
        )
    )
    // Large glow blob
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(size.width * 0.8f, size.height * 0.2f),
            radius = size.width * 0.6f
        ),
        radius = size.width * 0.6f,
        center = Offset(size.width * 0.8f, size.height * 0.2f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(bg.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(size.width * 0.1f, size.height * 0.8f),
            radius = size.width * 0.5f
        ),
        radius = size.width * 0.5f,
        center = Offset(size.width * 0.1f, size.height * 0.8f)
    )
}

private fun DrawScope.drawSparkleBackground(bg: Color, accent: Color, anim: Float) {
    drawRect(bg)
    // Animated sparkle dots
    for (i in 0 until 20) {
        val angle = anim + i * (Math.PI / 10).toFloat()
        val r = size.width * 0.3f + sin(anim + i.toFloat()) * size.width * 0.1f
        val cx = size.width / 2 + r * cos(angle)
        val cy = size.height / 2 + r * sin(angle) * 0.6f
        val starSize = 8f + sin(anim * 2 + i.toFloat()) * 4f
        drawCircle(
            color = accent.copy(alpha = 0.6f + 0.4f * sin(anim + i.toFloat()).coerceIn(-1f, 1f).let { (it + 1f) / 2f }),
            radius = starSize,
            center = Offset(cx.toFloat(), cy.toFloat())
        )
    }
    // Gradient overlay
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(bg.copy(alpha = 0.3f), Color.Transparent),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.width * 0.7f
        )
    )
}

private fun DrawScope.drawPatternBackground(bg: Color, accent: Color, anim: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(bg, bg.copy(alpha = 0.7f)),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
        )
    )
    // Polka dots pattern with animation
    val spacing = size.width / 7f
    var x = 0f
    while (x < size.width + spacing) {
        var y = 0f
        while (y < size.height + spacing) {
            val offsetX = if ((y / spacing).toInt() % 2 == 0) 0f else spacing / 2f
            val pulse = sin(anim + (x + y) / 100f) * 0.2f + 0.8f
            drawCircle(
                color = accent.copy(alpha = 0.25f * pulse),
                radius = spacing * 0.25f * pulse,
                center = Offset(x + offsetX, y)
            )
            y += spacing
        }
        x += spacing
    }
}
