package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun VirtualKeyboard(
    onNoteTrigger: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var baseOctave by remember { mutableIntStateOf(3) }
    var lastPlayedNote by remember { mutableStateOf("Ready") }

    val whiteNotes = listOf("C", "D", "E", "F", "G", "A", "B")
    val blackNoteMap = mapOf(
        0 to "C#",
        1 to "D#",
        3 to "F#",
        4 to "G#",
        5 to "A#"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F111A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF262C44), RoundedCornerShape(12.dp))
            .padding(8.dp)
            .testTag("virtual_keyboard")
    ) {
        // Keyboard Toolbar: Octave +/- and Note display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MIDI KEYBOARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = " • $lastPlayedNote",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonPink,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (baseOctave > 1) baseOctave-- },
                    modifier = Modifier.testTag("octave_down")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Octave Down", tint = Color.White)
                }
                Text(
                    text = "OCT $baseOctave",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = { if (baseOctave < 5) baseOctave++ },
                    modifier = Modifier.testTag("octave_up")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Octave Up", tint = Color.White)
                }
            }
        }

        // 2 Octaves Piano Roll
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .horizontalScroll(scrollState)
        ) {
            for (oct in baseOctave..(baseOctave + 1)) {
                Box(modifier = Modifier.height(130.dp)) {
                    // White keys
                    Row {
                        whiteNotes.forEachIndexed { index, noteName ->
                            val fullNote = "$noteName$oct"
                            var isKeyActive by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .width(42.dp)
                                    .fillMaxHeight()
                                    .padding(horizontal = 1.dp)
                                    .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                                    .background(if (isKeyActive) NeonCyan else Color(0xFFF2F4F8))
                                    .border(1.dp, Color(0xFF9EA3B0), RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                                    .pointerInput(fullNote) {
                                        detectTapGestures(
                                            onPress = {
                                                isKeyActive = true
                                                lastPlayedNote = fullNote
                                                onNoteTrigger(fullNote)
                                                tryAwaitRelease()
                                                isKeyActive = false
                                            }
                                        )
                                    }
                                    .testTag("key_$fullNote"),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = fullNote,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }

                    // Black keys overlaid on top
                    blackNoteMap.forEach { (whiteIndex, sharpName) ->
                        val fullNote = "$sharpName$oct"
                        val leftOffset = (whiteIndex * 42 + 28).dp
                        var isBlackActive by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .offset(x = leftOffset)
                                .width(28.dp)
                                .height(80.dp)
                                .zIndex(2f)
                                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(if (isBlackActive) NeonPink else Color(0xFF161822))
                                .border(1.dp, Color(0xFF3B415C), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                .pointerInput(fullNote) {
                                    detectTapGestures(
                                        onPress = {
                                            isBlackActive = true
                                            lastPlayedNote = fullNote
                                            onNoteTrigger(fullNote)
                                            tryAwaitRelease()
                                            isBlackActive = false
                                        }
                                    )
                                }
                                .testTag("key_$fullNote"),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = sharpName,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
