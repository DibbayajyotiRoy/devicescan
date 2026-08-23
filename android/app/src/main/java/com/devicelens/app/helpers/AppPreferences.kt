package com.devicelens.app.helpers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small, synchronously-readable flags about where the user is in the app.
 *
 * These are deliberately SharedPreferences rather than DataStore: the launcher
 * activity needs to know which screen to start on *before* the first frame. An
 * async read would mean either a blank frame or a visible jump from the scanner
 * to onboarding, and the flash is far more expensive than the blocking read of
 * a single boolean.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("devicelens_state", Context.MODE_PRIVATE)

    /** The user has been through the intro and knows what the app does. */
    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

    /**
     * Whether we have ever shown the system permission prompt.
     *
     * Android silently ignores a second request once the user has permanently
     * denied one, so a button that appears to do nothing is the result. Knowing
     * we have already asked lets the UI send them to system settings instead.
     */
    var hasRequestedPermissions: Boolean
        get() = prefs.getBoolean(KEY_REQUESTED_PERMISSIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_REQUESTED_PERMISSIONS, value).apply()

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        const val KEY_REQUESTED_PERMISSIONS = "has_requested_permissions"
    }
}
