package com.example.chatting_app.Screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatting_app.DestinationScreen
import com.example.chatting_app.R
import com.example.chatting_app.R.color.primColor
import com.example.chatting_app.navigateTo
import com.example.chatting_app.ui.theme.primaColor


enum class BottomNavigationItem(val icon:Int,val navDestination:DestinationScreen){
    CHATLIST(R.drawable.comment,DestinationScreen.ChatList),
    STATUSLIST(R.drawable.refresh,DestinationScreen.StatusList),
    PROFILE(R.drawable.user,DestinationScreen.Profile)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BottomNavigationMenu(selectedItem:BottomNavigationItem,navController: NavController) {

    Row (modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
        .background(color = Color(179, 153, 212)),
        verticalAlignment = Alignment.CenterVertically

    ){
        for (item in BottomNavigationItem.values()){
            Image(painter = painterResource(id =item.icon),
                contentDescription =null,
                modifier = Modifier
                    .size(45.dp)
                    .padding(6.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        navigateTo(navController, item.navDestination.route)
                    },
                colorFilter = if (item==selectedItem)
                ColorFilter.tint(color = primaColor)
                else
                ColorFilter.tint(color = Color.White)
            )
        }

    }

}