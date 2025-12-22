package com.example.sawit.data.remote

import com.squareup.moshi.Json

data class WeatherResponse(
    @Json(name = "name") val cityName: String,
    @Json(name = "main") val main: MainStats,
    @Json(name = "weather") val weather: List<WeatherDescription>,
    @Json(name = "sys") val sys: Sys
)

data class MainStats(
    @Json(name = "temp") val temp: Double,
    @Json(name = "humidity") val humidity: Int
)

data class WeatherDescription(
    @Json(name = "main") val main: String,
    @Json(name = "description") val description: String,
    @Json(name = "icon") val icon: String
)

data class Sys(
    @Json(name = "country") val country: String
)