package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BeatPadGrid
import com.example.ui.components.StepSequencer
import com.example.ui.components.VirtualKeyboard
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBackground
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun BeatMakerScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sequencer, 1: Beat Pads, 2: MIDI Piano
    val sequencerTracks by viewModel.sequencerTracks.collectAsState()
    val currentStep by viewModel.currentSequencerStep.collectAsState()
    val isPlaying by viewModel.isSequencerPlaying.collectAsState()
    val bpm by viewModel.audioEngine.bpm.collectAsState()

    val tabTitles = listOf("16-Step Sequencer", "MPC Beat Pads", "MIDI Keyboard")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBackground)
            .padding(12.dp)
            .testTag("beat_maker_screen")
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = StudioDarkSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (selectedTab == index) NeonCyan else Color(0xFF909BB2)
                        )
                    }
                )
            }
        }

        // Toolbar: BPM, Presets, Play / Stop
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
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
                // BPM Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                    Text(
                        text = "$bpm BPM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    IconButton(
                        onClick = { viewModel.audioEngine.setBpm(bpm - 5) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { viewModel.audioEngine.setBpm(bpm + 5) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Play / Stop Sequencer Button
                Button(
                    onClick = { viewModel.toggleSequencerPlay() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) NeonPink else NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("beatmaker_play_button")
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Text(
                        text = if (isPlaying) "PAUSE" else "PLAY",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Step Sequencer Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    // Pattern Presets Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PRESETS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E8BA4))
                        listOf("Default 4/4", "Trap Drill", "Lo-Fi Chill").forEach { preset ->
                            OutlinedButton(
                                onClick = { viewModel.loadSequencerPreset(preset) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(preset, fontSize = 10.sp, color = NeonCyan)
                            }
                        }
                        IconButton(
                            onClick = { viewModel.clearSequencer() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFE53935))
                        }
                    }

                    StepSequencer(
                        tracks = sequencerTracks,
                        currentStep = currentStep,
                        onToggleStep = { tIdx, sIdx -> viewModel.toggleSequencerStep(tIdx, sIdx) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            1 -> {
                // Beat Pads Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "TOUCH RESPONSIVE DRUM PADS & VOCAL CHOPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    BeatPadGrid(
                        onPadTrigger = { padId -> viewModel.triggerPad(padId) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            2 -> {
                // Virtual MIDI Piano Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "VIRTUAL MIDI CONTROLLER & SYNTH KEYBOARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    VirtualKeyboard(
                        onNoteTrigger = { noteName -> viewModel.triggerKeyboardNote(noteName) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudioDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Piano, contentDescription = null, tint = NeonAmber)
                                Text(
                                    text = "External MIDI Controller Integration",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Text(
                                text = "USB MIDI / Bluetooth LE MIDI input supported. Plug any class-compliant MIDI keyboard to trigger instruments directly with zero latency monitoring.",
                                fontSize = 11.sp,
                                color = Color(0xFF909DB6),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
