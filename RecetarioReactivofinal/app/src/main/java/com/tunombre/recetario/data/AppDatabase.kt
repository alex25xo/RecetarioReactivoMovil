package com.tunombre.recetario.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Historial de cambios en la tabla `recipes`
 * 1 → 2 : se añadió            `imageUri`
 * 2 → 3 : se añadió columna    `ingredientsJson` (JSON de ingredientes)
 * 3 → 4 : se elimina la vieja  `ingredients` (TEXT) y
 *          todo se guarda solo en `ingredientsJson`
 *
 * Mientras desarrollas, puedes usar .fallbackToDestructiveMigration(),
 * pero para producción registra SIEMPRE las migraciones necesarias.
 */
@Database(
    entities = [RecipeEntity::class],
    version  = 4,           // ⬅︎ ahora 4
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    /* ─────────── Migraciones ─────────── */

    companion object {

        /** 1 → 2 : nueva columna imageUri (TEXT) */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recipes ADD COLUMN imageUri TEXT"
                )
            }
        }

        /**
         * 2 → 3 : añade ingredientsJson (TEXT NOT NULL DEFAULT '[]') y
         *         copia cada línea antigua de `ingredients`
         *         a un JSON mínimo [{qty:1.0,unit:'',name:'…'}]
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recipes " +
                            "ADD COLUMN ingredientsJson TEXT NOT NULL DEFAULT '[]'"
                )

                db.query("SELECT id, ingredients FROM recipes").use { c ->
                    val idCol  = c.getColumnIndexOrThrow("id")
                    val txtCol = c.getColumnIndexOrThrow("ingredients")

                    while (c.moveToNext()) {
                        val id   = c.getInt(idCol)
                        val txt  = c.getString(txtCol) ?: ""
                        val json = txt.lines()
                            .filter { it.isNotBlank() }
                            .joinToString(prefix = "[", postfix = "]") { line ->
                                """{"qty":1.0,"unit":"","name":${line.trim().jsonQuote()}}"""
                            }

                        db.execSQL(
                            "UPDATE recipes SET ingredientsJson = ? WHERE id = ?",
                            arrayOf<Any>(json, id)
                        )
                    }
                }
            }

            /** Escapa comillas para JSON */
            private fun String.jsonQuote(): String =
                "\"" + replace("\"", "\\\"") + "\""
        }

        /**
         * 3 → 4 : elimina el campo legacy `ingredients` recreando la tabla
         *         sin él (Room no soporta DROP COLUMN).
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE recipes_new (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name            TEXT    NOT NULL,
                        category        TEXT    NOT NULL,
                        steps           TEXT    NOT NULL,
                        imageUri        TEXT,
                        ingredientsJson TEXT    NOT NULL,
                        calories        INTEGER NOT NULL,
                        protein         INTEGER NOT NULL,
                        cookTime        INTEGER NOT NULL,
                        isFavorite      INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO recipes_new
                    (id,name,category,steps,imageUri,ingredientsJson,
                     calories,protein,cookTime,isFavorite)
                    SELECT id,name,category,steps,imageUri,ingredientsJson,
                           calories,protein,cookTime,isFavorite
                    FROM recipes
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE recipes")
                db.execSQL("ALTER TABLE recipes_new RENAME TO recipes")
            }
        }
    }
}
