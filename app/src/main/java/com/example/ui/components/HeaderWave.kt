package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.WaveLightBlue
import com.example.ui.theme.WaveSubtleBlue

/**
 * Custom curved header with layered wave shapes matching the Get Vidya design specification.
 */
@Composable
fun HeaderWave(
    modifier: Modifier = Modifier,
    headerHeight: Dp = 270.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        val width = size.width
        val height = size.height

        // 1. Lightest subtle under-wave
        val underWavePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.68f)
            cubicTo(
                width * 0.85f, height * 0.82f,
                width * 0.58f, height * 0.54f,
                width * 0.42f, height * 0.50f
            )
            cubicTo(
                width * 0.22f, height * 0.46f,
                width * 0.10f, height * 0.72f,
                0f, height * 0.78f
            )
            close()
        }
        drawPath(
            path = underWavePath,
            color = WaveSubtleBlue
        )

        // 2. Light blue accent wave
        val lightWavePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.56f)
            cubicTo(
                width * 0.88f, height * 0.70f,
                width * 0.62f, height * 0.46f,
                width * 0.45f, height * 0.42f
            )
            cubicTo(
                width * 0.25f, height * 0.38f,
                width * 0.12f, height * 0.62f,
                0f, height * 0.68f
            )
            close()
        }
        drawPath(
            path = lightWavePath,
            color = WaveLightBlue
        )

        // 3. Deep royal blue main header wave
        val mainWavePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.46f)
            cubicTo(
                width * 0.88f, height * 0.58f,
                width * 0.65f, height * 0.36f,
                width * 0.46f, height * 0.33f
            )
            cubicTo(
                width * 0.26f, height * 0.30f,
                width * 0.12f, height * 0.50f,
                0f, height * 0.56f
            )
            close()
        }
        drawPath(
            path = mainWavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    DarkNavy,
                    RoyalBlue
                )
            )
        )
    }
}
