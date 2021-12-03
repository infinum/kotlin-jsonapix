package com.infinum.jsonapix.ui.examples.error

import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ErrorViewModel @Inject constructor() : BaseViewModel<ErrorState, ErrorEvent>()