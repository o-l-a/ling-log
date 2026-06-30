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
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.myinputlog"
        minSdk = 26
        targetSdk = 37
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
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
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)
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

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)

    // Confetti
    implementation(libs.konfetti.compose)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.integrity)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(kotlin("reflect"))

    implementation(libs.charty)
}