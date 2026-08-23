package com.devicelens.app.ui.onboarding

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.devicelens.app.helpers.AppPreferences
import com.devicelens.app.helpers.DebugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * State for the intro flow.
 *
 * Notably smaller than it used to be. Onboarding no longer authenticates
 * anybody: the Google sign-in that used to gate the whole app now lives in
 * Settings, next to the cloud-intelligence toggle it actually belongs to. What
 * is left is permissions, which is the only thing the scanner genuinely cannot
 * start without.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val TAG = "OnboardingViewModel"

    /** Current grant state for every permission the intro asks about. */
    fun permissionStates(context: Context): Map<String, Boolean> =
        permissionAsks()
            .flatMap { it.permissions }
            .associateWith { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
            }

    /**
     * True once the permissions the scanner needs are in place.
     *
     * Optional permissions are excluded deliberately: notifications being off
     * must not make the "start scanning" button unreachable.
     */
    fun essentialPermissionsGranted(context: Context): Boolean =
        permissionAsks()
            .filter { it.required }
            .flatMap { it.permissions }
            .all { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
            }

    fun onPermissionResult(result: Map<String, Boolean>) {
        // Remembering that we have asked lets the rest of the app offer a route
        // into system settings instead of re-requesting, which Android silently
        // ignores after a permanent denial.
        appPreferences.hasRequestedPermissions = true
        val granted = result.filterValues { it }.keys
        val denied = result.filterValues { !it }.keys
        DebugLog.i(TAG, "Permissions granted=$granted denied=$denied")
    }
}
