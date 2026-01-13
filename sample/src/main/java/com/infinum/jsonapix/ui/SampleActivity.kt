package com.infinum.jsonapix.ui

import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.infinum.jsonapix.databinding.ActivitySampleBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.examples.company.CompanyFragment
import com.infinum.jsonapix.ui.examples.dog.DogFragment
import com.infinum.jsonapix.ui.examples.error.ErrorFragment
import com.infinum.jsonapix.ui.examples.person.PersonFragment
import com.infinum.jsonapix.ui.shared.BaseActivity
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SampleActivity : BaseActivity<Unit, Unit>() {
    override val viewModel: BaseViewModel<Unit, Unit>? = null

    override val binding by viewBinding(ActivitySampleBinding::inflate)

    override fun handleState(state: Unit) = Unit

    override fun handleEvent(event: Unit) = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.pager.adapter =
            SamplePagerAdapter(
                this,
                listOf(
                    PersonFragment.newInstance(),
                    DogFragment.newInstance(),
                    CompanyFragment.newInstance(),
                    ErrorFragment.newInstance(),
                ),
            )
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = TITLES[position]
        }.attach()
    }

    companion object {
        private val TITLES = listOf("Person", "Dog", "Company", "Error")
    }
}
