package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerPreset
import com.example.data.model.SunoGeneratedSong
import com.example.ui.components.KaraokeLyricsView
import com.example.ui.components.SpectrumVisualizer
import com.example.ui.components.WaveformView
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun PlayerScreen(
    viewModel: StudioViewModel,
    onEditLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSong by viewModel.activePlayingSong.collectAsState()
    val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()
    val currentBeat by viewModel.audioEngine.currentPlayheadBeat.collectAsState()
    val bpm by viewModel.audioEngine.bpm.collectAsState()
    val spectrumData by viewModel.audioEngine.liveSpectrum.collectAsState()
    val projectLyrics by viewModel.projectLyrics.collectAsState()

    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(true) }
    var selectedEqPreset by remember { mutableStateOf(EqualizerPreset.BASS_BOOST) }

    // Vinyl rotate animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val currentPlaybackSec = (currentBeat / (bpm / 60f))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("player_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HI-FI MUSIC PLAYER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Master Audio Output • 320 kbps High Fidelity",
                    fontSize = 10.sp,
                    color = Color(0xFF7A869E)
                )
            }

            IconButton(
                onClick = { viewModel.exportAndShareMasterTrack {} },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF191D2C))
                    .testTag("player_share_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Track", tint = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vinyl Artwork Disc
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(activeSong?.albumCoverColor ?: 0xFF8E24AA),
                            Color(0xFF121422),
                            Color(0xFF08090E)
                        )
                    )
                )
                .border(3.dp, Color(0xFF262C44), CircleShape)
                .rotate(if (isPlaying) rotationAngle else 0f),
            contentAlignment = Alignment.Center
        ) {
            // Grooves ring
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF384061), CircleShape)
            )
            // Center label
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NeonPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CLR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Song Title & Metadata
        Text(
            text = activeSong?.title ?: "Untitled Track",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "CLR Music Studio • ${activeSong?.genre ?: "Electronic"} • $bpm BPM",
            fontSize = 12.sp,
            color = NeonAmber,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Spectrum visualizer
        SpectrumVisualizer(
            spectrumData = spectrumData,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Waveform Seekbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            WaveformView(
                amplitudes = List(70) { 0.2f + (it % 7) * 0.09f },
                playheadProgress = (currentBeat % 16f) / 16f,
                barColor = NeonCyan,
                progressColor = NeonPink
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Player Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = { isShuffle = !isShuffle }) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) NeonCyan else Color(0xFF6B7280)
                )
            }

            // Previous
            IconButton(
                onClick = { viewModel.stopStudioLoop() },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
            }

            // Play / Pause FAB
            Button(
                onClick = {
                    if (isPlaying) viewModel.stopStudioLoop() else viewModel.playStudioLoop()
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .testTag("player_play_pause_fab"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Next
            IconButton(
                onClick = { viewModel.stopStudioLoop() },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
            }

            // Repeat
            IconButton(onClick = { isRepeat = !isRepeat }) {
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeat) NeonPink else Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Synchronized Karaoke Lyrics View
        KaraokeLyricsView(
            lyrics = activeSong?.lyrics ?: projectLyrics,
            currentPlaybackSec = currentPlaybackSec,
            onEditLyricsClick = onEditLyrics,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Sound Profile EQ Presets
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = NeonAmber)
                    Text(
                        text = "HIGH-FIDELITY SOUND PROFILE PRESETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(EqualizerPreset.entries) { preset ->
                        val isSelected = selectedEqPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedEqPreset = preset },
                            label = { Text(preset.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonAmber.copy(alpha = 0.25f),
                                selectedLabelColor = NeonAmber,
                                containerColor = Color(0xFF141724),
                                labelColor = Color(0xFF8B96AF)
                            )
                        )
                    }
                }
            }
        }
    }
}
