package com.example.absensiapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDate(var date: Date, var isSelected: Boolean = false) {
    val calendarDay: String
        get() = SimpleDateFormat("EE", Locale.getDefault()).format(date)

    val calendarDate: String
        get() {
            val cal = Calendar.getInstance()
            cal.time = date
            return cal[Calendar.DAY_OF_MONTH].toString()
        }
}
