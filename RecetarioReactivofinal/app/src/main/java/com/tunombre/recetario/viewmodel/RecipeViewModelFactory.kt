package com.tunombre.recetario.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tunombre.recetario.data.RecipeRepository

class RecipeViewModelFactory(
    private val repo: RecipeRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RecipeViewModel(repo) as T
}
