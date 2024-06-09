package com.example.chatting_app.Screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.chatting_app.CommonDivider
import com.example.chatting_app.CommonImage
import com.example.chatting_app.Data.Message
import com.example.chatting_app.LCViewModel
import com.example.chatting_app.R
import com.example.chatting_app.VoiceToTextParser
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import kotlinx.coroutines.launch

@Composable
fun SingleChatScreen(
    navController: NavController,
    vm: LCViewModel,
    voice: VoiceToTextParser,
    chatId: String
) {

    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val voiceToTextParser = remember { voice }
    val context = LocalContext.current


    val onSendReply: (String, Uri?) -> Unit = { message, imageUri ->
        vm.onSendReply(context,chatId, message, imageUri)
        reply = ""
    }
    var chatMessage = vm.chatMessages
    val myUser = vm.userData.value
    var currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    LaunchedEffect(key1 = Unit) {
        vm.populateMessages(chatId)
    }
    BackHandler {
        vm.depopulateMessage()
    }
    // Lắng nghe kết quả từ VoiceToTextParser và cập nhật reply
    LaunchedEffect(key1 = voiceToTextParser.state.collectAsState().value) {
        voiceToTextParser.state.value?.spokenText?.let {
            reply += it
        }
    }

    val onSearch: (String) -> Unit = { query ->
        if (query.isNotBlank()) {
            vm.searchMessages(chatId, query)
        } else {
            vm.populateMessages(chatId)
        }
    }

    Column(modifier = Modifier .paint(painterResource(id = R.drawable.b4), contentScale = ContentScale.FillBounds)
    ) {
        ChatHeader(
            name = chatUser.name ?: "",
            imageUrl = chatUser.imageUrl ?: "",
            onBackClicked = {
                navController.popBackStack()
                vm.depopulateMessage()
            },
            onSearch = onSearch
        )
        BackHandler {
            vm.depopulateMessage()
            navController.popBackStack()
        }

            MessageBox(
                modifier = Modifier.weight(1f),
                chatMessages = chatMessage.value,
                currentUserId = myUser?.userId ?: "",
                onDeleteMessage = { messageId -> vm.deleteMessage(chatId, messageId) }
            )
        ReplyBox(
            reply = reply,
            onReplyChange = { reply = it },
            onSendReply = { message, imageUri -> onSendReply(message, imageUri) },
            onStartListening = { voiceToTextParser.startListening("en-US") }
        ) { voiceToTextParser.stopListening() }
    }
}



@Composable
fun MessageBox(
    modifier: Modifier,
    chatMessages: List<Message>,
    currentUserId: String,
    onDeleteMessage: (String) -> Unit
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }
    LazyColumn(modifier = modifier, state = listState) {
        items(chatMessages) { msg ->
            MessageItem(message=msg,currentUserId = currentUserId,onDeleteMessage = onDeleteMessage)
        }
    }

}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageItem(message: Message, currentUserId: String, onDeleteMessage: (String) -> Unit) {
    val isCurrentUser = message.sendBy == currentUserId
    var isMessageLongPressed by remember { mutableStateOf(false) }
    val color = if (isCurrentUser) Color(0xFF5C469C) else Color(0xFFC0C0C0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { isMessageLongPressed = true },
                    onPress = { isMessageLongPressed = false }
                )
            },

        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {

        if (isMessageLongPressed && isCurrentUser && !message.deleted) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Message",
                modifier = Modifier
                    .padding(end = 10.dp)
                    .offset(y=10.dp)
                    .clickable { onDeleteMessage(message.messageId ?: "") }
            )

        }

        if (message.deleted) {
            Text(
                text = "This message was deleted",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp
            )
        } else {
            if (!message.message.isNullOrEmpty()) {
                Text(
                    text = message.message,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!message.imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberImagePainter(data = message.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(4.dp)
                )
            }
        }

    }
}

@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClicked: () -> Unit, onSearch: (String) -> Unit) {
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .clickable { onBackClicked() }
                    .padding(8.dp)
            )
            CommonImage(
                data = imageUrl,
                modifier = Modifier
                    .padding(8.dp)
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier
                    .clickable { isSearchVisible = !isSearchVisible }
                    .padding(8.dp)
            )
        }
        if (isSearchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearch(it)
                },
                placeholder = { Text(text = "Search...") },
                shape = RoundedCornerShape(20.dp),
                modifier= Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: (String, Uri?) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showToast by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedImageUri)
                        .size(Size.ORIGINAL)
                        .scale(Scale.FIT)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.image_search_24),
                    contentDescription = "",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { launcher.launch("image/*") }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))


            OutlinedTextField(
                value = reply,
                onValueChange = onReplyChange,
                maxLines = 5,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(20.dp),
                modifier= Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier
                    .weight(0.2f)
                    .size(40.dp)
                    .clickable {
                        if (reply.isNotBlank() || selectedImageUri != null) {
                            onSendReply(reply, selectedImageUri)
                            selectedImageUri = null
                            onReplyChange("")
                            showToast = false
                        } else {
                            showToast = true
                        }
                    }
            )

        }

        if (showToast) {
            LaunchedEffect(showToast) {
                Toast.makeText(context, "Please enter a message or select an image", Toast.LENGTH_SHORT).show()
                showToast = false
            }
        }
    }
}