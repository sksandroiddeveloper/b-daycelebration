package com.birthday.celebrate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// App Colors
val BirthdayPink = Color(0xFFFF6B9D)
val BirthdayPurple = Color(0xFF9B59B6)
val BirthdayGold = Color(0xFFFFD700)
val BirthdayOrange = Color(0xFFFF6B35)
val BirthdayCream = Color(0xFFFFF8E7)
val BirthdayMint = Color(0xFF00CEC9)

// Color scheme
private val ColorScheme = darkColorScheme(
    primary = BirthdayPink,
    secondary = BirthdayPurple,
    tertiary = BirthdayGold,
    background = Color(0xFF1A0A2E),
    surface = Color(0xFF2D1554),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun BirthdayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content = content
    )
}

// Color extension
fun Long.toComposeColor() = Color(this)
