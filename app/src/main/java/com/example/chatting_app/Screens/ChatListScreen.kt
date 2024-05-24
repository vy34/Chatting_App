package com.example.chatting_app.Screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.chatting_app.LCViewModel

@Composable
fun ChatListScreen(navController: NavController,vm:LCViewModel) {

    BottomNavigationMenu(selectedItem = BottomNavigationItem.CHATLIST, navController = navController)
}