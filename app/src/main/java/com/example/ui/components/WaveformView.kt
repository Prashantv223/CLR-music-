package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun WaveformView(
    amplitudes: List<Float>,
    playheadProgress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    barColor: Color = NeonCyan,
    progressColor: Color = NeonPink,
    trimStart: Float = 0f, // 0f..1f
    trimEnd: Float = 1f,   // 0f..1f
    onSeek: (Float) -> Unit = {},
    onTrimChange: (Float, Float) -> Unit = { _, _ -> }
) {
    var isDraggingPlayhead by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val clickedProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(clickedProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val progress = (change.position.x / size.width).coerceIn(0f, 1f)
                    onSeek(progress)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val points = if (amplitudes.isEmpty()) List(60) { 0.2f } else amplitudes
        val barCount = points.size
        val barSpacing = width / barCount.toFloat()
        val barWidth = (barSpacing * 0.7f).coerceAtLeast(2f)

        // Draw center baseline
        drawLine(
            color = Color(0xFF1E2235),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.5f
        )

        // Draw waveform bars
        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val x = i * barSpacing + (barSpacing - barWidth) / 2f
            val amp = points[i].coerceIn(0.04f, 1.0f)
            val barHeight = (height * 0.88f) * amp
            val top = centerY - barHeight / 2f

            val isPlayed = progress <= playheadProgress
            val isInTrim = progress in trimStart..trimEnd

            val color = when {
                !isInTrim -> Color(0xFF33384F)
                isPlayed -> progressColor
                else -> barColor
            }

            drawRect(
                color = color,
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight)
            )
        }

        // Draw playhead vertical line
        val playheadX = (width * playheadProgress).coerceIn(0f, width)
        drawLine(
            color = Color.White,
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, height),
            strokeWidth = 3f
        )
        // Playhead indicator diamond/glow
        val headPath = Path().apply {
            moveTo(playheadX, 0f)
            lineTo(playheadX - 6f, 14f)
            lineTo(playheadX + 6f, 14f)
            close()
        }
        drawPath(headPath, color = NeonPink)
    }
}
