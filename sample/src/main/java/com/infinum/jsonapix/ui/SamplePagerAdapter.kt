package com.infinum.jsonapix.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SamplePagerAdapter(
    activity: FragmentActivity,
    private val fragments: List<Fragment>,
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
