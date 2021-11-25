package com.infinum.jsonapix.ui

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.data.models.Person
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SampleViewModel @Inject constructor(
    private val sampleApiService: SampleApiService
) : BaseViewModel() {

    private val personMutableStateFlow: MutableStateFlow<Person?> = MutableStateFlow(null)
    val personStateFlow: StateFlow<Person?> = personMutableStateFlow

    fun getPerson() {
        launch {
            val person = io { sampleApiService.getPerson() }
            personMutableStateFlow.emit(person)
        }
    }
}