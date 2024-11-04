package com.techlambda.chatlibrary.model

import com.techlambda.chatlibrary.convertMillSecondsToDateTimeString

data class Message(
    val chatId: String,
    val sender: String,
    val text: String,
    val isConnectionMessage: Boolean = false,
    val timestamp: String = convertMillSecondsToDateTimeString(System.currentTimeMillis())
)
