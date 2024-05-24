package com.example.chatting_app.Screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.chatting_app.LCViewModel

@Composable
fun StatusScreen(navController: NavController, vm: LCViewModel) {
    Text(text = "status")
    BottomNavigationMenu(selectedItem = BottomNavigationItem.STATUSLIST, navController = navController)
}