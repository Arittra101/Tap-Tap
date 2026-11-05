import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.habitTap"
    // Compiled against 37 because the latest stable AndroidX artifacts require it.
    // minSdk (NFR-8) and targetSdk are independent of this.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.habitTap"
        // NFR-8: Android 7.0 and up.
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Keep the Room schema in Git so later migrations are easy (tech spec 7.1).
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    val keystoreDir = rootProject.file("keystore")
    val props = Properties().apply {
        providers.fileContents(
            rootProject.layout.projectDirectory.file("keystore/release.properties"),
        ).asText.orNull?.let { load(it.reader()) }
    }
    val hasUploadKey = props.getProperty("keyStore") != null

    signingConfigs {
        if (hasUploadKey) {
            create("release") {
                storeFile = File(keystoreDir, props.getProperty("keyStore"))
                storePassword = props.getProperty("keyStorePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyAliasPassword")
            }
        }

        // Fallback so the shrunk build still installs on a machine without the upload key.
        // Play rejects anything signed with this — it is for local testing only.
        create("releaseTest") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName(
                if (hasUploadKey) "release" else "releaseTest",
            )
        }
    }
    // Stop a debug-signed bundle before it reaches the Play Console, which rejects it.
    tasks.matching { it.name == "bundleRelease" }.configureEach {
        doFirst {
            check(hasUploadKey) { "Missing keystore/release.properties — see RELEASE.md." }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // minSdk is 24 (NFR-8) but TimeUtils and the formatters use java.time.
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // Async (tech spec 2). Pinned so the runtime and kotlinx-coroutines-test agree.
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Settings + background work
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    // DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    // Room needs a Context, so the repository rules are exercised on the JVM via
    // Robolectric rather than requiring a connected device for every run.
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)
    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.koin.test.junit4)

    // Instrumented tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

