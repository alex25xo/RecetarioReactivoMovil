package com.tunombre.recetario.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(private val dao: RecipeDao) {

    /* ────────────────────────────
     * FLUJOS REACTIVOS PARA LA UI
     * ──────────────────────────── */

    fun recipesFlow(
        query: String = "",
        category: Category? = null
    ): Flow<List<RecipeEntity>> =
        when {
            query.isNotBlank() -> dao.search(query)
            category != null   -> dao.getByCategory(category)
            else               -> dao.getAll()
        }

    /** Ranking de recetas según coincidencias con la "despensa" del usuario */
    fun recommendByIngredients(userIngredients: List<String>):
            Flow<List<Pair<RecipeEntity, Int>>> {

        // normaliza los ingredientes que escribe el usuario
        val cleaned = userIngredients.map { it.trim().lowercase() }

        return dao.getAll().map { recipes ->
            recipes
                .map { r ->
                    // "heno" sobre el que buscamos: todos los nombres de ingredientes de la receta
                    val haystack = r.ingredients
                        .joinToString(" ") { ing -> ing.name }
                        .lowercase()

                    val hits = cleaned.count { it in haystack }
                    r to hits
                }
                .filter { it.second > 0 }              // al menos una coincidencia
                .sortedByDescending { it.second }      // más matches primero
        }
    }

    /* ────────────────────────────
     * ACCIONES CRUD SENCILLAS
     * ──────────────────────────── */

    suspend fun insert (recipe: RecipeEntity) = dao.insert(recipe)
    suspend fun update (recipe: RecipeEntity) = dao.update(recipe)
    suspend fun delete (recipe: RecipeEntity) = dao.delete(recipe)

    /** Invierte la bandera de favorito y persiste */
    suspend fun toggleFavorite(r: RecipeEntity) =
        dao.update(r.copy(isFavorite = !r.isFavorite))

    suspend fun getRecipeById(id: Int): RecipeEntity? = dao.getRecipeById(id)
    suspend fun random(): RecipeEntity                = dao.random()

    /* ────────────────────────────
     * SEMILLA DEMO (solo si la tabla está vacía)
     * ──────────────────────────── */

    suspend fun seedIfEmpty() {
        if (dao.count() > 0) return   // ya hay datos → salir

        val pkg = "com.tunombre.recetario"
        fun res(name: String) = "android.resource://$pkg/drawable/$name"

        val demo = listOf(
            RecipeEntity(
                name = "Tostadas Francesas",
                category = Category.BREAKFAST,
                ingredients = listOf(
                    Ingredient(2f, "",  "rebanadas de pan"),
                    Ingredient(1f, "",  "huevo"),
                    Ingredient(60f,"ml","leche"),
                    Ingredient(0.5f,"cdta","canela"),
                    Ingredient(0.5f,"cdta","vainilla")
                ),
                steps = """
                    1. Batir huevo, leche, canela y vainilla.
                    2. Remojar el pan 10 s por cada lado.
                    3. Dorar a fuego medio 2 min por lado y servir.
                """.trimIndent(),
                imageUri = res("french_toast"),
                calories = 350, protein = 12, cookTime = 10
            ),
            /* ──────  DESAYUNO  ────── */
            RecipeEntity(
                name = "Omelette de Espinacas",
                category = Category.BREAKFAST,
                ingredients = listOf(
                    Ingredient(2f,   "",     "huevos"),
                    Ingredient(0.5f, "taza", "espinacas frescas"),
                    Ingredient(20f,  "g",    "queso rallado"),
                    Ingredient(1f,   "pizca","sal"),
                    Ingredient(1f,   "pizca","pimienta")
                ),
                steps = """
        1. Batir los huevos con sal y pimienta.
        2. Verter en una sartén caliente ligeramente engrasada.
        3. Añadir espinacas y queso; plegar el omelette y cocinar 2 min.
    """.trimIndent(),
                imageUri = res("omelette"),
                calories = 240, protein = 15, cookTime = 8
            ),

            RecipeEntity(
                name = "Pancakes de Avena",
                category = Category.BREAKFAST,
                ingredients = listOf(
                    Ingredient(1f,   "taza", "avena molida"),
                    Ingredient(0.5f, "taza", "leche"),
                    Ingredient(1f,   "",     "huevo"),
                    Ingredient(1f,   "cdta", "polvo de hornear"),
                    Ingredient(1f,   "cda",  "miel (opcional)")
                ),
                steps = """
        1. Mezclar avena, polvo de hornear y una pizca de sal.
        2. Incorporar leche y huevo hasta obtener una masa homogénea.
        3. Cocinar porciones de ¼ taza en sartén antiadherente, 2 min por lado.
    """.trimIndent(),
                imageUri = res("oat_pancakes"),
                calories = 310, protein = 13, cookTime = 15
            ),

            RecipeEntity(
                name = "Smoothie Bowl Tropical",
                category = Category.BREAKFAST,
                ingredients = listOf(
                    Ingredient(1f,   "",     "banana madura congelada"),
                    Ingredient(0.5f, "taza", "mango congelado"),
                    Ingredient(0.5f, "taza", "piña congelada"),
                    Ingredient(0.25f,"taza", "leche de coco"),
                    Ingredient(2f,   "cda",  "granola (topping)"),
                    Ingredient(1f,   "cda",  "coco rallado (topping)")
                ),
                steps = """
        1. Licuar banana, mango, piña y leche de coco hasta lograr consistencia espesa.
        2. Servir en un bowl; decorar con granola y coco rallado.
    """.trimIndent(),
                imageUri = res("smoothie_bowl"),
                calories = 280, protein = 6, cookTime = 5
            ),

            /* ──────  ALMUERZO  ────── */
            RecipeEntity(
                name = "Spaghetti al Pesto",
                category = Category.LUNCH,
                ingredients = listOf(
                    Ingredient(100f,"g",    "spaghetti"),
                    Ingredient(2f,  "cda",  "salsa pesto"),
                    Ingredient(10f, "g",    "queso parmesano rallado"),
                    Ingredient(1f,  "pizca","sal"),
                    Ingredient(1f,  "pizca","pimienta")
                ),
                steps = """
        1. Cocer la pasta en agua con sal; escurrir.
        2. Mezclar con el pesto y el parmesano; rectificar sal y pimienta.
    """.trimIndent(),
                imageUri = res("pesto"),
                calories = 520, protein = 14, cookTime = 20
            ),

            RecipeEntity(
                name = "Ensalada César con Pollo",
                category = Category.LUNCH,
                ingredients = listOf(
                    Ingredient(2f,  "taza", "lechuga romana troceada"),
                    Ingredient(100f,"g",    "pechuga de pollo a la parrilla"),
                    Ingredient(20f, "g",    "crutones"),
                    Ingredient(15f, "g",    "queso parmesano rallado"),
                    Ingredient(2f,  "cda",  "aderezo César")
                ),
                steps = """
        1. Mezclar lechuga y aderezo.
        2. Añadir pollo en tiras, crutones y parmesano.
        3. Servir de inmediato.
    """.trimIndent(),
                imageUri = res("chicken_caesar"),
                calories = 430, protein = 32, cookTime = 15
            ),

            RecipeEntity(
                name = "Burrito de Frijoles",
                category = Category.LUNCH,
                ingredients = listOf(
                    Ingredient(1f,   "",     "tortilla grande de harina"),
                    Ingredient(80f,  "g",    "frijoles negros cocidos"),
                    Ingredient(50f,  "g",    "arroz cocido"),
                    Ingredient(20f,  "g",    "queso rallado"),
                    Ingredient(30f,  "g",    "salsa (pico de gallo)")
                ),
                steps = """
        1. Calentar la tortilla; colocar frijoles y arroz al centro.
        2. Añadir queso y salsa; enrollar formando burrito.
        3. Dorar ligeramente en sartén si se desea.
    """.trimIndent(),
                imageUri = res("bean_burrito"),
                calories = 460, protein = 18, cookTime = 12
            ),

            RecipeEntity(
                name = "Ensalada de Quinoa",
                category = Category.LUNCH,
                ingredients = listOf(
                    Ingredient(0.5f,"taza", "quinoa cocida"),
                    Ingredient(0.5f,"taza", "tomates cherry en mitades"),
                    Ingredient(0.25f,"taza","pepino en cubos"),
                    Ingredient(30f, "g",    "queso feta desmenuzado"),
                    Ingredient(1f,  "cda",  "vinagreta ligera")
                ),
                steps = """
        1. Mezclar quinoa, tomates y pepino.
        2. Añadir feta y vinagreta; refrigerar 10 min y servir.
    """.trimIndent(),
                imageUri = res("quinoa_salad"),
                calories = 390, protein = 13, cookTime = 25
            ),

            /* ──────  CENA  ────── */
            RecipeEntity(
                name = "Pollo al Curry",
                category = Category.DINNER,
                ingredients = listOf(
                    Ingredient(150f,"g",    "pechuga de pollo en cubos"),
                    Ingredient(1f,  "cda",  "aceite"),
                    Ingredient(1f,  "cda",  "polvo de curry"),
                    Ingredient(100f,"ml",   "leche de coco"),
                    Ingredient(1f,  "pizca","sal")
                ),
                steps = """
        1. Sofreír pollo en aceite hasta dorar.
        2. Añadir curry; cocinar 1 min.
        3. Verter leche de coco y sal; hervir suave 10 min.
    """.trimIndent(),
                imageUri = res("chicken_curry"),
                calories = 540, protein = 35, cookTime = 18
            ),

            RecipeEntity(
                name = "Salmón al Horno",
                category = Category.DINNER,
                ingredients = listOf(
                    Ingredient(150f,"g",    "filete de salmón"),
                    Ingredient(1f,  "cda",  "aceite de oliva"),
                    Ingredient(1f,  "",     "rodaja de limón"),
                    Ingredient(1f,  "pizca","sal"),
                    Ingredient(1f,  "pizca","pimienta")
                ),
                steps = """
        1. Precalentar horno a 200 °C.
        2. Colocar el salmón en bandeja, sazonar y rociar aceite.
        3. Hornear 12-15 min; servir con la rodaja de limón.
    """.trimIndent(),
                imageUri = res("baked_salmon"),
                calories = 480, protein = 34, cookTime = 15
            ),

            RecipeEntity(
                name = "Lasaña de Verduras",
                category = Category.DINNER,
                ingredients = listOf(
                    Ingredient(3f,   "láminas","pasta para lasaña precocida"),
                    Ingredient(100f, "g",    "zucchini en cubos"),
                    Ingredient(100f, "g",    "espinacas"),
                    Ingredient(200f, "ml",   "salsa de tomate"),
                    Ingredient(80f,  "g",    "mozzarella rallada")
                ),
                steps = """
        1. Montar capas de pasta, verduras salteadas y salsa.
        2. Cubrir con mozzarella; hornear 25-30 min a 180 °C.
        3. Dejar reposar 5 min antes de cortar.
    """.trimIndent(),
                imageUri = res("veggie_lasagna"),
                calories = 620, protein = 25, cookTime = 40
            ),

            /* ──────  POSTRE  ────── */
            RecipeEntity(
                name = "Brownies Clásicos",
                category = Category.DESSERT,
                ingredients = listOf(
                    Ingredient(100f,"g", "mantequilla"),
                    Ingredient(120f,"g", "chocolate negro"),
                    Ingredient(80f, "g", "harina de trigo"),
                    Ingredient(150f,"g", "azúcar"),
                    Ingredient(2f,  "",  "huevos")
                ),
                steps = """
        1. Derretir mantequilla y chocolate; templar.
        2. Añadir azúcar y huevos; mezclar.
        3. Incorporar harina; hornear 25-30 min a 175 °C.
    """.trimIndent(),
                imageUri = res("brownies"),
                calories = 420, protein = 6, cookTime = 30
            ),

            RecipeEntity(
                name = "Galletas de Avena",
                category = Category.DESSERT,
                ingredients = listOf(
                    Ingredient(1f,  "taza", "avena en hojuelas"),
                    Ingredient(0.5f,"taza", "harina"),
                    Ingredient(0.5f,"taza", "azúcar moreno"),
                    Ingredient(80f, "g",    "mantequilla"),
                    Ingredient(1f,  "",     "huevo")
                ),
                steps = """
        1. Cremar mantequilla con azúcar; añadir huevo.
        2. Incorporar avena y harina.
        3. Formar galletas y hornear 12-15 min a 180 °C.
    """.trimIndent(),
                imageUri = res("oat_cookies"),
                calories = 95, protein = 2, cookTime = 20
            ),

            RecipeEntity(
                name = "Cheesecake de Fresa",
                category = Category.DESSERT,
                ingredients = listOf(
                    Ingredient(200f,"g", "queso crema"),
                    Ingredient(50f, "g", "azúcar"),
                    Ingredient(1f,  "",  "huevo"),
                    Ingredient(80f, "g", "base de galleta triturada"),
                    Ingredient(100f,"g", "fresas frescas")
                ),
                steps = """
        1. Mezclar queso crema, azúcar y huevo.
        2. Verter sobre base de galleta en molde.
        3. Hornear 15 min a 180 °C; enfriar y cubrir con fresas.
    """.trimIndent(),
                imageUri = res("strawberry_cheesecake"),
                calories = 380, protein = 7, cookTime = 15
            ),

            /* ──────  EXTRA  ────── */
            RecipeEntity(
                name = "Crema de Calabaza",
                category = Category.DINNER,
                ingredients = listOf(
                    Ingredient(300f,"g",   "calabaza"),
                    Ingredient(0.5f,"ud",  "cebolla"),
                    Ingredient(500f,"ml",  "caldo vegetal"),
                    Ingredient(1f,  "cda", "nata"),
                    Ingredient(1f,  "pizca","sal y pimienta"),
                ),
                steps = """
                    1. Sofreír la cebolla; añadir calabaza y caldo.
                    2. Cocer 20 min y licuar.
                    3. Devolver a la olla, añadir nata y rectificar sal.
                """.trimIndent(),
                imageUri = res("pumpkin_soup"),
                calories = 170, protein = 3, cookTime = 25
            ),
            /* ──────  SNACKS  ────── */
            RecipeEntity(
                name = "Bolitas Energéticas de Avena",
                category = Category.SNACK,
                ingredients = listOf(
                    Ingredient(80f, "g", "avena"),
                    Ingredient(60f, "g", "mantequilla de maní"),
                    Ingredient(30f, "g", "miel"),
                    Ingredient(20f, "g", "proteína en polvo sabor vainilla"),
                    Ingredient(20f, "g", "semillas de chía")
                ),
                steps = """
        1. Mezclar todos los ingredientes en un bowl grande.
        2. Formar bolitas con las manos.
        3. Refrigerar 30 min para que compacten.
    """.trimIndent(),
                imageUri = res("bolitas"), // Asumiendo que 'bolitas' es un recurso drawable existente
                calories = 110,
                protein = 5,
                cookTime = 10 // Solo preparación activa; no cuento refrigerado
            ),
            RecipeEntity(
                name = "Barra de Cereal Saludable",
                category = Category.SNACK,
                ingredients = listOf(
                    Ingredient(1f,   "taza", "avena en hojuelas"),
                    Ingredient(0.5f, "taza", "frutos secos picados (nueces, almendras)"),
                    Ingredient(0.25f,"taza", "semillas (chía, lino)"),
                    Ingredient(0.5f, "taza", "miel o jarabe de arce"),
                    Ingredient(0.25f,"taza", "chocolate negro troceado (opcional)")
                ),
                steps = """
        1. Combinar avena, frutos secos y semillas en un bowl.
        2. Calentar la miel (o jarabe) ligeramente y añadir a la mezcla.
        3. Incorporar el chocolate troceado si se usa.
        4. Presionar la mezcla firmemente en un molde forrado con papel de horno.
        5. Refrigerar por al menos 1 hora antes de cortar en barras.
    """.trimIndent(),
                imageUri = res("barra_cereal"), // Placeholder para la imagen de la barra de cereal
                calories = 180,
                protein = 4,
                cookTime = 15 // Preparación activa
            ),
            RecipeEntity(
                name = "Vaso de Yogur con Frutas y Granola",
                category = Category.SNACK,
                ingredients = listOf(
                    Ingredient(150f, "g",    "yogur griego natural"),
                    Ingredient(0.5f, "taza", "fresas frescas en rodajas"),
                    Ingredient(0.25f,"taza", "arándanos"),
                    Ingredient(30f,  "g",    "granola"),
                    Ingredient(1f,   "cda",  "semillas de chía (opcional)")
                ),
                steps = """
        1. En un vaso o bowl, colocar una capa de yogur.
        2. Añadir una capa de fresas y arándanos.
        3. Cubrir con una capa de granola y semillas de chía.
        4. Repetir las capas si el recipiente lo permite.
        5. Servir inmediatamente o refrigerar hasta el momento de consumir.
    """.trimIndent(),
                imageUri = res("yogurt_fresa"), // Placeholder para la imagen del yogur con frutas
                calories = 250,
                protein = 15,
                cookTime = 5
            ),
            /* ──────  BEBIDAS  ────── */
            RecipeEntity(
                name = "Jugo de Mango y Maracuyá",
                category = Category.BEVERAGE,
                ingredients = listOf(
                    Ingredient(1f,   "ud",   "mango maduro"),
                    Ingredient(1f,   "ud",   "maracuyá (pulpa)"),
                    Ingredient(200f, "ml",   "agua fría"),
                    Ingredient(2f,   "cda",  "azúcar (opcional)"),
                    Ingredient(4f,   "cubos","hielo")
                ),
                steps = """
        1. Pelar y trocear el mango.
        2. Extraer la pulpa del maracuyá.
        3. Licuar mango, maracuyá, agua y azúcar.
        4. Servir con hielo.
    """.trimIndent(),
                imageUri = res("mango_maracuya"),
                calories = 120,
                protein = 1,
                cookTime = 5
            ),
            RecipeEntity(
                name = "Limonada Refrescante",
                category = Category.BEVERAGE,
                ingredients = listOf(
                    Ingredient(3f,   "ud",   "limones grandes"),
                    Ingredient(1f,   "litro","agua fría"),
                    Ingredient(100f, "g",    "azúcar (o al gusto)"),
                    Ingredient(6f,   "rodaja","hojas de menta (opcional)"),
                    Ingredient(4f,   "cubos","hielo")
                ),
                steps = """
        1. Exprimir el jugo de los limones.
        2. En una jarra, mezclar el jugo de limón, agua y azúcar hasta disolver.
        3. Añadir rodajas de limón y hojas de menta si se desea.
        4. Servir con hielo.
    """.trimIndent(),
                imageUri = res("limonada"), // Asume que tienes un drawable llamado 'limonada'
                calories = 80,
                protein = 0,
                cookTime = 5
            ),
            RecipeEntity(
                name = "Té Helado de Durazno",
                category = Category.BEVERAGE,
                ingredients = listOf(
                    Ingredient(2f,   "bolsitas","té negro"),
                    Ingredient(500f, "ml",   "agua caliente"),
                    Ingredient(1f,   "ud",   "durazno maduro en rodajas"),
                    Ingredient(50f,  "g",    "azúcar (o al gusto)"),
                    Ingredient(4f,   "cubos","hielo")
                ),
                steps = """
        1. Preparar el té negro con el agua caliente; dejar infusionar 5 minutos.
        2. Retirar las bolsitas de té y añadir el azúcar, revolviendo hasta disolver.
        3. Incorporar las rodajas de durazno.
        4. Dejar enfriar completamente y luego refrigerar.
        5. Servir bien frío con hielo.
    """.trimIndent(),
                imageUri = res("te_helado_durazno"), // Asume que tienes un drawable llamado 'iced_peach_tea'
                calories = 90,
                protein = 0,
                cookTime = 10 // Considera el tiempo de infusión y enfriamiento
            )
        )
        // ⚠️  insertAll ahora recibe UNA lista, no vararg
        dao.insertAll(demo)
    }

    fun getAllFlow(): Flow<List<RecipeEntity>> = dao.getAll()
    fun getByCategoryFlow(category: Category): Flow<List<RecipeEntity>> = dao.getByCategory(category)
}