package com.devicelens.app.ui.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.components.DeviceKind
import com.devicelens.app.ui.components.animatedCount
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.components.rememberPulse
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import com.devicelens.app.ui.theme.Tint

/**
 * The pieces of the status screen.
 *
 * Split out from the screen itself so the screen file stays a readable
 * description of the layout rather than eight hundred lines of nested boxes.
 */

// ─────────────────────────────────────────────────────────────────────
// Verdict
// ─────────────────────────────────────────────────────────────────────

/**
 * The answer to the only question the user came with: *am I okay?*
 *
 * It gets the largest type on the screen and the only saturated colour, because
 * everything else on this screen exists to support it. The previous design
 * spent its emphasis on three equally-weighted count cards, which made the
 * verdict something you had to assemble yourself from arithmetic.
 */
@Composable
fun VerdictHeader(
    status: OverallStatus,
    deviceCount: Int,
    suspiciousCount: Int,
    unknownCount: Int,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors

    val accent = when (status) {
        OverallStatus.SAFE -> colors.statusSafe
        OverallStatus.WARNING -> colors.statusWarning
        OverallStatus.RISK -> colors.statusRisk
        else -> colors.textTertiary
    }

    val headline = when {
        isScanning -> "Scanning"
        status == OverallStatus.RISK && suspiciousCount > 0 ->
            if (suspiciousCount == 1) "1 device needs attention"
            else "$suspiciousCount devices need attention"
        status == OverallStatus.RISK -> "Something needs attention"
        status == OverallStatus.WARNING && unknownCount > 0 ->
            if (unknownCount == 1) "1 device unidentified"
            else "$unknownCount devices unidentified"
        status == OverallStatus.WARNING -> "Worth a look"
        status == OverallStatus.SAFE -> "Nothing unexpected"
        else -> "Not scanned yet"
    }

    val support = when {
        isScanning -> "Checking every address and listening on both radios."
        status == OverallStatus.RISK ->
            "Open the flagged devices below to see what they are and what to do."
        status == OverallStatus.WARNING ->
            "These are probably fine. Mark the ones you recognise as yours to quieten them."
        status == OverallStatus.SAFE ->
            "Every device here is one you've confirmed."
        else -> "Run a scan to see what's on this network and what's broadcasting nearby."
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = accent, live = isScanning)
            Spacer(Modifier.width(Space.sm))
            Text(
                text = if (isScanning) "SCANNING" else statusLabel(status).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            if (deviceCount > 0) {
                Spacer(Modifier.width(Space.sm))
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textQuaternary
                )
                Spacer(Modifier.width(Space.sm))
                // The count eases rather than jumping, so devices arriving during
                // a scan are visible as movement in the corner of the eye.
                Text(
                    text = "${animatedCount(deviceCount)} found",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }

        Spacer(Modifier.height(Space.md))

        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary
        )

        Spacer(Modifier.height(Space.sm))

        Text(
            text = support,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

private fun statusLabel(status: OverallStatus) = when (status) {
    OverallStatus.SAFE -> "Clear"
    OverallStatus.WARNING -> "Review"
    OverallStatus.RISK -> "Attention"
    OverallStatus.SCANNING -> "Scanning"
    OverallStatus.NOT_CALIBRATED -> "Idle"
}

/** A dot that breathes only while something is actually happening. */
@Composable
private fun StatusDot(color: Color, live: Boolean) {
    val pulse by rememberPulse(active = live, min = 0.35f, max = 1f, periodMs = 1100)

    Box(contentAlignment = Alignment.Center) {
        if (live) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        alpha = (pulse - 0.3f).coerceAtLeast(0f) * 0.5f
                        val s = 0.7f + pulse * 0.5f
                        scaleX = s
                        scaleY = s
                    }
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Composition bar
// ─────────────────────────────────────────────────────────────────────

/**
 * The make-up of the network as one proportional bar.
 *
 * Replaces three big number cards. A bar answers "how much of this is a
 * problem?" instantly and in one glance, where three numbers require the reader
 * to compare them and do the division themselves. It also takes a quarter of
 * the vertical space, which is space the device list needed.
 */
@Composable
fun CompositionBar(
    safeCount: Int,
    unknownCount: Int,
    suspiciousCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors
    val total = (safeCount + unknownCount + suspiciousCount).coerceAtLeast(1)

    // Segments animate their weight, so a scan that reclassifies devices shows
    // the bar redistributing rather than blinking to a new shape.
    val suspiciousWeight by animateFloatAsState(
        targetValue = suspiciousCount.toFloat() / total,
        animationSpec = Motion.smooth(),
        label = "suspiciousWeight"
    )
    val unknownWeight by animateFloatAsState(
        targetValue = unknownCount.toFloat() / total,
        animationSpec = Motion.smooth(),
        label = "unknownWeight"
    )
    val safeWeight by animateFloatAsState(
        targetValue = safeCount.toFloat() / total,
        animationSpec = Motion.smooth(),
        label = "safeWeight"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(Radius.full)
                .background(colors.surfaceGlassHighlight),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (suspiciousWeight > 0.001f) {
                Segment(weight = suspiciousWeight, color = colors.statusRisk)
            }
            if (unknownWeight > 0.001f) {
                Segment(weight = unknownWeight, color = colors.statusWarning)
            }
            if (safeWeight > 0.001f) {
                Segment(weight = safeWeight, color = colors.statusSafe)
            }
        }

        Spacer(Modifier.height(Space.md))

        Row(horizontalArrangement = Arrangement.spacedBy(Space.lg)) {
            LegendEntry("Attention", suspiciousCount, colors.statusRisk)
            LegendEntry("Unidentified", unknownCount, colors.statusWarning)
            LegendEntry("Yours", safeCount, colors.statusSafe)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Segment(weight: Float, color: Color) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(6.dp)
            .clip(Radius.full)
            .background(color)
    )
}

@Composable
private fun LegendEntry(label: String, count: Int, color: Color) {
    val colors = ExtendedTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(Space.sm))
        Text(
            text = "${animatedCount(count)}",
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary
        )
        Spacer(Modifier.width(Space.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Filtering
// ─────────────────────────────────────────────────────────────────────

/**
 * Search and filters.
 *
 * Added because a real scan on a real network returns hundreds of rows, and
 * without a way to narrow them the one device that matters is somewhere on
 * screen nine. The risk chips come first because "show me only the alarming
 * ones" is overwhelmingly the most common thing a person wants here.
 */
@Composable
fun DeviceFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    riskFilter: RiskFilter,
    onRiskFilterChange: (RiskFilter) -> Unit,
    availableKinds: List<DeviceKind>,
    selectedKind: DeviceKind?,
    onKindChange: (DeviceKind?) -> Unit,
    counts: Map<RiskFilter, Int>,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Search name, vendor, IP or MAC",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textQuaternary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                // Only present when there is something to clear — a permanently
                // visible clear button on an empty field is noise.
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(Motion.fade()),
                    exit = fadeOut(Motion.fade())
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .pressable(onClick = { onQueryChange("") }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear search",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = Radius.sm,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceGlassHighlight,
                unfocusedContainerColor = colors.surfaceGlass,
                disabledContainerColor = colors.surfaceGlass,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = colors.securityTeal,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        Spacer(Modifier.height(Space.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            RiskFilter.entries.forEach { filter ->
                val count = counts[filter] ?: 0
                // A filter that would return nothing is not offered, so tapping a
                // chip never leads to an empty screen.
                if (filter == RiskFilter.ALL || count > 0) {
                    FilterChip(
                        label = filter.label,
                        count = if (filter == RiskFilter.ALL) null else count,
                        selected = riskFilter == filter,
                        accent = when (filter) {
                            RiskFilter.SUSPICIOUS -> colors.statusRisk
                            RiskFilter.UNKNOWN -> colors.statusWarning
                            RiskFilter.SAFE -> colors.statusSafe
                            RiskFilter.ALL -> colors.securityTeal
                        },
                        onClick = { onRiskFilterChange(filter) }
                    )
                }
            }

            if (availableKinds.size > 1) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = Space.xs)
                        .width(1.dp)
                        .height(28.dp)
                        .background(colors.hairlineBorder)
                )

                availableKinds.forEach { kind ->
                    FilterChip(
                        label = kind.label,
                        count = null,
                        selected = selectedKind == kind,
                        accent = colors.securityTeal,
                        icon = kind.icon,
                        onClick = { onKindChange(kind) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int?,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val colors = ExtendedTheme.colors

    // Background and border cross-fade between states rather than switching, so
    // selecting a chip reads as one object changing rather than two swapping.
    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = Motion.standard(),
        label = "chipSelection"
    )

    val background = androidx.compose.ui.graphics.lerp(
        colors.surfaceGlass, accent.copy(alpha = Tint.medium), selection
    )
    val contentColor = androidx.compose.ui.graphics.lerp(
        colors.textSecondary, accent, selection
    )

    Row(
        modifier = Modifier
            .clip(Radius.full)
            .background(background)
            .border(1.dp, if (selected) accent.copy(alpha = 0.35f) else colors.hairlineBorder, Radius.full)
            .pressable(onClick = onClick, pressScale = 0.94f)
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(Space.xs))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (count != null) {
            Spacer(Modifier.width(Space.xs))
            Text(
                text = count.toString(),
                style = MonoType.small,
                color = contentColor.copy(alpha = 0.75f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Containers
// ─────────────────────────────────────────────────────────────────────

/** The grouped surface device rows live on. */
@Composable
fun ListGroup(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val colors = ExtendedTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.hairlineBorder, Radius.md),
        content = content
    )
}

/** A small caps label that introduces a section. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    val colors = ExtendedTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MonoType.small,
                color = colors.textQuaternary
            )
        }
    }
}
