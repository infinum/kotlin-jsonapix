package com.infinum.jsonapix.data.assets

import android.content.res.AssetManager
import javax.inject.Inject

interface JsonAssetReader {
    fun readJsonAsset(path: String) : String
}

class JsonAssetReaderImpl @Inject constructor(
    private val assetManager: AssetManager
) : JsonAssetReader {
    override fun readJsonAsset(path: String): String {
        return assetManager.open(path).bufferedReader().readText()
    }
}