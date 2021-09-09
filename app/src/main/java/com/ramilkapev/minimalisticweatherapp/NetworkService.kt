package com.ramilkapev.minimalisticweatherapp

import com.ramilkapev.minimalisticweatherapp.RequestItem.Request
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {
    @GET("/data/2.5/onecall")
    fun getWeatherNextSevenDays(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String?,
        @Query("appid") appId: String,
        @Query("units") units: String
    ): Call<Request>
}