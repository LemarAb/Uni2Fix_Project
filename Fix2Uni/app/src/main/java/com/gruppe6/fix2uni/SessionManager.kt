package com.gruppe6.fix2uni

import android.content.Context
import android.content.SharedPreferences

/**
 * Session manager to save and fetch data from SharedPreferences
 */
object SessionManager {
    //private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    //private var context: Context

    private var token = ""
    private var email = ""
    private var username = ""
    private var role = ""

    fun getToken(): String {
        return token
    }

    fun setToken(token: String){
        this.token = token
    }

    fun getUsername(): String{
        return username
    }

    fun setUsername(username:String){
        this.username = username
    }

    fun getEmail(): String{
        return email
    }

    fun setEmail(email: String){
        this.email = email
    }

    fun setRole(role: String){
        this.role = role
    }

    fun getRole(): String{
        return role
    }

    /**
     * Function to save auth token
     */
    /*fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }*/
}