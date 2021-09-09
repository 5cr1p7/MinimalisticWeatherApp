package com.ramilkapev.minimalisticweatherapp

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(context: Context): AndroidViewModel(Application()) {

//    private fun getLatLon(city: String, citites: ArrayList<Address>, context: Context) {
//        if (Geocoder.isPresent()) {
//            val geocoder = Geocoder(context, Locale.getDefault())
//            val addresses: List<Address> = geocoder.getFromLocationName(city, 3)
//            Log.d("TAGgor", addresses.toString())
//            citites.clear()
//            citites.addAll(addresses)
//        }
//    }
}