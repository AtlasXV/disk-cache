import org.gradle.api.Project

/**
 * weiping@atlasv.com
 * 2021/8/26
 */
fun Project.getEnv(key: String): String? {
    return System.getenv(key) ?: project.findProperty(key)?.toString()
}