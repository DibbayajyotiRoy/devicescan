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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.theme.RiskRed
import com.devicelens.app.ui.theme.SafeGreen
import com.devicelens.app.ui.theme.WarningAmber

@Composable
fun StatusIndicator(
    status: OverallStatus,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when (status) {
            OverallStatus.SAFE -> SafeGreen
            OverallStatus.WARNING -> WarningAmber
            OverallStatus.RISK -> RiskRed
            OverallStatus.SCANNING -> MaterialTheme.colorScheme.onSurfaceVariant
            OverallStatus.NOT_CALIBRATED -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(500),
        label = "statusColor"
    )

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (status == OverallStatus.SCANNING) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.15f,
        targetValue = if (status == OverallStatus.SCANNING) 0.35f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val label = when (status) {
        OverallStatus.SAFE -> "Environment secure"
        OverallStatus.WARNING -> "Unknown devices nearby"
        OverallStatus.RISK -> "Suspicious device detected"
        OverallStatus.SCANNING -> "Scanning environment…"
        OverallStatus.NOT_CALIBRATED -> "Ready to scan"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .drawBehind {
                    // Radial glow behind the circles
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width * 0.8f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Outermost ring — faint
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale * 0.85f)
                    .background(
                        color = color.copy(alpha = 0.06f),
                        shape = CircleShape
                    )
            )
            // Middle ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale * 0.92f)
                    .background(
                        color = color.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )
            // Inner circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
