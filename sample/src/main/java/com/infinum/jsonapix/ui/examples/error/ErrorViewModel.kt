package com.infinum.jsonapix.ui.examples.error

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.data.assets.JsonAssetReader
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import retrofit2.HttpException

@HiltViewModel
class ErrorViewModel @Inject constructor(
    private val sampleApiService: SampleApiService,
    private val jsonAssetReader: JsonAssetReader
) : BaseViewModel<ErrorState, ErrorEvent>() {

    fun fetchError() {
        launch {
            showLoading()
            val bodyString = io { jsonAssetReader.readJsonAsset("responses/error.json") }
            try {
                val person = io { sampleApiService.fetchError() }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                showError(errorBody!!)
            }

            hideLoading()
        }
    }
}

