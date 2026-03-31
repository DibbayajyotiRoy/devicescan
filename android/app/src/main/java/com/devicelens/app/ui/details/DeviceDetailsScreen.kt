package com.devicelens.app.ui.details

import androidx.compose.foundation.background
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
                title = { Text(deviceName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                }
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
            Spacer(modifier = Modifier.height(8.dp))

            // Risk badge
            device?.let { dev ->
                val (badgeColor, badgeText) = when (dev.riskLevel) {
                    "SAFE" -> SafeGreen to "Safe"
                    "SUSPICIOUS" -> RiskRed to "Suspicious"
                    else -> WarningAmber to "Unknown"
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Risk explanation
            Text(
                text = riskExplanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Detail rows
            DetailRow(madeBy)
            DetailRow(firstSeen)
            DetailRow(lastSeen)
            DetailRow(detectionLabel)

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(24.dp))

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
private fun DetailRow(text: String) {
    if (text.isNotEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}
