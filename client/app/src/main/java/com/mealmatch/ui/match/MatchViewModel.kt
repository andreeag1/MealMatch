package com.mealmatch.ui.match

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mealmatch.data.model.Restaurant
import com.mealmatch.data.model.MatchResultResponse
import com.mealmatch.data.model.MatchSessionResponse
import com.mealmatch.data.model.SubmitSwipesRequest
import com.mealmatch.data.model.Swipe
import com.mealmatch.data.network.repository.SessionRepository
import com.mealmatch.ui.friends.ApiResult
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.*
import com.mealmatch.BuildConfig
import com.mealmatch.data.model.UserPreferences
import com.mealmatch.data.network.repository.ProfilePrefRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.mealmatch.data.model.Ambiance
import com.mealmatch.data.model.Budget
import com.mealmatch.data.model.DietaryNeed

class MatchViewModel (application: Application) : AndroidViewModel(application) {
    private val sessionRepository = SessionRepository()
    private val profileRepository = ProfilePrefRepository()
    internal val placesClient: PlacesClient

    private val _sessionResult = MutableLiveData<ApiResult<MatchSessionResponse>>()
    val sessionResult: LiveData<ApiResult<MatchSessionResponse>> = _sessionResult

    private val _submitSwipesResult = MutableLiveData<ApiResult<Unit>>()
    val submitSwipesResult: LiveData<ApiResult<Unit>> = _submitSwipesResult

    private val _matchResult = MutableLiveData<ApiResult<MatchResultResponse>>()
    val matchResult: LiveData<ApiResult<MatchResultResponse>> = _matchResult

    private val _preferences = MutableLiveData<ApiResult<UserPreferences>>()
    val preferences: LiveData<ApiResult<UserPreferences>> = _preferences

    private val _restaurants = MutableLiveData<ApiResult<List<Restaurant>>>()
    val restaurants : LiveData<ApiResult<List<Restaurant>>> = _restaurants

    init {
        val context = application.applicationContext
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(context)
    }

