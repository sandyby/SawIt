package com.example.sawit.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.sawit.R
import com.example.sawit.adapters.PredictionsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PredictionFragment : Fragment(R.layout.fragment_predictions) {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    companion object {
        fun newInstance(fieldId: String? = null, fieldName: String? = null) = PredictionFragment().apply {
            arguments = Bundle().apply {
                putString("fieldId", fieldId)
                putString("fieldName", fieldName)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tl_predictions)
        viewPager = view.findViewById(R.id.vp_predictions)

        val adapter = PredictionsPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Harvest"
                1 -> "Condition"
                else -> ""
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { viewPager.currentItem = it.position }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        if (arguments?.getString("fieldId") != null) {
            viewPager.post { viewPager.currentItem = 1 }
        }
    }
}
