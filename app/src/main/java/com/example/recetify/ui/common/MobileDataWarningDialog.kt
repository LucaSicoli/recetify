package com.example.recetify.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.recetify.R

// Declaramos la fuente
private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold, weight = FontWeight.Bold),
    Font(R.font.sen_semibold, weight = FontWeight.SemiBold)
)

@Composable
fun MobileDataWarningDialog(
    onContinueWithMobileData: () -> Unit,
    onWaitForWifi: () -> Unit
) {
    Dialog(
        onDismissRequest = onWaitForWifi,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono de advertencia
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFFFFF3CD), // Amarillo claro
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellular4Bar,
                        contentDescription = "Datos móviles",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF856404) // Amarillo oscuro
                    )
                }

                // Título
                Text(
                    text = "¿Usar datos móviles?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Sen,
                    color = Color(0xFF2D3748),
                    textAlign = TextAlign.Center
                )

                // Mensaje principal
                Text(
                    text = "Estás conectado solo con datos móviles. Subir fotos y videos puede consumir mucho de tu plan de datos.",
                    fontSize = 16.sp,
                    fontFamily = Sen,
                    color = Color(0xFF4A5568),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFED8936),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Te recomendamos usar WiFi para subir recetas con imágenes y videos",
                            fontSize = 14.sp,
                            fontFamily = Sen,
                            color = Color(0xFF2D3748),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Botones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón principal - Esperar WiFi
                    Button(
                        onClick = onWaitForWifi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4299E1)
                        )
                    ) {
                        Text(
                            text = "Esperar a conectar WiFi",
                            fontFamily = Sen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    // Botón secundario - Continuar con datos
                    OutlinedButton(
                        onClick = onContinueWithMobileData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFBC6154)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            Color(0xFFBC6154)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Continuar con datos móviles",
                            fontFamily = Sen,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
