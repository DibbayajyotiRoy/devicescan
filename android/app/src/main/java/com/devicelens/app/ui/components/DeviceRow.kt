package com.devicelens.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.ui.theme.RiskRed
import com.devicelens.app.ui.theme.SafeGreen
import com.devicelens.app.ui.theme.WarningAmber

@Composable
fun DeviceRow(
    device: DeviceEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val riskColor = when (device.riskLevel) {
        "SAFE" -> SafeGreen
        "SUSPICIOUS" -> RiskRed
        else -> WarningAmber
    }

    val isSuspicious = device.riskLevel == "SUSPICIOUS"

    val detectionBadge = when (device.detectionMethod) {
        "WIFI" -> "Wi-Fi"
        "BLE" -> "Bluetooth"
        "BOTH" -> "Wi-Fi + BT"
        else -> device.detectionMethod
    }

    val typeIcon = getDeviceTypeIcon(device.deviceType)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.97f else 1f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = if (isSuspicious)
            RiskRed.copy(alpha = 0.06f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                )
                .fillMaxWidth()
        ) {
            // Left risk accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(riskColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceTypeIcon(
                    deviceName = device.deviceName,
                    vendor = device.vendor,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Row 1: Device name
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSuspicious) RiskRed else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))

                    // Row 2: Type icon + detection method + vendor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Device type icon indicator (replaces text badge)
                        if (typeIcon != null) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSuspicious) RiskRed.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = device.deviceType,
                                        tint = if (isSuspicious) RiskRed else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        // Detection method chip
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = detectionBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (device.vendor != "Unknown") {
                            Text(
                                text = device.vendor,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Row 3: IP + MAC
                    val subInfo = buildString {
                        device.ipAddress?.let { append(it) }
                        device.macAddress?.let {
                            if (isNotEmpty()) append(" · ")
                            append(it)
                        }
                    }
                    if (subInfo.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subInfo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Row 4: Open ports (if any)
                    if (device.openPorts.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ports: ${device.openPorts}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun getDeviceTypeIcon(deviceType: String?): ImageVector? {
    if (deviceType.isNullOrBlank() || deviceType == "Unknown") return null

    val type = deviceType.lowercase()
    return when {
        // Camera devices
        "camera" in type || "dvr" in type || "nvr" in type || "surveillance" in type ->
            Icons.Rounded.Videocam

        // Router/Network
        "router" in type || "gateway" in type || "modem" in type ->
            Icons.Rounded.Router

        // Media/TV
        "tv" in type || "media" in type || "chromecast" in type || "roku" in type ||
        "fire tv" in type || "apple tv" in type || "bravia" in type ->
            Icons.Rounded.Tv

        // Speaker/Audio
        "speaker" in type || "audio" in type || "sonos" in type || "echo" in type ||
        "homepod" in type || "airpods" in type ->
            Icons.Rounded.Speaker

        // Computer
        "computer" in type || "laptop" in type || "desktop" in type || "pc" in type ||
        "macbook" in type || "imac" in type || "server" in type ->
            Icons.Rounded.Computer

        // Phone/Mobile
        "phone" in type || "mobile" in type || "iphone" in type || "pixel" in type ||
        "galaxy" in type || "android" in type ->
            Icons.Rounded.Smartphone

        // Wearable
        "watch" in type || "wearable" in type || "fitbit" in type || "band" in type ->
            Icons.Rounded.Watch

        // Printer
        "printer" in type ->
            Icons.Rounded.Print

        // NAS/Storage
        "nas" in type || "file server" in type || "storage" in type ->
            Icons.Rounded.Storage

        // IoT/Smart Home
        "iot" in type || "smart" in type || "sensor" in type || "thermostat" in type ||
        "nest" in type || "ring" in type || "hue" in type || "plug" in type ->
            Icons.Rounded.SmartToy

        // Cloud/Network Service
        "cloud" in type || "network" in type ->
            Icons.Rounded.Cloud

        // Default for unknown
        else -> Icons.Rounded.Devices
    }
}
