package com.ramilkapev.minimalisticweatherapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ramilkapev.minimalisticweatherapp.RequestItem.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainFragment : Fragment(R.layout.fragment_main) {

    lateinit var result: ((isAvailable: Boolean, type: ConnectionType?) -> Unit)
    private val api = NetworkClient()
    private var weatherData: TextView? = null
    private var weatherDataWeek: TextView? = null
    private var weatherIcon: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var city: String = ""
    private val citites: ArrayList<Address> = arrayListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.weeklyItemsRv)


        weatherData = view.findViewById(R.id.text)
        weatherIcon = view.findViewById(R.id.weatherIcon)
        val cityEntry = view.findViewById<EditText>(R.id.et)


        if (isNetworkConnected()) {
            getLocation()
            cityEntry.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    city = cityEntry.text.toString()
//                    Log.d("TAGedit", city)
                    getLatLon(city)
                    Log.d("TAGedit", citites.toString())
                    api.getNextSevenDays(citites[0].latitude, citites[0].longitude, callback)
                }
                return@setOnEditorActionListener true
            }
            city = "Moscow"
            getLatLon(city)
            api.getNextSevenDays(citites[0].latitude, citites[0].longitude, callback)
        } else {
            AlertDialog.Builder(requireContext()).setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
    }

    private val callback = object : Callback<Request> {
        override fun onFailure(call: Call<Request>?, t: Throwable?) {
            Log.d("TAGfail", t?.message.toString())
        }

        override fun onResponse(call: Call<Request>?, response: Response<Request>?) {
            response?.isSuccessful.let {
                val formatter = SimpleDateFormat("dd/MM/yyyy")
                val resultList = response?.body()
                Glide.with(requireView()).load("${IMAGE_URL}${resultList?.current?.weather?.get(0)?.icon}@2x.png").centerCrop().into(weatherIcon!!)
                weatherData?.text = "${resultList?.current?.temp}\n\n"
//                weatherDataWeek?.text = ""
//                resultList?.daily?.forEach {
//                    weatherDataWeek?.append("${formatter.format(Date(it.dt.toLong() * 1000))} || Day: ${it.temp.day} - Min: ${it.temp.min} - Max: ${it.temp.max} - ${it.wind_speed} - ${it.wind_deg}\n")
//                }
                Log.d("TAGsuccess", resultList?.daily.toString())

                val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                context?.let {
                    decoration.setDrawable(ContextCompat.getDrawable(it, R.drawable.inset_divider)!!)
                }
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = WeekForecastAdapter(resultList?.daily)
                recyclerView?.addItemDecoration(decoration)
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//            return
//        }
//        if (isPermissionGranted(
//                permissions,
//                grantResults,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        ) {
//            // Enable the my location layer if the permission has been granted.
//            enableMyLocation()
//        } else {
//            permissionDenied = true
//        }
//    }

    private fun getLatLon(city: String) {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocationName(city, 3)
            Log.d("TAGgor", addresses.toString())
            citites.clear()
            citites.addAll(addresses)
        }
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork
        } else {
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            requireContext().registerReceiver(networkChangeReceiver, intentFilter)
        }
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork as Network?)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo

            if (activeNetworkInfo != null) {
                when (activeNetworkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        result(true, ConnectionType.Wifi)
                    }
                    else -> {
                        result(true, ConnectionType.Cellular)
                    }
                }
            } else {
                result(false, null)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        const val IMAGE_URL = "https://openweathermap.org/img/wn/"
    }

    enum class ConnectionType {
        Wifi, Cellular
    }
}