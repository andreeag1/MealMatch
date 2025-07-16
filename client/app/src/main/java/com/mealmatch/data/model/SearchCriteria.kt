package com.mealmatch.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class holding all the user-selected criteria for a restaurant search.
 * This class is Parcelable for passing between Android components.
 */
@Parcelize
data class SearchCriteria(
    var cuisines: MutableSet<String> = mutableSetOf(),
    var dietaryNeeds: MutableSet<DietaryNeed> = mutableSetOf(),
    var ambiance: MutableSet<Ambiance> = mutableSetOf(),
    var budget: Budget = Budget.ANY
) : Parcelable
