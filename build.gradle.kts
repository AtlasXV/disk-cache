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
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.atlasv.android.plugin:publishlib:1.0.0")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
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