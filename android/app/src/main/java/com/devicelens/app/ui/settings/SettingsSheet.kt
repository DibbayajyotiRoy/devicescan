package com.devicelens.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.components.rememberHaptics
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import com.devicelens.app.ui.theme.Tint

/**
 * Settings.
 *
 * Two changes of substance beyond the visual pass.
 *
 * **Sign-in lives here now**, next to the cloud feature it enables, instead of
 * gating the whole app at first launch. Presented as what it is — an optional
 * upgrade — rather than as a toll gate.
 *
 * **The detection limits are stated permanently.** They used to appear once, in
 * a one-time bottom sheet, right when a new user was least equipped to absorb
 * them and would never see them again. A security tool's limits are exactly the
 * thing a user needs to be able to re-read later.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors = ExtendedTheme.colors
    val haptics = rememberHaptics()

    val bgScanEnabled by viewModel.backgroundScanEnabled.collectAsStateWithLifecycle()
    val cloudEnabled by viewModel.cloudIntelEnabled.collectAsStateWithLifecycle()
    val backendHealthy by viewModel.backendHealthy.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isLoggingIn by viewModel.isLoggingIn.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    var showResetConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.handleGoogleSignInResult(result.data) }

    LaunchedEffect(loginError) {
        loginError?.let {
            haptics.reject()
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearLoginError()
        }
    }

    LaunchedEffect(cloudEnabled) {
        if (cloudEnabled) viewModel.checkBackendHealth()
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            containerColor = colors.surfaceGlassHighlight,
            shape = Radius.lg,
            title = {
                Text(
                    "Erase everything?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    "Every trusted device, every scan result and the tracker history will be " +
                        "deleted from this phone. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    haptics.commit()
                    showResetConfirmation = false
                    onReset()
                    onDismiss()
                }) {
                    Text("Erase", color = colors.statusRisk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = Radius.xlDp, topEnd = Radius.xlDp
        ),
        containerColor = colors.surfaceGlass,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = Space.md)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(Radius.full)
                    .background(colors.hairlineBorderStrong)
            )
        }
    ) {
        SnackbarHost(hostState = snackbarHostState)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl)
                .padding(bottom = Space.x4l)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary
                )
                if (isLoggedIn) {
                    TextButton(onClick = { haptics.tap(); viewModel.logout() }) {
                        Text("Sign out", color = colors.statusRisk)
                    }
                }
            }

            Spacer(Modifier.height(Space.xl))

            SettingsGroup(label = "Scanning") {
                SettingToggle(
                    title = "Background scanning",
                    description = "Check every 30 minutes while the app is closed, and warn you " +
                        "if a tracker follows you. Uses the passive radios only.",
                    checked = bgScanEnabled,
                    onCheckedChange = {
                        haptics.select(it)
                        viewModel.toggleBackgroundScan(it)
                    }
                )
            }

            Spacer(Modifier.height(Space.lg))

            SettingsGroup(label = "Cloud intelligence") {
                SettingToggle(
                    title = "Match against known signatures",
                    description = "Compares device fingerprints against a database of known " +
                        "spy cameras. Everything else works without this.",
                    checked = cloudEnabled,
                    onCheckedChange = {
                        haptics.select(it)
                        viewModel.toggleCloudIntelligence(it)
                    },
                    badge = when {
                        !cloudEnabled -> null
                        backendHealthy == true -> "CONNECTED" to colors.statusSafe
                        backendHealthy == false -> "UNREACHABLE" to colors.statusWarning
                        else -> "CHECKING" to colors.textTertiary
                    }
                )

                AnimatedVisibility(
                    visible = cloudEnabled,
                    enter = fadeIn(Motion.fade()) + expandVertically(),
                    exit = fadeOut(Motion.fade()) + shrinkVertically()
                ) {
                    Column {
                        RowDivider()
                        // Stated precisely, because a vague privacy promise is
                        // worth nothing: name the fields that leave the device.
                        Text(
                            text = "Sent: the first 3 bytes of a MAC address, open port numbers, " +
                                "and HTTP banner text.\nNever sent: full MAC addresses, your IP, " +
                                "your location, your network name, or anything about you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                            modifier = Modifier.padding(Space.lg)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Space.lg))

            AccountCard(
                isLoggedIn = isLoggedIn,
                userName = userName,
                userEmail = userEmail,
                isLoggingIn = isLoggingIn,
                onSignIn = {
                    haptics.tap()
                    signInLauncher.launch(viewModel.getGoogleSignInIntent())
                }
            )

            Spacer(Modifier.height(Space.lg))

            DetectionLimits()

            Spacer(Modifier.height(Space.lg))

            Surface(
                color = colors.statusRisk.copy(alpha = Tint.faint),
                shape = Radius.md,
                modifier = Modifier
                    .fillMaxWidth()
                    .pressable(onClick = { showResetConfirmation = true }, pressScale = 0.99f)
            ) {
                Text(
                    text = "Erase all data",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.statusRisk,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.lg)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    label: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val colors = ExtendedTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(Space.sm))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.md)
                .background(colors.surfaceGlassHighlight)
                .border(1.dp, colors.hairlineBorder, Radius.md),
            content = content
        )
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: Pair<String, Color>? = null
) {
    val colors = ExtendedTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressable(
                onClick = { onCheckedChange(!checked) },
                pressScale = 0.995f,
                hapticOnPress = false
            )
            .padding(Space.lg),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                badge?.let { (text, color) ->
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = text,
                        style = MonoType.small,
                        color = color,
                        modifier = Modifier
                            .clip(Radius.xs)
                            .background(color.copy(alpha = Tint.subtle))
                            .padding(horizontal = Space.sm, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(Space.xs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
        }

        Spacer(Modifier.width(Space.lg))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.surfaceGlass,
                uncheckedBorderColor = colors.hairlineBorderStrong
            )
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ExtendedTheme.colors.hairlineBorder)
    )
}

/**
 * The account card.
 *
 * Framed as optional throughout — the heading says what signing in *adds*,
 * never what not signing in costs you. The scanner is complete without it and
 * the copy should not imply otherwise.
 */
