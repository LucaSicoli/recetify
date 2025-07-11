package com.example.recetify.util

/**
 * Utilidad MEJORADA para obtener URLs de imágenes de ingredientes desde TheMealDB
 * Lista expandida y verificada con ingredientes reales disponibles en TheMealDB
 */
object TheMealDBImagesFixed {

    private const val BASE_INGREDIENT_URL = "https://www.themealdb.com/images/ingredients"

    /**
     * Mapa COMPLETO de ingredientes en español a sus nombres en inglés VERIFICADOS en TheMealDB
     */
    private val ingredientImageMap = mapOf(
        // CARNES Y AVES - VERIFICADOS
        "carne de res" to "Beef",
        "carne" to "Beef",
        "pollo" to "Chicken",
        "cerdo" to "Pork",
        "cordero" to "Lamb",
        "pavo" to "Turkey",
        "tocino" to "Bacon",
        "panceta" to "Bacon",
        "jamón" to "Ham",
        "salchicha" to "Sausages",
        "chorizo" to "Chorizo",
        "carne molida" to "Ground Beef",
        "carne picada" to "Ground Beef",
        "pechuga de pollo" to "Chicken Breast",
        "muslo de pollo" to "Chicken Thighs",

        // PESCADOS Y MARISCOS - VERIFICADOS
        "salmón" to "Salmon",
        "atún" to "Tuna",
        "bacalao" to "Cod",
        "langostinos" to "Prawns",
        "camarones" to "Prawns",
        "gambas" to "Prawns",
        "mejillones" to "Mussels",
        "cangrejo" to "Crab",
        "langosta" to "Lobster",
        "anchoas" to "Anchovies",

        // LÁCTEOS - VERIFICADOS
        "leche" to "Milk",
        "queso" to "Cheese",
        "mantequilla" to "Butter",
        "manteca" to "Butter",
        "crema" to "Double Cream",
        "crema de leche" to "Double Cream",
        "nata" to "Double Cream",
        "yogur" to "Greek Yogurt",
        "yogurt" to "Greek Yogurt",
        "mozzarella" to "Mozzarella",
        "parmesano" to "Parmesan",
        "cheddar" to "Cheddar Cheese",
        "queso crema" to "Cream Cheese",
        "queso feta" to "Feta",
        "ricotta" to "Ricotta",
        "queso de cabra" to "Goats Cheese",

        // HUEVOS - VERIFICADOS
        "huevos" to "Eggs",
        "huevo" to "Eggs",

        // VEGETALES - VERIFICADOS
        "cebolla" to "Onion",
        "ajo" to "Garlic",
        "tomate" to "Tomatoes",
        "zanahoria" to "Carrots",
        "papa" to "Potatoes",
        "patata" to "Potatoes",
        "pimiento" to "Red Pepper",
        "pimiento rojo" to "Red Pepper",
        "champiñón" to "Mushrooms",
        "hongos" to "Mushrooms",
        "setas" to "Mushrooms",
        "brócoli" to "Broccoli",
        "espinaca" to "Spinach",
        "lechuga" to "Lettuce",
        "pepino" to "Cucumber",
        "apio" to "Celery",
        "maíz" to "Sweetcorn",
        "choclo" to "Sweetcorn",
        "guisantes" to "Peas",
        "arvejas" to "Peas",
        "coliflor" to "Cauliflower",
        "repollo" to "Cabbage",
        "col" to "Cabbage",
        "cebolla morada" to "Red Onion",
        "cebolla colorada" to "Red Onion",
        "berenjena" to "Aubergine",
        "calabacín" to "Courgette",
        "zapallito" to "Courgette",
        "batata" to "Sweet Potato",
        "boniato" to "Sweet Potato",
        "camote" to "Sweet Potato",
        "remolacha" to "Beetroot",
        "espárragos" to "Asparagus",
        "palta" to "Avocado",
        "aguacate" to "Avocado",

        // FRUTAS - VERIFICADAS
        "manzana" to "Apple",
        "banana" to "Banana",
        "plátano" to "Banana",
        "naranja" to "Orange",
        "limón" to "Lemon",
        "lima" to "Lime",
        "fresa" to "Strawberries",
        "frutilla" to "Strawberries",
        "arándano" to "Blueberries",
        "uva" to "Grapes",
        "piña" to "Pineapple",
        "ananá" to "Pineapple",
        "mango" to "Mango",
        "durazno" to "Peach",
        "melocotón" to "Peach",
        "pera" to "Pear",
        "cereza" to "Cherry",
        "sandía" to "Watermelon",
        "melón" to "Cantaloupe",
        "kiwi" to "Kiwi",

        // COCO Y DERIVADOS - USANDO FALLBACKS SEGUROS
        "coco" to "Coconut",
        "leche de coco" to "Coconut Milk",
        "aceite de coco" to "Olive Oil", // Fallback seguro
        "harina de coco" to "Plain Flour", // Fallback seguro
        "coco rallado" to "Coconut",
        "agua de coco" to "Coconut Milk",
        "crema de coco" to "Double Cream", // Fallback seguro
        "manteca de coco" to "Butter", // Fallback seguro
        "azúcar de coco" to "Brown Sugar", // Fallback seguro
        "coco fresco" to "Coconut",
        "coco seco" to "Coconut",

        // GRANOS Y CEREALES - VERIFICADOS
        "arroz" to "Rice",
        "pasta" to "Penne Rigate",
        "fideos" to "Spaghetti",
        "tallarines" to "Linguine",
        "espaguetis" to "Spaghetti",
        "macarrones" to "Penne Rigate",
        "harina" to "Plain Flour",
        "harina común" to "Plain Flour",
        "harina 000" to "Plain Flour",
        "harina 0000" to "Plain Flour",
        "harina integral" to "Plain Flour",
        "avena" to "Oats",
        "pan" to "Bread",
        "pan rallado" to "Breadcrumbs",

        // LEGUMBRES Y FRUTOS SECOS - VERIFICADOS
        "frijoles" to "Black Beans",
        "porotos" to "Black Beans",
        "judías" to "Black Beans",
        "garbanzos" to "Chickpeas",
        "lentejas" to "Red Lentils",
        "almendras" to "Almonds",
        "nueces" to "Walnuts",
        "maníes" to "Peanuts",
        "cacahuetes" to "Peanuts",

        // ESPECIAS Y HIERBAS - VERIFICADOS EN THEMEALDB
        "sal" to "Salt",
        "pimienta" to "Black Pepper",
        "pimienta negra" to "Black Pepper",
        "albahaca" to "Basil",
        "orégano" to "Oregano",
        "perejil" to "Parsley",
        "cilantro" to "Coriander",
        "jengibre" to "Ginger",
        "canela" to "Cinnamon",
        "comino" to "Cumin",
        "pimentón" to "Paprika",
        "ají" to "Chilli",
        "ají molido" to "Chilli",
        "chile" to "Chilli",
        "vainilla" to "Vanilla",
        "extracto de vainilla" to "Vanilla Extract",
        "tomillo" to "Thyme",
        "laurel" to "Bay Leaves",
        "hojas de laurel" to "Bay Leaves",
        "nuez moscada" to "Nutmeg",
        "clavo de olor" to "Cloves",
        "cardamomo" to "Cardamom",
        "semillas de sésamo" to "Sesame Seed",
        "sésamo" to "Sesame Seed",
        "ajonjolí" to "Sesame Seed",

        // ACEITES Y VINAGRES - VERIFICADOS
        "aceite de oliva" to "Olive Oil",
        "aceite" to "Vegetable Oil",
        "aceite vegetal" to "Vegetable Oil",
        "aceite de girasol" to "Vegetable Oil",
        "vinagre" to "White Wine Vinegar",
        "vinagre blanco" to "White Wine Vinegar",

        // CONDIMENTOS Y SALSAS - VERIFICADOS
        "azúcar" to "Sugar",
        "azúcar blanco" to "Sugar",
        "azúcar moreno" to "Brown Sugar",
        "azúcar mascabo" to "Brown Sugar",
        "miel" to "Honey",
        "mostaza" to "Dijon Mustard",
        "mayonesa" to "Mayonnaise",
        "ketchup" to "Tomato Ketchup",
        "salsa de tomate" to "Tomato Puree",
        "pasta de tomate" to "Tomato Puree",
        "puré de tomate" to "Tomato Puree",
        "salsa de soja" to "Soy Sauce",
        "salsa soja" to "Soy Sauce",

        // BEBIDAS - VERIFICADAS
        "agua" to "Water",
        "vino" to "White Wine",
        "vino blanco" to "White Wine",
        "vino tinto" to "Red Wine",
        "cerveza" to "Beer",

        // OTROS INGREDIENTES - VERIFICADOS
        "chocolate" to "Dark Chocolate",
        "chocolate negro" to "Dark Chocolate",
        "chocolate amargo" to "Dark Chocolate",
        "levadura" to "Yeast",
        "polvo de hornear" to "Baking Powder",
        "bicarbonato" to "Bicarbonate Of Soda",
        "bicarbonato de sodio" to "Bicarbonate Of Soda"
    )

    /**
     * Obtiene la URL de la imagen de un ingrediente desde TheMealDB
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
     * Obtiene la URL de imagen con traducción inteligente
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
