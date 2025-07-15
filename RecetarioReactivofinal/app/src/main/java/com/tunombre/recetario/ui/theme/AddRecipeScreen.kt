@file:OptIn(ExperimentalMaterial3Api::class)
package com.tunombre.recetario.ui.theme

/* ---------- imports ---------- */
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tunombre.recetario.R
import com.tunombre.recetario.data.*
import com.tunombre.recetario.viewmodel.RecipeViewModel

/* ---------- Pantalla alta de receta ---------- */
@Composable
fun AddRecipeScreen(
    vm: RecipeViewModel,
    onDone: () -> Unit
) {
    /* —— estado del formulario —— */
    var name        by rememberSaveable { mutableStateOf("") }
    var imageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    var category    by rememberSaveable { mutableStateOf(Category.BREAKFAST) }
    var calories    by rememberSaveable { mutableStateOf("") }
    var protein     by rememberSaveable { mutableStateOf("") }
    var cookTime    by rememberSaveable { mutableStateOf("") }
    var servings    by rememberSaveable { mutableStateOf(4) }
    var difficulty  by rememberSaveable { mutableStateOf(1) } // 1: Fácil, 2: Media, 3: Difícil
    var showSuccess by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf<String?>(null) }

    // Ingredientes y pasos dinámicos
    var ingredients by remember { mutableStateOf(mutableListOf("")) }
    var steps       by remember { mutableStateOf(mutableListOf("")) }

    val imageUri = imageUriStr?.let { Uri.parse(it) }
    val ctx = LocalContext.current
    val pick = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            ctx.contentResolver.takePersistableUriPermission(
                it, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        imageUriStr = uri?.toString()
    }
    val scroll = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Validación
    val isNameValid = name.isNotBlank()
    val areIngredientsValid = ingredients.any { it.isNotBlank() }
    val areStepsValid = steps.any { it.isNotBlank() }
    val canSave = isNameValid && areIngredientsValid && areStepsValid

    // Feedback visual al guardar
    if (showSuccess) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Receta guardada con éxito")
            showSuccess = false
            onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Receta") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* —— foto —— */
            OutlinedButton(
                onClick = {
                    pick.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (imageUri == null) "Elegir foto" else "Cambiar foto") }

            AnimatedVisibility(visible = imageUri != null) {
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            /* —— campos —— */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                isError = !isNameValid && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Selector de categoría
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = getCategoryDisplayName(category),
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
                            text = { Text(getCategoryDisplayName(cat)) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Ingredientes dinámicos
            Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
            ingredients.forEachIndexed { idx, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            ingredients = ingredients.toMutableList().also { it[idx] = newValue }
                        },
                        label = { Text("Ingrediente ${idx + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedVisibility(visible = ingredients.size > 1) {
                        IconButton(onClick = {
                            ingredients = ingredients.toMutableList().also { it.removeAt(idx) }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar ingrediente")
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { ingredients = (ingredients + "").toMutableList() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Agregar ingrediente")
            }

            // Pasos dinámicos
            Text("Pasos", style = MaterialTheme.typography.titleMedium)
            steps.forEachIndexed { idx, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            steps = steps.toMutableList().also { it[idx] = newValue }
                        },
                        label = { Text("Paso ${idx + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedVisibility(visible = steps.size > 1) {
                        IconButton(onClick = {
                            steps = steps.toMutableList().also { it.removeAt(idx) }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar paso")
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { steps = (steps + "").toMutableList() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Agregar paso")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    calories, { calories = it },
                    label = { Text("kcal") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    protein, { protein = it },
                    label = { Text("g prot") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Sección editable de tiempo, dificultad y porciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tiempo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { if (it.all { c -> c.isDigit() }) cookTime = it },
                        label = { Text("Minutos") },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }
                // Dificultad
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Row {
                        listOf(1 to "Fácil", 2 to "Media", 3 to "Difícil").forEach { (value, label) ->
                            FilterChip(
                                selected = difficulty == value,
                                onClick = { difficulty = value },
                                label = { Text(label) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
                // Porciones
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (servings > 1) servings-- }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Menos")
                        }
                        Text(servings.toString(), modifier = Modifier.width(24.dp), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { servings++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Más")
                        }
                    }
                    Text("Porciones", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Mensaje de error
            AnimatedVisibility(visible = errorMsg != null) {
                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }

            /* —— guardar —— */
            Button(
                onClick = {
                    if (!canSave) {
                        errorMsg = "Completa todos los campos obligatorios."
                        return@Button
                    }
                    errorMsg = null
                    vm.insert(
                        RecipeEntity(
                            name = name,
                            category = category,
                            imageUri = imageUriStr,
                            calories = calories.toIntOrNull() ?: 0,
                            protein = protein.toIntOrNull() ?: 0,
                            cookTime = cookTime.toIntOrNull() ?: 0,
                            difficulty = difficulty,
                            servings = servings,
                            description = "",
                            ingredients = parseIngredientsList(ingredients),
                            steps = steps.joinToString("\n") { it.trim() }.trim()
                        )
                    )
                    showSuccess = true
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}

/* ---------- util: parsear cada línea a Ingredient ---------- */
private fun parseIngredientsList(list: List<String>): List<Ingredient> =
    list.filter { it.isNotBlank() }.mapNotNull { line ->
        val parts = line.trim().split(Regex("\\s+"), limit = 3)
        if (parts.isEmpty()) return@mapNotNull null
        val qty = parts.first().toFloatOrNull() ?: return@mapNotNull null
        val unitAndName = parts.drop(1)
        val unit: String
        val name: String
        when (unitAndName.size) {
            0 -> { unit = ""; name = "" }
            1 -> { unit = ""; name = unitAndName[0] }
            else -> {
                unit = unitAndName[0]
                name = unitAndName.drop(1).joinToString(" ")
            }
        }
        Ingredient(qty, unit, name)
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
