plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // ➜  Room necesita Kapt para generar los DAO
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace  = "com.tunombre.recetario"
    compileSdk = 35          // Si usas Iguana Canary/AGP 9; de lo contrario pon 34

    defaultConfig {
        applicationId = "com.tunombre.recetario"
        minSdk        = 34
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    /* ———⚙️  Compatibilidad Java/Kotlin ——— */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17   // 17 recomendado para Compose ≥1.5
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    /* ———📐  Jetpack Compose ——— */
    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"      // Usa la última estable
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    // ———🧩  Plantilla original ———
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation.android)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    // ———📚  Room (BD local) ———
    implementation("androidx.room:room-ktx:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")

    // ———📦  ViewModel + StateFlow en Compose ———
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // ———⚡  Coroutines (reactividad) ———
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ———🖼️  Coil (carga de imágenes) ———
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")       // carga de imágenes
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui-text")
    implementation ("androidx.compose.runtime:runtime:1.6.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ———📅  DataStore Preferences ———
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ———🖼️  Accompanist Navigation Animation ———
    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")
}