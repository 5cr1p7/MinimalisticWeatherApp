package com.ramilkapev.minimalisticweatherapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ramilkapev.minimalisticweatherapp.RequestItem.Daily
import java.text.SimpleDateFormat
import java.util.*

class WeekForecastAdapter(private val daily: List<Daily>?) :
    RecyclerView.Adapter<WeekForecastAdapter.ViewHolder>() {

    private val formatter = SimpleDateFormat("dd/MM/yyyy")

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var weatherIcon: ImageView? = null
        var dayTempTv: TextView? = null
        var minTempTv: TextView? = null
        var maxTempTv: TextView? = null
        var dateTv: TextView? = null

        init {
            weatherIcon = itemView.findViewById(R.id.weatherIcon)
            minTempTv = itemView.findViewById(R.id.minTemp)
            maxTempTv = itemView.findViewById(R.id.maxTemp)
            dateTv = itemView.findViewById(R.id.dateTv)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.weekly_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("TAGsuccess", position.toString())
        Log.d("TAGsuccess", daily.toString())
        Glide.with(holder.itemView).load("${MainFragment.IMAGE_URL}${daily!![position].weather[0].icon}@2x.png").centerCrop().into(holder.weatherIcon!!)
        holder.minTempTv?.text = "Min: ${daily[position].temp.min}"
        holder.maxTempTv?.text = "Max: ${daily[position].temp.max}"
        holder.dateTv?.text = "${formatter.format(Date(daily[position].dt.toLong() * 1000))}"

    }

    override fun getItemCount(): Int {
        return daily?.size!!
    }

}