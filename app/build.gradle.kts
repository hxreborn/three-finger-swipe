import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifact
import com.mikepenz.aboutlibraries.plugin.AboutLibrariesTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
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
    alias(libs.plugins.ktlint)
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

// Copies the AboutLibraries JSON export into the variant's raw resources directory.
abstract class PrepareAboutLibrariesTask : DefaultTask() {
    @get:InputFile
    abstract val jsonFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun prepare() {
        val rawDir = outputDir.get().dir("raw").asFile
        rawDir.mkdirs()
        jsonFile.get().asFile.copyTo(rawDir.resolve("aboutlibraries.json"), overwrite = true)
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

ktlint {
    version.set("1.8.0")
    android.set(true)
    ignoreFailures.set(false)
}

aboutLibraries {
    android {
        registerAndroidTasks.set(false)
    }
    collect {
        filterVariants.add("release")
    }
}

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

        // AboutLibraries workaround: the variant-specific task emits empty JSON so we copy
        // from the working exportLibraryDefinitions task into raw resources instead.
        val prepareTask =
            tasks.register("prepareLibraryDefinitions$variantTaskSuffix", PrepareAboutLibrariesTask::class.java) {
                jsonFile.set(layout.buildDirectory.file("generated/aboutLibraries/aboutlibraries.json"))
                outputDir.set(layout.buildDirectory.dir("generated/aboutLibraries/${variant.name}/res"))
                // exportLibraryDefinitions registered by the plugin in afterEvaluate;
                // resolve lazily here so the task graph picks it up correctly
                dependsOn(tasks.named("exportLibraryDefinitions", AboutLibrariesTask::class.java))
            }
        variant.sources.res?.addGeneratedSourceDirectory(prepareTask, PrepareAboutLibrariesTask::outputDir)
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
