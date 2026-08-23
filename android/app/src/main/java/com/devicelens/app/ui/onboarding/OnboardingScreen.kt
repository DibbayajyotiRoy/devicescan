package com.devicelens.app.ui.onboarding

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.helpers.AppPreferences
import com.devicelens.app.ui.components.pagerParallax
import com.devicelens.app.ui.components.pressable
import com.devicelens.app.ui.components.rememberHaptics
import com.devicelens.app.ui.theme.ExtendedTheme
import com.devicelens.app.ui.theme.LocalWindowMetrics
import com.devicelens.app.ui.theme.MonoType
import com.devicelens.app.ui.theme.Motion
import com.devicelens.app.ui.theme.Radius
import com.devicelens.app.ui.theme.Space
import kotlinx.coroutines.launch

/**
 * The first thing anyone sees.
 *
 * Two structural decisions define it.
 *
 * **There is no account gate.** Onboarding used to end on a mandatory Google
 * sign-in, which contradicted the app's own privacy claim and blocked a scanner
 * that needs no server to work. Sign-in now lives in Settings, as an opt-in for
 * cloud threat intelligence.
 *
 * **Permissions are asked here, with reasons, not on cold start.** The app used
 * to fire the system location dialog during the first frame. Asking a stranger
 * for location access before saying hello earns a denial — and a denial that
 * Android will not let you ask about again.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val metrics = LocalWindowMetrics.current
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val totalPages = onboardingSlides.size + 1
    val permissionPage = totalPages - 1
    val pagerState = rememberPagerState(pageCount = { totalPages })

    var permissionsHandled by remember { mutableStateOf(false) }

    // A tick as each page settles. Discrete steps are exactly what a tick is
    // for, and it makes the pager feel mechanical in the good sense.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { haptics.tick() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsHandled = true
        viewModel.onPermissionResult(result)
        if (result.values.any { it }) haptics.success() else haptics.reject()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExtendedTheme.colors.canvas)
    ) {
        // A single static radial lift behind the content. Not animated: a
        // permanently-drifting background is a battery cost the user paid for
        // an effect they stopped noticing after four seconds.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ExtendedTheme.colors.securityTeal.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 1400f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            OnboardingTopBar(
                currentPage = pagerState.currentPage,
                totalPages = totalPages,
                onBack = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                },
                onSkip = {
                    scope.launch { pagerState.animateScrollToPage(permissionPage) }
                },
                modifier = Modifier.padding(
                    horizontal = metrics.horizontalPadding,
                    vertical = Space.md
                )
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                // Neighbouring pages are pre-composed so a swipe reveals finished
                // content rather than a blank panel that pops in.
                beyondBoundsPageCount = 1
            ) { page ->
                val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                if (page < onboardingSlides.size) {
                    ValueSlideContent(
                        slide = onboardingSlides[page],
                        variant = heroVariants[page],
                        isActive = pagerState.currentPage == page,
                        pageOffset = offset
                    )
                } else {
                    PermissionStepContent(
                        pageOffset = offset,
                        grantedStates = viewModel.permissionStates(context),
                        hasAsked = permissionsHandled
                    )
                }
            }

            OnboardingBottomBar(
                isPermissionPage = pagerState.currentPage == permissionPage,
                allEssentialGranted = viewModel.essentialPermissionsGranted(context),
                onNext = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                onRequestPermissions = {
                    val toRequest = permissionAsks()
                        .flatMap { it.permissions }
                        .filter {
                            ContextCompat.checkSelfPermission(context, it) !=
                                PackageManager.PERMISSION_GRANTED
                        }
                    if (toRequest.isEmpty()) {
                        haptics.success()
                        onComplete()
                    } else {
                        permissionLauncher.launch(toRequest.toTypedArray())
                    }
                },
                onFinish = {
                    haptics.commit()
                    onComplete()
                },
                modifier = Modifier.padding(
                    horizontal = metrics.horizontalPadding,
                    vertical = Space.xl
                )
            )
        }
    }
}

private val heroVariants = listOf(
    HeroVariant.FIELD,
    HeroVariant.LENS,
    HeroVariant.TRAIL,
    HeroVariant.ENCLOSURE
)

// ─────────────────────────────────────────────────────────────────────
// Chrome
// ─────────────────────────────────────────────────────────────────────

/**
 * Back, progress, skip.
 *
 * The progress bar is segmented rather than a single sliding fill: a person can
 * count remaining steps at a glance, which is the actual question they have
 * during onboarding.
 */
