package com.ramilkapev.minimalisticweatherapp

import com.ramilkapev.minimalisticweatherapp.RequestItem.Request
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkClient {
    private val service: NetworkService

    companion object {
        const val BASE_URL = "http://api.openweathermap.org/"
        const val APP_ID = "73cbebdd0322acd49bda6ede059b2b18"
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(NetworkService::class.java)
    }

    fun getNextSevenDays(lat: Double, lon: Double, callback: Callback<Request>) {
        val call =
            service.getWeatherNextSevenDays(lat, lon, "minutely,hourly,alerts", APP_ID, "metric")
        call.enqueue(callback)
    }
}