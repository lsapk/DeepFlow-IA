package com.deepflowia.app

import android.app.Application
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase is now initialized automatically via its ContentProvider and google-services.json.
        // The explicit, programmatic initialization has been removed to prevent conflicts
        // and ensure that all Firebase services, including Authentication, work correctly.
        // The Firebase AI SDK will automatically use the API key from the Firebase project
        // defined in google-services.json.
    }
}
