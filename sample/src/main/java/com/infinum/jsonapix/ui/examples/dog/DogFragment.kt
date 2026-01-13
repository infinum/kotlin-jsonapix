package com.infinum.jsonapix.ui.examples.dog

import androidx.fragment.app.viewModels
import com.infinum.jsonapix.R
import com.infinum.jsonapix.databinding.FragmentDogBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.shared.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DogFragment : BaseFragment<DogState, DogEvent>() {
    override val layoutRes: Int = R.layout.fragment_dog

    override val binding by viewBinding(FragmentDogBinding::bind)

    override val viewModel by viewModels<DogViewModel>()

    override fun handleState(state: DogState) = Unit

    override fun handleEvent(event: DogEvent) = Unit

    companion object {
        fun newInstance() = DogFragment()
    }
}
