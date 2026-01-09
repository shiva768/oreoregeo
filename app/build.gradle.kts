plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.zelretch.oreoregeo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zelretch.oreoregeo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // OSM OAuth credentials from environment variables or GitHub Secrets
        buildConfigField("String", "OSM_CLIENT_ID", "\"${System.getenv("OSM_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "OSM_CLIENT_SECRET", "\"${System.getenv("OSM_CLIENT_SECRET") ?: ""}\"")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.security.crypto)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // OpenStreetMap (osmdroid)
    implementation(libs.osmdroid)

    // Network
    implementation(libs.bundles.network)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Google Play Services (for location and Drive)
    implementation(libs.bundles.google.play)
    implementation(libs.identityCredentials)
    implementation(libs.identityCredentialsPlayAuth)
    implementation(libs.googleId)
    implementation(libs.authBlockstore)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Logging
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.googleFirebaseBom))
    implementation(libs.googleFirebaseAnalytics)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.bundles.android.testing)
    debugImplementation(libs.bundles.compose.debug)
}
