import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifact

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

val cfgModuleId: String = providers.gradleProperty("module.id").get()
val cfgModuleName: String = providers.gradleProperty("module.name").get()
val cfgModuleAuthor: String = providers.gradleProperty("module.author").get()
val cfgModuleDescription: String = providers.gradleProperty("module.description").get()
val cfgXposedApiMin: Int = providers.gradleProperty("xposed.api.min").get().toInt()
val cfgXposedApiTarget: Int = providers.gradleProperty("xposed.api.target").get().toInt()

abstract class GenerateXposedModuleProp : DefaultTask() {
    @get:Input
    abstract val moduleId: Property<String>

    @get:Input
    abstract val moduleName: Property<String>

    @get:Input
    abstract val moduleAuthor: Property<String>

    @get:Input
    abstract val moduleDescription: Property<String>

    @get:Input
    abstract val moduleVersionName: Property<String>

    @get:Input
    abstract val moduleVersionCode: Property<Int>

    @get:Input
    abstract val moduleMinApiVersion: Property<Int>

    @get:Input
    abstract val moduleTargetApiVersion: Property<Int>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val xposedDir = outputDir.get().file("META-INF/xposed").asFile
        xposedDir.mkdirs()
        xposedDir.resolve("module.prop").writeText(
            """
            id=${moduleId.get()}
            name=${moduleName.get()}
            version=${moduleVersionName.get()}
            versionCode=${moduleVersionCode.get()}
            author=${moduleAuthor.get()}
            description=${moduleDescription.get()}
            minApiVersion=${moduleMinApiVersion.get()}
            targetApiVersion=${moduleTargetApiVersion.get()}
            staticScope=true
            exceptionMode=protective
            autoHotReload=true
            """.trimIndent() + "\n",
        )
        xposedDir.resolve("scope.list").writeText("system\n")
    }
}

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
    compileSdk = 37
    buildToolsVersion = "37.0.0"

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
            merges += "META-INF/xposed/**"
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

val generateXposedModuleProp by tasks.registering(GenerateXposedModuleProp::class) {
    moduleId.set(cfgModuleId)
    moduleName.set(cfgModuleName)
    moduleAuthor.set(cfgModuleAuthor)
    moduleDescription.set(cfgModuleDescription)
    moduleVersionName.set(android.defaultConfig.versionName ?: "unknown")
    moduleVersionCode.set(android.defaultConfig.versionCode ?: 0)
    moduleMinApiVersion.set(cfgXposedApiMin)
    moduleTargetApiVersion.set(cfgXposedApiTarget)
}

androidComponents {
    onVariants { variant ->
        variant.sources.resources?.addGeneratedSourceDirectory(
            generateXposedModuleProp,
            GenerateXposedModuleProp::outputDir,
        )

        val versionName = android.defaultConfig.versionName ?: "unknown"
        val variantTaskSuffix = variant.name.replaceFirstChar { it.uppercaseChar() }

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
