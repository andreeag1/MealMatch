package com.mealmatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mealmatch.databinding.FragmentProfileBinding
import java.util.Optional

data class UserPreferences(
    var username: String,
    var email: String,
    var cuisines: String? = null,
    var dietary: String? = null,
    var ambiance: String? = null,
    var budget: String? = null,
)


class ProfileFragment : Fragment() {
    val userSettings = UserPreferences(
        username = "testUsername",
        email = "testemail@gmail.com")


    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root


        binding.editPreferences.setOnClickListener {}
        binding.btnLeaderboard.setOnClickListener {handleViewLeaderBoards()}
        binding.btnSettings.setOnClickListener {handleSettings()}
        binding.btnLogout.setOnClickListener {handleLogout()}

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProfileInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setProfileInfo(){
        binding.userName.text = "${userSettings.username}"
        binding.userEmail.text = "${userSettings.email}"
    }

    private fun handleEditPreferences(
        cuisinePreferences : String,
        dietaryPreferences : String,
        ambiancePreferences : String,
        budgetPreferences : String,
    ){
        // Updates user settings data class
        userSettings.cuisines = cuisinePreferences
        userSettings.dietary = dietaryPreferences
        userSettings.ambiance = ambiancePreferences
        userSettings.budget = budgetPreferences

        // Updates ui to show new preferences
        binding.prefCuisines.text = "Cuisine Preferences: ${userSettings.cuisines}"
        binding.prefDietary.text = "Cuisine Preferences: ${userSettings.dietary}"
        binding.prefAmbiance.text = "Cuisine Preferences: ${userSettings.ambiance}"
        binding.prefDietary.text = "Cuisine Preferences: ${userSettings.budget}"
    }

    private fun handleViewLeaderBoards(){
        //navigate to leaderboards fragment
    }

    private fun handleSettings(){
        //navigate to settings fragment
    }

    private fun handleLogout(){
        // logout of app
    }
}