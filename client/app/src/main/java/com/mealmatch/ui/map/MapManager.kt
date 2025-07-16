package com.mealmatch.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mealmatch.data.model.Restaurant

class MapManager(
    private val context: Context,
    private val map: GoogleMap,
    private val listener: MapManagerListener
) {
    // A toggle to enable/disable fetching restaurants when the camera moves
    var isFetchOnCameraMoveEnabled = false

    interface MapManagerListener {
        fun onCameraIdle(target: LatLng)
        fun onMarkerClicked(restaurant: Restaurant): Boolean
    }

    init {
        setupMap()
    }

    private fun setupMap() {
        map.setOnCameraIdleListener {
            if (isFetchOnCameraMoveEnabled) {
                listener.onCameraIdle(map.cameraPosition.target)
            }
        }
        map.setOnMarkerClickListener { marker ->
            (marker.tag as? Restaurant)?.let {
                return@setOnMarkerClickListener listener.onMarkerClicked(it)
            }
            false
        }
    }

    fun enableMyLocation(permissionLauncher: ActivityResultLauncher<String>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun moveCamera(target: LatLng, zoom: Float = 14f) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }

    fun animateCamera(target: LatLng, zoom: Float = 13f) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }

    fun updateMarkers(restaurants: List<Restaurant>) {
        map.clear()
        restaurants.forEach { restaurant ->
            val marker = map.addMarker(
                MarkerOptions().position(restaurant.latLng).title(restaurant.name)
            )
            marker?.tag = restaurant
        }
    }
}