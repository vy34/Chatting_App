package com.example.chatting_app

import android.icu.util.Calendar
import android.net.Uri
import com.example.chatting_app.Data.Message
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.chatting_app.Data.CHATS
import com.example.chatting_app.Data.ChatData
import com.example.chatting_app.Data.ChatUser
import com.example.chatting_app.Data.Event
import com.example.chatting_app.Data.MESSAGE
import com.example.chatting_app.Data.USER_NODE
import com.example.chatting_app.Data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth:FirebaseAuth,
    var db:FirebaseFirestore,
    var storage:FirebaseStorage
) :ViewModel(){

    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState=  mutableStateOf<Event<String>?>(value = null)
    var signIn= mutableStateOf(false)
    var userData= mutableStateOf<UserData?>(null)
    var chats = mutableStateOf<List<ChatData>>(listOf())

    init {
        val currentUser=auth.currentUser
        signIn.value=currentUser!=null
        currentUser?.uid?.let {
            getUserData(it)
        }
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

    fun onSendReply(chatID: String, message: String){
        val time = Calendar.getInstance().time.toString()
        val msg =  Message(userData.value?.userId, message, time)
        db.collection(CHATS)
            .document(chatID)
            .collection(MESSAGE)
            .document()
            .set(msg)
    }

    fun SignUp(name:String,number: String,email:String,password:String){
        inProcess.value=true
        if (name.isEmpty()||number.isEmpty() || email.isEmpty()||password.isEmpty()){
            handleException(customMessage = "Please fill all fields")
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
                            createOrUpdateProfile(name,number)
                            signIn.value=true
                        }else{

                        }
                    }
                }
                else{
                    handleException(customMessage = "number already exist")
                    inProcess.value=false
                }
            }


    }

    fun Login(email: String,password: String){

        if (email.isEmpty()||password.isEmpty()){
            handleException(customMessage = "Please fill all fields")
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

                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri){
        uploadImage(uri) { downloadUri ->
            createOrUpdateProfile(imageUrl = downloadUri.toString())
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
                inProcess.value = false // Giải phóng trạng thái xử lý khi thành công
            }

        }
            .addOnFailureListener{
                handleException(it)
                inProcess.value=false
            }

    }

    fun createOrUpdateProfile(name: String?=null, number: String?=null,imageUrl:String?=null) {
        var uid=auth.currentUser?.uid
        val userData=UserData(
            userId = uid,
            name=name?:userData.value?.name,
            number=number?:userData.value?.number,
            imageUrl=imageUrl?:userData.value?.imageUrl
        )
        uid?.let {
            inProcess.value=true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (it.exists()){
                    //update user data
                    db.collection(USER_NODE).document(uid).update(userData.toMap())
                        .addOnSuccessListener {
                            inProcess.value = false
                            getUserData(uid)
                        }.addOnFailureListener {
                            handleException(it, "Failed to update user")
                        }

                }else{
                    db.collection(USER_NODE).document(uid).set(userData)
                    inProcess.value=false
                    getUserData(uid)
                }
            }
                .addOnFailureListener{
                    handleException(it,"cannot retrieve user")
                }
        }
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
                                        userData.value?.imageUrl
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.number,
                                        chatPartner.imageUrl
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

}