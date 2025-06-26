package com.example.recetify.ui.common


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import java.net.URI

@Composable
fun RecipeCardDraft(
    recipe: RecipeResponse,
    navController: NavController
) {
    val base     = RetrofitClient.BASE_URL.trimEnd('/')
    val original = recipe.fotoPrincipal.orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
    }.getOrNull() ?: original
    val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"

    Card(
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("editDraft/${recipe.id}")

            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = finalUrl,
                    contentDescription = recipe.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )

                // Badge "Borrador"
                Text(
                    text = "BORRADOR",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            color = Color(0xFFE29587),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(12.dp))

            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = recipe.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(recipe.usuarioCreadorAlias.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Color.Black)

                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Outlined.Star, contentDescription = null, tint = Color(0xFFe29587), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("%.1f".format(recipe.promedioRating ?: 0.0), style = MaterialTheme.typography.bodySmall, color = Color(0xFFe29587))

                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${recipe.tiempo} min", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = recipe.descripcion.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
