package com.devicelens.app.ui.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.components.DeviceRow
import com.devicelens.app.ui.components.NetworkAlertCard
import com.devicelens.app.ui.components.ScrollEdgeFade
import com.devicelens.app.ui.components.TrackerAlertCard
import com.devicelens.app.ui.components.enterStaggered
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.components.rememberHaptics
import com.devicelens.app.ui.components.rememberScrollProgress
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.LocalWindowMetrics
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import com.devicelens.app.ui.theme.Tint
import kotlinx.coroutines.flow.StateFlow

/**
 * The home screen.
 *
 * Structured top-down by how urgently something needs the user's attention:
 * the verdict, then anything actively wrong, then the composition of the
 * network, then the devices themselves. Someone who reads only the first
 * screenful should still get the right answer.
 *
 * The scan control is a floating action rather than a card in the scroll,
 * because starting a scan is the one thing the user must always be able to do
 * without scrolling to find it.
 */
@Composable
fun StatusScreen(
    onDeviceClick: (Long) -> Unit,
    onNavigateToSetup: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDebugLog: () -> Unit,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val metrics = LocalWindowMetrics.current
    val haptics = rememberHaptics()
    val colors = ExtendedTheme.colors

    val overallStatus by viewModel.overallStatus.collectAsStateSafe()
    val safeCount by viewModel.safeCount.collectAsStateSafe()
    val unknownCount by viewModel.unknownCount.collectAsStateSafe()
    val suspiciousCount by viewModel.suspiciousCount.collectAsStateSafe()
    val allDevices by viewModel.devices.collectAsStateSafe()
    val devices by viewModel.filteredDevices.collectAsStateSafe()
    val isScanning by viewModel.isScanning.collectAsStateSafe()
    val scanProgress by viewModel.scanProgress.collectAsStateSafe()
    val networkAlerts by viewModel.networkAlerts.collectAsStateSafe()
    val trackerAlerts by viewModel.trackerAlerts.collectAsStateSafe()
    val networkSummary by viewModel.networkSummary.collectAsStateSafe()
    val unavailableReason by viewModel.scanUnavailableReason.collectAsStateSafe()
    val locationEnabled by viewModel.locationEnabled.collectAsStateSafe()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsStateSafe()
    val searchQuery by viewModel.searchQuery.collectAsStateSafe()
    val riskFilter by viewModel.riskFilter.collectAsStateSafe()
    val kindFilter by viewModel.kindFilter.collectAsStateSafe()
    val availableKinds by viewModel.availableKinds.collectAsStateSafe()
    val isFiltered by viewModel.isFiltered.collectAsStateSafe()

    val listState = rememberLazyListState()
    val scrollProgress by rememberScrollProgress(listState)

    LaunchedEffect(Unit) {
        viewModel.navigateToSetup.collect { onNavigateToSetup() }
    }

    val riskCounts = remember(safeCount, unknownCount, suspiciousCount) {
        mapOf(
            RiskFilter.ALL to (safeCount + unknownCount + suspiciousCount),
            RiskFilter.SUSPICIOUS to suspiciousCount,
            RiskFilter.UNKNOWN to unknownCount,
            RiskFilter.SAFE to safeCount
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = metrics.horizontalPadding,
                end = metrics.horizontalPadding,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + Space.md,
                // Room for the floating scan button, so the last row is never
                // trapped underneath it.
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 108.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            item(key = "toolbar") {
                TopRow(
                    networkName = networkSummary?.ssid ?: viewModel.getCurrentSsid(),
                    detail = networkSummary?.let { summary ->
                        listOfNotNull(summary.cidr, summary.securityLabel).joinToString(" · ")
                    },
                    onOpenSettings = onOpenSettings,
                    onOpenDebugLog = onOpenDebugLog
                )
            }

            item(key = "verdict") {
                VerdictHeader(
                    status = overallStatus,
                    deviceCount = allDevices.size,
                    suspiciousCount = suspiciousCount,
                    unknownCount = unknownCount,
                    isScanning = isScanning,
                    modifier = Modifier.widthIn(max = readableMax(metrics.maxContentWidth))
                )
            }

            if (isScanning) {
                item(key = "progress") {
                    ScanProgressPanel(progress = scanProgress)
                }
            }

            // Hardware that is switched off silently halves what a scan can see,
            // so it is surfaced before the results rather than after them.
            if (!locationEnabled || !bluetoothEnabled) {
                item(key = "hardware") {
                    HardwareNotice(
                        locationEnabled = locationEnabled,
                        bluetoothEnabled = bluetoothEnabled
                    )
                }
            }

            unavailableReason?.let { reason ->
                if (!isScanning) {
                    item(key = "unavailable") { InlineNotice(text = reason) }
                }
            }

            if (trackerAlerts.isNotEmpty()) {
                item(key = "trackers-label") {
                    SectionLabel("Following you", trailing = "${trackerAlerts.size}")
                }
                itemsIndexed(trackerAlerts, key = { _, alert -> "tracker-${alert.identity}" }) { index, alert ->
                    TrackerAlertCard(
                        alert = alert,
                        onMarkAsMine = { viewModel.markTrackerAsMine(alert.identity) },
                        modifier = Modifier.enterStaggered(index)
                    )
                }
            }

            if (networkAlerts.isNotEmpty()) {
                item(key = "network-label") {
                    SectionLabel("This network", trailing = "${networkAlerts.size}")
                }
                itemsIndexed(networkAlerts, key = { _, alert -> "alert-${alert.id}" }) { index, alert ->
                    NetworkAlertCard(
                        alert = alert,
                        modifier = Modifier.enterStaggered(index)
                    )
                }
            }

            if (allDevices.isNotEmpty()) {
                item(key = "composition") {
                    CompositionBar(
                        safeCount = safeCount,
                        unknownCount = unknownCount,
                        suspiciousCount = suspiciousCount
                    )
                }

                item(key = "filters") {
                    DeviceFilterBar(
                        query = searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        riskFilter = riskFilter,
                        onRiskFilterChange = viewModel::setRiskFilter,
                        availableKinds = availableKinds,
                        selectedKind = kindFilter,
                        onKindChange = viewModel::setKindFilter,
                        counts = riskCounts
                    )
                }
            }

            if (devices.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        isScanning = isScanning,
                        isFiltered = isFiltered,
                        hasAnyDevices = allDevices.isNotEmpty(),
                        onClearFilters = viewModel::clearFilters,
                        onScan = { haptics.commit(); viewModel.startScan() }
                    )
                }
            } else {
                item(key = "devices-label") {
                    SectionLabel(
                        text = if (isFiltered) "Matching devices" else "All devices",
                        trailing = if (isFiltered) "${devices.size} of ${allDevices.size}" else "${devices.size}"
                    )
                }

                // Chunked into groups so the rounded container's corners land on
                // real boundaries, and so a 200-row list is not one enormous
                // composable that has to re-measure as a unit.
                itemsIndexed(
                    items = devices,
                    // A stable, unique key is what keeps scroll position anchored
                    // when the list updates mid-scan.
                    key = { _, device -> device.id }
                ) { index, device ->
                    val isFirst = index == 0
                    val isLast = index == devices.lastIndex

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(groupShape(isFirst, isLast))
                            .background(ExtendedTheme.colors.surfaceGlass)
                    ) {
                        DeviceRow(
                            device = device,
                            onClick = { onDeviceClick(device.id) },
                            showDivider = !isLast,
                            // Only the first screenful staggers. Rows revealed by
                            // scrolling should already be there when they arrive.
                            modifier = if (index < 8) Modifier.enterStaggered(index) else Modifier
                        )
                    }
                }
            }
        }

        // Chrome sits above the content, and the content fades under it rather
        // than being cut off by a hard rule.
        ScrollEdgeFade(
            progress = scrollProgress,
            height = 56.dp,
            color = colors.canvas
        )

        ScanButton(
            isScanning = isScanning,
            progressPercent = scanProgress.percent,
            onClick = {
                haptics.commit()
                if (isScanning) viewModel.stopScan() else viewModel.startScan()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + Space.xxl
                )
        )
    }
}

