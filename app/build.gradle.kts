plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.bugsnag)
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zacharee1.calculatorwidget"
        namespace = applicationId
        minSdk = 21
        targetSdk = 35
        versionCode = 4
        versionName = "1.2.1"

        vectorDrawables {
            useSupportLibrary = true
        }

        base.archivesName.set("HomeCalc_${versionCode}")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(fileTree("libs") { arrayOf("*.jar") })
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)

    implementation(libs.colorpicker.compat)
    implementation(libs.bugsnag.android)
}
