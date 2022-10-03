package com.gruppe6.fix2uni

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.android.gms.location.*
import com.gruppe6.fix2uni.ui.theme.Blue09
import com.gruppe6.fix2uni.ui.theme.GrayB8
import com.gruppe6.fix2uni.ui.theme.White7

// create the navigation bar and initialize the screens
@Composable
fun Navigation(reportGETS: List<ReportIdGET>, fusedLocationProviderClient: FusedLocationProviderClient) {
    val navController = rememberNavController()

    val items = listOf(
        Screen.MapScreen,
        Screen.ListReportScreen,
        Screen.LoginScreen,
    )

    Scaffold(
        bottomBar = {
                BottomNavigation(
                    backgroundColor = White7,
                    modifier = Modifier.graphicsLayer {
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        clip = true
                        shadowElevation = 60.2f
                    },
                    elevation = 80.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination


                    items.forEach { screen ->
                        BottomNavigationItem(
                            selectedContentColor = Blue09,
                            unselectedContentColor = GrayB8,
                            icon = {
                                if (screen.route.equals("map_screen")) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                                } else if (screen.route.equals("report_screen")) {
                                    Icon(Icons.Filled.List, contentDescription = null)
                                } else {
                                    Icon(Icons.Filled.AccountBox, contentDescription = null)
                                }
                            },
                            //label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.MapScreen.route, route = ROOT_ROUTE, modifier = Modifier.padding(innerPadding)){
            composable(route = Screen.MapScreen.route){
                val reportListGET = navController.previousBackStackEntry?.savedStateHandle?.get<List<ReportIdGET>>("reportListGET")
                if (reportListGET != null) {
                    MapScreen(
                        navController = navController,
                        fusedLocationProviderClient
                    )
                } else {
                    MapScreen(
                        navController = navController,
                        fusedLocationProviderClient
                    )
                }
            }
            navigation(startDestination = Screen.ListReportScreen.route, route = REPORTS_ROUTE){
                composable(route = Screen.ListReportScreen.route){
                    val reportListGET = navController.previousBackStackEntry?.savedStateHandle?.get<List<ReportIdGET>>("reportListGET")
                    val reportBuildingRoomState = navController.previousBackStackEntry?.savedStateHandle?.get<String>("reportBuildingRoomState")
                    if (reportListGET != null) {
                        if (reportBuildingRoomState != null) {
                            ListReportScreen(reportGETS = reportListGET, navController = navController,reportBuildingRoomState, viewModel())
                        }
                        else{
                            ListReportScreen(reportGETS = reportListGET, navController = navController,"", viewModel())
                        }
                    }
                    else{
                        if (reportBuildingRoomState != null) {
                            ListReportScreen(reportGETS = reportGETS, navController = navController,reportBuildingRoomState, viewModel())
                        }
                        else{
                            ListReportScreen(reportGETS = reportGETS, navController = navController,"", viewModel())
                        }
                    }
                }
                composable(route = Screen.FullReportScreen.route){
                    val reportGET = navController.previousBackStackEntry?.savedStateHandle?.get<MutableState<ReportIdGET>>("reportGET")
                    if (reportGET != null) {
                        FullReportScreen(reportGET = reportGET.value, navController = navController)
                    }
                }
                composable(route = Screen.CreateReport.route){
                    val reportBuildingRoomState = navController.previousBackStackEntry?.savedStateHandle?.get<String>("reportBuildingRoomState")
                    if (reportBuildingRoomState != null) {
                        CreateReport(navController = navController, reportBuildingRoomState)
                    } else {
                        CreateReport(navController = navController)
                    }
                }
            }
            composable(route = Screen.LoginScreen.route){
                LoginScreen(navController = navController)
            }
            composable(route = Screen.RegisteredScreen.route){
                RegisteredScreen(navController = navController)
            }

        }
    }

}

