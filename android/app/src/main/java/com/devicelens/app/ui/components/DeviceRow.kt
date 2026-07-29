package com.devicelens.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.JetBrainsMonoFamily
import com.devicelens.app.ui.theme.ShapeMedium

// ═════════════════════════════════════════════════════════════════════════════
// DEVICE ROW — DOUBLE-BEZEL NESTED ARCHITECTURE
// Glass-morphic cards with haptic press feedback and risk indicators
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun DeviceRow(
    device: DeviceEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Theme-aware status colors
    val riskColor = when (device.riskLevel) {
        "SAFE" -> ExtendedTheme.colors.statusSafe
        "SUSPICIOUS" -> ExtendedTheme.colors.statusRisk
        else -> ExtendedTheme.colors.statusWarning
    }

    val riskGlowColor = when (device.riskLevel) {
        "SAFE" -> ExtendedTheme.colors.statusSafeGlow
        "SUSPICIOUS" -> ExtendedTheme.colors.statusRiskGlow
        else -> ExtendedTheme.colors.statusWarningGlow
    }

    val isSuspicious = device.riskLevel == "SUSPICIOUS"

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "pressScale"
    )

    // Outer shell — Double-bezel glass container with theme-aware accent
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clip(ShapeMedium)
            .background(
                if (isSuspicious) {
                    riskColor.copy(alpha = 0.2f)
                } else {
                    ExtendedTheme.colors.hairlineBorder
                }
            )
            .padding(1.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
    ) {
        // Inner core — glass-morphic content container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            if (isSuspicious) riskGlowColor.copy(alpha = 0.3f)
                            else ExtendedTheme.colors.surfaceGlass,
                            if (isSuspicious) riskGlowColor.copy(alpha = 0.1f)
                            else ExtendedTheme.colors.surfaceGlassHighlight.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device icon
                DeviceTypeIcon(
                    deviceName = device.deviceName,
                    vendor = device.vendor,
                    deviceType = device.deviceType,
                    modifier = Modifier.size(48.dp),
                    isSuspicious = isSuspicious
                )

                // Content column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Device name + risk badge if suspicious
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = device.deviceName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSuspicious) ExtendedTheme.colors.statusRisk
                            else MaterialTheme.colorScheme.onSurface
                        )

                        // Risk badge for suspicious devices
                        if (isSuspicious) {
                            RiskBadge()
                        }
                    }

                    // Row 2: Detection method + vendor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Detection method pill
                        DetectionPill(
                            method = device.detectionMethod,
                            isSuspicious = isSuspicious
                        )

                        // Vendor chip
                        if (device.vendor != "Unknown") {
                            Text(
                                text = device.vendor,
                                style = MaterialTheme.typography.labelSmall,
                                color = ExtendedTheme.colors.textTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Row 3: Technical details (IP, MAC)
                    val subInfo = buildString {
                        device.ipAddress?.let { append(it) }
                        device.macAddress?.let {
                            if (isNotEmpty()) append(" · ")
                            append(it.uppercase())
                        }
                    }
                    if (subInfo.isNotEmpty()) {
                        Text(
                            text = subInfo,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFamily,
                            color = ExtendedTheme.colors.textQuaternary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Row 4: Open ports (if any)
                    if (device.openPorts.isNotBlank()) {
                        Text(
                            text = "Ports: ${device.openPorts}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFamily,
                            color = ExtendedTheme.colors.textQuaternary.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Trailing chevron
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = ExtendedTheme.colors.textQuaternary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DetectionPill(
    method: String,
    isSuspicious: Boolean
) {
    val (icon, label) = when (method) {
        "WIFI" -> Icons.Outlined.Wifi to "Wi-Fi"
        "BLE" -> Icons.Outlined.Bluetooth to "Bluetooth"
        "BOTH" -> Icons.Outlined.Wifi to "Dual"
        else -> Icons.Outlined.Wifi to method
    }

    val riskColor = if (isSuspicious) ExtendedTheme.colors.statusRisk else ExtendedTheme.colors.textTertiary

    val backgroundColor = if (isSuspicious) {
        ExtendedTheme.colors.statusRiskGlow.copy(alpha = 0.4f)
    } else {
        ExtendedTheme.colors.surfaceGlassBorder.copy(alpha = 0.3f)
    }

    val contentColor = riskColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
private fun RiskBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ExtendedTheme.colors.statusRiskGlow.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "SUSPICIOUS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ExtendedTheme.colors.statusRisk
        )
    }
}

// Keep reference to detectDeviceIcon for legacy compatibility
@Deprecated("Use detectDeviceIcon from DeviceTypeIcon.kt instead")
private fun getDeviceTypeIcon(deviceType: String?): ImageVector? {
    if (deviceType.isNullOrBlank() || deviceType == "Unknown") return null

    val type = deviceType.lowercase()
    return when {
        type.contains("camera") || type.contains("dvr") || type.contains("nvr") ->
            Icons.Outlined.Videocam
        type.contains("router") || type.contains("gateway") || type.contains("modem") ->
            Icons.Outlined.Router
        type.contains("tv") || type.contains("media") || type.contains("chromecast") ->
            Icons.Outlined.Tv
        type.contains("speaker") || type.contains("audio") ->
            Icons.Outlined.Speaker
        type.contains("computer") || type.contains("laptop") || type.contains("desktop") ->
            Icons.Outlined.Computer
        type.contains("phone") || type.contains("mobile") ->
            Icons.Outlined.Smartphone
        type.contains("watch") || type.contains("wearable") ->
            Icons.Outlined.Watch
        type.contains("printer") ->
            Icons.Outlined.Print
        type.contains("nas") || type.contains("storage") ->
            Icons.Outlined.Storage
        else -> null
    }
}
