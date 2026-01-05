package com.example.upcycle.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    secondary = EarthyOrange,
    tertiary = HunterGreen,
    background = TextDark,
    surface = TextDark,
    onPrimary = TextDark,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = WarmWhite,
    onSurface = WarmWhite,
    outline = SageGreen
)

private val LightColorScheme = lightColorScheme(
    primary = HunterGreen, 
    secondary = SageGreen,
    tertiary = EarthyOrange, 
    background = WarmWhite, 
    surface = Color.White, 
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = SageGreen,
    outlineVariant = OutlineGray
)

@Composable
fun UpCycleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}