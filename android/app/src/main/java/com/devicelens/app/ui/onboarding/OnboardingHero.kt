package com.devicelens.app.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.LocalReducedMotion
import kotlin.math.cos
import kotlin.math.sin

/**
 * The onboarding illustrations.
 *
 * One motif, four states — deliberately, rather than four unrelated pictures.
 * Every slide draws the same radar field; what changes is what is *in* it. By
 * the fourth slide the user has learned to read the field, so the last image
 * needs no explanation. Four unrelated stock icons would have taught them
 * nothing and would have looked like every other app's onboarding.
 *
 * Drawn rather than shipped as assets: a Canvas is a few kilobytes of code
 * instead of four sets of density-specific PNGs, it re-colours itself for light
 * and dark automatically, and it can animate in response to the page.
 *
 * Cost control — the whole reason this is affordable:
 *  - Geometry is computed once per size, not per frame.
 *  - A single `Animatable` per page drives the entrance; it runs once and stops.
 *  - The only continuous animation is the sweep, and it exists solely on the
 *    page that is currently in front of the user.
 */
enum class HeroVariant { FIELD, LENS, TRAIL, ENCLOSURE }

@Composable
fun OnboardingHero(
    variant: HeroVariant,
    isActive: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors
    val reducedMotion = LocalReducedMotion.current

    // Entrance progress. Driven once when the page becomes current; a page the
    // user has swiped away from stops animating entirely.
    val entrance = remember(variant) { Animatable(0f) }
    LaunchedEffect(isActive, reducedMotion) {
        if (isActive) {
            entrance.animateTo(
                targetValue = 1f,
                animationSpec = tween(if (reducedMotion) 1 else 900, easing = LinearEasing)
            )
        } else {
            entrance.snapTo(0f)
        }
    }

    // The radar sweep only turns while its own page is on screen and the user
    // has not asked for reduced motion.
    val sweepAngle = if (isActive && !reducedMotion) {
        val transition = rememberInfiniteTransition(label = "sweep")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
            label = "sweepAngle"
        )
        angle
    } else {
        0f
    }

    val accent = colors.securityTeal
    val threat = colors.statusRisk
    val safe = colors.statusSafe
    val hairline = colors.hairlineBorderStrong
    val faint = colors.hairlineBorder

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = entrance.value
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            drawRadarField(center, maxRadius, progress, hairline, faint)

            if (!reducedMotion && variant != HeroVariant.ENCLOSURE) {
                drawSweep(center, maxRadius, sweepAngle, accent)
            }

            when (variant) {
                HeroVariant.FIELD -> drawDeviceField(center, maxRadius, progress, accent, hairline)
                HeroVariant.LENS -> drawLens(center, maxRadius, progress, threat, hairline)
                HeroVariant.TRAIL -> drawTrail(center, maxRadius, progress, threat, hairline)
                HeroVariant.ENCLOSURE -> drawEnclosure(center, maxRadius, progress, safe, accent, hairline)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// The shared field
// ─────────────────────────────────────────────────────────────────────

/** Three concentric rings that expand into place, establishing the space. */
private fun DrawScope.drawRadarField(
    center: Offset,
    maxRadius: Float,
    progress: Float,
    ring: Color,
    faintRing: Color
) {
    val ringCount = 3
    repeat(ringCount) { index ->
        // Each ring starts a little after the one inside it, so the field opens
        // outwards rather than appearing all at once.
        val delay = index * 0.12f
        val local = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
        if (local <= 0f) return@repeat

        val targetRadius = maxRadius * (0.38f + index * 0.31f)
        drawCircle(
            color = if (index == ringCount - 1) faintRing else ring,
            radius = targetRadius * easeOut(local),
            center = center,
            alpha = local * (1f - index * 0.18f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/** A soft rotating wedge, the one thing on screen that says "still looking". */
private fun DrawScope.drawSweep(
    center: Offset,
    maxRadius: Float,
    angle: Float,
    accent: Color
) {
    rotate(degrees = angle, pivot = center) {
        drawArc(
            brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                0f to Color.Transparent,
                0.06f to accent.copy(alpha = 0.16f),
                0.12f to Color.Transparent,
                1f to Color.Transparent,
                center = center
            ),
            startAngle = 0f,
            sweepAngle = 60f,
            useCenter = true,
            topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
            size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Per-slide contents
// ─────────────────────────────────────────────────────────────────────

/**
 * Slide 1: many devices, no verdict yet.
 *
 * Positions are fixed constants rather than random, so the picture is identical
 * every time the user sees it. A layout that reshuffles on each visit reads as
 * noise, and it makes the illustration impossible to art-direct.
 */
private fun DrawScope.drawDeviceField(
    center: Offset,
    maxRadius: Float,
    progress: Float,
    accent: Color,
    neutral: Color
) {
    val nodes = listOf(
        Triple(24f, 0.42f, 5f), Triple(96f, 0.72f, 4f), Triple(158f, 0.50f, 6f),
        Triple(212f, 0.80f, 4f), Triple(268f, 0.38f, 5f), Triple(310f, 0.66f, 4f),
        Triple(340f, 0.90f, 3f), Triple(62f, 0.92f, 3f)
    )

    nodes.forEachIndexed { index, (degrees, radiusFraction, dotRadius) ->
        val delay = 0.25f + index * 0.07f
        val local = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
        if (local <= 0f) return@forEachIndexed

        val point = polar(center, maxRadius * radiusFraction, degrees)
        // Every third node is accented — enough to feel surveyed, not so much
        // that the accent stops meaning anything.
        val color = if (index % 3 == 0) accent else neutral
        drawCircle(
            color = color,
            radius = dotRadius.dp.toPx() * easeOut(local),
            center = point,
            alpha = local
        )
    }

    // The phone at the centre: the fixed point everything is measured from.
    drawCircle(color = accent, radius = 6.dp.toPx() * easeOut(progress), center = center)
    drawCircle(
        color = accent,
        radius = 13.dp.toPx() * easeOut(progress),
        center = center,
        alpha = 0.30f * progress,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

/** Slide 2: an aperture — unmistakably a lens, and it is looking back. */
private fun DrawScope.drawLens(
    center: Offset,
    maxRadius: Float,
    progress: Float,
    threat: Color,
    neutral: Color
) {
    val outerRadius = maxRadius * 0.40f * easeOut(progress)
    val bladeCount = 6

    repeat(bladeCount) { index ->
        val delay = 0.20f + index * 0.06f
        val local = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
        if (local <= 0f) return@repeat

        val angle = index * (360f / bladeCount)
        val from = polar(center, outerRadius, angle)
        val to = polar(center, outerRadius, angle + 360f / bladeCount)
        drawLine(
            color = neutral,
            start = from,
            end = lerp(from, to, local),
            strokeWidth = 1.5.dp.toPx(),
            alpha = local
        )
    }

    drawCircle(
        color = neutral,
        radius = outerRadius,
        center = center,
        alpha = 0.55f * progress,
        style = Stroke(width = 1.dp.toPx())
    )

    // The pupil arrives last and in the threat colour — the whole point of the
    // slide lands on the final beat.
    val pupil = ((progress - 0.62f) / 0.38f).coerceIn(0f, 1f)
    if (pupil > 0f) {
        drawCircle(color = threat, radius = outerRadius * 0.34f * easeOut(pupil), center = center, alpha = pupil)
        drawCircle(
            color = threat,
            radius = outerRadius * 0.62f * easeOut(pupil),
            center = center,
            alpha = 0.22f * pupil,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Slide 3: the same tag, at four points along a path.
 *
 * The idea being drawn is *persistence over time* — the earlier positions fade
 * but do not vanish, so the trail itself is the evidence.
 */
private fun DrawScope.drawTrail(
    center: Offset,
    maxRadius: Float,
    progress: Float,
    threat: Color,
    neutral: Color
) {
    val steps = 4
    val userPath = listOf(
        polar(center, maxRadius * 0.62f, 200f),
        polar(center, maxRadius * 0.38f, 240f),
        polar(center, maxRadius * 0.30f, 320f),
        center
    )
    val tagPath = listOf(
        polar(center, maxRadius * 0.80f, 186f),
        polar(center, maxRadius * 0.56f, 232f),
        polar(center, maxRadius * 0.46f, 318f),
        polar(center, maxRadius * 0.24f, 20f)
    )

    repeat(steps) { index ->
        val delay = 0.18f + index * 0.18f
        val local = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
        if (local <= 0f) return@repeat

        val isLatest = index == steps - 1
        // Older positions are dimmer: the trail reads as history, not as four
        // separate devices.
        val historyAlpha = (0.28f + index * 0.24f) * local

        if (index > 0) {
            drawLine(
                color = neutral,
                start = userPath[index - 1],
                end = lerp(userPath[index - 1], userPath[index], local),
                strokeWidth = 1.dp.toPx(),
                alpha = historyAlpha * 0.7f
            )
        }

        drawCircle(color = neutral, radius = 4.dp.toPx(), center = userPath[index], alpha = historyAlpha)
        drawCircle(
            color = threat,
            radius = (if (isLatest) 6f else 4f).dp.toPx() * easeOut(local),
            center = tagPath[index],
            alpha = historyAlpha
        )

        if (isLatest) {
            drawCircle(
                color = threat,
                radius = 14.dp.toPx() * easeOut(local),
                center = tagPath[index],
                alpha = 0.28f * local,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

/**
 * Slide 4: a closed boundary with everything inside it.
 *
 * No sweep on this one. The field is at rest because the claim is that nothing
 * is going anywhere — the stillness carries the message.
 */
private fun DrawScope.drawEnclosure(
    center: Offset,
    maxRadius: Float,
    progress: Float,
    safe: Color,
    accent: Color,
    neutral: Color
) {
    val boundary = maxRadius * 0.66f

    // A dashed ring that closes as it draws: a container being sealed.
    val segments = 28
    repeat(segments) { index ->
        val delay = index * (0.55f / segments)
        val local = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
        if (local <= 0f) return@repeat

        val angle = index * (360f / segments)
        val inner = polar(center, boundary - 3.dp.toPx(), angle)
        val outer = polar(center, boundary + 3.dp.toPx(), angle)
        drawLine(
            color = safe,
            start = inner,
            end = outer,
            strokeWidth = 2.dp.toPx(),
            alpha = 0.55f * local
        )
    }

    // Contained data, held well inside the boundary.
    val inner = listOf(
        Triple(40f, 0.28f, 4f), Triple(150f, 0.34f, 5f),
        Triple(255f, 0.26f, 4f), Triple(320f, 0.36f, 3f)
    )
    inner.forEachIndexed { index, (degrees, fraction, dotRadius) ->
        val delay = 0.45f + index * 0.08f
        val local = ((progress - delay) / (1f - delay)).coerceIn(0f, 1f)
        if (local <= 0f) return@forEachIndexed
        drawCircle(
            color = if (index % 2 == 0) accent else neutral,
            radius = dotRadius.dp.toPx() * easeOut(local),
            center = polar(center, maxRadius * fraction, degrees),
            alpha = local
        )
    }

    drawCircle(color = accent, radius = 7.dp.toPx() * easeOut(progress), center = center)
}

// ─────────────────────────────────────────────────────────────────────
// Geometry
// ─────────────────────────────────────────────────────────────────────

private fun polar(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(
        x = center.x + radius * cos(radians).toFloat(),
        y = center.y + radius * sin(radians).toFloat()
    )
}

private fun lerp(from: Offset, to: Offset, fraction: Float) = Offset(
    x = from.x + (to.x - from.x) * fraction,
    y = from.y + (to.y - from.y) * fraction
)

/** Decelerating curve, so elements arrive rather than land. */
private fun easeOut(t: Float): Float {
    val clamped = t.coerceIn(0f, 1f)
    return 1f - (1f - clamped) * (1f - clamped) * (1f - clamped)
}
