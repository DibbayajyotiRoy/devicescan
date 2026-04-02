package com.devicelens.app.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.ui.components.DeviceTypeIcon
import com.devicelens.app.ui.theme.RiskRed
import com.devicelens.app.ui.theme.SafeGreen
import com.devicelens.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    onBack: () -> Unit,
    onLocate: (Long) -> Unit,
    viewModel: DeviceDetailsViewModel = hiltViewModel()
) {
    val device by viewModel.device.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val madeBy by viewModel.madeBy.collectAsState()
    val firstSeen by viewModel.firstSeenRelative.collectAsState()
    val lastSeen by viewModel.lastSeenRelative.collectAsState()
    val detectionLabel by viewModel.detectionLabel.collectAsState()
    val riskExplanation by viewModel.riskExplanation.collectAsState()
    val canLocate by viewModel.canLocate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Header: Icon + Name + Risk badge
            device?.let { dev ->
                val (badgeColor, badgeText) = when (dev.riskLevel) {
                    "SAFE" -> SafeGreen to "Safe"
                    "SUSPICIOUS" -> RiskRed to "Suspicious"
                    else -> WarningAmber to "Unknown"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DeviceTypeIcon(
                        deviceName = dev.deviceName,
                        vendor = dev.vendor,
                        modifier = Modifier.size(52.dp)
                    )
                    Column {
                        Text(
                            text = deviceName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Risk explanation
            Text(
                text = riskExplanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Detail card — grouped key-value rows
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (madeBy.isNotEmpty()) {
                        DetailRow(label = "Manufacturer", value = madeBy.removePrefix("Made by: ").removePrefix("Vendor: "))
                    }
                    if (firstSeen.isNotEmpty()) {
                        DetailRow(label = "First seen", value = firstSeen.removePrefix("First seen: "))
                    }
                    if (lastSeen.isNotEmpty()) {
                        DetailRow(label = "Last seen", value = lastSeen.removePrefix("Last seen: "))
                    }
                    if (detectionLabel.isNotEmpty()) {
                        DetailRow(label = "Detection", value = detectionLabel.removePrefix("Detected via: "))
                    }
                    device?.let { dev ->
                        if (!dev.ipAddress.isNullOrBlank()) {
                            DetailRow(label = "IP address", value = dev.ipAddress.orEmpty())
                        }
                        if (!dev.macAddress.isNullOrBlank()) {
                            DetailRow(label = "MAC address", value = dev.macAddress.orEmpty())
                        }
                        if (dev.openPorts.isNotBlank()) {
                            DetailRow(label = "Open ports", value = dev.openPorts)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions
            Button(
                onClick = viewModel::markAsMine,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text("This is my device", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = viewModel::dismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text("Dismiss", style = MaterialTheme.typography.labelLarge)
            }

            if (canLocate) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { device?.let { onLocate(it.id) } },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        "Try to locate this device",
                        style = MaterialTheme.typography.labelLarge,
                        color = RiskRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
