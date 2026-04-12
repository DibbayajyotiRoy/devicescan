package com.devicelens.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.domain.auth.GoogleAuthManager
import com.devicelens.app.worker.BackgroundScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendClient: BackendClient,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    companion object {
        private const val BG_SCAN_WORK_NAME = "device_lens_background_scan"
    }

    private val _backgroundScanEnabled = MutableStateFlow(false)
    val backgroundScanEnabled: StateFlow<Boolean> = _backgroundScanEnabled

    private val _cloudIntelEnabled = MutableStateFlow(backendClient.isEnabled)
    val cloudIntelEnabled: StateFlow<Boolean> = _cloudIntelEnabled

    private val _backendHealthy = MutableStateFlow<Boolean?>(null)
    val backendHealthy: StateFlow<Boolean?> = _backendHealthy

    private val _isLoggedIn = MutableStateFlow(backendClient.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(backendClient.getUserName())
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(backendClient.getUserEmail())
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun getGoogleSignInIntent() = googleAuthManager.getSignInIntent()

    fun handleGoogleSignInResult(intent: android.content.Intent?) {
        if (intent == null) {
            _loginError.value = "Sign-in was cancelled"
            return
        }
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(intent)
        val idToken = googleAuthManager.handleSignInResult(task)
        if (idToken != null) {
            loginWithBackend(idToken)
        } else {
            _loginError.value = "Google Sign-In failed — please try again"
        }
    }

    fun clearLoginError() { _loginError.value = null }

    private fun loginWithBackend(idToken: String) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null
            val success = backendClient.loginWithGoogle(idToken)
            if (success) {
                _isLoggedIn.value = true
                _userName.value = backendClient.getUserName()
                _userEmail.value = backendClient.getUserEmail()
            } else {
                _loginError.value = "Could not connect to server — check your internet"
            }
            _isLoggingIn.value = false
        }
    }

    fun logout() {
        googleAuthManager.signOut {
            backendClient.logout()
            _isLoggedIn.value = false
            _userName.value = null
            _userEmail.value = null
        }
    }

    fun toggleBackgroundScan(enabled: Boolean) {
        _backgroundScanEnabled.value = enabled
        if (enabled) {
            scheduleBackgroundScan()
        } else {
            cancelBackgroundScan()
        }
    }

    fun toggleCloudIntelligence(enabled: Boolean) {
        _cloudIntelEnabled.value = enabled
        backendClient.isEnabled = enabled
        if (enabled) {
            checkBackendHealth()
        } else {
            _backendHealthy.value = null
        }
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            _backendHealthy.value = backendClient.checkHealth()
        }
    }

    private fun scheduleBackgroundScan() {
        val request = PeriodicWorkRequestBuilder<BackgroundScanWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BG_SCAN_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun cancelBackgroundScan() {
        WorkManager.getInstance(context).cancelUniqueWork(BG_SCAN_WORK_NAME)
    }
}

