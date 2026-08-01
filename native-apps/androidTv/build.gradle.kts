plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
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
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("gongrab-release.jks")
            storePassword = "gongrab2024"
            keyAlias = "gongrab"
            keyPassword = "gongrab2024"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    
    // Firebase
    implementation(libs.gitlive.firebase.firestore)
}
