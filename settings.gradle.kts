pluginManagement {
    repositories {
        // Use the official plugin marker published for org.jetbrains.kotlin.plugin.compose.
        // Kotlin's Compose Compiler Gradle plugin is versioned with Kotlin; do not remap
        // the plugin ID to an implementation module or bypass marker resolution.
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BerkahPos"
include(":app")
