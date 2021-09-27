package com.atlasv.android.diskcache

import android.content.Context
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

    private fun ensureDir(): File? {
        val dir = currentDir ?: cacheDirectory?.also {
            currentDir = it
        }
        dir?.mkdirs()
        return dir
    }

    fun getFile(fileName: String): File? {
        val dir = ensureDir() ?: return null
        return File(dir, fileName)
    }
}