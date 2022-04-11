package com.infinum.jsonapix.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<State, Event> : ViewModel() {
    private val mutableStateFlow: MutableStateFlow<State?> = MutableStateFlow(null)
    private val mutableLoadingStateFlow: MutableStateFlow<LoadingState> =
        MutableStateFlow(LoadingState.Idle)
    private val mutableEventFlow: MutableSharedFlow<Event> = MutableSharedFlow(replay = 0)
    private val mutableErrorFlow: MutableSharedFlow<ErrorEvent> = MutableSharedFlow(replay = 0)

    val stateFlow: StateFlow<State?> = mutableStateFlow
    val loadingStateFlow: StateFlow<LoadingState> = mutableLoadingStateFlow
    val eventFlow: SharedFlow<Event> = mutableEventFlow
    val errorFlow: SharedFlow<ErrorEvent> = mutableErrorFlow

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        hideLoading()
    }

    protected var viewState: State?
        get() = mutableStateFlow.value
        set(value) {
            mutableStateFlow.value = value
        }

    private var loadingState: LoadingState
        get() = mutableLoadingStateFlow.value
        set(value) {
            mutableLoadingStateFlow.value = value
        }

    protected fun emitEvent(event: Event) {
        viewModelScope.launch {
            mutableEventFlow.emit(event)
        }
    }

    protected fun showLoading() {
        loadingState = LoadingState.Loading
    }

    protected fun hideLoading() {
        loadingState = LoadingState.Idle
    }

    protected fun showError(message: String) {
        viewModelScope.launch {
            mutableErrorFlow.emit(ErrorEvent(message))
        }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Main, block = block)

    protected suspend fun <T> io(block: suspend CoroutineScope.() -> T) =
        withContext(coroutineExceptionHandler + Dispatchers.IO) { block.invoke(this) }
}
