package com.devicelens.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.EaseOutCubic

enum class SignalTrend { STRONGER, STABLE, WEAKER }

@Composable
fun SignalCircle(
    trend: SignalTrend,
    modifier: Modifier = Modifier
) {
    val securityTeal = ExtendedTheme.colors.securityTeal

    val targetScale = when (trend) {
        SignalTrend.STRONGER -> 1.4f
        SignalTrend.STABLE -> 1.0f
        SignalTrend.WEAKER -> 0.6f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "signalScale"
    )

    val pulseTransition = rememberInfiniteTransition(label = "signalPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(animatedScale * pulse * 0.9f)
                .background(
                    color = securityTeal.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        // Middle ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(animatedScale * pulse * 0.95f)
                .background(
                    color = securityTeal.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
        // Inner circle
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(animatedScale)
                .background(
                    color = securityTeal,
                    shape = CircleShape
                )
        )
    }
}
