package com.devicelens.app.ui.status

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.components.*
import com.devicelens.app.ui.theme.RiskRed

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
                        "DEVICE LENS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    IconButton(onClick = onOpenDebugLog) {
                        Icon(Icons.Rounded.BugReport, contentDescription = "Debug Logs", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
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
                text = { Text("Scan Environment") },
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
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Diagnostic Warnings
            if (!locationEnabled || !bluetoothEnabled) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ Scanning Limited",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = buildString {
                                    if (!locationEnabled) append("• Location Services are OFF. ")
                                    if (!bluetoothEnabled) append("• Bluetooth is OFF. ")
                                    append("\nThese are required to detect nearby devices.")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
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

            // Location Context
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 Network Location: ",
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
                    Text(
                        text = scanPhase,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
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
                                containerColor = RiskRed.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "See what was found →",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RiskRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Count row
            if (!isScanning) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CountLabel("Known", safeCount)
                        CountLabel("Unknown", unknownCount)
                        CountLabel("Suspicious", suspiciousCount)
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }

            // Device list
            if (devices.isEmpty() && !isScanning) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No devices found yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ensure Wi-Fi/Bluetooth are ON.\nSome devices may hide their signatures.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                item {
                   Text(
                       text = "💡 WiFi devices are shown by IP. Bluetooth devices show signal distance.",
                       style = MaterialTheme.typography.labelSmall,
                       color = MaterialTheme.colorScheme.secondary,
                       modifier = Modifier.padding(vertical = 8.dp)
                   )
                }
                items(devices, key = { it.id }) { device ->
                    DeviceRow(
                        device = device,
                        onClick = { onDeviceClick(device.id) }
                    )
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun CountLabel(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
