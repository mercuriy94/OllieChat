import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinSymbolProcessiong)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.mercuriy94.olliechat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mercuriy94.olliechat"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            javaParameters = true
            jvmTarget = JvmTarget.JVM_21
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

detekt {
    toolVersion = libs.versions.detekt.get()
    source.setFrom("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")
    parallel = true
    config.setFrom(rootProject.files("detekt/detekt-config.yml"))
    buildUponDefaultConfig = true
    baseline = rootProject.file("detekt/baseline.yml")
    ignoreFailures = false
    basePath = rootDir.toString()
    autoCorrect = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        html.outputLocation = file("build/reports/detekt/detekt.html")
    }

    finalizedBy("openDetektReport")
}

tasks.register("openDetektReport", Exec::class) {
    val reportPath = file("build/reports/detekt/detekt.html").absolutePath
    val os = System.getProperty("os.name").lowercase()
    val command = when {
        os.contains("win") -> listOf("cmd", "/c", "start", reportPath)
        os.contains("mac") -> listOf("open", reportPath)
        else -> listOf("xdg-open", reportPath)
    }
    commandLine = command
}

dependencies {

    implementation(libs.commonmark)
    implementation(libs.richtext)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.workmanager.ktx)
    implementation(libs.material.icon.extended)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)

    /*Serialization*/
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.langchain4j)
    implementation(libs.langchain4j.core)
    implementation(libs.langchain4j.kotlin)
    implementation(libs.langchain4j.ollama)
    implementation(libs.langchain4j.http.client)
    implementation(libs.langchain4j.http.client.jdk)

    //Network
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.kotlinx.serialization)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.timber)

    /*Room*/
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}