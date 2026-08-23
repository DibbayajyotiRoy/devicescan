package com.devicelens.app.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devicelens.app.data.db.DeviceEntity
import com.devicelens.app.ui.components.DeviceKind
import com.devicelens.app.ui.components.SkeletonBlock
import com.devicelens.app.ui.components.enterStaggered
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.components.rememberHaptics
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.LocalWindowMetrics
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import com.devicelens.app.ui.theme.Tint

/**
 * "Which of these are yours?"
 *
 * This is the step that makes every later scan useful: once the user's own
 * devices are known, anything new genuinely stands out. Getting people through
 * it matters, so it is built to be finishable in one pass — tap the ones you
 * recognise, done.
 *
 * Three problems with the previous version are fixed here:
 *
 *  - It rendered an empty list with a "Retry Scan" button when the user had
 *    simply arrived before any scan had run, which reads as a failure that the
 *    user caused. It now starts a scan itself and says what it is doing.
 *  - Selection used a Material `Switch` per row. A switch means "turn this
 *    setting on"; this is a multi-select list, and it should look like one.
 *  - The primary button said "Done" regardless of state. It now says what will
 *    actually happen, and counts.
 */
@Composable
fun SetupScreen(
    onComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val metrics = LocalWindowMetrics.current
    val haptics = rememberHaptics()
    val colors = ExtendedTheme.colors

    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val trustedKeys by viewModel.trustedKeys.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setupComplete.collect { onComplete() }
    }

    // Arriving with nothing to show is not an error state — it just means no
    // scan has run yet. So run one, rather than blaming the user for it.
    LaunchedEffect(Unit) { viewModel.scanIfEmpty() }

    val listState = rememberLazyListState()
    val selectedCount = trustedKeys.size

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
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + Space.x3l,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 156.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Space.lg)
        ) {
            item(key = "header") {
                Column(modifier = Modifier.widthIn(max = 560.dp)) {
                    Text(
                        text = "STEP 1 OF 1",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.securityTeal
                    )
                    Spacer(Modifier.height(Space.md))
                    Text(
                        text = "Which of these\nare yours?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(Space.md))
                    Text(
                        text = "Everything you mark is treated as trusted and stays quiet. " +
                            "Anything you don't will be flagged if it behaves oddly — and " +
                            "you can change any of this later.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            if (devices.isNotEmpty()) {
                item(key = "select-all") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${devices.size} FOUND",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                        val allSelected = selectedCount == devices.size
                        Text(
                            text = if (allSelected) "Clear all" else "Select all",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.securityTeal,
                            modifier = Modifier
                                .clip(Radius.xs)
                                .pressable(
                                    onClick = {
                                        haptics.select(!allSelected)
                                        if (allSelected) viewModel.clearAll() else viewModel.trustAll()
                                    }
                                )
                                .padding(horizontal = Space.sm, vertical = Space.xs)
                        )
                    }
                }
            }

            when {
                devices.isEmpty() && isScanning -> {
                    item(key = "scanning") { ScanningPlaceholder() }
                }

                devices.isEmpty() -> {
                    item(key = "empty") {
                        EmptySetupState(onScan = { haptics.commit(); viewModel.startScan() })
                    }
                }

                else -> {
                    itemsIndexed(devices, key = { _, device -> device.id }) { index, device ->
                        SelectableDeviceRow(
                            device = device,
                            selected = trustedKeys.contains(device.compositeKey),
                            isFirst = index == 0,
                            isLast = index == devices.lastIndex,
                            onToggle = {
                                haptics.select(!trustedKeys.contains(device.compositeKey))
                                viewModel.toggle(device.compositeKey)
                            },
                            modifier = if (index < 10) Modifier.enterStaggered(index) else Modifier
                        )
                    }
                }
            }
        }

        // A pinned footer, because the commit action must never be something the
        // user has to scroll past two hundred devices to reach.
        SetupFooter(
            selectedCount = selectedCount,
            totalCount = devices.size,
            onFinish = {
                haptics.commit()
                viewModel.complete()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = metrics.horizontalPadding,
                    vertical = Space.xl
                )
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────

/**
 * A selectable row.
 *
 * The whole row is the target rather than a small control on the end of it —
 * this is a list people tap through quickly, and a 24 dp switch is a fiddly
 * thing to hit repeatedly.
 */
@Composable
private fun SelectableDeviceRow(
    device: DeviceEntity,
    selected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors
    val kind = DeviceKind.resolve(device.deviceType, device.deviceName, device.vendor)

    val selection by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = Motion.standard(),
        label = "selection"
    )

    val background = lerp(
        colors.surfaceGlass,
        colors.statusSafe.copy(alpha = Tint.faint),
        selection
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(groupShape(isFirst, isLast))
            .background(background)
            .pressable(onClick = onToggle, pressScale = 0.985f, hapticOnPress = false)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.surfaceGlassHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = kind.icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

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
                val address = device.ipAddress ?: device.macAddress
                if (!address.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = address,
                        style = MonoType.small,
                        color = colors.textQuaternary,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(Space.md))

            SelectionMark(progress = selection)
        }

        if (!isLast) {
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

/**
 * The check.
 *
 * The tick scales in from nothing as the row is selected, so a fast tap through
 * a long list still produces a visible confirmation for each one.
 */
@Composable
private fun SelectionMark(progress: Float) {
    val colors = ExtendedTheme.colors
    val fill = lerp(Color.Transparent, colors.statusSafe, progress)
    val border = lerp(colors.hairlineBorderStrong, colors.statusSafe, progress)

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(fill)
            .border(1.5.dp, border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .size(14.dp)
                .graphicsLayer {
                    alpha = progress
                    scaleX = progress
                    scaleY = progress
                }
        )
    }
}

@Composable
private fun SetupFooter(
    selectedCount: Int,
    totalCount: Int,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ExtendedTheme.colors

    // The label states the actual outcome, including the honest one for zero:
    // skipping is a legitimate choice and should not be dressed up as "Done".
    val label = when {
        selectedCount == 0 -> "Skip for now"
        selectedCount == totalCount && totalCount > 0 -> "Trust all $totalCount"
        else -> "Trust $selectedCount ${if (selectedCount == 1) "device" else "devices"}"
    }

    Surface(
        color = if (selectedCount == 0) colors.surfaceGlassHighlight else MaterialTheme.colorScheme.primary,
        shape = Radius.lg,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .pressable(onClick = onFinish, pressScale = 0.985f)
    ) {
        Box(
            modifier = Modifier.padding(vertical = Space.lg),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = label,
                transitionSpec = { fadeIn(Motion.fade()) togetherWith fadeOut(Motion.fade(120)) },
                label = "footerLabel"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selectedCount == 0) colors.textSecondary
                    else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Skeleton rows while the first scan runs.
 *
 * Placeholders shaped like the content that is coming, rather than a spinner:
 * the user can see how the list will be laid out before it arrives, and the
 * wait feels like loading rather than like nothing happening.
 */
@Composable
private fun ScanningPlaceholder() {
    val colors = ExtendedTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(colors.surfaceGlass)
            .padding(Space.lg),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Radar,
                contentDescription = null,
                tint = colors.securityTeal,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(Space.sm))
            Text(
                text = "Looking for devices…",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        repeat(4) { index ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(
                    modifier = Modifier.size(38.dp),
                    height = 38.dp,
                    cornerRadius = 11.dp
                )
                Spacer(Modifier.width(Space.md))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.55f - index * 0.06f),
                        height = 12.dp
                    )
                    Spacer(Modifier.height(Space.sm))
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(0.34f),
                        height = 9.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySetupState(onScan: () -> Unit) {
    val colors = ExtendedTheme.colors

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
                Icons.Rounded.Radar,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(Space.lg))
        Text(
            text = "Nothing found yet",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(Space.sm))
        Text(
            text = "Make sure you're connected to Wi-Fi and Bluetooth is on, then scan again.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
        Spacer(Modifier.height(Space.xl))
        Surface(
            color = colors.surfaceGlassHighlight,
            shape = Radius.full,
            modifier = Modifier.pressable(onClick = onScan, pressScale = 0.95f)
        ) {
            Text(
                text = "Scan again",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.md)
            )
        }
    }
}

private fun groupShape(isFirst: Boolean, isLast: Boolean) = when {
    isFirst && isLast -> Radius.md
    isFirst -> RoundedCornerShape(topStart = Radius.mdDp, topEnd = Radius.mdDp)
    isLast -> RoundedCornerShape(bottomStart = Radius.mdDp, bottomEnd = Radius.mdDp)
    else -> RoundedCornerShape(0.dp)
}
