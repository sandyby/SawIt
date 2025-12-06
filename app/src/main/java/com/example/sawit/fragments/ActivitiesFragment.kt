package com.example.sawit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.sawit.R
import com.example.sawit.activities.CreateEditActivity
import com.example.sawit.adapters.ActivitiesAdapter
import com.example.sawit.adapters.ActivitiesPagerAdapter
import com.example.sawit.databinding.FragmentActivitiesBinding
import com.example.sawit.viewmodels.ActivityViewModel
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

    private val viewModel: ActivityViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentActivitiesBinding.bind(view)

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

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupRecyclerView()
//        setupTabLayout()
//        observeActivities()
//        setupFabListener()
//    }
//
//    private fun setupFabListener() {
//        binding.fabAddActivity.setOnClickListener {
//            val intent = Intent(requireActivity(), CreateEditActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    private fun setupRecyclerView() {
//        activitiesAdapter = ActivitiesAdapter(
//            onCheckboxClicked = { activity, isChecked ->
//                val status = if (isChecked) "Completed" else "Planned"
//                Toast.makeText(
//                    context,
//                    "${activity.fieldName} status changed to $status (Demo)",
//                    Toast.LENGTH_SHORT
//                ).show()
//            },
//            onEditClicked = { activity ->
//                val intent = Intent(requireActivity(), CreateEditActivity::class.java).apply {
//                    putExtra(CreateEditActivity.Companion.EXTRA_ACTIVITY, activity)
//                }
//                startActivity(intent)
//            },
//            onDeleteClicked = { activity ->
//                Toast.makeText(context, "Delete ${activity.fieldName} (Demo)", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        )
//
//        binding.recyclerViewActivities.apply {
//            adapter = activitiesAdapter
//            layoutManager = LinearLayoutManager(context)
//        }
//    }
//
//    private fun observeActivities() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.activities.collect { allActivities ->
//                    if (binding.tabLayout.selectedTabPosition == 0) {
//                        val plannedActivities = allActivities.filter { it.status == "planned" }
//                        activitiesAdapter.submitList(plannedActivities)
//                    } else {
//                        val completedActivities = allActivities.filter { it.status == "completed" }
//                        activitiesAdapter.submitList(completedActivities)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun setupTabLayout() {
//        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                val allActivities = viewModel.activities.value
//                when (tab?.position) {
//                    0 -> {
//                        val plannedActivities = allActivities.filter { it.status == "planned" }
//                        activitiesAdapter.submitList(plannedActivities)
//                    }
//
//                    1 -> {
//                        val completedActivities = allActivities.filter { it.status == "completed" }
//                        activitiesAdapter.submitList(completedActivities)
//                    }
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
}