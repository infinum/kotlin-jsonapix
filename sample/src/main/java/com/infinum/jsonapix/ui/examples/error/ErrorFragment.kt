package com.infinum.jsonapix.ui.examples.error

import androidx.fragment.app.viewModels
import com.infinum.jsonapix.R
import com.infinum.jsonapix.databinding.FragmentErrorBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.shared.BaseFragment

class ErrorFragment : BaseFragment<ErrorState, ErrorEvent>() {

    companion object {
        fun newInstance() = ErrorFragment()
    }

    override val layoutRes: Int = R.layout.fragment_error

    override val binding by viewBinding(FragmentErrorBinding::bind)

    override val viewModel by viewModels<ErrorViewModel>()

    override fun handleState(state: ErrorState) = Unit

    override fun handleEvent(event: ErrorEvent) = Unit
}