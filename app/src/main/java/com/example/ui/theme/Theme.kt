package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = PureWhite,
    primaryContainer = Slate800,
    onPrimaryContainer = Indigo100,
    secondary = Cyan500,
    onSecondary = Slate950,
    tertiary = Purple500,
    background = Slate950,
    surface = Slate900,
    onBackground = Slate50,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = PureWhite,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo700,
    secondary = Cyan600,
    onSecondary = PureWhite,
    tertiary = Purple500,
    background = Slate50,
    surface = PureWhite,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200
)

@Composable
fun ShopMindAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ShopMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ShopMindAITheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun SmartCartAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ShopMindAITheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun SmartCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ShopMindAITheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
