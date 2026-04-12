package com.devicelens.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.R

// ═════════════════════════════════════════════════════════════════════════════
// PREMIUM UI/UX TRANSFORMATION — ETHREAL GLASS ARCHITECTURE
// Agency-grade design system for DeviceLens Security App
// ═════════════════════════════════════════════════════════════════════════════

// ── Typography ───────────────────────────────────────────────────────────────
// Premium geometric sans-serif with superior legibility
val PlusJakartaFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
)

// Monospace for technical data (MAC, IP addresses)
val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_regular, FontWeight.Medium),
)

// ── Animation Easing Curves ───────────────────────────────────────────────────
// Premium cubic-bezier curves — NEVER use linear or ease-in-out
val EaseOutExpo = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
val EaseOutQuart = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f)
val EaseOutCubic = CubicBezierEasing(0.33f, 1.0f, 0.68f, 1.0f)
val EaseSpring = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)
val EaseInOutQuint = CubicBezierEasing(0.83f, 0.0f, 0.17f, 1.0f)

// ── Ultra-Premium Color System ───────────────────────────────────────────────
// Vibe Archetype: ETHREAL GLASS — Deep OLED, radial mesh gradients

// Absolute blacks for OLED optimization
val AbsoluteBlack = Color(0xFF000000)
val OLEDBlack = Color(0xFF050505)
val VantaBlack = Color(0xFF0A0A0B)
val DeepVoid = Color(0xFF0D0D0F)

// Elevated surfaces with glass morphism
val SurfaceElevated = Color(0xFF111113)
val SurfaceGlass = Color(0xFF161618)
val SurfaceGlassHighlight = Color(0xFF1C1C1F)
val SurfaceGlassBorder = Color(0xFF2A2A2E)

// Hairline borders for glass containers
val HairlineBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val HairlineBorderStrong = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val InnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.05f)

// Primary accent — Security Teal with depth
val SecurityTeal = Color(0xFF00D4AA)
val SecurityTealDim = Color(0xFF00A885)
val SecurityTealGlow = Color(0xFF00D4AA).copy(alpha = 0.4f)

// Status colors — refined saturations
val StatusSafe = Color(0xFF34D399)
val StatusSafeGlow = Color(0xFF34D399).copy(alpha = 0.3f)
val StatusWarning = Color(0xFFFBBF24)
val StatusWarningGlow = Color(0xFFFBBF24).copy(alpha = 0.3f)
val StatusRisk = Color(0xFFEF4444)
val StatusRiskGlow = Color(0xFFEF4444).copy(alpha = 0.3f)

// Mesh gradient colors — subtle radial orbs
val MeshPurple = Color(0xFF7C3AED).copy(alpha = 0.15f)
val MeshTeal = Color(0xFF14B8A6).copy(alpha = 0.12f)
val MeshAmber = Color(0xFFF59E0B).copy(alpha = 0.08f)

// Text colors with perfect contrast hierarchy
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.87f)
val TextTertiary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
val TextQuaternary = Color(0xFFEBEBF5).copy(alpha = 0.38f)

// Light mode surfaces — Ethereal Glass for Light
val LightSurface = Color(0xFFF8F8FA)
val LightSurfaceElevated = Color(0xFFFFFFFF)
val LightSurfaceGlass = Color(0xFFF4F4F6)
val LightSurfaceGlassHighlight = Color(0xFFFAFAFC)
val LightSurfaceGlassBorder = Color(0xFFE5E5EA)

// Light mode accents — softer but still vibrant
val LightSecurityTeal = Color(0xFF0D9488)
val LightSecurityTealDim = Color(0xFF0F766E)
val LightSecurityTealGlow = Color(0xFF14B8A6).copy(alpha = 0.25f)

// Light mode status colors
val LightStatusSafe = Color(0xFF059669)
val LightStatusSafeGlow = Color(0xFF10B981).copy(alpha = 0.2f)
val LightStatusWarning = Color(0xFFB45309)
val LightStatusWarningGlow = Color(0xFFF59E0B).copy(alpha = 0.2f)
val LightStatusRisk = Color(0xFFDC2626)
val LightStatusRiskGlow = Color(0xFFEF4444).copy(alpha = 0.2f)

