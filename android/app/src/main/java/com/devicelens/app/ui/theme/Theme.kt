package com.devicelens.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Color Palette ──────────────────────────────────────────────────
val SafeGreen = Color(0xFF00FFB2)
val WarningAmber = Color(0xFFFFD60A)
val RiskRed = Color(0xFFFF2D55)

val DarkSurface = Color(0xFF0A0A0B)
val DarkBackground = Color(0xFF050505)
val DarkCard = Color(0xFF151517)
val LightSurface = Color(0xFFF2F2F7)
val LightBackground = Color(0xFFFFFFFF)
val LightCard = Color(0xFFF9F9FB)

val AccentBlue = Color(0xFF007AFF)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = SafeGreen,
    tertiary = WarningAmber,
    error = RiskRed,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = Color(0xFFE5E5E7),
    onSurface = Color(0xFFE5E5E7),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF38383A),
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = SafeGreen,
    tertiary = WarningAmber,
    error = RiskRed,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    outline = Color(0xFFD1D1D6),
)

private val DeviceLensTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = Typography().titleLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = Typography().titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    labelLarge = Typography().labelLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
    ),
    labelMedium = Typography().labelMedium.copy(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    ),
)

@Composable
fun DeviceLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            darkColorScheme()
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> {
            lightColorScheme()
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DeviceLensTypography,
        content = content
    )
}
