package com.infinum.jsonapix.ui.examples.person

import android.util.Log
import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.data.assets.JsonAssetReader
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonRootMeta
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val sampleApiService: SampleApiService,
    private val jsonAssetReader: JsonAssetReader
) : BaseViewModel<PersonState, PersonEvent>() {

    init {
        fetchPerson()
    }
    fun fetchPerson() {
        launch {
            try {

                showLoading()
                val bodyString = io { jsonAssetReader.readJsonAsset("responses/person.json") }
                val personModel = io { sampleApiService.fetchPerson() }
                val person = personModel.data
                hideLoading()
                viewState = PersonState(
                    bodyString,
                    person,
                    person.rootLinks()?.self,
                    person.resourceLinks()?.self,
                    person.relationshipsLinks()?.values?.firstOrNull()?.self,
                    person.rootMeta<PersonRootMeta>()?.owner
                )
            } catch (t: Throwable) {
                hideLoading()
                Log.e("Error", "Test", t)
            }
        }
    }

    fun fetchPersonList(hasRelationships: Boolean) {
        launch {
            try {
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
                    persons.first().rootMeta<PersonRootMeta>()?.owner
                )
            } catch (t: Throwable) {
                Log.e("Error", "Test", t)
            }
        }
    }
}
