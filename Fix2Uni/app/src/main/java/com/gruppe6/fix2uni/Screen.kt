package com.gruppe6.fix2uni

const val REPORTS_ROUTE = "reports"
const val ROOT_ROUTE = "root"

// define the roots for the screens
sealed class Screen(val route: String){
    object MapScreen : Screen("map_screen")
    object ListReportScreen: Screen("report_screen")
    object RegisteredScreen: Screen("registered_screen")
    object FullReportScreen: Screen("full_report_screen")
    object LoginScreen: Screen("login_screen")
    object CreateReport: Screen("create_report")
}