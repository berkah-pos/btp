import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

fun String.asBuildConfigLiteral(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val candidateApplicationId = "id.co.berkahtigaputeri.btp"
val configuredApplicationId = providers.gradleProperty("btpApplicationId")
    .orNull
    ?.trim()
    ?.takeIf(String::isNotBlank)
val applicationIdValue = configuredApplicationId ?: candidateApplicationId

if (applicationIdValue != candidateApplicationId) {
    throw GradleException(
        "BTP_APPLICATION_ID must match the domain-derived permanent ID: $candidateApplicationId"
    )
}
val saasUrl = providers.gradleProperty("btpSaasUrl").orElse("").get().trim()
val supportEmail = providers.gradleProperty("btpSupportEmail").orElse("").get().trim()

require(applicationIdValue.matches(Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+"))) {
    "btpApplicationId must be a valid multi-segment Android application ID."
}
require(saasUrl.isEmpty() || saasUrl.startsWith("https://", ignoreCase = true)) {
    "btpSaasUrl must be an HTTPS URL."
}
require(supportEmail.isEmpty() || (supportEmail.contains("@") && supportEmail.none(Char::isWhitespace))) {
    "btpSupportEmail must be a valid email address."
}

val uploadKeystorePath = providers.environmentVariable("ANDROID_UPLOAD_KEYSTORE_PATH").orNull
val uploadStorePassword = providers.environmentVariable("ANDROID_UPLOAD_STORE_PASSWORD").orNull
val uploadKeyAlias = providers.environmentVariable("ANDROID_UPLOAD_KEY_ALIAS").orNull
val uploadKeyPassword = providers.environmentVariable("ANDROID_UPLOAD_KEY_PASSWORD").orNull
val signingValues = listOf(uploadKeystorePath, uploadStorePassword, uploadKeyAlias, uploadKeyPassword)
val hasAnySigningValue = signingValues.any { !it.isNullOrBlank() }
val hasCompleteSigningValues = signingValues.all { !it.isNullOrBlank() }

if (hasAnySigningValue && !hasCompleteSigningValues) {
    throw GradleException("Release signing is partially configured. Supply all four ANDROID_UPLOAD_* environment variables.")
}

android {
    namespace = "id.berkahtigaputeri.basicpos"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        // Debug defaults to the candidate. Signed release requires BTP_APPLICATION_ID to be configured.
        applicationId = applicationIdValue
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "id.berkahtigaputeri.basicpos.BerkahPosTestRunner"
        buildConfigField("String", "SAAS_URL", saasUrl.asBuildConfigLiteral())
        buildConfigField("String", "SUPPORT_EMAIL", supportEmail.asBuildConfigLiteral())
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        if (hasCompleteSigningValues) {
            create("release") {
                storeFile = file(uploadKeystorePath!!)
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasCompleteSigningValues) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    sourceSets {
        // Migration tests use a historical SQLite fixture and Room's real migration validation.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
