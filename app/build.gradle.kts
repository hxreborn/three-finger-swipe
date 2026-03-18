import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

// Renames the APK output file for a variant using the Artifacts transform API (AGP 9+).
abstract class RenameApkTask : DefaultTask() {
    @get:Internal
    abstract val transformRequest: Property<ArtifactTransformationRequest<RenameApkTask>>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val apkName: Property<String>

    @TaskAction
    fun transform() {
        transformRequest.get().submit(this) { artifact: BuiltArtifact ->
            val output = outputDir.get().file(apkName.get()).asFile
            File(artifact.outputFile).copyTo(output, overwrite = true)
            output
        }
    }
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
}

kotlin { jvmToolchain(21) }

val ktlintCli: Configuration by configurations.creating

dependencies {
    ktlintCli(libs.ktlint.cli)
}

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Check Kotlin code style"
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    args("src/**/*.kt")
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "verification"
    description = "Fix Kotlin code style violations"
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    args("-F", "src/**/*.kt")
}

val copyAboutLibraries by tasks.registering(Copy::class) {
    dependsOn("exportLibraryDefinitions")
    from("build/generated/aboutLibraries/aboutlibraries.json")
    into("build/generated/aboutLibrariesRes/raw")
}

android.sourceSets["main"]
    .res.directories
    .add("build/generated/aboutLibrariesRes")

androidComponents {
    onVariants { variant ->
        val versionName = android.defaultConfig.versionName ?: "unknown"
        val variantTaskSuffix = variant.name.replaceFirstChar { it.uppercaseChar() }

        // Rename APK output (AGP 9+ Artifacts transform API)
        val renameTask =
            tasks.register("renameApk$variantTaskSuffix", RenameApkTask::class.java) {
                apkName.set("tfs-v$versionName-${variant.name}.apk")
            }
        val request =
            variant.artifacts
                .use(renameTask)
                .wiredWithDirectories(RenameApkTask::inputDir, RenameApkTask::outputDir)
                .toTransformMany(SingleArtifact.APK)
        renameTask.configure { transformRequest.set(request) }
    }
}

tasks.named("preBuild").configure {
    dependsOn(copyAboutLibraries)
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
