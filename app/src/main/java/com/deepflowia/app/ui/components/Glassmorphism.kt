package com.deepflowia.app.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.glassmorphism(
    shape: Shape,
    color: Color,
    blurRadius: Float = 25f
): Modifier = composed {
    val glassmorphismModifier = this.then(
        Modifier
            .clip(shape)
            .background(color)
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        glassmorphismModifier.then(
            Modifier.graphicsLayer {
                renderEffect = RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.DECAL
                )
            }
        )
    } else {
        glassmorphismModifier
    }
}
