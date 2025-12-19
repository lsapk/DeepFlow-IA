package com.deepflowia.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.annotation.RequiresApi
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape

fun Modifier.glassmorphism(
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    blurRadius: Float = 25f
): Modifier = composed {
    val modifier = this.then(
        Modifier
            .clip(shape)
            .background(color)
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        modifier.then(
            Modifier.customBlur(blurRadius)
        )
    } else {
        modifier
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun Modifier.customBlur(blurRadius: Float) = this.then(
    Modifier.graphicsLayer {
        clip = true
        renderEffect = RenderEffect.createBlurEffect(
            blurRadius,
            blurRadius,
            Shader.TileMode.DECAL
        )
    }
)