private fun readableMax(max: Dp): Dp = if (max == Dp.Unspecified) Dp.Infinity else max

private fun groupShape(isFirst: Boolean, isLast: Boolean) = when {
    isFirst && isLast -> Radius.md
    isFirst -> androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = Radius.mdDp, topEnd = Radius.mdDp
    )
    isLast -> androidx.compose.foundation.shape.RoundedCornerShape(
        bottomStart = Radius.mdDp, bottomEnd = Radius.mdDp
    )
    else -> androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
}

// ─────────────────────────────────────────────────────────────────────
// Chrome
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun TopRow(
    networkName: String,
    detail: String?,
    onOpenSettings: () -> Unit,
    onOpenDebugLog: () -> Unit
) {
    val colors = ExtendedTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = networkName,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1
            )
            if (!detail.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                // The subnet and encryption in monospace: it tells the user
                // exactly what was scanned, which turns a claim into a fact.
                Text(
                    text = detail,
                    style = MonoType.small,
                    color = colors.textQuaternary,
                    maxLines = 1
                )
            }
        }

        IconAction(icon = Icons.Rounded.BugReport, description = "Debug log", onClick = onOpenDebugLog)
        Spacer(Modifier.width(Space.xs))
        IconAction(icon = Icons.Rounded.Settings, description = "Settings", onClick = onOpenSettings)
    }
}

