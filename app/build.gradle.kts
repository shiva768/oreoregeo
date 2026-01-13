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

    val isCI = System.getenv("CI") != null

    signingConfigs {
        // GHAビルド時のみデバッグ署名設定を適用
        if (isCI) {
            getByName("debug").apply {
                storeFile = file("$rootDir/app/debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // GHAビルド時のみデバッグ署名を明示的に設定
        if (isCI) {
            getByName("debug") {
                signingConfig = signingConfigs.getByName("debug")
            }
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

// detekt / ktlint を「いい塩梅」に動かすための設定
detekt {
    // 公式デフォルト設定の上に、プロジェクトの設定を重ねる
    buildUponDefaultConfig = true
    parallel = true
    config = files("$rootDir/config/detekt/detekt.yml")
    autoCorrect = false

    // レポート設定（CI 向けに SARIF も出力）
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(System.getenv("CI") != null)
    }
}

// detekt タスクの JVM 設定（Kotlin 17 に合わせる）
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    // 生成物やリソースを除外
    setSource(files("src"))
    include("**/*.kt", "**/*.kts")
    exclude(
        "**/build/**",
        "**/generated/**",
        "**/resources/**"
    )
}

// ktlint の除外設定（生成コードやビルド出力を無視）
ktlint {
    debug.set(false)
    ignoreFailures.set(false)
    verbose.set(true)

    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
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
