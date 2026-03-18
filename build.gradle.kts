plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.aboutlibraries) apply false
}

tasks.register<Exec>("buildLibxposedApi") {
    workingDir = file("libxposed/api")
    commandLine("./gradlew", "publishToMavenLocal", "--no-daemon")
}

tasks.register<Exec>("buildLibxposedService") {
    workingDir = file("libxposed/service")
    commandLine("./gradlew", "publishToMavenLocal", "--no-daemon")
}

tasks.register("buildLibxposed") {
    dependsOn("buildLibxposedApi", "buildLibxposedService")
}

tasks.register("assembleDebugRelease") {
    dependsOn(":app:assembleDebug", ":app:assembleRelease")
}

tasks.register("cleanBuild") {
    dependsOn("clean", "assembleDebugRelease")
}
