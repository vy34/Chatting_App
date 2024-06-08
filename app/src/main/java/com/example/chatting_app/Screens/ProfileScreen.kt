package com.example.chatting_app.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatting_app.CommonDivider
import com.example.chatting_app.CommonImage
import com.example.chatting_app.CommonProcessBar
import com.example.chatting_app.DestinationScreen
import com.example.chatting_app.LCViewModel
import com.example.chatting_app.R
import com.example.chatting_app.navigateTo

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel) {
    val inProgress=vm.inProcess.value
    val context = LocalContext.current
    if (inProgress){
        CommonProcessBar()
    }else{
        val userData = vm.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.name?:"")
        }
        var number by rememberSaveable {
            mutableStateOf(userData?.number?:"")
        }
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
                    .paint(
                        painterResource(id = R.drawable.b4),
                        contentScale = ContentScale.FillBounds
                    )
                ,
                vm=vm,
                name = name,
                number = number,
                onNameChange = {name = it},
                onNumberChange = {number = it},
                onBack = {
                         navigateTo(navController = navController, route = DestinationScreen.ChatList.route)
                },
                onSave = {
                         vm.createOrUpdateProfile(context,name = name, number = number)
                },
                onLogOut = {
                    vm.logout()
                    navigateTo(navController = navController, route = DestinationScreen.Login.route)
                }
            )
            BottomNavigationMenu(selectedItem = BottomNavigationItem.PROFILE, navController = navController)

        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    vm: LCViewModel,
    modifier: Modifier,
    name: String,
    number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogOut: () -> Unit
) {
    Column(modifier = modifier) {
        val imageUrl = vm.userData.value?.imageUrl
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription =null,modifier=Modifier.clickable { onBack.invoke() } )
            Icon(Icons.Default.SaveAlt, contentDescription = null,modifier=Modifier.clickable { onSave.invoke() })
        }

            ProfileImage(imageUrl = imageUrl, vm = vm)


        Spacer(modifier = Modifier.padding(20.dp))
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Name", modifier = Modifier.width(100.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier= Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Number", modifier = Modifier.width(100.dp))
                OutlinedTextField(
                    value = number,
                    onValueChange = onNumberChange,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier= Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }
        }



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Logout, contentDescription =null , modifier = Modifier.clickable { onLogOut.invoke() })
            Text(text = "Log Out", modifier = Modifier.clickable { onLogOut.invoke() })
        }
    }
}

@Composable
fun ProfileImage(imageUrl:String?,vm:LCViewModel) {
    val context = LocalContext.current
    val launcher= rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        uri ->
        uri?.let {
            vm.uploadProfileImage(context,uri)
        }
    }
    Box (modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)){
        Column(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .clickable {
                launcher.launch("image/*")
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card (shape = CircleShape, modifier = Modifier
                .padding(8.dp)
                .size(100.dp)){
                CommonImage(data = imageUrl)
            }
//            Text(text = "Change Prifile Picture")
        }
        val isLoading=vm.inProcess.value
        if (isLoading){
            CommonProcessBar()
        }

    }

}
