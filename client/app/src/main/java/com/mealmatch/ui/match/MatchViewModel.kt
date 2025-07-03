package com.mealmatch.ui.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmatch.data.model.MatchSessionResponse
import com.mealmatch.data.model.SubmitSwipesRequest
import com.mealmatch.data.model.Swipe
import com.mealmatch.data.network.repository.SessionRepository
import com.mealmatch.ui.friends.ApiResult
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {
    private val sessionRepository = SessionRepository()

    private val _sessionResult = MutableLiveData<ApiResult<MatchSessionResponse>>()
    val sessionResult: LiveData<ApiResult<MatchSessionResponse>> = _sessionResult

    private val _submitSwipesResult = MutableLiveData<ApiResult<Unit>>()
    val submitSwipesResult: LiveData<ApiResult<Unit>> = _submitSwipesResult

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
}