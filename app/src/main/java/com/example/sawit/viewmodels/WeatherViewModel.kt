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
    private var lastFetchTimestamp: Long = 0
    private val CACHE_DURATION = 30 * 60 * 1000L

    fun isWeatherStale(): Boolean {
        val currentTime = System.currentTimeMillis()
        val isDataMissing = _weatherState.value !is WeatherState.Success
        val isTimeExpired = (currentTime - lastFetchTimestamp) > CACHE_DURATION

        return isDataMissing || isTimeExpired
    }

    fun fetchWeather(lat: Double, lon: Double, forceRefresh: Boolean = false) {
        if (!forceRefresh && !isWeatherStale()) {
            Log.d("WeatherViewModel", "Using cached data. Skipping API call.")
            return
        }
        viewModelScope.launch {
            try {
                _weatherState.value = WeatherState.Loading
                withContext(Dispatchers.IO) {
                    val weatherResponse =
                        RetrofitClient.weatherService.getCurrentWeather(lat, lon, API_KEY, "metric")
                    var provinceName = ""
                    try {
                        val geocoder = Geocoder(application, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            provinceName = addresses[0].adminArea ?: ""
                        }
                        _weatherState.value = WeatherState.Success(weatherResponse, provinceName)
                        lastFetchTimestamp = System.currentTimeMillis()
                    } catch (e: Exception) {
                        Log.e("WeatherVM", "Geocoder failed", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather", e)
                _weatherState.value = WeatherState.Error("Failed to load weather")
            }
        }
    }
}