@Composable
private fun IconAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = ExtendedTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surfaceGlass)
            .pressable(onClick = onClick, pressScale = 0.9f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * The scan control.
 *
 * Doubles as the progress indicator while a scan runs: the fill sweeping across
 * the button is the same object the user pressed, so progress is reported where
 * they are already looking rather than somewhere else on screen.
 */
@Composable
private fun ScanButton(
    isScanning: Boolean,
    progressPercent: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors

    val fill by animateFloatAsState(
        targetValue = if (isScanning) progressPercent / 100f else 0f,
        animationSpec = Motion.smooth(),
        label = "scanFill"
    )

    Surface(
        color = if (isScanning) colors.surfaceGlassHighlight else MaterialTheme.colorScheme.primary,
        shape = Radius.full,
        modifier = modifier
            .pressable(onClick = onClick, pressScale = 0.95f)
            .border(1.dp, colors.hairlineBorder, Radius.full)
    ) {
        Box {
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { scaleX = fill; transformOrigin = originLeft }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = Tint.strong))
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = Space.xxl, vertical = Space.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Rounded.Refresh else Icons.Rounded.Radar,
                    contentDescription = null,
                    tint = if (isScanning) colors.textPrimary else MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Space.sm))
                Text(
                    text = if (isScanning) "Scanning · $progressPercent%" else "Scan now",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isScanning) colors.textPrimary else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private val originLeft = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)

// ─────────────────────────────────────────────────────────────────────
// Panels
// ─────────────────────────────────────────────────────────────────────

/**
 * Live scan detail.
 *
 * Every line here is something the scanner is genuinely doing at that moment.
 * A scan legitimately takes tens of seconds; the fix for that is telling the
 * user what is happening, not pretending it is faster than it is.
 */
