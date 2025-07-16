package com.mealmatch.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModelFactory(private val repository: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}