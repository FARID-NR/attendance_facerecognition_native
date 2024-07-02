package com.example.absensiapp.model.response.home


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GetUserResponse(
    @Expose
    @SerializedName("created_at")
    val createdAt: String,
    @Expose
    @SerializedName("department")
    val department: Any,
    @Expose
    @SerializedName("email")
    val email: String,
    @Expose
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any,
    @Expose
    @SerializedName("face_embedding")
    val faceEmbedding: String,
    @Expose
    @SerializedName("fcm_token")
    val fcmToken: String,
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
    val twoFactorConfirmedAt: Any,
    @Expose
    @SerializedName("two_factor_recovery_codes")
    val twoFactorRecoveryCodes: Any,
    @Expose
    @SerializedName("two_factor_secret")
    val twoFactorSecret: Any,
    @Expose
    @SerializedName("updated_at")
    val updatedAt: String
)