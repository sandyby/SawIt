package com.example.sawit.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.sawit.R
import com.example.sawit.activities.CreateEditActivity
import com.example.sawit.activities.LoginActivity
import com.example.sawit.adapters.ActivitiesAdapter
import com.example.sawit.adapters.ActivitiesPagerAdapter
import com.example.sawit.databinding.FragmentActivitiesBinding
import com.example.sawit.viewmodels.ActivityViewModel
import com.example.sawit.viewmodels.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ActivitiesFragment : Fragment(R.layout.fragment_activities) {
    /**
     * menampilkan daftar aktivitas yang berkaitan dengan setiap field yang telah terdaftar oleh seorang user
     *
     * - menampilkan daftar aktivitas yang sedang direncanakan (planned) maupun sudah selesai (completed).
     * - memungkinkan pengguna menambah aktivitas baru melalui tombol FAB
     * - memfasilitasi aksi edit dan hapus aktivitas (sementara masih berupa demo/toast).
     * - memperbarui tampilan data secara real-time menggunakan ViewModel dan Kotlin Flow.
     */
    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!

    private val activityViewModel: ActivityViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentActivitiesBinding.bind(view)

        if (userViewModel.currentUser.value != null) {
            activityViewModel.listenForActivitiesUpdate()
        } else {
            Log.e("ActivitiesFragment", "User not logged in, field data listener cannot be started!")
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        if (binding.vpActivities.adapter == null) {
            setupViewPager()
        }
        setupFab()
    }

    private fun setupViewPager() {
        val adapter = ActivitiesPagerAdapter(this)
        binding.vpActivities.adapter = adapter
        binding.vpActivities.isSaveEnabled = false

        TabLayoutMediator(binding.tlActivities, binding.vpActivities) { tab, position ->
            tab.text = when (position) {
                0 -> "Planned"
                1 -> "Completed"
                else -> ""
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fabAddActivity.setOnClickListener {
            val intent = Intent(requireActivity(), CreateEditActivity::class.java)
            startActivity(intent)
        }
    }
}