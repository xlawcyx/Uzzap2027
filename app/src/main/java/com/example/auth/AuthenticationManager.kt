package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * AuthenticationManager handles Firebase Authentication and Google Sign-In via Credential Manager.
 */
class AuthenticationManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "AuthenticationManager"

        @Volatile
        private var instance: AuthenticationManager? = null

        fun getInstance(): AuthenticationManager {
            return instance ?: synchronized(this) {
                instance ?: AuthenticationManager().also { instance = it }
            }
        }
    }

    /**
     * Represents the outcome of an authentication request.
     */
    sealed interface AuthResultState {
        data class Success(val user: FirebaseUser) : AuthResultState
        data class Error(val message: String, val throwable: Throwable? = null) : AuthResultState
        data object Cancelled : AuthResultState
    }

    /**
     * Currently authenticated Firebase user, or null if not signed in.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Checks if a user is currently signed in.
     */
    fun isUserSignedIn(): Boolean = auth.currentUser != null

    /**
     * Emits the current [FirebaseUser] whenever authentication state changes.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        // Send initial state immediately
        trySend(auth.currentUser)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Initiates Google Sign-In using Android Credential Manager and Firebase Auth.
     *
     * @param context Application or Activity context.
     * @param serverClientId The Web Client ID from Google Cloud Console / Firebase Authentication.
     * @param filterByAuthorizedAccounts If true, only shows accounts already signed in to the app.
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String,
        filterByAuthorizedAccounts: Boolean = false
    ): AuthResultState {
        if (serverClientId.isBlank()) {
            val errorMsg = "Web Client ID (serverClientId) is required for Google Sign-In."
            Log.e(TAG, errorMsg)
            return AuthResultState.Error(errorMsg)
        }

        val credentialManager = CredentialManager.create(context)

        return try {
            // Generate a random hashed nonce for request validation
            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Exchange Google ID token with Firebase Auth
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).awaitResult()
                val user = authResult.user

                if (user != null) {
                    Log.d(TAG, "Google Sign-In successful for user: ${user.email}")
                    AuthResultState.Success(user)
                } else {
                    AuthResultState.Error("Failed to retrieve user from Firebase.")
                }
            } else {
                val errorMsg = "Unexpected credential type returned: ${credential::class.java.name}"
                Log.e(TAG, errorMsg)
                AuthResultState.Error(errorMsg)
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In cancelled by user.")
            AuthResultState.Cancelled
        } catch (e: NoCredentialException) {
            Log.d(TAG, "No Google credential is available for sign-in.")
            AuthResultState.Error("No Google account is available for sign-in.", e)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Failed to parse Google ID token", e)
            AuthResultState.Error("Invalid Google ID token response.", e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            AuthResultState.Error(e.localizedMessage ?: "Credential retrieval failed.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            AuthResultState.Error(e.localizedMessage ?: "Authentication failed.", e)
        }
    }

    /**
     * Signs out from Firebase and clears Credential Manager session state.
     */
    suspend fun signOut(context: Context): Result<Unit> {
        return try {
            auth.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "User successfully signed out.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
            Result.failure(e)
        }
    }

    /**
     * Helper extension to await a Google/Firebase Task using coroutines.
     */
    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
        addOnCanceledListener {
            if (continuation.isActive) {
                continuation.cancel()
            }
        }
    }
}
