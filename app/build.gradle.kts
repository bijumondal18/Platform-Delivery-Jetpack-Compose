plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.platform.platformdelivery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.platform.platformdelivery"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.play.services.location)
    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.accompanist.placeholder.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
    // Retrofit
    implementation(libs.retrofit)

    // Gson Converter for JSON
    implementation(libs.converter.gson)

    // OkHttp
    implementation(libs.okhttp)

    // OkHttp Logging Interceptor (for debugging API calls)
    implementation(libs.logging.interceptor)

    implementation(libs.coil.compose)
    implementation(libs.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material.icons.extended.android)

    implementation("com.google.maps.android:maps-compose:4.3.3") // latest as of Oct 2025
    implementation("com.google.android.gms:play-services-maps:19.0.0")
}