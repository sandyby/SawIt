package com.example.sawit.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.format.TextStyle
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCountdownTimer(targetDate: Date) {
    var timeRemaining by remember { mutableLongStateOf(targetDate.time - System.currentTimeMillis()) }

    LaunchedEffect(key1 = targetDate) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining = targetDate.time - System.currentTimeMillis()
        }
    }

    val days = (timeRemaining / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
    val hours = ((timeRemaining / (1000 * 60 * 60)) % 24).coerceAtLeast(0)
    val minutes = ((timeRemaining / (1000 * 60)) % 60).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = Color(0xFFE0E0E0),
                strokeWidth = 8.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${days}d ${hours}h",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        color = Color(0xFF1B3A34)
                    )
                )
                Text(
                    text = "${minutes}m left",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}