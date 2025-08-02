package com.mealmatch.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.data.model.UserPreferences
import com.mealmatch.databinding.FragmentProfileBinding
import com.mealmatch.ui.auth.AuthActivity
import com.mealmatch.ui.friends.ApiResult

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private var currentPreferences: UserPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.btnLogout.setOnClickListener { handleLogout() }
        binding.editPreferences.setOnClickListener { showEditPreferencesDialog() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        fetchData()
    }

    private fun fetchData() {
        val token = TokenManager.getToken(requireContext())
        if (token != null) {
            val authToken = "Bearer $token"
            viewModel.fetchUserProfile(authToken)
            viewModel.fetchPreferences(authToken)
        } else {
            handleLogout()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.userProfileResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.userName.text = "Loading..."
                    binding.userEmail.text = ""
                }
                is ApiResult.Success -> {
                    binding.userName.text = result.data.username
                    binding.userEmail.text = result.data.email
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error fetching profile: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.preferencesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    // Optionally handle loading state for preferences
                }
                is ApiResult.Success -> {
                    currentPreferences = result.data
                    updatePreferencesUI(result.data)
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error fetching preferences: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.updatePreferencesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(context, "Saving...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    Toast.makeText(context, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
                    // Re-fetch preferences to show the updated values
                    val token = TokenManager.getToken(requireContext())
                    if (token != null) viewModel.fetchPreferences("Bearer $token")
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error saving preferences: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePreferencesUI(prefs: UserPreferences) {
        binding.prefCuisines.text = "Cuisine: ${prefs.cuisine}"
        binding.prefDietary.text = "Dietary: ${prefs.dietary}"
        binding.prefAmbiance.text = "Ambiance: ${prefs.ambiance}"
        binding.prefBudget.text = "Budget: ${prefs.budget}"
    }

    private fun showEditPreferencesDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_preferences_popup, null)

        val inputCuisine = dialogView.findViewById<Spinner>(R.id.inputCuisine)
        val inputDietary = dialogView.findViewById<Spinner>(R.id.inputDietary)
        val inputAmbiance = dialogView.findViewById<Spinner>(R.id.inputAmbiance)
        val inputBudget = dialogView.findViewById<Spinner>(R.id.inputBudget)

        val cuisineOptions = resources.getStringArray(R.array.cuisine_options)
        val dietaryOptions = resources.getStringArray(R.array.dietary_options)
        val ambianceOptions = resources.getStringArray(R.array.ambiance_options)
        val budgetOptions = resources.getStringArray(R.array.budget_options)

        setupSpinner(inputCuisine, cuisineOptions, currentPreferences?.cuisine)
        setupSpinner(inputDietary, dietaryOptions, currentPreferences?.dietary)
        setupSpinner(inputAmbiance, ambianceOptions, currentPreferences?.ambiance)
        setupSpinner(inputBudget, budgetOptions, currentPreferences?.budget)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Preferences")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val newPrefs = UserPreferences(
                    cuisine = inputCuisine.selectedItem.toString(),
                    dietary = inputDietary.selectedItem.toString(),
                    ambiance = inputAmbiance.selectedItem.toString(),
                    budget = inputBudget.selectedItem.toString()
                )
                val token = TokenManager.getToken(requireContext())
                if (token != null) {
                    viewModel.updatePreferences("Bearer $token", newPrefs)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupSpinner(spinner: Spinner, options: Array<String>, currentValue: String?) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        currentValue?.let {
            val position = options.indexOf(it)
            if (position >= 0) {
                spinner.setSelection(position)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleLogout() {
        TokenManager.clearToken(requireContext()) // Clear the token on logout
        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}