package com.devicelens.app.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.domain.analysis.DeviceExplainer
import com.devicelens.app.ui.components.DeviceKind
import com.devicelens.app.ui.components.SignalBars
import com.devicelens.app.ui.components.enterStaggered
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.components.rememberHaptics
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.LocalWindowMetrics
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import com.devicelens.app.ui.theme.Tint

/**
 * Everything known about one device.
 *
 * Ordered by what a worried person actually asks, in order: *what is this*,
 * *what can it do to me*, *what should I do*, and only then the raw technical
 * detail. The previous layout led with a key-value table of MAC and IP —
 * accurate, and useless to almost everyone who opens this screen.
 */
@Composable
fun DeviceDetailsScreen(
    onBack: () -> Unit,
    onLocate: (Long) -> Unit,
    viewModel: DeviceDetailsViewModel = hiltViewModel()
) {
    val metrics = LocalWindowMetrics.current
    val haptics = rememberHaptics()
    val colors = ExtendedTheme.colors

    val device by viewModel.device.collectAsStateWithLifecycle()
    val explanation by viewModel.explanation.collectAsStateWithLifecycle()
    val canLocate by viewModel.canLocate.collectAsStateWithLifecycle()
    val firstSeen by viewModel.firstSeenRelative.collectAsStateWithLifecycle()
    val lastSeen by viewModel.lastSeenRelative.collectAsStateWithLifecycle()
    val detectionLabel by viewModel.detectionLabel.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { onBack() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        val current = device

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = metrics.horizontalPadding,
                end = metrics.horizontalPadding,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + Space.sm,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 160.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Space.xl)
        ) {
            item(key = "back") {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceGlass)
                        .pressable(onClick = onBack, pressScale = 0.9f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (current != null) {
                item(key = "hero") {
                    DeviceHero(device = current, modifier = Modifier.enterStaggered(0))
                }

                explanation?.let { exp ->
                    item(key = "what") {
                        Section(title = "What this is", modifier = Modifier.enterStaggered(1)) {
                            Text(
                                text = exp.whatItIs,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.height(Space.md))
                            Text(
                                text = exp.whyItIsHere,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (exp.capabilities.isNotEmpty()) {
                        item(key = "capabilities") {
                            Section(title = "What it can do", modifier = Modifier.enterStaggered(2)) {
                                exp.capabilities.forEachIndexed { index, capability ->
                                    CapabilityRow(capability)
                                    if (index < exp.capabilities.lastIndex) {
                                        Spacer(Modifier.height(Space.md))
                                    }
                                }
                            }
                        }
                    }

                    item(key = "action") {
                        Section(title = "What to do", modifier = Modifier.enterStaggered(3)) {
                            Text(
                                text = exp.whatToDo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }

                    if (exp.evidence.isNotEmpty()) {
                        item(key = "evidence") {
                            EvidenceSection(
                                evidence = exp.evidence,
                                confidence = exp.confidence,
                                modifier = Modifier.enterStaggered(4)
                            )
                        }
                    }
                }

                item(key = "facts") {
                    TechnicalFacts(
                        device = current,
                        firstSeen = firstSeen,
                        lastSeen = lastSeen,
                        detectionLabel = detectionLabel,
                        modifier = Modifier.enterStaggered(5)
                    )
                }
            }
        }

        if (current != null) {
            ActionBar(
                isTrusted = current.isTrustedByUser,
                canLocate = canLocate,
                onTrust = { haptics.success(); viewModel.markAsMine() },
                onDismiss = { haptics.tap(); viewModel.dismiss() },
                onLocate = { haptics.commit(); onLocate(current.id) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = metrics.horizontalPadding,
                        vertical = Space.xl
                    )
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────

@Composable
private fun DeviceHero(device: DeviceEntity, modifier: Modifier = Modifier) {
    val colors = ExtendedTheme.colors
    val kind = DeviceKind.resolve(device.deviceType, device.deviceName, device.vendor)

    val (accent, verdict) = when (device.riskLevel) {
        "SAFE" -> colors.statusSafe to "Yours"
        "SUSPICIOUS" -> colors.statusRisk to "Needs attention"
        else -> colors.statusWarning to "Unidentified"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(accent.copy(alpha = Tint.subtle)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = kind.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(Space.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = verdict.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary
                )
            }

            device.rssiLastSeen?.let { rssi ->
                Spacer(Modifier.width(Space.md))
                Column(horizontalAlignment = Alignment.End) {
                    SignalBars(rssi = rssi, tint = accent)
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        text = "$rssi dBm",
                        style = MonoType.small,
                        color = colors.textQuaternary
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val colors = ExtendedTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(Space.md))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.md)
                .background(colors.surfaceGlass)
                .border(1.dp, colors.hairlineBorder, Radius.md)
                .padding(Space.lg),
            content = content
        )
    }
}

/**
 * One capability, with a severity dot.
 *
 * Levels are colour-coded so the serious ones can be picked out without reading
 * every line — a camera's entry is scanned, not studied.
 */
@Composable
private fun CapabilityRow(capability: DeviceExplainer.Capability) {
    val colors = ExtendedTheme.colors
    val color = when (capability.level) {
        DeviceExplainer.Capability.Level.SERIOUS -> colors.statusRisk
        DeviceExplainer.Capability.Level.NOTABLE -> colors.statusWarning
        DeviceExplainer.Capability.Level.BENIGN -> colors.statusSafe
    }

    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(Space.md))
        Text(
            text = capability.label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}

/**
 * The observations behind the verdict, collapsed by default.
 *
 * Available rather than absent: a user should be able to check our reasoning
 * instead of trusting a label, and a security tool that will not show its
 * working has not earned the trust it is asking for.
 */
@Composable
private fun EvidenceSection(
    evidence: List<String>,
    confidence: DeviceExplainer.Explanation.Confidence,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.md)
                .background(colors.surfaceGlass)
                .border(1.dp, colors.hairlineBorder, Radius.md)
                .pressable(onClick = { expanded = !expanded }, pressScale = 0.99f)
                .padding(Space.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Why we think this",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${evidence.size} observations · ${confidence.name.lowercase()} confidence",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Hide" else "Show",
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = Space.sm)) {
                evidence.forEach { line ->
                    Row(
                        modifier = Modifier.padding(vertical = Space.xs),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "—",
                            style = MonoType.small,
                            color = colors.textQuaternary
                        )
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/** The raw facts, in monospace, last — where the people who want them will look. */
@Composable
private fun TechnicalFacts(
    device: DeviceEntity,
    firstSeen: String,
    lastSeen: String,
    detectionLabel: String,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors

    val facts = buildList {
        device.ipAddress?.takeIf { it.isNotBlank() }?.let { add("Address" to it) }
        device.macAddress?.takeIf { it.isNotBlank() }?.let { add("Hardware" to it) }
        device.vendor.takeIf { it.isNotBlank() && it != "Unknown" }?.let { add("Vendor" to it) }
        device.deviceType.takeIf { it.isNotBlank() }?.let { add("Reported as" to it) }
        device.openPorts.takeIf { it.isNotBlank() }?.let { add("Open ports" to it) }
        detectionLabel.takeIf { it.isNotBlank() }?.let { add("Found by" to it) }
        firstSeen.takeIf { it.isNotBlank() }?.let { add("First seen" to it.removePrefix("First seen ")) }
        lastSeen.takeIf { it.isNotBlank() }?.let { add("Last seen" to it.removePrefix("Last seen ")) }
        add("Times seen" to device.seenCount.toString())
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "TECHNICAL DETAIL",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(Space.md))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.md)
                .background(colors.surfaceGlass)
                .border(1.dp, colors.hairlineBorder, Radius.md)
        ) {
            facts.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.lg, vertical = Space.md),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        modifier = Modifier.width(104.dp)
                    )
                    Text(
                        text = value,
                        style = MonoType.medium,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < facts.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = Space.lg)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.hairlineBorder)
                    )
                }
            }
        }
    }
}

