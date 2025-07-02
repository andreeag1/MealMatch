package com.mealmatch.data.network

import android.util.Log
import com.google.gson.Gson
import com.mealmatch.data.model.WebSocketIncomingMessage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    private var listener: WebSocketListener? = null

    fun setListener(webSocketListener: WebSocketListener) {
        this.listener = webSocketListener
    }

    fun connect(token: String) {
        if (webSocket != null) return // Already connected

        val request = Request.Builder()
            .url("ws://10.0.2.2:3000/ws?token=$token")
            .build()

        webSocket = client.newWebSocket(request, listener!!)
    }

    fun sendMessage(messageJson: String) {
        webSocket?.send(messageJson)
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    abstract class AppWebSocketListener : WebSocketListener() {
        private val gson = Gson()

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocketClient", "Connection opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val message = gson.fromJson(text, WebSocketIncomingMessage::class.java)
                onNewMessage(message)
            } catch (e: Exception) {
                Log.e("WebSocketClient", "Error parsing message: $text", e)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocketClient", "Connection closing: $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocketClient", "Connection failure", t)
        }

        abstract fun onNewMessage(message: WebSocketIncomingMessage)
    }
}
