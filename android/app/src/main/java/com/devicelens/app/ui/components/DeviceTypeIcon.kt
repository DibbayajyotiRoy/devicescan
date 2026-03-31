package com.devicelens.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeviceTypeIcon(
    deviceName: String,
    vendor: String,
    modifier: Modifier = Modifier
) {
    val name = deviceName.lowercase()
    val v = vendor.lowercase()

    val icon = when {
        name.contains("router") || name.contains("gateway") ||
        v.contains("netgear") || v.contains("tp-link") ||
        v.contains("linksys") || v.contains("cisco") -> Icons.Rounded.Router

        name.contains("iphone") || name.contains("pixel") ||
        name.contains("galaxy") || name.contains("android") ||
        name.contains("phone") -> Icons.Rounded.PhoneAndroid

        name.contains("macbook") || name.contains("laptop") ||
        name.contains("pc") || name.contains("thinkpad") ||
        v.contains("dell") || v.contains("lenovo") -> Icons.Rounded.Laptop

        name.contains("tv") || name.contains("chromecast") ||
        name.contains("roku") || name.contains("fire tv") -> Icons.Rounded.Tv

        name.contains("speaker") || name.contains("echo") ||
        name.contains("homepod") || name.contains("airpods") ||
        name.contains("buds") || v.contains("bose") ||
        v.contains("jbl") -> Icons.Rounded.Speaker

        name.contains("watch") || name.contains("band") ||
        name.contains("fitbit") -> Icons.Rounded.Watch

        name.contains("camera") || name.contains("cam") ||
        v.contains("hikvision") || v.contains("wyze") -> Icons.Rounded.CameraAlt

        name.contains("sensor") || name.contains("thermostat") ||
        name.contains("plug") || name.contains("smart") -> Icons.Rounded.Sensors

        else -> Icons.Rounded.DevicesOther
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}
