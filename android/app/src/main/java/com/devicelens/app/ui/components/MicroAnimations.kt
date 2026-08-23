package com.devicelens.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devicelens.app.ui.theme.LocalReducedMotion
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.STAGGER_STEP_MS
import com.devicelens.app.ui.theme.STAGGER_TRANSLATION_DP
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Small, reusable motion primitives.
 *
 * These are what make an interface feel considered rather than assembled: a row
 * that arrives instead of appearing, a number that counts rather than jumping, a
 * value that eases to its new position. Individually none of them are noticed.
 * Collectively they are the entire difference.
 *
 * **The performance contract every helper in this file keeps**, because dozens
 * of small animations can be free or can be ruinous depending only on how they
 * are written:
 *
 *  - Animate **only** `graphicsLayer` properties — alpha, scale, translation,
 *    rotation. These are handled by the render thread; nothing re-lays-out.
 *  - Always use the **lambda form** of `graphicsLayer { }`. It reads animated
 *    state during the draw phase, so a running animation skips recomposition
 *    and layout entirely. The non-lambda form recomposes on every frame.
 *  - **Never** animate size, padding or layout position in a list. One such
 *    animation costs more than twenty `graphicsLayer` ones.
 *  - Scroll-derived values go through `derivedStateOf`, so a value that has not
 *    actually changed does not invalidate anything.
 *  - Infinite animations exist only while something is genuinely live, and stop
 *    when it is not. A permanently-running loop keeps the display awake and
 *    shows up directly in battery use.
 *  - Everything degrades when the user asks for reduced motion.
 */

// ─────────────────────────────────────────────────────────────────────
// Entrance
// ─────────────────────────────────────────────────────────────────────

/**
 * Fades and lifts content into place, offset by its position in a list.
 *
 * The stagger is what makes a list read as *arriving* rather than *blinking on*.
 * Kept to a few tens of milliseconds per row so it suggests order without ever
 * making someone wait for their data.
 *
 * @param index position in the list; drives the delay.
 * @param maxStaggered rows past this appear immediately — a long list should
 *        not have its hundredth row waiting three seconds to show up.
 */
fun Modifier.enterStaggered(
    index: Int = 0,
    maxStaggered: Int = 12,
    translation: Dp = STAGGER_TRANSLATION_DP.dp
): Modifier = composedEnter(index.coerceAtMost(maxStaggered) * STAGGER_STEP_MS, translation)

/** Fades and lifts content into place after a fixed delay. */
fun Modifier.enterAfter(delayMs: Int = 0, translation: Dp = STAGGER_TRANSLATION_DP.dp): Modifier =
    composedEnter(delayMs, translation)

private fun Modifier.composedEnter(delayMs: Int, translation: Dp): Modifier =
    composed {
        val reduced = LocalReducedMotion.current
        var visible by remember { mutableStateOf(false) }

        val progress by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (reduced) 120 else 320,
                delayMillis = if (reduced) 0 else delayMs,
                easing = com.devicelens.app.ui.theme.EaseOutCubic
            ),
            label = "enter"
        )

        LaunchedEffect(Unit) { visible = true }

        val translationPx = with(LocalDensity.current) { translation.toPx() }

        // Lambda form: this block runs at draw time, so the 320 ms of animation
        // costs no recompositions and no layout passes.
        graphicsLayer {
            alpha = progress
            if (!reduced) translationY = (1f - progress) * translationPx
        }
    }

// ─────────────────────────────────────────────────────────────────────
// Numbers
// ─────────────────────────────────────────────────────────────────────

/**
 * Eases an integer towards a new value so counters count instead of jumping.
 *
 * A device count that slides 3 → 7 tells the user something arrived. The same
 * count snapping to 7 tells them nothing, and they may not even notice it moved.
 */
@Composable
fun animatedCount(target: Int, spec: androidx.compose.animation.core.AnimationSpec<Float>? = null): Int {
    val reduced = LocalReducedMotion.current
    if (reduced) return target

    val animated by animateFloatAsState(
        targetValue = target.toFloat(),
        animationSpec = spec ?: Motion.smooth(),
        label = "count"
    )
    return animated.roundToInt()
}

