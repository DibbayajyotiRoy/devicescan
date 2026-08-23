package com.devicelens.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devicelens.app.R

// ═════════════════════════════════════════════════════════════════════
// The theme. See DesignTokens.kt for the reasoning behind the values.
// ═════════════════════════════════════════════════════════════════════

// ── Type families ────────────────────────────────────────────────────

/** Outfit: a geometric sans with a tall x-height, legible at small sizes. */
val PlusJakartaFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
)

/**
 * JetBrains Mono, used for every value the *machine* produced — MAC addresses,
 * IPs, ports, subnets, signal strengths. Monospace does two jobs here: hex
 * columns line up, and the reader can tell measured fact from our prose without
 * reading a word.
 */
val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_regular, FontWeight.Medium),
)

// ── Easing ───────────────────────────────────────────────────────────
// Decelerating curves only. Motion should arrive gently and leave decisively;
// nothing in a real interface accelerates into its resting position.
val EaseOutExpo = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
val EaseOutQuart = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f)
val EaseOutCubic = CubicBezierEasing(0.33f, 1.0f, 0.68f, 1.0f)
val EaseSpring = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)
val EaseInOutQuint = CubicBezierEasing(0.83f, 0.0f, 0.17f, 1.0f)

// ── Semantic colours ─────────────────────────────────────────────────

/**
 * Colours Material 3 has no slot for.
 *
 * The property names are deliberately about *meaning* — `statusRisk`, not
 * `red`; `surfaceGlass`, not `grey900`. A name that describes appearance goes
 * stale the moment the palette changes; a name that describes intent does not.
 */
