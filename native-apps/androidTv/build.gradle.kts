plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.gongrab.tv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gongrab.tv"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // Build for 64-bit (arm64-v8a, x86_64) and 32-bit (armeabi-v7a, x86) to support all TVs and emulators
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    signingConfigs {
        // No custom signing config; using debug signing.
    }

    buildTypes {
        release {
            // R8 full-mode: dead code elimination + obfuscation
            isMinifyEnabled = true
            // Strip unused resources (fonts, images, layouts not referenced)
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            // Strip duplicate/unnecessary META-INF files that bloat the APK
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "google/**/*.proto",        // Firestore .proto definitions not needed at runtime
                "**/*.proto"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.google.firebase.bom))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.ui.tooling)   // Only in debug builds
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Firebase
    implementation(libs.gitlive.firebase.firestore)
}
