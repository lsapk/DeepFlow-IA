package com.deepflowia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(
    shape: Shape,
    color: Color,
    blurRadius: Dp = 8.dp
): Modifier = composed {
    this.then(
        Modifier
            .clip(shape)
            .background(color)
            .blur(radius = blurRadius)
    )
}
