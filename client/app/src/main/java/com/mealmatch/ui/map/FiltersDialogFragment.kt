package com.mealmatch.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mealmatch.R
import com.mealmatch.data.model.Ambiance
import com.mealmatch.data.model.Budget
import com.mealmatch.data.model.DietaryNeed
import com.mealmatch.data.model.SearchCriteria
import com.mealmatch.databinding.DialogFiltersBinding
import java.util.Locale

class FiltersDialogFragment : DialogFragment() {

    interface FiltersDialogListener {
        fun onFiltersApplied(searchCriteria: SearchCriteria)
    }

    private var _binding: DialogFiltersBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentCriteria: SearchCriteria

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a standard, safe theme for the full-screen dialog
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
        currentCriteria = arguments?.getParcelable(ARG_CRITERIA) ?: SearchCriteria()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupChips()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { dismiss() }
    }

    private fun setupButtons() {
        binding.btnClear.setOnClickListener {
            currentCriteria = SearchCriteria() // Reset to default
            updateChipSelections() // Update UI to reflect reset
        }
        binding.btnApply.setOnClickListener {
            collectChipSelections()
            (parentFragment as? FiltersDialogListener)?.onFiltersApplied(currentCriteria)
            dismiss()
        }
    }

    private fun setupChips() {
        val cuisineOptions = listOf("Italian", "Japanese", "Mexican", "Cafe", "Burger", "Bar", "Sushi")
        // Use non-deprecated string transformations
        val dietaryOptions = DietaryNeed.values().map { it.name.lowercase(Locale.getDefault()).replace("_", " ").replaceFirstChar(Char::titlecase) }
        val ambianceOptions = Ambiance.values().map { it.name.lowercase(Locale.getDefault()).replaceFirstChar(Char::titlecase) }
        val budgetOptions = Budget.values().map { it.symbol }

        populateChipGroup(binding.filterChipGroupCuisines, cuisineOptions)
        populateChipGroup(binding.filterChipGroupDietary, dietaryOptions)
        populateChipGroup(binding.filterChipGroupAmbiance, ambianceOptions)
        populateChipGroup(binding.filterChipGroupBudget, budgetOptions)

        updateChipSelections()
    }

    private fun populateChipGroup(chipGroup: ChipGroup, options: List<String>) {
        options.forEach { option ->
            val chip = Chip(requireContext()).apply { text = option; isCheckable = true }
            chipGroup.addView(chip)
        }
    }

    private fun updateChipSelections() {
        setChipStates(binding.filterChipGroupCuisines, currentCriteria.cuisines)
        val selectedDietary = currentCriteria.dietaryNeeds.map { it.name.lowercase(Locale.getDefault()).replace("_", " ").replaceFirstChar(Char::titlecase) }.toSet()
        val selectedAmbiance = currentCriteria.ambiance.map { it.name.lowercase(Locale.getDefault()).replaceFirstChar(Char::titlecase) }.toSet()
        setChipStates(binding.filterChipGroupDietary, selectedDietary)
        setChipStates(binding.filterChipGroupAmbiance, selectedAmbiance)

        // Correctly find and check the selected budget chip
        for (i in 0 until binding.filterChipGroupBudget.childCount) {
            val chip = binding.filterChipGroupBudget.getChildAt(i) as Chip
            if (chip.text.toString() == currentCriteria.budget.symbol) {
                chip.isChecked = true
                break
            }
        }
    }

    private fun setChipStates(chipGroup: ChipGroup, selected: Set<String>) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = selected.contains(chip.text.toString())
        }
    }

    private fun collectChipSelections() {
        currentCriteria.cuisines = getSelectedFrom(binding.filterChipGroupCuisines).toMutableSet()
        currentCriteria.dietaryNeeds = getSelectedFrom(binding.filterChipGroupDietary)
            .map { DietaryNeed.valueOf(it.replace(" ", "_").uppercase(Locale.getDefault())) }
            .toMutableSet()
        currentCriteria.ambiance = getSelectedFrom(binding.filterChipGroupAmbiance)
            .map { Ambiance.valueOf(it.uppercase(Locale.getDefault())) }
            .toMutableSet()

        val selectedBudgetSymbol = getSelectedFrom(binding.filterChipGroupBudget).firstOrNull()
        currentCriteria.budget = Budget.values().firstOrNull { it.symbol == selectedBudgetSymbol } ?: Budget.ANY
    }

    private fun getSelectedFrom(chipGroup: ChipGroup): Set<String> {
        return chipGroup.checkedChipIds.mapNotNull { id ->
            chipGroup.findViewById<Chip>(id)?.text?.toString()
        }.toSet()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FiltersDialog"
        private const val ARG_CRITERIA = "arg_criteria"

        fun newInstance(searchCriteria: SearchCriteria): FiltersDialogFragment {
            return FiltersDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CRITERIA, searchCriteria)
                }
            }
        }
    }
}
