pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.plugin.compose") {
                // Resolve the Compose compiler implementation directly from Maven Central.
                // This avoids depending on the Gradle Plugin Portal marker lookup in cold CI runners.
                useModule("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${requested.version}")
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
