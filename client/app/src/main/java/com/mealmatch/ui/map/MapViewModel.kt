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
                // Store the new unfiltered list
                fullRestaurantList = results
                // IMPORTANT: Immediately apply the current text filter to the new list
                applyTextFilter()
            },
            onFailure = { exception ->
                _error.postValue("Failed to fetch restaurants: ${exception.message}")
            }
        )
    }

    /** Filters the currently held list of restaurants by a text query. */
    fun filterListByText(query: String?) {
        currentTextQuery = query
        applyTextFilter()
    }

    private fun applyTextFilter() {
        val query = currentTextQuery
        val filteredList = if (query.isNullOrBlank()) {
            fullRestaurantList
        } else {
            fullRestaurantList.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.cuisine.contains(query, ignoreCase = true)
            }
        }
        _restaurants.postValue(filteredList)
    }
}