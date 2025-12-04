package com.example.sawit.adapters

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sawit.fragments.CompletedActivitiesFragment
import com.example.sawit.fragments.PlannedActivitiesFragment
import kotlin.jvm.Throws
import androidx.fragment.app.Fragment

class ActivitiesPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle) {

    companion object {
        private const val PLANNED_ID = 100L
        private const val COMPLETED_ID = 101L
    }
//    private val fragmentKeys = listOf("planned", "completed")

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PlannedActivitiesFragment()
            1 -> CompletedActivitiesFragment()
            else -> throw IllegalStateException("page ada yg misconfigure")
        }
    }

    override fun getItemId(position: Int): Long {
        return when (position) {
            0 -> PLANNED_ID
            1 -> COMPLETED_ID
            else -> super.getItemId(position)
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemId == PLANNED_ID || itemId == COMPLETED_ID
    }

}