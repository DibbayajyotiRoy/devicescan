package com.devicelens.app.ui.locate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.ui.components.SignalCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocateModeSheet(
    onDismiss: () -> Unit,
    viewModel: LocateViewModel = hiltViewModel()
) {
    val device by viewModel.device.collectAsState()
    val feedbackText by viewModel.feedbackText.collectAsState()
    val trend by viewModel.trend.collectAsState()
    val cameraAvailable by viewModel.cameraAvailable.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.stopTracking() }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.stopTracking()
            onDismiss()
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = device?.deviceName ?: "Unknown Device",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Locating…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Signal circle
            SignalCircle(trend = trend)

            Spacer(modifier = Modifier.height(24.dp))

            // Feedback text
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!cameraAvailable) {
                Text(
                    text = "Camera unavailable — tracking by signal only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stop button
            OutlinedButton(
                onClick = {
                    viewModel.stopTracking()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text("Stop locating", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
