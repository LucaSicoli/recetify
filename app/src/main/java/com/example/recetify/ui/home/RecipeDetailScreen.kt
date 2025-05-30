package com.example.recetify.ui.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(id: String) {
    var recipe by remember { mutableStateOf<RecipeResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showIngredients by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(0) }

    LaunchedEffect(id) {
        try {
            recipe = RetrofitClient.api.getRecipeById(id.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    val primaryTextColor = Color(0xFF042628)
    val selectedButtonColor = Color(0xFF042628)
    val unselectedButtonColor = Color(0xFFE6EBF2)
    val unselectedTextColor = Color(0xFF042628)
    val ingredientCardColor = Color.White
    val ingredientIconBackground = Color(0xFFE6EBF2)
    val unitBackgroundColor = Color(0xFF995850)
    val unitTextColor = Color.White

    Scaffold(containerColor = Color(0xFFF9FAFA)) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            recipe?.let { receta ->
                val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
                val imgPath = receta.fotoPrincipal?.removePrefix("http://localhost:8080") ?: ""
                val fullUrl = "$baseUrl$imgPath"

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = receta.nombre,
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentScale = ContentScale.Crop
                    )

                    Surface(
                        modifier = Modifier.offset(y = (-24).dp),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = Color.White,
                        tonalElevation = 4.dp
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text(receta.nombre, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = primaryTextColor)
                            Spacer(Modifier.height(4.dp))
                            Text(receta.descripcion ?: "", style = MaterialTheme.typography.bodyMedium, color = primaryTextColor)

                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = primaryTextColor)
                                    Spacer(Modifier.width(6.dp))
                                    Text("${receta.tiempo} min", color = primaryTextColor)
                                }
                                Text("Porciones: ${receta.porciones}", color = primaryTextColor)
                                Button(onClick = {}, enabled = false, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = selectedButtonColor)) {
                                    Text("Personalizar", color = Color.White)
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                                Button(
                                    onClick = {
                                        showIngredients = true
                                        currentStep = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (showIngredients) selectedButtonColor else unselectedButtonColor,
                                        contentColor = if (showIngredients) Color.White else unselectedTextColor
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Ingredientes") }

                                Button(
                                    onClick = {
                                        showIngredients = false
                                        currentStep = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!showIngredients) selectedButtonColor else unselectedButtonColor,
                                        contentColor = if (!showIngredients) Color.White else unselectedTextColor
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Instrucciones") }
                            }

                            Spacer(Modifier.height(16.dp))
                            if (showIngredients) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Ingredientes", style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
                                    Text("${receta.ingredients.size} Items", style = MaterialTheme.typography.bodySmall, color = primaryTextColor)
                                }
                                Spacer(Modifier.height(8.dp))
                                receta.ingredients.forEach {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = ingredientCardColor),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(ingredientIconBackground),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🍽")
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Text(it.nombre, fontWeight = FontWeight.SemiBold, color = primaryTextColor)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = unitBackgroundColor
                                            ) {
                                                Text(
                                                    text = "${it.cantidad} ${it.unidadMedida}",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    color = unitTextColor,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                            } else {
                                Text("Instrucciones", style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
                                Spacer(Modifier.height(12.dp))
                                val pasos = receta.steps.sortedBy { it.numeroPaso }
                                if (currentStep in pasos.indices) {
                                    val paso = pasos[currentStep]
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text("${paso.numeroPaso}. ${paso.titulo}", fontWeight = FontWeight.Bold, color = primaryTextColor)
                                            if (!paso.urlMedia.isNullOrBlank()) {
                                                Spacer(Modifier.height(8.dp))
                                                AsyncImage(
                                                    model = baseUrl + paso.urlMedia,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            if (!paso.descripcion.isNullOrBlank()) {
                                                Spacer(Modifier.height(8.dp))
                                                Text(paso.descripcion, color = primaryTextColor)
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Button(
                                                onClick = { currentStep++ },
                                                enabled = currentStep < pasos.lastIndex,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = unselectedButtonColor)
                                            ) {
                                                Text("Paso Siguiente", color = primaryTextColor)
                                            }
                                        }
                                    }
                                }
                            }

                            // Autor
                            Spacer(Modifier.height(24.dp))
                            Text("Autor", style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.LightGray))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("${receta.usuarioCreadorAlias ?: "Autor desconocido"}", fontWeight = FontWeight.Bold, color = primaryTextColor)
                                    Text("Apasionado por la cocina", style = MaterialTheme.typography.bodySmall, color = primaryTextColor)
                                }
                            }

                            // Reseñas
                            Spacer(Modifier.height(24.dp))
                            Text("Reseñas", style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
                            Spacer(Modifier.height(8.dp))
                            Text("4.2 ★ (35 reseñas)", style = MaterialTheme.typography.bodySmall, color = primaryTextColor)
                            Spacer(Modifier.height(8.dp))
                            listOf(
                                "Muy rico y fácil de preparar. Los platos son abundantes así que tranquilamente pueden picar 3 personas. Lo super recomiendo." to "Juan Perez",
                                "Es una ensalada como cualquier otra. Para mí le faltaba una salsa que acompañe o algo para que no quede tan seca. Igualmente esta buena la idea y es original." to "Mariana Suarez"
                            ).forEach { (comentario, autor) ->
                                Card(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("› $autor", fontWeight = FontWeight.SemiBold, color = primaryTextColor)
                                        Spacer(Modifier.height(4.dp))
                                        Text(comentario, color = primaryTextColor)
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("Deja tu comentario", style = MaterialTheme.typography.titleMedium, color = primaryTextColor)
                            Spacer(Modifier.height(4.dp))
                            TextField(
                                value = "",
                                onValueChange = {},
                                placeholder = { Text("Contanos que te pareció la receta...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )
                            Spacer(Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}
