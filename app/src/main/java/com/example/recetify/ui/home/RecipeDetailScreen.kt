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
fun obtenerEmoji(nombre: String): String {
    val mapa = mapOf(
        "queso" to "🧀", "queso rallado" to "🧀", "queso parmesano" to "🧀", "parmesano" to "🧀",
        "queso azul" to "🧀", "gorgonzola" to "🧀", "roquefort" to "🧀", "queso roquefort" to "🧀",
        "mozzarella" to "🧀", "muzzarella" to "🧀", "queso mozzarella" to "🧀",
        "cheddar" to "🧀", "queso cheddar" to "🧀", "queso untable" to "🧀", "queso crema" to "🧀",
        "queso fresco" to "🧀", "provolone" to "🧀", "pategrás" to "🧀", "cremoso" to "🧀",
        "gruyere" to "🧀", "brie" to "🧀", "camembert" to "🧀","queso" to "🧀", "leche" to "🥛", "leche entera" to "🥛", "leche descremada" to "🥛", "leche en polvo" to "🥛",
        "leche de soja" to "🥛", "leche de almendras" to "🥛", "leche vegetal" to "🥛",
        "crema" to "🥛", "crema de leche" to "🥛", "nata" to "🥛",
        "yogur" to "🥛", "yogurt" to "🥛", "yogur bebible" to "🥛",
        "manteca" to "🧈", "mantequilla" to "🧈", "leche" to "🥛", "huevo" to "🥚", "huevos" to "🥚", "huevo duro" to "🥚", "huevo revuelto" to "🥚",
        "clara" to "🥚", "yema" to "🥚", "clara de huevo" to "🥚", "yema de huevo" to "🥚", "huevo" to "🥚", "tomate" to "🍅",
        "papa" to "🥔", "papas" to "🥔", "patata" to "🥔", "papas fritas" to "🥔",
        "puré de papa" to "🥔", "batata" to "🍠", "boniato" to "🍠", "papa" to "🥔", "zanahoria" to "🥕", "palta" to "🥑", "harina" to "🌾",
        "agua" to "💧", "panceta" to "🥓", "pollo" to "🍗", "carne" to "🥩",
        "pescado" to "🐟", "sal" to "🧂", "azúcar" to "🍬", "cebolla" to "🧅",
        "ajo" to "🧄", "manzana" to "🍎", "banana" to "🍌", "fresa" to "🍓",
        "pan" to "🍞", "aceite" to "🫒", "arroz" to "🍚", "fideos" to "🍝",
        "spaghetti" to "🍝", "macarrones" to "🍝", "ravioles" to "🥟", "gnocchi" to "🥟",
        "chocolate" to "🍫", "manteca" to "🧈", "crema" to "🥛", "maíz" to "🌽",
        "limón" to "🍋", "lentejas" to "🌰", "champiñón" to "🍄", "seta" to "🍄",
        "hongos" to "🍄", "berenjena" to "🍆", "pepino" to "🥒", "espinaca" to "🥬",
        "rúcula" to "🥬", "lechuga" to "🥬", "espinaca" to "🥬", "rúcula" to "🥬", "acelga" to "🥬", "kale" to "🥬", "lechuga" to "🥬", "miel" to "🍯", "nuez" to "🥜",
        "almendra" to "🥜", "avena" to "🌾", "yogur" to "🥛", "jamón" to "🥩",
        "queso rallado" to "🧀", "queso parmesano" to "🧀", "gorgonzola" to "🧀",
        "queso azul" to "🧀", "mayonesa" to "🧂", "mostaza" to "🧂", "ají" to "🌶", "chile" to "🌶", "jalapeño" to "🌶", "pimiento picante" to "🌶",
        "pimentón picante" to "🌶", "tabasco" to "🌶", "wasabi" to "🌶", "curry picante" to "🌶", "tomate" to "🍅", "tomates" to "🍅", "tomate cherry" to "🍅", "tomates cherry" to "🍅",
        "puré de tomate" to "🍅", "salsa de tomate" to "🍅", "ketchup" to "🍅", "ketchup" to "🍅",
        "salsa de soja" to "🥣", "vinagre" to "🧂", "perejil" to "🌿",  "harina" to "🌾", "harina integral" to "🌾", "avena" to "🌾", "sésamo" to "🌾",
        "chia" to "🌾", "linaza" to "🌾", "trigo" to "🌾", "cebada" to "🌾", "centeno" to "🌾", "albahaca" to "🌿",
        "orégano" to "🌿", "curry" to "🌶", "pimiento" to "🌶", "pimentón" to "🌶", "carne" to "🥩", "carne picada" to "🥩", "carne molida" to "🥩", "carne de res" to "🥩",
        "vacuno" to "🥩", "bife" to "🥩", "asado" to "🥩", "jamón" to "🥩", "lomo" to "🥩",
        "ají" to "🌶", "chile" to "🌶", "jalapeño" to "🌶", "tabasco" to "🌶", "pescado" to "🐟", "atún" to "🐟", "salmón" to "🐟", "merluza" to "🐟",
        "bacalao" to "🐟", "trucha" to "🐟", "pez espada" to "🐟",  "pan" to "🍞", "pan integral" to "🍞", "pan lactal" to "🍞", "pan de campo" to "🍞",
        "tostada" to "🍞", "tostadas" to "🍞", "galleta" to "🍪", "galletita" to "🍪", "bizcochuelo" to "🍰", "torta" to "🍰", "pastel" to "🍰",
        "postre" to "🍮", "flan" to "🍮", "helado" to "🍨", "dulce de leche" to "🍯", "mermelada" to "🍯",
        "canela" to "🌰", "vainilla" to "🌼", "anís" to "🌸", "clavo de olor" to "🌿", "manzana" to "🍎", "manzana verde" to "🍏", "banana" to "🍌", "plátano" to "🍌",
        "fresa" to "🍓", "frutilla" to "🍓", "kiwi" to "🥝", "uva" to "🍇", "agua" to "💧", "jugo" to "🧃", "licuado" to "🧃", "café" to "☕", "té" to "🍵",
        "mate" to "🧉", "vino" to "🍷", "cerveza" to "🍺", "infusión" to "🍵", "pizza" to "🍕", "hamburguesa" to "🍔", "hot dog" to "🌭", "taco" to "🌮",
        "burrito" to "🌯", "sushi" to "🍣", "falafel" to "🥙", "tortilla" to "🌮",
        "hummus" to "🥣", "tempura" to "🍤", "nori" to "🌿",
        "pera" to "🍐", "cereza" to "🍒", "sandía" to "🍉", "melón" to "🍈",
        "piña" to "🍍", "ananá" to "🍍", "mango" to "🥭", "durazno" to "🍑",
        "papaya" to "🍈", "granada" to "🍎", "arándano" to "🫐", "mora" to "🫐",
        "laurel" to "🍃", "tomillo" to "🌿", "romero" to "🌿", "caldo" to "🥣",
        "galleta" to "🍪", "bizcochuelo" to "🍰", "torta" to "🍰", "pizza" to "🍕",
        "hamburguesa" to "🍔", "hot dog" to "🌭", "cerveza" to "🍺", "vino" to "🍷",
        "té" to "🍵", "café" to "☕", "mate" to "🧉", "helado" to "🍨", "postre" to "🍮",
        "dulce de leche" to "🍯", "frutilla" to "🍓", "kiwi" to "🥝", "uva" to "🍇",
        "pera" to "🍐", "cereza" to "🍒", "sandía" to "🍉", "melón" to "🍈",
        "piña" to "🍍", "ananá" to "🍍", "mango" to "🥭", "durazno" to "🍑",
        "papaya" to "🍈", "tamarindo" to "🌰", "batata" to "🍠", "mandarina" to "🍊",
        "naranja" to "🍊", "granada" to "🍎", "arándano" to "🫐", "mora" to "🫐",
        "avellana" to "🥜", "castaña" to "🌰", "soja" to "🌿", "tofu" to "🍢",
        "edamame" to "🌿", "algas" to "🌿", "wasabi" to "🌶", "misô" to "🥣",
        "sésamo" to "🌾", "chia" to "🌾", "trigo" to "🌾", "centeno" to "🌾", "sal marina" to "🧂", "sal rosada" to "🧂", "sal negra" to "🧂",
        "azúcar moreno" to "🍬", "azúcar rubia" to "🍬", "azúcar impalpable" to "🍬", "azúcar glas" to "🍬",
        "stevia" to "🍬", "edulcorante" to "🍬", "sal marina" to "🧂", "sal rosada" to "🧂", "sal negra" to "🧂",
        "azúcar moreno" to "🍬", "azúcar rubia" to "🍬", "azúcar impalpable" to "🍬", "azúcar glas" to "🍬",
        "stevia" to "🍬", "edulcorante" to "🍬","aceite de oliva" to "🫒", "aceite vegetal" to "🫒", "aceite de girasol" to "🫒", "aceite de coco" to "🫒",
        "grasa vacuna" to "🧈", "margarina" to "🧈","aceite de oliva" to "🫒", "aceite vegetal" to "🫒", "aceite de girasol" to "🫒", "aceite de coco" to "🫒",
        "grasa vacuna" to "🧈", "margarina" to "🧈","tomate en lata" to "🥫", "arvejas en lata" to "🥫", "choclo en lata" to "🥫",
        "atún en lata" to "🥫", "sardinas" to "🥫", "pickles" to "🥫", "aceitunas" to "🫒", "aceitunas verdes" to "🫒", "aceitunas negras" to "🫒","gaseosa" to "🧃", "soda" to "🧃", "agua con gas" to "🧃",
        "bebida isotónica" to "🧃", "kombucha" to "🧃",
        "chorizo" to "🥩", "salchicha" to "🌭", "morcilla" to "🥩", "fiambre" to "🥩",
        "bondiola" to "🥩", "panceta ahumada" to "🥓", "tocino" to "🥓",
        "seitán" to "🥗", "tempeh" to "🥗", "hamburguesa vegetal" to "🥗",
        "leche de avena" to "🥛", "queso vegano" to "🧀", "pimienta" to "🌶", "pimienta negra" to "🌶", "pimienta blanca" to "🌶", "pimienta rosa" to "🌶",
        "pimienta verde" to "🌶", "pimienta roja" to "🌶", "pimienta molida" to "🌶", "pimienta en grano" to "🌶",
        "pimienta recién molida" to "🌶", "mezcla de pimientas" to "🌶", "cuatro pimientas" to "🌶",
        "pimienta cayena" to "🌶", "cayena" to "🌶", "pimienta de cayena" to "🌶",
        "pimienta de jamaica" to "🌶", "allspice" to "🌶",
        "pimienta negra molida" to "🌶", "pimienta negra en grano" to "🌶",

        "portobello" to "🍄", "champiñones" to "🍄", "shitake" to "🍄", "boletus" to "🍄",
        "empanada" to "🥟", "empanadas" to "🥟", "tartaleta" to "🍰", "croissant" to "🥐", "medialuna" to "🥐",
        "baguette" to "🥖", "brioche" to "🥐", "pan árabe" to "🥙", "pita" to "🥙",
        "esencia de vainilla" to "🌼", "esencia de almendra" to "🌰",
        "extracto de café" to "☕", "agua de azahar" to "🌼", "ralladura de limón" to "🍋", "ralladura de naranja" to "🍊",
        "levadura" to "🧪", "levadura seca" to "🧪", "polvo de hornear" to "🧪",
        "bicarbonato de sodio" to "🧪", "gelatina" to "🧪", "agar agar" to "🧪",
        "congelado" to "🧊", "rallado" to "🔪", "triturado" to "🔪",
        "picado" to "🔪", "fileteado" to "🔪", "cocido" to "🔥", "crudo" to "🥗",
        "hervido" to "🍲", "al vapor" to "♨️", "a la plancha" to "🍳",
        "cebada" to "🌾", "hummus" to "🥣", "falafel" to "🥙", "tortilla" to "🌮",
        "taco" to "🌮", "burrito" to "🌯", "sushi" to "🍣", "nori" to "🌿",
        "tempura" to "🍤", "wasabi" to "🌶", "salmón" to "🐟", "atun" to "🐟","echalote" to "🧄", "cebollín" to "🧄", "cebolla de verdeo" to "🧄", "cebolleta" to "🧄",
        "puerro" to "🧄", "jengibre" to "🧄", "ajo en polvo" to "🧄", "cebolla en polvo" to "🧅"

        )
    return mapa.entries.firstOrNull { nombre.lowercase().contains(it.key) }?.value ?: "🍽"
}

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
