package com.gruppe6.fix2uni

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val baseUrl = "http://msp-ss22-6.mobile.ifi.lmu.de:80/" // TODO: add the base url

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}