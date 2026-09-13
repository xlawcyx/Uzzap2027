package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

class UzzapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase(this)
    }

    companion object {
        private const val TAG = "UzzapApplication"
        @Volatile
        var isRealFirebaseConfigured: Boolean = false
            private set

        fun initFirebase(context: Context) {
            try {
                val app = if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                } else {
                    FirebaseApp.getInstance()
                }

                isRealFirebaseConfigured = app != null && app.options.apiKey.isNotBlank()
                if (!isRealFirebaseConfigured) {
                    Log.w(TAG, "Firebase is not configured; release authentication is unavailable")
                }
            } catch (e: Exception) {
                isRealFirebaseConfigured = false
                Log.w(TAG, "Firebase initialization failed")
            }
        }
    }
}
