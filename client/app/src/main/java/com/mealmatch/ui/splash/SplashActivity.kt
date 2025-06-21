package com.mealmatch.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mealmatch.data.local.TokenManager
import com.mealmatch.ui.auth.AuthActivity
import com.mealmatch.ui.MainActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (TokenManager.getToken(this) != null) {
            // User is logged in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // No token, go to AuthActivity
            startActivity(Intent(this, AuthActivity::class.java))
        }
        // Finish SplashActivity so the user can't navigate back to it
        finish()
    }
}