package com.gruppe6.fix2uni

import android.media.Image
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ReportPOST(
    //@SerializedName("id")
    //val reportId: String,
    @SerializedName("reportTitle")
    val reportTitle: String,
    @SerializedName("reportDate")
    val reportDate: String,
    @SerializedName("reportBuildingRoom")
    val reportBuildingRoom: String,
    @SerializedName("reportText")
    val reportText: String,
    @SerializedName("reportStatus")
    var reportStatus: Int, // 0 = offen, 1 = gelöst, 2 = archiviert
    //val reportPic: Image,
    @SerializedName("reportLikes")
    var reportLikes: Int,
    @SerializedName("reportDislikes")
    var reportDislikes: Int,
    @SerializedName("reportImg")
    var reportImg: String
)

@Parcelize
data class ReportIdGET(
    @SerializedName("id")
    val reportId: String,
    @SerializedName("reportTitle")
    val reportTitle: String,
    @SerializedName("reportDate")
    val reportDate: String,
    @SerializedName("reportBuildingRoom")
    val reportBuildingRoom: String,
    @SerializedName("reportText")
    val reportText: String,
    @SerializedName("reportStatus")
    var reportStatus: Int, // 0 = offen, 1 = gelöst, 2 = archiviert
    //val reportPic: Image,
    @SerializedName("reportLikes")
    var reportLikes: Int,
    @SerializedName("reportDislikes")
    var reportDislikes: Int,
    @SerializedName("reportImg")
    var reportImg: String
): Parcelable

data class User(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class ServerResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("isAdmin")
    val isAdmin: Boolean
)

data class Token(
    @SerializedName("token")
    val token: String
)

data class CustomMarker(
    val position: LatLng,
    val room: String,
    val building: String
)

