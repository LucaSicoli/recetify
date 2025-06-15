package com.example.recetify.ui.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetify.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import java.net.URI


@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = viewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortField by viewModel.sortField.collectAsState()
    val isAscending by viewModel.isAscending.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val recipes by viewModel.filteredAndSortedRecipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Search Input
        SearchInput(searchQuery = searchQuery, onSearchQueryChanged = { viewModel.setSearchQuery(it) })

        Spacer(modifier = Modifier.height(16.dp))

        // Bubble Tabs
        BubbleTabs(selectedIndex = selectedTab, onTabSelected = { viewModel.setSelectedTab(it) })

        Spacer(modifier = Modifier.height(16.dp))

        // Sort and Title Row
        SortAndTitleRow(
            title = if (selectedTab == 0) "Mis Recetas" else "Favoritas",
            selectedField = sortField,
            isAscending = isAscending,
            onFieldSelected = { viewModel.setSortField(it) },
            onToggleOrder = { viewModel.toggleOrder() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de recetas o loader o empty state
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "No favorites",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0) "No tienes recetas propias" else "No tienes recetas favoritas",
                        color = Color.Gray
                    )
                }
            }
        } else {
            RecipeList(recipes = recipes, viewModel = viewModel)
        }
    }
}


@Composable
fun SortAndTitleRow(
    title: String,
    selectedField: String,
    isAscending: Boolean,
    onFieldSelected: (String) -> Unit,
    onToggleOrder: () -> Unit
) {
    val sortOptions = listOf("Nombre", "Autor", "Calificación")
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clickable { expanded = true }
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedField, fontSize = 13.sp)
                    IconButton(
                        onClick = onToggleOrder,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Orden",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onFieldSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchInput(searchQuery: String, onSearchQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = { Text("Buscar recetas...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedBorderColor = Color(0xFFD36B5A)
        )
    )
}

@Composable
fun BubbleTabs(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabTitles = listOf("Mis Recetas", "Favoritos")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabTitles.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            val backgroundColor = if (isSelected) Color(0xFFD36B5A) else Color(0xFFF0F0F0)
            val textColor = if (isSelected) Color.White else Color.Black

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = title, color = textColor, fontWeight = FontWeight.Medium)
            }
        }
    }
}

fun getRecipeImageUrl(recipe: RecipeResponse): String {
    val base = RetrofitClient.BASE_URL.trimEnd('/')
    val original = recipe.fotoPrincipal.orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
    }.getOrNull() ?: original
    return if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"
}

@Composable
fun RecipeList(recipes: List<RecipeResponse>, viewModel: FavoritesViewModel) {
    LazyColumn {
        items(recipes) { recipe ->
            RecipeItem(
//                title = recipe.nombre ?: "Sin nombre",
//                author = recipe.usuarioCreadorAlias ?: "Desconocido",
//                time = recipe.tiempo?.toString() ?: "N/A",
//                rating = recipe.promedioRating?.toString() ?: "0"
                recipe = recipe,
                viewModel = viewModel
            )
        }
    }
}



@Composable
fun RecipeItem(
    recipe: RecipeResponse,
    viewModel: FavoritesViewModel,

//    title: String,
//    author: String,
//    time: String,
//    rating: String,
    modifier: Modifier = Modifier
) {
    // Get the current favorite state (initially false)
    var isFavorite by remember { mutableStateOf(false) }

    // Check if this recipe is favorited by the user
    LaunchedEffect(recipe.id) {
        isFavorite = viewModel.isRecipeFavorited(recipe.id)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 0.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val imageUrl = getRecipeImageUrl(recipe)

            // Imagen + Rating
            Box() {
//                modifier = Modifier.size(76.dp)
//                Image(
//                    painter = painterResource(id = R.drawable.logo_chef),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(16.dp)),
//                    contentScale = ContentScale.Crop
//                )
                val aspectRatio = 3f / 4f
                AsyncImage(
                    model = imageUrl,
                    contentDescription = recipe.nombre ?: "Imagen de receta",
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(94.dp / aspectRatio)
//                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(topEnd = 6.dp, bottomStart = 12.dp),
                            clip = false
                        )
                        .background(
                            color = Color(0xFFE26D5A),
                            shape = RoundedCornerShape(topEnd = 6.dp, bottomStart = 12.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .height(20.dp)
                        .width(30.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recipe.promedioRating?.toString() ?: "N/A",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(10.dp)
                                .offset(y = (1).dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Info y acciones
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Primera fila: Título + Corazón
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = recipe.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(recipe.id, isFavorite)
                            isFavorite = !isFavorite
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = Color(0xFFE26D5A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Segunda fila: autor a la izquierda y tiempo a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFE26D5A), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = recipe.usuarioCreadorAlias ?: "Desconocido", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = recipe.tiempo?.toString() ?: "n/a", fontSize = 12.sp, color = Color.Gray)
                        Text(text = " min", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    FavoritesScreen()
}