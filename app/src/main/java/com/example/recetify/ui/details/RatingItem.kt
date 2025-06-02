package com.example.recetify.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recetify.data.remote.model.RatingResponse

@Composable
fun RatingItem(rating: RatingResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "${rating.userAlias} • ${rating.puntos}⭐", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(text = rating.comentario)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Publicado el ${rating.fecha.substring(0, 10)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
