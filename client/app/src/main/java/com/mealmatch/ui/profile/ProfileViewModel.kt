package com.mealmatch.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmatch.data.model.UserPreferences
import com.mealmatch.data.model.UserProfileResponse
import com.mealmatch.data.network.repository.ProfilePrefRepository
import com.mealmatch.ui.friends.ApiResult
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfilePrefRepository()

    private val _userProfileResult = MutableLiveData<ApiResult<UserProfileResponse>>()
    val userProfileResult: LiveData<ApiResult<UserProfileResponse>> = _userProfileResult

    private val _preferencesResult = MutableLiveData<ApiResult<UserPreferences>>()
    val preferencesResult: LiveData<ApiResult<UserPreferences>> = _preferencesResult

    private val _updatePreferencesResult = MutableLiveData<ApiResult<Unit>>()
    val updatePreferencesResult: LiveData<ApiResult<Unit>> = _updatePreferencesResult

    fun fetchUserProfile(token: String) {
        _userProfileResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getMyProfile(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _userProfileResult.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to fetch profile"
                    _userProfileResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Network error fetching profile", e)
                _userProfileResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun fetchPreferences(token: String) {
        _preferencesResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.getProfilePref(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _preferencesResult.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to fetch preferences"
                    _preferencesResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Network error fetching preferences", e)
                _preferencesResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun updatePreferences(token: String, preferences: UserPreferences) {
        _updatePreferencesResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = repository.setProfilePref(token, preferences)
                if (response.isSuccessful && response.body()?.success == true) {
                    _updatePreferencesResult.value = ApiResult.Success(Unit)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to update preferences"
                    _updatePreferencesResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Network error updating preferences", e)
                _updatePreferencesResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }
}