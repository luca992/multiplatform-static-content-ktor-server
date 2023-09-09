import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.io.ktor.plugin)
}

group = "io.eqoty"
version = "0.0.1"

object Targets {
    val macosTargets = arrayOf(
        "macosX64", "macosArm64",
    )
    val linuxTargets = arrayOf("linuxX64", "linuxArm64")
    val mingwTargets = arrayOf<String>()
    val nativeTargets = linuxTargets + macosTargets + mingwTargets

}

kotlin {
    jvm {
        withJava()
    }
    for (target in Targets.nativeTargets) {
        targets.add((presets.getByName(target).createTarget(target) as KotlinNativeTarget).apply {
            binaries {
                executable {
                    entryPoint = "io.eqoty.server.main"
                }
            }
        })
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.host.common)
                implementation(libs.ktor.server.cio)
                implementation(libs.squareup.okio)
                implementation(libs.logback.classic)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.tests)
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        Targets.nativeTargets.forEach { target ->
            getByName("${target}Main") {
                dependsOn(nativeMain)
            }
            getByName("${target}Test") {
                dependsOn(nativeMain)
            }
        }
    }
}

application {
    mainClass.set("io.eqoty.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}