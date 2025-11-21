package com.deepflowia.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure et initialise Firebase avec la clé API Gemini
        val options = FirebaseOptions.Builder()
            .setApiKey(BuildConfig.GEMINI_API_KEY)
            .setApplicationId(BuildConfig.APPLICATION_ID) // Assurez-vous que l'ID de l'application est disponible
            .build()

        try {
            FirebaseApp.initializeApp(this, options)
        } catch (e: IllegalStateException) {
            // FirebaseApp est déjà initialisé, ce qui est normal dans certains scénarios.
        }
    }
}
