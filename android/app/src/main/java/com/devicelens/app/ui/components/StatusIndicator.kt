package com.devicelens.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.EaseOutCubic

// ═════════════════════════════════════════════════════════════════════════════
// CINEMATIC STATUS INDICATOR — ORBITAL GLOW ARCHITECTURE
// Concentric animated rings with radial gradients and pulse physics
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun StatusIndicator(
    status: OverallStatus,
    modifier: Modifier = Modifier
) {
    // Theme-aware status colors (works in both light and dark modes)
    val animatedColor by animateColorAsState(
        targetValue = when (status) {
            OverallStatus.SAFE -> ExtendedTheme.colors.statusSafe
            OverallStatus.WARNING -> ExtendedTheme.colors.statusWarning
            OverallStatus.RISK -> ExtendedTheme.colors.statusRisk
            OverallStatus.SCANNING -> ExtendedTheme.colors.securityTeal
            OverallStatus.NOT_CALIBRATED -> ExtendedTheme.colors.textTertiary
        },
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "statusColor"
    )

    val glowColor = when (status) {
        OverallStatus.SAFE -> ExtendedTheme.colors.statusSafeGlow
        OverallStatus.WARNING -> ExtendedTheme.colors.statusWarningGlow
        OverallStatus.RISK -> ExtendedTheme.colors.statusRiskGlow
        OverallStatus.SCANNING -> ExtendedTheme.colors.securityTealGlow
        OverallStatus.NOT_CALIBRATED -> ExtendedTheme.colors.textQuaternary.copy(alpha = 0.3f)
    }

    val label = when (status) {
        OverallStatus.SAFE -> "Environment Secure"
        OverallStatus.WARNING -> "Unknown Devices Nearby"
        OverallStatus.RISK -> "Suspicious Device Detected"
        OverallStatus.SCANNING -> "Scanning Environment…"
        OverallStatus.NOT_CALIBRATED -> "Ready to Scan"
    }

    val subLabel = when (status) {
        OverallStatus.SAFE -> "No threats detected"
        OverallStatus.WARNING -> "Review unknown devices"
        OverallStatus.RISK -> "Immediate attention required"
        OverallStatus.SCANNING -> "Analyzing network traffic"
        OverallStatus.NOT_CALIBRATED -> "Tap scan to begin"
    }

    // Pulse animations
    val pulseTransition = rememberInfiniteTransition(label = "statusPulse")

    val outerRingScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == OverallStatus.SCANNING) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outerRing"
    )

    val middleRingScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == OverallStatus.SCANNING) 1.1f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "middleRing"
    )

    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (status == OverallStatus.SCANNING) 0.6f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val meshTealColor = ExtendedTheme.colors.meshTeal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 32.dp)
    ) {
        // Cinematic orbital container
        Box(
            modifier = Modifier
                .size(200.dp)
                .drawBehind {
                    drawOrbitalGlow(
                        glowColor = glowColor,
                        glowAlpha = glowAlpha,
                        status = status,
                        meshTeal = meshTealColor
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Outermost ring — faintest
            OrbitalRing(
                size = 180.dp,
                scale = outerRingScale,
                color = animatedColor.copy(alpha = 0.08f)
            )

            // Second ring
            OrbitalRing(
                size = 140.dp,
                scale = middleRingScale * 0.95f,
                color = animatedColor.copy(alpha = 0.15f)
            )

            // Third ring — medium intensity
            OrbitalRing(
                size = 100.dp,
                scale = 0.98f,
                color = animatedColor.copy(alpha = 0.25f)
            )

            // Core status orb
            CoreStatusOrb(
                color = animatedColor,
                isPulsing = status == OverallStatus.SCANNING || status == OverallStatus.RISK
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status label
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = animatedColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-label
        Text(
            text = subLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = ExtendedTheme.colors.textTertiary
        )
    }
}

@Composable
private fun OrbitalRing(
    size: androidx.compose.ui.unit.Dp,
    scale: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}

@Composable
private fun CoreStatusOrb(
    color: Color,
    isPulsing: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    // Double-bezel core orb
    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(if (isPulsing) scale else 1f)
            .background(
                color = color.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = color.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )

        // Inner solid core
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
    }
}

private fun DrawScope.drawOrbitalGlow(
    glowColor: Color,
    glowAlpha: Float,
    status: OverallStatus,
    meshTeal: Color = Color.Unspecified
) {
    val center = Offset(size.width / 2, size.height / 2)

    // Radial glow gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = glowAlpha),
                glowColor.copy(alpha = glowAlpha * 0.5f),
                Color.Transparent
            ),
            center = center,
            radius = size.width * 0.4f
        ),
        center = center,
        radius = size.width * 0.4f
    )

    // Subtle mesh gradient dots for ethereal effect
    if (status == OverallStatus.SCANNING) {
        val meshColor = meshTeal
        repeat(8) { i ->
            val angle = (i * 45f) * (Math.PI / 180f)
            val radius = size.width * 0.35f
            val x = center.x + (kotlin.math.cos(angle) * radius).toFloat()
            val y = center.y + (kotlin.math.sin(angle) * radius).toFloat()

            drawCircle(
                color = meshColor,
                center = Offset(x, y),
                radius = 3f
            )
        }
    }
}