// Light mode mesh gradients
val LightMeshPurple = Color(0xFF8B5CF6).copy(alpha = 0.08f)
val LightMeshTeal = Color(0xFF14B8A6).copy(alpha = 0.06f)
val LightMeshAmber = Color(0xFFF59E0B).copy(alpha = 0.04f)

// Light mode text
val LightTextPrimary = Color(0xFF1C1C1E)
val LightTextSecondary = Color(0xFF3A3A3C)
val LightTextTertiary = Color(0xFF636366)
val LightTextQuaternary = Color(0xFF8E8E93)

// ── Extended Color Scheme ───────────────────────────────────────────────────
// Custom colors beyond Material 3 for premium glass-morphism effects
class ExtendedColors(
    // Surfaces
    val surfaceGlass: Color = Color.Unspecified,
    val surfaceGlassHighlight: Color = Color.Unspecified,
    val surfaceGlassBorder: Color = Color.Unspecified,
    val hairlineBorder: Color = Color.Unspecified,
    val hairlineBorderStrong: Color = Color.Unspecified,
    val innerHighlight: Color = Color.Unspecified,

    // Mesh gradients
    val meshPurple: Color = Color.Unspecified,
    val meshTeal: Color = Color.Unspecified,
    val meshAmber: Color = Color.Unspecified,

    // Status glows
    val statusSafeGlow: Color = Color.Unspecified,
    val statusWarningGlow: Color = Color.Unspecified,
    val statusRiskGlow: Color = Color.Unspecified,
    val securityTealGlow: Color = Color.Unspecified,

    // Text hierarchy
    val textTertiary: Color = Color.Unspecified,
    val textQuaternary: Color = Color.Unspecified,

    // Semantic status colors (theme-aware)
    val statusSafe: Color = Color.Unspecified,
    val statusWarning: Color = Color.Unspecified,
    val statusRisk: Color = Color.Unspecified,
    val securityTeal: Color = Color.Unspecified,
)

// Composition local for extended colors
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// ── Material 3 Color Schemes ─────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = SecurityTeal,
    onPrimary = AbsoluteBlack,
    primaryContainer = SecurityTeal.copy(alpha = 0.15f),
    onPrimaryContainer = SecurityTeal,

    secondary = StatusSafe,
    onSecondary = AbsoluteBlack,
    secondaryContainer = StatusSafe.copy(alpha = 0.15f),
    onSecondaryContainer = StatusSafe,

    tertiary = StatusWarning,
    onTertiary = AbsoluteBlack,
    tertiaryContainer = StatusWarning.copy(alpha = 0.15f),
    onTertiaryContainer = StatusWarning,

    error = StatusRisk,
    onError = AbsoluteBlack,
    errorContainer = StatusRisk.copy(alpha = 0.15f),
    onErrorContainer = StatusRisk,

    background = OLEDBlack,
    onBackground = TextPrimary,

    surface = VantaBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextTertiary,

    outline = HairlineBorder,
    outlineVariant = SurfaceGlassBorder,

    scrim = AbsoluteBlack.copy(alpha = 0.8f),
)

private val LightColorScheme = lightColorScheme(
    primary = SecurityTealDim,
    onPrimary = Color.White,
    primaryContainer = SecurityTeal.copy(alpha = 0.1f),
    onPrimaryContainer = SecurityTealDim,

    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = StatusSafe.copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFF059669),

    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = StatusWarning.copy(alpha = 0.1f),
    onTertiaryContainer = Color(0xFFD97706),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = StatusRisk.copy(alpha = 0.1f),
    onErrorContainer = Color(0xFFDC2626),

    background = LightSurface,
    onBackground = Color(0xFF1C1C1E),

    surface = LightSurfaceElevated,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = LightSurfaceGlass,
    onSurfaceVariant = Color(0xFF636366),

    outline = Color(0xFFD1D1D6),
    outlineVariant = Color(0xFFE5E5EA),
)

