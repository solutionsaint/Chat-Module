package com.techlambda.chatlibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techlambda.chatlibrary.model.Message
import com.techlambda.chatlibrary.network.ApiResult

@Composable
fun ChatScreen(
    userSelf: String = "user2",
    userOther: String = "user2",
    modifier: Modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
) {
    val chatId = remember { mutableStateOf("") }
    val chatIdError = remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val viewModel: ChatViewModel = viewModel()

    val createChatResponse by viewModel.createChatResponse.collectAsState()
    LaunchedEffect(createChatResponse) {
        when (createChatResponse) {
            is ApiResult.Success -> {
                chatIdError.value = false
                chatId.value = (createChatResponse as ApiResult.Success).data?.chatId ?: ""
                chatIdError.value = false
                viewModel.createChat(listOf(userSelf, userOther))
                viewModel.setupWebSocket(chatId.value, userSelf) {
                    messages.add(it)
                }
            }

            is ApiResult.Error -> {
                chatIdError.value = true
            }

            else -> Unit
        }
    }

    fun createChatId() {
        chatIdError.value = false
        viewModel.createChat(listOf(userSelf, userOther))
    }

    LaunchedEffect(Unit) {
        //TODO uncomment and make the api call working
        //createChatId()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (chatIdError.value) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Something went wrong...",
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier.padding(8.dp)
                )
                Button(onClick = {
                    createChatId()
                }) {
                    Text("Try Again")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp)
            ) {
                LazyColumn {
                    items(messages.size) { index ->
                        val message = messages[index]
                        ChatItem(message = message, userSelf = userSelf)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.Gray)
                        .padding(16.dp),
                    textStyle = TextStyle(fontSize = 18.sp),
                    decorationBox = { innerTextField ->
                        if (text.value.isEmpty()) {
                            Text(
                                text = "Enter message",
                                color = Color.Gray,
                                style = TextStyle(fontSize = 18.sp)
                            )
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val msg = Message(chatId.value, userSelf, text.value)
                    viewModel.sendMessage(msg)
                    text.value = ""
                }) {
                    Text("Send")
                }
            }
        }
    }

    if(createChatResponse is ApiResult.Loading){
        CircularProgressIndicator()
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}

@Composable
fun ChatItem(message: Message, userSelf: String){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (message.sender == userSelf) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (message.sender == userSelf) Color(0xFFDCF8C6) else Color(0xFFFFFFFF), // Sent messages green, received messages white
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
                .widthIn(max = 240.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.sender == userSelf) Color.Black else Color.DarkGray,
                fontSize = 16.sp
            )
        }
    }
}
