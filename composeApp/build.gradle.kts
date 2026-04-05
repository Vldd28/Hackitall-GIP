import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// ── Read .env ────────────────────────────────────────────────────────────────
fun loadEnv(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()
    return envFile.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") && "=" in it }
        .associate { line ->
            val idx = line.indexOf('=')
            line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }
}

val env = loadEnv()
val supabaseUrl: String = env["SUPABASE_URL"] ?: ""
val supabaseAnonKey: String = env["SUPABASE_ANON_KEY"] ?: ""

// ── Read local.properties ─────────────────────────────────────────────────────
fun loadLocalProperties(): Map<String, String> {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return emptyMap()
    return file.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") && "=" in it }
        .associate { line ->
            val idx = line.indexOf('=')
            line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }
}

val localProps = loadLocalProperties()
val mapsApiKey: String = localProps["MAPS_API_KEY"] ?: ""

// ── Generate SupabaseConfig.kt from .env values ───────────────────────────────
val generatedSrcDir = layout.buildDirectory.dir("generated/kotlin/commonMain")

val generateSupabaseConfig by tasks.registering {
    outputs.dir(generatedSrcDir)
    doFirst {
        val dir = generatedSrcDir.get().asFile.resolve("org/example/project/data/remote")
        dir.mkdirs()
        dir.resolve("SupabaseConfig.kt").writeText(
            """
            package org.example.project.data.remote

            internal object SupabaseConfig {
                const val URL = "$supabaseUrl"
                const val ANON_KEY = "$supabaseAnonKey"
            }
            """.trimIndent()
        )
        dir.resolve("PlacesConfig.kt").writeText(
            """
            package org.example.project.data.remote

            internal object PlacesConfig {
                const val API_KEY = "$mapsApiKey"
            }
            """.trimIndent()
        )
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.maps.compose)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonMain {
            kotlin.srcDir(generateSupabaseConfig)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)

            // Supabase
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.realtime)

            // Serialization & coroutines
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Navigation
            implementation(libs.navigation.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
    debugImplementation(libs.compose.uiTooling)
}

// Force kotlinx-datetime to a single version compatible with Kotlin 2.3.x / wasmJs.
// Supabase pulls in an older version whose Instant class clashes with kotlin.time.Instant
// added in Kotlin 2.0 stdlib, causing an IrTypeAliasSymbolImpl "already bound" crash.
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.get()}")
    }
}