@Composable
private fun ScanProgressPanel(progress: com.devicelens.app.domain.model.ScanProgress) {
    val colors = ExtendedTheme.colors

    val fill by animateFloatAsState(
        targetValue = progress.percent / 100f,
        animationSpec = Motion.smooth(),
        label = "progressFill"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(colors.surfaceGlass)
            .border(1.dp, colors.hairlineBorder, Radius.md)
            .padding(Space.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = progress.message.ifBlank { "Getting started" },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(Space.md))
            Text(
                text = "${progress.percent}%",
                style = MonoType.medium,
                color = colors.securityTeal
            )
        }

        Spacer(Modifier.height(Space.md))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(Radius.full)
                .background(colors.surfaceGlassHighlight)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { scaleX = fill; transformOrigin = originLeft }
                    .background(colors.securityTeal)
            )
        }

        if (progress.devicesFound > 0) {
            Spacer(Modifier.height(Space.sm))
            Text(
                text = "${progress.devicesFound} found so far",
                style = MonoType.small,
                color = colors.textQuaternary
            )
        }
    }
}

@Composable
private fun HardwareNotice(locationEnabled: Boolean, bluetoothEnabled: Boolean) {
    val colors = ExtendedTheme.colors
    val missing = buildList {
        if (!bluetoothEnabled) add("Bluetooth" to Icons.Rounded.Bluetooth)
        if (!locationEnabled) add("Location" to Icons.Rounded.LocationOff)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(colors.statusWarning.copy(alpha = Tint.faint))
            .border(1.dp, colors.statusWarning.copy(alpha = 0.2f), Radius.md)
            .padding(Space.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            missing.forEach { (_, icon) ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.statusWarning,
                    modifier = Modifier
                        .padding(end = Space.sm)
                        .size(18.dp)
                )
            }
            Text(
                text = "${missing.joinToString(" and ") { it.first }} " +
                    if (missing.size == 1) "is off" else "are off",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
        }
        Spacer(Modifier.height(Space.xs))
        Text(
            text = "Scans will miss anything that relies on it. Turn it back on in " +
                "Android's quick settings and scan again.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun InlineNotice(text: String) {
    val colors = ExtendedTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textTertiary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.sm)
            .background(colors.surfaceGlass)
            .padding(Space.lg)
    )
}

/**
 * Empty states, plural.
 *
 * "No devices" after a scan, "no matches" after a filter and "nothing yet"
 * before the first scan are three different situations with three different
 * correct next actions. Showing one generic message for all three leaves the
 * user with no idea what to do.
 */
@Composable
private fun EmptyState(
    isScanning: Boolean,
    isFiltered: Boolean,
    hasAnyDevices: Boolean,
    onClearFilters: () -> Unit,
    onScan: () -> Unit
) {
    val colors = ExtendedTheme.colors

    val (icon, title, body, actionLabel, action) = when {
        isScanning -> Quintuple(
            Icons.Rounded.Radar,
            "Looking around",
            "Devices appear here as they answer.",
            null, null
        )
        isFiltered && hasAnyDevices -> Quintuple(
            Icons.Rounded.SearchOff,
            "No matches",
            "Nothing here fits those filters.",
            "Clear filters", onClearFilters
        )
        hasAnyDevices -> Quintuple(
            Icons.Rounded.Tune,
            "Nothing to show",
            "Try scanning again.",
            "Scan now", onScan
        )
        else -> Quintuple(
            Icons.Rounded.Radar,
            "Nothing scanned yet",
            "Run a scan to see what's on this network and what's broadcasting nearby.",
            "Scan now", onScan
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.x4l),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.surfaceGlass),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(Space.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(Space.sm))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
        if (actionLabel != null && action != null) {
            Spacer(Modifier.height(Space.xl))
            Surface(
                color = colors.surfaceGlassHighlight,
                shape = Radius.full,
                modifier = Modifier.pressable(onClick = action, pressScale = 0.95f)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.md)
                )
            }
        }
    }
}

private data class Quintuple(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val actionLabel: String?,
    val action: (() -> Unit)?
)

/**
 * `collectAsState` with the lifecycle-aware collector.
 *
 * Aliased so every collection point in this screen is consistent and none of
 * them keep collecting while the screen is in the background.
 */
@Composable
private fun <T> StateFlow<T>.collectAsStateSafe(): State<T> = collectAsStateWithLifecycle()
