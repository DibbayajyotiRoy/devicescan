package com.devicelens.app.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive layout, driven by how much room there actually is.
 *
 * Not "phone versus tablet" — a folded foldable, a half-screen split view and a
 * small phone all want the same layout, and the device category tells you
 * nothing useful about which you are in. Everything here keys off available
 * width, which is the thing that actually constrains the design.
 *
 * The rule the whole app follows: **content has a maximum readable width.** On a
 * tablet a list stretched to 1200 dp is unreadable — the eye loses the line and
 * a row's leading icon ends up an inch from its trailing chevron. Wide screens
 * get more *structure* (a second pane, more columns), never wider rows.
 */
@Immutable
data class WindowMetrics(
    val widthDp: Int,
    val heightDp: Int
) {
    val width: WidthClass = when {
        widthDp < 600 -> WidthClass.COMPACT
        widthDp < 840 -> WidthClass.MEDIUM
        else -> WidthClass.EXPANDED
    }

    /** Short height means landscape phone: vertical space is the scarce resource. */
    val isShort: Boolean = heightDp < 480

    enum class WidthClass { COMPACT, MEDIUM, EXPANDED }

    val isCompact: Boolean get() = width == WidthClass.COMPACT

    /**
     * Side padding for page content. Grows with the window so content never
     * runs into the bezel, but stops growing once the max width takes over.
     */
    val horizontalPadding: Dp
        get() = when (width) {
            WidthClass.COMPACT -> if (widthDp < 360) Space.lg else Space.xl
            WidthClass.MEDIUM -> Space.x3l
            WidthClass.EXPANDED -> Space.x4l
        }

    /** Vertical rhythm compresses on short screens rather than scrolling. */
    val sectionSpacing: Dp
        get() = when {
            isShort -> Space.lg
            isCompact -> Space.xxl
            else -> Space.x3l
        }

    /**
     * The widest a single column of content may become.
     *
     * Roughly 66 characters of body text at our body size, which is the span
     * the eye tracks comfortably without losing its place on the return sweep.
     */
    val maxContentWidth: Dp
        get() = when (width) {
            WidthClass.COMPACT -> Dp.Unspecified
            WidthClass.MEDIUM -> 620.dp
            WidthClass.EXPANDED -> 700.dp
        }

    /** Device lists become multi-column before rows would get uncomfortably wide. */
    val listColumns: Int
        get() = when (width) {
            WidthClass.COMPACT -> 1
            WidthClass.MEDIUM -> 1
            WidthClass.EXPANDED -> 2
        }

    /** On a wide window, a selected device can sit beside the list. */
    val supportsTwoPane: Boolean get() = width == WidthClass.EXPANDED

    /**
     * Onboarding puts art above text on a tall screen and beside it on a wide or
     * short one — the same content, laid out for the space that exists.
     */
    val onboardingIsHorizontal: Boolean
        get() = isShort || width != WidthClass.COMPACT

    /** Illustration size scales with the room available rather than being fixed. */
    val heroSize: Dp
        get() = when {
            isShort -> 132.dp
            width == WidthClass.COMPACT -> if (heightDp < 700) 168.dp else 200.dp
            else -> 232.dp
        }

    fun contentPadding(
        top: Dp = Dp.Unspecified,
        bottom: Dp = Dp.Unspecified
    ) = PaddingValues(
        start = horizontalPadding,
        end = horizontalPadding,
        top = if (top == Dp.Unspecified) Space.lg else top,
        bottom = if (bottom == Dp.Unspecified) Space.x3l else bottom
    )

    companion object {
        val Default = WindowMetrics(widthDp = 411, heightDp = 891)
    }
}

val LocalWindowMetrics = staticCompositionLocalOf { WindowMetrics.Default }

/**
 * Constrains content to a readable measure and centres it in whatever space is
 * left. On a compact window this is a no-op, so it is safe to apply everywhere.
 */
@Composable
fun Modifier.readableWidth(metrics: WindowMetrics = LocalWindowMetrics.current): Modifier =
    if (metrics.maxContentWidth == Dp.Unspecified) {
        this.fillMaxWidth()
    } else {
        this
            .fillMaxWidth()
            .widthIn(max = metrics.maxContentWidth)
    }

/** Centres a readable column inside a wide parent. */
val CenteredColumn: Arrangement.Horizontal = Arrangement.Center
val CenteredAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
