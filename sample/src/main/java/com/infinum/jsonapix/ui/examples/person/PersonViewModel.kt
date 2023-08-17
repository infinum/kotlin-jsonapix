package com.infinum.jsonapix.ui.examples.person

import android.util.Log
import com.infinum.jsonapix.core.resources.DefaultLinks
import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.data.assets.JsonAssetReader
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.data.models.PersonItem
import com.infinum.jsonapix.data.models.PersonList
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
                val personsData: PersonList
                val persons: List<PersonItem>
                if (hasRelationships) {
                    bodyString = io { jsonAssetReader.readJsonAsset("responses/person_list.json") }
                    personsData = io { sampleApiService.fetchPersons() }
                    persons = personsData.data
                } else {
                    bodyString = io { jsonAssetReader.readJsonAsset("responses/person_list_no_relationships.json") }
                    personsData = io { sampleApiService.fetchPersonsNoRelationships() }
                    persons = personsData.data
                }
                Log.d("Person",personsData.toString())
                hideLoading()
                viewState = PersonState(
                    bodyString,
                    persons.first().data,
                    (personsData.rootLinks as DefaultLinks) .self,
                    (persons.firstOrNull()?.resourceObjectLinks as? DefaultLinks)?.self,
                    (persons.firstOrNull()?.relationshipsLinks as? Map<String, DefaultLinks>)?.values?.firstOrNull()?.self,
                    personsData.rootMeta?.owner
                )
            } catch (t: Throwable) {
                Log.e("Error", "Test", t)
            }
        }
    }
}
