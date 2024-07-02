package com.example.absensiapp.model.response.login

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class User(
    @Expose
    @SerializedName("created_at")
    val created_at: String,
    @Expose
    @SerializedName("department")
    val department: Any,
    @Expose
    @SerializedName("email")
    val email: String,
    @Expose
    @SerializedName("email_verified_at")
    val email_verified_at: Any,
    @Expose
    @SerializedName("face_embedding")
    val face_embedding: String,
    @Expose
    @SerializedName("fcm_token")
    val fcm_token: String,
    @Expose
    @SerializedName("id")
    val id: Int,
    @Expose
    @SerializedName("image")
    val image: Any,
    @Expose
    @SerializedName("name")
    val name: String,
    @Expose
    @SerializedName("phone")
    val phone: String,
    @Expose
    @SerializedName("position")
    val position: Any,
    @Expose
    @SerializedName("role")
    val role: String,
    @Expose
    @SerializedName("two_factor_confirmed_at")
    val two_factor_confirmed_at: Any,
    @Expose
    @SerializedName("two_factor_recovery_codes")
    val two_factor_recovery_codes: Any,
    @Expose
    @SerializedName("two_factor_secret")
    val two_factor_secret: Any,
    @Expose
    @SerializedName("updated_at")
    val updated_at: String
)