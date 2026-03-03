import com.mikepenz.aboutlibraries.plugin.AboutLibrariesTask
import org.gradle.api.tasks.Sync
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
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

        val gitHash: String =
            runCatching {
                providers
                    .exec {
                        commandLine("git", "rev-parse", "--short", "HEAD")
                    }.standardOutput.asText
                    .get()
                    .trim()
            }.getOrDefault("")

        buildConfigField("String", "GIT_HASH", "\"$gitHash\"")
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

    androidResources {
        localeFilters += "en"
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

aboutLibraries {
    registerAndroidTasks = false
    filterVariants = arrayOf("release")
}

afterEvaluate {
    // AboutLibraries' Android variant task emits an empty JSON for release in this project.
    // Reuse the working export task output and package it as generated raw resources instead.
    val exportTask = tasks.named("exportLibraryDefinitions", AboutLibrariesTask::class.java)

    extensions.findByType(com.android.build.gradle.AppExtension::class.java)?.applicationVariants?.configureEach {
        val variant = this
        val variantName = variant.name
        val variantTaskSuffix =
            variantName.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase(Locale.ENGLISH)
                } else {
                    char.toString()
                }
            }

        val generatedResDir = layout.buildDirectory.dir("generated/aboutLibraries/$variantName/res")
        val generatedRawDir = generatedResDir.map { it.dir("raw") }

        val prepareTask =
            tasks.register("prepareLibraryDefinitions$variantTaskSuffix", Sync::class.java) {
                from(layout.buildDirectory.file("generated/aboutLibraries/aboutlibraries.json"))
                into(generatedRawDir)
                dependsOn(exportTask)
            }

        variant.registerGeneratedResFolders(files(generatedResDir).builtBy(prepareTask))
        variant.mergeResourcesProvider.configure { this.dependsOn(prepareTask) }
    }
}

tasks.named("check").configure {
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

    // About
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose)
}
