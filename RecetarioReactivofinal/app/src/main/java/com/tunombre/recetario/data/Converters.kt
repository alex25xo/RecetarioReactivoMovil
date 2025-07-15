package com.tunombre.recetario.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    /* ---------- Category ---------- */
    @TypeConverter
    fun fromCategory(cat: Category): String = cat.name

    @TypeConverter
    fun toCategory(str: String): Category = Category.valueOf(str)

    /* ---------- List<Ingredient> ---------- */
    private val gson = Gson()
    private val listType = object : TypeToken<List<Ingredient>>() {}.type

    // Convertir List<Ingredient> a String (JSON)
    @TypeConverter
    fun listToJson(list: List<Ingredient>): String = gson.toJson(list, listType)

    // Convertir String (JSON) a List<Ingredient>
    @TypeConverter
    fun jsonToList(json: String): List<Ingredient> =
        gson.fromJson(json, listType)
}
