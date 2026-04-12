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
            val idToken = googleAuthManager.handleSignInResult(task)
            
            if (idToken != null) {
                val success = backendClient.loginWithGoogle(idToken)
                if (success) {
                    _uiState.value = OnboardingUiState.Success
                    onComplete()
                } else {
                    _uiState.value = OnboardingUiState.Error("Backend authentication failed")
                }
            } else {
                _uiState.value = OnboardingUiState.Error("Google Sign-In failed")
            }
        }
    }
}

sealed class OnboardingUiState {
    data object Idle : OnboardingUiState()
    data object Loading : OnboardingUiState()
    data object Success : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}
