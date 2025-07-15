@file:OptIn(ExperimentalMaterial3Api::class)
package com.tunombre.recetario.ui.theme

/* ---------- imports ---------- */
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tunombre.recetario.data.Category
import com.tunombre.recetario.data.RecipeEntity
import com.tunombre.recetario.viewmodel.RecipeViewModel
import com.tunombre.recetario.data.Ingredient


/* ---------- Pantalla editar ---------- */
@Composable
fun EditRecipeScreen(original: RecipeEntity, vm: RecipeViewModel, onDone: () -> Unit)
 {
    /* --- estado editable (persistente) --- */
    var name        by rememberSaveable { mutableStateOf(original.name) }
    var imageUriStr by rememberSaveable { mutableStateOf(original.imageUri) }

    /* seguimos usando texto multilínea para ingredientes / pasos */
    var ingredients by rememberSaveable {
        mutableStateOf(original.ingredientsText)  // helper abajo
    }
    var steps by rememberSaveable { mutableStateOf(original.steps) }

    var category by rememberSaveable { mutableStateOf(original.category) }
    var calories by rememberSaveable { mutableStateOf(original.calories.toString()) }
    var protein  by rememberSaveable { mutableStateOf(original.protein.toString()) }
    var cookTime by rememberSaveable { mutableStateOf(original.cookTime.toString()) }

    val ctx      = LocalContext.current
    val imgUri   = imageUriStr?.let { Uri.parse(it) }

    /* ---------- picker de foto ---------- */
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            ctx.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        imageUriStr = uri?.toString()
    }

    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar receta") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /* --- Imagen --- */
            OutlinedButton(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (imgUri == null) "Elegir foto" else "Cambiar foto") }

            imgUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            /* --- Campos --- */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            // Selector de categoría
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    Category.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredientes (uno por línea)") },
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                label = { Text("Pasos (uno por línea)") },
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("kcal") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("g prot") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cookTime,
                    onValueChange = { cookTime = it },
                    label = { Text("min") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            /* --- Guardar --- */
            Button(
                onClick = {
                    vm.update(
                        original.copy(
                            name = name,
                            category = category,
                            ingredients = ingredients.toIngredientList(), // helper abajo
                            steps = steps,
                            imageUri = imageUriStr,
                            calories = calories.toIntOrNull() ?: 0,
                            protein = protein.toIntOrNull() ?: 0,
                            cookTime = cookTime.toIntOrNull() ?: 0,
                            difficulty = original.difficulty, // Mantener valor original
                            servings = original.servings,     // Mantener valor original
                            description = original.description // Mantener valor original
                        )
                    )
                    onDone()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) { Text("Guardar cambios") }
        }
    }
}

/* ---------- Helpers ---------- */

/* de texto multilínea "2 taza harina" → List<Ingredient> */
private fun String.toIngredientList(): List<Ingredient> =
    lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { raw ->
            // "cantidad unidad resto"; si falla guardamos todo como nombre
            val parts = raw.split(" ", limit = 3)
            val qty   = parts.getOrNull(0)?.toFloatOrNull() ?: 1f
            val unit  = parts.getOrNull(1) ?: ""
            val name  = parts.getOrNull(2) ?: raw
            Ingredient(qty, unit, name)
        }.toList()

/* inverso: List<Ingredient> → texto multilínea */
private val RecipeEntity.ingredientsText: String
    get() = ingredients.joinToString("\n") { ing ->
        buildString {
            append(ing.qty)
            if (ing.unit.isNotBlank()) append(" ${ing.unit}")
            append(" ${ing.name}")
        }
    }
