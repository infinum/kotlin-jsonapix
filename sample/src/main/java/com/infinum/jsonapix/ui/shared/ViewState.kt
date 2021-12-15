package com.infinum.jsonapix.ui.shared

sealed interface LoadingState {
    object Idle : LoadingState
    object Loading : LoadingState
}

class ErrorEvent(val message: String)
