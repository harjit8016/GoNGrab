plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.gongrab.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gongrab.tv"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Only build for arm64-v8a — covers 100% of modern Android TVs
        // This alone removes ~30% of the APK by eliminating 3 unused ABI native libs
        ndk {
            abiFilters += listOf("arm64-v8a")
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
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.activity.compose)
    implementation(compose.ui)
    debugImplementation(compose.uiTooling)   // Only in debug builds
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Firebase
    implementation(libs.gitlive.firebase.firestore)
}
