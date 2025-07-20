package com.example.recetify.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BookmarkBorder
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

private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold, weight = FontWeight.Bold),
    Font(R.font.sen_semibold, weight = FontWeight.SemiBold)
)

@Composable
fun RecipePublishedDialog(
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
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFDCFCE7), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Publicado",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF16A34A)
                    )
                }
                Text(
                    text = "¡Receta publicada!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Sen,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tu receta fue enviada y está pendiente de aprobación por la administración. Una vez aprobada, otros usuarios podrán verla.",
                    fontSize = 16.sp,
                    fontFamily = Sen,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        Text(
                            text = "Moderación",
                            fontSize = 15.sp,
                            fontFamily = Sen,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                        Text(
                            text = "La revisión puede demorar hasta 24 horas.",
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
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBC6154))
                ) {
                    Text(
                        text = "¡Entendido!",
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

@Composable
fun RecipeSavedDialog(
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
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE0E7FF), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Borrador guardado",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF3B82F6)
                    )
                }
                Text(
                    text = "¡Receta guardada como borrador!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Sen,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Puedes editar o publicar tu receta desde la lista de borradores en cualquier momento.",
                    fontSize = 16.sp,
                    fontFamily = Sen,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        Text(
                            text = "¿Dónde encontrarla?",
                            fontSize = 15.sp,
                            fontFamily = Sen,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                        Text(
                            text = "Ve a la sección de borradores en tu perfil para editar o publicar tu receta.",
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
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBC6154))
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

