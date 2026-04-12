package com.devicelens.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DeviceIcon(
    deviceType: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val icon = when (deviceType?.uppercase()) {
        "CAMERA", "IP CAMERA", "WEBCAM" -> Icons.Rounded.Videocam
        "MOBILE", "SMARTPHONE", "PHONE" -> Icons.Rounded.PhoneAndroid
        "COMPUTER", "LAPTOP", "DESKTOP" -> Icons.Rounded.Computer
        "TABLET" -> Icons.Rounded.TabletAndroid
        "WATCH", "SMARTWATCH" -> Icons.Rounded.Watch
        "TV", "SMART TV", "DISPLAY" -> Icons.Rounded.Tv
        "PRINTER" -> Icons.Rounded.Print
        "ROUTER", "GATEWAY" -> Icons.Rounded.Router
        "LIGHT", "BULB" -> Icons.Rounded.Lightbulb
        "SPEAKER", "AUDIO" -> Icons.Rounded.Speaker
        "IOT", "SENSOR" -> Icons.Rounded.Sensors
        "STATION" -> Icons.Rounded.SettingsInputAntenna
        else -> Icons.Rounded.DevicesOther
    }

    Icon(
        imageVector = icon,
        contentDescription = deviceType,
        modifier = modifier,
        tint = tint
    )
}
