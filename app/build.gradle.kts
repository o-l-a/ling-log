plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.myinputlog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myinputlog"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY")}\"")

        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.myinputlog"
    }

    buildTypes.all {
        buildConfigField("String", "CLIENT_ID", "\"${project.findProperty("CLIENT_ID")}\"")
        buildConfigField("String", "API_KEY", "\"${project.findProperty("API_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Enable R8 for production
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            kotlin.directories.add(
                layout.buildDirectory.dir("generated/ksp/main/kotlin").get().asFile.path
            )
        }
        getByName("debug") {
            kotlin.directories.add(
                layout.buildDirectory.dir("generated/ksp/debug/kotlin").get().asFile.path
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Switched from kapt to ksp
    implementation(libs.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.serialization)

    // Image Loading
    implementation(libs.coil.compose)

    // Firebase (Use BOM for all)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.perf)

    // DataStore
    implementation(libs.androidx.datastore)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Auth & OAuth
    implementation(libs.openid.appauth)
    implementation("com.auth0.android:jwtdecode:2.0.2")

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)

    // Confetti
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.google.accompanist.systemui)

    implementation(libs.androidx.ui.tooling.preview) // Must be implementation
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.kotlinx.serialization.json)

    implementation("com.google.android.play:integrity:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation(kotlin("reflect"))
}