import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/AtlasXV/android-libs")
            credentials {
                username = project.getEnv("GPR_USR")
                password = project.getEnv("GPR_KEY")
            }
        }
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
        classpath("com.atlasv.android.plugin:publishlib:2.2.1-alpha03")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
}

// 检测依赖更新 https://github.com/ben-manes/gradle-versions-plugin
// ./gradlew dependencyUpdates

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            setUrl("https://maven.pkg.github.com/AtlasXV/android-libs")
            credentials {
                username = project.getEnv("GPR_USR")
                password = project.getEnv("GPR_KEY")
            }
        }
        mavenLocal()
    }
}

tasks.register<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}