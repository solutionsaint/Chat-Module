package com.techlambda.chatlibrary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlambda.chatlibrary.model.Chat
import com.techlambda.chatlibrary.model.CreateChatRequest
import com.techlambda.chatlibrary.network.ApiClient
import com.techlambda.chatlibrary.model.Message
import com.techlambda.chatlibrary.network.ApiResult
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel: ViewModel() {

    private val _createChatResponse = MutableStateFlow<ApiResult<Chat?>>(ApiResult.Idle())
    val createChatResponse : StateFlow<ApiResult<Chat?>> = _createChatResponse
    private var socket: Socket? = null

    fun setupWebSocket(chatId: String, username: String, onMessageReceived: (Message) -> Unit) {
        try {
            socket = IO.socket("http://techlambda.com:9001")

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("ChatRoomActivity", "WebSocket connected successfully")
                val message = Message(chatId,username,"WebSocket connected successfully")
                onMessageReceived(message)
                socket?.emit("joinChat", chatId)
            }

            socket?.on("newMessage") { args ->
                val data = args[0] as JSONObject
                val message = Message(
                    chatId = data.getString("chatId"),
                    sender = data.getString("sender"),
                    text = data.getString("text")
                )
                Log.d("ChatRoomActivity", "New message received: ${message.text}")
                onMessageReceived(message)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args[0] as Exception
                Log.e("ChatRoomActivity", "WebSocket connection error: ${error.message}")
                val message = Message(chatId,username,"WebSocket connection error: ${error.message}")
                onMessageReceived(message)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("ChatRoomActivity", "WebSocket disconnected")
                val message = Message(chatId,username,"WebSocket disconnected")
                onMessageReceived(message)
            }

            socket?.connect()

        } catch (e: Exception) {
            Log.e("ChatRoomActivity", "Error connecting WebSocket: ${e.message}")
            val message = Message(chatId,username,"Error connecting WebSocket: ${e.message}")
            onMessageReceived(message)
        }
    }

    fun sendMessage(message: Message){
        socket?.emit("sendMessage", JSONObject().apply {
            put("chatId", message.chatId)
            put("sender", message.sender)
            put("text", message.text)
        })?.also {
            Log.d("ChatRoomActivity", "Message sent via WebSocket: ${message.text}")
        } ?: Log.e("ChatRoomActivity", "Failed to send message via WebSocket")
    }

    fun createChat(participantArray: List<String>) {
        _createChatResponse.value = ApiResult.Loading()
        viewModelScope.launch {
            val request = CreateChatRequest(participants = participantArray)
            val response = ApiClient.apiService.createChat(request)
            if (response.isSuccessful) {
                val chat = response.body()
                Log.d("ChatViewModel", "Chat created successfully: ${chat?.chatId}")
                _createChatResponse.value = ApiResult.Success(chat)
            }else{
                _createChatResponse.value = ApiResult.Error("Failed to create chat")
            }
        }
    }
}