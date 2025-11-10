plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.proyecto_app"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        buildConfig = true      // 👈 Habilita BuildConfig
    }

    defaultConfig {
        applicationId = "com.example.proyecto_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        "-proj-_F2QO1qUe8XKWwdDgaBJGNoXEqF0gchMXj3GhdhhQplRFY3CjmcUOJk_1RSByj1ZaTeXGxQnW_T3BlbkFJWx56LE0SnB9nQyfruSPlMfZrUmp7A_DxXrNYylQwt_RnGKCQ099JywLejg5pIlP3_ojMBBu-4A"
        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"-proj-_F2QO1qUe8XKWwdDgaBJGNoXEqF0gchMXj3GhdhhQplRFY3CjmcUOJk_1RSByj1ZaTeXGxQnW_T3BlbkFJWx56LE0SnB9nQyfruSPlMfZrUmp7A_DxXrNYylQwt_RnGKCQ099JywLejg5pIlP3_ojMBBu-4A\""
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

    // CameraX
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion") // PreviewView

    // ML Kit (on-device)
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:object-detection:17.0.1")

    // AndroidX / UI que ya tenías
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // OkHttp para llamadas HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Lifecycle (para lifecycleScope)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
