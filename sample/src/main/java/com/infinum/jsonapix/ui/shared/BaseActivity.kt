package com.infinum.jsonapix.ui.shared

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.infinum.jsonapix.R
import com.infinum.jsonapix.ui.views.LoaderView
import kotlinx.coroutines.flow.collect

abstract class BaseActivity<State : Any, Event : Any> : AppCompatActivity() {
    protected abstract val binding: ViewBinding

    protected abstract val viewModel: BaseViewModel<State, Event>?

    private var loader: LoaderView? = null

    abstract fun handleState(state: State)
    abstract fun handleEvent(event: Event)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launchWhenCreated {
            viewModel?.stateFlow?.collect {
                if (it != null) {
                    handleState(it)
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel?.loadingStateFlow
                ?.collect { state -> handleLoading(state) }
        }

        lifecycleScope.launchWhenCreated {
            viewModel?.eventFlow
                ?.collect { event -> handleEvent(event) }
        }

        lifecycleScope.launchWhenCreated {
            viewModel?.errorFlow
                ?.collect { event -> showMessage("Error", event.message) }
        }
    }

    override fun onStop() {
        super.onStop()
        loader?.dismiss()
        loader = null
    }

    open fun handleLoading(loadingState: LoadingState) {
        when (loadingState) {
            is LoadingState.Loading -> showLoader()
            is LoadingState.Idle -> hideLoader()
        }
    }

    fun showLoader() {
        if (loader == null) {
            loader = LoaderView(this)
        }
        if (!isFinishing) {
            loader?.show()
        }
    }

    fun hideLoader() {
        if (!isFinishing) {
            loader?.dismiss()
            loader = null
        }
    }

    fun showMessage(
        title: String = getString(R.string.app_name),
        message: String,
        positiveCallback: (() -> Unit)? = null
    ) {
        showMessageDialog(
            this,
            title,
            message,
            positiveCallback
        )
    }
}
