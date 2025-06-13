package com.example.recetify.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    onRetry: () -> Unit,
    onContinueOffline: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Sección oscura (60% alto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(Color(0xFF0D0B1F)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_chef),
                    contentDescription = "Logo Recetify",
                    modifier = Modifier.size(110.dp)
                )
                Spacer(Modifier.height(24.dp))
                Image(
                    painter = painterResource(R.drawable.no_wifi),
                    contentDescription = "Sin conexión",
                    modifier = Modifier.size(180.dp)
                )
            }
        }

        // Sección blanca (40% alto) superpuesta con esquinas redondeadas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
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

                // Botones
                Column {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D0B1F),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "REINTENTAR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Sen
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onContinueOffline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xCCBC6154),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "CONTINUAR OFFLINE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Sen
                        )
                    }
                }
            }
        }
    }
}