package com.example.absensiapp.model.response.history


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
    @Expose
    @SerializedName("created_at")
    val createdAt: String,
    @Expose
    @SerializedName("date")
    val date: String,
    @Expose
    @SerializedName("id")
    val id: Int,
    @Expose
    @SerializedName("latlon_in")
    val latlonIn: String,
    @Expose
    @SerializedName("latlon_out")
    val latlonOut: String,
    @Expose
    @SerializedName("time_in")
    val timeIn: String,
    @Expose
    @SerializedName("time_out")
    val timeOut: String,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String,
    @Expose
    @SerializedName("user_id")
    val userId: Int
)