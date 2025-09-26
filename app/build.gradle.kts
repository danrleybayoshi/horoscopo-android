import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// ⚠️ Paso 1: Lee las propiedades del archivo local.properties (si existe)
// Esto permite acceder a las claves que definiremos allí.
val properties = Properties()
if (rootProject.file("local.properties").exists()) {
    rootProject.file("local.properties").inputStream().use { properties.load(it) }
}

android {
    namespace = "com.example.horoscopo_android"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        dataBinding = true
        // Habilitar la generación de la clase BuildConfig
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.horoscopo_android"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ⚠️ Paso 2: Inyecta SOLO la clave API necesaria (RAPIDAPI_KEY)
        // El host se gestiona en HoroscopoService.kt y ya no se inyecta aquí.
        buildConfigField("String", "RAPIDAPI_KEY", properties.getProperty("RAPIDAPI_KEY", "\"CLAVE_DE_PRUEBA\""))
        // Eliminamos buildConfigField para RAPIDAPI_HOST (que era de la API de traducción)
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // --- DEPENDENCIAS DE RED (PARA HORÓSCOPO) ---
    // Retrofit y conversor GSON (Necesario para la API)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp (Necesario para OkHttpClient y LoggingInterceptor)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Logging-interceptor (Útil para debugging)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- DEPENDENCIAS DE ANDROID ---
    implementation("androidx.core:core-ktx:1.12.0")

    // Asumo que estas dependencias vienen del libs.toml
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Dependencias de prueba
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}