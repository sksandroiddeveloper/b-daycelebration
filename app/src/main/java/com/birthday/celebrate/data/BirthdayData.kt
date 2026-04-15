package com.birthday.celebrate.data

data class BirthdaySlide(
    val id: Int,
    val title: String,
    val message: String,
    val emoji: String,
    val photoUri: String? = null,
    val backgroundColor: Long,
    val accentColor: Long,
    val backgroundType: BackgroundType = BackgroundType.GRADIENT,
    val animationType: AnimationType = AnimationType.FADE
)

enum class BackgroundType { GRADIENT, PATTERN, SOLID, SPARKLE }
enum class AnimationType  { FADE, BOUNCE, SLIDE, SPIN, FLOAT }

data class BirthdayData(
    val childName: String = "My Little Star ⭐",
    val age: Int = 1,
    val birthdayDate: String = "",
    val parentMessage: String = "We love you so much!",
    // 🎵 Music from phone — stores content URI, null = no music chosen
    val musicUri: String? = null,
    val musicTitle: String = "No song selected",
    val slides: List<BirthdaySlide> = defaultSlides()
)

fun defaultSlides(): List<BirthdaySlide> = listOf(
    BirthdaySlide(id=0, title="Happy Birthday!", message="Today is YOUR special day! 🎉\nThe whole world celebrates YOU!", emoji="🎂", backgroundColor=0xFFFF6B6B, accentColor=0xFFFFE66D, backgroundType=BackgroundType.SPARKLE, animationType=AnimationType.BOUNCE),
    BirthdaySlide(id=1, title="You Are Amazing!", message="Every single day you make\nour hearts overflow with joy! 💖", emoji="🌟", backgroundColor=0xFF845EC2, accentColor=0xFFFF9671, backgroundType=BackgroundType.GRADIENT, animationType=AnimationType.FLOAT),
    BirthdaySlide(id=2, title="Sweetest Moments", message="From your first smile to today,\nevery moment is pure magic! 📸", emoji="🦋", backgroundColor=0xFF00C9A7, accentColor=0xFFC4FCEF, backgroundType=BackgroundType.PATTERN, animationType=AnimationType.FADE),
    BirthdaySlide(id=3, title="You Bring Sunshine!", message="Like the brightest star in our sky,\nyou light up everything! ☀️", emoji="🌈", backgroundColor=0xFFFF9A3C, accentColor=0xFFFFF3B0, backgroundType=BackgroundType.GRADIENT, animationType=AnimationType.SPIN),
    BirthdaySlide(id=4, title="Our Little Hero!", message="You are brave, kind, and incredible.\nNever stop being YOU! 🦸", emoji="👑", backgroundColor=0xFF3D5A80, accentColor=0xFF98C1D9, backgroundType=BackgroundType.SPARKLE, animationType=AnimationType.SLIDE),
    BirthdaySlide(id=5, title="Laughter & Love!", message="Your giggles fill our home\nwith the sweetest music! 🎵", emoji="😂", backgroundColor=0xFFE84393, accentColor=0xFFFFB3DE, backgroundType=BackgroundType.PATTERN, animationType=AnimationType.BOUNCE),
    BirthdaySlide(id=6, title="Big Dreams Ahead!", message="The sky is not the limit —\nyour heart is! Dream big! 🚀", emoji="🌙", backgroundColor=0xFF2C3E50, accentColor=0xFFF39C12, backgroundType=BackgroundType.SPARKLE, animationType=AnimationType.FLOAT),
    BirthdaySlide(id=7, title="Cake Time! 🎂", message="Make a wish and blow those candles!\nEvery wish comes true for you! ✨", emoji="🎈", backgroundColor=0xFFFF6B35, accentColor=0xFFFFF9C4, backgroundType=BackgroundType.GRADIENT, animationType=AnimationType.SPIN),
    BirthdaySlide(id=8, title="Forever & Always!", message="Our love for you grows bigger\nwith every passing year! 💝", emoji="🥰", backgroundColor=0xFF6C5CE7, accentColor=0xFFA29BFE, backgroundType=BackgroundType.PATTERN, animationType=AnimationType.FADE),
    BirthdaySlide(id=9, title="Happy Birthday, Love!", message="Here's to YOU — the most wonderful\ngift life has given us! 🎁", emoji="🎊", backgroundColor=0xFFD63031, accentColor=0xFFFFD32A, backgroundType=BackgroundType.SPARKLE, animationType=AnimationType.BOUNCE),
)
