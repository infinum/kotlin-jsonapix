package com.infinum.jsonapix.ui.examples.dog

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DogViewModel @Inject constructor(
    @Suppress("UnusedPrivateMember")
    private val sampleApiService: SampleApiService,
) : BaseViewModel<DogState, DogEvent>()
