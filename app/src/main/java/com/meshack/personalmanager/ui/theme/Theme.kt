package com.meshack.personalmanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightScheme = lightColorScheme(
    primary = Color(0xFF4F378B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Gold40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE9B8),
    onSecondaryContainer = Color(0xFF2A1F00),
    tertiary = Color(0xFF7D5260),
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFE7E0EC),
    error = Color(0xFFBA1A1A)
)

private val DarkScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Gold80,
    onSecondary = Color(0xFF2A1F00),
    secondaryContainer = Color(0xFF5A4410),
    onSecondaryContainer = Color(0xFFFFE9B8),
    tertiary = Color(0xFFEFB8C8),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF49454F),
    error = Color(0xFFFFB4AB)
)

@Composable
fun PersonalManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
