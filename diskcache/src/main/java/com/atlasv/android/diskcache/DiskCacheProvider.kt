package com.atlasv.android.diskcache

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter
import java.io.File
import java.util.*

/**
 * weiping@atlasv.com
 * 2021/9/26
 */
class DiskCacheProvider(private val factory: DiskCache.Factory) {
    private var diskCache: DiskCache? = null

    @VisibleForTesting
    @Synchronized
    fun clearDiskCacheIfCreated() {
        diskCache?.clear()
    }

    fun getDiskCache(): DiskCache {
        return diskCache ?: synchronized(this) {
            diskCache ?: createCache().also {
                diskCache = it
            }
        }
    }

    private fun createCache(): DiskCache {
        return factory.build() ?: DiskCacheAdapter()
    }

    companion object {
        private lateinit var application: Application
        fun init(app: Application) {
            application = app
        }

        private val tempDirectoryGetter by lazy {
            ConfigurableCacheDirectoryGetter(
                context = application,
                diskCacheName = "temp_disk_cache",
                internalPreferred = true,
                cachePreferred = true
            )
        }

        fun createTempFile(prefix: String = "", suffix: String = ""): File? {
            return tempDirectoryGetter.createTempFile(prefix, suffix)
        }

        fun randomFileName(): String {
            return UUID.randomUUID().toString().replace("-", "")
        }

    }
}

fun DiskCache.Factory.toProvider(): DiskCacheProvider {
    return DiskCacheProvider(this)
}