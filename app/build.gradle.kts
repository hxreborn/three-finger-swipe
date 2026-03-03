plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "eu.hxreborn.tfs"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "eu.hxreborn.tfs"
        minSdk = 28
        targetSdk = 36
        versionCode = project.findProperty("version.code")?.toString()?.toInt() ?: 10001
        versionName = project.findProperty("version.name")?.toString() ?: "1.0.0"
    }

    signingConfigs {
        create("release") {
            fun secret(name: String): String? =
                providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orNull

            val storeFilePath = secret("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = secret("RELEASE_STORE_PASSWORD")
                keyAlias = secret("RELEASE_KEY_ALIAS")
                keyPassword = secret("RELEASE_KEY_PASSWORD")
                storeType = secret("RELEASE_STORE_TYPE") ?: "PKCS12"
            }
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
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            pickFirsts += "META-INF/xposed/*"
            excludes += "META-INF/LICENSE*"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        disable.addAll(
            listOf(
                "PrivateApi",
                "DiscouragedPrivateApi",
                "VectorPath",
                "ViewConstructor",
                "ClickableViewAccessibility",
                "GradleDependency",
                "AndroidGradlePluginVersion",
            ),
        )
        ignoreTestSources = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    applicationVariants.all {
        outputs.forEach { output ->
            if (output is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                output.outputFileName = "tfs-v$versionName-$name.apk"
            }
        }
    }
}

kotlin { jvmToolchain(21) }

ktlint {
    version.set("1.8.0")
    android.set(true)
    ignoreFailures.set(false)
}

tasks.named("preBuild").configure {
    dependsOn("ktlintCheck")
}

dependencies {
    // libxposed
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    // AndroidX
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)

    // Navigation
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)

    // UI
    implementation(libs.lottie.compose)
    implementation(libs.compose.preferences)
}
