package com.devicelens.app.domain.auth

import android.content.Context
import android.content.Intent
import com.devicelens.app.BuildConfig
import com.devicelens.app.helpers.DebugLog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        const val TAG = "GoogleAuthManager"
    }

    private val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    .requestProfile()
                    .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    sealed class SignInOutcome {
        data class Success(val idToken: String) : SignInOutcome()
        object Cancelled : SignInOutcome()
        data class Failure(val message: String, val statusCode: Int = 0) : SignInOutcome()
    }

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>): String? =
        (handleSignInResultDetailed(completedTask) as? SignInOutcome.Success)?.idToken

    fun handleSignInResultDetailed(completedTask: Task<GoogleSignInAccount>): SignInOutcome {
        return try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                DebugLog.i(TAG, "Successfully retrieved Google ID token")
                SignInOutcome.Success(idToken)
            } else {
                DebugLog.w(TAG, "ID token was null — check GOOGLE_CLIENT_ID in frontend.env (must be a Web OAuth client ID, not Android)")
                SignInOutcome.Failure("Google returned no ID token. Check that GOOGLE_CLIENT_ID is a Web OAuth client ID.")
            }
        } catch (e: ApiException) {
            val friendly = when (e.statusCode) {
                12501 -> return SignInOutcome.Cancelled // SIGN_IN_CANCELLED
                7 -> "No network connection. Check your internet."
                10 -> "Developer error — SHA-1 fingerprint not registered in Google Cloud Console for this package."
                8 -> "Internal error from Google Play services. Try again."
                4 -> "Sign-in required."
                else -> "Google sign-in failed (code ${e.statusCode}): ${e.message ?: "unknown"}"
            }
            DebugLog.e(TAG, "Google sign-in failed: status=${e.statusCode}, message=${e.message}")
            SignInOutcome.Failure(friendly, e.statusCode)
        } catch (e: Exception) {
            DebugLog.e(TAG, "Google sign-in unexpected error: ${e.javaClass.simpleName}: ${e.message}")
            SignInOutcome.Failure("Unexpected: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            DebugLog.i(TAG, "Successfully signed out of Google")
            onComplete()
        }
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
