
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
        val commonMain by getting {
            dependencies {
                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Lifecycle
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                // Дополнительные Compose зависимости
                implementation("androidx.compose.material:material:1.8.3")
                implementation("androidx.compose.runtime:runtime-livedata:1.8.3")

                // Koin + Voyager
                implementation("io.insert-koin:koin-android:4.1.0")
                implementation("io.insert-koin:koin-androidx-compose:4.1.0")
                implementation("cafe.adriel.voyager:voyager-koin:1.1.0-beta03")

                // Ktor
                implementation("io.ktor:ktor-client-core:3.2.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

                // Gson (опционально для Room TypeConverters)
                implementation("com.google.code.gson:gson:2.10.1")

                // Paging (общая часть)
                implementation("androidx.paging:paging-compose:3.3.6")

                // Coil
                implementation("io.coil-kt:coil-compose:2.7.0")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                // Room (Android)
                implementation("androidx.room:room-runtime:2.7.2")
                implementation("androidx.room:room-ktx:2.7.2")
                implementation("androidx.room:room-paging:2.7.2")

                // Ktor Android
                implementation("io.ktor:ktor-client-cio:3.2.3")

                // Paging runtime (Android)
                implementation("androidx.paging:paging-runtime-ktx:3.3.6")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
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

        // Здесь уже глобальный контекст DependencyHandler, тут доступен kspAndroid
    add("kspAndroid", "androidx.room:room-compiler:2.7.2")

}
