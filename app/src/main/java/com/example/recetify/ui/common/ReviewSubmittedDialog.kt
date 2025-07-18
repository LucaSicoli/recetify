package com.example.recetify.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
fun ReviewSubmittedDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth() // Cambiado de 0.92f a fillMaxWidth() para mayor ancho
                .padding(12.dp), // Mantengo el padding externo
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp), // Ligeramente menos padding interno
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icono de éxito con círculo verde
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFFDCFCE7), // Verde claro
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF16A34A) // Verde
                    )
                }

                // Título principal
                Text(
                    text = "¡Tu reseña fue enviada!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Sen,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )

                // Mensaje explicativo
                Text(
                    text = "Recordá que los usuarios podrán verla una vez que haya sido aprobada por la administración.",
                    fontSize = 16.sp,
                    fontFamily = Sen,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Información adicional con layout mejorado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Proceso de moderación",
                                fontSize = 15.sp,
                                fontFamily = Sen,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "Nuestro equipo revisará tu reseña en las próximas 24 horas",
                            fontSize = 13.sp,
                            fontFamily = Sen,
                            color = Color(0xFF6B7280),
                            lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Botón de confirmación
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBC6154)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "¡Perfecto!",
                        fontFamily = Sen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
