package com.example.recetify.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.recetify.R

// Declaramos la fuente igual que en Login
private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold, weight = FontWeight.Bold)
)

@Composable
fun NoConnectionScreen(
    onRetry: () -> Unit = {},
    onContinueOffline: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = {}) // Captura toques para bloquear interacción trasera
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sección oscura (60% alto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(Color(0xFF0D0B1F)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_chef),
                        contentDescription = "Logo Recetify",
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Image(
                        painter = painterResource(R.drawable.no_wifi),
                        contentDescription = "Sin conexión",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            // Sección blanca (40% alto) superpuesta con esquinas redondeadas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
                    .offset(y = (-24).dp)
                    .zIndex(1f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mensaje central
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO TIENES CONEXIÓN",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Sen,
                            color = Color(0xFF0D0B1F)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Te estás perdiendo poder acceder a una increíble colección de recetas",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}