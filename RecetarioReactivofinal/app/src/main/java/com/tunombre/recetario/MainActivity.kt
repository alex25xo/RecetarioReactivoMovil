package com.tunombre.recetario          // ⬅︎ único package

/* ────────── Android ────────── */
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.room.Room

/* ────────── Compose ────────── */
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

/* ────────── Proyecto ────────── */
import com.tunombre.recetario.data.AppDatabase
import com.tunombre.recetario.data.RecipeRepository
import com.tunombre.recetario.ui.theme.MainNav
import com.tunombre.recetario.ui.theme.RecetarioReactivoTheme
import com.tunombre.recetario.viewmodel.RecipeViewModel
import com.tunombre.recetario.viewmodel.RecipeViewModelFactory
import com.tunombre.recetario.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

/* ────────── Corrutinas ────────── */
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /* ───── Base de datos y repositorio (lazy, 1 sola instancia) ───── */
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "recipes.db"
        )
            // registra TODAS las migraciones existentes
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4
            )
            //.fallbackToDestructiveMigration()   // ← Úsalo solo si te da igual borrar datos
            .build()
    }

    private val repository by lazy { RecipeRepository(database.recipeDao()) }

    /* ───── ViewModel que sobrevive a recreaciones (giro, etc.) ───── */
    private val vm: RecipeViewModel by viewModels {
        RecipeViewModelFactory(repository)
    }

    /* ──────────────────────────────────────────────────────────────── */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Sembrar datos demo (solo la 1.ª vez) en un hilo IO */
        lifecycleScope.launch(Dispatchers.IO) {
            repository.seedIfEmpty()
        }

        /* UI Compose */
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            RecetarioReactivoTheme(darkTheme = isDarkMode) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNav(vm)        // pasamos SIEMPRE el mismo ViewModel
                }
            }
        }
    }
}
