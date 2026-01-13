package com.infinum.jsonapix.ui.shared

sealed interface LoadingState {
    object Idle : LoadingState

    object Loading : LoadingState
}

data class ErrorEvent(
    val message: String,
)
