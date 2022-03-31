package com.atlasv.android.diskcache

import android.content.Context
import com.atlasv.android.diskcache.DiskCacheProvider.Companion.randomFileName
import com.atlasv.android.diskcache.DiskCacheProvider.Companion.sizeOfDir
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import java.io.File

/**
 * weiping@atlasv.com
 * 2021/9/27
 */
class ConfigurableCacheDirectoryGetter(
    private val context: Context,
    private val diskCacheName: String?,
    private val internalPreferred: Boolean = false,
    private val cachePreferred: Boolean = false
) : DiskLruCacheFactory.CacheDirectoryGetter {

    private var currentDir: File? = null

    private fun getBaseInternalDir(): File? {
        return if (cachePreferred) context.cacheDir else context.filesDir
    }

    private fun getBaseExternalDir(): File? {
        return if (cachePreferred) context.externalCacheDir else context.getExternalFilesDir(
            null
        )?.takeIf { it.canWrite() }
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
        dir?.mkdirs()
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

    fun getFile(fileName: String, childDirName: String = ""): File? {
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