/**
 * Pinned actions.
 *
 * "This is mine" is the primary because it is the action that improves every
 * future scan. Locate is offered only when there is a signal to walk towards —
 * a button that cannot work is worse than no button.
 */
@Composable
private fun ActionBar(
    isTrusted: Boolean,
    canLocate: Boolean,
    onTrust: () -> Unit,
    onDismiss: () -> Unit,
    onLocate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        if (canLocate) {
            Surface(
                color = colors.surfaceGlassHighlight,
                shape = Radius.lg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.hairlineBorder, Radius.lg)
                    .pressable(onClick = onLocate, pressScale = 0.985f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = Space.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.MyLocation,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = "Find where it is",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            Surface(
                color = if (isTrusted) colors.surfaceGlassHighlight else MaterialTheme.colorScheme.primary,
                shape = Radius.lg,
                modifier = Modifier
                    .weight(1f)
                    .pressable(onClick = onTrust, enabled = !isTrusted, pressScale = 0.98f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = Space.lg),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTrusted) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = colors.statusSafe,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(Space.sm))
                    }
                    Text(
                        text = if (isTrusted) "Marked as yours" else "This is mine",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isTrusted) colors.textSecondary
                        else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (!isTrusted) {
                Surface(
                    color = Color.Transparent,
                    shape = Radius.lg,
                    modifier = Modifier
                        .border(1.dp, colors.hairlineBorderStrong, Radius.lg)
                        .pressable(onClick = onDismiss, pressScale = 0.96f)
                ) {
                    Text(
                        text = "Ignore",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.lg)
                    )
                }
            }
        }
    }
}
