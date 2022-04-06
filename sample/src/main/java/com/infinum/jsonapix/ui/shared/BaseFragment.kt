package com.infinum.jsonapix.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.infinum.jsonapix.R
import kotlinx.coroutines.flow.collect

abstract class BaseFragment<State : Any, Event : Any> : Fragment() {

    abstract val layoutRes: Int
    protected abstract val binding: ViewBinding

    protected abstract val viewModel: BaseViewModel<State, Event>?
    abstract fun handleState(state: State)
    abstract fun handleEvent(event: Event)

    private fun getBaseActivity(): BaseActivity<*, *>? = activity as? BaseActivity<*, *>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutRes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // CPD-OFF
        lifecycleScope.launchWhenCreated {
            viewModel?.stateFlow?.collect { state ->
                state?.let { handleState(it) }
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
        // CPD-ON
    }

    open fun handleLoading(loadingState: LoadingState) {
        when (loadingState) {
            is LoadingState.Loading -> showLoader()
            is LoadingState.Idle -> hideLoader()
        }
    }

    open fun showLoader() {
        if (!isDetached) {
            getBaseActivity()?.showLoader()
        }
    }

    open fun hideLoader() {
        if (!isDetached) {
            getBaseActivity()?.hideLoader()
        }
    }

    open fun showMessage(
        title: String = getString(R.string.app_name),
        message: String,
        positiveCallback: (() -> Unit)? = null
    ) {
        if (!isDetached) {
            getBaseActivity()?.showMessage(
                title,
                message,
                positiveCallback
            )
        }
    }
}
