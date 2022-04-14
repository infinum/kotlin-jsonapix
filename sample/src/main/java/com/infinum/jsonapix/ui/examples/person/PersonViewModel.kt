package com.infinum.jsonapix.ui.examples.person

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.data.assets.JsonAssetReader
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonMeta
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
            viewState = PersonState(
                bodyString,
                person,
                person.rootLinks()?.self,
                person.resourceLinks()?.self,
                person.relationshipsLinks()?.values?.firstOrNull()?.self,
                person.meta<PersonMeta>()?.owner
            )
        }
    }

    fun fetchPersonList(hasRelationships: Boolean) {
        launch {
            showLoading()
            val bodyString: String
            val persons: List<Person>
            if (hasRelationships) {
                bodyString = io { jsonAssetReader.readJsonAsset("responses/person_list.json") }
                persons = io { sampleApiService.fetchPersons() }
            } else {
                bodyString = io { jsonAssetReader.readJsonAsset("responses/person_list_no_relationships.json") }
                persons = io { sampleApiService.fetchPersonsNoRelationships() }
            }
            hideLoading()
            viewState = PersonState(
                bodyString,
                persons.first(),
                persons.last().rootLinks()?.self,
                persons.last().resourceLinks()?.self,
                persons.first().relationshipsLinks()?.values?.firstOrNull()?.self,
                persons.first().meta<PersonMeta>()?.owner
            )
        }
    }
}
