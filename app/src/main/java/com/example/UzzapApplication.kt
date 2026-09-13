package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class UzzapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase(this)
    }

    companion object {
        private const val TAG = "UzzapApplication"
        const val FALLBACK_API_KEY = "AIzaSyB-UzzapFallbackKeyForOfflineClientInit"

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

                if (app != null && app.options.apiKey.isNotBlank() && !app.options.apiKey.contains("Fallback")) {
                    isRealFirebaseConfigured = true
                    Log.i(TAG, "Initialized FirebaseApp from google-services.json (Project: ${app.options.projectId})")
                } else {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:143263500044:android:de32efddd044d73ba36b29")
                        .setApiKey("AIzaSyA5BLZhshvV3XBP7BSJ8rBoKaDJwP6uQes")
                        .setProjectId("uzzap2027")
                        .build()
                    if (app == null) {
                        FirebaseApp.initializeApp(context, options)
                    }
                    isRealFirebaseConfigured = true
                    Log.i(TAG, "Configured FirebaseApp options for uzzap2027")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase init exception: ${e.message}", e)
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:143263500044:android:de32efddd044d73ba36b29")
                        .setApiKey("AIzaSyA5BLZhshvV3XBP7BSJ8rBoKaDJwP6uQes")
                        .setProjectId("uzzap2027")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    isRealFirebaseConfigured = true
                } catch (err: Exception) {
                    Log.e(TAG, "Firebase fallback failed: ${err.message}", err)
                }
            }
        }
    }
}
