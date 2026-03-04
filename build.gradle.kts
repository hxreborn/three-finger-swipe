plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.ktlint) apply false
}

tasks.register<Exec>("buildLibxposedApi") {
    workingDir = file("libxposed/api")
    commandLine(
        "./gradlew",
        ":api:publishApiPublicationToMavenLocal",
        "-x",
        ":checks:compileKotlin",
        "--no-daemon",
    )
}

tasks.register<Exec>("buildLibxposedService") {
    workingDir = file("libxposed/service")
    // TODO: remove once https://github.com/libxposed/api/pull/51 is merged
    val interfaceBuild = file("libxposed/service/interface/build.gradle.kts")
    doFirst {
        interfaceBuild.writeText(
            interfaceBuild.readText().replace(
                """namespace = "io.github.libxposed.service"""",
                """namespace = "io.github.libxposed.iface"""",
            ),
        )
    }
    commandLine(
        "./gradlew",
        ":interface:publishInterfacePublicationToMavenLocal",
        ":service:publishServicePublicationToMavenLocal",
        "--no-daemon",
    )
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