@Composable
private fun OnboardingTopBar(
    currentPage: Int,
    totalPages: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val backVisible = currentPage > 0
        val backAlpha by animateFloatAsState(
            targetValue = if (backVisible) 1f else 0f,
            animationSpec = Motion.standard(),
            label = "backAlpha"
        )

        IconButton(
            onClick = onBack,
            enabled = backVisible,
            modifier = Modifier.graphicsLayer { alpha = backAlpha }
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Previous step",
                tint = ExtendedTheme.colors.textSecondary
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Space.md),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { index ->
                val isDone = index < currentPage
                val isCurrent = index == currentPage

                val weight by animateFloatAsState(
                    targetValue = if (isCurrent) 2.4f else 1f,
                    animationSpec = Motion.smooth(),
                    label = "segmentWeight"
                )
                val color = when {
                    isCurrent -> ExtendedTheme.colors.securityTeal
                    isDone -> ExtendedTheme.colors.securityTeal.copy(alpha = 0.45f)
                    else -> ExtendedTheme.colors.hairlineBorderStrong
                }

                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(3.dp)
                        .clip(Radius.full)
                        .background(color)
                )
            }
        }

        // The skip target is the permission step, never the end of the flow:
        // skipping the pitch is reasonable, skipping the thing that makes the
        // app function is not.
        val showSkip = currentPage < totalPages - 1
        TextButton(onClick = onSkip, enabled = showSkip) {
            Text(
                text = if (showSkip) "Skip" else "",
                style = MaterialTheme.typography.labelLarge,
                color = ExtendedTheme.colors.textTertiary
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    isPermissionPage: Boolean,
    allEssentialGranted: Boolean,
    onNext: () -> Unit,
    onRequestPermissions: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = LocalWindowMetrics.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val label = when {
            !isPermissionPage -> "Continue"
            allEssentialGranted -> "Start scanning"
            else -> "Allow access"
        }

        PrimaryAction(
            label = label,
            icon = if (isPermissionPage && allEssentialGranted) Icons.Rounded.Check
            else Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = {
                when {
                    !isPermissionPage -> onNext()
                    allEssentialGranted -> onFinish()
                    else -> onRequestPermissions()
                }
            },
            modifier = Modifier.widthIn(max = if (metrics.isCompact) Dp.Unspecified else 420.dp)
        )

        // An escape hatch, not a dead end. Someone who will not grant Bluetooth
        // can still use the Wi-Fi half of the app, and telling them so is more
        // honest than a button that silently does nothing.
        if (isPermissionPage && !allEssentialGranted) {
            Spacer(Modifier.height(Space.sm))
            TextButton(onClick = onFinish) {
                Text(
                    text = "Continue without these",
                    style = MaterialTheme.typography.labelLarge,
                    color = ExtendedTheme.colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun PrimaryAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = Radius.lg,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .pressable(onClick = onClick, pressScale = 0.985f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.xxl, vertical = Space.lg),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The label crossfades rather than hard-cutting, so "Allow access"
            // becoming "Start scanning" reads as the same button changing state.
            AnimatedContent(
                targetState = label,
                transitionSpec = { fadeIn(Motion.fade()) togetherWith fadeOut(Motion.fade(120)) },
                label = "actionLabel"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.width(Space.sm))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Pages
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun ValueSlideContent(
    slide: ValueSlide,
    variant: HeroVariant,
    isActive: Boolean,
    pageOffset: Float
) {
    val metrics = LocalWindowMetrics.current

    // On a tall phone the illustration sits above the words; on a landscape
    // phone or a tablet it sits beside them. Same content, laid out for the
    // space that actually exists rather than scaled to fit.
    if (metrics.onboardingIsHorizontal) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = metrics.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.x4l)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .pagerParallax(pageOffset, driftDp = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                OnboardingHero(variant = variant, isActive = isActive, size = metrics.heroSize)
            }
            Box(modifier = Modifier.weight(1f)) {
                SlideCopy(slide = slide, pageOffset = pageOffset, centered = false)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = metrics.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.pagerParallax(pageOffset, driftDp = 48.dp)) {
                OnboardingHero(variant = variant, isActive = isActive, size = metrics.heroSize)
            }
            Spacer(Modifier.height(metrics.sectionSpacing))
            SlideCopy(slide = slide, pageOffset = pageOffset, centered = true)
        }
    }
}

/**
 * The words move less than the illustration does.
 *
 * Differential travel is what creates depth — text drifting at the same rate as
 * the art would read as one flat card sliding, which is the default pager
 * behaviour and looks it.
 */
@Composable
private fun SlideCopy(
    slide: ValueSlide,
    pageOffset: Float,
    centered: Boolean
) {
    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        modifier = Modifier.pagerParallax(pageOffset, driftDp = 20.dp, scaleFalloff = 0f)
    ) {
        Text(
            text = slide.eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ExtendedTheme.colors.securityTeal
        )
        Spacer(Modifier.height(Space.md))
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineLarge,
            color = ExtendedTheme.colors.textPrimary,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )
        Spacer(Modifier.height(Space.lg))
        Text(
            text = slide.body,
            style = MaterialTheme.typography.bodyLarge,
            color = ExtendedTheme.colors.textSecondary,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )
        Spacer(Modifier.height(Space.xl))
        // The protocol list in monospace: a small, concrete proof that there is
        // real engineering under the claim above it.
        Text(
            text = slide.proof,
            style = MonoType.small,
            color = ExtendedTheme.colors.textQuaternary
        )
    }
}

