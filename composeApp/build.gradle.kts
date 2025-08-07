import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation("androidx.compose.runtime:runtime-livedata:1.8.3")

            // Koin для Compose и Voyager
            implementation("io.insert-koin:koin-android:4.1.0")
            implementation("io.insert-koin:koin-androidx-compose:4.1.0")
            implementation("cafe.adriel.voyager:voyager-koin:1.1.0-beta03") // Интеграция Koin с Voyager

            // Сеть (Ktor)
            implementation("io.ktor:ktor-client-core:3.2.3")
            implementation("io.ktor:ktor-client-cio:3.2.3") // Android-движок для Ktor
            implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

            // Кеширование (SQLDelight)
            implementation("com.squareup.sqldelight:android-driver:1.5.5")
            implementation("com.squareup.sqldelight:coroutines-extensions-jvm:1.5.5")

            // Paging 3 (для пагинации)
            implementation("androidx.paging:paging-compose:3.3.6")
            implementation("androidx.paging:paging-runtime-ktx:3.3.6")

            // Дополнительные библиотеки
            implementation("io.coil-kt:coil-compose:2.7.0") // Загрузка изображений
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.sexyslave.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.sexyslave.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

