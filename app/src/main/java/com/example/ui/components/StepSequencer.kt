package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink

data class SequencerTrack(
    val name: String,
    val color: Color,
    val steps: BooleanArray
)

@Composable
fun StepSequencer(
    tracks: List<SequencerTrack>,
    currentStep: Int, // 0..15
    onToggleStep: (trackIndex: Int, stepIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF10121C), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF23283E), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("step_sequencer")
    ) {
        // Step headers (1 to 16)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer for track name column
            Box(modifier = Modifier.width(72.dp)) {
                Text("TRACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A869E))
            }

            for (step in 0 until 16) {
                val isBeatQuarter = (step % 4 == 0)
                val isCurrent = (step == currentStep)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(2.dp)
                        .background(
                            if (isCurrent) NeonCyan.copy(alpha = 0.35f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step + 1}",
                        fontSize = 10.sp,
                        fontWeight = if (isBeatQuarter) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) Color.White else if (isBeatQuarter) NeonAmber else Color(0xFF6B7280)
                    )
                }
            }
        }

        // Track rows
        tracks.forEachIndexed { trackIndex, track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Label
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = track.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = track.color,
                        maxLines = 1
                    )
                }

                // 16 Step Buttons
                for (step in 0 until 16) {
                    val isChecked = track.steps.getOrElse(step) { false }
                    val isCurrent = (step == currentStep)
                    val isQuarterBeat = (step % 4 == 0)

                    val cellBg = when {
                        isChecked && isCurrent -> Color.White
                        isChecked -> track.color
                        isCurrent -> Color(0xFF282F48)
                        isQuarterBeat -> Color(0xFF1B1F30)
                        else -> Color(0xFF141724)
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(cellBg)
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) Color.White else Color(0xFF252B42),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onToggleStep(trackIndex, step) }
                            .testTag("step_${track.name}_$step")
                    )
                }
            }
        }
    }
}
