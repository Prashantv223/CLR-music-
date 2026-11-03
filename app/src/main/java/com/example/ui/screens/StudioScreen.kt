package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrackType
import com.example.data.model.StudioTrack
import com.example.ui.components.FxRackView
import com.example.ui.components.SpectrumVisualizer
import com.example.ui.components.WaveformView
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    onNavigateToVocalBooth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.tracks.collectAsState()
    val isPlaying by viewModel.audioEngine.isPlaying.collectAsState()
    val isMetronomeActive by viewModel.audioEngine.isMetronomeActive.collectAsState()
    val currentBeat by viewModel.audioEngine.currentPlayheadBeat.collectAsState()
    val bpm by viewModel.audioEngine.bpm.collectAsState()
    val spectrumData by viewModel.audioEngine.liveSpectrum.collectAsState()
    val selectedTrackId by viewModel.selectedTrackId.collectAsState()
    val project by viewModel.currentProject.collectAsState()

    var showFxRackForTrack by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("studio_screen")
    ) {
        // 1. Studio Header with Project Info, BPM, Metronome, Export
        StudioHeader(
            projectTitle = project.title,
            bpm = bpm,
            isMetronomeActive = isMetronomeActive,
            isExporting = isExporting,
            onBpmChange = { viewModel.audioEngine.setBpm(it) },
            onToggleMetronome = { viewModel.audioEngine.toggleMetronome() },
            onExportShare = {
                isExporting = true
                viewModel.exportAndShareMasterTrack { isExporting = false }
            }
        )

        // 2. Real-time Live Spectrum Visualizer
        SpectrumVisualizer(
            spectrumData = spectrumData,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        // 3. Transport Playback Bar
        StudioTransportBar(
            isPlaying = isPlaying,
            currentBeat = currentBeat,
            onPlayPause = {
                if (isPlaying) viewModel.stopStudioLoop() else viewModel.playStudioLoop()
            },
            onRecordMic = onNavigateToVocalBooth
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Multitrack Timeline & Clips
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                TrackRowItem(
                    track = track,
                    currentPlayheadProgress = (currentBeat % 16f) / 16f,
                    isSelected = track.id == selectedTrackId,
                    onSelect = { viewModel.selectTrack(track.id) },
                    onMute = { viewModel.toggleMuteTrack(track.id) },
                    onSolo = { viewModel.toggleSoloTrack(track.id) },
                    onVolumeChange = { viewModel.updateTrackVolume(track.id, it) },
                    onPanChange = { viewModel.updateTrackPan(track.id, it) },
                    onToggleFx = {
                        showFxRackForTrack = if (showFxRackForTrack == track.id) null else track.id
                    }
                )

                // Expandable VST FX Rack for this track
                AnimatedVisibility(visible = showFxRackForTrack == track.id) {
                    FxRackView(
                        effects = track.effects,
                        onEffectsChange = { viewModel.updateTrackEffects(track.id, it) },
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudioHeader(
    projectTitle: String,
    bpm: Int,
    isMetronomeActive: Boolean,
    isExporting: Boolean,
    onBpmChange: (Int) -> Unit,
    onToggleMetronome: () -> Unit,
    onExportShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Project Title & Studio Badge
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CLR STUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = " • 44.1kHz 16-bit",
                        fontSize = 10.sp,
                        color = Color(0xFF7F8C9F),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(
                    text = projectTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Controls: BPM, Metronome, Export
            Row(verticalAlignment = Alignment.CenterVertically) {
                // BPM Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B1E2E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$bpm BPM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Metronome button
                IconButton(
                    onClick = onToggleMetronome,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isMetronomeActive) NeonAmber.copy(alpha = 0.25f) else Color(0xFF1B1E2E))
                        .testTag("metronome_toggle_btn")
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = "Metronome",
                        tint = if (isMetronomeActive) NeonAmber else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Export & Share Button
                Button(
                    onClick = onExportShare,
                    enabled = !isExporting,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("export_share_button")
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Export WAV",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioTransportBar(
    isPlaying: Boolean,
    currentBeat: Float,
    onPlayPause: () -> Unit,
    onRecordMic: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioDarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Beat counter display: e.g. "BAR 01 : 03"
            val barNumber = ((currentBeat / 4).toInt() % 4) + 1
            val beatInBar = (currentBeat.toInt() % 4) + 1
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) NeonLime else Color(0xFF6B7280))
                )
                Text(
                    text = "BAR 0$barNumber : 0$beatInBar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) NeonLime else Color.White,
                    modifier = Modifier.padding(start = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            // Transport Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play / Stop Master Loop Button
                Button(
                    onClick = onPlayPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) NeonPink else NeonCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("studio_play_button")
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        tint = Color.Black
                    )
                    Text(
                        text = if (isPlaying) "STOP" else "PLAY LOOP",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Record Mic Button
                OutlinedButton(
                    onClick = onRecordMic,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("record_mic_button")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record", tint = NeonPink, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Vocal Take",
                        color = NeonPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: StudioTrack,
    currentPlayheadProgress: Float,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onMute: () -> Unit,
    onSolo: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onToggleFx: () -> Unit
) {
    val trackColor = Color(track.colorHex)
    val firstClip = track.clips.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("track_row_${track.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF161928) else Color(0xFF11131E)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) trackColor.copy(alpha = 0.8f) else StudioBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Track Header: Name, Type, M/S, FX button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(trackColor)
                    )
                    Text(
                        text = track.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "(${track.type.displayName})",
                        fontSize = 10.sp,
                        color = Color(0xFF818EA6),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Mute, Solo, FX buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mute
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (track.isMuted) Color(0xFFE53935) else Color(0xFF1F2436))
                            .clickable { onMute() }
                            .testTag("mute_${track.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (track.isMuted) Color.White else Color(0xFF909BB2)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Solo
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (track.isSolo) NeonAmber else Color(0xFF1F2436))
                            .clickable { onSolo() }
                            .testTag("solo_${track.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (track.isSolo) Color.Black else Color(0xFF909BB2)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // FX Rack button
                    IconButton(
                        onClick = onToggleFx,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1F2436))
                            .testTag("fx_${track.id}")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "FX",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Waveform Timeline Box for the Clip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0E16))
                    .border(1.dp, Color(0xFF1F2336), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                WaveformView(
                    amplitudes = firstClip?.waveformAmplitudes ?: emptyList(),
                    playheadProgress = currentPlayheadProgress,
                    barColor = trackColor,
                    progressColor = NeonPink
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Track Volume & Pan sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Volume
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = "VOL ${(track.volume * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA5B2CA),
                        modifier = Modifier.width(58.dp)
                    )
                    Slider(
                        value = track.volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = trackColor,
                            activeTrackColor = trackColor,
                            inactiveTrackColor = Color(0xFF22273C)
                        ),
                        modifier = Modifier.testTag("vol_slider_${track.id}")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Pan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.9f)
                ) {
                    val panLabel = when {
                        track.pan < -0.1f -> "L ${(kotlin.math.abs(track.pan) * 100).toInt()}"
                        track.pan > 0.1f -> "R ${(track.pan * 100).toInt()}"
                        else -> "C"
                    }
                    Text(
                        text = "PAN $panLabel",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA5B2CA),
                        modifier = Modifier.width(52.dp)
                    )
                    Slider(
                        value = track.pan,
                        onValueChange = onPanChange,
                        valueRange = -1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0xFF22273C)
                        ),
                        modifier = Modifier.testTag("pan_slider_${track.id}")
                    )
                }
            }
        }
    }
}
