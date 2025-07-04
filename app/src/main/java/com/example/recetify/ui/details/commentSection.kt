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
    maxLines: Int = Int.MAX_VALUE
) {
    Box(
        modifier
            .background(Color(0xFFF8F8F8), RoundedCornerShape(6.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
            .padding(12.dp)
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
                fontSize = 14.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Cada reseña individual con sombra envolvente y fondo claro:
 */
@Composable
fun CommentCard(rating: RatingResponse) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Flechita antes del alias
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF042628),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))

                // Alias con la fuente Destacado
                Text(
                    text = rating.userAlias,
                    fontFamily = Destacado,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF042628)
                )

                Spacer(Modifier.weight(1f))

                // Estrellitas
                StarRow(puntos = rating.puntos)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = rating.comentario,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
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
                    .size(16.dp)
                    .padding(end = 2.dp)
            )
        }
    }
}