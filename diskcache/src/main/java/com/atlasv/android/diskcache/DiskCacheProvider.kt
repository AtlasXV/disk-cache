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

        fun randomFileName(): String {
            return UUID.randomUUID().toString().replace("-", "")
        }

        fun sizeOfDir(dir: File): Long {
            return dir.walkBottomUp().sumOf {
                if (it.exists() && it.isFile) {
                    it.length()
                } else {
                    0
                }
            }
        }
    }
}

fun DiskCache.Factory.toProvider(): DiskCacheProvider {
    return DiskCacheProvider(this)
}