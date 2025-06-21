package com.mealmatch.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.CreateGroupResponse
import com.mealmatch.data.model.GetGroupMessagesResponse
import com.mealmatch.data.network.repository.GroupRepository
import kotlinx.coroutines.launch

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

class FriendsViewModel : ViewModel() {
    private val groupRepository = GroupRepository()

    private val _createGroupResult = MutableLiveData<ApiResult<CreateGroupResponse>>()
    val createGroupResult: LiveData<ApiResult<CreateGroupResponse>> = _createGroupResult

    private val _groupMessagesResult = MutableLiveData<ApiResult<List<GetGroupMessagesResponse>>>()
    val groupMessagesResult: LiveData<ApiResult<List<GetGroupMessagesResponse>>> = _groupMessagesResult

    fun createGroup(token: String, groupName: String, memberUsernames: List<String>) {
        _createGroupResult.value = ApiResult.Loading

        val request = CreateGroupRequest(name = groupName, members = memberUsernames)

        viewModelScope.launch {
            try {
                val response = groupRepository.createGroup(token, request)
                if (response.isSuccessful) {
                    _createGroupResult.value = ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _createGroupResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _createGroupResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun getGroupMessages(token: String, roomId: String) {
        _groupMessagesResult.value = ApiResult.Loading

        viewModelScope.launch {
            try {
                val response = groupRepository.getGroupMessages(token, roomId)
                if (response.isSuccessful) {
                    _groupMessagesResult.value = ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _groupMessagesResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _groupMessagesResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }
}