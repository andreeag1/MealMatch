package com.mealmatch.ui.map

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.mealmatch.data.model.Ambiance
import com.mealmatch.data.model.Budget
import com.mealmatch.data.model.DietaryNeed
import com.mealmatch.data.model.Restaurant

class RestaurantRepository(private val placesClient: PlacesClient) {
    private val TAG = "MapDebug"

    fun fetchNearby(
        center: LatLng,
        origin: Location,
        onSuccess: (List<Restaurant>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG, "Repository: Starting fetchNearby.")
        val placeFields = listOf(
            Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.RATING,
            Place.Field.TYPES, Place.Field.PHOTO_METADATAS
        )

        val circle = CircularBounds.newInstance(center, 2000.0)

        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setMaxResultCount(5)
            .build()


        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                val results = response.places.mapNotNull { place ->
                    place.latLng?.let { latLng ->
                        val rawType = place.placeTypes?.firstOrNull()?.toString().orEmpty()
                        val primaryCuisine = rawType.replace("_", " ").split(" ")
                            .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

                        val restaurantLocation = Location("").apply {
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                        }
                        val distanceInKm = origin.distanceTo(restaurantLocation) / 1000.0

                        // --- Simulate additional data for filtering ---
                        val allCuisines = (listOf(primaryCuisine.split(" ").firstOrNull() ?: "") +
                                listOf("Italian", "Japanese", "Mexican", "Cafe", "Burger", "Bar").shuffled().take(2))
                            .filter { it.isNotBlank() }.distinct()

                        val dietaryNeeds = DietaryNeed.values().toList().shuffled().take((0..2).random())
                        val ambiance = Ambiance.values().toList().shuffled().take((0..2).random())
                        val budget = Budget.values()[(1..4).random()] // Exclude ANY

                        Restaurant(
                            name = place.name ?: "Unnamed Restaurant",
                            primaryCuisine = primaryCuisine,
                            rating = place.rating ?: 0.0,
                            distance = distanceInKm,
                            latLng = latLng,
                            photoMetadata = place.photoMetadatas?.firstOrNull(),
                            // Add new simulated properties
                            allCuisines = allCuisines,
                            dietaryNeeds = dietaryNeeds,
                            ambiance = ambiance,
                            budget = budget
                        )
                    }
                }
                Log.d(TAG, "Repository: fetchNearby SUCCESS. Found ${results.size} restaurants.")
                onSuccess(results)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Repository: fetchNearby FAILED.", exception)
                onFailure(exception)
            }
    }
}
