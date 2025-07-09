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

import com.mealmatch.data.network.repository.FriendRepository
import com.mealmatch.data.model.FriendModel as Friend

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

//data class Friend(val id: String, val username: String, val email: String = "")

class FriendsViewModel : ViewModel() {
    private val groupRepository = GroupRepository()
    private val friendRepo = FriendRepository()

    private val _createGroupResult = MutableLiveData<ApiResult<Unit>>()
    val createGroupResult: LiveData<ApiResult<Unit>> = _createGroupResult

    private val _userGroupsResult = MutableLiveData<ApiResult<List<GroupResponse>>>()
    val userGroupsResult: LiveData<ApiResult<List<GroupResponse>>> = _userGroupsResult

    private val _friends = MutableLiveData<ApiResult<List<Friend>>>()
    val friends: LiveData<ApiResult<List<Friend>>> = _friends

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

    fun fetchFriends(token: String) {
        _friends.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val res = friendRepo.getFriends(token)
                if (res.isSuccessful && res.body()?.friends != null) {
                    _friends.value = ApiResult.Success(res.body()!!.friends.map {
                        Friend(_id = it._id, username = it.username, email = it.email ?: "")
                    })
                }
                else {
                    _friends.value = ApiResult.Error("Could not fetch friends")
                }
            } catch (e: Exception) {
                _friends.value = ApiResult.Error("Network error: ${e.message}")
            }
        }
    }

    fun addFriend(token: String, username: String) {
        viewModelScope.launch {
            try {
                val res = friendRepo.addFriend(token, username)
                if (res.isSuccessful) fetchFriends(token)
                else {
                    val errorMsg = res.errorBody()?.string() ?: "Failed to add friend"
                    Log.e("AddFriend", errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AddFriend", "Error", e)
            }
        }
    }

    fun removeFriend(token: String, username: String) {
        viewModelScope.launch {
            try {
                val res = friendRepo.removeFriend(token, username)
                if (res.isSuccessful) fetchFriends(token)
                else {
                    val errorMsg = res.errorBody()?.string() ?: "Failed to remove friend"
                    Log.e("RemoveFriend", errorMsg)
                }
            } catch (e: Exception) {
                Log.e("RemoveFriend", "Error", e)
            }
        }
    }
}
