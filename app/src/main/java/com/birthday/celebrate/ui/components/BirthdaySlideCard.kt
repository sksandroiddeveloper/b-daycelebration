package com.birthday.celebrate.ui.components

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.birthday.celebrate.data.AnimationType
import com.birthday.celebrate.data.BirthdaySlide
import com.birthday.celebrate.ui.theme.toComposeColor
import com.birthday.celebrate.utils.ConfettiOverlay

@Composable
fun BirthdaySlideCard(
    slide: BirthdaySlide,
    childName: String,
    age: Int,
    modifier: Modifier = Modifier
) {
    val accent = slide.accentColor.toComposeColor()
    val infiniteTransition = rememberInfiniteTransition(label = "slide_${slide.id}")

    // Y offset: bounce or float, else 0
    val emojiOffsetY by infiniteTransition.animateFloat(
        initialValue = when (slide.animationType) {
            AnimationType.BOUNCE -> 0f
            AnimationType.FLOAT -> -10f
            else -> 0f
        },
        targetValue = when (slide.animationType) {
            AnimationType.BOUNCE -> -28f
            AnimationType.FLOAT -> 10f
            else -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (slide.animationType) {
                    AnimationType.BOUNCE -> 700
                    AnimationType.FLOAT -> 2000
                    else -> 1000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // Rotation: spin, else 0
    val emojiRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (slide.animationType == AnimationType.SPIN) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (slide.animationType == AnimationType.SPIN) 2000 else 1000,
                easing = if (slide.animationType == AnimationType.SPIN) LinearEasing else LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Animated background
        SlideBackground(slide = slide, modifier = Modifier.fillMaxSize()) {}

        // Confetti on celebration slides
        if (slide.id == 0 || slide.id == 7 || slide.id == 9) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize(), active = true)
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Name + age badge
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                    .border(2.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(50))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🎉 $childName • Turning $age!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Slide title with drop shadow
            Text(
                text = slide.title,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(3f, 3f),
                        blurRadius = 8f
                    )
                )
            )

            // Animated emoji with glow ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = emojiOffsetY.dp)
                    .rotate(emojiRotation)
                    .scale(emojiScale),
                contentAlignment = Alignment.Center
            ) {
                // Glow circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(accent.copy(alpha = glowAlpha), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                Text(text = slide.emoji, fontSize = 80.sp, textAlign = TextAlign.Center)
            }

            // Photo from phone gallery (if user added one)
            slide.photoUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            4.dp,
                            Brush.linearGradient(listOf(accent, Color.White, accent)),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    AsyncImage(
                        model = Uri.parse(uri),
                        contentDescription = "Birthday photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Message card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = slide.message,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(1f, 1f),
                            blurRadius = 4f
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(10) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == slide.id) 12.dp else 7.dp)
                            .background(
                                if (i == slide.id) accent else Color.White.copy(alpha = 0.38f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}
