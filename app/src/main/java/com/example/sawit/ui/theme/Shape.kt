package com.example.sawit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val FormCardCorners = RoundedCornerShape(
    topStart = 32.dp,
    topEnd = 32.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

val WeatherCardCorners = RoundedCornerShape(16.dp)

val ActivityCardCorners = RoundedCornerShape(16.dp)

val ActivityCardItemCorners = RoundedCornerShape(8.dp)

val TopHeaderCardCorners = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 32.dp,
    bottomEnd = 32.dp
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)