package com.example.recetify.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    // Carga la animación Lottie desde assets
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("loading_lottie.json"))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    Box(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier.size(320.dp)
        )
    }
}
