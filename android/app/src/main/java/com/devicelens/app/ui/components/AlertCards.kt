package com.devicelens.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devicelens.app.domain.analysis.NetworkThreatAnalyzer
import com.devicelens.app.domain.analysis.TrackerDetector
import com.devicelens.app.ui.theme.ExtendedTheme

/**
 * Cards for findings that are about the *situation* rather than about one
 * device: a tracker travelling with the user, a router that changed identity,
 * an unencrypted network. These sit above the device list because they are
 * what the user needs to act on first.
 */

@Composable
fun TrackerAlertCard(
    alert: TrackerDetector.TrackerAlert,
    onMarkAsMine: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(alert.severity == TrackerDetector.TrackerAlert.Severity.CRITICAL) }

    val accent = when (alert.severity) {
        TrackerDetector.TrackerAlert.Severity.CRITICAL -> ExtendedTheme.colors.statusRisk
        TrackerDetector.TrackerAlert.Severity.WARNING -> ExtendedTheme.colors.statusWarning
        TrackerDetector.TrackerAlert.Severity.INFO -> ExtendedTheme.colors.textTertiary
    }

    AlertShell(
        accent = accent,
        icon = Icons.Rounded.MyLocation,
        title = alert.headline,
        subtitle = alert.detail,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        modifier = modifier
    ) {
        Text(
            text = alert.advice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = observedSummary(alert),
            style = MaterialTheme.typography.labelSmall,
            color = ExtendedTheme.colors.textTertiary
        )
        TextButton(onClick = onMarkAsMine) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text("This is mine — stop alerting")
        }
    }
}

private fun observedSummary(alert: TrackerDetector.TrackerAlert): String {
    val parts = mutableListOf("Seen in ${alert.scanCount} scans")
    if (alert.minutesObserved > 0) parts += "over ${alert.minutesObserved} min"
    if (alert.networksSeenOn > 1) parts += "on ${alert.networksSeenOn} networks"
    alert.closestRssi?.let { parts += "closest signal $it dBm" }
    return parts.joinToString(" · ")
}

@Composable
fun NetworkAlertCard(
    alert: NetworkThreatAnalyzer.NetworkAlert,
    modifier: Modifier = Modifier
) {
    var expanded by remember {
        mutableStateOf(alert.severity == NetworkThreatAnalyzer.NetworkAlert.Severity.CRITICAL)
    }

    val accent = when (alert.severity) {
        NetworkThreatAnalyzer.NetworkAlert.Severity.CRITICAL -> ExtendedTheme.colors.statusRisk
        NetworkThreatAnalyzer.NetworkAlert.Severity.WARNING -> ExtendedTheme.colors.statusWarning
        NetworkThreatAnalyzer.NetworkAlert.Severity.INFO -> ExtendedTheme.colors.textTertiary
    }

    val icon = when (alert.severity) {
        NetworkThreatAnalyzer.NetworkAlert.Severity.INFO -> Icons.Rounded.Info
        else -> Icons.Rounded.Warning
    }

    AlertShell(
        accent = accent,
        icon = icon,
        title = alert.title,
        subtitle = alert.detail,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        modifier = modifier
    ) {
        Text(
            text = alert.advice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val target = listOfNotNull(alert.relatedIp, alert.relatedMac)
        if (target.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = target.joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = ExtendedTheme.colors.textTertiary
            )
        }
    }
}

/**
 * Shared chrome so every alert reads the same way: a one-line claim that is
 * always visible, and the reasoning plus advice a tap away. Collapsing the
 * detail keeps a long list of findings scannable.
 */
@Composable
private fun AlertShell(
    accent: Color,
    icon: ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.08f))
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Show less" else "Show more",
                tint = ExtendedTheme.colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 10.dp, start = 32.dp)) {
                content()
            }
        }
    }
}
