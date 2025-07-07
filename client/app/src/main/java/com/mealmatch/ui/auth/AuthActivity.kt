package com.mealmatch.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.ui.MainActivity
import com.mealmatch.ui.friends.ApiResult

class AuthActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val usernameField = findViewById<EditText>(R.id.editTextUsername)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val actionButton = findViewById<Button>(R.id.buttonAction)
        val switchModeTextView = findViewById<TextView>(R.id.switchModeTextView)

        actionButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (isLoginMode) {
                viewModel.login(email, password)
            } else {
                val username = usernameField.text.toString().trim()
                if (username.isEmpty()) {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.signUp(username, email, password)
            }
        }

        switchModeTextView.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                titleTextView.text = "Login"
                usernameField.visibility = View.GONE
                actionButton.text = "Login"
                switchModeTextView.text = "Don't have an account? Sign Up"
            } else {
                titleTextView.text = "Sign Up"
                usernameField.visibility = View.VISIBLE
                actionButton.text = "Sign Up"
                switchModeTextView.text = "Already have an account? Login"
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.authResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                    TokenManager.saveToken(this, result.data.token)
                    val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    prefs.edit().putString("username", result.data.username).apply()
                    goToMainApp()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        // Clear the activity stack so the user can't go back to the login screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}