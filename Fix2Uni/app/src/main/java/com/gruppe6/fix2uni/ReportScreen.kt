package com.gruppe6.fix2uni

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gruppe6.fix2uni.ui.theme.poppins
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gruppe6.fix2uni.ui.theme.Blue09
import com.gruppe6.fix2uni.ui.theme.Green12
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


// initialize the list of reports
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ListReportScreen(reportGETS: List<ReportIdGET>, navController: NavController, reportBuildingRoomState: String, viewModel: ReportViewModel) {

    val viewModel = viewModel(modelClass = ReportViewModel::class.java)
    val context = LocalContext.current
    viewModel.getResponses(context)

    Column(modifier = Modifier
        .fillMaxSize()) {
        TopBar(reportGETS = viewModel.reports, navController = navController, viewModel = viewModel,reportBuildingRoomState)
        Spacer(modifier = Modifier.height(5.dp))
    }
}

// initialize the top bar
@Composable
fun TopBar(reportGETS: List<ReportIdGET>, navController: NavController, viewModel: ReportViewModel, reportBuildingRoomState: String) {
    val list = listOf("Hot", "Offen", "Gelöst", "Archiviert")
    val expanded = remember { mutableStateOf(false) }
    val currentValue = rememberSaveable { mutableStateOf(list[0]) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxWidth().padding(0.dp, 12.dp)) {
        if(reportBuildingRoomState != null && reportBuildingRoomState != "") {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                TextButton(onClick = {
                    navController.popBackStack()
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "reportBuildingRoomState",
                        ""
                    )
                }) {
                    Image(
                        painterResource(R.drawable.ic_baseline_arrow_back_24),
                        modifier = Modifier.padding(bottom = 4.dp),
                        contentDescription = "back"
                    )
                }
            }
        }

        Row(modifier = Modifier
            .align(Alignment.Center)
            .clickable {
                expanded.value = !expanded.value
            }) {
            Text(
                text = currentValue.value,
                fontFamily = poppins,
                fontSize = 23.sp,
                color = Color.Black
            )
            Image(
                painterResource(R.drawable.ic_baseline_keyboard_arrow_down_24),
                modifier = Modifier.padding(top = 7.dp),
                contentDescription = "arrow down"
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = {
                expanded.value = false
            }) {
                list.forEach {
                    DropdownMenuItem(onClick = {
                        currentValue.value = it
                        expanded.value = false
                    }) {
                        Text(text = it)
                    }
                }
            }
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            TextButton(onClick = { navController.navigate("create_report") }) {
                Image(
                    painterResource(R.drawable.ic_baseline_add_24),
                    modifier = Modifier.padding(bottom = 4.dp),
                    contentDescription = "add"
                )
            }
        }
    }

    Column() {
        // filter reports by "Hot", "Offen" "Archiviert", "Gelöst"
        LazyColumn() {
            if(reportBuildingRoomState == ""){
                // if filter is equal to "hot", then pre-sort the list
                if(currentValue.value.equals("Hot")) {
                    // create a copy of the list
                    val reportGETSsorted = reportGETS.sortedByDescending { it.reportLikes }
                    items(reportGETSsorted) { item ->
                        val it = remember { mutableStateOf(item) }
                        if (item.reportStatus == 0) {
                            ReportRow(reportGET = it, navController = navController)
                        }
                    }
                }
                else{
                    items(reportGETS) { item ->
                        val it = remember { mutableStateOf(item)}
                         if (currentValue.value.equals("Offen")){
                            if(item.reportStatus == 0){
                                ReportRow(reportGET = it, navController = navController)
                            }
                        } else if (currentValue.value.equals("Gelöst")){
                            if(item.reportStatus == 1){
                                ReportRow(reportGET = it, navController = navController)
                            }
                        } else if (currentValue.value.equals("Archiviert")){
                            if(item.reportStatus == 2){
                                ReportRow(reportGET = it, navController = navController)
                            }
                        }
                    }
                }
            } else {
                items(reportGETS) { item ->
                    val it = remember { mutableStateOf(item)}
                    if(item.reportStatus == 0 && item.reportBuildingRoom == reportBuildingRoomState){
                        ReportRow(reportGET = it, navController = navController)
                    }
                }
            }
        }
    }

}

