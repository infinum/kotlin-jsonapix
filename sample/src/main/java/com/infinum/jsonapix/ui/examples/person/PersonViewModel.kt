package com.infinum.jsonapix.ui.examples.person

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.data.assets.JsonAssetReader
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val sampleApiService: SampleApiService,
    private val jsonAssetReader: JsonAssetReader
) : BaseViewModel<PersonState, PersonEvent>() {

    fun fetchPerson() {
        launch {
            showLoading()
            val bodyString = io { jsonAssetReader.readJsonAsset("responses/person.json") }
            val person = io { sampleApiService.fetchPerson() }
            hideLoading()
            viewState = PersonState(bodyString, person, person.rootLinks()?.self)
        }
    }
}