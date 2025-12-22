package com.example.sawit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawit.viewmodels.WeatherUIState

@Composable
fun WeatherCard(state: WeatherUIState) {
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF709ED2), Color(0xFF5384BE)))
    )) {
        when (state) {
            is WeatherUIState.Loading -> CircularProgressIndicator(color = Color.White)
            is WeatherUIState.Success -> {
                Row(modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.city, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(state.temp, fontSize = 48.sp, color = Color.White)
                        Text(state.condition, fontSize = 20.sp, color = Color.White)
                    }
                    // Load icon via Coil
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${state.iconCode}@2x.png",
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
            else -> Text("Check GPS & Internet", color = Color.White)
        }
    }
}