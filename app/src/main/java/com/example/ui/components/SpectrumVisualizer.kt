package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun SpectrumVisualizer(
    spectrumData: List<Float>,
    modifier: Modifier = Modifier.fillMaxWidth().height(44.dp)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = 16
        val barSpacing = width / barCount
        val barWidth = barSpacing * 0.72f

        for (i in 0 until barCount) {
            val level = spectrumData.getOrElse(i) { 0.1f }.coerceIn(0.05f, 1.0f)
            val barHeight = height * level
            val x = i * barSpacing + (barSpacing - barWidth) / 2f
            val y = height - barHeight

            val gradient = Brush.verticalGradient(
                colors = listOf(
                    NeonPink,
                    NeonAmber,
                    NeonCyan
                ),
                startY = y,
                endY = height
            )

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}
