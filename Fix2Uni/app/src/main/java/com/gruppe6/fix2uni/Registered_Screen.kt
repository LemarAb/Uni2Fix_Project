package com.gruppe6.fix2uni

import com.gruppe6.fix2uni.ui.theme.Fix2UniTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gruppe6.fix2uni.ui.theme.Blue09
import com.gruppe6.fix2uni.ui.theme.GrayB8

// display the user data after successful login
@Composable
fun RegisteredScreen(navController: NavController) {
    Fix2UniTheme {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.padding(20.dp, 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Konto", fontSize = 23.sp)
            Column() {
                Row(Modifier.padding(0.dp, 25.dp)) {
                    Text(buildAnnotatedString {
                        append("Name:\r\n")
                        withStyle(style = SpanStyle(color = GrayB8)) {
                            append("${SessionManager.getUsername()}")
                        }
                    })
                }
                Divider(thickness = 3.dp)
                Row(Modifier.padding(0.dp, 15.dp)) {
                    Text(buildAnnotatedString {
                        append("E-Mail:\r\n")
                        withStyle(style = SpanStyle(color = GrayB8)) {
                            append("${SessionManager.getEmail()}")
                        }
                    })
                }
                Divider(thickness = 3.dp)
                Row(Modifier.padding(0.dp, 15.dp)) {
                    Text(buildAnnotatedString {
                        append("Rolle:\r\n")
                        withStyle(style = SpanStyle(color = GrayB8)) {
                            append("${SessionManager.getRole()}")
                        }
                    })
                }
            }
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Blue09),
                onClick = {
                    // Reset the current token
                    SessionManager.setToken("")
                    navController.navigate("login_screen") },
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                shape = CircleShape,
            ) {
                Text(text = "Abmelden", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}}

