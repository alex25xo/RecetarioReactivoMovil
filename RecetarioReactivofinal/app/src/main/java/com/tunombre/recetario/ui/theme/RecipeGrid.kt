@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.tunombre.recetario.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tunombre.recetario.data.RecipeEntity
import com.tunombre.recetario.viewmodel.RecipeViewModel

@Composable
fun RecipeGrid(
    vm:            RecipeViewModel,
    onlyFavorites: Boolean                    = false,
    recipeHolder:  MutableMap<Int, RecipeEntity>,
    nav:           NavHostController,
    modifier:      Modifier                   = Modifier
) {
    /* —— aquí damos tipo explícito —— */
    val recipes: List<RecipeEntity> by vm.recipes.collectAsState(
        initial = emptyList()
    )

    /* guardamos copia en el holder para uso posterior */
    LaunchedEffect(recipes) {
        recipes.forEach { recipeHolder[it.id] = it }
    }

    /* filtrado de favoritos si procede */
    val visible = remember(recipes, onlyFavorites) {
        if (onlyFavorites) recipes.filter { it.isFavorite } else recipes
    }

    if (recipes.isEmpty()) {
        CircularProgressIndicator(modifier)
        return
    }

    LazyVerticalGrid(
        columns        = GridCells.Fixed(2),
        contentPadding = PaddingValues(4.dp),
        modifier       = modifier
    ) {
        items(
            items = visible,
            key   = { it.id }      // ahora sí reconoce id
        ) { recipe ->
            RecipeCardRich(
                recipe = recipe,
                onOpen = { nav.navigate("detail/${recipe.id}") },
                onFav  = { vm.toggleFavorite(recipe) }
            )
        }
    }
}
