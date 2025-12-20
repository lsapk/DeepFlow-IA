package com.deepflowia.app.data

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.deepflowia.app.BuildConfig

class GoogleAuthHandler(private val context: Context, private val launcher: ActivityResultLauncher<Intent>) {

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, gso)

    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun handleSignInResult(data: Intent?, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            onSuccess(idToken)
        } catch (e: ApiException) {
            onError("La connexion avec Google a échoué : ${e.statusCode}")
        }
    }
}