@Composable
private fun AccountCard(
    isLoggedIn: Boolean,
    userName: String?,
    userEmail: String?,
    isLoggingIn: Boolean,
    onSignIn: () -> Unit
) {
    val colors = ExtendedTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ACCOUNT",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        Spacer(Modifier.height(Space.sm))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.md)
                .background(colors.surfaceGlassHighlight)
                .border(1.dp, colors.hairlineBorder, Radius.md)
                .padding(Space.lg)
        ) {
            if (isLoggedIn) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = Tint.medium)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(Space.lg))
                    Column {
                        Text(
                            text = userName ?: "Signed in",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        userEmail?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Sync across devices",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = "Optional. Signing in keeps your trusted devices across phones. " +
                        "Scanning works exactly the same without an account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
                Spacer(Modifier.height(Space.lg))

                Surface(
                    color = colors.surfaceGlass,
                    shape = Radius.sm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.hairlineBorderStrong, Radius.sm)
                        .pressable(onClick = onSignIn, enabled = !isLoggingIn, pressScale = 0.98f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = Space.md),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = colors.securityTeal
                            )
                        } else {
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * What the app cannot do.
 *
 * Permanent and expandable, rather than a one-time popup that appeared before
 * the user had any context for it. Someone deciding whether to trust a clear
 * result needs to know what a clear result does not rule out, and they need to
 * be able to find that out on the day they are worried — not on install day.
 */
@Composable
private fun DetectionLimits() {
    val colors = ExtendedTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(colors.surfaceGlassHighlight)
            .border(1.dp, colors.hairlineBorder, Radius.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressable(onClick = { expanded = !expanded }, pressScale = 0.995f)
                .padding(Space.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(Space.md))
            Text(
                text = "What this can't detect",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(Motion.fade()) + expandVertically(),
            exit = fadeOut(Motion.fade()) + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = Space.lg).padding(bottom = Space.lg)) {
                limitations.forEach { limitation ->
                    Row(
                        modifier = Modifier.padding(vertical = Space.sm),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "—",
                            style = MonoType.small,
                            color = colors.textQuaternary
                        )
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            text = limitation,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

private val limitations = listOf(
    "A device that is switched off, or recording to a local card without transmitting, " +
        "broadcasts nothing — so nothing can find it over the air.",
    "A camera on a different network from yours will not appear in a Wi-Fi scan. Check " +
        "the Bluetooth results, and look physically.",
    "Bluetooth trackers rotate their identity periodically. A tag separated from its " +
        "owner rotates slowly and is detectable; one still with its owner may not be.",
    "Android hides the ARP table from apps, so some devices will show an address but no " +
        "manufacturer.",
    "A clear result means nothing suspicious was broadcasting during the scan. It is " +
        "not a guarantee that a room is clean."
)
