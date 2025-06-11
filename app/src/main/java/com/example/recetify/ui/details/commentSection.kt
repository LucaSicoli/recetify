package com.example.recetify.ui.details

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
@Composable
fun CommentsSection(
    ratings: List<RatingResponse>,
    onSend: (comentario: String, puntos: Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val previewCount = 2

    // Si está expandido o hay 2 o menos, muestro todas; si no, tomo sólo las primeras dos
    val displayList = remember(ratings, expanded) {
        if (expanded || ratings.size <= previewCount) ratings
        else ratings.take(previewCount)
    }

    // Promedio en 1 decimal
    val averageRating = remember(ratings) {
        if (ratings.isEmpty()) 0.0 else String.format("%.1f", ratings.map { it.puntos }.average()).toDouble()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // margen externo
    ) {
        // ── 1) Card principal para la sección “Reseñas” ────────────────────
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize() // anima al expandir/colapsar
            ) {
                // ── Encabezado “Reseñas” ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reseñas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF042628)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "%.1f".format(averageRating),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF042628)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Estrella promedio",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${ratings.size} reseñas)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // ── Lista de CommentCard ────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    displayList.forEachIndexed { index, rating ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        CommentCard(rating = rating)
                    }

                    if (ratings.size > previewCount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(top = 8.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (expanded) "Ver menos" else "Ver más",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF042628),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF042628)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 2) Card independiente para el formulario “Dejá tu comentario” ─
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                draggedElevation = 0.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dejá tu comentario",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF042628)
                )

                Spacer(modifier = Modifier.height(12.dp))

                var commentStars by remember { mutableStateOf(0) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { commentStars = i },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= commentStars) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (i <= commentStars) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var textComment by remember { mutableStateOf(TextFieldValue("")) }
                val maxChars = 100
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = textComment,
                        onValueChange = {
                            if (it.text.length <= maxChars) textComment = it
                        },
                        placeholder = { Text("Contanos qué te pareció la receta…", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        textStyle = LocalTextStyle.current.copy(color = Color.Black)
                    )
                    Text(
                        text = "${textComment.text.length}/$maxChars",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onSend(textComment.text.trim(), commentStars)
                            textComment = TextFieldValue("")
                            commentStars = 0
                        },
                        enabled = textComment.text.isNotBlank() && commentStars > 0,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF042628))
                    ) {
                        Text(
                            text = "Enviar",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cada reseña individual con sombra envolvente y fondo claro:
 */
@Composable
fun CommentCard(rating: RatingResponse) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // separación entre tarjetas
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = rating.userAlias,
                    color = Color(0xFF042628),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                StarRow(puntos = rating.puntos)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = rating.comentario,
                color = Color(0xFF424242),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Publicado el ${rating.fecha.substring(0, 10)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575).copy(alpha = 0.8f),
                fontSize = 12.sp
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
