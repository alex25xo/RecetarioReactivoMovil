package com.tunombre.recetario.util

import com.tunombre.recetario.data.RecipeEntity

val RecipeEntity.stepsLines: List<String>
    get() = steps.lines().filter { it.isNotBlank() }

// solo si aún lo usas para el tamaño del checklist
fun RecipeEntity.ingredientsLineCount(): Int = ingredients.size
