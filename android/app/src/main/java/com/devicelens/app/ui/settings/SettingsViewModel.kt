package com.devicelens.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.worker.BackgroundScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendClient: BackendClient
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

