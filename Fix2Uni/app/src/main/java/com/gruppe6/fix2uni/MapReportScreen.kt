package com.gruppe6.fix2uni

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import java.util.concurrent.TimeUnit


@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavController, fusedLocationProviderClient: FusedLocationProviderClient) {

    val viewModel = viewModel(modelClass = ReportViewModel::class.java)
    val context = LocalContext.current
    viewModel.getResponses(context)

    val reportGetsVar = viewModel.reports

    val problemRooms : ArrayList<String> by remember{
        mutableStateOf(ArrayList())
    }

    for (problem in reportGetsVar){
        problemRooms.add(problem.reportBuildingRoom)
    }

    var doNotShowRationale by rememberSaveable {
        mutableStateOf(false)
    }
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val uiSettings by remember { mutableStateOf(
        MapUiSettings(
            compassEnabled = true,
            zoomControlsEnabled = false
        )
    ) }

    val markerDragState = rememberMarkerDragState()
    var dragMarkerPos by remember{
        mutableStateOf(LatLng(48.15, 11.581))
    }

    //calls to get user location
    var userLocation: Location? = null
    val locationRequest = LocationRequest().apply {
        interval = TimeUnit.SECONDS.toMillis(60)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
        priority = 100
    }
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let {
                userLocation = locationResult.lastLocation!!
            } ?: run {
                Log.d(ContentValues.TAG, "Location information isn't available.")
            }
        }
    }

    val activity = context.getActivity() as Activity

    //only get user location if permission is given
    if(isLocationPermissionGranted(activity)) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    //map properties
    val properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL,isMyLocationEnabled = true, mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_json)))
    }

    //positions of buildings
    val georgen11 = LatLng(48.154393307495674, 11.580509262104815)
    val georgen7 = LatLng(48.15431091826868, 11.58105)

    //set camera position to user location if available, else set on implemented buildings
    val cameraPositionState: CameraPositionState?
    if(userLocation!=null){
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(userLocation!!.latitude,userLocation!!.longitude), 15f)
        }
    } else {
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(georgen11, 15f)
        }
    }

    //markers that mark all rooms
    val markers : ArrayList<CustomMarker> = ArrayList()

    //levels of the currently selected building
    var levels : Array<Int> by remember {
        mutableStateOf(arrayOf<Int>())
    }

    //plans of the buildings
    var georgen11Map by remember {
        mutableStateOf(R.drawable.georgen111)
    }
    var georgen7Map by remember {
        mutableStateOf(R.drawable.georgen71)
    }

    //if ...Visible is true, the building has been selected
    var georgen11Visible by remember{
        mutableStateOf(false)
    }
    var georgen7Visible by remember{
        mutableStateOf(false)
    }

    //if ...Visible is true, the floor has been selected
    var ugVisible by remember{
        mutableStateOf( false)
    }
    var egVisible by remember{
        mutableStateOf( false)
    }
    var fsVisible by remember{
        mutableStateOf( false)
    }
    var snVisible by remember{
        mutableStateOf( false)
    }

    //true, if manual location selection is in progress
    var manualSelection by remember{
        mutableStateOf( false)
    }

    //filter room markers by building and floor
    val filterMarkers: () -> ArrayList<CustomMarker> = {
        val filteredMarkers : ArrayList<CustomMarker> = ArrayList()
        for (marker in markers){
            if(georgen11Visible && marker.building == "Georgenstraße 11"){
                if(ugVisible && marker.room.startsWith("U")){
                    filteredMarkers.add(marker)
                } else {
                    if(egVisible && marker.room.startsWith("0")){
                        filteredMarkers.add(marker)
                    } else {
                        if(fsVisible && marker.room.startsWith("1")){
                            filteredMarkers.add(marker)
                        } else {
                            if(snVisible && marker.room.startsWith("2")){
                                filteredMarkers.add(marker)
                            }
                        }
                    }
                }
            }
            if(georgen7Visible && marker.building == "Georgenstraße 7"){
                if(egVisible && marker.room.startsWith("0")){
                    filteredMarkers.add(marker)
                } else {
                    if(fsVisible && marker.room.startsWith("1")){
                        filteredMarkers.add(marker)
                    } else {
                        if(snVisible && marker.room.startsWith("2")){
                            filteredMarkers.add(marker)
                        }
                    }
                }
            }
        }
        filteredMarkers
    }



    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            if (doNotShowRationale) {
                Text("Feature not available")
            } else {
                PermissionNotGrantedUI(
                    onYesClick = {
                        locationPermissionState.launchPermissionRequest()
                    }, onCancelClick = {
                        doNotShowRationale = true
                    })
            }
        },
        permissionNotAvailableContent = {
            PermissionNotAvailableContent(
                onOpenSettingsClick = { })
        },
        content = {
            Box(modifier = Modifier
                .fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    properties = properties,
                    uiSettings = uiSettings,
                    cameraPositionState = cameraPositionState,

                    )
                {
                    //lambda functions to call on marker click
                    val showGeorgen11: (Marker) -> Boolean = {
                        georgen11Visible = true
                        georgen7Visible = false
                        egVisible = true
                        levels = arrayOf<Int>(-1,0,1,2)
                        true
                    }

                    val showGeorgen7: (Marker) -> Boolean = {
                        georgen7Visible = true
                        georgen11Visible = false
                        egVisible = true
                        levels = arrayOf<Int>(0,1,2)
                        true
                    }

                    //ground overlays to display building plans
                    GroundOverlay(
                        position = GroundOverlayPosition.create(georgen11, 30f, 30f),
                        image = BitmapDescriptorFactory.fromResource(georgen11Map),
                        visible = true
                    )

                    GroundOverlay(
                        position = GroundOverlayPosition.create(georgen7, 47f, 47f),
                        image = BitmapDescriptorFactory.fromResource(georgen7Map),
                        visible = true
                    )

                    //markers to click to select building
                    if(!georgen11Visible) {
                        Marker(position = georgen11, onClick = showGeorgen11)
                    }
                    if(!georgen7Visible) {
                        Marker(position = georgen7, onClick = showGeorgen7)
                    }

                    //room markers
                    //for Georgenstraße 11
                    markers.add(CustomMarker(LatLng(48.15446747451083,11.580486036837101), "U102", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15445942235492,11.580562815070152), "U103", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.154453383237154,11.580606065690517), "U104", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.154430121443546,11.58062484115362), "U105", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.1543370741637,11.580575890839102), "U106", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15434646836788,11.580526269972324), "U107", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15430598380903,11.580429710447788), "U108", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15436548044252,11.580430716276169), "U109", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15441401711872,11.580449491739273), "U110", building = "Georgenstraße 11"))

                    markers.add(CustomMarker(LatLng(48.1544556199475,11.580479331314562), "002", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15442430599351,11.58054169267416), "003", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15443302916834,11.580586619675158), "005", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15433863986451,11.580570191144943), "006", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.154339981893756,11.580518558621408), "007", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15434266595212,11.580422334372997), "009", building = "Georgenstraße 11"))

                    markers.add(CustomMarker(LatLng(48.154456290960596,11.58047564327717), "102", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15442050358351,11.580538675189018), "103", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15443370018172,11.580580919981003), "104", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15434132392295,11.580570191144943), "106", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15434378430973,11.580519564449785), "107", building = "Georgenstraße 11"))
                    markers.add(CustomMarker(LatLng(48.15433841619297,11.580424346029758), "109", building = "Georgenstraße 11"))

                    markers.add(CustomMarker(LatLng(48.15443660790628,11.580592319369316), "204", building = "Georgenstraße 11"))

                    //for Georgenstraße 7
                    markers.add(CustomMarker(LatLng(48.15432499589865,11.58119011670351),"101", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15423530350818,11.581180393695831),"102", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.1542406716357,11.581137478351593),"103", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15426013109329,11.58107344061136),"104", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15426997265527,11.581028178334236),"105", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15427422242007,11.58097118139267),"106", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.154291221475766,11.580904461443424),"107", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15437174324203,11.580942012369633),"108", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15436302005679,11.5810040384531),"109", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15435049445487,11.581058688461782),"110", "Georgenstraße 7"))

                    markers.add(CustomMarker(LatLng(48.15431985145157,11.581178046762943),"201", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15424425038708,11.581151559948921),"203", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.154261249452695,11.581077463924885),"204", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15427243304545,11.581026837229729),"205", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15428205093325,11.580972857773304),"206", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15429256350624,11.580909155309199),"207", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15437107222781,11.580942012369633),"208", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.15435899397078,11.581006720662115),"209", "Georgenstraße 7"))
                    markers.add(CustomMarker(LatLng(48.154344902667304,11.581063382327557),"210", "Georgenstraße 7"))

                    //display markers in rooms if there is an issue in the room
                    if(georgen11Visible){
                        if(ugVisible){
                            if (problemRooms.contains("Georgenstraße 11, U102")) {
                                Marker(
                                    position = LatLng(48.15446747451083,11.580486036837101), title = "U102",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U102"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U103")) {
                                Marker(
                                    position = LatLng(48.15445942235492,11.580562815070152), title = "U103",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U103"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U104")) {
                                Marker(
                                    position = LatLng(48.154453383237154,11.580606065690517), title = "U104",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U104"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U105")) {
                                Marker(
                                    position = LatLng(48.154430121443546,11.58062484115362), title = "U105",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U105"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U106")) {
                                Marker(
                                    position = LatLng(48.1543370741637,11.580575890839102), title = "U106",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U106"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U107")) {
                                Marker(
                                    position = LatLng(48.15434646836788,11.580526269972324), title = "U107",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U107"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U108")) {
                                Marker(
                                    position = LatLng(48.15430598380903,11.580429710447788), title = "U108",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U108"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U109")) {
                                Marker(
                                    position = LatLng(48.15436548044252,11.580430716276169), title = "U109",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U109"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, U110")) {
                                Marker(
                                    position = LatLng(48.15441401711872,11.580449491739273), title = "U110",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, U110"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                        if(egVisible) {
                            if (problemRooms.contains("Georgenstraße 11, 002")) {
                                Marker(
                                    position = LatLng(48.1544556199475,11.580479331314562), title = "002",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 002"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 003")) {
                                Marker(
                                    position = LatLng(48.15442430599351,11.58054169267416), title = "003",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 003"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 005")) {
                                Marker(
                                    position = LatLng(48.15443302916834,11.580586619675158), title = "005",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 005"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 006")) {
                                Marker(
                                    position = LatLng(48.15433863986451,11.580570191144943), title = "006",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 006"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 007")) {
                                Marker(
                                    position = LatLng(48.154339981893756,11.580518558621408), title = "007",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 007"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 009")) {
                                Marker(
                                    position = LatLng(48.15434266595212,11.580422334372997), title = "009",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 009"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                        if(fsVisible){
                            if (problemRooms.contains("Georgenstraße 11, 102")) {
                                Marker(
                                    position = LatLng(48.154456290960596,11.58047564327717), title = "102",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 102"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 103")) {
                                Marker(
                                    position = LatLng(48.15442050358351,11.580538675189018), title = "103",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 103"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 104")) {
                                Marker(
                                    position = LatLng(48.15443370018172,11.580580919981003), title = "104",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 104"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 106")) {
                                Marker(
                                    position = LatLng(48.15434132392295,11.580570191144943), title = "106",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 106"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 107")) {
                                Marker(
                                    position = LatLng(48.15434378430973,11.580519564449785), title = "107",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 107"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 11, 109")) {
                                Marker(
                                    position = LatLng(48.15433841619297,11.580424346029758), title = "109",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 109"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                        if(snVisible){
                            if (problemRooms.contains("Georgenstraße 11, 204")) {
                                Marker(
                                    position = LatLng(48.15443660790628,11.580592319369316), title = "204",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 11, 204"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                    }

                    if(georgen7Visible){
                        if(egVisible){
                            if (problemRooms.contains("Georgenstraße 7, 001")) {
                                Marker(
                                    position = LatLng(48.15432499589865,11.58119112253189), title = "001",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 001"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 003")) {
                                Marker(
                                    position = LatLng(48.154232619444194,11.581156924366953), title = "003",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 003"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 004")) {
                                Marker(
                                    position = LatLng(48.15426080210895,11.581073775887491), title = "004",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 004"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 005")) {
                                Marker(
                                    position = LatLng(48.15427131468626,11.581028178334236), title = "005",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 005"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 006")) {
                                Marker(
                                    position = LatLng(48.154277130153666,11.580971851944922), title = "006",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 006"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 007")) {
                                Marker(
                                    position = LatLng(48.15429010311699,11.580905802547932), title = "007",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 007"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 008")) {
                                Marker(
                                    position = LatLng(48.154369282856585,11.580941677093506), title = "008",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 008"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 009")) {
                                Marker(
                                    position = LatLng(48.15436659879961,11.581001691520214), title = "009",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 009"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                        if(fsVisible){
                            if (problemRooms.contains("Georgenstraße 7, 101")) {
                                Marker(
                                    position = LatLng(48.15432499589865,11.58119011670351), title = "101",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 101"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 102")) {
                                Marker(
                                    position = LatLng(48.15423530350818,11.581180393695831), title = "102",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 102"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 103")) {
                                Marker(
                                    position = LatLng(48.1542406716357,11.581137478351593), title = "103",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 103"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 104")) {
                                Marker(
                                    position = LatLng(48.15426013109329,11.58107344061136), title = "104",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 104"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 105")) {
                                Marker(
                                    position = LatLng(48.15426997265527,11.581028178334236), title = "105",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 105"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 106")) {
                                Marker(
                                    position = LatLng(48.15427422242007,11.58097118139267), title = "106",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 106"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 107")) {
                                Marker(
                                    position = LatLng(48.154291221475766,11.580904461443424), title = "107",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 107"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 108")) {
                                Marker(
                                    position = LatLng(48.15437174324203,11.580942012369633), title = "108",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 108"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 109")) {
                                Marker(
                                    position = LatLng(48.15436302005679,11.5810040384531), title = "109",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 109"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 110")) {
                                Marker(
                                    position = LatLng(48.15435049445487,11.581058688461782), title = "110",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 110"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                        if(snVisible){
                            if (problemRooms.contains("Georgenstraße 7, 201")) {
                                Marker(
                                    position = LatLng(48.15431985145157,11.581178046762943), title = "201",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 201"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 203")) {
                                Marker(
                                    position = LatLng(48.15424425038708,11.581151559948921), title = "203",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 203"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 204")) {
                                Marker(
                                    position = LatLng(48.154261249452695,11.581077463924885), title = "204",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 204"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 205")) {
                                Marker(
                                    position = LatLng(48.15427243304545,11.581026837229729), title = "205",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 205"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 206")) {
                                Marker(
                                    position = LatLng(48.15428205093325,11.580972857773304), title = "206",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 206"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 207")) {
                                Marker(
                                    position = LatLng(48.15429256350624,11.580909155309199), title = "207",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 207"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 208")) {
                                Marker(
                                    position = LatLng(48.15437107222781,11.580942012369633), title = "208",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 208"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 209")) {
                                Marker(
                                    position = LatLng(48.15435899397078,11.581006720662115), title = "209",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 209"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                            if (problemRooms.contains("Georgenstraße 7, 210")) {
                                Marker(
                                    position = LatLng(48.154344902667304,11.581063382327557), title = "210",
                                    onClick = {
                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                            "reportBuildingRoomState",
                                            "Georgenstraße 7, 210"
                                        )
                                        navController.navigate(REPORTS_ROUTE)
                                        true
                                    }
                                )
                            }
                        }
                    }

                    //display manualSelectionMarker if requested
                    if(manualSelection) {
                        Marker(
                            position = dragMarkerPos,
                            visible = true,
                            onClick = { true },
                            draggable = true,
                            markerDragState = markerDragState,
                            zIndex = 3.0f
                        )
                    }
                }

                //floor selections
                Column(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(0.dp, 40.dp)) {
                    TextButton(onClick = {
                        if(levels.contains(-1)){
                            ugVisible = true
                            egVisible = false
                            fsVisible = false
                            snVisible = false
                        }
                        if(georgen11Visible){
                            georgen11Map = R.drawable.georgen110
                        }
                    }, enabled = levels.contains(-1)) {
                        Text("UG")
                    }

                    TextButton(onClick = {
                        if(levels.contains(0)){
                            ugVisible = false
                            egVisible = true
                            fsVisible = false
                            snVisible = false
                        }
                        if(georgen11Visible){
                            georgen11Map = R.drawable.georgen111
                        }
                        if(georgen7Visible){
                            georgen7Map = R.drawable.georgen71
                        }
                    }, enabled = levels.contains(-0)) {
                        Text("EG")
                    }

                    TextButton(onClick = {
                        if(levels.contains(1)){
                            ugVisible = false
                            egVisible = false
                            fsVisible = true
                            snVisible = false
                        }
                        if(georgen11Visible){
                            georgen11Map = R.drawable.georgen112
                        }
                        if(georgen7Visible){
                            georgen7Map = R.drawable.georgen72
                        }
                    }, enabled = levels.contains(1)) {
                        Text("1")
                    }

                    TextButton(onClick = {
                        if(levels.contains(2)){
                            ugVisible = false
                            egVisible = false
                            fsVisible = false
                            snVisible = true
                        }
                        if(georgen11Visible){
                            georgen11Map = R.drawable.georgen113
                        }
                        if(georgen7Visible){
                            georgen7Map = R.drawable.georgen73
                        }
                    }, enabled = levels.contains(2)) {
                        Text("2")
                    }
                }

                //buttosn to add issue near user or at desired location
                Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                    TextButton(onClick = {

                        if(isLocationPermissionGranted(activity)) {
                            if (ActivityCompat.checkSelfPermission(
                                    activity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                    activity,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ),
                                    1
                                )
                            }
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )
                        }
                        val pi = Math.PI
                        val r = 6371 //equatorial radius
                        var distance = Double.MAX_VALUE
                        var closest: CustomMarker? = null
                        if(userLocation != null) {
                            val filteredMarkers: ArrayList<CustomMarker> = filterMarkers.invoke()
                            for (marker in filteredMarkers) {
                                val lat2 = marker.position.latitude
                                val lon2 = marker.position.longitude

                                val chLat = lat2 - userLocation!!.latitude
                                val chLon = lon2 - userLocation!!.longitude

                                val dLat = chLat * (pi / 180)
                                val dLon = chLon * (pi / 180)

                                val rLat1 = userLocation!!.latitude * (pi / 180)
                                val rLat2 = lat2 * (pi / 180)

                                val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                        Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(
                                    rLat2
                                )
                                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                                val d = r * c

                                if (closest == null || d < distance) {
                                    closest = marker
                                    distance = d
                                }
                            }
                            if(closest != null){
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "reportBuildingRoomState",
                                    closest.building +", " + closest.room
                                )
                                navController.navigate("create_report")
                            }
                        }
                        else {
                            Log.d(ContentValues.TAG, "Couldn't get user location.")
                        }

                    }) {
                        Image(
                            painterResource(R.drawable.ic_baseline_add_box_24),
                            modifier = Modifier.padding(bottom = 4.dp),
                            contentDescription = "add"
                        )
                    }
                    TextButton(onClick = {
                        dragMarkerPos = cameraPositionState.position.target
                        manualSelection = true
                    }) {
                        Image(
                            painterResource(R.drawable.ic_baseline_location_on_24),
                            modifier = Modifier.padding(bottom = 4.dp),
                            contentDescription = "add"
                        )
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                    if(manualSelection) {
                        TextButton(onClick = {
                            manualSelection = false
                        }) {
                            Image(
                                painterResource(R.drawable.ic_baseline_clear_24),
                                modifier = Modifier.padding(bottom = 4.dp),
                                contentDescription = "add"
                            )
                        }
                        TextButton(onClick = {
                            manualSelection = false
                            val pi = Math.PI
                            val r = 6371 //equatorial radius
                            var distance = Double.MAX_VALUE
                            var closest: CustomMarker? = null
                            val filteredMarkers: ArrayList<CustomMarker> = filterMarkers.invoke()
                            for (marker in filteredMarkers) {
                                val lat2 = marker.position.latitude
                                val lon2 = marker.position.longitude

                                val chLat = lat2 - dragMarkerPos.latitude
                                val chLon = lon2 - dragMarkerPos.longitude

                                val dLat = chLat * (pi / 180)
                                val dLon = chLon * (pi / 180)

                                val rLat1 = userLocation!!.latitude * (pi / 180)
                                val rLat2 = lat2 * (pi / 180)

                                val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                        Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(
                                    rLat2
                                )
                                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                                val d = r * c

                                if (closest == null || d < distance) {
                                    closest = marker
                                    distance = d
                                }
                            }
                            if(closest != null){
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "reportBuildingRoomState",
                                    closest!!.building +", " + closest!!.room
                                )
                                navController.navigate("create_report")
                            }
                        }) {
                            Image(
                                painterResource(R.drawable.ic_baseline_check_24),
                                modifier = Modifier.padding(bottom = 4.dp),
                                contentDescription = "add"
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun PermissionNotAvailableContent(onOpenSettingsClick: () -> Unit) {

    Column {
        Text("Camera permission denied.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onOpenSettingsClick() }) {
            Text("Open settings")
        }
    }
}


@Composable
fun PermissionNotGrantedUI(onYesClick: () -> Unit, onCancelClick: () -> Unit) {
    Column {
        Text("Location is important for this app. Please grant the permission.")
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                onYesClick()
            }) {
                Text("Yes")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                onCancelClick()
            }) {
                Text("Cancel")
            }
        }
    }
}

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

private fun isLocationPermissionGranted(activity: Activity): Boolean {
    return if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
        false
    } else {
        true
    }
}