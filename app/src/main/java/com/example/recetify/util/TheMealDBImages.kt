package com.example.recetify.util

/**
 * Utilidad para obtener URLs de imágenes de ingredientes desde TheMealDB
 */
object TheMealDBImages {

    private const val BASE_INGREDIENT_URL = "https://www.themealdb.com/images/ingredients"

    /**
     * Mapa de ingredientes en español a sus nombres en inglés para TheMealDB
     */
    private val ingredientImageMap = mapOf(
        // Carnes
        "carne de res" to "Beef",
        "carne" to "Beef",
        "pollo" to "Chicken",
        "cerdo" to "Pork",
        "cordero" to "Lamb",
        "pavo" to "Turkey",
        "tocino" to "Bacon",
        "jamón" to "Ham",
        "salchicha" to "Sausages",
        "carne molida" to "Ground Beef",
        "carne picada" to "Ground Beef",
        "pechuga de pollo" to "Chicken Breast",
        "muslo de pollo" to "Chicken Thighs",

        // Pescados y mariscos
        "salmón" to "Salmon",
        "atún" to "Tuna",
        "bacalao" to "Cod",
        "langostinos" to "Prawns",
        "camarones" to "Prawns",
        "mejillones" to "Mussels",
        "cangrejo" to "Crab",
        "langosta" to "Lobster",
        "anchoas" to "Anchovies",

        // Lácteos
        "leche" to "Milk",
        "queso" to "Cheese",
        "mantequilla" to "Butter",
        "manteca" to "Butter",
        "crema" to "Double Cream",
        "crema de leche" to "Double Cream",
        "yogur" to "Greek Yogurt",
        "yogurt" to "Greek Yogurt",
        "mozzarella" to "Mozzarella",
        "parmesano" to "Parmesan",
        "cheddar" to "Cheddar Cheese",
        "queso crema" to "Cream Cheese",
        "queso feta" to "Feta",

        // Huevos
        "huevos" to "Eggs",
        "huevo" to "Eggs",

        // Vegetales
        "cebolla" to "Onion",
        "ajo" to "Garlic",
        "tomate" to "Tomatoes",
        "zanahoria" to "Carrots",
        "papa" to "Potatoes",
        "pimiento" to "Red Pepper",
        "champiñón" to "Mushrooms",
        "hongos" to "Mushrooms",
        "brócoli" to "Broccoli",
        "espinaca" to "Spinach",
        "lechuga" to "Lettuce",
        "pepino" to "Cucumber",
        "apio" to "Celery",
        "maíz" to "Sweetcorn",
        "guisantes" to "Peas",
        "coliflor" to "Cauliflower",
        "repollo" to "Cabbage",
        "cebolla morada" to "Red Onion",
        "berenjena" to "Aubergine",
        "calabacín" to "Courgette",
        "batata" to "Sweet Potato",
        "remolacha" to "Beetroot",
        "espárragos" to "Asparagus",
        "palta" to "Avocado",

        // Frutas
        "manzana" to "Apple",
        "banana" to "Banana",
        "plátano" to "Banana",
        "naranja" to "Orange",
        "limón" to "Lemon",
        "lima" to "Lime",
        "fresa" to "Strawberries",
        "arándano" to "Blueberries",
        "uva" to "Grapes",
        "piña" to "Pineapple",
        "ananá" to "Pineapple",
        "mango" to "Mango",
        "durazno" to "Peach",
        "pera" to "Pear",
        "cereza" to "Cherry",
        "sandía" to "Watermelon",
        "melón" to "Cantaloupe",
        "kiwi" to "Kiwi",
        "coco" to "Coconut",
        "leche de coco" to "Coconut Milk",
        "aceite de coco" to "Coconut Oil",
        "harina de coco" to "Coconut",
        "coco rallado" to "Coconut",
        "agua de coco" to "Coconut Milk",
        "crema de coco" to "Coconut Cream",
        "manteca de coco" to "Coconut Oil",
        "azúcar de coco" to "Coconut",

        // Granos y cereales
        "arroz" to "Rice",
        "pasta" to "Penne Rigate",
        "fideos" to "Spaghetti",
        "espaguetis" to "Spaghetti",
        "harina" to "Plain Flour",
        "avena" to "Oats",
        "pan" to "Bread",
        "pan rallado" to "Breadcrumbs",

        // Legumbres y frutos secos
        "frijoles" to "Black Beans",
        "garbanzos" to "Chickpeas",
        "lentejas" to "Red Lentils",
        "almendras" to "Almonds",
        "nueces" to "Walnuts",
        "maníes" to "Peanuts",

        // Especias y hierbas
        "sal" to "Salt",
        "pimienta" to "Black Pepper",
        "albahaca" to "Basil",
        "orégano" to "Oregano",
        "perejil" to "Parsley",
        "cilantro" to "Coriander",
        "jengibre" to "Ginger",
        "canela" to "Cinnamon",
        "comino" to "Cumin",
        "pimentón" to "Paprika",
        "ají" to "Chili Powder",
        "vainilla" to "Vanilla",

        // Aceites y vinagres
        "aceite de oliva" to "Olive Oil",
        "aceite" to "Vegetable Oil",
        "vinagre" to "White Wine Vinegar",

        // Condimentos
        "azúcar" to "Sugar",
        "miel" to "Honey",
        "mostaza" to "Dijon Mustard",
        "mayonesa" to "Mayonnaise",
        "ketchup" to "Tomato Ketchup",
        "salsa de soja" to "Soy Sauce",

        // Bebidas
        "agua" to "Water",
        "vino" to "White Wine",
        "cerveza" to "Beer",
        "café" to "Coffee",

        // Otros
        "chocolate" to "Dark Chocolate",
        "levadura" to "Yeast",
        "gelatina" to "Gelatine Leaves"
    )

    /**
     * Obtiene la URL de la imagen de un ingrediente desde TheMealDB
     * @param ingredientName Nombre del ingrediente en español
     * @return URL de la imagen o null si no se encuentra
     */
    fun getIngredientImageUrl(ingredientName: String): String? {
        val normalizedName = ingredientName.lowercase().trim()

        // Buscar coincidencia exacta
        ingredientImageMap[normalizedName]?.let { englishName ->
            return "$BASE_INGREDIENT_URL/${englishName.replace(" ", "%20")}-Small.png"
        }

        // Buscar coincidencias parciales
        for ((spanish, english) in ingredientImageMap) {
            if (normalizedName.contains(spanish) || spanish.contains(normalizedName)) {
                return "$BASE_INGREDIENT_URL/${english.replace(" ", "%20")}-Small.png"
            }
        }

        return null
    }

    /**
     * Obtiene la URL de imagen usando la traducción de TheMealDB
     * @param ingredientName Nombre del ingrediente (puede estar en inglés o español)
     * @return URL de la imagen o null si no se encuentra
     */
    fun getIngredientImageUrlSmart(ingredientName: String): String? {
        val normalizedName = ingredientName.lowercase().trim()

        // Primero intenta como está
        getIngredientImageUrl(normalizedName)?.let { return it }

        // Luego intenta traducir desde inglés
        val translated = TheMealDBTranslations.translateIngredientSmart(normalizedName)
        if (translated != ingredientName) {
            return getIngredientImageUrl(translated)
        }

        return null
    }
}
