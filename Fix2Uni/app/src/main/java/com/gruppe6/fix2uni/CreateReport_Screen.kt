package com.gruppe6.fix2uni

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gruppe6.fix2uni.ui.theme.Blue09
import com.gruppe6.fix2uni.ui.theme.Fix2UniTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

}

// create a report
@Composable
fun CreateReport(navController: NavController, _reportBuildingRoomState: String = "") {
    // Report data variables
    //var reportIdState by remember { mutableStateOf("") }
    var reportTitle by remember { mutableStateOf("") }
    var reportDateState by remember { mutableStateOf("") }
    var reportBuildingRoomState by remember { mutableStateOf("") }
    var reportTextState by remember { mutableStateOf("") }
    var reportImgBase64String by remember { mutableStateOf("") }

    // Empty strings for testing, will be cleared when all the fields are done on the Create Report Screen
    reportTitle = ""
    reportDateState = ""
    reportBuildingRoomState = _reportBuildingRoomState
    reportTextState = ""
    reportImgBase64String = ""

    val context = LocalContext.current
    var expanded by remember {
        mutableStateOf(false)
    }

    // For image upload from device
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val bitmap =  remember {
        mutableStateOf<Bitmap?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Fix2UniTheme {

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Row(modifier = Modifier
                .padding(top = 8.dp)) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Image(
                        painterResource(R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = "back"
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(20.dp, 15.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Meldung erstellen", fontSize = 23.sp)
                Row(
                    Modifier
                        .padding(top = 30.dp)
                        .padding(0.dp, 15.dp)
                ) {
                    OutlinedTextField(
                        value = reportTitle,
                        onValueChange = { reportTitle = it },
                        shape = RoundedCornerShape(10),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Titel"
                            )
                        })
                }
                Row(Modifier.padding(0.dp, 15.dp)) {
                    OutlinedTextField(
                        value = reportTextState,
                        onValueChange = { reportTextState = it },
                        shape = RoundedCornerShape(10),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Beschreibung"
                            )
                        })
                }

                Row(Modifier.padding(0.dp, 15.dp)) {
                    var expanded by remember { mutableStateOf(false) }
                    val suggestions = listOf("Georgenstraße 7, 001", "Georgenstraße 7, 003",
                        "Georgenstraße 7, 004", "Georgenstraße 7, 005", "Georgenstraße 7, 006",
                        "Georgenstraße 7, 007", "Georgenstraße 7, 008", "Georgenstraße 7, 009",
                        "Georgenstraße 7, 101", "Georgenstraße 7, 102", "Georgenstraße 7, 103",
                        "Georgenstraße 7, 104", "Georgenstraße 7, 105", "Georgenstraße 7, 106",
                        "Georgenstraße 7, 107", "Georgenstraße 7, 108", "Georgenstraße 7, 109",
                        "Georgenstraße 7, 110", "Georgenstraße 7, 201", "Georgenstraße 7, 203",
                        "Georgenstraße 7, 204", "Georgenstraße 7, 205", "Georgenstraße 7, 206",
                        "Georgenstraße 7, 207", "Georgenstraße 7, 208", "Georgenstraße 7, 209",
                        "Georgenstraße 7, 210", "Georgenstraße 11, U102", "Georgenstraße 11, U103",
                        "Georgenstraße 11, U104", "Georgenstraße 11, U105", "Georgenstraße 11, U106",
                        "Georgenstraße 11, U107", "Georgenstraße 11, U108", "Georgenstraße 11, U109",
                        "Georgenstraße 11, U110", "Georgenstraße 11, 003", "Georgenstraße 11, 006",
                        "Georgenstraße 11, 007", "Georgenstraße 11, 009", "Georgenstraße 11, 102",
                        "Georgenstraße 11, 103", "Georgenstraße 11, 104", "Georgenstraße 11, 105",
                        "Georgenstraße 11, 106", "Georgenstraße 11, 107", "Georgenstraße 11, 108",
                        "Georgenstraße 11, 204")

                    val icon = if (expanded)
                        Icons.Filled.KeyboardArrowUp
                    else
                        Icons.Filled.KeyboardArrowDown

                    OutlinedTextField(
                        value = reportBuildingRoomState,
                        onValueChange = { reportBuildingRoomState = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                            },
                        label = {Text("Gebäude/Raum")},
                        trailingIcon = {
                            Icon(icon,"Gebäude/Raum",
                                Modifier.clickable { expanded = !expanded })
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.requiredSize(350.dp, 250.dp)
                    ) {
                        suggestions.forEach { label ->
                            DropdownMenuItem(onClick = {
                                reportBuildingRoomState = label
                                expanded = false
                            }) {
                                Text(text = label)
                            }
                        }
                    }
                }
                    Row(
                        modifier = Modifier.padding(vertical = 15.dp, horizontal = 5.dp)
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                            onClick = {
                                // Pick an image for the report
                                launcher.launch("image/*")
                            }, modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.Start) {
                                Text(text = "Bild hinzufügen", textAlign = TextAlign.Start, color = Color.White)
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_photo_camera_24),
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 100.dp)
                                )
                            }
                        }
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Blue09),
                        onClick = {
                            GlobalScope.launch(Dispatchers.IO) {
                                // image has been selected and is ready for upload
                                imageUri?.let {
                                    if (Build.VERSION.SDK_INT < 28) {
                                        bitmap.value = MediaStore.Images
                                            .Media.getBitmap(context.contentResolver, it)
                                    } else {
                                        val source =
                                            ImageDecoder.createSource(context.contentResolver, it)
                                        bitmap.value = ImageDecoder.decodeBitmap(source)
                                    }

                                    reportImgBase64String =
                                        Utils.bitmapToBase64(bitmap.value) // Set the image in base64 format
                                }

                                // Set the current date
                                val current = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                reportDateState = current.format(formatter).toString()

                                val reportPOST = ReportPOST(
                                    reportTitle = reportTitle,
                                    reportDate = reportDateState,
                                    reportBuildingRoom = reportBuildingRoomState,
                                    reportDislikes = 0,
                                    reportLikes = 0,
                                    reportStatus = "0".toInt(),
                                    reportText = reportTextState,
                                    reportImg = reportImgBase64String
                                )

                                val restAPI = RestAPI.getInstance()
                                val result =
                                    restAPI.uploadReport(SessionManager.getToken(), reportPOST)

                                if (result.isSuccessful) {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Meldung wurde erstellt.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Meldung konnte nicht erstellt werden.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                            navController.navigate(ROOT_ROUTE)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp), shape = CircleShape
                    ) {
                        Text(text = "Erstellen", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
}





