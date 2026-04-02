package com.devicelens.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.devicelens.app.R

// ── Font Family ───────────────────────────────────────────────────
val OutfitFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
)

// ── Color Palette ─────────────────────────────────────────────────
// Desaturated, considered. Not neon, not iOS.
val SafeGreen = Color(0xFF34D399)       // ~70% sat, easy on OLED
val WarningAmber = Color(0xFFFBBF24)    // warm gold, not screaming yellow
val RiskRed = Color(0xFFEF4444)         // firm red, not neon

val AccentTeal = Color(0xFF00D4AA)      // identity color — reads "secure"

val DarkSurface = Color(0xFF0A0A0B)
val DarkBackground = Color(0xFF050506)
val DarkCard = Color(0xFF141416)
val LightSurface = Color(0xFFF4F4F8)
val LightBackground = Color(0xFFFAFAFC)
val LightCard = Color(0xFFEFEFF3)

private val DarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = Color(0xFF00201A),
    secondary = SafeGreen,
    tertiary = WarningAmber,
    error = RiskRed,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = Color(0xFFE5E5EA),
    onSurface = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF2C2C2E),
    outlineVariant = Color(0xFF1C1C1E),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00A885),        // slightly darker teal for light mode readability
    onPrimary = Color.White,
    secondary = Color(0xFF059669),
    tertiary = Color(0xFFD97706),
    error = Color(0xFFDC2626),
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    onSurfaceVariant = Color(0xFF636366),
    outline = Color(0xFFD1D1D6),
    outlineVariant = Color(0xFFE5E5EA),
)

private val DeviceLensTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        letterSpacing = (-1.2).sp,
        lineHeight = 36.sp
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.6).sp,
        lineHeight = 28.sp
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.3).sp
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    titleSmall = Typography().titleSmall.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = OutfitFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.15).sp
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontFamily = OutfitFamily,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = Typography().bodySmall.copy(
        fontFamily = OutfitFamily,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = Typography().labelLarge.copy(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    labelMedium = Typography().labelMedium.copy(
        fontFamily = OutfitFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = Typography().labelSmall.copy(
        fontFamily = OutfitFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    ),
)

@Composable
fun DeviceLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use curated palette. No dynamic color override.
    // Brand identity > Material You wallpaper colors.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DeviceLensTypography,
        content = content
    )
}
