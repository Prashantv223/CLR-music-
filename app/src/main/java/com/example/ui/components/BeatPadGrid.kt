package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPink

data class BeatPadItem(
    val id: String,
    val label: String,
    val subLabel: String,
    val color: Color
)

val DEFAULT_BEAT_PADS = listOf(
    BeatPadItem("pad_kick", "KICK", "808 Sub", NeonCyan),
    BeatPadItem("pad_snare", "SNARE", "Crisp Punch", NeonPink),
    BeatPadItem("pad_hihat_c", "HI-HAT", "Closed", NeonAmber),
    BeatPadItem("pad_hihat_o", "OPEN HAT", "Sizzle", NeonAmber),
    BeatPadItem("pad_clap", "CLAP", "Studio Layer", NeonPink),
    BeatPadItem("pad_808", "808 BOOM", "Low F#", Color(0xFFB388FF)),
    BeatPadItem("pad_synth_c", "SYNTH C4", "Saw Chord", NeonCyan),
    BeatPadItem("pad_synth_g", "SYNTH G4", "Pluck Lead", NeonLime),
    BeatPadItem("pad_vocal_yeah", "VOCAL", "Chop 'Yeah'", Color(0xFFFF4081)),
    BeatPadItem("pad_vocal_drop", "VOCAL", "Chop 'Drop'", Color(0xFFFF5252)),
    BeatPadItem("pad_crash", "CRASH", "16\" Brass", NeonAmber),
    BeatPadItem("pad_rim", "RIMSHOT", "Percussion", NeonLime)
)

@Composable
fun BeatPadGrid(
    onPadTrigger: (String) -> Unit,
    modifier: Modifier = Modifier,
    pads: List<BeatPadItem> = DEFAULT_BEAT_PADS
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .testTag("beat_pad_grid"),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pads, key = { it.id }) { pad ->
            BeatPadCell(pad = pad, onTrigger = { onPadTrigger(pad.id) })
        }
    }
}

@Composable
fun BeatPadCell(
    pad: BeatPadItem,
    onTrigger: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) pad.color else Color(0xFF161926),
        animationSpec = tween(durationMillis = 80),
        label = "pad_color"
    )

    val textColor = if (isPressed) Color.Black else Color.White
    val subTextColor = if (isPressed) Color(0xFF212121) else pad.color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isPressed) 2.dp else 1.dp,
                color = if (isPressed) Color.White else pad.color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onTrigger()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .testTag("beat_pad_${pad.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            Text(
                text = pad.label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textColor,
                letterSpacing = 1.sp
            )
            Text(
                text = pad.subLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = subTextColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
