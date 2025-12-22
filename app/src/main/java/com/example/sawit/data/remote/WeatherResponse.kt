package com.example.sawit.data.remote

data class WeatherResponse(
    val name: String,
    val main: MainData,
    val weather: List<WeatherDescription>,
    val sys: SysData
)

data class MainData(val temp: Double)
data class WeatherDescription(val main: String, val icon: String)
data class SysData(val country: String)