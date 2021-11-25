package com.infinum.jsonapix.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel : ViewModel() {

    protected val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        // No-op
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(coroutineExceptionHandler + Dispatchers.Main, block = block)

    protected suspend fun <T> io(block: suspend CoroutineScope.() -> T) =
        withContext(coroutineExceptionHandler + Dispatchers.IO) { block.invoke(this) }
}