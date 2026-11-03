package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.VoiceModulationPreset
import com.example.ui.components.WaveformView
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun VocalBoothScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
    val liveAmplitude by viewModel.audioRecorder.liveAmplitude.collectAsState()
    val waveformPoints by viewModel.audioRecorder.waveformPoints.collectAsState()
    val transcribedText by viewModel.audioRecorder.transcribedText.collectAsState()
    val isTranscribing by viewModel.audioRecorder.isTranscribing.collectAsState()

    var recordedSamples by remember { mutableStateOf<ShortArray?>(null) }
    var selectedModulation by remember { mutableStateOf(VoiceModulationPreset.AUTOTUNE) }
    var noiseThreshold by remember { mutableStateOf(0.04f) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.audioRecorder.startRecording()
        }
    }

    // Pulse animation for recording ring
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("vocal_booth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VOCAL BOOTH & MIC RECORDER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "High-Fidelity Voice Recording & Real-Time Pitch Correction",
                    fontSize = 11.sp,
                    color = NeonPink
                )
            }

            // Noise gate status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1B2032), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = NeonLime, modifier = Modifier.size(14.dp))
                Text("AI De-Noiser", fontSize = 10.sp, color = NeonLime, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Mic Record Button with Pulse Glow
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(if (isRecording) pulseScale else 1f)
                .clip(CircleShape)
                .background(
                    if (isRecording) Color(0xFFE53935).copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.15f)
                )
                .border(
                    width = 2.dp,
                    color = if (isRecording) Color(0xFFE53935) else NeonPink,
                    shape = CircleShape
                )
                .clickable {
                    if (isRecording) {
                        val samples = viewModel.audioRecorder.stopRecording()
                        recordedSamples = samples
                    } else {
                        if (hasMicPermission) {
                            viewModel.audioRecorder.startRecording()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
                .testTag("vocal_record_btn"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color(0xFFE53935) else NeonPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop" else "Record",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Text(
            text = if (isRecording) "RECORDING IN PROGRESS... TAP TO STOP" else "TAP MICROPHONE TO RECORD VOCALS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRecording) Color(0xFFE53935) else Color(0xFF9EACB9),
            modifier = Modifier.padding(top = 12.dp),
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Real-Time Waveform & RMS Amplitude Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LIVE AMPLITUDE & WAVEFORM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Text("RMS: ${(liveAmplitude * 100).toInt()}%", fontSize = 10.sp, color = NeonLime)
                }
                WaveformView(
                    amplitudes = if (isRecording) waveformPoints else (recordedSamples?.let {
                        com.example.data.audio.WavExporter.generateWaveformPreview(it)
                    } ?: List(60) { 0.05f }),
                    playheadProgress = 0.5f,
                    barColor = NeonPink,
                    progressColor = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Recording to Track Button
        if (recordedSamples != null && !isRecording) {
            Button(
                onClick = {
                    recordedSamples?.let {
                        viewModel.saveVocalRecordingToTrack(it)
                        onNavigateToStudio()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("save_vocal_take_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Text(
                    text = "SAVE VOCAL TAKE TO MULTITRACK STUDIO",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Voice Modulation Presets
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "VOICE MODULATION & VOCAL EFFECTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPink
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(VoiceModulationPreset.entries) { mod ->
                        val isSelected = selectedModulation == mod
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedModulation = mod },
                            label = { Text(mod.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPink.copy(alpha = 0.25f),
                                selectedLabelColor = NeonPink,
                                containerColor = Color(0xFF141724),
                                labelColor = Color(0xFF8B96AF)
                            )
                        )
                    }
                }
                Text(
                    text = selectedModulation.description,
                    fontSize = 10.sp,
                    color = Color(0xFF8C98B2)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Noise Reduction Gate Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AI BACKGROUND NOISE REDUCTION GATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber
                    )
                    Text("${(noiseThreshold * 100).toInt()}%", fontSize = 11.sp, color = NeonAmber)
                }
                Slider(
                    value = noiseThreshold,
                    onValueChange = {
                        noiseThreshold = it
                        viewModel.audioRecorder.noiseSuppressionThreshold = it
                    },
                    valueRange = 0f..0.2f,
                    colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
                )
                Text(
                    text = "Automated smart suppression cuts fan rumble, street noise, and room reflections.",
                    fontSize = 10.sp,
                    color = Color(0xFF8C98B2)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Voice-to-Text Transcription for Lyrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NeonCyan)
                        Text(
                            text = "VOICE-TO-TEXT LYRICS TRANSCRIPTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (isTranscribing) {
                                viewModel.audioRecorder.stopSpeechTranscription()
                            } else {
                                viewModel.audioRecorder.startSpeechTranscription { text ->
                                    viewModel.updateLyrics(text)
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (isTranscribing) "Stop" else "Transcribe", fontSize = 10.sp)
                    }
                }

                if (transcribedText.isNotBlank()) {
                    Text(
                        text = "\"$transcribedText\"",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeonLime,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = "Speak or sing into the mic to convert your vocal ideas into text lyrics automatically.",
                        fontSize = 10.sp,
                        color = Color(0xFF8C98B2),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
