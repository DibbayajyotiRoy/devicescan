package com.devicelens.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicelens.app.data.remote.BackendClient
import com.devicelens.app.domain.auth.GoogleAuthManager
import com.devicelens.app.helpers.DebugLog
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val googleAuthManager: GoogleAuthManager,
    private val backendClient: BackendClient
) : ViewModel() {

    private val TAG = "OnboardingViewModel"

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun getSignInIntent() = googleAuthManager.getSignInIntent()

    fun handleSignInResult(task: Task<GoogleSignInAccount>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            val signInOutcome = googleAuthManager.handleSignInResultDetailed(task)
            when (signInOutcome) {
                is GoogleAuthManager.SignInOutcome.Success -> {
                    val result = backendClient.loginWithGoogle(signInOutcome.idToken)
                    when (result) {
                        is BackendClient.LoginResult.Success -> {
                            _uiState.value = OnboardingUiState.Success
                            onComplete()
                        }
                        is BackendClient.LoginResult.Failure -> {
                            DebugLog.w(TAG, "Backend login failed: ${result.message} (http=${result.httpCode})")
                            _uiState.value = OnboardingUiState.Error(result.message)
                        }
                    }
                }
                is GoogleAuthManager.SignInOutcome.Cancelled -> {
                    _uiState.value = OnboardingUiState.Idle
                }
                is GoogleAuthManager.SignInOutcome.Failure -> {
                    _uiState.value = OnboardingUiState.Error(signInOutcome.message)
                }
            }
        }
    }

    fun onSignInCancelled() {
        _uiState.value = OnboardingUiState.Idle
    }
}

sealed class OnboardingUiState {
    data object Idle : OnboardingUiState()
    data object Loading : OnboardingUiState()
    data object Success : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}
