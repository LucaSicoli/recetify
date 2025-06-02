package com.example.recetify.ui.details

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.util.obtenerEmoji

@Composable
fun RecipeDetailContent(
    receta: RecipeResponse,
    padding: PaddingValues,
    showIngredients: MutableState<Boolean>,
    currentStep: MutableState<Int>,
    navController: NavController
) {
    val primaryTextColor = Color(0xFF042628)
    val selectedButtonColor = Color(0xFF042628)
    val unselectedButtonColor = Color(0xFFE6EBF2)
    val unselectedTextColor = Color(0xFF042628)
    val ingredientCardColor = Color.White
    val ingredientIconBackground = Color(0xFFE6EBF2)
    val unitBackgroundColor = Color(0xFF995850)
    val unitTextColor = Color.White

    val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
    val imgPath = receta.fotoPrincipal?.removePrefix("http://localhost:8080") ?: ""
    val fullUrl = "$baseUrl$imgPath"

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            AsyncImage(
                model = fullUrl,
                contentDescription = receta.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }

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
                            showIngredients.value = true
                            currentStep.value = 0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showIngredients.value) selectedButtonColor else unselectedButtonColor,
                            contentColor = if (showIngredients.value) Color.White else unselectedTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Ingredientes") }

                    Button(
                        onClick = {
                            showIngredients.value = false
                            currentStep.value = 0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showIngredients.value) selectedButtonColor else unselectedButtonColor,
                            contentColor = if (!showIngredients.value) Color.White else unselectedTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Instrucciones") }
                }

                Spacer(Modifier.height(16.dp))
                if (showIngredients.value) {
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
                                        Text(obtenerEmoji(it.nombre), fontSize = 20.sp)
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
                    if (currentStep.value in pasos.indices) {
                        val paso = pasos[currentStep.value]
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
                                    onClick = { currentStep.value++ },
                                    enabled = currentStep.value < pasos.lastIndex,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = unselectedButtonColor)
                                ) {
                                    Text("Paso Siguiente", color = primaryTextColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
