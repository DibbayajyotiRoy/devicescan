package com.devicelens.app.ui.status

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.domain.model.OverallStatus
import com.devicelens.app.ui.components.*
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.ShapeLarge
import com.devicelens.app.ui.theme.ShapeMedium
import com.devicelens.app.ui.theme.EaseOutCubic

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val userAvatar = viewModel.getUserAvatar()
                        
                        if (userAvatar != null) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            }
                        }

                        IconButton(onClick = onOpenDebugLog) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Help,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

            if (!locationEnabled || !bluetoothEnabled) {
                item {
                    Surface(
                        color = Color(0xFFC0392B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Scanning limited",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = buildString {
                                        if (!locationEnabled) append("Location Services are off. ")
                                        if (!bluetoothEnabled) append("Bluetooth is off. ")
                                        append("These are required to detect nearby devices.")
                                    },
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                StatusIndicator(
                    status = overallStatus,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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

            if (isScanning) {
                item {
                    ScanProgressCard(
                        phase = scanPhase,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (overallStatus == OverallStatus.RISK) {
                item {
                    val topSuspicious = viewModel.getTopSuspiciousDevice()
                    if (topSuspicious != null) {
                        RiskAlertCard(
                            onClick = { onDeviceClick(topSuspicious.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (!isScanning) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Known",
                            count = safeCount,
                            accentColor = ExtendedTheme.colors.statusSafe,
                            glowColor = ExtendedTheme.colors.statusSafeGlow,
                            icon = Icons.Rounded.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Unknown",
                            count = unknownCount,
                            accentColor = ExtendedTheme.colors.statusWarning,
                            glowColor = ExtendedTheme.colors.statusWarningGlow,
                            icon = Icons.AutoMirrored.Rounded.Help,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Suspicious",
                            count = suspiciousCount,
                            accentColor = ExtendedTheme.colors.statusRisk,
                            glowColor = ExtendedTheme.colors.statusRiskGlow,
                            icon = Icons.Rounded.Warning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (devices.isEmpty() && !isScanning) {
                item {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = EaseOutCubic),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.scale(pulseScale)
                        ) {
                            Box(
                                modifier = Modifier.padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.WifiFind,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Ready to scan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the Scan button below to detect\ndevices on your network.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Tap to start scanning",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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
private fun ScanProgressCard(
    phase: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val primaryColor = ExtendedTheme.colors.securityTeal

    // Glass-morphic container with mesh gradient hint
    Box(
        modifier = modifier
            .clip(ShapeLarge)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.08f),
                        primaryColor.copy(alpha = 0.02f)
                    )
                )
            )
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(23.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animated scanning indicator
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glow ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = ExtendedTheme.colors.securityTealGlow.copy(alpha = glowAlpha),
                                    shape = CircleShape
                                )
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 3.dp,
                            color = primaryColor
                        )
                        Icon(
                            Icons.Rounded.WifiFind,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Scanning your network…",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phase.ifEmpty { "Initializing…" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExtendedTheme.colors.textTertiary
                        )
                    }
                }

                // Progress steps visualization with glass-morphic pills
                Spacer(modifier = Modifier.height(20.dp))
                val steps = listOf("Wi-Fi", "Bluetooth", "Analysis")
                val currentStep = when {
                    "Wi-Fi" in phase || "Discovering" in phase -> 0
                    "Bluetooth" in phase || "BLE" in phase -> 1
                    else -> 2
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, stepName ->
                        StepIndicatorPill(
                            label = stepName,
                            isActive = index == currentStep,
                            isCompleted = index < currentStep,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicatorPill(
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCompleted -> ExtendedTheme.colors.statusSafe.copy(alpha = 0.12f)
        isActive -> ExtendedTheme.colors.securityTealGlow.copy(alpha = 0.3f)
        else -> ExtendedTheme.colors.surfaceGlassBorder.copy(alpha = 0.3f)
    }

    val textColor = when {
        isCompleted -> ExtendedTheme.colors.statusSafe
        isActive -> ExtendedTheme.colors.securityTeal
        else -> ExtendedTheme.colors.textTertiary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}


@Composable
private fun StatCard(
    label: String,
    count: Int,
    accentColor: Color,
    glowColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "count_$label"
    )

    // Double-bezel glass container
    Box(
        modifier = modifier
            .clip(ShapeMedium)
            .background(ExtendedTheme.colors.hairlineBorder)
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(19.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon with accent background
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(glowColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Count
                Text(
                    text = animatedCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ExtendedTheme.colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun RiskAlertCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val riskColor = ExtendedTheme.colors.statusRisk
    val riskGlow = ExtendedTheme.colors.statusRiskGlow

    // Pulsing animation for urgency
    val pulseTransition = rememberInfiniteTransition(label = "riskPulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "riskGlow"
    )

    Box(
        modifier = modifier
            .clip(ShapeLarge)
            .background(riskColor.copy(alpha = 0.15f))
            .padding(1.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(23.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            riskGlow.copy(alpha = glowAlpha),
                            riskColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Alert icon with pulsing glow
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(riskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Security Alert",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                    Text(
                        text = "Suspicious device detected · Tap to review",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExtendedTheme.colors.textTertiary
                    )
                }

                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = riskColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
