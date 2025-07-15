package com.tunombre.recetario.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val name:     String,
    val category: Category,

    /**  Se guarda como JSON en la columna real `ingredientsJson`  */
    @ColumnInfo(name = "ingredientsJson")
    val ingredients: List<Ingredient>,

    val steps:    String,
    val imageUri: String? = null,

    val calories: Int = 0,
    val protein:  Int = 0,
    val cookTime: Int = 0,
    val difficulty: Int = 2,
    val servings: Int = 4,
    val description: String = "",

    val isFavorite: Boolean = false
)
