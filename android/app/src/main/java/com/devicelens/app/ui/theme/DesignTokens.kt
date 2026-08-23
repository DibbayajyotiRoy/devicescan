package com.devicelens.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The design system, stated once.
 *
 * ── The direction: *instrument* ──────────────────────────────────────
 *
 * This app measures the invisible and reports a verdict. It should feel like a
 * precision instrument, not a consumer toy and not a dashboard: quiet, dense,
 * confident, and legible at a glance in a dark room — which is exactly where
 * someone checks a hotel room for cameras.
 *
 * Three rules follow from that, and every value below obeys them.
 *
 * **1. Colour is information, never decoration.**
 * The canvas and every surface are neutral greys. Exactly four hues exist in
 * the whole app and each one *means* something: clear, unidentified, threat,
 * and interactive. A decorative gradient would compete with the only signal
 * the user came here to read. If a colour is not carrying a verdict or marking
 * something tappable, it should not be on screen.
 *
 * **2. Machine identity is set in monospace.**
 * MAC addresses, IPs, ports and subnets are set in JetBrains Mono; prose is set
 * in Outfit. The reader can then tell *fact* from *interpretation* without
 * reading a word — and columns of hex actually align.
 *
 * **3. One scale for each property.**
 * One spacing ramp, one radius ramp, one elevation ramp. The previous design
 * used seven different corner radii — 12, 14, 16, 20, 23, 24 and 32 — often two
 * of them nested. That inconsistency is invisible individually and reads as
 * carelessness collectively.
 */

// ── Spacing ──────────────────────────────────────────────────────────
// A 4dp base grid. Every margin, gap and inset in the app is one of these.
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val x3l = 32.dp
    val x4l = 40.dp
    val x5l = 56.dp
    val x6l = 72.dp
}

// ── Radii ────────────────────────────────────────────────────────────
// Four steps, chosen so a nested shape can always be derived from its parent
// (inner = outer − padding) instead of being guessed.
object Radius {
    val xs = RoundedCornerShape(8.dp)
    val sm = RoundedCornerShape(12.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(22.dp)
    val xl = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(percent = 50)

    val xsDp = 8.dp
    val smDp = 12.dp
    val mdDp = 16.dp
    val lgDp = 22.dp
    val xlDp = 28.dp
}

// ── Dark palette ─────────────────────────────────────────────────────
// Not pure black: #000 against an OLED bezel makes hairlines disappear and
// makes any elevation impossible to read. A hair above black keeps the surface
// ramp visible while still switching most pixels off.

object Ink {
    val canvas = Color(0xFF08090B)
    val surface = Color(0xFF101215)
    val surfaceHigh = Color(0xFF171A1E)
    val surfaceMax = Color(0xFF1E2227)

    val hairline = Color(0xFFFFFFFF).copy(alpha = 0.07f)
    val hairlineStrong = Color(0xFFFFFFFF).copy(alpha = 0.13f)

    val textPrimary = Color(0xFFF6F7F8)
    val textSecondary = Color(0xFF9BA1A9)
    val textTertiary = Color(0xFF6B7078)
    val textQuaternary = Color(0xFF4A4F57)
}

object Paper {
    val canvas = Color(0xFFFAFAFB)
    val surface = Color(0xFFFFFFFF)
    val surfaceHigh = Color(0xFFF3F4F6)
    val surfaceMax = Color(0xFFE9EBEE)

    val hairline = Color(0xFF000000).copy(alpha = 0.07f)
    val hairlineStrong = Color(0xFF000000).copy(alpha = 0.13f)

    val textPrimary = Color(0xFF0B0C0E)
    val textSecondary = Color(0xFF565C64)
    val textTertiary = Color(0xFF848A93)
    val textQuaternary = Color(0xFFA8AEB6)
}

/**
 * The four meaningful hues.
 *
 * Tuned per theme rather than reused, because a colour that is legible on black
 * is usually too light on white. Each pair holds roughly the same perceived
 * position in its own context, so a green reads as "clear" in both.
 */
object Signal {
    // Dark
    val clearDark = Color(0xFF32D74B)      // verified / yours
    val cautionDark = Color(0xFFFFD426)    // unidentified
    val threatDark = Color(0xFFFF453A)     // active concern
    val accentDark = Color(0xFF4E8CFF)     // interactive, progress, focus

    // Light
    val clearLight = Color(0xFF128A3A)
    val cautionLight = Color(0xFF9A6300)
    val threatLight = Color(0xFFD22B20)
    val accentLight = Color(0xFF0A5FE0)
}

/**
 * How strongly a colour tints a surface behind it.
 *
 * Named rather than sprinkled as literals, so a "subtle tint" is the same
 * strength on every screen. Tints stay low on purpose: a status colour should
 * mark a card, not flood it.
 */
object Tint {
    const val faint = 0.06f
    const val subtle = 0.10f
    const val medium = 0.16f
    const val strong = 0.24f
}

/** Long-form text needs more line height than the Material defaults give it. */
object Leading {
    const val body = 1.55f
    const val tight = 1.25f
}
