package com.devicelens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.devicelens.app.ui.theme.ExtendedTheme

// ═════════════════════════════════════════════════════════════════════════════
// DEVICE TYPE ICON — GLASS MORPHIC ICON CONTAINER
// Double-bezel nested architecture with intelligent device detection
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun DeviceTypeIcon(
    deviceName: String,
    vendor: String,
    deviceType: String = "",
    modifier: Modifier = Modifier,
    isSuspicious: Boolean = false
) {
    val icon = detectDeviceIcon(deviceName, vendor, deviceType)
    val iconColor = if (isSuspicious) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val containerColor = if (isSuspicious) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    } else {
        ExtendedTheme.colors.surfaceGlass.copy(alpha = 0.6f)
    }

    // Double-bezel outer shell
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ExtendedTheme.colors.hairlineBorder)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner core
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(13.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun detectDeviceIcon(deviceName: String, vendor: String, deviceType: String): ImageVector {
    val name = deviceName.lowercase()
    val vendorLower = vendor.lowercase()
    val type = deviceType.lowercase()

    return when {
        type.contains("camera") || type.contains("cam") -> Icons.Rounded.Videocam
        type.contains("phone") || type.contains("smartphone") -> Icons.Rounded.Smartphone
        type.contains("tablet") || type.contains("ipad") -> Icons.Rounded.Tablet
        type.contains("computer") || type.contains("laptop") || type.contains("desktop") -> Icons.Rounded.Computer
        type.contains("router") || type.contains("gateway") || type.contains("modem") -> Icons.Rounded.Router
        type.contains("tv") || type.contains("television") || type.contains("media") -> Icons.Rounded.Tv
        type.contains("speaker") || type.contains("audio") || type.contains("sound") -> Icons.Rounded.Speaker
        type.contains("watch") || type.contains("wearable") -> Icons.Rounded.Watch
        type.contains("printer") -> Icons.Rounded.Print
        type.contains("nas") || type.contains("storage") || type.contains("server") -> Icons.Rounded.Storage
        type.contains("sensor") -> Icons.Rounded.Sensors
        type.contains("hub") || type.contains("bridge") -> Icons.Rounded.DeviceHub
        type.contains("light") || type.contains("bulb") -> Icons.Rounded.Lightbulb
        type.contains("game") || type.contains("console") -> Icons.Rounded.SportsEsports
        type.contains("headphone") || type.contains("headset") -> Icons.Rounded.Headphones
        
        name.contains("iphone") || name.contains("ipad") || vendorLower.contains("apple") -> Icons.Rounded.Smartphone
        name.contains("android") || name.contains("pixel") || name.contains("samsung") -> Icons.Rounded.Smartphone
        name.contains("laptop") || name.contains("pc") || name.contains("macbook") -> Icons.Rounded.Computer
        
        else -> Icons.Rounded.Devices
    }
}
