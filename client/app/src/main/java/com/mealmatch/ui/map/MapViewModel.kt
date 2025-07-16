package com.mealmatch.ui.map

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.mealmatch.data.model.Restaurant

class MapViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private var fullRestaurantList = listOf<Restaurant>()
    private var currentTextQuery: String? = null
    private var currentCuisineFilter: String? = null // 1. Add state for cuisine filter

    private val _restaurants = MutableLiveData<List<Restaurant>>()
    val restaurants: LiveData<List<Restaurant>> = _restaurants

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchRestaurantsForLocation(center: LatLng, origin: Location) {
        repository.fetchNearby(
            center = center,
            origin = origin,
            onSuccess = { results ->
                fullRestaurantList = results
                applyAllFilters() // IMPORTANT: Changed to applyAllFilters
            },
            onFailure = { exception ->
                _error.postValue("Failed to fetch restaurants: ${exception.message}")
            }
        )
    }

    fun filterListByText(query: String?) {
        currentTextQuery = query
        applyAllFilters() // Changed to applyAllFilters
    }

    // 2. Add new function to handle cuisine selection
    fun filterListByCuisine(cuisine: String?) {
        currentCuisineFilter = cuisine
        applyAllFilters()
    }

    // 3. Rename applyTextFilter to applyAllFilters and update logic
    private fun applyAllFilters() {
        // Start with the full list
        var filteredList = fullRestaurantList

        // First, apply the cuisine filter if one is selected
        currentCuisineFilter?.let { cuisine ->
            filteredList = filteredList.filter { restaurant ->
                restaurant.cuisine.contains(cuisine, ignoreCase = true)
            }
        }

        // Next, apply the text query on the already-filtered list
        if (!currentTextQuery.isNullOrBlank()) {
            filteredList = filteredList.filter { restaurant ->
                restaurant.name.contains(currentTextQuery!!, ignoreCase = true) ||
                        restaurant.cuisine.contains(currentTextQuery!!, ignoreCase = true)
            }
        }

        _restaurants.postValue(filteredList)
    }
}