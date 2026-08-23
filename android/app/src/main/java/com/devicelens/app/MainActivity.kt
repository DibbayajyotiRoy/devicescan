package com.devicelens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devicelens.app.helpers.AppPreferences
import com.devicelens.app.ui.navigation.DeviceLensNavHost
import com.devicelens.app.ui.theme.DeviceLensTheme
import com.devicelens.app.ui.theme.LocalWindowMetrics
import com.devicelens.app.ui.theme.WindowMetrics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Permissions are deliberately NOT requested here.
        //
        // The previous build threw the system location and Bluetooth dialogs at
        // people during the first frame, before they had read a single word about
        // what the app does. For an app whose entire pitch is "we protect your
        // privacy", opening with an unexplained demand for location access is the
        // worst possible first impression — and the denial it earns is permanent.
        //
        // Onboarding now explains what each permission is for, in context, and
        // asks only once the user knows why. See PermissionStep in OnboardingScreen.

        setContent {
            DeviceLensTheme {
                // Measured rather than assumed, so a folded foldable, a split-screen
                // window and a small phone all get the layout that fits, instead of
                // being classified by device type.
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val metrics = WindowMetrics(
                        widthDp = maxWidth.value.toInt(),
                        heightDp = maxHeight.value.toInt()
                    )

                    CompositionLocalProvider(LocalWindowMetrics provides metrics) {
                        var onboardingComplete by remember {
                            mutableStateOf(appPreferences.onboardingComplete)
                        }

                        DeviceLensNavHost(
                            onboardingComplete = onboardingComplete,
                            onOnboardingFinished = {
                                appPreferences.onboardingComplete = true
                                onboardingComplete = true
                            }
                        )
                    }
                }
            }
        }
    }
}
