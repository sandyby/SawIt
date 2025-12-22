package com.example.sawit.viewmodels

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.sawit.BuildConfig
import com.example.sawit.api.RetrofitClient
import com.example.sawit.data.remote.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(
        val data: WeatherResponse,
        val province: String
    ) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState

    private val API_KEY = BuildConfig.OPENWEATHERMAP_API_KEY

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                _weatherState.value = WeatherState.Loading
                withContext(Dispatchers.IO) {
                    // 1. Fetch Weather from Network
                    val weatherResponse = RetrofitClient.weatherService.getCurrentWeather(lat, lon, "metric", API_KEY)

                    // 2. Fetch Province from Geocoder (Local Android System)
                    var provinceName = ""
                    try {
                        val geocoder = Geocoder(application, Locale.getDefault())
                        // getFromLocation returns a list of addresses
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            // adminArea is usually the Province/State
                            provinceName = addresses[0].adminArea ?: ""
                        }
                    } catch (e: Exception) {
                        Log.e("WeatherVM", "Geocoder failed", e)
                    }

                    // 3. Post Success
                    _weatherState.value = WeatherState.Success(weatherResponse, provinceName)
                }

//                val response = RetrofitClient.weatherService.getCurrentWeather(lat, lon, API_KEY, "metric")
//                _weatherState.value = WeatherState.Success(response)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather", e)
                _weatherState.value = WeatherState.Error("Failed to load weather")
            }
        }
    }
}