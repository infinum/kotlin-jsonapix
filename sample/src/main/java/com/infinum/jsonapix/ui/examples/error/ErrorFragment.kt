package com.infinum.jsonapix.ui.examples.error

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.infinum.jsonapix.R
import com.infinum.jsonapix.databinding.FragmentErrorBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.shared.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ErrorFragment : BaseFragment<ErrorState, ErrorEvent>() {

    override val layoutRes: Int = R.layout.fragment_error

    override val binding by viewBinding(FragmentErrorBinding::bind)

    override val viewModel by viewModels<ErrorViewModel>()

    override fun handleState(state: ErrorState) = Unit

    override fun handleEvent(event: ErrorEvent) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.downloadButton.setOnClickListener {
            viewModel.fetchError()
        }
    }

    companion object {
        fun newInstance() = ErrorFragment()
    }
}