// show the the full report
@SuppressLint("UnrememberedMutableState")
@Composable
fun FullReportScreen(
    modifier: Modifier = Modifier,
    reportGET: ReportIdGET,
    navController: NavController
) {
    val status = mutableStateOf(reportGET.reportStatus)
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier
            .padding(top = 15.dp)
            .align(Alignment.TopCenter)
        ) {
            Text(
                text = "Meldung",
                fontSize = 23.sp,
                color = Color.Black,
            )
        }
        Row(modifier = Modifier
            .align(Alignment.TopStart)
            .padding(top = 8.dp)) {
            TextButton(onClick = { navController.popBackStack() }) {
                Image(
                    painterResource(R.drawable.ic_baseline_arrow_back_24),
                    contentDescription = "back"
                )
            }
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier
            .padding(top = 60.dp)
        ) {
            Text(
                text = reportGET.reportTitle,
                fontSize = 20.sp,
            )
        }
        Row(modifier = Modifier.padding(top = 5.dp)) {
            Image(
                painterResource(R.drawable.ic_baseline_location_on_24),
                contentDescription = "calender"
            )
            Text(text = reportGET.reportBuildingRoom, fontFamily = poppins)
        }
        Text(
            text = reportGET.reportText,
            modifier = Modifier.padding(top = 10.dp)
        )
        Row(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            text = "Status: " +
                    if(status.value == 0){
                        "Offen"
                    } else if(status.value == 1){
                        "Gelöst"
                    } else{
                        "Archiviert"
                    },
            color = Color.Blue
        )
            Spacer(Modifier.weight(2f))
        Image(
            painterResource(R.drawable.ic_baseline_calendar_today_24),
            contentDescription = "calender"
        )
        Text(text = reportGET.reportDate, fontFamily = poppins)
        }
        Image(
            // render the bitmap image by converting it from base64
            bitmap = Utils.base64toBitmap(reportGET.reportImg).asImageBitmap(),
            contentDescription = "reportImg",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
        if(SessionManager.getRole() == "Admin" && reportGET.reportStatus == 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp)
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Blue09,
                        contentColor = Color.White
                    ),
                    onClick = {
                        status.value = 2
                        GlobalScope.launch(Dispatchers.IO) {
                            reportGET.reportStatus = 2
                            RestAPI.getInstance().updateReport(
                                SessionManager.getToken(),
                                reportGET.reportId.toInt(),
                                reportGET
                            )
                        }
                    }, modifier = Modifier
                        .width(140.dp)
                        .padding(end = 5.dp), shape = CircleShape
                ) {
                    Text(text = "Archivieren")
                }
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = Green12,
                    contentColor = Color.White
                ),
                    onClick = {
                        status.value = 1
                        GlobalScope.launch(Dispatchers.IO) {
                            reportGET.reportStatus = 1
                            RestAPI.getInstance().updateReport(
                                SessionManager.getToken(),
                                reportGET.reportId.toInt(),
                                reportGET
                            )
                        }
                    }, modifier = Modifier.width(180.dp), shape = CircleShape
                ) {
                    Text(text = "Gelöst")
                }
            }
        }
    }
}

// show the report in a compact view with like and dislike functionality
@Composable
fun ReportRow(
    reportGET: MutableState<ReportIdGET>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val thumbIconLiked = rememberSaveable { mutableStateOf(false) }
    val thumbIconDisliked = rememberSaveable { mutableStateOf(false) }

    Card(
        elevation = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "reportGET",
                        reportGET
                    )
                    navController.navigate("full_report_screen")
                }
            ),
        shape = RoundedCornerShape(10)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text(text = reportGET.value.reportTitle, fontFamily = poppins, fontSize = 20.sp)
            Row(modifier=Modifier.padding( top = 5.dp)) {
                Image(
                    painterResource(R.drawable.ic_baseline_location_on_24),
                    contentDescription = "location"
                )
                Text(text = reportGET.value.reportBuildingRoom, fontFamily = poppins)
            }
            Text(
                text = reportGET.value.reportText,
                fontFamily = poppins,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Row() {
                Row(modifier = Modifier
                    .weight(156f)
                    .padding(top = 12.dp)) {
                    Image(
                        painterResource(R.drawable.ic_baseline_calendar_today_24),
                        contentDescription = "calender"
                    )
                    Text(text = reportGET.value.reportDate, fontFamily = poppins)
                }
                TextButton(onClick = {

                    if(!thumbIconDisliked.value){ thumbIconLiked.value = !thumbIconLiked.value }
                    if(thumbIconLiked.value){
                        // Update the likes on the server
                        GlobalScope.launch(Dispatchers.IO) {
                            reportGET.value.reportLikes++
                            RestAPI.getInstance().updateReport(SessionManager.getToken(), reportGET.value.reportId.toInt(), reportGET.value)
                        }
                    }
                    else if (!thumbIconLiked.value && !thumbIconDisliked.value){
                        // Update the likes on the server
                        GlobalScope.launch(Dispatchers.IO) {
                            reportGET.value.reportLikes--
                            RestAPI.getInstance().updateReport(SessionManager.getToken(), reportGET.value.reportId.toInt(), reportGET.value)
                        }
                    }
                }) {
                    Image(
                        painterResource(
                            if (thumbIconLiked.value) {
                                R.drawable.ic_baseline_thumb_up_24_pressed
                            } else {
                                R.drawable.ic_baseline_thumb_up_24
                            }
                        ),
                        contentDescription = "thumb up",
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Text(reportGET.value.reportLikes.toString(), color = Color.Black)
                }
                TextButton(onClick = { if(!thumbIconLiked.value){ thumbIconDisliked.value = !thumbIconDisliked.value}
                    if(thumbIconDisliked.value){
                        // Update the likes on the server
                        GlobalScope.launch(Dispatchers.IO) {
                            reportGET.value.reportDislikes++
                            RestAPI.getInstance().updateReport(SessionManager.getToken(), reportGET.value.reportId.toInt(), reportGET.value)
                        }
                    }
                    else if (!thumbIconDisliked.value && !thumbIconLiked.value){
                        // Update the likes on the server
                        GlobalScope.launch(Dispatchers.IO) {
                            reportGET.value.reportDislikes--
                            RestAPI.getInstance().updateReport(SessionManager.getToken(), reportGET.value.reportId.toInt(), reportGET.value)
                        }
                    }
                }) {
                    Image(
                        painterResource(
                            if (thumbIconDisliked.value) {
                                R.drawable.ic_baseline_thumb_down_24_pressed
                            } else {
                                R.drawable.ic_baseline_thumb_down_24
                            }
                        ),
                        contentDescription = "thumb down",
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Text(reportGET.value.reportDislikes.toString(), color = Color.Black)
                }
            }
        }
    }

}
