// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Also force on the Gradle plugin/buildscript classpath
buildscript {
    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}

subprojects {
    configurations.configureEach {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}