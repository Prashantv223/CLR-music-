package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LyricLine
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun KaraokeLyricsView(
    lyrics: List<LyricLine>,
    currentPlaybackSec: Float,
    onEditLyricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Find active lyric index
    val activeIndex = lyrics.indexOfLast { it.timestampSec <= currentPlaybackSec }
        .coerceAtLeast(0)

    LaunchedEffect(activeIndex) {
        if (lyrics.isNotEmpty() && activeIndex in lyrics.indices) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF10121B))
            .border(1.dp, Color(0xFF242A42), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("karaoke_lyrics_view")
    ) {
        // Karaoke Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = "Karaoke", tint = NeonPink)
                Text(
                    text = "REAL-TIME KARAOKE & LYRICS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 6.dp),
                    letterSpacing = 1.sp
                )
            }

            OutlinedButton(
                onClick = onEditLyricsClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("edit_lyrics_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.height(14.dp))
                Text("Edit Lyrics", fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (lyrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No lyrics added yet. Tap 'Edit Lyrics' or generate a song with Suno AI!",
                    color = Color(0xFF7A869E),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeIndex
                    val textColor by animateColorAsState(
                        targetValue = if (isActive) Color.White else Color(0xFF6B758E),
                        label = "lyric_color"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isActive) NeonPink.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (line.section.isNotBlank()) {
                            Text(
                                text = line.section,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) NeonAmber else Color(0xFF818DA7)
                            )
                        }
                        Text(
                            text = line.text,
                            fontSize = if (isActive) 16.sp else 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
