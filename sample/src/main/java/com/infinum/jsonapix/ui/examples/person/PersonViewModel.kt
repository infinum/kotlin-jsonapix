package com.infinum.jsonapix.ui.examples.person

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val sampleApiService: SampleApiService
) : BaseViewModel<PersonState, PersonEvent>()