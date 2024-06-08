package com.example.chatting_app.Screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatting_app.DestinationScreen
import com.example.chatting_app.R
import com.example.chatting_app.navigateTo
import com.example.chatting_app.ui.theme.primaColor
import com.example.chatting_app.ui.theme.white
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius
import com.google.android.play.integrity.internal.w

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
        .background(Color.White)
        .wrapContentHeight()
        .clip(RoundedCornerShape(10.dp))
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
                ColorFilter.tint(color = Color.Gray)
            )
        }

    }

}