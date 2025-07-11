package com.example.recetify.util

/**
 * Traducciones de ingredientes de TheMealDB (inglés) al español
 * para integrar correctamente con la base de datos de TheMealDB
 */
object TheMealDBTranslations {

    private val ingredientTranslations = mapOf(
        // Carnes
        "beef" to "carne de res",
        "chicken" to "pollo",
        "pork" to "cerdo",
        "lamb" to "cordero",
        "turkey" to "pavo",
        "bacon" to "tocino",
        "ham" to "jamón",
        "sausage" to "salchicha",
        "ground beef" to "carne molida",
        "chicken breast" to "pechuga de pollo",
        "chicken thigh" to "muslo de pollo",
        "minced beef" to "carne picada",

        // Pescados y mariscos
        "salmon" to "salmón",
        "tuna" to "atún",
        "cod" to "bacalao",
        "prawns" to "langostinos",
        "shrimp" to "camarones",
        "mussels" to "mejillones",
        "crab" to "cangrejo",
        "lobster" to "langosta",
        "sea bass" to "lubina",
        "mackerel" to "caballa",

        // Lácteos
        "milk" to "leche",
        "cheese" to "queso",
        "butter" to "mantequilla",
        "cream" to "crema",
        "yogurt" to "yogur",
        "sour cream" to "crema agria",
        "mozzarella" to "mozzarella",
        "parmesan" to "parmesano",
        "cheddar" to "cheddar",
        "ricotta" to "ricotta",
        "feta" to "queso feta",
        "goat cheese" to "queso de cabra",
        "cream cheese" to "queso crema",

        // Huevos
        "eggs" to "huevos",
        "egg" to "huevo",
        "egg yolk" to "yema de huevo",
        "egg white" to "clara de huevo",

        // Vegetales
        "onion" to "cebolla",
        "garlic" to "ajo",
        "tomato" to "tomate",
        "carrot" to "zanahoria",
        "potato" to "papa",
        "bell pepper" to "pimiento",
        "mushroom" to "champiñón",
        "broccoli" to "brócoli",
        "spinach" to "espinaca",
        "lettuce" to "lechuga",
        "cucumber" to "pepino",
        "celery" to "apio",
        "corn" to "maíz",
        "peas" to "guisantes",
        "green beans" to "judías verdes",
        "cauliflower" to "coliflor",
        "cabbage" to "repollo",
        "red onion" to "cebolla morada",
        "spring onion" to "cebolleta",
        "leek" to "puerro",
        "aubergine" to "berenjena",
        "courgette" to "calabacín",
        "sweet potato" to "batata",
        "beetroot" to "remolacha",
        "radish" to "rábano",
        "turnip" to "nabo",
        "asparagus" to "espárragos",
        "artichoke" to "alcachofa",
        "avocado" to "palta",

        // Frutas
        "apple" to "manzana",
        "banana" to "banana",
        "orange" to "naranja",
        "lemon" to "limón",
        "lime" to "lima",
        "strawberry" to "fresa",
        "blueberry" to "arándano",
        "raspberry" to "frambuesa",
        "blackberry" to "mora",
        "grape" to "uva",
        "pineapple" to "piña",
        "mango" to "mango",
        "peach" to "durazno",
        "pear" to "pera",
        "plum" to "ciruela",
        "cherry" to "cereza",
        "watermelon" to "sandía",
        "melon" to "melón",
        "kiwi" to "kiwi",
        "coconut" to "coco",

        // Granos y cereales
        "rice" to "arroz",
        "pasta" to "pasta",
        "bread" to "pan",
        "flour" to "harina",
        "oats" to "avena",
        "quinoa" to "quinoa",
        "barley" to "cebada",
        "wheat" to "trigo",
        "breadcrumbs" to "pan rallado",
        "noodles" to "fideos",
        "spaghetti" to "espaguetis",
        "macaroni" to "macarrones",
        "penne" to "penne",
        "linguine" to "linguine",
        "fusilli" to "fusilli",

        // Legumbres y frutos secos
        "beans" to "frijoles",
        "black beans" to "frijoles negros",
        "kidney beans" to "frijoles rojos",
        "chickpeas" to "garbanzos",
        "lentils" to "lentejas",
        "almonds" to "almendras",
        "walnuts" to "nueces",
        "peanuts" to "maníes",
        "pistachios" to "pistachos",
        "cashews" to "anacardos",
        "pine nuts" to "piñones",
        "hazelnuts" to "avellanas",
        "pecans" to "nueces pecanas",

        // Especias y hierbas
        "salt" to "sal",
        "pepper" to "pimienta",
        "black pepper" to "pimienta negra",
        "white pepper" to "pimienta blanca",
        "paprika" to "pimentón",
        "cumin" to "comino",
        "coriander" to "cilantro",
        "basil" to "albahaca",
        "oregano" to "orégano",
        "thyme" to "tomillo",
        "rosemary" to "romero",
        "sage" to "salvia",
        "parsley" to "perejil",
        "dill" to "eneldo",
        "mint" to "menta",
        "chives" to "cebollino",
        "ginger" to "jengibre",
        "turmeric" to "cúrcuma",
        "cinnamon" to "canela",
        "nutmeg" to "nuez moscada",
        "cardamom" to "cardamomo",
        "cloves" to "clavos de olor",
        "bay leaves" to "hojas de laurel",
        "star anise" to "anís estrellado",
        "fennel" to "hinojo",
        "mustard seed" to "semilla de mostaza",
        "sesame seeds" to "semillas de sésamo",
        "poppy seeds" to "semillas de amapola",
        "caraway seeds" to "semillas de alcaravea",
        "vanilla" to "vainilla",
        "vanilla extract" to "extracto de vainilla",

        // Aceites y vinagres
        "olive oil" to "aceite de oliva",
        "vegetable oil" to "aceite vegetal",
        "sunflower oil" to "aceite de girasol",
        "coconut oil" to "aceite de coco",
        "sesame oil" to "aceite de sésamo",
        "vinegar" to "vinagre",
        "white vinegar" to "vinagre blanco",
        "red wine vinegar" to "vinagre de vino tinto",
        "balsamic vinegar" to "vinagre balsámico",
        "apple cider vinegar" to "vinagre de manzana",

        // Condimentos y salsas
        "soy sauce" to "salsa de soja",
        "worcestershire sauce" to "salsa worcestershire",
        "hot sauce" to "salsa picante",
        "tomato sauce" to "salsa de tomate",
        "tomato paste" to "pasta de tomate",
        "mayonnaise" to "mayonesa",
        "mustard" to "mostaza",
        "ketchup" to "ketchup",
        "honey" to "miel",
        "maple syrup" to "jarabe de arce",
        "brown sugar" to "azúcar moreno",
        "white sugar" to "azúcar blanco",
        "icing sugar" to "azúcar impalpable",
        "castor sugar" to "azúcar refinado",

        // Bebidas
        "water" to "agua",
        "wine" to "vino",
        "red wine" to "vino tinto",
        "white wine" to "vino blanco",
        "beer" to "cerveza",
        "stock" to "caldo",
        "chicken stock" to "caldo de pollo",
        "beef stock" to "caldo de carne",
        "vegetable stock" to "caldo de verduras",
        "coconut milk" to "leche de coco",
        "almond milk" to "leche de almendras",

        // Otros ingredientes comunes
        "baking powder" to "polvo de hornear",
        "baking soda" to "bicarbonato de sodio",
        "yeast" to "levadura",
        "cornstarch" to "maicena",
        "gelatin" to "gelatina",
        "chocolate" to "chocolate",
        "dark chocolate" to "chocolate negro",
        "milk chocolate" to "chocolate con leche",
        "white chocolate" to "chocolate blanco",
        "cocoa powder" to "cacao en polvo",
        "coffee" to "café",
        "tea" to "té",
        "green tea" to "té verde",
        "black tea" to "té negro"
    )

