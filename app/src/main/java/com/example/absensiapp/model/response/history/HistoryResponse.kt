package com.example.absensiapp.model.response.history


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    @Expose
    @SerializedName("data")
    val `data`: List<Data>,
    @Expose
    @SerializedName("message")
    val message: String
)