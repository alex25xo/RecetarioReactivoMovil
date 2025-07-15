package com.tunombre.recetario.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert
    suspend fun insert(recipe: RecipeEntity)

    @Insert
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Query("SELECT * FROM recipes")
    fun getAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :q || '%'")
    fun search(q: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE category = :c")
    fun getByCategory(c: Category): Flow<List<RecipeEntity>>

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun count(): Int

    @Query("SELECT * FROM recipes ORDER BY RANDOM() LIMIT 1")
    suspend fun random(): RecipeEntity

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: Int): RecipeEntity

    @Delete
    suspend fun delete(recipe: RecipeEntity)
}

