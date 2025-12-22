package com.example.sawit.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawit.BuildConfig
import com.example.sawit.api.RetrofitClient
import com.example.sawit.data.remote.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData
    private val _weatherState = MutableStateFlow<WeatherUIState>(WeatherUIState.Loading)
    val weatherState = _weatherState.asStateFlow()

    fun fetchWeatherIfNeeded(lat: Double, lon: Double) {
        if (_weatherData.value != null) return

        viewModelScope.launch {
            val result = RetrofitClient.weatherService.getWeather(lat, lon, BuildConfig.OPENWEATHERMAP_API_KEY)
            _weatherData.value = result
        }
    }

    fun fetchWeather(lat: Double, lon: Double) {
        val currentTime = System.currentTimeMillis()
        if (weatherState.value is WeatherState.Success && (currentTime - lastFetchTime < 20 * 60 * 1000)) {
            Log.d("WeatherVM", "Using cached weather data")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.weatherService.getWeather(lat, lon, BuildConfig.OPENWEATHERMAP_API_KEY)
                _weatherState.value = WeatherUIState.Success(
                    city = response.name,
                    temp = "${response.main.temp.toInt()}°C",
                    condition = response.weather.firstOrNull()?.main ?: "Unknown",
                    iconCode = response.weather.firstOrNull()?.icon ?: ""
                )
            } catch (e: Exception) {
                _weatherState.value = WeatherUIState.Error
            }
        }
    }
}

sealed class WeatherUIState {
    object Loading : WeatherUIState()
    data class Success(val city: String, val temp: String, val condition: String, val iconCode: String) : WeatherUIState()
    object Error : WeatherUIState()
}