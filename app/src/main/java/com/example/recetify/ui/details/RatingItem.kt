// RatingItem.kt
package com.example.recetify.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color       // <— IMPORTAR
import androidx.compose.ui.text.font.FontWeight // <— IMPORTAR
import androidx.compose.ui.unit.dp
import com.example.recetify.data.remote.model.RatingResponse

@Composable
fun RatingItem(rating: RatingResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Alias y puntos en negrita y negro
        Text(
            text = "${rating.userAlias} • ${rating.puntos}⭐",
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))

        // Comentario en negro normal
        Text(
            text = rating.comentario,
            color = Color.Black
        )
        Spacer(Modifier.height(4.dp))

        // Fecha en un tono más suave (puede quedar gris oscuro, o dejarse negro claro)
        Text(
            text = "Publicado el ${rating.fecha.substring(0, 10)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}
