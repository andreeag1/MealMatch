package com.mealmatch.ui.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mealmatch.data.model.CreateGroupRequest
import com.mealmatch.data.model.GroupResponse
import com.mealmatch.data.network.repository.GroupRepository
import kotlinx.coroutines.launch

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

class FriendsViewModel : ViewModel() {
    private val groupRepository = GroupRepository()

    private val _createGroupResult = MutableLiveData<ApiResult<Unit>>()
    val createGroupResult: LiveData<ApiResult<Unit>> = _createGroupResult

    private val _userGroupsResult = MutableLiveData<ApiResult<List<GroupResponse>>>()
    val userGroupsResult: LiveData<ApiResult<List<GroupResponse>>> = _userGroupsResult

    fun createGroup(token: String, groupName: String, memberUsernames: List<String>) {
        _createGroupResult.value = ApiResult.Loading
        val request = CreateGroupRequest(name = groupName, members = memberUsernames)
        viewModelScope.launch {
            try {
                val response = groupRepository.createGroup(token, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _createGroupResult.value = ApiResult.Success(Unit)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _createGroupResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Create group API call failed", e)
                _createGroupResult.value = ApiResult.Error(e.message ?: "Network request failed")

            }
        }
    }

    fun getUserGroups(token: String) {
        _userGroupsResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = groupRepository.getUserGroups(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _userGroupsResult.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    _userGroupsResult.value = ApiResult.Error(response.body()?.message ?: "Failed to fetch groups")
                }
            } catch (e: Exception) {
                _userGroupsResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }
}