// Plugins que indican qué tipo de proyecto es (Android App y Kotlin)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// ❌ Se elimina la sección de lectura manual de 'local.properties'
// Las propiedades de 'gradle.properties' se acceden directamente vía 'project.properties'

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

        // ==========================================================
        // ✅ CLAVES PARA FAILOVER LEÍDAS DESDE GRADLE.PROPERTIES
        // ==========================================================
        buildConfigField(
            "String",
            "API_KEY_PRINCIPAL", // Único
            project.properties["MY_API_KEY_PRINCIPAL"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_1", // Único
            project.properties["MY_API_KEY_RESPALDO_1"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_2", // Único
            project.properties["MY_API_KEY_RESPALDO_2"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_3", // Único
            project.properties["MY_API_KEY_RESPALDO_3"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_4", // Único
            project.properties["MY_API_KEY_RESPALDO_4"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_5", // Único
            project.properties["MY_API_KEY_RESPALDO_5"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_6", // Único
            project.properties["MY_API_KEY_RESPALDO_6"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_7", // Único
            project.properties["MY_API_KEY_RESPALDO_7"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_8", // Único
            project.properties["MY_API_KEY_RESPALDO_8"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_9", // Único
            project.properties["MY_API_KEY_RESPALDO_9"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_10", // Único
            project.properties["MY_API_KEY_RESPALDO_10"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_11", // Único
            project.properties["MY_API_KEY_RESPALDO_11"] as String
        )
        buildConfigField(
            "String",
            "API_KEY_RESPALDO_12", // Único
            project.properties["MY_API_KEY_RESPALDO_12"] as String
        )
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

    // --- DEPENDENCIAS DE ARQUITECTURA (Para ViewModel y Coroutines Scope) ---
    // ✅ NECESARIO para que Kotlin reconozca 'viewModelScope'
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // --- DEPENDENCIAS DE RED (PARA HORÓSCOPO) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // --- DEPENDENCIAS DE UI Y ANDROID ---
    implementation("androidx.core:core-ktx:1.12.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

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
