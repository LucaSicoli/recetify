package com.example.recetify.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetify.R
import com.example.recetify.data.remote.model.RatingResponse

/**
 * CommentsSection:
 *
 * 1) Agrupa todas las reseñas en un solo Card de fondo blanco con esquinas redondeadas.
 * 2) Dentro: encabezado “Reseñas” + promedio + total.
 * 3) Cada item (CommentCard) se dibuja con su propio Card pequeño (esquinas redondeadas, sombra).
 * 4) Botón “Ver más / Ver menos” si hay más de 2 reseñas.
 * 5) Debajo del Card de reseñas, otro Card independiente para “Dejá tu comentario”.
 */

private val Destacado = FontFamily(
    Font(R.font.sen_semibold, weight = FontWeight.ExtraBold)
)

@Composable
fun StyledBasicField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    maxLines: Int = Int.MAX_VALUE,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp // Nuevo parámetro con valor por defecto
) {
    Box(
        modifier
            .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .padding(0.dp) // sin padding aquí, lo damos al TextField
            .height(200.dp) // más alto aún
    ) {
        if (value.text.isEmpty() && placeholder != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                placeholder()
            }
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            maxLines = maxLines,
            cursorBrush = SolidColor(Color.Black),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontFamily = Destacado,
                fontSize = fontSize // Usar el nuevo parámetro
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(20.dp) // padding interno grande
        )
    }
}

/**
 * Cada reseña individual con sombra envolvente y fondo claro:
 */
@Composable
fun CommentCard(rating: RatingResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .defaultMinSize(minHeight = 0.dp)
            .wrapContentHeight(), // igual que ingredientes
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // igual que ingredientes
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna para el contenido textual
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color(0xFF042628),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = rating.userAlias,
                        fontFamily = Destacado,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF042628)
                    )
                    Spacer(Modifier.weight(1f))
                    StarRow(puntos = rating.puntos)
                }
                Text(
                    text = rating.comentario,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
                    color = Color(0xFF424242),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StarRow(puntos: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= puntos) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= puntos) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 2.dp)
            )
        }
    }
}