@Immutable
class ExtendedColors(
    // Surface ramp
    val surfaceGlass: Color = Color.Unspecified,
    val surfaceGlassHighlight: Color = Color.Unspecified,
    val surfaceGlassBorder: Color = Color.Unspecified,
    val hairlineBorder: Color = Color.Unspecified,
    val hairlineBorderStrong: Color = Color.Unspecified,
    val innerHighlight: Color = Color.Unspecified,

    // Retained so existing call sites keep compiling. These are now near-neutral
    // by design: the old decorative purple/amber orbs competed with the status
    // colours, which are the only thing on screen the user actually needs to read.
    val meshPurple: Color = Color.Unspecified,
    val meshTeal: Color = Color.Unspecified,
    val meshAmber: Color = Color.Unspecified,

    // Status glows, used for focus rings and halos — never as fills.
    val statusSafeGlow: Color = Color.Unspecified,
    val statusWarningGlow: Color = Color.Unspecified,
    val statusRiskGlow: Color = Color.Unspecified,
    val securityTealGlow: Color = Color.Unspecified,

    // Text ramp
    val textPrimary: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val textTertiary: Color = Color.Unspecified,
    val textQuaternary: Color = Color.Unspecified,

    // The four meaningful hues
    val statusSafe: Color = Color.Unspecified,
    val statusWarning: Color = Color.Unspecified,
    val statusRisk: Color = Color.Unspecified,
    /** The interactive accent. Named for history; it is the accent, not a brand teal. */
    val securityTeal: Color = Color.Unspecified,

    val canvas: Color = Color.Unspecified,
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

private val DarkExtended = ExtendedColors(
    surfaceGlass = Ink.surface,
    surfaceGlassHighlight = Ink.surfaceHigh,
    surfaceGlassBorder = Ink.surfaceMax,
    hairlineBorder = Ink.hairline,
    hairlineBorderStrong = Ink.hairlineStrong,
    innerHighlight = Color.White.copy(alpha = 0.04f),

    meshPurple = Color.White.copy(alpha = 0.02f),
    meshTeal = Signal.accentDark.copy(alpha = 0.05f),
    meshAmber = Color.White.copy(alpha = 0.015f),

    statusSafeGlow = Signal.clearDark.copy(alpha = 0.22f),
    statusWarningGlow = Signal.cautionDark.copy(alpha = 0.22f),
    statusRiskGlow = Signal.threatDark.copy(alpha = 0.22f),
    securityTealGlow = Signal.accentDark.copy(alpha = 0.22f),

    textPrimary = Ink.textPrimary,
    textSecondary = Ink.textSecondary,
    textTertiary = Ink.textTertiary,
    textQuaternary = Ink.textQuaternary,

    statusSafe = Signal.clearDark,
    statusWarning = Signal.cautionDark,
    statusRisk = Signal.threatDark,
    securityTeal = Signal.accentDark,

    canvas = Ink.canvas,
)

private val LightExtended = ExtendedColors(
    surfaceGlass = Paper.surface,
    surfaceGlassHighlight = Paper.surfaceHigh,
    surfaceGlassBorder = Paper.surfaceMax,
    hairlineBorder = Paper.hairline,
    hairlineBorderStrong = Paper.hairlineStrong,
    innerHighlight = Color.White.copy(alpha = 0.9f),

    meshPurple = Color.Black.copy(alpha = 0.015f),
    meshTeal = Signal.accentLight.copy(alpha = 0.04f),
    meshAmber = Color.Black.copy(alpha = 0.01f),

    statusSafeGlow = Signal.clearLight.copy(alpha = 0.16f),
    statusWarningGlow = Signal.cautionLight.copy(alpha = 0.16f),
    statusRiskGlow = Signal.threatLight.copy(alpha = 0.16f),
    securityTealGlow = Signal.accentLight.copy(alpha = 0.16f),

    textPrimary = Paper.textPrimary,
    textSecondary = Paper.textSecondary,
    textTertiary = Paper.textTertiary,
    textQuaternary = Paper.textQuaternary,

    statusSafe = Signal.clearLight,
    statusWarning = Signal.cautionLight,
    statusRisk = Signal.threatLight,
    securityTeal = Signal.accentLight,

    canvas = Paper.canvas,
)

// ── Material 3 schemes ───────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = Signal.accentDark,
    onPrimary = Color(0xFF04121F),
    primaryContainer = Signal.accentDark.copy(alpha = Tint.subtle),
    onPrimaryContainer = Signal.accentDark,

    secondary = Signal.clearDark,
    onSecondary = Color(0xFF04140A),
    secondaryContainer = Signal.clearDark.copy(alpha = Tint.subtle),
    onSecondaryContainer = Signal.clearDark,

    tertiary = Signal.cautionDark,
    onTertiary = Color(0xFF161200),
    tertiaryContainer = Signal.cautionDark.copy(alpha = Tint.subtle),
    onTertiaryContainer = Signal.cautionDark,

    error = Signal.threatDark,
    onError = Color(0xFF1A0503),
    errorContainer = Signal.threatDark.copy(alpha = Tint.subtle),
    onErrorContainer = Signal.threatDark,

    background = Ink.canvas,
    onBackground = Ink.textPrimary,

    surface = Ink.surface,
    onSurface = Ink.textPrimary,
    surfaceVariant = Ink.surfaceHigh,
    onSurfaceVariant = Ink.textSecondary,

    surfaceContainerLowest = Ink.canvas,
    surfaceContainerLow = Ink.surface,
    surfaceContainer = Ink.surfaceHigh,
    surfaceContainerHigh = Ink.surfaceMax,
    surfaceContainerHighest = Ink.surfaceMax,

    outline = Ink.hairlineStrong,
    outlineVariant = Ink.hairline,

    scrim = Color.Black.copy(alpha = 0.6f),
)

private val LightColorScheme = lightColorScheme(
    primary = Signal.accentLight,
    onPrimary = Color.White,
    primaryContainer = Signal.accentLight.copy(alpha = Tint.subtle),
    onPrimaryContainer = Signal.accentLight,

    secondary = Signal.clearLight,
    onSecondary = Color.White,
    secondaryContainer = Signal.clearLight.copy(alpha = Tint.subtle),
    onSecondaryContainer = Signal.clearLight,

    tertiary = Signal.cautionLight,
    onTertiary = Color.White,
    tertiaryContainer = Signal.cautionLight.copy(alpha = Tint.subtle),
    onTertiaryContainer = Signal.cautionLight,

    error = Signal.threatLight,
    onError = Color.White,
    errorContainer = Signal.threatLight.copy(alpha = Tint.subtle),
    onErrorContainer = Signal.threatLight,

    background = Paper.canvas,
    onBackground = Paper.textPrimary,

    surface = Paper.surface,
    onSurface = Paper.textPrimary,
    surfaceVariant = Paper.surfaceHigh,
    onSurfaceVariant = Paper.textSecondary,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Paper.canvas,
    surfaceContainer = Paper.surfaceHigh,
    surfaceContainerHigh = Paper.surfaceMax,
    surfaceContainerHighest = Paper.surfaceMax,

    outline = Paper.hairlineStrong,
    outlineVariant = Paper.hairline,

    scrim = Color.Black.copy(alpha = 0.35f),
)

