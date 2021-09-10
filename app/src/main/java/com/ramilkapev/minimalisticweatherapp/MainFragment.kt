package com.ramilkapev.minimalisticweatherapp

import android.Manifest
import android.annotation.SuppressLint
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
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ramilkapev.minimalisticweatherapp.RequestItem.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.roundToInt

class MainFragment : Fragment(R.layout.fragment_main) {

    lateinit var result: ((isAvailable: Boolean, type: ConnectionType?) -> Unit)
    private val api = NetworkClient()
    private var weatherData: TextView? = null
    private var currentCity: TextView? = null
    private var weatherIcon: ImageView? = null
    private var cityEntry: EditText? = null
    private var recyclerView: RecyclerView? = null
    private var city: String = ""
    private val cities: ArrayList<Address> = arrayListOf()
    private var adapter: ArrayAdapter<String>? = null
    private var isPermissionGranted = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat = 0.0
    private var long = 0.0

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.weeklyItemsRv)
        weatherData = view.findViewById(R.id.currentWeather)
        currentCity = view.findViewById(R.id.currentCityTv)
        weatherIcon = view.findViewById(R.id.weatherIcon)
        cityEntry = view.findViewById<EditText>(R.id.et) as AutoCompleteTextView

        (cityEntry as AutoCompleteTextView).setAdapter(adapter)

        if (isNetworkConnected()) {
            requestLocation()
            (cityEntry as AutoCompleteTextView).setOnEditorActionListener { text, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE && text.text.isNotEmpty()) {
                    city = (cityEntry as AutoCompleteTextView).text.toString()
                    getLatLon(city)
                    api.getNextSevenDays(cities[0].latitude, cities[0].longitude, callback)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.emptyOrIncorrectCityName),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                (cityEntry as AutoCompleteTextView).setOnTouchListener { v, event ->
                    val DRAWABLE_RIGHT = 2
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (event.rawX >= ((cityEntry as AutoCompleteTextView).right - (cityEntry as AutoCompleteTextView).compoundDrawables[DRAWABLE_RIGHT].bounds
                                .width())
                        ) {
                            requestLocation()
                        }
                    }
                    return@setOnTouchListener false
                }
                return@setOnEditorActionListener true
            }
        } else {
            AlertDialog.Builder(requireContext()).setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }
    }

    private val callback = object : Callback<Request> {
        override fun onFailure(call: Call<Request>?, t: Throwable?) {
            Log.d("TAG", t?.message.toString())
        }

        override fun onResponse(call: Call<Request>?, response: Response<Request>?) {
            response?.isSuccessful.let {
                val resultList = response?.body()
                Glide.with(requireView())
                    .load("${IMAGE_URL}${resultList?.current?.weather?.get(0)?.icon}@2x.png")
                    .centerCrop().into(weatherIcon!!)
                weatherData?.text = "${resultList?.current?.temp?.roundToInt()} ℃"

                val decoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                context?.let {
                    decoration.setDrawable(
                        ContextCompat.getDrawable(
                            it,
                            R.drawable.inset_divider
                        )!!
                    )
                }
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = WeekForecastAdapter(resultList?.daily)
                recyclerView?.addItemDecoration(decoration)
            }
        }
    }

    private fun requestLocation() {
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
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    long = location.longitude
                    getLatLon(lat, long)
                    api.getNextSevenDays(lat, long, callback)
                }
            }
        }
    }

    private fun getLatLon(city: String) {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocationName(city, 3)
            if (addresses.isNotEmpty()) {
                val addressesSearch: List<Address> =
                    geocoder.getFromLocation(addresses[0].latitude, addresses[0].longitude, 3)
                cities.clear()
                cities.addAll(addresses)
                currentCity?.text =
                    "${addressesSearch[0].locality}, ${addressesSearch[0].countryName}"
                (cityEntry as AutoCompleteTextView).setText("${addressesSearch[0].locality}, ${addressesSearch[0].countryName}")
            } else {
                Toast.makeText(requireContext(), "Enter the correct city name", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getLatLon(lat: Double, lon: Double) {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addressesSearch: List<Address> =
                geocoder.getFromLocation(lat, lon, 3)
            currentCity?.text =
                "${addressesSearch[0].locality}, ${addressesSearch[0].countryName}"
            (cityEntry as AutoCompleteTextView).setText("${addressesSearch[0].locality}, ${addressesSearch[0].countryName}")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
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