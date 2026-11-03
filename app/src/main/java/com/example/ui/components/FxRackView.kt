package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerPreset
import com.example.data.model.TrackEffects
import com.example.data.model.VoiceModulationPreset
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink

@Composable
fun FxRackView(
    effects: TrackEffects,
    onEffectsChange: (TrackEffects) -> Unit,
    modifier: Modifier = Modifier
) {
    val eqFrequencies = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF10131F))
            .border(1.dp, Color(0xFF23283E), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("fx_rack_view")
    ) {
        // Rack Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, contentDescription = "FX Rack", tint = NeonCyan)
                Text(
                    text = "STUDIO VST & DSP EFFECTS RACK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            // Latency badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1E2336), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = NeonLime, modifier = Modifier.height(14.dp))
                Text(
                    text = "8.5ms Latency-Free",
                    fontSize = 10.sp,
                    color = NeonLime,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Equalizer Section & Presets
        Text(
            text = "5-BAND GRAPHIC EQUALIZER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonAmber,
            letterSpacing = 0.5.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(EqualizerPreset.entries) { preset ->
                val isSelected = effects.eqBands == preset.bands
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onEffectsChange(effects.copy(eqBands = preset.bands))
                    },
                    label = { Text(preset.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                        selectedLabelColor = NeonCyan,
                        containerColor = Color(0xFF181C2B),
                        labelColor = Color(0xFF8B96AF)
                    )
                )
            }
        }

        // 5 Faders for EQ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            eqFrequencies.forEachIndexed { index, freqLabel ->
                val gainDb = effects.eqBands.getOrElse(index) { 0f }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(56.dp)
                ) {
                    Text(
                        text = "${if (gainDb > 0) "+" else ""}${gainDb.toInt()}dB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (gainDb != 0f) NeonCyan else Color(0xFF7E89A4)
                    )
                    Slider(
                        value = gainDb,
                        onValueChange = { newVal ->
                            val updatedBands = effects.eqBands.toMutableList()
                            if (index < updatedBands.size) {
                                updatedBands[index] = newVal
                                onEffectsChange(effects.copy(eqBands = updatedBands))
                            }
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0xFF23283D)
                        ),
                        modifier = Modifier.testTag("eq_band_$freqLabel")
                    )
                    Text(freqLabel, fontSize = 9.sp, color = Color(0xFF909BB2))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Voice Modulation & Vocal Pitch Correction
        Text(
            text = "VOICE MODULATION & REAL-TIME PITCH CORRECTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonPink,
            letterSpacing = 0.5.sp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(VoiceModulationPreset.entries) { modPreset ->
                val isSelected = effects.modulationPreset == modPreset
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onEffectsChange(effects.copy(modulationPreset = modPreset))
                    },
                    label = { Text(modPreset.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPink.copy(alpha = 0.25f),
                        selectedLabelColor = NeonPink,
                        containerColor = Color(0xFF181C2B),
                        labelColor = Color(0xFF8B96AF)
                    )
                )
            }
        }

        Text(
            text = effects.modulationPreset.description,
            fontSize = 10.sp,
            color = Color(0xFF8C98B0),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // 3. Reverb, Delay & Noise Gate Sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reverb
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Space Reverb", fontSize = 11.sp, color = Color.White)
                    Text("${(effects.reverbLevel * 100).toInt()}%", fontSize = 11.sp, color = NeonCyan)
                }
                Slider(
                    value = effects.reverbLevel,
                    onValueChange = { onEffectsChange(effects.copy(reverbLevel = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
            }

            // Delay
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tape Delay", fontSize = 11.sp, color = Color.White)
                    Text("${(effects.delayLevel * 100).toInt()}%", fontSize = 11.sp, color = NeonPink)
                }
                Slider(
                    value = effects.delayLevel,
                    onValueChange = { onEffectsChange(effects.copy(delayLevel = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Distortion
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Overdrive / Drive", fontSize = 11.sp, color = Color.White)
                    Text("${(effects.distortionLevel * 100).toInt()}%", fontSize = 11.sp, color = NeonAmber)
                }
                Slider(
                    value = effects.distortionLevel,
                    onValueChange = { onEffectsChange(effects.copy(distortionLevel = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
                )
            }

            // AI Noise Reduction Gate
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("AI Noise Gate", fontSize = 11.sp, color = Color.White)
                    Text("${(effects.noiseGateThreshold * 100).toInt()}%", fontSize = 11.sp, color = NeonLime)
                }
                Slider(
                    value = effects.noiseGateThreshold,
                    onValueChange = { onEffectsChange(effects.copy(noiseGateThreshold = it)) },
                    valueRange = 0f..0.25f,
                    colors = SliderDefaults.colors(thumbColor = NeonLime, activeTrackColor = NeonLime)
                )
            }
        }
    }
}
