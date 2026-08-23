package com.devicelens.app.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import com.devicelens.app.ui.theme.LocalReducedMotion
import com.devicelens.app.ui.theme.Motion

/**
 * Touch feedback, in one place.
 *
 * Two rules from Apple's fluid-interface work drive this file:
 *
 *  1. **Feedback belongs on press-down, not on release.** The instant a finger
 *     lands, something must change. Waiting for the tap to complete before
 *     reacting is the difference between an interface that feels alive and one
 *     that feels like it is thinking about it.
 *  2. **Haptics must be earned.** A buzz on every touch trains people to stop
 *     noticing all of them, which costs you the ones that matter. They are
 *     reserved here for commitment, selection, and outcomes.
 */

@Immutable
class Haptics(private val view: View) {

    /** A light tick for a press landing on a control. */
    fun tap() = perform(HapticFeedbackConstants.VIRTUAL_KEY)

    /** Moving through discrete steps — pages, list positions, segments. */
    fun tick() = perform(
        if (Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.SEGMENT_TICK
        else HapticFeedbackConstants.CLOCK_TICK
    )

    /** A choice was made: an item selected, a switch turned on. */
    fun select(on: Boolean = true) = perform(
        when {
            Build.VERSION.SDK_INT >= 34 && on -> HapticFeedbackConstants.TOGGLE_ON
            Build.VERSION.SDK_INT >= 34 -> HapticFeedbackConstants.TOGGLE_OFF
            else -> HapticFeedbackConstants.CLOCK_TICK
        }
    )

    /** Something completed successfully. */
    fun success() = perform(
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.CONTEXT_CLICK
    )

    /** Something was refused or failed. */
    fun reject() = perform(
        if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.LONG_PRESS
    )

    /** A meaningful, weighty commitment — starting a scan, trusting a device. */
    fun commit() = perform(HapticFeedbackConstants.LONG_PRESS)

    private fun perform(constant: Int) {
        try {
            // FLAG_IGNORE_VIEW_SETTING is deliberately not used: if the user has
            // turned haptics off for this view hierarchy, that is their call.
            view.performHapticFeedback(constant)
        } catch (e: Exception) {
            // A missing haptic is never worth an exception reaching the user.
        }
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/**
 * A tappable surface that reacts the moment it is touched.
 *
 * Replaces `Modifier.clickable` wherever the target is a card, tile or custom
 * button. The scale change is small on purpose — at 0.97 it reads as the
 * surface yielding under a finger; any deeper and it reads as a bug.
 *
 * @param pressScale how far the surface compresses. Larger targets need less.
 * @param hapticOnPress fire a tick as the finger lands. Leave off for rows in a
 *        long list, where every scroll-start would otherwise buzz.
 */
fun Modifier.pressable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    pressScale: Float = 0.97f,
    hapticOnPress: Boolean = true,
    role: Role? = Role.Button,
    onLongClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    val haptics = rememberHaptics()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !reducedMotion) pressScale else 1f,
        // A spring rather than a tween, so releasing mid-press returns from
        // wherever the scale actually is instead of snapping to the start.
        animationSpec = Motion.instant(),
        label = "pressScale"
    )

    // The press ripple is suppressed in favour of the scale: two simultaneous
    // feedback mechanisms on one control read as noise.
    val clickModifier = if (onLongClick != null) {
        Modifier.combinedPressable(
            interactionSource = interactionSource,
            enabled = enabled,
            role = role,
            onClick = onClick,
            onLongClick = onLongClick
        )
    } else {
        Modifier.androidxClickable(
            interactionSource = interactionSource,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
    }

    if (hapticOnPress) {
        LaunchedEffect(isPressed) {
            if (isPressed && enabled) haptics.tap()
        }
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(clickModifier)
}

private fun Modifier.androidxClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    role: Role?,
    onClick: () -> Unit
): Modifier = this.clickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = role,
    onClick = onClick
)

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedPressable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    role: Role?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = this.combinedClickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = role,
    onClick = onClick,
    onLongClick = onLongClick
)
