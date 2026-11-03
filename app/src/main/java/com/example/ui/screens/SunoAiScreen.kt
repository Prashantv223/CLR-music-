package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.SunoMusicGenerator
import com.example.data.model.SunoGeneratedSong
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.viewmodel.StudioUiState
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun SunoAiScreen(
    viewModel: StudioViewModel,
    onOpenInStudio: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sunoSongs by viewModel.sunoSongs.collectAsState()
    val activePlayingSong by viewModel.activePlayingSong.collectAsState()
    val isAudioPlaying by viewModel.audioEngine.isPlaying.collectAsState()

    var songTitle by remember { mutableStateOf("") }
    var songLyrics by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(SunoMusicGenerator.PRESET_GENRES[0]) }
    var selectedMood by remember { mutableStateOf(SunoMusicGenerator.PRESET_MOODS[0]) }
    var selectedVocalStyle by remember { mutableStateOf(SunoMusicGenerator.PRESET_VOCAL_STYLES[0]) }

    var lyricsEditingSong by remember { mutableStateOf<SunoGeneratedSong?>(null) }
    var editingLyricsContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(14.dp)
            .testTag("suno_ai_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF281136), Color(0xFF101322))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "SUNO AI MUSIC GENERATOR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Turn lyrics into high-quality studio songs & stems",
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Song Title Input
        item {
            Column {
                Text(
                    text = "SONG TITLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp
                )
                OutlinedTextField(
                    value = songTitle,
                    onValueChange = { songTitle = it },
                    placeholder = { Text("e.g. Cyber Raat, Dil Ki Beat, Neon Horizon", color = Color(0xFF6B7280)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("suno_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF121422),
                        unfocusedContainerColor = Color(0xFF121422)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Lyrics Input + Quick Presets
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SONG LYRICS (SHAYARI / WORDS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        letterSpacing = 0.5.sp
                    )

                    // Quick fill preset button
                    TextButton(
                        onClick = {
                            songLyrics = """
                                [Intro] Dhun baji hai raat ki chhaon mein
                                [Verse 1] Chalte rahe hum neon galiyon mein
                                Dil ki dhadkan ban gayi hai sangeet
                                [Chorus] CLR Music gaaye naya geet
                                Har dil ko chhu le yeh meethe meethe bol
                                [Outro] Suno aawaz, fiza mein ghul gayi
                            """.trimIndent()
                        }
                    ) {
                        Text("Auto-fill Hindi Lyrics", fontSize = 11.sp, color = NeonAmber)
                    }
                }

                OutlinedTextField(
                    value = songLyrics,
                    onValueChange = { songLyrics = it },
                    placeholder = {
                        Text(
                            "Enter your lyrics here with sections like:\n[Verse 1] Your words...\n[Chorus] Your hook melody...",
                            color = Color(0xFF6B7280)
                        )
                    },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("suno_lyrics_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF121422),
                        unfocusedContainerColor = Color(0xFF121422)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Genre Selector Chips
        item {
            Column {
                Text(
                    text = "GENRE & STYLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber,
                    letterSpacing = 0.5.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(SunoMusicGenerator.PRESET_GENRES) { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonAmber.copy(alpha = 0.25f),
                                selectedLabelColor = NeonAmber,
                                containerColor = Color(0xFF151827),
                                labelColor = Color(0xFF8A97AF)
                            )
                        )
                    }
                }
            }
        }

        // Mood & Vocal Style Selectors
        item {
            Column {
                Text(
                    text = "MOOD & VIBE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    letterSpacing = 0.5.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(SunoMusicGenerator.PRESET_MOODS) { mood ->
                        val isSelected = selectedMood == mood
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = mood },
                            label = { Text(mood, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonLime.copy(alpha = 0.25f),
                                selectedLabelColor = NeonLime,
                                containerColor = Color(0xFF151827),
                                labelColor = Color(0xFF8A97AF)
                            )
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "VOCAL MODULATION STYLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(SunoMusicGenerator.PRESET_VOCAL_STYLES) { style ->
                        val isSelected = selectedVocalStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedVocalStyle = style },
                            label = { Text(style, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NeonCyan,
                                containerColor = Color(0xFF151827),
                                labelColor = Color(0xFF8A97AF)
                            )
                        )
                    }
                }
            }
        }

        // Generate Button
        item {
            val isGenerating = uiState is StudioUiState.GeneratingSong
            Button(
                onClick = {
                    viewModel.generateSunoSong(
                        title = songTitle.ifBlank { "$selectedGenre Hit" },
                        lyrics = songLyrics,
                        genre = selectedGenre,
                        mood = selectedMood,
                        vocalStyle = selectedVocalStyle
                    )
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("suno_generate_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = (uiState as StudioUiState.GeneratingSong).message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Text(
                        text = "GENERATE FULL SONG & STEMS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Generated Songs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR GENERATED TRACKS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${sunoSongs.size} Tracks",
                    fontSize = 11.sp,
                    color = Color(0xFF8C98B2)
                )
            }
        }

        items(sunoSongs, key = { it.id }) { song ->
            val isCurrentActive = activePlayingSong?.id == song.id
            val isPlayingThis = isCurrentActive && isAudioPlaying

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("song_card_${song.id}"),
                colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrentActive) NeonCyan else StudioBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Song Cover & Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(song.albumCoverColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                            }

                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = song.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${song.genre} • ${song.vocalStyle} • ${song.bpm} BPM",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9EACB9)
                                )
                            }
                        }

                        // Play / Pause Button
                        IconButton(
                            onClick = {
                                if (isPlayingThis) {
                                    viewModel.stopStudioLoop()
                                } else {
                                    viewModel.playStudioLoop()
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingThis) NeonPink else NeonCyan)
                                .testTag("play_song_${song.id}")
                        ) {
                            Icon(
                                if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Open in Studio, Edit Lyrics, Player
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenInStudio,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Open in Studio", fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }

                        OutlinedButton(
                            onClick = {
                                lyricsEditingSong = song
                                editingLyricsContent = song.lyrics.joinToString("\n") { it.text }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Edit Lyrics", fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }

                        OutlinedButton(
                            onClick = onNavigateToPlayer,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonAmber),
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Player", fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }

    // Lyrics Editor Dialog
    if (lyricsEditingSong != null) {
        AlertDialog(
            onDismissRequest = { lyricsEditingSong = null },
            title = {
                Text(
                    text = "Edit Song Lyrics",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Modify or write new lyrics. These will automatically synchronize with the music playback and karaoke reader.",
                        fontSize = 11.sp,
                        color = Color(0xFFA5B2CA),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = editingLyricsContent,
                        onValueChange = { editingLyricsContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .testTag("lyrics_dialog_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = StudioBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateLyrics(editingLyricsContent)
                        lyricsEditingSong = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Save & Sync Lyrics", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { lyricsEditingSong = null }) {
                    Text("Cancel", color = Color(0xFFA0AABF))
                }
            },
            containerColor = StudioDarkSurface
        )
    }
}
