package com.example.caloriespedometer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2  // Number of fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PedometerFragment()
            1 -> CalorieCounterFragment()
            else -> PedometerFragment()
        }
    }
}
