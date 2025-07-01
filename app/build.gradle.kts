plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mcards.sdk.history.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mcards.sdk.history.demo"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        addManifestPlaceholders(mapOf("auth0Domain" to "@string/auth0_domain",
            "auth0Scheme" to "com.mcards.sdk.history.demo"))

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

    val javaVersion = JavaVersion.VERSION_17
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    implementation(platform(libs.sdk.bom))
    implementation(libs.sdk.auth)
    implementation(libs.sdk.cards)
    implementation(libs.sdk.history)
}
