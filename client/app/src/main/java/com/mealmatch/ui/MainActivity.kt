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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the NavHostFragment by its ID from activity_main.xml
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the BottomNavigationView with the NavController
        binding.bottomNavigationView.setupWithNavController(navController)


        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.matchFragment -> {
                    navController.navigate(R.id.mapFragment)
                    true
                }
                R.id.friendsFragment -> {
                    navController.navigate(R.id.mapFragment)
                    true
                }
                R.id.homeFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.mapFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }

        // Set up the BottomNavigationView with the NavController
        binding.bottomNavigationView.setupWithNavController(navController)
    }


}