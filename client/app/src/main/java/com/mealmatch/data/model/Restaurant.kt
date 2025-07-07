package com.mealmatch.data.model
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.PhotoMetadata

data class Restaurant (
    val name: String,
    val cuisine: String,
    val rating: Double,
    val distance: Double,
    val photoMetadata: PhotoMetadata?,
    val latLng: LatLng
)