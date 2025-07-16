package com.mealmatch.ui.match

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.*
import com.mealmatch.BuildConfig
import com.mealmatch.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class MatchViewModel (application: Application) : AndroidViewModel(application) {
    private val sessionRepository = SessionRepository()
    private val placesClient: PlacesClient

    private val _sessionResult = MutableLiveData<ApiResult<MatchSessionResponse>>()
    val sessionResult: LiveData<ApiResult<MatchSessionResponse>> = _sessionResult

    private val _submitSwipesResult = MutableLiveData<ApiResult<Unit>>()
    val submitSwipesResult: LiveData<ApiResult<Unit>> = _submitSwipesResult

    private val _matchResult = MutableLiveData<ApiResult<MatchResultResponse>>()
    val matchResult: LiveData<ApiResult<MatchResultResponse>> = _matchResult


    private val _restaurants          = MutableLiveData<List<Restaurant>>()
    val    restaurants : LiveData<List<Restaurant>>               = _restaurants

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

    fun fetchNearbyRestaurants(location: android.location.Location) {
        viewModelScope.launch(Dispatchers.IO) {
            val placeFields = listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.TYPES,
                com.google.android.libraries.places.api.model.Place.Field.RATING,
                com.google.android.libraries.places.api.model.Place.Field.PHOTO_METADATAS,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            )
            val center = com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)
            val circle = com.google.android.libraries.places.api.model.CircularBounds.newInstance(center, 1500.0)
            val request = com.google.android.libraries.places.api.net.SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedPrimaryTypes(listOf("restaurant"))
                .setExcludedPrimaryTypes(listOf("lodging"))
                .setMaxResultCount(20)
                .build()

            placesClient.searchNearby(request)
                .addOnSuccessListener { resp ->
                    val results = resp.places.map { p ->
                        val cuisine = p.placeTypes?.firstOrNull()?.toString()?.replace("_", " ") ?: "Unknown"
                        Restaurant(
                            name = p.name ?: "Unnamed",
                            cuisine = cuisine,
                            rating = p.rating ?: 0.0,
                            distance = 0.0, // You can calculate distance if needed
                            latLng = p.latLng!!,
                            photoMetadata = p.photoMetadatas?.firstOrNull()
                        )
                    }
                    _restaurants.postValue(results)
                }
                .addOnFailureListener { it.printStackTrace() }
        }
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
                cuisine = place.types?.firstOrNull()?.name
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