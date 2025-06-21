package com.mealmatch.ui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mealmatch.R
import com.mealmatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the activity was launched by a deep link
        val intentData: Uri? = intent?.data

        if (intentData != null) {
            val token = intentData.getQueryParameter("token")

            if (token != null) {
                // Now you would save it to a secure place like SharedPreferences
                // and then you can pass it to your fragments or ViewModels.
                saveToken(token) // Example function to save the token
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the NavHostFragment by its ID from activity_main.xml
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the BottomNavigationView with the NavController
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}