    /**
     * Traduce un ingrediente de inglés a español
     * @param englishIngredient El nombre del ingrediente en inglés
     * @return El nombre traducido al español, o el original si no se encuentra traducción
     */
    fun translateIngredient(englishIngredient: String): String {
        val normalized = englishIngredient.lowercase().trim()
        return ingredientTranslations[normalized] ?: englishIngredient
    }

    /**
     * Traduce una lista de ingredientes de inglés a español
     * @param englishIngredients Lista de ingredientes en inglés
     * @return Lista de ingredientes traducidos al español
     */
    fun translateIngredients(englishIngredients: List<String>): List<String> {
        return englishIngredients.map { translateIngredient(it) }
    }

    /**
     * Busca coincidencias parciales para ingredientes compuestos
     * @param englishIngredient El ingrediente en inglés que puede contener múltiples palabras
     * @return El ingrediente traducido o el original si no hay coincidencia
     */
    fun translateIngredientSmart(englishIngredient: String): String {
        val normalized = englishIngredient.lowercase().trim()

        // Buscar coincidencia exacta primero
        ingredientTranslations[normalized]?.let { return it }

        // Buscar coincidencias parciales para ingredientes compuestos
        for ((english, spanish) in ingredientTranslations) {
            if (normalized.contains(english) || english.contains(normalized)) {
                return spanish
            }
        }

        return englishIngredient
    }
}
