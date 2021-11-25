package com.infinum.jsonapix.ui.examples.company

import androidx.fragment.app.viewModels
import com.infinum.jsonapix.R
import com.infinum.jsonapix.databinding.FragmentCompanyBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.shared.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompanyFragment : BaseFragment<CompanyState, CompanyEvent>() {

    companion object {
        fun newInstance() = CompanyFragment()
    }

    override val layoutRes: Int = R.layout.fragment_company

    override val binding by viewBinding(FragmentCompanyBinding::bind)

    override val viewModel by viewModels<CompanyViewModel>()

    override fun handleState(state: CompanyState) = Unit

    override fun handleEvent(event: CompanyEvent) = Unit
}