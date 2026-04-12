package com.devicelens.app.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.devicelens.app.ui.theme.ExtendedTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is OnboardingUiState.Error) {
            snackbarHostState.showSnackbar(
                (uiState as OnboardingUiState.Error).message,
                duration = SnackbarDuration.Short
            )
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            viewModel.handleSignInResult(task, onComplete)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Subtle background gradient mesh
        MeshBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = uiState !is OnboardingUiState.Loading
            ) { page ->
                OnboardingPage(
                    page = page,
                    pagerState = pagerState
                )
            }

            // Bottom Actions
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                pageCount = 4,
                isLoading = uiState is OnboardingUiState.Loading,
                onNext = {
                    if (pagerState.currentPage < 3) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        launcher.launch(viewModel.getSignInIntent())
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Loading Overlay
        if (uiState is OnboardingUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(10.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingPage(
    page: Int,
    pagerState: androidx.compose.foundation.pager.PagerState
) {
    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    
    val slide = when (page) {
        0 -> OnboardingSlide(
            title = "Privacy First",
            description = "DeviceLens identifies hidden trackers without ever exposing your personal data to the cloud.",
            icon = Icons.Rounded.VerifiedUser,
            accentColor = ExtendedTheme.colors.securityTeal
        )
        1 -> OnboardingSlide(
            title = "Threat Detection",
            description = "Advanced scanning algorithms detect suspicious behavior from hidden cameras and network spies.",
            icon = Icons.Rounded.Security,
            accentColor = ExtendedTheme.colors.statusRisk
        )
        2 -> OnboardingSlide(
            title = "Deep Intelligence",
            description = "Sync your findings with our cloud threat database for real-time analysis of emerging risks.",
            icon = Icons.Rounded.Insights,
            accentColor = Color(0xFF6366F1) // Indigo
        )
        else -> OnboardingSlide(
            title = "Secure Access",
            description = "Sign in with your Google account to enable 30-day session persistence and cloud device syncing.",
            icon = Icons.Rounded.AccountCircle,
            accentColor = Color(0xFF3B82F6) // Blue
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Parallax Icon Container
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    translationX = pageOffset * 200f
                    alpha = 1f - kotlin.math.abs(pageOffset)
                    scaleX = 1f - (kotlin.math.abs(pageOffset) * 0.2f)
                    scaleY = 1f - (kotlin.math.abs(pageOffset) * 0.2f)
                },
            contentAlignment = Alignment.Center
        ) {
            // Liquid Glass Background for the icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(slide.accentColor.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Icon(
                imageVector = slide.icon,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = slide.accentColor
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Staggered text reveal
        Column(
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * 100f
                alpha = 1f - kotlin.math.abs(pageOffset)
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slide.title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold, // Outfit style
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = slide.description,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    isLoading: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pageCount) { index ->
                val isActive = index == currentPage
                val width by animateDpAsState(if (isActive) 24.dp else 8.dp)
                val alpha by animateFloatAsState(if (isActive) 1f else 0.3f)
                
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Large Premium Button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .scale(if (isLoading) 0.98f else 1f),
            shape = RoundedCornerShape(20.dp),
            colors = if (currentPage == 3) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            },
            enabled = !isLoading
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentPage == 3) {
                    Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sign in with Google", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                } else {
                    Text("Continue", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun MeshBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val color1 = ExtendedTheme.colors.securityTeal.copy(alpha = 0.05f)
    val color2 = Color(0xFF6366F1).copy(alpha = 0.05f)
    
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse)
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(0.5f)) {
        drawCircle(
            color = color1,
            radius = 600f,
            center = androidx.compose.ui.geometry.Offset(offset1, 200f)
        )
        drawCircle(
            color = color2,
            radius = 800f,
            center = androidx.compose.ui.geometry.Offset(size.width - offset1, size.height - 400f)
        )
    }
}

private data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)
