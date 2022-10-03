package com.gruppe6.fix2uni

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    var reports:List<ReportIdGET> by mutableStateOf(listOf())

    fun getResponses(context: Context){
        viewModelScope.launch(Dispatchers.IO){
            val restAPI = RestAPI.getInstance()
            if (!SessionManager.getToken().equals("")){
                reports = restAPI.getReports(SessionManager.getToken()).body()!!
            }
            else{
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Sie sind nicht angemeldet", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}