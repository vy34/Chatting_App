package com.example.chatting_app.Data

data class UserData(
    var userId:String?=null,
    var name:String?=null,
    var number:String?=null,
    var imageUrl:String?=null,
    val fcmToken: String? = "",
){
    fun toMap()= mapOf(
        "userId" to userId,
        "name" to name,
        "number" to number,
        "imageUrl" to imageUrl
    )
}

data class ChatData(
    val chatId: String?="",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()
)

data class ChatUser(
    val userId: String? = "",
    val name: String? = "",
    val number: String? = "",
    val imageUrl: String? = "",
    val fcmToken: String? = "",

)

data class Message(
    var sendBy: String?="",
    val message: String?="",
    val messageId: String? = "",
    val timeStamp: String?="",
    val imageUrl: String? = "",
    val deleted: Boolean = false
)

data class Status(
    val user : ChatUser= ChatUser(),
    val imageUrl: String?="",
    val timeStamp: Long?=null
)

data class Voice(
    val spokenText:String="",
    val isSpeaking:Boolean=false,
    val error:String?=null
)