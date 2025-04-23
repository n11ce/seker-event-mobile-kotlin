buildscript {
    repositories {
        google() // Google repository for Android tools
        mavenCentral() // Maven Central for other dependencies
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0") // Correct version of Android Gradle Plugin
        classpath("com.google.gms:google-services:4.4.2") // Google services classpath
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:<kotlin_version>")


    }
}

plugins {
    // Applying the Android and Kotlin plugins for the project
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // Google services plugin

}
