package com.example.chatting_app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun colorResource(id: Int): Color {
    val context = LocalContext.current
    val colorInt = ContextCompat.getColor(context, id)
    return Color(colorInt)
}

val primaColor = Color(0xFF5C469C)
val white = Color(0xFFFFFFFF)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)