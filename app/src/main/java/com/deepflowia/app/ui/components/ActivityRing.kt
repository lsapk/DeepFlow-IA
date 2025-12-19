package com.deepflowia.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ActivityRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 12.dp,
    gradient: List<Color>,
    label: String,
    value: String
) {
    var animatedProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(progress) {
        animatedProgress = progress
    }

    val sweepAngle by animateFloatAsState(
        targetValue = animatedProgress * 360,
        animationSpec = tween(durationMillis = 1000)
    )

    val backgroundStrokeColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background ring
            drawArc(
                color = backgroundStrokeColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Progress ring
            drawArc(
                brush = Brush.sweepGradient(gradient),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
