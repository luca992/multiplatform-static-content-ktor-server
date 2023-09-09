
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.2"
}

refreshVersions { // Optional: configure the plugin
}


rootProject.name = "eqoty-ktor-server"
include(":server")
