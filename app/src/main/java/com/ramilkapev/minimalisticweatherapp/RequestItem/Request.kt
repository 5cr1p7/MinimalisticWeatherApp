package com.ramilkapev.minimalisticweatherapp.RequestItem

data class Request(
    val current: Current,
    val daily: List<Daily>,
    val lat: Double,
    val lon: Double
)