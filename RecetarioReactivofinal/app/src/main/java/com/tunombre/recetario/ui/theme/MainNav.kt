@file:OptIn(ExperimentalMaterial3Api::class)
package com.tunombre.recetario.ui.theme

/* ---------- imports ---------- */
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tunombre.recetario.data.RecipeEntity
import com.tunombre.recetario.viewmodel.RecipeViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tunombre.recetario.ui.SettingsScreen
import com.tunombre.recetario.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally


/* ---------- nav ---------- */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNav(vm: RecipeViewModel) {
    val nav = rememberNavController()

    /** "memoria" temporal de recetas abiertas (para no volver a cargarlas si giras) */
    val recipeHolder = remember { mutableMapOf<Int, RecipeEntity>() }

    AnimatedNavHost(
        navController = nav,
        startDestination = "explore",
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) }
    ) {

        /* ---------- Explorar ---------- */
        composable("explore") {
            val settingsViewModel: SettingsViewModel = viewModel()
            RecipeScreen(
                vm = vm,
                navController = nav,
                onOpen = { recipe ->
                    recipeHolder[recipe.id] = recipe
                    nav.navigate("detail/${recipe.id}")
                },
                onAddRecipe = {
                    nav.navigate("add")
                }
            )
        }

        /* ---------- Detalle ---------- */
        composable(
            "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id")

            /** estado save-able que puede quedar nulo hasta que cargue */
            var recipe by remember(id) { mutableStateOf<RecipeEntity?>(recipeHolder[id]) }

            LaunchedEffect(id) {
                if (recipe == null) {
                    recipe = vm.getRecipeById(id)
                    recipe?.let { recipeHolder[id] = it }
                }
            }

            recipe?.let { rec ->
                RecipeDetailScreen(
                    recipe = rec,
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("edit/$id") },
                    onDelete = { r ->
                        vm.delete(r)
                        nav.popBackStack()
                    }
                )
            }
        }

        /* ---------- Favoritos ---------- */
        composable("favorites") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                "❤️ Mis Favoritos",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { padding ->
                // Mostrar solo recetas favoritas
                val recipes by vm.recipes.collectAsState()
                val favoriteRecipes = recipes.filter { it.isFavorite }
                
                if (favoriteRecipes.isEmpty()) {
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
                                Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes favoritos aún",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Agrega recetas a favoritos para verlas aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(favoriteRecipes) { recipe ->
                            RecipeCardRich(
                                recipe = recipe,
                                onFav = { vm.toggleFavorite(recipe) },
                                onOpen = { 
                                    recipeHolder[recipe.id] = recipe
                                    nav.navigate("detail/${recipe.id}")
                                }
                            )
                        }
                    }
                }
            }
        }

        /* ---------- Nuevo ---------- */
        composable("add") {
            AddRecipeScreen(
                vm = vm,
                onDone = { nav.popBackStack() }
            )
        }

        /* ---------- Editar ---------- */
        composable(
            "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id")
            var recipe by remember(id) { mutableStateOf<RecipeEntity?>(recipeHolder[id]) }

            LaunchedEffect(id) {
                if (recipe == null) {
                    recipe = vm.getRecipeById(id)
                    recipe?.let { recipeHolder[id] = it }
                }
            }

            recipe?.let { rec ->
                EditRecipeScreen(
                    original = rec,
                    vm = vm,
                    onDone = { nav.popBackStack() }
                )
            }
        }

        /* ---------- Configuración ---------- */
        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(settingsViewModel = settingsViewModel, onBack = { nav.popBackStack() })
        }
    }
}
