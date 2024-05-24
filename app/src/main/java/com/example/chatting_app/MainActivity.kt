package com.example.chatting_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatting_app.Screens.ChatListScreen
import com.example.chatting_app.Screens.LoginScreen
import com.example.chatting_app.Screens.ProfileScreen
import com.example.chatting_app.Screens.SignUpScreen
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

    @Composable
    fun ChatAppNavigation() {
        val navController = rememberNavController()
        var vm= hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route){

            composable(DestinationScreen.SignUp.route){
                SignUpScreen(navController,vm)
            }
            composable(DestinationScreen.Login.route){
                LoginScreen(navController,vm)
            }
            composable(DestinationScreen.ChatList.route){
                ChatListScreen(navController,vm)
            }
            composable(DestinationScreen.StatusList.route){
                StatusScreen(navController,vm)
            }
            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController,vm)
            }

        }

    }

}

