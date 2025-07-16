package com.mealmatch.ui.map

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.mealmatch.data.model.Restaurant
import android.util.Log

class RestaurantRepository(private val placesClient: PlacesClient) {
    private val TAG = "MapDebug"

    /**
     * Fetches nearby restaurants using a callback pattern.
     * @param onSuccess A function to be called with the list of restaurants on success.
     * @param onFailure A function to be called with an exception on failure.
     */
    // No textQuery parameter needed here anymore
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

        // The request is simple again
        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setExcludedPrimaryTypes(listOf("lodging"))
            .setMaxResultCount(20)
            .build()


        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                val results = response.places.mapNotNull { place ->
                    place.latLng?.let { latLng ->
                        val rawType = place.placeTypes?.firstOrNull()?.toString().orEmpty()
                        val cuisine = rawType.replace("_", " ").split(" ")
                            .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

                        val restaurantLocation = Location("").apply {
                            latitude = latLng.latitude
                            longitude = latLng.longitude
                        }
                        val distanceInKm = origin.distanceTo(restaurantLocation) / 1000.0

                        Restaurant(
                            name = place.name ?: "Unnamed Restaurant",
                            cuisine = cuisine,
                            rating = place.rating ?: 0.0,
                            distance = distanceInKm,
                            latLng = latLng,
                            photoMetadata = place.photoMetadatas?.firstOrNull()
                        )
                    }
                }
                // Call the success callback with the results
                Log.d(TAG, "Repository: fetchNearby SUCCESS. Found ${results.size} restaurants.")
                onSuccess(results)
            }
            .addOnFailureListener { exception ->
                // Call the failure callback with the exception
                Log.e(TAG, "Repository: fetchNearby FAILED.", exception)
                onFailure(exception)
            }
    }
}