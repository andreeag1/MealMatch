package com.mealmatch.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mealmatch.data.model.MatchSessionResponse
import com.mealmatch.data.model.MessageResponse
import com.mealmatch.data.model.WebSocketIncomingMessage
import com.mealmatch.data.model.WebSocketJoinMessage
import com.mealmatch.data.model.WebSocketSendMessage
import com.mealmatch.data.network.WebSocketClient
import com.mealmatch.data.network.repository.GroupRepository
import com.mealmatch.data.network.repository.SessionRepository
import com.mealmatch.ui.friends.ApiResult
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val groupRepository = GroupRepository()
    private val sessionRepository = SessionRepository()
    private val webSocketClient = WebSocketClient()
    private val gson = Gson()

    private val _messages = MutableLiveData<ApiResult<List<MessageResponse>>>()
    val messages: LiveData<ApiResult<List<MessageResponse>>> = _messages

    private val _newMessage = MutableLiveData<WebSocketIncomingMessage>()
    val newMessage: LiveData<WebSocketIncomingMessage> = _newMessage

    private val _createSessionResult = MutableLiveData<ApiResult<MatchSessionResponse>>()
    val createSessionResult: LiveData<ApiResult<MatchSessionResponse>> = _createSessionResult


    init {
        webSocketClient.setListener(object : WebSocketClient.AppWebSocketListener() {
            override fun onNewMessage(message: WebSocketIncomingMessage) {
                _newMessage.postValue(message)
            }
        })
    }

    fun fetchMessages(token: String, roomId: String) {
        _messages.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = groupRepository.getGroupMessages(token, roomId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _messages.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to fetch messages"
                    _messages.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _messages.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun startNewMatchSession(token: String, groupId: String) {
        _createSessionResult.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = sessionRepository.createMatchSession(token, groupId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _createSessionResult.value = ApiResult.Success(response.body()!!.data!!)
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to start session"
                    _createSessionResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _createSessionResult.value = ApiResult.Error(e.message ?: "Network request failed")
            }
        }
    }

    fun connectWebSocket(token: String, roomId: String) {
        webSocketClient.connect(token)
        val joinMessage = WebSocketJoinMessage(roomId = roomId)
        webSocketClient.sendMessage(gson.toJson(joinMessage))
    }

    fun sendMessage(roomId: String, content: String) {
        if (content.isNotBlank()) {
            val message = WebSocketSendMessage(roomId = roomId, content = content)
            webSocketClient.sendMessage(gson.toJson(message))
        }
    }

    fun disconnectWebSocket() {
        webSocketClient.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}