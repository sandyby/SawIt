package com.example.sawit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sawit.R

private val LightColorScheme = lightColorScheme(
    primary = BgPrimary500,
    secondary = BgSecondaryOverlay2,
    background = BgSecondary900,
    surface = White,
    error = TextError900,
    onPrimary = White,
    onSecondary = Text600,
    onBackground = TextPrimary900,
    onSurface = Text900,
    onError = White
)

val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        fontFamily = FontFamily(Font(R.font.lato_bold))
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = FontFamily(Font(R.font.lato_bold))
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        fontFamily = FontFamily(Font(R.font.lato_regular))
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        fontFamily = FontFamily(Font(R.font.lato_bold))
    ),
)

@Composable
fun SawItTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}