package com.techlambda.chatlibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun ChatScreen(
    userSelf: String = "user1",
    userOther: String = "user2",
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
) {
    val chatId = remember { mutableStateOf(if(isAdmin) "${userSelf}_${userOther}_Chat" else "${userOther}_${userSelf}_Chat") }
    val text = remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val viewModel: ChatViewModel = viewModel()
    val listState = rememberLazyListState()

    // Scroll to the latest message when the list of messages changes
    LaunchedEffect(messages.size) {
        if (messages.size > 1)
            listState.animateScrollToItem(messages.size - 1)
    }

    LaunchedEffect(Unit) {
        viewModel.setupWebSocket(chatId.value, userSelf) {
            messages.add(it)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        ) {
            LazyColumn(state = listState) {
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

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}

@Composable
fun ChatItem(message: Message, userSelf: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if(message.isConnectionMessage) Arrangement.Center else if (message.sender == userSelf) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    if(message.isConnectionMessage)Color(0xFFF7F9F3) else if (message.sender == userSelf) Color(0xFFDCE1BB) else Color(0xFFF3F3F3),
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = if(message.isConnectionMessage) Color.DarkGray else Color.Black,
                fontSize = 16.sp
            )
            if(!message.isConnectionMessage) {
                Text(
                    modifier = Modifier.align(Alignment.End),
                    text = message.timestamp,
                    color = Color.DarkGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
