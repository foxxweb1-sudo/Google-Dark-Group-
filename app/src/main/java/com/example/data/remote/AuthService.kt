package com.example.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthService(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)
    private val TAG = "AuthService"

    fun getCurrentUser() = auth.currentUser

    suspend fun attemptAutoSignIn(): Boolean {
        if (auth.currentUser != null) return true

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Web client ID not found", e)
            return false
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        return try {
            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result.credential)
        } catch (e: Exception) {
            Log.d(TAG, "Auto sign-in failed or no credentials: ${e.message}")
            false
        }
    }

    suspend fun signInWithGoogle(activity: Activity): Boolean {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Web client ID not found", e)
            return false
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            handleSignInResult(result.credential)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            false
        }
    }

    private suspend fun handleSignInResult(credential: androidx.credentials.Credential): Boolean {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(authCredential).await()
            return true
        }
        return false
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<com.google.firebase.auth.FirebaseUser> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال البريد الإلكتروني وكلمة المرور"))
        }
        return try {
            val res = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            val user = res.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("فشل تسجيل الدخول: المستخدم غير موجود"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Email Sign-In failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String): Result<com.google.firebase.auth.FirebaseUser> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        val cleanName = displayName.trim()
        return try {
            val res = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            val user = res.user
            if (user != null) {
                // Update profile display name
                try {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (pe: Exception) {
                    Log.w(TAG, "Failed to set display name on Firebase Auth", pe)
                }
                Result.success(user)
            } else {
                Result.failure(Exception("فشل إنشاء الحساب"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Email Sign-Up failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear credential state", e)
        }
    }
}
