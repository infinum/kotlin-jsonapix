package com.infinum.jsonapix.di

import com.infinum.jsonapix.data.assets.JsonAssetReader
import com.infinum.jsonapix.data.assets.JsonAssetReaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface JsonAssetReaderModule {
    @Binds
    fun jsonAssetReader(impl: JsonAssetReaderImpl): JsonAssetReader
}
