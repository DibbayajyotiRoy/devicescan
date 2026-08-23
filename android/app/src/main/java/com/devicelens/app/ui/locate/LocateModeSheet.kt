package com.devicelens.app.ui.locate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devicelens.app.ui.components.rememberHaptics
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.LocalReducedMotion
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Locate Mode.
 *
 * This is the moment the app exists for: someone standing in a room they do not
 * trust, trying to find the thing that is watching them. It has one job — make
 * "warmer" and "colder" unmistakable while the user's eyes are on the room, not
 * on the phone.
 *
 * So the feedback is deliberately multi-sensory, and the haptic is the part
 * that matters most. A repeating pulse whose interval shortens as the signal
 * strengthens is legible in a pocket, in the dark, and without looking — which
 * is exactly the situation this screen is used in. That is the same trick a
 * metal detector uses, for the same reason.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocateModeSheet(
    onDismiss: () -> Unit,
    viewModel: LocateViewModel = hiltViewModel()
) {
    val colors = ExtendedTheme.colors
    val haptics = rememberHaptics()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val device by viewModel.device.collectAsStateWithLifecycle()
    val rssi by viewModel.rssi.collectAsStateWithLifecycle()
    val proximity by viewModel.proximity.collectAsStateWithLifecycle()
    val trend by viewModel.trend.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackText.collectAsStateWithLifecycle()
    val metres by viewModel.estimatedMetres.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val unavailable by viewModel.unavailableReason.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.stopTracking() }
    }

    // The proximity pulse. Interval collapses from about a second to a tenth as
    // the user closes in, so the rhythm alone communicates distance.
    LaunchedEffect(isTracking, proximity) {
        if (!isTracking) return@LaunchedEffect
        while (true) {
            val interval = (900 - (proximity * 780)).toLong().coerceAtLeast(120L)
            delay(interval)
            if (proximity > 0.15f) haptics.tick()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.stopTracking()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = colors.surfaceGlass,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = Radius.xlDp, topEnd = Radius.xlDp
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = Space.md)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(Radius.full)
                    .background(colors.hairlineBorderStrong)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.xl)
                .padding(bottom = Space.x4l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LOCATING",
                style = MaterialTheme.typography.labelSmall,
                color = colors.securityTeal
            )
            Spacer(Modifier.height(Space.sm))
            Text(
                text = device?.deviceName ?: "Device",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Space.x3l))

            if (unavailable != null) {
                UnavailableNotice(text = unavailable!!)
            } else {
                ProximityMeter(
                    proximity = proximity,
                    trend = trend,
                    rssi = rssi
                )

                Spacer(Modifier.height(Space.xxl))

                AnimatedContent(
                    targetState = feedback,
                    transitionSpec = { fadeIn(Motion.fade()) togetherWith fadeOut(Motion.fade(140)) },
                    label = "feedback"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 320.dp)
                    )
                }

                metres?.let { estimate ->
                    Spacer(Modifier.height(Space.lg))
                    Text(
                        // Labelled as approximate, because signal-based distance
                        // genuinely is — walls and bodies move it by metres.
                        text = "roughly ${formatDistance(estimate)} away",
                        style = MonoType.small,
                        color = colors.textQuaternary
                    )
                }
            }

            Spacer(Modifier.height(Space.x3l))

            Surface(
                color = colors.surfaceGlassHighlight,
                shape = Radius.lg,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .border(1.dp, colors.hairlineBorder, Radius.lg)
                    .pressable(
                        onClick = {
                            viewModel.stopTracking()
                            onDismiss()
                        },
                        pressScale = 0.985f
                    )
            ) {
                Text(
                    text = "Stop",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.lg)
                )
            }
        }
    }
}

/**
 * The meter.
 *
 * An arc that fills with proximity, plus rings that expand outward faster as
 * the signal strengthens — motion the eye reads as "closing in" without any
 * label. The number is present for people who want it, but the shape is what
 * carries the message.
 */
@Composable
private fun ProximityMeter(
    proximity: Float,
    trend: LocateViewModel.Trend,
    rssi: Int?
) {
    val colors = ExtendedTheme.colors
    val reducedMotion = LocalReducedMotion.current

    val accent = when (trend) {
        LocateViewModel.Trend.VERY_CLOSE -> colors.statusRisk
        LocateViewModel.Trend.WARMER -> colors.statusWarning
        LocateViewModel.Trend.COLDER -> colors.textTertiary
        else -> colors.securityTeal
    }

    // Both the fill and the colour ease, so a swing in signal reads as the meter
    // moving rather than as the screen flickering between states.
    val animatedFill by animateFloatAsState(
        targetValue = proximity,
        animationSpec = Motion.smooth(),
        label = "proximityFill"
    )
    val animatedColor by animateColorAsState(
        targetValue = accent,
        animationSpec = Motion.gentle(),
        label = "proximityColor"
    )

    // Ring expansion speed is the second channel: quicker pulses mean closer.
    val ringPhase = if (reducedMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "rings")
        val period = (2200 - (proximity * 1700)).toInt().coerceAtLeast(420)
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(period, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ringPhase"
        )
        phase
    }

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Track
            drawArc(
                color = colors.hairlineBorder,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Fill
            drawArc(
                color = animatedColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedFill,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Expanding rings, fading as they travel outward.
            if (!reducedMotion) {
                repeat(2) { index ->
                    val phase = (ringPhase + index * 0.5f) % 1f
                    val radius = size.minDimension * (0.16f + phase * 0.28f)
                    drawCircle(
                        color = animatedColor,
                        radius = radius,
                        center = center,
                        alpha = (1f - phase) * 0.30f * (0.35f + proximity),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedFill * 100).roundToInt()}",
                style = MaterialTheme.typography.displayMedium,
                color = colors.textPrimary
            )
            Text(
                text = when (trend) {
                    LocateViewModel.Trend.VERY_CLOSE -> "VERY CLOSE"
                    LocateViewModel.Trend.WARMER -> "WARMER"
                    LocateViewModel.Trend.COLDER -> "COLDER"
                    LocateViewModel.Trend.STEADY -> "STEADY"
                    LocateViewModel.Trend.SEARCHING -> "SEARCHING"
                },
                style = MaterialTheme.typography.labelSmall,
                color = animatedColor
            )
            rssi?.let {
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "$it dBm",
                    style = MonoType.small,
                    color = colors.textQuaternary
                )
            }
        }
    }
}

@Composable
private fun UnavailableNotice(text: String) {
    val colors = ExtendedTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp)
            .clip(Radius.md)
            .background(colors.surfaceGlassHighlight)
            .padding(Space.xl)
    )
}

private fun formatDistance(metres: Float): String = when {
    metres < 1f -> "under a metre"
    metres < 10f -> "${metres.roundToInt()} m"
    else -> "${(metres / 5).roundToInt() * 5} m"
}
