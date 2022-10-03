package com.gruppe6.fix2uni

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RestAPI {

    @GET("/reports/")
    suspend fun getReports(@Header("Authorization") token: String) : Response<List<ReportIdGET>>

    @POST("/reports/")
    suspend fun uploadReport(@Header("Authorization") token: String, @Body reportPOST: ReportPOST) : Response<Any>

    @DELETE("/reports/{id}/")
    suspend fun deleteReport(@Header("Authorization") token: String, @Path("id") id: Int) : Response<Any>

    @PUT("/reports/{id}/")
    suspend fun updateReport(@Header("Authorization") token: String, @Path("id") id: Int, @Body reportIdGET: ReportIdGET) : Response<Any>

    @POST("/register/")
    suspend fun postNewUser(@Body user: User) : Response<ServerResponse>

    @POST("/auth-register/")
    suspend fun authenticateUser(@Body user: User) : Response<ServerResponse>

    @POST("/api-token-auth/")
    suspend fun requestToken(@Body user: User) : Response<Token>

    companion object{
        var restApi: RestAPI? = null
        fun getInstance() : RestAPI{
            if(restApi == null){
                restApi = RetrofitInstance.getInstance().create(RestAPI::class.java)
            }
            return restApi!!
        }
    }

}