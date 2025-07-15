package com.tunombre.recetario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunombre.recetario.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

class RecipeViewModel(
    private val repo: RecipeRepository
) : ViewModel() {

    /* --- filtros --- */
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCat = MutableStateFlow<Category?>(null)
    private val _selectedDifficulty = MutableStateFlow<Int?>(null)

    /** Flujo que observa la UI */
    val recipes: StateFlow<List<RecipeEntity>> =
        combine(_searchQuery, _selectedCat, _selectedDifficulty) { q, c, d -> Triple(q, c, d) }
            .flatMapLatest { (q, c, d) ->
                // Obtener recetas por categoría si está seleccionada, si no, todas
                val baseFlow = if (c != null) repo.getByCategoryFlow(c) else repo.getAllFlow()
                baseFlow.map { list ->
                    list.filter { recipe ->
                        // Filtro por dificultad
                        val difficultyMatch = d == null || recipe.difficulty == d
                        // Filtro por texto (nombre o ingredientes)
                        val query = q.trim().lowercase()
                        val nameMatch = recipe.name.lowercase().contains(query)
                        val ingredientsMatch = recipe.ingredients.any { ing ->
                            ing.name.lowercase().contains(query)
                        }
                        val textMatch = query.isBlank() || nameMatch || ingredientsMatch
                        difficultyMatch && textMatch
                    }
                }
            }
            .stateIn(
                scope   = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /* --- acciones de búsqueda / filtro --- */
    fun onSearch(text: String)     { _searchQuery.value = text }
    fun onCategory(cat: Category?) { _selectedCat.value = cat }
    fun onDifficulty(dif: Int?)    { _selectedDifficulty.value = dif }

    /* --- CRUD --- */
    fun insert(r: RecipeEntity)  = viewModelScope.launch { repo.insert(r) }
    fun update(r: RecipeEntity)  = viewModelScope.launch { repo.update(r) }
    fun delete(r: RecipeEntity)  = viewModelScope.launch {
        repo.delete(r)
        removeChecklist(r.id)
    }

    fun toggleFavorite(r: RecipeEntity) =
        viewModelScope.launch { repo.toggleFavorite(r) }

    suspend fun getRecipeById(id: Int): RecipeEntity? = repo.getRecipeById(id)

    fun random(onResult: (RecipeEntity) -> Unit) =
        viewModelScope.launch { onResult(repo.random()) }

    /* ---------- check-list por receta ---------- */

    private val _checklists =
        mutableStateMapOf<Int, SnapshotStateList<Boolean>>()

    fun checklistFor(recipe: RecipeEntity): SnapshotStateList<Boolean> {
        val wanted = recipe.ingredients.size
        val list   = _checklists.getOrPut(recipe.id) {
            MutableList(wanted) { false }.toMutableStateList()
        }
        // ajusta longitud sin removeLast()
        while (list.size < wanted) list.add(false)
        while (list.size > wanted) list.removeAt(list.lastIndex)
        return list
    }

    fun toggleIngredient(recipeId: Int, index: Int) {
        _checklists[recipeId]?.let { list -> list[index] = !list[index] }
    }

    fun removeChecklist(id: Int) { _checklists -= id }
}
