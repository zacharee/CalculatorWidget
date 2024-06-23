plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.bugsnag)
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zacharee1.calculatorwidget"
        namespace = applicationId
        minSdk = 21
        targetSdk = 34
        versionCode = 3
        versionName = "1.2.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        kotlinOptions {
            jvmTarget = "17"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree("libs") { arrayOf("*.jar") })
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.preference.ktx)

    implementation(libs.colorpicker.compat)
    implementation(libs.bugsnag.android)
}
