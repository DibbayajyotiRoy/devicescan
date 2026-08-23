package com.devicelens.app.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * The app's motion system.
 *
 * Two ideas drive everything here.
 *
 * **Springs, not durations.** A fixed-duration tween cannot respond to new
 * input: interrupt it and it either finishes stubbornly or jumps. A spring
 * always animates from wherever the value currently *is*, and re-targeting it
 * mid-flight is seamless. Anything a finger can touch gets a spring.
 *
 * **Two parameters, not three.** Apple's interface teams describe springs as
 * *damping* (how much it overshoots) and *response* (how quickly it arrives),
 * rather than mass/stiffness/damping. Compose wants stiffness, so the response
 * times below are converted once, here, and never thought about again:
 *
 *     ω = 2π / response          stiffness = ω²
 *
 * The house rule is **critically damped (1.0) by default**. Overshoot is only
 * correct when the gesture itself carried momentum — a flick, a drag release, a
 * thing being thrown. A menu that merely appeared has no momentum to express,
 * and bouncing it looks like decoration rather than physics.
 */
object Motion {

    // ── Response times, in seconds ───────────────────────────────────
    private const val RESPONSE_INSTANT = 0.2f   // press states, toggles
    private const val RESPONSE_QUICK = 0.3f     // sheets, drawers, most UI
    private const val RESPONSE_STANDARD = 0.4f  // repositioning, layout shifts
    private const val RESPONSE_GENTLE = 0.55f   // large surfaces, page-level

    private fun stiffnessFor(responseSeconds: Float): Float {
        val omega = (2.0 * Math.PI / responseSeconds).toFloat()
        return omega * omega
    }

    /** Immediate, no overshoot. For press feedback that must feel like contact. */
    fun <T> instant(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_INSTANT)
    )

    /** The default. Critically damped — arrives and stops, with no wobble. */
    fun <T> standard(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_QUICK)
    )

    /** For repositioning something the user is watching move. */
    fun <T> smooth(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_STANDARD)
    )

    /** Large surfaces: slower, because big things read as heavier. */
    fun <T> gentle(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_GENTLE)
    )

    /**
     * Slight overshoot. Use *only* after a gesture with momentum, or when
     * something lands after being thrown. Never for a passive appearance.
     */
    fun <T> springy(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = stiffnessFor(RESPONSE_QUICK)
    )

    /** Pronounced bounce. Reserved for a single celebratory moment per flow. */
    fun <T> playful(): SpringSpec<T> = spring(
        dampingRatio = 0.55f,
        stiffness = stiffnessFor(RESPONSE_STANDARD)
    )

    // Dp and offset animations need a visibility threshold, or they settle on a
    // sub-pixel value and keep the frame loop awake.
    fun dp(): SpringSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_QUICK),
        visibilityThreshold = Dp.VisibilityThreshold
    )

    fun intOffset(): SpringSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_QUICK),
        visibilityThreshold = IntOffset.VisibilityThreshold
    )

    fun intSize(): SpringSpec<IntSize> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = stiffnessFor(RESPONSE_QUICK),
        visibilityThreshold = IntSize.VisibilityThreshold
    )

    /**
     * A plain fade. Opacity is the one property that stays acceptable when the
     * user has asked for reduced motion, so this is the fallback everything
     * degrades to.
     */
    fun <T> fade(durationMs: Int = 180): FiniteAnimationSpec<T> =
        tween(durationMs, easing = EaseOutCubic)
}

private typealias SpringSpec<T> = androidx.compose.animation.core.SpringSpec<T>

/**
 * True when the user has turned system animations off.
 *
 * Android exposes this as the developer-options animator scale, which
 * accessibility settings and battery savers also drive. Honouring it is not
 * optional: for people with vestibular disorders, motion that ignores this
 * setting causes real nausea.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

fun isSystemReducedMotion(context: Context): Boolean = try {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
} catch (e: Exception) {
    false
}

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { isSystemReducedMotion(context) }
}

/**
 * Wraps a spec so it collapses to a cross-fade — or to nothing at all for
 * position and size — when reduced motion is on.
 *
 * Reduced motion does not mean *no feedback*. It means no vestibular motion:
 * things may still change colour and fade, they must not fly, bounce or slide.
 */
@Composable
@ReadOnlyComposable
fun <T> AnimationSpec<T>.respectMotionPreference(
    reduced: Boolean = LocalReducedMotion.current,
    fallback: AnimationSpec<T> = snap()
): AnimationSpec<T> = if (reduced) fallback else this

@Composable
@ReadOnlyComposable
fun <T> motionSpec(
    spec: AnimationSpec<T>,
    reducedFallback: AnimationSpec<T> = snap()
): AnimationSpec<T> = if (LocalReducedMotion.current) reducedFallback else spec

// ── Spatial constants ────────────────────────────────────────────────
// Shared so screens that hand off to each other agree on how far things move.

/** How far a page-level element travels when entering. */
const val ENTER_TRANSLATION_DP = 24f

/** How far a list row travels when it appears in a stagger. */
const val STAGGER_TRANSLATION_DP = 12f

/**
 * Delay between consecutive items in a staggered reveal.
 *
 * Kept short deliberately: a stagger is meant to suggest order, not to make the
 * user wait. Anything past ~40 ms per item and a ten-row list feels sluggish.
 */
const val STAGGER_STEP_MS = 28
