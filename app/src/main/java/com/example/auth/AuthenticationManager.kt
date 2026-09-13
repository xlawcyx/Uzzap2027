package com.example.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

/**
 * Small session facade for the email/password Firebase authentication used by the app.
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
     * Checks if a user is currently signed in.
     */
    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Log.d(TAG, "User successfully signed out.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
            Result.failure(e)
        }
    }
}
