package com.infinum.jsonapix.di

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AssetsModule {
    @Provides
    fun assets(
        @ApplicationContext context: Context,
    ): AssetManager = context.assets
}
