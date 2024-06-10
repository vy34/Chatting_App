package com.example.chatting_app

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.remember
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatting_app.Screens.ChatListScreen
import com.example.chatting_app.Screens.LoginScreen
import com.example.chatting_app.Screens.ProfileScreen
import com.example.chatting_app.Screens.SignUpScreen
import com.example.chatting_app.Screens.SingleChatScreen
import com.example.chatting_app.Screens.SingleStatusScreen
import com.example.chatting_app.Screens.StatusScreen
import com.example.chatting_app.ui.theme.Chatting_AppTheme
import dagger.hilt.android.AndroidEntryPoint


sealed class DestinationScreen(var route:String){
    object SignUp:DestinationScreen(route = "signup")
    object Login:DestinationScreen(route = "login")
    object Profile:DestinationScreen(route = "profile")
    object ChatList:DestinationScreen(route = "chatlist")
    object SingleChat:DestinationScreen(route = "singlechat/{chatId}"){
        fun createRoute(id:String)="singlechat/$id"
    }
    object StatusList:DestinationScreen(route = "statuslist")
    object SingleStatus:DestinationScreen(route = "singlestatus/{userId}"){
        fun createRoute(userId:String)="singlestatus/$userId"
    }



}
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions() // Yêu cầu quyền ghi âm
        setContent {
            Chatting_AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()


                }
            }
        }
    }
    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }


    @Composable
    fun ChatAppNavigation() {
        val navController = rememberNavController()
        var vm= hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route){

            composable(DestinationScreen.SignUp.route){
                SignUpScreen(navController,vm)
            }
            composable(DestinationScreen.Login.route){
                LoginScreen(navController = navController,vm = vm)
            }
            composable(DestinationScreen.ChatList.route){
                ChatListScreen(navController = navController,vm = vm)
            }
            composable(DestinationScreen.SingleChat.route) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                chatId?.let {
                    val context = LocalContext.current
                    val application = context.applicationContext as Application
                    val voiceToTextParser = remember { VoiceToTextParser(app = application, vm = vm) }
                    SingleChatScreen(navController = navController, vm = vm, voice = voiceToTextParser, chatId = chatId)
                }
            }
            composable(DestinationScreen.StatusList.route){
                StatusScreen(navController = navController,vm = vm )
            }
            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController = navController,vm = vm)
            }
            composable(DestinationScreen.SingleStatus.route){
                val userId = it.arguments?.getString("userId")
                userId?.let {
                SingleStatusScreen(navController = navController,vm = vm, userId = it)
                }
            }

        }
    }
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}