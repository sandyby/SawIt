package com.example.sawit.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sawit.fragments.FieldDetailFragment
import com.example.sawit.fragments.FieldLogsFragment
import kotlin.jvm.Throws

class FieldsPagerAdapter(
    fragment: Fragment,
    private val fieldId: String,
    private val fieldName: String,
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FieldDetailFragment.newInstance(fieldId, fieldName)
            1 -> FieldLogsFragment.newInstance(fieldId, fieldName)
            else -> throw IllegalStateException("Invalid position on the tab layout!")
        }
    }
}