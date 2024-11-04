package com.techlambda.chatlibrary

import android.util.Log
import androidx.lifecycle.ViewModel
import com.techlambda.chatlibrary.model.Message
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class ChatViewModel: ViewModel() {

    private var socket: Socket? = null

    fun setupWebSocket(chatId: String, username: String, onMessageReceived: (Message) -> Unit) {
        try {
            socket = IO.socket("http://techlambda.com:9001")

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("ChatRoomActivity", "WebSocket connected successfully")
                val message = Message(chatId,username,"Connected", isConnectionMessage = true)
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
                val message = Message(chatId,username,"Connection error: ${error.message}", isConnectionMessage = true)
                onMessageReceived(message)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("ChatRoomActivity", "WebSocket disconnected")
                val message = Message(chatId,username,"Connection disconnected", isConnectionMessage = true)
                onMessageReceived(message)
            }

            socket?.connect()

        } catch (e: Exception) {
            Log.e("ChatRoomActivity", "Error connecting : ${e.message}")
            val message = Message(chatId,username,"Error connecting : ${e.message}", isConnectionMessage = true)
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
}