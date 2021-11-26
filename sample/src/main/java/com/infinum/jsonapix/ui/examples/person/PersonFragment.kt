package com.infinum.jsonapix.ui.examples.person

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.infinum.jsonapix.R
import com.infinum.jsonapix.databinding.FragmentPersonBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.shared.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonFragment : BaseFragment<PersonState, PersonEvent>() {

    companion object {
        fun newInstance() = PersonFragment()
    }

    override val layoutRes: Int = R.layout.fragment_person

    override val binding by viewBinding(FragmentPersonBinding::bind)

    override val viewModel by viewModels<PersonViewModel>()

    override fun handleState(state: PersonState) = with(binding) {
        responseHeader.isVisible = true
        bodyContainer.text = state.bodyString
        parsedHeader.isVisible = true
        parsedContainer.text = state.person.toString()
    }

    override fun handleEvent(event: PersonEvent) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.downloadButton.setOnClickListener {
            viewModel.fetchPerson()
        }
    }
}