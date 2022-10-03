package com.gruppe6.fix2uni

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gruppe6.fix2uni.ui.theme.Blue09
import com.gruppe6.fix2uni.ui.theme.Fix2UniTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// login screen for the different roles
@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current

    Fix2UniTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(
                modifier = Modifier.padding(20.dp, 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Anmeldung", fontSize = 23.sp)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var usernameState by remember { mutableStateOf("") }
                    var passwordState by remember { mutableStateOf("") }
                    var passwordVisibleState by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = usernameState,
                        onValueChange = { usernameState = it },
                        placeholder = { Text("Nutzername") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.padding(vertical = 15.dp))
                    OutlinedTextField(
                        value = passwordState,
                        onValueChange = { passwordState = it },
                        placeholder = { Text("Kennwort") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { passwordVisibleState=!passwordVisibleState }) {
                            if (passwordVisibleState) {
                                Icon(painter = painterResource(id =  R.drawable.ic_baseline_visibility_24) , contentDescription = null)}

                            else Icon(painter = painterResource(id =  R.drawable.ic_baseline_visibility_off_24) , contentDescription = null)}

                        },
                        visualTransformation = if(passwordVisibleState) VisualTransformation.None else PasswordVisualTransformation()
                    )
                    // Login button
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Blue09),
                        onClick = {
                            val restApi = RetrofitInstance.getInstance().create(RestAPI::class.java)
                            val user = User(usernameState, passwordState)
                            var loginSuccessful = false

                            // Launching the auth coroutine
                            GlobalScope.launch(Dispatchers.IO) {
                                val result = restApi.authenticateUser(user)
                                if (result.isSuccessful){
                                    if (result.body()?.result.equals("Success")){
                                        loginSuccessful = true

                                        // Set user properties
                                        result.body()?.let { SessionManager.setUsername(it.username) }
                                        result.body()?.let { SessionManager.setEmail(it.email) }
                                        result.body()?.let {
                                            if (result.body()?.isAdmin == true) SessionManager.setRole("Admin")
                                            else SessionManager.setRole("Student")
                                        }

                                        // Request and save user token
                                        val tokenResult = restApi.requestToken(user)
                                        val token = tokenResult.body()?.token
                                        val sessionManager = SessionManager
                                        if (token != null) {
                                            sessionManager.setToken("Token $token")
                                        }
                                        if (loginSuccessful){
                                            GlobalScope.launch(Dispatchers.Main) {
                                                navController.navigate("registered_screen")
                                            }
                                        }
                                        else{
                                            GlobalScope.launch(Dispatchers.Main){
                                                Toast.makeText(context, "Anmeldung nicht erfolgreich", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        shape = CircleShape
                    ) {
                        Text(text = "Anmelden", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }}



