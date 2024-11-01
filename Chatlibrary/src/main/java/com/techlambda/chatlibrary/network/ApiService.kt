package com.techlambda.chatlibrary.network

import com.techlambda.chatlibrary.model.Chat
import com.techlambda.chatlibrary.model.CreateChatRequest
import com.techlambda.chatlibrary.model.Message
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/chat/createChat")
    fun createChat(@Body request: CreateChatRequest): Response<Chat>

    @POST("/chat/send-message")
    fun sendMessage(
        @Body message: Message
    ): Call<Message>

    @GET("/chat/messages/{chatId}")
    fun getMessages(@Path("chatId") chatId: String): Call<List<Message>>
}
