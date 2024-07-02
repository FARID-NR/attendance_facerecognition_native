package com.example.absensiapp.model.response.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @Expose
    @SerializedName("user")
    val user: User,
    @Expose
    @SerializedName("token")
    val token: String
)