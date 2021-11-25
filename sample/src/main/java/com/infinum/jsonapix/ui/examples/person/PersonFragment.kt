package com.infinum.jsonapix.ui.examples.person

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

    override fun handleState(state: PersonState) = Unit

    override fun handleEvent(event: PersonEvent) = Unit
}