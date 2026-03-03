pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
        mavenLocal {
            content { includeGroup("io.github.libxposed") }
        }
    }
}

rootProject.name = "ThreeFingerSwipe"
include(":app")
