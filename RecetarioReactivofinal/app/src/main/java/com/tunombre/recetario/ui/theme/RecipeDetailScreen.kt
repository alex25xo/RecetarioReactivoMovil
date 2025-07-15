@file:OptIn(ExperimentalMaterial3Api::class)
package com.tunombre.recetario.ui.theme

/* ---------- imports ---------- */
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunombre.recetario.R
import com.tunombre.recetario.data.Ingredient
import com.tunombre.recetario.data.RecipeEntity
import com.tunombre.recetario.viewmodel.RecipeViewModel
import androidx.compose.runtime.saveable.rememberSaveable

/* ---------- Pantalla Detalle ---------- */
@Composable
fun RecipeDetailScreen(
    recipe: RecipeEntity, 
    vm: RecipeViewModel, 
    onBack: () -> Unit, 
    onEdit: () -> Unit, 
    onDelete: (RecipeEntity) -> Unit
) {
    val checked: SnapshotStateList<Boolean> = vm.checklistFor(recipe)
    var showConfirm by remember { mutableStateOf(false) }
    var servings by rememberSaveable { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, "Editar")
                    }
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Filled.Delete, "Eliminar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            // Imagen de la receta
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
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
                    
                    // Información de la receta sobre la imagen
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        CategoryChip(
                            category = recipe.category,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )
                        
                        if (recipe.description.isNotBlank()) {
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Información nutricional y tiempo (solo visual)
            item {
                RecipeInfoCard(recipe = recipe, servings = servings.toInt())
            }
            // Slider de porciones
            item {
                ServingsSlider(
                    servings = servings,
                    onServingsChange = { servings = it }
                )
            }
            // Ingredientes
            item {
                IngredientsSection(
                    ingredients = recipe.ingredients,
                    checked = checked,
                    servings = servings,
                    onToggleIngredient = { idx -> vm.toggleIngredient(recipe.id, idx) }
                )
            }

            // Pasos de preparación
            item {
                StepsSection(steps = recipe.stepsLines)
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Diálogo de confirmación
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { 
                Text(
                    "¿Eliminar receta?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = { 
                Text(
                    "Esta acción no se puede deshacer. La receta se eliminará permanentemente.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onDelete(recipe)
                    }
                ) { 
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun RecipeInfoCard(recipe: RecipeEntity, servings: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(
                icon = Icons.Filled.Schedule,
                value = "${recipe.cookTime}",
                label = "Minutos",
                color = MaterialTheme.colorScheme.primary
            )
            
            InfoItem(
                icon = Icons.Filled.Star,
                value = getDifficultyText(recipe.difficulty),
                label = "Dificultad",
                color = ButterYellow
            )
            
            InfoItem(
                icon = Icons.Filled.Person,
                value = servings.toString(),
                label = "Porciones",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ServingsSlider(
    servings: Float,
    onServingsChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ajustar porciones",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Slider(
                    value = servings,
                    onValueChange = onServingsChange,
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Text(
                    text = "10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Text(
                text = "${servings.toInt()} porciones",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IngredientsSection(
    ingredients: List<Ingredient>,
    checked: SnapshotStateList<Boolean>,
    servings: Float,
    onToggleIngredient: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🥘 Ingredientes",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ingredients.forEachIndexed { idx, ingredient ->
                val isChecked = checked[idx]
                val scaledQty = ingredient.qty * servings
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleIngredient(idx) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Filled.CheckCircle
                        else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isChecked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        buildString {
                            append(
                                if (scaledQty == scaledQty.toInt().toFloat())
                                    scaledQty.toInt()
                                else "%.1f".format(scaledQty)
                            )
                            if (ingredient.unit.isNotBlank()) append(" ${ingredient.unit}")
                            append("  ${ingredient.name}")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (isChecked) TextDecoration.LineThrough
                        else TextDecoration.None,
                        color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsSection(steps: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "👨‍🍳 Pasos de preparación",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            steps.forEachIndexed { idx, step ->
                var expanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = "Paso ${idx + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expanded) "Contraer" else "Expandir",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Funciones auxiliares
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

private val RecipeEntity.stepsLines: List<String>
    get() = steps.lines().filter { it.isNotBlank() }
