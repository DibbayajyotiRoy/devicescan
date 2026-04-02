package com.devicelens.app.ui.status

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.components.*
import com.devicelens.app.ui.theme.RiskRed
import com.devicelens.app.ui.theme.SafeGreen
import com.devicelens.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    onDeviceClick: (Long) -> Unit,
    onNavigateToSetup: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDebugLog: () -> Unit,
    viewModel: StatusViewModel = hiltViewModel()
) {
    val overallStatus by viewModel.overallStatus.collectAsState()
    val safeCount by viewModel.safeCount.collectAsState()
    val unknownCount by viewModel.unknownCount.collectAsState()
    val suspiciousCount by viewModel.suspiciousCount.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanPhase by viewModel.scanPhase.collectAsState()
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val shouldShowNudge by viewModel.shouldShowNudge.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToSetup.collect {
            onNavigateToSetup()
        }
    }

    if (shouldShowNudge) {
        LimitationNudge(onDismiss = viewModel::onNudgeDismissed)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Device Lens",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(onClick = onOpenDebugLog) {
                        Icon(
                            Icons.Rounded.BugReport,
                            contentDescription = "Debug Logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.restartScan() },
                icon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                text = { Text("Scan environment") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                expanded = !isScanning,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Diagnostic Warnings — using icons, not emoji
            if (!locationEnabled || !bluetoothEnabled) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Scanning limited",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = buildString {
                                        if (!locationEnabled) append("Location Services are off. ")
                                        if (!bluetoothEnabled) append("Bluetooth is off. ")
                                        append("These are required to detect nearby devices.")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Status indicator
            item {
                StatusIndicator(
                    status = overallStatus,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Location context — icon replaces emoji
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Network: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val ssid = viewModel.getCurrentSsid()
                        Text(
                            text = ssid,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Scan phase
            if (isScanning && scanPhase.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = scanPhase,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Risk CTA
            if (overallStatus == OverallStatus.RISK) {
                item {
                    val topSuspicious = viewModel.getTopSuspiciousDevice()
                    if (topSuspicious != null) {
                        Card(
                            onClick = { onDeviceClick(topSuspicious.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = RiskRed.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = RiskRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "See what was found",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RiskRed,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = RiskRed.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Count row — stat cards with colored leading accents
            if (!isScanning) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "Known",
                            count = safeCount,
                            accentColor = SafeGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Unknown",
                            count = unknownCount,
                            accentColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Suspicious",
                            count = suspiciousCount,
                            accentColor = RiskRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Device list
            if (devices.isEmpty() && !isScanning) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.WifiFind,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No devices detected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Make sure Wi-Fi and Bluetooth are enabled,\nthen tap Scan environment.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Wi-Fi devices shown by IP. Bluetooth devices show signal distance.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                itemsIndexed(devices, key = { _, it -> it.id }) { index, device ->
                    // Staggered entry animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index.toLong() * 40)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300, easing = EaseOutCubic)
                        )
                    ) {
                        DeviceRow(
                            device = device,
                            onClick = { onDeviceClick(device.id) }
                        )
                    }
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "count_$label"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(accentColor)
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = animatedCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
