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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import com.example.sawit.R
import kotlinx.coroutines.delay
import java.time.format.TextStyle
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCountdownTimer(targetDate: Date) {
    var timeRemaining by remember { mutableLongStateOf(targetDate.time - System.currentTimeMillis()) }

    LaunchedEffect(key1 = targetDate) {
        while (true) {
            val current = System.currentTimeMillis()
            val diff = targetDate.time - current

            if (diff <= 0) {
                timeRemaining = 0
                break
            }

            timeRemaining = diff

            val millisUntilNextMinute = 60000L - (current % 60000L)
            delay(millisUntilNextMinute)
        }
    }

    val days = timeRemaining / (1000 * 60 * 60 * 24)
    val hours = (timeRemaining / (1000 * 60 * 60)) % 24
    val minutes = (timeRemaining / (1000 * 60)) % 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(150.dp),
                color = colorResource(id = R.color.bg_primary_overlay_4),
                strokeWidth = 12.dp,
                trackColor = Color(0xFFC8E6C9),
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = {
                    (timeRemaining.toFloat() / (targetDate.time - System.currentTimeMillis() + 86400000f)).coerceIn(
                        0f,
                        1f
                    )
                },
                modifier = Modifier.size(150.dp),
                color = colorResource(id = R.color.text_primary_900),
                trackColor = Color.Transparent,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${days}d ${hours}h",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B3A34)
                    )
                )
                Text(
                    text = "${minutes}m remaining",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }
    }
}