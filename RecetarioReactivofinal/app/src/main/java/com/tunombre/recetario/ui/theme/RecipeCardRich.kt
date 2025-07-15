package com.tunombre.recetario.ui.theme

/* ---------- imports ---------- */
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunombre.recetario.R
import com.tunombre.recetario.data.Category
import com.tunombre.recetario.data.RecipeEntity

/* ---------- Tarjeta ---------- */
@Composable
fun RecipeCardRich(
    recipe: RecipeEntity,        // ✅ sin <Any?>
    onOpen: () -> Unit,
    onFav : () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onOpen() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Imagen de la receta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = getRecipeImage(recipe.name)),
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay con gradiente
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    // El gradiente se puede simular con un Box transparente
                    // o simplemente eliminar si no es esencial
                }
                
                // Botón de favorito
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    IconButton(
                        onClick = onFav
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (recipe.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (recipe.isFavorite) TomatoRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Categoría
                CategoryChip(
                    category = recipe.category,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
            }
            
            // Contenido de la tarjeta
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Título
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Información adicional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tiempo de preparación
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Tiempo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${recipe.cookTime} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Dificultad
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Dificultad",
                            tint = ButterYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getDifficultyText(recipe.difficulty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Descripción
                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* ---------- Chip reutilizable ---------- */
@Composable
private fun NutrientBadge(text: String) = AssistChip(
    onClick = {},
    enabled = false,
    label   = { Text(text) }
)

@Composable
fun CategoryChip(
    category: Category,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(category)
    val categoryName = getCategoryDisplayName(category)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = categoryColor.copy(alpha = 0.9f)
    ) {
        Text(
            text = categoryName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

private fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.BREAKFAST -> BreakfastColor
        Category.LUNCH -> LunchColor
        Category.DINNER -> DinnerColor
        Category.DESSERT -> DessertColor
        Category.SNACK -> SnackColor
        Category.BEVERAGE -> BeverageColor
    }
}

private fun getCategoryDisplayName(category: Category): String {
    return when (category) {
        Category.BREAKFAST -> "Desayuno"
        Category.LUNCH -> "Almuerzo"
        Category.DINNER -> "Cena"
        Category.DESSERT -> "Postre"
        Category.SNACK -> "Snack"
        Category.BEVERAGE -> "Bebida"
    }
}

private fun getDifficultyText(difficulty: Int): String {
    return when {
        difficulty <= 2 -> "Fácil"
        difficulty <= 4 -> "Media"
        else -> "Difícil"
    }
}

private fun getRecipeImage(recipeName: String): Int {
    // Mapeo de nombres de recetas a recursos de drawable
    val imageMap = mapOf(
        "Pancakes de Avena" to R.drawable.oat_pancakes,
        "Ensalada César con Pollo" to R.drawable.chicken_caesar,
        "Crema de Calabaza" to R.drawable.pumpkin_soup,
        "Spaghetti al Pesto" to R.drawable.pesto,
        "Salmón al Horno" to R.drawable.baked_salmon,
        "Burrito de Frijoles" to R.drawable.bean_burrito,
        "Pollo al Curry" to R.drawable.chicken_curry,
        "Ensalada de Quinoa" to R.drawable.quinoa_salad,
        "Tostadas Francesas" to R.drawable.french_toast,
        "Omelette de Espinacas" to R.drawable.omelette,
        "Galletas de Avena" to R.drawable.oat_cookies,
        "Brownies Clásicos" to R.drawable.brownies,
        "Cheesecake de Fresa" to R.drawable.strawberry_cheesecake,
        "Lasaña de Verduras" to R.drawable.veggie_lasagna,
        "Smoothie Bowl Tropical" to R.drawable.smoothie_bowl,
        "Bolitas Energéticas de Avena" to R.drawable.bolitas,
        "Barra de Cereal Saludable" to R.drawable.barra_cereal,
        "Vaso de Yogur con Frutas y Granola" to R.drawable.yogurt_fresa,
        "Jugo de Mango y Maracuyá" to R.drawable.mango_maracuya,
        "Limonada Refrescante" to R.drawable.limonada,
        "Té Helado de Durazno" to R.drawable.te_helado_durazno


    )
    
    return imageMap[recipeName] ?: R.drawable.placeholder
}
