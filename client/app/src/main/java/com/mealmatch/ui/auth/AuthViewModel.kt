package com.mealmatch.ui.auth


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmatch.data.model.AuthResponse
import com.mealmatch.data.model.LoginRequest
import com.mealmatch.data.model.RegisterRequest
import com.mealmatch.data.network.repository.AuthRepository
import com.mealmatch.ui.friends.ApiResult
import kotlinx.coroutines.launch
import com.mealmatch.data.model.UserViewModel

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authResult = MutableLiveData<ApiResult<AuthResponse>>()
    val authResult: LiveData<ApiResult<AuthResponse>> = _authResult

    fun login(email: String, password: String) {
        _authResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    UserViewModel.email = email

                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data?.token != null) {
                        _authResult.value = ApiResult.Success(AuthResponse(apiResponse.data.token, apiResponse.data.username))
                    } else {
                        _authResult.value = ApiResult.Error(apiResponse?.message ?: "Login failed")
                    }
                } else {
                    _authResult.value = ApiResult.Error(response.errorBody()?.string() ?: "An error occurred")
                }
            } catch (e: Exception) {
                _authResult.value = ApiResult.Error(e.message ?: "Network error")
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        _authResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    UserViewModel.username = username
                    UserViewModel.email = email

                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.token != null) {
                        _authResult.value = ApiResult.Success(AuthResponse(apiResponse.token, username))
                    } else {
                        _authResult.value = ApiResult.Error(apiResponse?.message ?: "Sign up failed")
                    }
                } else {
                    _authResult.value = ApiResult.Error(response.errorBody()?.string() ?: "An error occurred")
                }
            } catch (e: Exception) {
                _authResult.value = ApiResult.Error(e.message ?: "Network error")
            }
        }
    }
}