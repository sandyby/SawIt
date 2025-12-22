package com.example.sawit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawit.R
import com.example.sawit.data.remote.WeatherResponse
import com.example.sawit.viewmodels.WeatherState
import java.util.Locale
import java.util.Locale.getDefault

@Composable
fun WeatherCard(state: WeatherState) {
    // Premium Gradient Background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF709ED2),
            Color(0xFF5A86B8)
        )
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            when (state) {
                is WeatherState.Loading -> {
                    LoadingView()
                }

                is WeatherState.Error -> {
                    ErrorView(message = state.message)
                }

                is WeatherState.Success -> {
                    WeatherContent(data = state.data, province = state.province)
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Loading Weather...", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_filled_warning_24_grayed_1),
            contentDescription = "Error",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Weather unavailable",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Please check internet/GPS",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun WeatherContent(data: WeatherResponse, province: String) {
    val temp = data.main.temp.toInt()
    val humidity = data.main.humidity
    val condition = data.weather.firstOrNull()?.main ?: "Clear"
    val description = data.weather.firstOrNull()?.description?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    } ?: ""
    val city = data.cityName
    val iconCode = data.weather.firstOrNull()?.icon ?: "01d"

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filled_location_on_24_white),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = city,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (province.isNotEmpty()) {
                Text(
                    text = province,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        }

        Column {
            Text(
                text = "$temp°",
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 64.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_filled_water_drop_24_black),
                contentDescription = "Humidity",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Humidity: $humidity%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Image(
            painter = painterResource(id = getWeatherIcon(iconCode)),
            contentDescription = condition,
            modifier = Modifier
                .size(120.dp)
                .offset(x = 10.dp, y = 0.dp)
        )
    }
}

// Map OpenWeatherMap icon codes to your drawable resources
fun getWeatherIcon(code: String): Int {
    return when (code) {
        // Clear
        "01d" -> R.drawable.ic_clear_day_64
        "01n" -> R.drawable.ic_clear_night_64

        // Clouds
        "02d", "03d", "04d" -> R.drawable.ic_cloudy_64
        "02n", "03n", "04n" -> R.drawable.ic_cloudy_1_night_64

        // Rain
        "09d", "10d" -> R.drawable.ic_rainy_3_64
        "09n", "10n" -> R.drawable.ic_rainy_3_night_64

        // Thunderstorm
        "11d", "11n" -> R.drawable.ic_thunderstorms_64

        // Snow (Optional, unlikely in Indonesia but good to handle)
        "13d" -> R.drawable.ic_snowy_2_64
        "13n" -> R.drawable.ic_snowy_2_night_64

        // Mist/Fog
        "50d", "50n" -> R.drawable.ic_fog_64

        else -> R.drawable.ic_cloudy_2_day_64
    }
}