plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.bugsnag) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}