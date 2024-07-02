package com.example.absensiapp.model.response.checkin

data class Attendance(
    val created_at: String,
    val date: String,
    val id: Int,
    val latlon_in: String,
    val time_in: String,
    val updated_at: String,
    val user_id: Int
)