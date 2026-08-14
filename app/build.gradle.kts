plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jocala.glucocalc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jocala.glucocalc"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "2.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/glucocalc.jks")
            storePassword = "glucocalc123"
            keyAlias = "glucocalc"
            keyPassword = "glucocalc123"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
