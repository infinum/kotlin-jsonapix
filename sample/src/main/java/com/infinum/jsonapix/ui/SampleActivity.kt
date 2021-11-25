package com.infinum.jsonapix.ui

import com.infinum.jsonapix.databinding.ActivitySampleBinding
import com.infinum.jsonapix.extensions.viewBinding
import com.infinum.jsonapix.ui.shared.BaseActivity
import com.infinum.jsonapix.ui.shared.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SampleActivity : BaseActivity<Unit, Unit>() {

    override val viewModel: BaseViewModel<Unit, Unit>? = null

    override val binding by viewBinding(ActivitySampleBinding::inflate)

    override fun handleState(state: Unit) = Unit

    override fun handleEvent(event: Unit) = Unit
}
