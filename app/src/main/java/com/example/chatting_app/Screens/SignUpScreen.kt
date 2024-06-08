package com.example.chatting_app.Screens

import android.app.LocaleManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatting_app.CheckSignedIn
import com.example.chatting_app.CommonProcessBar
import com.example.chatting_app.DestinationScreen
import com.example.chatting_app.LCViewModel
import com.example.chatting_app.R
import com.example.chatting_app.navigateTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, vm: LCViewModel) {

    CheckSignedIn(vm = vm, navController = navController)
    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxSize()
        .paint(painterResource(id = R.drawable.b1), contentScale = ContentScale.FillBounds)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val nameState = remember { mutableStateOf(TextFieldValue()) }
            val numberState = remember { mutableStateOf(TextFieldValue()) }
            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passwordState = remember { mutableStateOf(TextFieldValue()) }

            val local = LocalFocusManager.current

            Image(
                painter = painterResource(id = R.drawable.b2),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
            )
            Text(
                text = "Sign Up",
                fontSize = 35.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(5.dp)
            )
            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "",
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                label = { Text(text = "Name") },
                modifier = Modifier.padding(8.dp)
            )
            OutlinedTextField(
                value = numberState.value,
                onValueChange = { numberState.value = it },
                label = { Text(text = "Phone Number") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.telephone),
                        contentDescription = "",
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.padding(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    capitalization = KeyboardCapitalization.None
                )
            )
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
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.padding(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    autoCorrect = false
                )
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
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
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
                modifier = Modifier.padding(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Button(onClick = {
                vm.SignUp(context,
                    nameState.value.text,
                    numberState.value.text,
                    emailState.value.text,
                    passwordState.value.text,

                    )
            },
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF5C469C))
            ) {
                Text(text = "Sign Up")

            }
            Row {
                Text(text = "Already a user ? Go to ")
                Text(
                    text = "Login",
                    color = Color(0xFF5C469C),
                    modifier = Modifier.clickable {
                        navigateTo(navController, DestinationScreen.Login.route)
                    })
            }
        }
    }

    if (vm.inProcess.value) {
        CommonProcessBar()

    }
}