// ── Typography ───────────────────────────────────────────────────────

/**
 * Tracking is size-specific, never one value for everything.
 *
 * Letterforms drift apart optically as they grow, so display sizes are pulled
 * tight (negative tracking) while small labels are opened up slightly. A single
 * `letterSpacing` applied across a scale is always wrong at one end of it.
 *
 * Line height follows the inverse rule: tight on headlines where the eye
 * travels a short distance, generous on body copy where it has to find the next
 * line reliably.
 */
private val LineHeightTrim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun display(size: Int, weight: FontWeight, tracking: Float, leading: Int) = TextStyle(
    fontFamily = PlusJakartaFamily,
    fontWeight = weight,
    fontSize = size.sp,
    letterSpacing = tracking.sp,
    lineHeight = leading.sp,
    lineHeightStyle = LineHeightTrim,
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

private val AppTypography = Typography(
    displayLarge = display(56, FontWeight.ExtraBold, -2.4f, 58),
    displayMedium = display(44, FontWeight.ExtraBold, -1.8f, 48),
    displaySmall = display(36, FontWeight.Bold, -1.4f, 40),

    headlineLarge = display(30, FontWeight.Bold, -1.0f, 36),
    headlineMedium = display(25, FontWeight.Bold, -0.7f, 31),
    headlineSmall = display(21, FontWeight.SemiBold, -0.5f, 27),

    titleLarge = display(18, FontWeight.SemiBold, -0.3f, 24),
    titleMedium = display(16, FontWeight.SemiBold, -0.2f, 22),
    titleSmall = display(14, FontWeight.SemiBold, -0.1f, 19),

    // Body copy sits at 0 tracking and generous leading — this is what people
    // actually read, and it is the one place where comfort beats compactness.
    bodyLarge = display(16, FontWeight.Normal, 0f, 25),
    bodyMedium = display(14, FontWeight.Normal, 0f, 21),
    bodySmall = display(13, FontWeight.Normal, 0.05f, 19),

    labelLarge = display(14, FontWeight.SemiBold, 0.1f, 18),
    labelMedium = display(12, FontWeight.Medium, 0.2f, 16),
    labelSmall = display(11, FontWeight.Medium, 0.35f, 14),
)

/**
 * Styles for machine-produced values. Not part of the Material scale because
 * they are a parallel voice, not a step in the same hierarchy.
 */
object MonoType {
    val large = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 20.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    val medium = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 18.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    val small = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.3.sp,
        lineHeight = 15.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
}

// ── Legacy shape aliases ─────────────────────────────────────────────
// Kept so existing screens compile; all now resolve to the Radius ramp so the
// app has one shape language rather than seven ad-hoc values.
val ShapeExtraLarge = Radius.xl
val ShapeLarge = Radius.lg
val ShapeMedium = Radius.md
val ShapeSmall = Radius.sm
val ShapeExtraSmall = Radius.xs
val ShapePill = Radius.full

/** Inner radius of a nested shape, so concentric corners stay concentric. */
fun calcInnerRadius(
    outerRadius: androidx.compose.ui.unit.Dp,
    padding: androidx.compose.ui.unit.Dp
): androidx.compose.ui.unit.Dp = (outerRadius - padding).coerceAtLeast(0.dp)

// ── Theme ────────────────────────────────────────────────────────────

@Composable
fun DeviceLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtended else LightExtended
    val reducedMotion = rememberReducedMotion()

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalReducedMotion provides reducedMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
