package com.example.sawit.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sawit.fragments.PredictionConditionFragment
import com.example.sawit.fragments.PredictionYieldFragment

class PredictionsPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PredictionYieldFragment()
            1 -> PredictionConditionFragment()
            else -> PredictionYieldFragment()
        }
    }
}