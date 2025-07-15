package com.tunombre.recetario.ui.theme

/* ─── Imports ─── */
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunombre.recetario.VoiceRecognitionService
import com.tunombre.recetario.data.Category
import com.tunombre.recetario.data.RecipeEntity
import com.tunombre.recetario.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Toast



@Composable
fun RecipeScreen(
    vm: RecipeViewModel,
    navController: NavController,
    onOpen: (RecipeEntity) -> Unit,
    onAddRecipe: () -> Unit = {}
) {
    val recipes by vm.recipes.collectAsState()
    val selectedDifficulty = remember { mutableStateOf<Int?>(null) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var scope = rememberCoroutineScope()
    
    // Servicio de reconocimiento de voz
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceDialog = true
        } else {
            Toast.makeText(context, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
        }
    }
    val voiceService = remember { VoiceRecognitionService(context) }
    
    // Observar estados del servicio de voz
    val isListening by voiceService.isListening.collectAsState()
    val recognizedText by voiceService.recognizedText.collectAsState()
    val voiceError by voiceService.error.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CategoryDrawer(
                onCategorySelect = { category ->
                    vm.onCategory(category)
                    scope.launch {
                        drawerState.close()
                    }
                },
                onClearCategories = {
                    vm.onCategory(null)
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = { 
                RecipeTopBar(
                    onSearch = { vm.onSearch(it) },
                    onVoiceClick = {
                        val permissionStatus = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.RECORD_AUDIO
                        )
                        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                            showVoiceDialog = true
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onFavoritesClick = { navController.navigate("favorites") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            },
            floatingActionButton = {
                Column {
                    // Botón de receta aleatoria
                    FloatingActionButton(
                        onClick = { vm.random { onOpen(it) } },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "Receta Aleatoria")
                    }
                    
                    // Botón de agregar receta
                    FloatingActionButton(
                        onClick = onAddRecipe,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar Receta")
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                // Filtros de dificultad
                DifficultyChips(
                    selected = selectedDifficulty.value,
                    onSelect = {
                        selectedDifficulty.value = it
                        vm.onDifficulty(it)
                    }
                )
                if (recipes.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay recetas disponibles",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Agrega tu primera receta tocando el botón +",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = recipes,
                            key = { it.id }
                        ) { recipe: RecipeEntity ->
                            RecipeCardRich(
                                recipe = recipe,
                                onFav = { vm.toggleFavorite(recipe) },
                                onOpen = { onOpen(recipe) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de voz
    if (showVoiceDialog) {
        VoiceSearchDialog(
            isListening = isListening,
            recognizedText = recognizedText,
            error = voiceError,
            onStartListening = { voiceService.startListening() },
            onStopListening = { voiceService.stopListening() },
            onDismiss = { 
                showVoiceDialog = false
                voiceService.stopListening()
            },
            onVoiceResult = { query ->
                // Procesamiento avanzado de comandos de voz
                val lower = query.trim().lowercase()
                // Mapear palabras clave a categorías
                val categoryMap = mapOf(
                    "desayuno" to Category.BREAKFAST,
                    "almuerzo" to Category.LUNCH,
                    "comida" to Category.LUNCH,
                    "cena" to Category.DINNER,
                    "postre" to Category.DESSERT,
                    "postres" to Category.DESSERT,
                    "snack" to Category.SNACK,
                    "bebida" to Category.BEVERAGE,
                    "bebidas" to Category.BEVERAGE
                )
                val difficultyMap = mapOf(
                    "fácil" to 1,
                    "facil" to 1,
                    "media" to 2,
                    "intermedia" to 2,
                    "difícil" to 3,
                    "dificil" to 3
                )
                var cat: Category? = null
                var dif: Int? = null
                // Buscar palabras clave de categoría y dificultad
                for ((k, v) in categoryMap) {
                    if (lower.contains(k)) cat = v
                }
                for ((k, v) in difficultyMap) {
                    if (lower.contains(k)) dif = v
                }
                // Si hay comando 'buscar', eliminarlo del query
                val clean = lower.replace("buscar", "")
                    .replace("recetas", "")
                    .replace("receta", "")
                    .replace("fáciles", "")
                    .replace("faciles", "")
                    .replace("difíciles", "")
                    .replace("dificiles", "")
                    .replace("fácil", "")
                    .replace("facil", "")
                    .replace("media", "")
                    .replace("intermedia", "")
                    .replace("difícil", "")
                    .replace("dificil", "")
                    .replace("desayuno", "")
                    .replace("almuerzo", "")
                    .replace("comida", "")
                    .replace("cena", "")
                    .replace("postre", "")
                    .replace("postres", "")
                    .replace("snack", "")
                    .replace("bebida", "")
                    .replace("bebidas", "")
                    .trim()
                // Aplicar filtros
                vm.onCategory(cat)
                vm.onDifficulty(dif)
                vm.onSearch(clean)
                showVoiceDialog = false
                voiceService.stopListening()
            }
        )
    }
    
    // Limpiar el servicio cuando se desmonte
    DisposableEffect(Unit) {
        onDispose {
            voiceService.destroy()
        }
    }
}

@Composable
private fun RecipeTopBar(
    onSearch: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onMenuClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título principal con botón de menú
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menú de categorías",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "🍳 Recetario Gourmet",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFavoritesClick) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favoritos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de búsqueda con botón de voz
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        onSearch(it)
                    },
                    label = { Text("Buscar recetas...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón de voz
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    IconButton(
                        onClick = onVoiceClick
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Búsqueda por voz",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(
    onSelect: (Category) -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Categorías",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.values().forEach { category ->
                    CategoryChip(
                        category = category,
                        onClick = { onSelect(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onClear,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Filled.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mostrar todas")
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(category)
    val categoryName = getCategoryDisplayName(category)
    
    AssistChip(
        onClick = onClick,
        label = { 
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = categoryColor.copy(alpha = 0.1f),
            labelColor = categoryColor
        ),
        modifier = modifier
    )
}

@Composable
private fun VoiceSearchDialog(
    isListening: Boolean,
    recognizedText: String,
    error: String?,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onDismiss: () -> Unit,
    onVoiceResult: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Búsqueda por Voz",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isListening) {
                    Text(
                        text = "🎤 Escuchando...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Habla ahora para buscar recetas",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Toca el micrófono para buscar recetas por voz",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                
                if (recognizedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Buscando: $recognizedText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row {
                // Botón de micrófono
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    color = if (isListening) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    IconButton(
                        onClick = if (isListening) onStopListening else onStartListening
                    ) {
                        Icon(
                            if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isListening) "Detener" else "Iniciar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón de búsqueda
                if (recognizedText.isNotEmpty()) {
                    Button(
                        onClick = { onVoiceResult(recognizedText) }
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buscar")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Funciones auxiliares (ya definidas en RecipeCardRich.kt)
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

@Composable
private fun CategoryDrawer(
    onCategorySelect: (Category) -> Unit,
    onClearCategories: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Título del drawer
        Text(
            text = "📂 Categorías",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Botón para mostrar todas las categorías
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClearCategories() }
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.AllInclusive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Todas las recetas",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de categorías
        Category.values().forEach { category ->
            CategoryDrawerItem(
                category = category,
                onClick = { onCategorySelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryDrawerItem(
    category: Category,
    onClick: () -> Unit
) {
    val categoryColor = getCategoryColor(category)
    val categoryName = getCategoryDisplayName(category)
    val categoryIcon = getCategoryIcon(category)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = categoryColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getCategoryIcon(category: Category): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        Category.BREAKFAST -> Icons.Filled.WbSunny
        Category.LUNCH -> Icons.Filled.Restaurant
        Category.DINNER -> Icons.Filled.NightsStay
        Category.DESSERT -> Icons.Filled.Cake
        Category.SNACK -> Icons.Filled.LocalCafe
        Category.BEVERAGE -> Icons.Filled.LocalBar
    }
}

@Composable
private fun DifficultyChips(selected: Int?, onSelect: (Int?) -> Unit) {
    val difficulties = listOf(
        null to "Todas",
        1 to "Fácil",
        2 to "Media",
        3 to "Difícil"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        difficulties.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}
