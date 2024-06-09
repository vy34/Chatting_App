package com.example.chatting_app

import android.content.Context
import android.icu.util.Calendar
import android.net.Uri
import com.example.chatting_app.Data.Message
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key.Companion.U
//import androidx.compose.ui.tooling.data.EmptyGroup.data
//import androidx.compose.ui.tooling.data.EmptyGroup.data
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.chatting_app.Data.CHATS
import com.example.chatting_app.Data.ChatData
import com.example.chatting_app.Data.ChatUser
import com.example.chatting_app.Data.Event
import com.example.chatting_app.Data.MESSAGE
import com.example.chatting_app.Data.STATUS
import com.example.chatting_app.Data.Status
import com.example.chatting_app.Data.USER_NODE
import com.example.chatting_app.Data.UserData
import com.example.chatting_app.Data.Voice
import com.google.android.gms.tasks.OnCompleteListener
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

data class FCMMessage(
    val message: com.example.chatting_app.Message
)

data class Message(
    val token: String,
    val notification: Notification
)

data class Notification(
    val title: String?,
    val body: String
)

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth:FirebaseAuth,
    var db:FirebaseFirestore,
    var storage:FirebaseStorage,
    @ApplicationContext private val applicationContext: Context

) :ViewModel(){

    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState=  mutableStateOf<Event<String>?>(value = null)
    var signIn= mutableStateOf(false)
    val userData= mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val voice= mutableStateOf<Voice?>(null)
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener : ListenerRegistration?=null

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser=auth.currentUser
        signIn.value=currentUser!=null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId: String){
        inProgressChatMessage.value=true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if(error!=null){
                    handleException(error)

                }
                if (value!=null){
                    chatMessages.value=value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun depopulateMessage(){
        chatMessages.value = listOf()
        currentChatMessageListener=null
    }

    fun populateChats(){
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId",userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null){
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }
    }

    fun searchMessages(chatId: String, query: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    val filteredMessages = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.filter { message ->
                        message.message?.contains(query, ignoreCase = true) ?: false
                    }.sortedBy { it.timeStamp }

                    chatMessages.value = filteredMessages
                    inProgressChatMessage.value = false
                }
            }
    }


    fun onSendReply(context: Context,chatId: String, message: String, imageUri: Uri? = null) {
        if (message.isNotBlank() || imageUri != null) {
            if (imageUri != null) {
                uploadImage(imageUri, onSuccess = { imageUrl ->
                    sendMessage(chatId, message, imageUrl)
                }, onFailure = {
                    handleException(it, "Image upload failed")
                })
            } else {
                sendMessage(chatId, message, null)
            }
        } else {
            // Hiển thị thông báo cho người dùng
            Toast.makeText(context, "Please enter a message or select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(chatId: String, message: String, imageUrl: String?) {
        val time = Calendar.getInstance().time.toString()
        val messageId = db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGE)
            .document()
            .id
        val msg = Message(
            sendBy = userData.value?.userId,
            message = message,
            messageId = messageId,
            timeStamp = time,
            imageUrl = imageUrl,
            deleted = false
        )
        db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGE)
            .document(messageId)
            .set(msg)
        sendNotification( applicationContext,msg.message,chatId,userData.value!!.userId!!)
    }


    private fun sendNotification(context: Context, message: String?, chatId: String,currentUserId: String) {


        val chat = chats.value.find { it.chatId == chatId }


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resources = context.resources
                    val rawId = resources.getIdentifier("service_account", "raw", context.packageName)
                    val inputStream = resources.openRawResource(rawId)
                    val googleCredentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                    googleCredentials.refreshIfExpired()

                    val client = OkHttpClient()
                    val gson = Gson()

                    val name : String? = if(currentUserId == chat!!.user1.userId){
                        chat.user1.name
                    } else{
                        chat.user2.name
                    }

                    val notification = Notification(
                        title = name,
                        body = message!!
                    )

                    val token = if (currentUserId == chat!!.user1.userId) {
                        chat.user2.fcmToken
                    } else {
                        chat.user1.fcmToken
                    }

                    val fcmMessage = FCMMessage(
                        message = Message(
                            token = token!!,
                            notification = notification
                        )
                    )
                    val json = gson.toJson(fcmMessage)
                    val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/chatting-app-668f4/messages:send")
                        .addHeader("Authorization", "Bearer ${googleCredentials.accessToken.tokenValue}")
                        .post(body)
                        .build()

                    val response = client.newCall(request).execute()
                    withContext(Dispatchers.Main) {
                        if (!response.isSuccessful) {
                            Toast.makeText(context, "Token: ${chat.user1.fcmToken}", Toast.LENGTH_SHORT).show()

                            Log.d("Notification", "Unexpected code $response")
                        } else {
                            Log.d("Notification", "Notification sent successfully")
                            Log.d("Notification", "Token: ${fcmMessage.message.token}")
                            Log.d("Notification", "Title: ${notification.title}")
                            Log.d("Notification", "Body: ${notification.body}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("Notification", "Failed to send notification", e)
                    }
                }
            }


    }

    fun deleteMessage(chatId: String, messageId: String) {
        val db = FirebaseFirestore.getInstance()
        val messageRef = db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGE)
            .document(messageId)

        messageRef.update(
            mapOf(
                "message" to "",
                "imageUrl" to "",
                "deleted" to true
            )
        ).addOnSuccessListener {

        }.addOnFailureListener { e ->

        }
    }

    fun uploadImage(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
                inProcess.value = false
            }
        }.addOnFailureListener {
            onFailure(it)
            inProcess.value = false
        }
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun SignUp(context: Context, name:String, number: String, email:String, password:String){

        inProcess.value=true
        if (name.isEmpty()||number.isEmpty() || email.isEmpty()||password.isEmpty()){
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            inProcess.value=false
            return
        }
        if (!isValidEmail(email)) {
            Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
            inProcess.value = false
            return
        }
        inProcess.value=true
        db.collection(USER_NODE)
            .whereEqualTo("number",number)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty){
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                        if (it.isSuccessful){
                            createOrUpdateProfile(context,name,number)
                            signIn.value=true
                        }else{
                            val exception = it.exception
                            val errorMessage = exception?.message ?: "Failed to sign in user"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            inProcess.value = false
                            Log.e("SignUpError", "Error creating user", exception)
                        }
                    }
                }
                else{
                    Toast.makeText(context, "Number already exists", Toast.LENGTH_SHORT).show()
                    inProcess.value = false
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to check if number exists", Toast.LENGTH_SHORT).show()
                inProcess.value = false
            }
    }

    fun Login(context: Context,email: String,password: String){

        if (email.isEmpty()||password.isEmpty()){
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            inProcess.value=false
            return
        }else{
            inProcess.value=true
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    signIn.value=true
                    inProcess.value=false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                }else{
                    Toast.makeText(context, "gmail or password is incorrect", Toast.LENGTH_SHORT).show()
                    inProcess.value = false
                }
            }
        }
    }

    fun uploadProfileImage(context: Context,uri: Uri){
        uploadImage(uri) { downloadUri ->
            createOrUpdateProfile(context,imageUrl = downloadUri.toString())
        }

    }

    private fun updateStatus(userId: String,name: String?, imageUrl: String?) {
        db.collection(STATUS).whereEqualTo("user.userId", userId).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val updates = mutableMapOf<String, Any?>()
                updates["user.name"] = name
                updates["user.imageUrl"] = imageUrl
                db.collection(STATUS).document(document.id).update(updates)
            }
        }
    }

    private fun updateChat(userId: String,name: String?, imageUrl: String?) {
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userId),
                Filter.equalTo("user2.userId", userId)
            )
        ).get().addOnSuccessListener { documents ->
            for (document in documents) {
                val chatData = document.toObject<ChatData>()
                val updates = mutableMapOf<String, Any?>()
                if (chatData.user1.userId == userId) {
                    updates["user1.name"] = name
                    updates["user1.imageUrl"] = imageUrl
                } else if (chatData.user2.userId == userId) {
                    updates["user2.name"] = name
                    updates["user2.imageUrl"] = imageUrl
                }
                if (updates.isNotEmpty()) {
                    db.collection(CHATS).document(document.id).update(updates)
                }
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "Failed to update chat user image")
        }
    }

    fun uploadImage(uri:Uri,onSuccess:(Uri)->Unit) {
        inProcess.value=true
        val storageRef=storage.reference
        val uuid=UUID.randomUUID()
        val imageRef=storageRef.child("images/$uuid")
        val uploadTask=imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result=it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri)
                inProcess.value = false
            }

        }
            .addOnFailureListener{
                handleException(it)
                inProcess.value=false
            }

    }

    fun createOrUpdateProfile(context: Context,name: String?=null, number: String?=null,imageUrl:String?=null) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            val uid = auth.currentUser?.uid
            val userData = UserData(
                userId = uid,
                name = name ?: userData.value?.name,
                number = number ?: userData.value?.number,
                imageUrl = imageUrl ?: userData.value?.imageUrl,
                fcmToken = token
            )
            uid?.let {
                inProcess.value = true
                db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        //update user data
                        db.collection(USER_NODE).document(uid).update(userData.toMap())
                            .addOnSuccessListener {
                                inProcess.value = false
                                getUserData(uid)
                                updateStatus(uid, userData.name, userData.imageUrl)
                                updateChat(uid,userData.name, userData.imageUrl)
                            }.addOnFailureListener {
                                handleException(it, "Failed to update user")
                                Toast.makeText(context, "Failed to update user", Toast.LENGTH_SHORT)
                                    .show()
                                inProcess.value = false
                            }

                    } else {
                        db.collection(USER_NODE).document(uid).set(userData).addOnSuccessListener {

                            inProcess.value = false
                            getUserData(uid)
                            updateStatus(uid, userData.name, userData.imageUrl)
                            updateChat(uid,userData.name, userData.imageUrl)
                        }.addOnFailureListener { e ->
                            handleException(e, "Failed to create user")
                            Toast.makeText(context, "Failed to create user", Toast.LENGTH_SHORT).show()
                            inProcess.value = false
                        }
                    }
                }
                    .addOnFailureListener {
                        Toast.makeText(context, "Cannot retrieve user", Toast.LENGTH_SHORT).show()
                        inProcess.value = false
                    }
            }
        })
    }

    private fun getUserData(uid:String) {
        inProcess.value=true
        db.collection(USER_NODE).document(uid).addSnapshotListener{
                value,error->
            if (error!=null){
                handleException(error,"cannot retrieve user")
            }
            if (value!=null){
                var user=value.toObject<UserData>()
                userData.value=user
                inProcess.value=false
                populateChats()
                populateStatuses()
            }
        }

    }

    fun handleException(exception: Exception?=null,customMessage:String=""){
        Log.e("TAG","Live chat exception: ",exception)
        exception?.printStackTrace()
        val errorMsg=exception?.localizedMessage?:""
        val message=if (customMessage.isNullOrEmpty()) errorMsg else customMessage

        eventMutableState.value= Event(message)
        inProcess.value=false
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessage()
        currentChatMessageListener= null
        eventMutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or ! number.isDigitsOnly()) {
            handleException(customMessage = "Number must be contain digits only")
        } else {
            db.collection(CHATS).where(Filter.or(
                Filter.and(
                    Filter.equalTo("user1.number",number),
                    Filter.equalTo("user2.number", userData.value?.number)
                ),
                Filter.and(
                    Filter.equalTo("user1.number",userData.value?.number),
                    Filter.equalTo("user2.number",number )
                )
            )).get().addOnSuccessListener {
                if (it.isEmpty){
                    db.collection(USER_NODE)
                        .whereEqualTo("number",number)
                        .get()
                        .addOnSuccessListener {
                            if (it.isEmpty){
                                Toast.makeText(applicationContext, "Number not found", Toast.LENGTH_SHORT).show()
                                handleException(customMessage = "number not found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.number,
                                        userData.value?.imageUrl,
                                        userData.value?.fcmToken
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.number,
                                        chatPartner.imageUrl,
                                        chatPartner.fcmToken
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                            }
                        }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat already exist")
                }
            }
        }
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri){
        createStatus(it.toString())
        Toast.makeText(applicationContext, "Status update!", Toast.LENGTH_SHORT).show()
        }
    }

    fun createStatus(imageUrl: String?){
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.number,
                userData.value?.imageUrl,
            ),
            imageUrl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatuses(){
//        val timeDelta = 24L * 60 * 60 * 1000
//        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)

            )
        ).addSnapshotListener{
            value, error ->
            if(error!=null)
                handleException(error)
            if (value!=null){
                val currentConnections = arrayListOf(userData.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach{
                    chat ->
                    if (chat.user1.userId == userData.value?.userId){
                       currentConnections.add(chat.user2.userId)
                    }else
                        currentConnections.add(chat.user1.userId)

                }
//                    .whereGreaterThan("timeStamp", cutOff)
                db.collection(STATUS).whereIn("user.userId", currentConnections)
                    .addSnapshotListener { value, error ->
                        if( error!=null){
                            handleException(error)
                        }
                        if (value!=null){
                            status.value=value.toObjects()
                            inProgressStatus.value = false
                        }
                    }
            }
U        }
    }


}