// ─────────────────────────────────────────────────────────────────────
// Live activity
// ─────────────────────────────────────────────────────────────────────

/**
 * A slow breathing pulse, for something that is genuinely working right now.
 *
 * [active] is not optional politeness — when it goes false the infinite
 * transition is removed from composition and stops driving frames. A pulse left
 * running behind an idle screen is a measurable battery cost for zero
 * information.
 */
@Composable
fun rememberPulse(
    active: Boolean,
    min: Float = 0.6f,
    max: Float = 1f,
    periodMs: Int = 1600
): State<Float> {
    val reduced = LocalReducedMotion.current
    if (!active || reduced) {
        return remember(max) { mutableFloatStateOf(max) }
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    return transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = com.devicelens.app.ui.theme.EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseValue"
    )
}

/**
 * A shimmer sweep for skeleton placeholders.
 *
 * Drawn with `drawWithCache`, so the gradient is built once and only the
 * translation changes per frame. Only ever shown while data is actually
 * loading.
 */
fun Modifier.shimmer(active: Boolean = true, cornerRadius: Dp = 12.dp): Modifier =
    composed {
        if (!active || LocalReducedMotion.current) return@composed this

        val transition = rememberInfiniteTransition(label = "shimmer")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerSweep"
        )

        val highlight = Color.White.copy(alpha = 0.06f)
        this
            .clip(RoundedCornerShape(cornerRadius))
            .drawWithCache {
                val width = size.width
                val sweep = width * 1.6f
                val brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = androidx.compose.ui.geometry.Offset(-sweep + progress * (width + sweep), 0f),
                    end = androidx.compose.ui.geometry.Offset(progress * (width + sweep), size.height)
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(brush)
                }
            }
    }

/** A skeleton block, for content that has not arrived yet. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = 0.05f))
            .shimmer(cornerRadius = cornerRadius)
    )
}

// ─────────────────────────────────────────────────────────────────────
// Scroll-driven
// ─────────────────────────────────────────────────────────────────────

/**
 * How far the list has scrolled, as 0..1 over [overDp].
 *
 * Routed through `derivedStateOf` so anything reading it only wakes up when the
 * value genuinely changes, not on every pixel of scroll.
 */
@Composable
fun rememberScrollProgress(state: LazyListState, overDp: Dp = 72.dp): State<Float> {
    val overPx = with(LocalDensity.current) { overDp.toPx() }
    return remember(state, overPx) {
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) 1f
            else (state.firstVisibleItemScrollOffset / overPx).coerceIn(0f, 1f)
        }
    }
}

/**
 * A soft gradient where scrolling content passes under floating chrome.
 *
 * A hard 1 px divider announces "there is a bar here". A fade says the content
 * continues underneath, which is what is actually true, and it only appears
 * once there is something to fade.
 */
@Composable
fun BoxScope.ScrollEdgeFade(
    progress: Float,
    height: Dp = 40.dp,
    color: Color = Color.Black
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { alpha = progress }
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.75f), Color.Transparent)
                )
            )
    )
}

// ─────────────────────────────────────────────────────────────────────
// Pager
// ─────────────────────────────────────────────────────────────────────

/**
 * Parallax and depth for a horizontally paged item.
 *
 * The content drifts slower than the page it sits on and shrinks slightly as it
 * leaves, which reads as depth rather than as a flat slide. Distances are in dp
 * and converted here, so the effect is identical on every screen density —
 * hardcoded pixel offsets look right on one device and wrong on the rest.
 */
fun Modifier.pagerParallax(
    pageOffset: Float,
    driftDp: Dp = 40.dp,
    scaleFalloff: Float = 0.12f,
    fadeFalloff: Float = 1.15f
): Modifier = composed {
    val reduced = LocalReducedMotion.current
    val driftPx = with(LocalDensity.current) { driftDp.toPx() }

    graphicsLayer {
        val distance = pageOffset.absoluteValue.coerceAtMost(1.5f)
        alpha = (1f - distance * fadeFalloff).coerceIn(0f, 1f)
        if (!reduced) {
            translationX = pageOffset * driftPx
            val s = 1f - distance * scaleFalloff
            scaleX = s
            scaleY = s
        }
    }
}
