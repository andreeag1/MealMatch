package com.mealmatch.ui.map

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.mealmatch.data.model.Budget
import com.mealmatch.data.model.Restaurant
import com.mealmatch.data.model.SearchCriteria

class MapViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private var fullRestaurantList = listOf<Restaurant>()
    private var activeSearchCriteria = SearchCriteria()

    private val _restaurants = MutableLiveData<List<Restaurant>>()
    val restaurants: LiveData<List<Restaurant>> = _restaurants

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /** Fetches a new list of all restaurants for a given location. */
    fun fetchRestaurantsForLocation(center: LatLng, origin: Location) {
        repository.fetchNearby(
            center = center,
            origin = origin,
            onSuccess = { results ->
                fullRestaurantList = results
                // Apply the existing filters to the new list of restaurants
                applyFilters()
            },
            onFailure = { exception ->
                _error.postValue("Failed to fetch restaurants: ${exception.message}")
            }
        )
    }

    /**
     * Updates the active search criteria and re-filters the restaurant list.
     * This is the main entry point for all filtering actions.
     */
    fun updateSearchCriteria(newCriteria: SearchCriteria) {
        activeSearchCriteria = newCriteria
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = fullRestaurantList

        // 1. Filter by Cuisines
        if (activeSearchCriteria.cuisines.isNotEmpty()) {
            filteredList = filteredList.filter { restaurant ->
                // Check if the restaurant's cuisine list contains ANY of the selected cuisines
                activeSearchCriteria.cuisines.any { selectedCuisine ->
                    restaurant.allCuisines.contains(selectedCuisine)
                }
            }
        }

        // 2. Filter by Dietary Needs
        if (activeSearchCriteria.dietaryNeeds.isNotEmpty()) {
            filteredList = filteredList.filter { restaurant ->
                // Check if the restaurant's dietary list contains ALL of the selected needs
                restaurant.dietaryNeeds.containsAll(activeSearchCriteria.dietaryNeeds)
            }
        }

        // 3. Filter by Ambiance
        if (activeSearchCriteria.ambiance.isNotEmpty()) {
            filteredList = filteredList.filter { restaurant ->
                restaurant.ambiance.containsAll(activeSearchCriteria.ambiance)
            }
        }

        // 4. Filter by Budget
        if (activeSearchCriteria.budget != Budget.ANY) {
            filteredList = filteredList.filter { restaurant ->
                restaurant.budget == activeSearchCriteria.budget
            }
        }

        _restaurants.postValue(filteredList)
    }
}
