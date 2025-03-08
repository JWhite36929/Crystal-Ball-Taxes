import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.crystalballtaxes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.crystalballtaxes"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        packaging {
            resources {
                pickFirsts.add("mockito-extensions/org.mockito.plugins.MockMaker")
            }
        }
        unitTests.all {
            it.useJUnit {
                systemProperty("mockito.mock-maker-class", "mock-maker-inline")
                systemProperty("jdk.instrument.traceUsage", "false")
                systemProperty("java.util.logging.config.file", "logging.properties")
            }
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.com.google.firebase.firebase.auth2)
    implementation(libs.firebase.analytics)
    implementation(libs.espresso.intents)
    implementation(libs.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.byte.buddy)
    testImplementation(libs.byte.buddy.agent)
    testImplementation(libs.com.google.firebase.firebase.auth2)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.google.firebase.auth.ktx)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.core)
    androidTestImplementation(libs.runner.v152)
    androidTestImplementation(libs.rules.v150)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(libs.espresso.intents.v351)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.core.v150)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockito.core.v570)
    androidTestImplementation(libs.mockito.android.v570)
    androidTestImplementation(libs.dexmaker.mockito)

    configurations.all {
        resolutionStrategy {
            force("androidx.test:runner:1.5.2")
        }
    }
}