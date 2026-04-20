plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.room)
    jacoco
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = mutableMapOf<String, String>()
versionPropsFile.readLines().forEach { line ->
    if (line.contains("=")) {
        val (key, value) = line.split("=", limit = 2)
        versionProps[key.trim()] = value.trim()
    }
}

android {
    namespace = "com.sofato.krone"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.sofato.krone"
        minSdk = 30
        targetSdk = 36
        versionCode = versionProps["VERSION_CODE"]!!.toInt()
        versionName = versionProps["VERSION_NAME"]!!

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Groups (Phase 0): donated-server pin + scheme policy.
        // Placeholders — real donated URL/fingerprint set when the server is deployed.
        buildConfigField(
            "String",
            "GROUPS_DONATED_SERVER_URL",
            "\"https://groups.krone.app\""
        )
        buildConfigField(
            "String",
            "GROUPS_DONATED_SERVER_PK_HEX",
            "\"0000000000000000000000000000000000000000000000000000000000000000\""
        )
    }

    signingConfigs {
        create("share") {
            val ksFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
        create("release") {
            val ksFile = rootProject.file("krone-release.jks")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    testCoverage {
        jacocoVersion = libs.versions.jacoco.get()
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            enableUnitTestCoverage = true
            buildConfigField("boolean", "GROUPS_ALLOW_HTTP", "true")
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "GROUPS_ALLOW_HTTP", "false")
        }
        create("share") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("share")
            matchingFallbacks += "release"
            buildConfigField("boolean", "GROUPS_ALLOW_HTTP", "false")
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("foss") {
            dimension = "distribution"
        }
        create("google") {
            dimension = "distribution"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

val coverageExclusions = listOf(
    "**/hilt_aggregated_deps/**",
    "**/*_HiltModules*.*",
    "**/*_Factory*.*",
    "**/*_MembersInjector*.*",
    "**/Dagger*.*",
    "**/*_Impl*.*",
    "**/*ComposableSingletons*.*",
    "**/*\$\$ExternalSyntheticLambda*.*",
    "**/ui/theme/**",
    "**/MainActivity*.*",
    "**/KroneApplication*.*",
    "**/di/**",
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*"
)

tasks.register<JacocoReport>("jacocoFossDebugTestReport") {
    group = "verification"
    description = "Generates JaCoCo coverage report for fossDebug unit tests"
    dependsOn("testFossDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val kotlinClasses = fileTree("${layout.buildDirectory.get()}/intermediates/built_in_kotlinc/fossDebug/compileFossDebugKotlin/classes") {
        exclude(coverageExclusions)
    }
    val javaClasses = fileTree("${layout.buildDirectory.get()}/intermediates/javac/fossDebug/compileFossDebugJavaWithJavac/classes") {
        exclude(coverageExclusions)
    }

    classDirectories.setFrom(files(kotlinClasses, javaClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include("outputs/unit_test_code_coverage/fossDebugUnitTest/*.exec")
        }
    )
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // KotlinX
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Vico Charts
    implementation(libs.vico.compose.m3)

    // Crypto (libsodium via Lazysodium). Android needs JNA as an AAR (bundles
    // native .so files); exclude the transitive JAR variant to avoid META-INF
    // resource collisions between the classes.jar inside the AAR and the JAR.
    implementation(libs.lazysodium.android) {
        exclude(group = "net.java.dev.jna", module = "jna")
    }
    implementation(libs.jna) {
        artifact { type = "aar" }
    }

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.lazysodium.java)
    testImplementation(libs.jna)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
