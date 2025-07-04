// app/src/main/java/com/example/recetify/ui/common/NoConnectionScreen.kt
package com.example.recetify.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
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
    Font(R.font.sen_bold,     weight = FontWeight.Bold)
)

@Composable
fun NoConnectionScreen(
    onContinueOffline: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = {}) // bloquea clicks “detrás”
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Sección superior oscura ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(Color(0xFF0D0B1F)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(24.dp))
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = "Sin conexión",
                        modifier = Modifier.size(160.dp),
                        tint = White// o el gris que quieras
                    )
                }
            }

            // ── Sección inferior blanca (30% alto) ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
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
                    // Mensaje
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO TIENES CONEXIÓN",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Sen,
                            color = Color(0xFF0D0B1F),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Te estás perdiendo poder acceder a una increíble colección de recetas",
                            fontSize = 16.sp,
                            fontFamily = Sen,
                            color = Color.Gray,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    // Botón "Continuar offline"
                    Button(
                        onClick = onContinueOffline,
                        modifier = Modifier
                            .offset( y = (-20).dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCBC6154))
                    ) {
                        Text(
                            text = "Continuar offline",
                            fontFamily = Sen,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}