    fun startNewSession(token: String, groupId: String?) {
        _sessionResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = if (groupId != null) {
                    sessionRepository.createMatchSession(token, groupId)
                } else {
                    sessionRepository.createSoloSession(token)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    _sessionResult.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to start session"
                    _sessionResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _sessionResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun submitSwipes(token: String, sessionId: String, swipes: List<Swipe>) {
        _submitSwipesResult.value = ApiResult.Loading
        val request = SubmitSwipesRequest(swipes)
        viewModelScope.launch {
            try {
                val response = sessionRepository.submitSwipes(token, sessionId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _submitSwipesResult.value = ApiResult.Success(Unit)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to submit swipes"
                    _submitSwipesResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _submitSwipesResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun pollForSessionResult(token: String, sessionId: String) {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = sessionRepository.getSessionResult(token, sessionId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val resultData = response.body()?.data
                        // the backend returns a 202 if the session is still active
                        if (response.code() == 200 && resultData?.restaurantId != null) {
                            _matchResult.value = ApiResult.Success(resultData)
                            break
                        }
                    } else {
                        _matchResult.value = ApiResult.Error(response.body()?.message ?: "Failed to get result")
                        break
                    }
                } catch (e: Exception) {
                    _matchResult.value = ApiResult.Error(e.message ?: "Network error during polling")
                    break
                }
                // wait for 5 seconds before checking again
                delay(5000)
            }
        }
    }

    fun fetchUserPreferences(token: String) {
        _preferences.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = profileRepository.getProfilePref(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _preferences.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    _preferences.value = ApiResult.Error(response.body()?.message ?: "Failed to fetch preferences")
                }
            } catch (e: Exception) {
                _preferences.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun fetchNearbyRestaurants(location: Location, preferences: UserPreferences? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val placeFields = listOf(
                Field.ID,
                Field.NAME,
                Field.TYPES,
                Field.RATING,
                Field.PHOTO_METADATAS,
                Field.LAT_LNG,
                Field.PRICE_LEVEL,
                Field.SERVES_VEGETARIAN_FOOD,
            )
            val center = LatLng(location.latitude, location.longitude)
            val circle = CircularBounds.newInstance(center, 5000.0) //5km radius
            val request = SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedPrimaryTypes(listOf("restaurant"))
                .setExcludedPrimaryTypes(listOf("lodging"))
                .setMaxResultCount(20)
                .build()

            placesClient.searchNearby(request)
                .addOnSuccessListener { resp ->
                    val allRestaurants =
                        resp.places.mapNotNull { p -> mapPlaceToRestaurant(p, location) }

                    val filteredRestaurants =
                        filterRestaurantsByPreferences(allRestaurants, preferences)

                    _restaurants.postValue(ApiResult.Success(filteredRestaurants))
                }
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    private fun mapPlaceToRestaurant(place: Place, origin: Location): Restaurant? {
        val name = place.name ?: return null
        val latLng = place.latLng ?: return null

        val placeLocation = Location("place").apply {
            latitude = latLng.latitude
            longitude = latLng.longitude
        }
        val distanceKm = origin.distanceTo(placeLocation) / 1000.0

        val allTypes = place.placeTypes?.map { it.toString().lowercase() } ?: emptyList()

        // Infer properties from types and name
        val dietaryNeeds = mutableListOf<DietaryNeed>()
        if (allTypes.contains("vegan_restaurant") || name.contains("vegan", true)) dietaryNeeds.add(DietaryNeed.VEGAN)
        if (allTypes.contains("vegetarian_restaurant") || name.contains("vegetarian", true)) dietaryNeeds.add(DietaryNeed.VEGETARIAN)

        val ambiance = mutableListOf<Ambiance>()
        if (name.contains("cafe", true) || name.contains("cozy", true)) ambiance.add(Ambiance.COZY)
        if (name.contains("bar", true) || name.contains("pub", true)) ambiance.add(Ambiance.CASUAL)
        if (name.contains("fine dining", true)) ambiance.add(Ambiance.FORMAL)

        val budget = when (place.priceLevel) {
            1 -> Budget.CHEAP
            2 -> Budget.MODERATE
            3 -> Budget.EXPENSIVE
            4 -> Budget.VERY_EXPENSIVE
            else -> Budget.ANY
        }

        return Restaurant(
            name = name,
            primaryCuisine = allTypes.firstOrNull()?.replace("_", " ") ?: "Unknown",
            rating = place.rating ?: 0.0,
            distance = distanceKm,
            latLng = latLng,
            photoMetadata = place.photoMetadatas?.firstOrNull(),
            allCuisines = allTypes,
            dietaryNeeds = dietaryNeeds,
            ambiance = ambiance,
            budget = budget
        )
    }

    private fun filterRestaurantsByPreferences(restaurants: List<Restaurant>, preferences: UserPreferences?): List<Restaurant> {
        if (preferences == null) {
            Log.d("FilterDebug", "No preferences provided. Returning all ${restaurants.size} restaurants.")
            return restaurants
        }

        Log.d("FilterDebug", "Filtering with preferences: $preferences")

        val cuisinePref = preferences.cuisine.lowercase().takeIf { it != "any" }
        val dietaryPref = preferences.dietary.lowercase().takeIf { it != "none" }
        val ambiancePref = preferences.ambiance.lowercase().takeIf { it != "any" }
        val budgetPref = preferences.budget

        val filteredList = restaurants.filter { restaurant ->
            Log.d("FilterDebug", "  - Checking '${restaurant.name}'. Types: ${restaurant.allCuisines.joinToString()}")

            // Check if the preference keyword exists in the restaurant's name OR its types list
            val cuisineMatch = cuisinePref == null || restaurant.name.lowercase().contains(cuisinePref) || restaurant.allCuisines.any { it.contains(cuisinePref) }
            val dietaryMatch = dietaryPref == null || restaurant.name.lowercase().contains(dietaryPref) || restaurant.allCuisines.any { it.contains(dietaryPref) }
            val ambianceMatch = ambiancePref == null || restaurant.name.lowercase().contains(ambiancePref) || restaurant.allCuisines.any { it.contains(ambiancePref) }

            val budgetMatch = if (budgetPref == "Any") {
                true // If user preference is "Any", it matches all restaurants
            } else {
                // If user has a specific preference, the restaurant must also have a specific price level
                if (restaurant.budget == Budget.ANY) {
                    false // A restaurant with no price info doesn't match a specific budget
                } else {
                    when (budgetPref) {
                        "$" -> restaurant.budget.ordinal <= Budget.CHEAP.ordinal
                        "$$" -> restaurant.budget.ordinal <= Budget.MODERATE.ordinal
                        "$$$" -> restaurant.budget.ordinal <= Budget.EXPENSIVE.ordinal
                        "$$$$" -> restaurant.budget.ordinal <= Budget.VERY_EXPENSIVE.ordinal
                        else -> true
                    }
                }
            }

            val finalMatch = cuisineMatch || dietaryMatch || ambianceMatch || budgetMatch

            Log.d("FilterDebug", "  - Checking '${restaurant.name}': Cuisine? $cuisineMatch, Dietary? $dietaryMatch, Ambiance? $ambianceMatch, Budget? $budgetMatch -> Final? $finalMatch")

            finalMatch
        }

        Log.d("FilterDebug", "Filtering complete. Found ${filteredList.size} matching restaurants.")
        return filteredList
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun fetchOnePlace(id: String): Restaurant? {
        return try {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.TYPES,
                Place.Field.RATING,
                Place.Field.PHOTO_METADATAS,
                Place.Field.LAT_LNG
            )

            val place = placesClient
                .fetchPlace(FetchPlaceRequest.builder(id, fields).build())
                .await()
                .place

            val photoMetadata = place.photoMetadatas?.firstOrNull()
            val latLng = place.latLng ?: return null

            val restaurant = Restaurant(
                name = place.name ?: "Unnamed",
                primaryCuisine = place.types?.firstOrNull()?.name
                    ?.lowercase()?.replace('_', ' ')
                    ?.replaceFirstChar(Char::uppercaseChar) ?: "Unknown",
                rating = place.rating ?: 0.0,
                distance = 0.0, // needed?
                photoMetadata = photoMetadata,
                latLng = latLng
            )

            restaurant
        } catch (_: Exception) {
            null
        }
    }

}