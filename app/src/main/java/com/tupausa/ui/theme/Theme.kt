package com.tupausa.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable


private val LightColorScheme = lightColorScheme(
    primary = ArenaPrimary,
    onPrimary = ArenaOnPrimary,
    primaryContainer = ArenaPrimaryContainer,
    onPrimaryContainer = ArenaOnPrimaryContainer,

    background = ArenaBackground,
    onBackground = ArenaOnSurface,

    surface = ArenaSurface,
    onSurface = ArenaOnSurface,

    onSurfaceVariant = ArenaOnSurfaceVariant,


    outline = ArenaOutline
)

// Esquema Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB951),
    onPrimary = Color(0xFF452B00),
    background = Color(0xFF1E1B16),
    surface = Color(0xFF332A22),
    onSurface = Color(0xFFEDE0D4)
)

@Composable
fun TuPausaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Barra de estado del color arena primary o transparente
            window.statusBarColor = ArenaPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}