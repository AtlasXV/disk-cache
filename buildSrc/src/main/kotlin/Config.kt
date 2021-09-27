object Config {
    const val kotlinVersion = "1.5.31"

    object SdkVersions {
        const val compile = 30
        const val target = 30
        const val min = 21
    }

    object Plugins {
        const val android = "com.android.tools.build:gradle:7.0.2"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    }
}
