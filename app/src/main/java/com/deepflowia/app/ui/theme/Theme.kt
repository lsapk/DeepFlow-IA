package com.deepflowia.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = dark_primary,
    background = dark_background,
    surface = dark_surface,
    onPrimary = dark_onPrimary,
    onBackground = dark_onBackground,
    onSurface = dark_onSurface,
    // Vous pouvez mapper les autres couleurs ici si nécessaire
    secondary = dark_primary,
    onSecondary = dark_onPrimary,
    tertiary = dark_primary,
    onTertiary = dark_onPrimary,
    surfaceVariant = dark_surface,
    onSurfaceVariant = dark_text_secondary
)

private val LightColorScheme = lightColorScheme(
    primary = light_primary,
    background = light_background,
    surface = light_surface,
    onPrimary = light_onPrimary,
    onBackground = light_onBackground,
    onSurface = light_onSurface,
    // Vous pouvez mapper les autres couleurs ici si nécessaire
    secondary = light_primary,
    onSecondary = light_onPrimary,
    tertiary = light_primary,
    onTertiary = light_onPrimary,
    surfaceVariant = light_fill_quaternary,
    onSurfaceVariant = light_text_secondary
)

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun DeepFlowIATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Rendre la barre de navigation transparente pour l'effet "Liquid Glass"
            window.navigationBarColor = colorScheme.surface.copy(alpha = 0.5f).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