/**
 * The permission step.
 *
 * Each permission gets its own reason, and the reason is the honest one. The
 * location entry says outright that Android forces the requirement and that we
 * never read position — because a privacy app asking for location without
 * explaining itself deserves the suspicion it gets.
 */
@Composable
private fun PermissionStepContent(
    pageOffset: Float,
    grantedStates: Map<String, Boolean>,
    hasAsked: Boolean
) {
    val metrics = LocalWindowMetrics.current
    val asks = remember { permissionAsks() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = metrics.horizontalPadding)
            .pagerParallax(pageOffset, driftDp = 24.dp, scaleFalloff = 0.04f),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(Space.xl))
        Text(
            text = "BEFORE WE START",
            style = MaterialTheme.typography.labelSmall,
            color = ExtendedTheme.colors.securityTeal
        )
        Spacer(Modifier.height(Space.md))
        Text(
            text = "What we need,\nand why",
            style = MaterialTheme.typography.headlineLarge,
            color = ExtendedTheme.colors.textPrimary
        )
        Spacer(Modifier.height(metrics.sectionSpacing))

        asks.forEachIndexed { index, ask ->
            val granted = ask.permissions.isNotEmpty() &&
                ask.permissions.all { grantedStates[it] == true }

            PermissionRow(
                ask = ask,
                granted = granted,
                denied = hasAsked && !granted
            )
            if (index < asks.lastIndex) Spacer(Modifier.height(Space.md))
        }

        Spacer(Modifier.height(Space.xl))
    }
}

@Composable
private fun PermissionRow(
    ask: PermissionAsk,
    granted: Boolean,
    denied: Boolean
) {
    val accent = when {
        granted -> ExtendedTheme.colors.statusSafe
        denied && ask.required -> ExtendedTheme.colors.statusWarning
        else -> ExtendedTheme.colors.textTertiary
    }

    // Border and icon tint animate as the permission is granted, so the result
    // of the system dialog is visible on the page the user returns to.
    val borderAlpha by animateFloatAsState(
        targetValue = if (granted) 1f else 0.35f,
        animationSpec = Motion.standard(),
        label = "permissionBorder"
    )

    Surface(
        color = ExtendedTheme.colors.surfaceGlass,
        shape = Radius.md,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = 1f }
    ) {
        Row(
            modifier = Modifier.padding(Space.lg),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(Radius.sm)
                    .background(accent.copy(alpha = 0.12f * borderAlpha + 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (granted) Icons.Rounded.Check else ask.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(Space.lg))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ask.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ExtendedTheme.colors.textPrimary
                    )
                    if (!ask.required) {
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            text = "OPTIONAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExtendedTheme.colors.textQuaternary
                        )
                    }
                }
                Spacer(Modifier.height(Space.xs))
                Text(
                    text = ask.why,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ExtendedTheme.colors.textSecondary
                )
                if (denied && ask.required) {
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        text = "Not granted — some scanning will be unavailable. " +
                            "You can change this in Android Settings at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExtendedTheme.colors.statusWarning
                    )
                }
            }
        }
    }
}
