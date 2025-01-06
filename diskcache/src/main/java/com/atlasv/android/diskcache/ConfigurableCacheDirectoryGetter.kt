package com.atlasv.android.diskcache

import android.content.Context
import com.atlasv.android.diskcache.DiskCacheProvider.Companion.randomFileName
import com.atlasv.android.diskcache.DiskCacheProvider.Companion.sizeOfDir
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * weiping@atlasv.com
 * 2021/9/27
 */
class ConfigurableCacheDirectoryGetter(
    private val context: Context,
    val diskCacheName: String?,
    private val internalPreferred: Boolean = false,
    private val cachePreferred: Boolean = false
) : DiskLruCacheFactory.CacheDirectoryGetter {

    private var currentDir: File? = null


    companion object {
        // 全局共享的目录变量
        private var initialized = false
        private lateinit var appContext: Context
        private var cachedCacheDir: File? = null
        private var cachedFilesDir: File? = null
        private var cachedExternalCacheDir: File? = null
        private var cachedExternalFilesDir: File? = null

        // 初始化共享变量并预加载目录
        suspend fun initialize(context: Context) {
            if (!initialized) {
                appContext = context.applicationContext
                preloadDirectories()
                initialized = true
            }
        }

        // 预加载目录，异步预加载
        private suspend fun preloadDirectories() {
            cachedCacheDir = runCatching { appContext.cacheDir }.getOrNull()
            cachedFilesDir = runCatching { appContext.filesDir }.getOrNull()
            cachedExternalCacheDir = runCatching { appContext.externalCacheDir }.getOrNull()
            cachedExternalFilesDir = runCatching { appContext.getExternalFilesDir(null) }.getOrNull()
        }

        // 获取缓存的目录
        val cacheDir: File?
            get() = cachedCacheDir ?: runCatching { appContext.cacheDir }.getOrNull()
        val filesDir: File?
            get() = cachedFilesDir ?: runCatching { appContext.filesDir }.getOrNull()
        val externalCacheDir: File?
            get() = cachedExternalCacheDir ?: runCatching { appContext.externalCacheDir }.getOrNull()
        val externalFilesDir: File?
            get() = cachedExternalFilesDir ?: runCatching { appContext.getExternalFilesDir(null) }.getOrNull()
    }

    private fun getBaseInternalDir(): File? {
        return if (cachePreferred) cacheDir else filesDir
    }

    private fun getBaseExternalDir(): File? {
        return (if (cachePreferred) externalCacheDir else externalFilesDir?.takeIf { it.canWrite() })
    }

    private fun setupDir(dir: File): File {
        return if (!diskCacheName.isNullOrEmpty()) {
            File(dir, diskCacheName)
        } else dir
    }

    override fun getCacheDirectory(): File? {
        val baseExternalDir = if (internalPreferred) null else getBaseExternalDir()
        if (baseExternalDir != null) {
            return setupDir(baseExternalDir)
        }
        return getBaseInternalDir()?.let { setupDir(it) }
    }

    fun ensureDir(): File? {
        val dir = currentDir ?: cacheDirectory?.also {
            currentDir = it
        }
        // 仅在目录不存在时才创建
        if (dir?.exists() != true) {
            dir?.mkdirs() // 只在目录不存在时创建
        }
        return dir
    }

    fun clean(): Boolean {
        return cacheDirectory?.deleteRecursively() == true
    }

    fun deleteChild(childDirName: String): Boolean {
        val childDir = cacheDirectory?.let { File(it, childDirName) } ?: return false
        if (childDir.exists()) {
            return childDir.deleteRecursively()
        }
        return false
    }

    fun createTempFile(childDirName: String = "", prefix: String = "", suffix: String = ""): File? {
        return getFile(childDirName, prefix + randomFileName() + suffix)
    }

    fun size(): Long {
        return ensureDir()?.let { sizeOfDir(it) } ?: 0
    }

    fun cloneDir(targetBaseDirName: String): Boolean {
        val originDir = ensureDir() ?: return false
        val newDir = originDir.parentFile?.let { File(it, targetBaseDirName) } ?: return false
        originDir.copyRecursively(newDir)
        return sizeOfDir(originDir) == sizeOfDir(newDir)
    }

    fun fork(
        targetBaseDirName: String,
        internalPreferred: Boolean = this.internalPreferred,
        cachePreferred: Boolean = this.cachePreferred,
        copyContent: Boolean = false
    ): ConfigurableCacheDirectoryGetter {
        if (targetBaseDirName == this.diskCacheName) {
            return this
        }
        val targetStorage = ConfigurableCacheDirectoryGetter(
            context, targetBaseDirName, internalPreferred, cachePreferred
        )
        if (copyContent) {
            kotlin.runCatching {
                ensureDir()?.copyRecursively(targetStorage.ensureDir()!!, overwrite = true)
            }
        }
        return targetStorage
    }

    fun getFile(childDirName: String = "", fileName: String): File? {
        val dir = ensureDir() ?: return null
        if (childDirName.isNotEmpty()) {
            return File(dir, childDirName).let { childDir ->
                childDir.mkdirs()
                File(childDir, fileName)
            }
        }
        return File(dir, fileName)
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun isEmpty(): Boolean {
        return ensureDir()?.list().isNullOrEmpty()
    }
}