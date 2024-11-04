package com.techlambda.chatlibrary

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertMillSecondsToDateTimeString(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    return format.format(date)
}