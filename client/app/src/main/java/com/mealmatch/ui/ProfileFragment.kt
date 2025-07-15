package com.mealmatch.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.data.model.*
import com.mealmatch.data.network.repository.ProfilePrefRepository
import com.mealmatch.databinding.FragmentProfileBinding
import com.mealmatch.ui.auth.AuthActivity
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

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profilePrefRepository = ProfilePrefRepository()

    private val userSettings = UserPreferences(
        username = UserViewModel.username ?: "testUsername",
        email = UserViewModel.email ?: "testemail@gmail.com"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        with(binding) {
            btnLeaderboard.setOnClickListener { handleViewLeaderBoards() }
            btnSettings.setOnClickListener { handleSettings() }
            btnLogout.setOnClickListener { handleLogout() }
            editPreferences.setOnClickListener { showEditPreferencesPopup() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProfileInfo()
        fetchPreferences()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setProfileInfo() {
        binding.userName.text = userSettings.username
        binding.userEmail.text = userSettings.email
    }

    @SuppressLint("SetTextI18n")
    private fun handleEditPreferences(
        cuisinePreferences: String,
        dietaryPreferences: String,
        ambiancePreferences: String,
        budgetPreferences: String
    ) {
        userSettings.apply {
            cuisines = cuisinePreferences
            dietary = dietaryPreferences
            ambiance = ambiancePreferences
            budget = budgetPreferences
        }

        with(binding) {
            prefCuisines.text = "Cuisine: ${userSettings.cuisines}"
            prefDietary.text = "Dietary: ${userSettings.dietary}"
            prefAmbiance.text = "Ambiance: ${userSettings.ambiance}"
            prefBudget.text = "Budget: ${userSettings.budget}"
        }
    }

    private fun showPreferencesDialog(
        title: String,
        onSave: (String, String, String, String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.edit_preferences_popup, null)

        val inputCuisine = dialogView.findViewById<Spinner>(R.id.inputCuisine)
        val inputDietary = dialogView.findViewById<Spinner>(R.id.inputDietary)
        val inputAmbiance = dialogView.findViewById<Spinner>(R.id.inputAmbiance)
        val spinner = dialogView.findViewById<Spinner>(R.id.inputBudget)

        val cuisineOptions = arrayOf("Select a cuisine", "Chinese", "Japanese", "Korean", "Thai", "Indian", "Italian", "French", "Mexican", "Middle Eastern", "American")
        val dietaryOptions = arrayOf("Select a dietary preference", "Vegetarian", "Vegan", "Gluten-Free", "Pescatarian", "Halal", "Kosher", "Dairy-Free", "Nut-Free")
        val ambianceOptions = arrayOf("Select an ambiance", "Casual", "Cozy", "Romantic", "Trendy", "Family-Friendly", "Upscale", "Outdoor Seating", "Lively", "Quiet")
        val inputBudgetOptions = arrayOf("Select a budget", "$", "$$", "$$$", "$$$$")

        fun setupSpinner(spinner: Spinner, options: Array<String>) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(0)
        }

        setupSpinner(inputCuisine, cuisineOptions)
        setupSpinner(inputDietary, dietaryOptions)
        setupSpinner(inputAmbiance, ambianceOptions)
        setupSpinner(spinner, inputBudgetOptions)

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val cuisine = inputCuisine.selectedItem.toString()
                val dietary = inputDietary.selectedItem.toString()
                val ambiance = inputAmbiance.selectedItem.toString()
                val budget = spinner.selectedItem.toString()

                // send to backend
                sendPreferencesToBackend()

                onSave(cuisine, dietary, ambiance, budget)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showEditPreferencesPopup() {
        showPreferencesDialog("Edit Preferences") { cuisine, dietary, ambiance, budget ->
            handleEditPreferences(cuisine, dietary, ambiance, budget)
            sendPreferencesToBackend()
        }
    }

    private fun showInitialPreferencesDialog() {
        showPreferencesDialog("Set Your Preferences") { cuisine, dietary, ambiance, budget ->
            handleEditPreferences(cuisine, dietary, ambiance, budget)
            sendPreferencesToBackend()
        }
    }

    private fun sendPreferencesToBackend() {
        val token = TokenManager.getToken(requireContext())
        if (token == null) {
            Log.e("ProfileFragment", "Failed to save profile due to empty token")
            return
        }

        val authHeader = "Bearer $token"

        val preferences = UserPreferenceMessage(
            cuisine = userSettings.cuisines.orEmpty(),
            dietary = userSettings.dietary.orEmpty(),
            ambiance = userSettings.ambiance.orEmpty(),
            budget = userSettings.budget.orEmpty()
        )

        val userProfile = UserProfileMessage(
            userID = token,
            username = userSettings.username,
            email = userSettings.email,
            userPreferenceMessage = preferences
        )

        lifecycleScope.launch {
            try {
                Log.i("ProfileFragment", "Sending preferences: $userProfile")
                profilePrefRepository.setProfilePref(authHeader, userProfile) // ✅ FIXED
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Failed to save profile", e)
            }
        }
    }

    private fun fetchPreferences() {
        val token = TokenManager.getToken(requireContext()) ?: run {
            Log.e("ProfileFragment", "Empty token")
            return
        }

        val authHeader = "Bearer $token"

        lifecycleScope.launch {
            try {
                val response = profilePrefRepository.getProfilePref(authHeader) 
                if (response.isSuccessful) {
                    val prefs = response.body()?.data?.userPreferenceMessage

                    handleEditPreferences(
                        prefs?.cuisine.orEmpty(),
                        prefs?.dietary.orEmpty(),
                        prefs?.ambiance.orEmpty(),
                        prefs?.budget.orEmpty()
                    )

                    if (prefs?.run {
                            cuisine.isEmpty() && dietary.isEmpty() &&
                                    ambiance.isEmpty() && budget.isEmpty()
                        } == true
                    ) {
                        showInitialPreferencesDialog()
                    }

                } else {
                    Log.e("ProfileFragment", "Profile fetch unsuccessful: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Failed to fetch profile", e)
            }
        }
    }

    private fun handleViewLeaderBoards() {
    }

    private fun handleSettings() {
    }

    private fun handleLogout() {
        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
