package com.birthday.celebrate.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.birthday.celebrate.data.BirthdayViewModel
import com.birthday.celebrate.ui.theme.BirthdayPink
import com.birthday.celebrate.ui.theme.BirthdayPurple
import com.birthday.celebrate.ui.theme.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: BirthdayViewModel,
    onBack: () -> Unit
) {
    val data           by viewModel.birthdayData.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()

    var childName     by remember(data.childName)      { mutableStateOf(data.childName) }
    var age           by remember(data.age)            { mutableStateOf(data.age.toString()) }
    var parentMessage by remember(data.parentMessage)  { mutableStateOf(data.parentMessage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✏️ Customize Birthday", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.resetToDefaults() }) {
                        Text("Reset", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BirthdayPurple)
            )
        },
        containerColor = Color(0xFF1A0A2E)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Basic info ─────────────────────────────────────────
            item {
                SectionCard(title = "🎂 Basic Info") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BirthdayTextField(value = childName, onValueChange = { childName = it },
                            label = "Child's Name", onDone = { viewModel.updateChildName(childName) })
                        BirthdayTextField(value = age, onValueChange = { age = it },
                            label = "Age (Turning)", keyboardType = KeyboardType.Number,
                            onDone = { age.toIntOrNull()?.let { viewModel.updateAge(it) } })
                        BirthdayTextField(value = parentMessage, onValueChange = { parentMessage = it },
                            label = "Your Love Message", singleLine = false, minLines = 2,
                            onDone = { viewModel.updateParentMessage(parentMessage) })
                        Button(
                            onClick = {
                                viewModel.updateChildName(childName)
                                age.toIntOrNull()?.let { viewModel.updateAge(it) }
                                viewModel.updateParentMessage(parentMessage)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BirthdayPink),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── 🎵 Music picker ────────────────────────────────────
            item {
                MusicPickerSection(
                    currentMusicTitle = data.musicTitle,
                    hasMusicUri       = data.musicUri != null,
                    isMusicPlaying    = isMusicPlaying,
                    onMusicSelected   = { uri, title -> viewModel.pickMusic(uri, title) },
                    onTogglePlayPause = { viewModel.toggleMusicPlayPause() },
                    onRemoveMusic     = { viewModel.stopMusic() }
                )
            }

            // ── Slides customization ───────────────────────────────
            item {
                Text("📸 Customize Slides", color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                Text("Add photos from your phone gallery to each slide!",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }

            itemsIndexed(data.slides) { index, slide ->
                SlideEditCard(
                    slideIndex      = index,
                    title           = slide.title,
                    currentMessage  = slide.message,
                    currentPhoto    = slide.photoUri,
                    backgroundColor = slide.backgroundColor.toComposeColor(),
                    emoji           = slide.emoji,
                    onPhotoSelected = { uri -> viewModel.updateSlidePhoto(slide.id, uri) },
                    onPhotoRemoved  = { viewModel.updateSlidePhoto(slide.id, null) },
                    onMessageChanged = { msg -> viewModel.updateSlideMessage(slide.id, msg) }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── 🎵 Music Picker Section ────────────────────────────────────────
@Composable
fun MusicPickerSection(
    currentMusicTitle: String,
    hasMusicUri: Boolean,
    isMusicPlaying: Boolean,
    onMusicSelected: (uri: String, title: String) -> Unit,
    onTogglePlayPause: () -> Unit,
    onRemoveMusic: () -> Unit
) {
    val context = LocalContext.current

    // Opens the system music picker — returns a content URI to an audio file
    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Resolve the display name of the song from MediaStore
            val title = resolveSongTitle(context, uri) ?: "Selected Song"
            onMusicSelected(uri.toString(), title)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1554))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎵", fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Background Music", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Play a song from your phone during the slideshow",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (hasMusicUri) {
                // Music is selected — show player controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF6C5CE7).copy(alpha = 0.3f), Color(0xFFE84393).copy(alpha = 0.2f))),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Animated music icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.radialGradient(listOf(Color(0xFF6C5CE7), Color(0xFF2D1554))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isMusicPlaying) "▶" else "⏸", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selected Song", color = Color(0xFFAD7BFF), fontSize = 11.sp)
                            Text(currentMusicTitle, color = Color.White, fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                        // Play/Pause toggle
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF6C5CE7), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle music",
                                tint = Color.White, modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { musicPickerLauncher.launch("audio/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Change Song")
                    }
                    OutlinedButton(
                        onClick = onRemoveMusic,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                        border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Remove")
                    }
                }
            } else {
                // No music selected — show picker button
                Button(
                    onClick = { musicPickerLauncher.launch("audio/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFE84393))),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Pick a Song from Your Phone", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Supports MP3, AAC, FLAC, WAV and more. The song loops throughout the slideshow.",
                    color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp
                )
            }
        }
    }
}

// Resolve human-readable song title from a content URI using MediaStore
fun resolveSongTitle(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(
            uri,
            arrayOf(android.provider.MediaStore.Audio.Media.TITLE, android.provider.MediaStore.Audio.Media.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val titleIdx = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val nameIdx  = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
                cursor.getString(if (titleIdx >= 0) titleIdx else nameIdx)
                    ?.removeSuffix(".mp3")?.removeSuffix(".flac")?.removeSuffix(".aac")
            } else null
        }
    } catch (e: Exception) { null }
}

// ── Slide edit card ────────────────────────────────────────────────
@Composable
fun SlideEditCard(
    slideIndex: Int,
    title: String,
    currentMessage: String,
    currentPhoto: String?,
    backgroundColor: Color,
    emoji: String,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit,
    onMessageChanged: (String) -> Unit
) {
    var message  by remember(currentMessage) { mutableStateOf(currentMessage) }
    var expanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onPhotoSelected(it.toString()) } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, backgroundColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).background(backgroundColor, CircleShape),
                        contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Slide ${slideIndex + 1}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = Color.White
                )
            }

            if (currentPhoto != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null,
                        tint = Color(0xFF4ECDC4), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Photo added ✓", color = Color(0xFF4ECDC4), fontSize = 12.sp)
                }
            }

            if (expanded) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(Modifier.height(16.dp))

                Text("📸 Slide Photo", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                if (currentPhoto != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(model = Uri.parse(currentPhoto), contentDescription = "Photo",
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        IconButton(onClick = onPhotoRemoved,
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Change Photo")
                    }
                } else {
                    Button(onClick = { photoPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text("Pick from Phone Gallery")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("💬 Slide Message", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = message, onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(), minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = backgroundColor, unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color.White
                    ), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(12.dp))
                Button(onClick = { onMessageChanged(message) },
                    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()) { Text("Save Slide", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ── Reusable helpers ───────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1554))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun BirthdayTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDone: () -> Unit = {}, singleLine: Boolean = true, minLines: Int = 1
) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(), singleLine = singleLine, minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(alpha = 0.9f),
            focusedBorderColor = BirthdayPink, unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = BirthdayPink, focusedLabelColor = BirthdayPink
        ), shape = RoundedCornerShape(12.dp))
}