// ── Premium Typography Scale ─────────────────────────────────────────────────
private val PremiumTypography = Typography(
    // Display styles — massive, editorial
    displayLarge = Typography().displayLarge.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-2.5).sp,
    ),
    displayMedium = Typography().displayMedium.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1.8).sp,
    ),
    displaySmall = Typography().displaySmall.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.2).sp,
    ),

    // Headlines — tight tracking, substantial weight
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = (-1.2).sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = (-0.8).sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 28.sp,
    ),

    // Titles — clean, authoritative
    titleLarge = Typography().titleLarge.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.3).sp,
        lineHeight = 28.sp,
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = (-0.2).sp,
        lineHeight = 24.sp,
    ),
    titleSmall = Typography().titleSmall.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = (-0.1).sp,
        lineHeight = 20.sp,
    ),

    // Body — comfortable reading
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    bodySmall = Typography().bodySmall.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),

    // Labels — small, functional
    labelLarge = Typography().labelLarge.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = (0.1).sp,
        lineHeight = 20.sp,
    ),
    labelMedium = Typography().labelMedium.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = (0.15).sp,
        lineHeight = 16.sp,
    ),
    labelSmall = Typography().labelSmall.copy(
        fontFamily = PlusJakartaFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = (0.2).sp,
        lineHeight = 14.sp,
    ),
)

// ── Shape System ─────────────────────────────────────────────────────────────
// Exaggerated squircle radii for premium feel
val ShapeExtraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
val ShapeLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
val ShapeMedium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
val ShapeSmall = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
val ShapeExtraSmall = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
val ShapePill = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50)

// Double-bezel radii calculations
// Usage: Outer = 24.dp, Inner = calcInnerRadius(24.dp, 6.dp)
fun calcInnerRadius(outerRadius: androidx.compose.ui.unit.Dp, padding: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp {
    return outerRadius - padding
}

// ── Theme Composable ─────────────────────────────────────────────────────────
@Composable
fun DeviceLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        ExtendedColors(
            // Dark theme glass surfaces
            surfaceGlass = SurfaceGlass,
            surfaceGlassHighlight = SurfaceGlassHighlight,
            surfaceGlassBorder = SurfaceGlassBorder,
            hairlineBorder = HairlineBorder,
            hairlineBorderStrong = HairlineBorderStrong,
            innerHighlight = InnerHighlight,

            // Dark theme mesh
            meshPurple = MeshPurple,
            meshTeal = MeshTeal,
            meshAmber = MeshAmber,

            // Dark theme glows
            statusSafeGlow = StatusSafeGlow,
            statusWarningGlow = StatusWarningGlow,
            statusRiskGlow = StatusRiskGlow,
            securityTealGlow = SecurityTealGlow,

            // Dark theme text
            textTertiary = TextTertiary,
            textQuaternary = TextQuaternary,

            // Dark theme semantic colors
            statusSafe = StatusSafe,
            statusWarning = StatusWarning,
            statusRisk = StatusRisk,
            securityTeal = SecurityTeal,
        )
    } else {
        ExtendedColors(
            // Light theme glass surfaces
            surfaceGlass = LightSurfaceGlass,
            surfaceGlassHighlight = LightSurfaceGlassHighlight,
            surfaceGlassBorder = LightSurfaceGlassBorder,
            hairlineBorder = Color(0xFF000000).copy(alpha = 0.06f),
            hairlineBorderStrong = Color(0xFF000000).copy(alpha = 0.1f),
            innerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.8f),

            // Light theme mesh
            meshPurple = LightMeshPurple,
            meshTeal = LightMeshTeal,
            meshAmber = LightMeshAmber,

            // Light theme glows
            statusSafeGlow = LightStatusSafeGlow,
            statusWarningGlow = LightStatusWarningGlow,
            statusRiskGlow = LightStatusRiskGlow,
            securityTealGlow = LightSecurityTealGlow,

            // Light theme text
            textTertiary = LightTextTertiary,
            textQuaternary = LightTextQuaternary,

            // Light theme semantic colors
            statusSafe = LightStatusSafe,
            statusWarning = LightStatusWarning,
            statusRisk = LightStatusRisk,
            securityTeal = LightSecurityTeal,
        )
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PremiumTypography,
            content = content
        )
    }
}

// Helper to access extended colors
object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
