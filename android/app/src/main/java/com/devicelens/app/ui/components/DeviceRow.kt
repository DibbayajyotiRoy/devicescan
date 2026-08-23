package com.devicelens.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import com.devicelens.app.ui.theme.Tint

/**
 * One device in the list.
 *
 * Designed as a row inside a grouped container rather than a floating card. A
 * screen of individually-shadowed cards reads as a pile of unrelated objects;
 * rows sharing one surface with hairline separators read as a single list, and
 * they fit far more devices on screen — which matters when a network has two
 * hundred of them.
 *
 * The information hierarchy is fixed and deliberate:
 *
 *  1. **Name** — the only line most people read.
 *  2. **What it is, and who made it** — the answer to "should this be here?".
 *  3. **Address** — set in monospace, because it is a machine fact, and dimmed,
 *     because it only matters once you are investigating.
 *
 * Risk is carried by a single 3 dp bar on the leading edge, not by tinting the
 * whole row. A list where every row is a coloured panel has no hierarchy left
 * to spend on the one row that is genuinely alarming.
 */
@Composable
fun DeviceRow(
    device: DeviceEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val colors = ExtendedTheme.colors
    val riskColor = when (device.riskLevel) {
        "SAFE" -> colors.statusSafe
        "SUSPICIOUS" -> colors.statusRisk
        else -> colors.statusWarning
    }
    val isSuspicious = device.riskLevel == "SUSPICIOUS"
    val kind = DeviceKind.resolve(device.deviceType, device.deviceName, device.vendor)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // No haptic here: in a scrolling list every touch-to-scroll would
                // fire one, and constant buzzing while scrolling is worse than none.
                .pressable(onClick = onClick, pressScale = 0.985f, hapticOnPress = false)
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RiskEdge(color = riskColor, emphasised = isSuspicious)

            Spacer(Modifier.width(Space.md))

            DeviceGlyph(kind = kind, riskColor = riskColor, emphasised = isSuspicious)

            Spacer(Modifier.width(Space.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                // Kind and vendor, joined only when both say something. A row
                // reading "Unidentified device · Unknown" is worse than silence.
                val descriptor = listOfNotNull(
                    kind.takeIf { it != DeviceKind.UNKNOWN }?.label,
                    device.vendor.takeIf { it.isNotBlank() && it != "Unknown" }
                ).distinct().joinToString(" · ")

                if (descriptor.isNotBlank()) {
                    Text(
                        text = descriptor,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val address = device.ipAddress?.takeIf { it.isNotBlank() }
                    ?: device.macAddress?.takeIf { it.isNotBlank() }
                if (address != null) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = address,
                        style = MonoType.small,
                        color = colors.textQuaternary,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(Space.sm))

            Column(horizontalAlignment = Alignment.End) {
                DetectionBadge(method = device.detectionMethod)
                device.rssiLastSeen?.let { rssi ->
                    Spacer(Modifier.height(Space.sm))
                    SignalBars(rssi = rssi, tint = riskColor)
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textQuaternary,
                modifier = Modifier
                    .padding(start = Space.xs)
                    .size(18.dp)
            )
        }

        if (showDivider) {
            // Inset to align with the text, not the container edge — the divider
            // separates rows, so it should start where the content does.
            Box(
                modifier = Modifier
                    .padding(start = 64.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.hairlineBorder)
            )
        }
    }
}

/** The leading risk bar. Present on every row so the list has a readable rhythm. */
@Composable
private fun RiskEdge(color: Color, emphasised: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (emphasised) 1f else 0.55f,
        animationSpec = Motion.standard(),
        label = "riskEdge"
    )
    Box(
        modifier = Modifier
            .width(3.dp)
            .height(if (emphasised) 34.dp else 26.dp)
            .clip(Radius.full)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * The device icon in a tinted tile.
 *
 * Neutral for everything except a genuine concern, which is the only case where
 * colour has something to say.
 */
@Composable
private fun DeviceGlyph(
    kind: DeviceKind,
    riskColor: Color,
    emphasised: Boolean
) {
    val colors = ExtendedTheme.colors
    val background = if (emphasised) riskColor.copy(alpha = Tint.subtle) else colors.surfaceGlassHighlight
    val tint = if (emphasised) riskColor else colors.textSecondary

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = kind.icon,
            contentDescription = kind.label,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

/** Which radio found this device. */
@Composable
private fun DetectionBadge(method: String) {
    val colors = ExtendedTheme.colors
    val icon = when (method) {
        "WIFI" -> Icons.Rounded.Wifi
        else -> Icons.Rounded.Bluetooth
    }
    Icon(
        imageVector = icon,
        contentDescription = when (method) {
            "WIFI" -> "Found on Wi-Fi"
            "BT_CLASSIC" -> "Found over Bluetooth"
            else -> "Found over Bluetooth LE"
        },
        tint = colors.textQuaternary,
        modifier = Modifier.size(14.dp)
    )
}

/**
 * Four bars for signal strength.
 *
 * RSSI is in dBm and negative — closer to zero is stronger. The thresholds
 * below are the usual practical ones: better than −55 is in the room with you,
 * worse than −85 is through a wall or two.
 */
@Composable
fun SignalBars(
    rssi: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors
    val filled = when {
        rssi >= -55 -> 4
        rssi >= -68 -> 3
        rssi >= -80 -> 2
        rssi >= -92 -> 1
        else -> 0
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) { index ->
            val isOn = index < filled
            // Each bar eases independently, so a device getting closer animates
            // up the scale instead of snapping between states.
            val alpha by animateFloatAsState(
                targetValue = if (isOn) 1f else 0.22f,
                animationSpec = Motion.standard(),
                label = "bar$index"
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((5 + index * 3).dp)
                    .clip(Radius.full)
                    .background((if (isOn) tint else colors.textQuaternary).copy(alpha = alpha))
            )
        }
    }
}
