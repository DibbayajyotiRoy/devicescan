package com.devicelens.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.EaseSpring
import com.devicelens.app.ui.theme.EaseOutExpo
import com.devicelens.app.ui.theme.ShapePill
import kotlinx.coroutines.launch

// ═════════════════════════════════════════════════════════════════════════════
// FLOATING GLASS NAVIGATION — FLUID ISLAND ARCHITECTURE
// Morphing hamburger, staggered reveal, heavy glass morphism
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun FloatingGlassNav(
    title: String = "Device Lens",
    onSettingsClick: () -> Unit = {},
    onDebugClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Animation specs
    val expandAnim by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(400, easing = EaseOutExpo),
        label = "expand"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Collapsed state — Floating glass pill
        if (expandAnim < 0.5f) {
            CollapsedNavPill(
                title = title,
                isExpanded = isExpanded,
                onExpandToggle = { isExpanded = !isExpanded },
                modifier = Modifier.alpha(1f - expandAnim * 2)
            )
        }

        // Expanded state — Full-screen overlay
        if (expandAnim > 0f) {
            ExpandedNavOverlay(
                title = title,
                expandProgress = expandAnim,
                onSettingsClick = {
                    scope.launch {
                        isExpanded = false
                        kotlinx.coroutines.delay(200)
                        onSettingsClick()
                    }
                },
                onDebugClick = {
                    scope.launch {
                        isExpanded = false
                        kotlinx.coroutines.delay(200)
                        onDebugClick()
                    }
                },
                onAboutClick = {
                    scope.launch {
                        isExpanded = false
                        kotlinx.coroutines.delay(200)
                        onAboutClick()
                    }
                },
                onCollapse = { isExpanded = false }
            )
        }
    }
}

@Composable
private fun CollapsedNavPill(
    title: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "pressScale"
    )

    // Glass morphism pill container
    Box(
        modifier = modifier
            .padding(top = 16.dp)
            .wrapContentSize()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(ShapePill)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ExtendedTheme.colors.surfaceGlass.copy(alpha = 0.95f),
                        ExtendedTheme.colors.surfaceGlassHighlight.copy(alpha = 0.9f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onExpandToggle() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer hairline border
        Box(
            modifier = Modifier
                .padding(1.dp)
                .clip(ShapePill)
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hamburger morph icon
                MorphingHamburger(
                    isOpen = isExpanded,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onExpandToggle
                        ),
                    onClick = onExpandToggle
                )

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Spacer for symmetry
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun MorphingHamburger(
    isOpen: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = isOpen, label = "hamburger")

    // Line 1 (top) — rotates to top of X
    val line1Rotation by transition.animateFloat(
        label = "line1Rotation",
        transitionSpec = { tween(300, easing = EaseSpring) }
    ) { open ->
        if (open) 45f else 0f
    }
    val line1Translation by transition.animateFloat(
        label = "line1Translation",
        transitionSpec = { tween(300, easing = EaseSpring) }
    ) { open ->
        if (open) 6f else 0f
    }

    // Line 2 (middle) — fades out
    val line2Alpha by transition.animateFloat(
        label = "line2Alpha",
        transitionSpec = { tween(200) }
    ) { open ->
        if (open) 0f else 1f
    }

    // Line 3 (bottom) — rotates to bottom of X
    val line3Rotation by transition.animateFloat(
        label = "line3Rotation",
        transitionSpec = { tween(300, easing = EaseSpring) }
    ) { open ->
        if (open) -45f else 0f
    }
    val line3Translation by transition.animateFloat(
        label = "line3Translation",
        transitionSpec = { tween(300, easing = EaseSpring) }
    ) { open ->
        if (open) -6f else 0f
    }

    Box(
        modifier = modifier
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Three lines that morph
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top line
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .graphicsLayer {
                        rotationZ = line1Rotation
                        translationY = line1Translation
                    }
                    .clip(ShapePill)
                    .background(MaterialTheme.colorScheme.onSurface)
            )

            // Middle line
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .alpha(line2Alpha)
                    .clip(ShapePill)
                    .background(MaterialTheme.colorScheme.onSurface)
            )

            // Bottom line
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .graphicsLayer {
                        rotationZ = line3Rotation
                        translationY = line3Translation
                    }
                    .clip(ShapePill)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
private fun ExpandedNavOverlay(
    title: String,
    expandProgress: Float,
    onSettingsClick: () -> Unit,
    onDebugClick: () -> Unit,
    onAboutClick: () -> Unit,
    onCollapse: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = expandProgress
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Close when tapping outside content
                    onCollapse()
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Heavy glass backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(
                        alpha = 0.85f * expandProgress
                    )
                )
        )

        // Content container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .graphicsLayer {
                    translationY = (1f - expandProgress) * -50f
                }
        ) {
            // Menu items with staggered animation
            NavMenuItem(
                label = "Settings",
                description = "Configure app preferences",
                delayMs = 0,
                expandProgress = expandProgress,
                onClick = onSettingsClick
            )

            NavMenuItem(
                label = "Debug Logs",
                description = "View diagnostic information",
                delayMs = 80,
                expandProgress = expandProgress,
                onClick = onDebugClick
            )

            NavMenuItem(
                label = "About",
                description = "Learn about Device Lens",
                delayMs = 160,
                expandProgress = expandProgress,
                onClick = onAboutClick
            )
        }
    }
}

@Composable
private fun NavMenuItem(
    label: String,
    description: String,
    delayMs: Int,
    expandProgress: Float,
    onClick: () -> Unit
) {
    // Staggered entry based on expand progress
    val threshold = delayMs / 400f
    val itemProgress = ((expandProgress - threshold) / (1f - threshold)).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .graphicsLayer {
                alpha = itemProgress
                translationY = (1f - itemProgress) * 30f
            }
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ExtendedTheme.colors.surfaceGlass.copy(alpha = 0.6f),
                        ExtendedTheme.colors.surfaceGlassHighlight.copy(alpha = 0.4f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ExtendedTheme.colors.textTertiary
            )
        }
    }
}
