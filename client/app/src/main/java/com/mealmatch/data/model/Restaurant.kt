package com.mealmatch.data.model
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.PhotoMetadata

data class Restaurant(
    val name: String,
    val primaryCuisine: String,
    val rating: Double,
    val distance: Double,
    val latLng: LatLng,
    val photoMetadata: PhotoMetadata?,
    val allCuisines: List<String> = listOf(),
    val dietaryNeeds: List<DietaryNeed> = listOf(),
    val ambiance: List<Ambiance> = listOf(),
    val budget: Budget = Budget.ANY
)