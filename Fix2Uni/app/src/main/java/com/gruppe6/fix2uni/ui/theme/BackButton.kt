package com.gruppe6.fix2uni.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.gruppe6.fix2uni.R

@Composable
fun BackButton(navController: NavController, Uri: String){

    Row(horizontalArrangement = Arrangement.Start) {


        IconButton(
            onClick = {navController.navigate(Uri)},) {
            Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription =null )
        }
    }
}