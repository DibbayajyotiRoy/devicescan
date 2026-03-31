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
import androidx.compose.ui.draw.scale
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
        targetValue = if (status == OverallStatus.SCANNING) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val label = when (status) {
        OverallStatus.SAFE -> "Environment Secure"
        OverallStatus.WARNING -> "Unknown devices nearby"
        OverallStatus.RISK -> "Suspicious device detected"
        OverallStatus.SCANNING -> "Scanning your environment…"
        OverallStatus.NOT_CALIBRATED -> "Start your scan"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .background(color = color.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color = color, shape = CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
