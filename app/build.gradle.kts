plugins {
    alias(libs.plugins.android.application)
    // 👇 Esta es la línea mágica que faltaba para compilar el código 👇
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // ==========================================
    // BASE DE DATOS: ROOM Y CORRUTINAS
    // ==========================================
    // Librerías de ejecución de Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Corrutinas para procesar datos en segundo plano sin congelar la pantalla
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Compilador moderno KSP para generar la base de datos
    add("ksp", "androidx.room:room-compiler:2.6.1")
}