package com.example.chatting_app.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatting_app.CheckSignedIn
import com.example.chatting_app.CommonProcessBar
import com.example.chatting_app.DestinationScreen
import com.example.chatting_app.LCViewModel
import com.example.chatting_app.R
import com.example.chatting_app.navigateTo


@Composable
fun LoginScreen(navController: NavController, vm: LCViewModel) {
    val context = LocalContext.current
    CheckSignedIn(vm = vm, navController = navController)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
//                .wrapContentHeight()
//                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passwordState = remember { mutableStateOf(TextFieldValue()) }

            val local = LocalFocusManager.current

            Image(
                painter = painterResource(id = R.drawable.login_image),
                contentDescription = null,
                modifier = Modifier
                    .size(260.dp)
//                    .padding(top = 16.dp)
//                    .padding(8.dp)
            )
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Login to your account")
            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = "Sign In",
//                fontSize = 35.sp,
//                fontFamily = FontFamily.SansSerif,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(8.dp),
//                color = Color.Black
//            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text(text = "Email") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.arroba),
                        contentDescription = "",
                        modifier = Modifier.size(25.dp)
                    )
                },
                modifier = Modifier.padding(8.dp)
            )
            val passwordVisible = remember {
                mutableStateOf(false)
            }
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                label = { Text(text = "Password") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.padlock),
                        contentDescription = "",
                        modifier = Modifier.size(25.dp)
                    )
                },
                trailingIcon = {
                    val iconImage = if (passwordVisible.value) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }
                    var description = if (passwordVisible.value) {
                        "Hide password"
                    } else {
                        "Show password"
                    }
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(imageVector = iconImage, contentDescription = description)
                    }
                },
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.padding(8.dp)
            )
            Button(
                onClick = {
                    vm.Login(
                        context,
                        emailState.value.text,
                        passwordState.value.text
                    )
                },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF5C469C))
            ) {
                Text(text = "Sign In")
            }
            Row {
                Text(text = "New user? Go to ")

                Text(
                    text = "Sign Up",
                    color = Color(0xFF5C469C),
                    modifier = Modifier.clickable {
                        navigateTo(navController, DestinationScreen.SignUp.route)
                    }
                )
            }
            Spacer(modifier = Modifier.height(26.dp))
            Text(text = "Or sign in with")
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_facebook),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(45.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.gmail_logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

        }

    }
    if (vm.inProcess.value) {
        CommonProcessBar()

    }

}

