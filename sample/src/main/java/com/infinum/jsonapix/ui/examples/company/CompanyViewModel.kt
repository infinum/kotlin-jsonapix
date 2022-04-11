package com.infinum.jsonapix.ui.examples.company

import com.infinum.jsonapix.data.api.SampleApiService
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompanyViewModel @Inject constructor(
    @Suppress("UnusedPrivateMember")
    private val sampleApiService: SampleApiService
) : BaseViewModel<CompanyState, CompanyEvent>()
