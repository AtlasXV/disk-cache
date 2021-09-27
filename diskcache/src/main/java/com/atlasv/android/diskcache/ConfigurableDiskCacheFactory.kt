package com.atlasv.android.diskcache

import android.content.Context
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory

class ConfigurableDiskCacheFactory(
    context: Context,
    diskCacheName: String?,
    internalPreferred: Boolean = false,
    cachePreferred: Boolean = false,
    diskCacheSize: Long = UNLIMITED_CACHE_SIZE
) : DiskLruCacheFactory(
    ConfigurableCacheDirectoryGetter(context, diskCacheName, internalPreferred, cachePreferred),
    diskCacheSize
) {
    companion object {
        private const val UNLIMITED_CACHE_SIZE = Long.MAX_VALUE
    }
}