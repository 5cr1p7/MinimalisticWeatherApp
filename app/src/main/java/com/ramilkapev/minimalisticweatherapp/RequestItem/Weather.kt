package com.ramilkapev.minimalisticweatherapp.RequestItem

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)