package com.mealmatch.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.mealmatch.databinding.FragmentProfileBinding
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.mealmatch.R
import com.mealmatch.data.model.UserPreferenceMessage
import com.mealmatch.data.model.UserProfileMessage
import com.mealmatch.ui.auth.AuthActivity
import com.mealmatch.data.network.repository.ProfilePrefRepository
import kotlinx.coroutines.launch

data class UserPreferences(
    var username: String,
    var email: String,
    var cuisines: String? = null,
    var dietary: String? = null,
    var ambiance: String? = null,
    var budget: String? = null,
)


class ProfileFragment : Fragment() {
    private val profilePrefRepository = ProfilePrefRepository()

    val userSettings = UserPreferences(
        username = "testUsername",
        email = "testemail@gmail.com"
    )

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.btnLeaderboard.setOnClickListener { handleViewLeaderBoards() }
        binding.btnSettings.setOnClickListener { handleSettings() }
        binding.btnLogout.setOnClickListener { handleLogout() }

        binding.editPreferences.setOnClickListener { showEditPreferencesPopup() }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProfileInfo()
        fetchPreferences()
        
        // Prompt if preferences are empty
        if (userSettings.cuisines.isNullOrEmpty() &&
            userSettings.dietary.isNullOrEmpty() &&
            userSettings.ambiance.isNullOrEmpty() &&
            userSettings.budget.isNullOrEmpty()
        ) {
            showInitialPreferencesDialog()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setProfileInfo() {
        binding.userName.text = "${userSettings.username}"
        binding.userEmail.text = "${userSettings.email}"
    }

    private fun handleEditPreferences(
        cuisinePreferences: String,
        dietaryPreferences: String,
        ambiancePreferences: String,
        budgetPreferences: String,
    ) {
        // Updates user settings data class
        userSettings.cuisines = cuisinePreferences
        userSettings.dietary = dietaryPreferences
        userSettings.ambiance = ambiancePreferences
        userSettings.budget = budgetPreferences

        // Updates ui to show new preferences
        binding.prefCuisines.text = "Cuisine: ${userSettings.cuisines}"
        binding.prefDietary.text = "Dietary: ${userSettings.dietary}"
        binding.prefAmbiance.text = "Ambiance: ${userSettings.ambiance}"
        binding.prefBudget.text = "Budget: ${userSettings.budget}"
    }

    private fun handleViewLeaderBoards() {
        //navigate to leaderboards fragment
    }

    private fun handleSettings() {
        //navigate to settings fragment
    }

    private fun handleLogout() {
        // logout of app
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears back stack
        startActivity(intent)
    }

    private fun showEditPreferencesPopup() {
        Log.i("ProfileFragment", "showEditPreferencesPopup")
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_preferences_popup, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.inputBudget)

        val inputCuisine = dialogView.findViewById<EditText>(R.id.inputCuisine)
        val inputDietary = dialogView.findViewById<EditText>(R.id.inputDietary)
        val inputAmbiance = dialogView.findViewById<EditText>(R.id.inputAmbiance)
        val inputBudgetOptions = arrayOf("Select a budget", "$$", "$$$", "$$$$")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, inputBudgetOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)


        AlertDialog.Builder(requireContext())
            .setTitle("Edit Preferences")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val cuisine = inputCuisine.text.toString()
                val dietary = inputDietary.text.toString()
                val ambiance = inputAmbiance.text.toString()
                val budget = spinner.selectedItem.toString()
                handleEditPreferences(cuisine, dietary, ambiance, budget)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showInitialPreferencesDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_preferences_popup, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.inputBudget)

        val inputCuisine = dialogView.findViewById<EditText>(R.id.inputCuisine)
        val inputDietary = dialogView.findViewById<EditText>(R.id.inputDietary)
        val inputAmbiance = dialogView.findViewById<EditText>(R.id.inputAmbiance)

        val inputBudgetOptions = arrayOf("Select a budget", "$$", "$$$", "$$$$")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, inputBudgetOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        AlertDialog.Builder(requireContext())
            .setTitle("Set Your Preferences")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val cuisine = inputCuisine.text.toString()
                val dietary = inputDietary.text.toString()
                val ambiance = inputAmbiance.text.toString()
                val budget = spinner.selectedItem.toString()
                handleEditPreferences(cuisine, dietary, ambiance, budget)
                sendPreferencesToBackend()
                dialog.dismiss()
            }
            .setNegativeButton("Skip") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    private fun sendPreferencesToBackend() {
        val preferences = UserPreferenceMessage(
            cuisine = userSettings.cuisines ?: "",
            dietary = userSettings.dietary ?: "",
            ambiance = userSettings.ambiance ?: "",
            budget = userSettings.budget ?: ""
        )

        val userProfile = UserProfileMessage(
            userId = "some_user_id", // You must replace with the actual user ID (maybe from auth)
            username = userSettings.username,
            email = userSettings.email,
            userPreferenceMessage = preferences
        )
        lifecycleScope.launch {
            try{
                profilePrefRepository.createProfilePref("testtoken", userProfile)
            }catch (e: Exception){
                Log.e("ProfileFragment", "Failed to save profile")
            }
        }
    }

    private fun fetchPreferences() {
        lifecycleScope.launch {
            try{
                val response = profilePrefRepository.getProfilePref("testtoken")
                if (response.isSuccessful){
                    if (response.body() != null){
                        val profileMessage = response.body()!!.data
                        val prefs = profileMessage?.userPreferenceMessage
                        handleEditPreferences(
                            prefs?.cuisine ?: "",
                            prefs?.dietary ?: "",
                            prefs?.ambiance ?: "",
                            prefs?.budget ?: ""
                        )
                    }
                }
            }catch (e: Exception){
                Log.e("ProfileFragment", "Failed to fetch profile")
            }
        }
    }
}